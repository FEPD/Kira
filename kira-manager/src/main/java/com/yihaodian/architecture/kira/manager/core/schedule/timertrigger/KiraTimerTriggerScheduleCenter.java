/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.manager.core.schedule.timertrigger;

import com.yihaodian.architecture.kira.common.ChangedSetHolder;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneEventType;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.KiraWorkCanceledException;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerUtils;
import com.yihaodian.architecture.kira.manager.criteria.QrtzSimpleTriggersCriteria;
import com.yihaodian.architecture.kira.manager.criteria.QrtzTriggersCriteria;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneEventHandleComponent;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.domain.QrtzSimpleTriggers;
import com.yihaodian.architecture.kira.manager.domain.QrtzTriggers;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.QrtzFiredTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzSimpleTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzTriggersService;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.service.UpgradeRoadmapService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import com.yihaodian.architecture.kira.server.util.KiraServerEventHandleComponent;
import com.yihaodian.architecture.kira.server.util.KiraServerRoleEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class KiraTimerTriggerScheduleCenter extends KiraServerEventHandleComponent implements
    IKiraTimerTriggerScheduleCenter {

  private static final long NEXT_FIRE_TIME_SPAN_LIMIT_FOR_LOADBALANCE_IN_MS = 15000L; //15 seconds
  private static final int MAX_COUNT_FOR_LOADBALANCE_PER_TIME = 15;
  private static final int DEFAULT_MAXDOLOADBALANCEROUNDCOUNT = 100;
  private static final ThreadLocal<String> highPriorityWorkThreadLocal = new ThreadLocal<String>();
  private static final String HIGHPRIORITYWORK_HANDLEZONEROLECHANGE = "handleZoneRoleChange";
  private static final ThreadLocal<String> heavyWorkThreadLocal = new ThreadLocal<String>();
  private static final String HEAVYWORKNAME_DOLEADERROUTINEWORK = "doLeaderRoutineWork";
  private static final String HEAVYWORKNAME_LOADBALANCEFORKIRATIMERTRIGGERSCHEDULE = "loadBalanceForKiraTimerTriggerSchedule";
  private static final ThreadLocal<String> workThreadLocal = new ThreadLocal<String>();
  private static final String WORK_DOLOADBALANCE = "doLoadBalance";
  private final ReadWriteLock lockForScheduleRuntimeData = new ReentrantReadWriteLock();
  private UpgradeRoadmapService upgradeRoadmapService;
  private QrtzTriggersService qrtzTriggersService;
  private QrtzSimpleTriggersService qrtzSimpleTriggersService;
  private QrtzFiredTriggersService qrtzFiredTriggersService;
  private KiraClientMetadataService kiraClientMetadataService;
  private TriggerMetadataService triggerMetadataService;
  private TimerTriggerScheduleService timerTriggerScheduleService;
  //runtimeData for schedule
  private LinkedHashSet<String> assignedServerIdBlackList = new LinkedHashSet<String>();
  private Date lastUpdateAssignedServerIdBlackListTime;
  private KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole;

  public KiraTimerTriggerScheduleCenter(IKiraServer kiraServer,
      UpgradeRoadmapService upgradeRoadmapService, QrtzTriggersService qrtzTriggersService,
      QrtzSimpleTriggersService qrtzSimpleTriggersService,
      QrtzFiredTriggersService qrtzFiredTriggersService,
      KiraClientMetadataService kiraClientMetadataService,
      TriggerMetadataService triggerMetadataService,
      TimerTriggerScheduleService timerTriggerScheduleService) throws Exception {
    super(kiraServer);
    this.upgradeRoadmapService = upgradeRoadmapService;
    this.qrtzTriggersService = qrtzTriggersService;
    this.qrtzSimpleTriggersService = qrtzSimpleTriggersService;
    this.qrtzFiredTriggersService = qrtzFiredTriggersService;

    this.kiraClientMetadataService = kiraClientMetadataService;
    this.triggerMetadataService = triggerMetadataService;
    this.timerTriggerScheduleService = timerTriggerScheduleService;

    this.init();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  @Override
  public LinkedHashSet<String> getAssignedServerIdBlackList() throws Exception {
    return this.assignedServerIdBlackList;
    //return this.doGetAssignedServerIdBlackList();
  }

  @Override
  public void setAssignedServerIdBlackList(
      LinkedHashSet<String> assignedServerIdBlackList) throws Exception {
    this.doSetAssignedServerIdBlackList(assignedServerIdBlackList);
  }

  private LinkedHashSet<String> doGetAssignedServerIdBlackList() throws Exception {
    final LinkedHashSet<String> returnValue = this.assignedServerIdBlackList;
    this.doWork("doGetAssignedServerIdBlackList", "", new Callable<LinkedHashSet<String>>() {
      @Override
      public LinkedHashSet<String> call() throws Exception {
        if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
          return returnValue;
        } else {
          throw new KiraHandleException(
              "Can not to doGetAssignedServerIdBlackList for the server is not leader. kiraServerRole="
                  + kiraServerRole);
        }
      }
    }, this.lockForScheduleRuntimeData, false);

    return returnValue;
  }

  private void doSetAssignedServerIdBlackList(final LinkedHashSet<String> assignedServerIdBlackList)
      throws Exception {
    this.doWork("doSetAssignedServerIdBlackList",
        "assignedServerIdBlackList=" + assignedServerIdBlackList, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
              KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList = assignedServerIdBlackList;
              KiraTimerTriggerScheduleCenter.this.lastUpdateAssignedServerIdBlackListTime = new Date();
              return null;
            } else {
              throw new KiraHandleException(
                  "Can not to doSetAssignedServerIdBlackList for the server is not leader. kiraServerRole="
                      + kiraServerRole + " and the to be set assignedServerIdBlackList="
                      + assignedServerIdBlackList);
            }
          }
        }, this.lockForScheduleRuntimeData, true);
  }

  @Override
  public void clearAssignedServerIdBlackList() throws Exception {
    this.doClearAssignedServerIdBlackList();
  }

  private void doClearAssignedServerIdBlackList() throws Exception {
    this.doWork("doClearAssignedServerIdBlackList", "", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
          KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList.clear();
          KiraTimerTriggerScheduleCenter.this.lastUpdateAssignedServerIdBlackListTime = new Date();
          return null;
        } else {
          throw new KiraHandleException(
              "Can not to doClearAssignedServerIdBlackList for the server is not leader. kiraServerRole="
                  + kiraServerRole
                  + " and KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList="
                  + KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList);
        }
      }
    }, this.lockForScheduleRuntimeData, true);
  }

  @Override
  public void addServerIdToAssignedServerIdBlackList(String serverId) throws Exception {
    this.doAddServerIdToAssignedServerIdBlackList(serverId);
  }

  private void doAddServerIdToAssignedServerIdBlackList(final String serverId) throws Exception {
    this.doWork("doAddServerIdToAssignedServerIdBlackList", "serverId=" + serverId,
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
              KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList.add(serverId);
              KiraTimerTriggerScheduleCenter.this.lastUpdateAssignedServerIdBlackListTime = new Date();
              return null;
            } else {
              throw new KiraHandleException(
                  "Can not to doAddServerIdToAssignedServerIdBlackList for the server is not leader. kiraServerRole="
                      + kiraServerRole + " and the to be added serverId=" + serverId);
            }
          }
        }, this.lockForScheduleRuntimeData, true);
  }

  @Override
  public void removeServerIdFromAssignedServerIdBlackList(String serverId) throws Exception {
    this.doRemoveServerIdFromAssignedServerIdBlackList(serverId);
  }

  private void doRemoveServerIdFromAssignedServerIdBlackList(final String serverId)
      throws Exception {
    this.doWork("doRemoveServerIdFromAssignedServerIdBlackList", "serverId=" + serverId,
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
              KiraTimerTriggerScheduleCenter.this.assignedServerIdBlackList.remove(serverId);
              KiraTimerTriggerScheduleCenter.this.lastUpdateAssignedServerIdBlackListTime = new Date();
              return null;
            } else {
              throw new KiraHandleException(
                  "Can not to doRemoveServerIdFromAssignedServerIdBlackList for the server is not leader. kiraServerRole="
                      + kiraServerRole + " and the to be removed serverId=" + serverId);
            }
          }
        }, this.lockForScheduleRuntimeData, true);
  }

  @Override
  public Date getLastUpdateAssignedServerIdBlackListTime() throws Exception {
    return this.lastUpdateAssignedServerIdBlackListTime;
  }

  @Override
  protected void init() throws Exception {
    logger.info("Initializing KiraTimerTriggerScheduleCenter...");
    long startTime = System.currentTimeMillis();
    try {
      super.init();
      //No more to init for me now.
      logger.info("Successfully initialize KiraTimerTriggerScheduleCenter.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraTimerTriggerScheduleCenter.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraTimerTriggerScheduleCenter. And it takes " + costTime
          + " milliseconds.");
    }
  }

  @Override
  protected void startByKiraServerStartedEvent(KiraServerStartedEvent kiraServerStartedEvent)
      throws Exception {

    this.lockForScheduleRuntimeData.writeLock().lock();
    try {
      //need to see if it is kiraCrossMultiZoneRole first.
      this.kiraCrossMultiZoneRole = KiraManagerCrossMultiZoneUtils.getKiraCrossMultiZoneRole(true);
    } finally {
      this.lockForScheduleRuntimeData.writeLock().unlock();
    }

    super.startByKiraServerStartedEvent(kiraServerStartedEvent);
  }

  @Override
  protected boolean isKiraCrossMultiZoneDataChangeEventOccur(boolean throwExceptionIfNeedAbort)
      throws Exception {
    boolean returnValue = false;

    returnValue = this.isHasPendingKiraCrossMultiZoneDataChangeEvent();
    if (returnValue) {
      String message = this.getClass().getSimpleName()
          + " need to abort current operation. May KiraCrossMultiZoneDataChangeEventOccur: isHasPendingKiraCrossMultiZoneDataChangeEvent="
          + returnValue;
      logger.warn(message);
      if (throwExceptionIfNeedAbort) {
        throw new KiraWorkCanceledException(message);
      }
    }

    return returnValue;
  }

  private boolean isHasPendingKiraCrossMultiZoneDataChangeEvent() {
    boolean returnValue = false;

    try {
      Set<KiraCrossMultiZoneEventType> kiraCrossMultiZoneEventTypes = new LinkedHashSet<KiraCrossMultiZoneEventType>();
      kiraCrossMultiZoneEventTypes
          .add(KiraCrossMultiZoneEventType.KIRA_CROSS_MULTI_ZONE_DATA_CHANGED);
      returnValue = KiraManagerCrossMultiZoneEventHandleComponent
          .getKiraManagerCrossMultiZoneEventHandleComponent()
          .isHasPendingEvent(kiraCrossMultiZoneEventTypes);
    } catch (Throwable t) {
      if (null != logger) {
        logger.error("Error occurs when calling isHasPendingKiraCrossMultiZoneDataChangeEvent.", t);
      }
    }

    return returnValue;
  }

  @Override
  public void handleZoneRoleChange(final KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception {
    this.lockForComponentState.readLock().lock();
    try {
      if (this.isStarted(true)) {
        try {
          highPriorityWorkThreadLocal.set(HIGHPRIORITYWORK_HANDLEZONEROLECHANGE);

          this.doWork(HIGHPRIORITYWORK_HANDLEZONEROLECHANGE,
              "newKiraCrossMultiZoneRole=" + newKiraCrossMultiZoneRole, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                  KiraTimerTriggerScheduleCenter.this
                      .doHandleZoneRoleChange(newKiraCrossMultiZoneRole);
                  return null;
                }
              }, this.lockForScheduleRuntimeData, true);
        } finally {
          highPriorityWorkThreadLocal.remove();
        }
      } else {
        logger.warn("KiraTimerTriggerScheduleCenter is not in started state. So do not "
                + HIGHPRIORITYWORK_HANDLEZONEROLECHANGE
                + ". The zoneRole will be get correctly during the next start process. newKiraCrossMultiZoneRole={}",
            newKiraCrossMultiZoneRole);
      }
    } finally {
      this.lockForComponentState.readLock().unlock();
    }
  }

  private void doHandleZoneRoleChange(KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception {
    this.kiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;

    if (this.isLeaderServer(true)) {
      logger.warn(
          "The kiraTimerTriggerScheduleCenter will doLeaderRoutineWork for doHandleZoneRoleChange.");
      this.doLeaderRoutineWork();
    } else {
      logger.warn(
          "The kiraTimerTriggerScheduleCenter is not leader. So do not doLeaderRoutineWork for doHandleZoneRoleChange.");
    }

  }

  @Override
  protected void doLeaderRoutineWork() throws Exception {
    try {
      heavyWorkThreadLocal.set(HEAVYWORKNAME_DOLEADERROUTINEWORK);

      super.doLeaderRoutineWork();
      long startTime = System.currentTimeMillis();
      try {
        this.handleScheduleRoutineWork();
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish " + HEAVYWORKNAME_DOLEADERROUTINEWORK + " for " + this.getClass()
            .getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      heavyWorkThreadLocal.remove();
    }
  }

  /**
   * Now quartz stuff had already been migrated and discarded in kira server side. Keep the code
   * just for reference.
   */
  private void handleMigrateStuff() throws Exception {
    this.doWork("handleMigrateStuff", "", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        KiraTimerTriggerScheduleCenter.this.doHandleMigrateStuff();
        return null;
      }
    }, null, false);
  }

  private void doHandleMigrateStuff() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      this.migrateQuartzScheduleDataIfNeeded();
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish doHandleMigrateStuff for " + this.getClass().getSimpleName() + ". And it takes "
              + costTime + " milliseconds.");
    }
  }

  private void handleScheduleRoutineWork() throws Exception {
    this.doWork("handleScheduleRoutineWork", "", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        KiraTimerTriggerScheduleCenter.this.doHandleScheduleRoutineWork();
        return null;
      }
    }, this.lockForScheduleRuntimeData, true);
  }

  private void doHandleScheduleRoutineWork() throws Exception {
    long startTime = System.currentTimeMillis();

    try {
      List<String> allKiraServerIdList = new ArrayList<String>();
      List<String> allOtherKiraServerIdList = new ArrayList<String>();
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap = new HashMap<String, KiraServerEntity>();
      this.calculateKiraServerIdListWhenWorkAsLeader(allKiraServerIdList, allOtherKiraServerIdList,
          allKiraServerIdKiraServerEntityMap);

      if (null != allKiraServerIdKiraServerEntityMap) {
        Set<String> allKiraServerIdSet = allKiraServerIdKiraServerEntityMap.keySet();
        if (null != allKiraServerIdSet) {
          if (this.assignedServerIdBlackList.containsAll(allKiraServerIdSet)) {
            logger.error(
                "The assignedServerIdBlackList contains allKiraServerIdSet. So may have no kiraServer available to schedule now. For safety consideration, just do not continue to doHandleScheduleRoutineWork. assignedServerIdBlackList={} and allKiraServerIdSet={}",
                assignedServerIdBlackList, allKiraServerIdSet);
            return;
          }
        }
      }

      startTime = System.currentTimeMillis();
      try {
        this.scanAndScheduleOrUnscheduleTriggersIfNeeded(allKiraServerIdKiraServerEntityMap);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish scanAndScheduleOrUnscheduleTriggersIfNeeded for " + this.getClass()
            .getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }

      startTime = System.currentTimeMillis();
      try {
        List<TriggerIdentity> orphanTimerTriggerScheduleList = this
            .getOrphanTriggerIdentityList(allKiraServerIdList);
        this.assignOrphanTriggersToServersIfNeeded(orphanTimerTriggerScheduleList,
            allKiraServerIdList, allOtherKiraServerIdList, allKiraServerIdKiraServerEntityMap,
            true);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish assignOrphanTriggersToServersIfNeeded for " + this.getClass().getSimpleName()
                + ". And it takes " + costTime + " milliseconds.");
      }

      startTime = System.currentTimeMillis();
      try {
        this.checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded(
            allKiraServerIdKiraServerEntityMap);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded for " + this
                .getClass().getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }

//			startTime = System.currentTimeMillis();
//			try {
//				int maxDoLoadBalanceRoundCount = DEFAULT_MAXDOLOADBALANCEROUNDCOUNT;
//				this.doLoadBalance(allKiraServerIdList, allOtherKiraServerIdList, allKiraServerIdKiraServerEntityMap, maxDoLoadBalanceRoundCount, true);
//			} finally {
//				long costTime = System.currentTimeMillis() - startTime;
//				logger.warn("Finish doLoadBalance for "+this.getClass().getSimpleName()+". And it takes "+costTime + " milliseconds.");
//			}
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish doHandleScheduleRoutineWork for " + this.getClass().getSimpleName()
          + ". And it takes " + costTime + " milliseconds.");
    }
  }

  @Override
  public void loadBalanceForKiraTimerTriggerSchedule(final int maxDoLoadBalanceRoundCount)
      throws Exception {
    try {
      heavyWorkThreadLocal.set(HEAVYWORKNAME_LOADBALANCEFORKIRATIMERTRIGGERSCHEDULE);

      this.doWork(HEAVYWORKNAME_LOADBALANCEFORKIRATIMERTRIGGERSCHEDULE, "", new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          KiraTimerTriggerScheduleCenter.this
              .doLoadBalanceForKiraTimerTriggerSchedule(maxDoLoadBalanceRoundCount);
          return null;
        }
      }, this.lockForScheduleRuntimeData, true);
    } finally {
      heavyWorkThreadLocal.remove();
    }
  }

  private void doLoadBalanceForKiraTimerTriggerSchedule(int maxDoLoadBalanceRoundCount)
      throws Exception {
    if (this.isLeaderServer()) {
      long startTime = System.currentTimeMillis();

      try {
        List<String> allKiraServerIdList = new ArrayList<String>();
        List<String> allOtherKiraServerIdList = new ArrayList<String>();
        Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap = new HashMap<String, KiraServerEntity>();
        this.calculateKiraServerIdListWhenWorkAsLeader(allKiraServerIdList,
            allOtherKiraServerIdList, allKiraServerIdKiraServerEntityMap);

        if (null != allKiraServerIdKiraServerEntityMap) {
          Set<String> allKiraServerIdSet = allKiraServerIdKiraServerEntityMap.keySet();
          if (null != allKiraServerIdSet) {
            if (this.assignedServerIdBlackList.containsAll(allKiraServerIdSet)) {
              String message =
                  "The assignedServerIdBlackList contains allKiraServerIdSet. So may have no kiraServer available to schedule now. For safety consideration, just do not continue to doLoadBalanceForKiraTimerTriggerSchedule. assignedServerIdBlackList="
                      + assignedServerIdBlackList + " and allKiraServerIdSet=" + allKiraServerIdSet;
              logger.error(message);
              throw new KiraHandleException();
            }
          }
        }

        this.doLoadBalance(allKiraServerIdList, allOtherKiraServerIdList,
            allKiraServerIdKiraServerEntityMap, maxDoLoadBalanceRoundCount, true);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish doLoadBalanceForKiraTimerTriggerSchedule for {}. And it takes {} milliseconds. and maxDoLoadBalanceRoundCount={}",
            this.getClass().getSimpleName(), costTime, maxDoLoadBalanceRoundCount);
      }
    } else {
      if (null != logger) {
        logger.info("Do not doLoadBalanceForKiraTimerTriggerSchedule for i am not leader.");
      }
    }
  }

  @Override
  protected boolean isNormalOperationNeedToBeAborted(String operationSummary,
      boolean throwExceptionIfNeedAbort) throws Exception {
    boolean returnValue = false;

    returnValue = super
        .isNormalOperationNeedToBeAborted(operationSummary, throwExceptionIfNeedAbort);
    if (!returnValue) {
      String heavyWorkName = heavyWorkThreadLocal.get();
      if (StringUtils.isNotBlank(heavyWorkName)) {
        if (HEAVYWORKNAME_LOADBALANCEFORKIRATIMERTRIGGERSCHEDULE.equals(heavyWorkName)) {
          returnValue = this.isKiraCrossMultiZoneDataChangeEventOccur(throwExceptionIfNeedAbort);
          if (!returnValue) {
            String work = workThreadLocal.get();
            if (WORK_DOLOADBALANCE.equals(work)) {
              returnValue = this.isDoLoadBalanceWorkNeedToBeAborted(throwExceptionIfNeedAbort);
            }
          }
        } else if (HEAVYWORKNAME_DOLEADERROUTINEWORK.equals(heavyWorkName)) {
          returnValue = this.isKiraCrossMultiZoneDataChangeEventOccur(throwExceptionIfNeedAbort);

          if (!returnValue) {
            String highPriorityWork = highPriorityWorkThreadLocal.get();
            if (!HIGHPRIORITYWORK_HANDLEZONEROLECHANGE.equals(highPriorityWork)) {
              //It is not on the way of handleZoneRoleChange, so can block the work.

              returnValue = this.ifDoLeaderRoutineWorkNeedToBeAborted(throwExceptionIfNeedAbort);
              if (!returnValue) {
                String work = workThreadLocal.get();
                if (WORK_DOLOADBALANCE.equals(work)) {
                  returnValue = this.isDoLoadBalanceWorkNeedToBeAborted(throwExceptionIfNeedAbort);
                }
              }
            }
          }
        }
      }
    }

    return returnValue;
  }

  private boolean isDoLoadBalanceWorkNeedToBeAborted(boolean throwExceptionIfNeedAbort)
      throws Exception {
    boolean returnValue = false;

    //Need to check to see if have something block in handleTriggerZKDataChange() method or handleChildChangeForPoolOfTrigger() method of KiraTimerTriggerMetadataManager to support quick response to ui operation.
    returnValue = KiraManagerDataCenter.getKiraTimerTriggerMetadataManager()
        .isHandlingKiraTimerTriggerMetadata();
    if (returnValue) {
      String message = this.getClass().getSimpleName()
          + " need to abort current operation. May highPriorityWorkOccur: isHandlingKiraTimerTriggerMetadata="
          + returnValue;
      logger.warn(message);
      if (throwExceptionIfNeedAbort) {
        throw new KiraWorkCanceledException(message);
      }
    }
    return returnValue;
  }

  private boolean ifDoLeaderRoutineWorkNeedToBeAborted(boolean throwExceptionIfNeedAbort)
      throws Exception {
    boolean returnValue = false;

    boolean isHasPendingKiraServerChangedEvent = this.isHasPendingKiraServerChangedEvent();
    if (isHasPendingKiraServerChangedEvent) {
      String message = this.getClass().getSimpleName()
          + " need to abort doLeaderRoutineWork. isHasPendingKiraServerChangedEvent="
          + isHasPendingKiraServerChangedEvent;
      logger.warn(message);
      if (throwExceptionIfNeedAbort) {
        throw new KiraWorkCanceledException(message);
      }
    }
    return returnValue;
  }

  @Override
  protected void afterStartByKiraServerStartedEventSuccess() throws Exception {
    //Now it is ready and should be set before do some big stuffs.
    KiraManagerDataCenter.setKiraTimerTriggerScheduleCenter(this);
    super.afterStartByKiraServerStartedEventSuccess();
  }

  @Override
  protected void clearAllRunTimeData() {
    super.clearAllRunTimeData();

    //need to clear assignedServerIdBlackList.
    this.lockForScheduleRuntimeData.writeLock().lock();
    try {
      this.assignedServerIdBlackList.clear();
      this.lastUpdateAssignedServerIdBlackListTime = new Date();
    } finally {
      this.lockForScheduleRuntimeData.writeLock().unlock();
    }
  }

  @Override
  protected void afterChangeToWorkAsLeader() throws Exception {
    //need to clear assignedServerIdBlackList first.
    this.lockForScheduleRuntimeData.writeLock().lock();
    try {
      this.assignedServerIdBlackList.clear();
      this.lastUpdateAssignedServerIdBlackListTime = new Date();
    } finally {
      this.lockForScheduleRuntimeData.writeLock().unlock();
    }
    super.afterChangeToWorkAsLeader();
  }

  @Override
  protected void afterChangeToWorkAsFollower() throws Exception {
    //need to clear assignedServerIdBlackList first.
    this.lockForScheduleRuntimeData.writeLock().lock();
    try {
      this.assignedServerIdBlackList.clear();
      this.lastUpdateAssignedServerIdBlackListTime = new Date();
    } finally {
      this.lockForScheduleRuntimeData.writeLock().unlock();
    }
    super.afterChangeToWorkAsFollower();
  }

  private void checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded(
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    if (null != allKiraServerIdKiraServerEntityMap) {
      String myServerId = this.kiraServer.getServerId();
      for (Entry<String, KiraServerEntity> entry : allKiraServerIdKiraServerEntityMap.entrySet()) {
        try {
          String serverId = entry.getKey();

          List<TriggerIdentity> managedTriggerIdentityList = new ArrayList<TriggerIdentity>();
          if (myServerId.equals(serverId)) {
            managedTriggerIdentityList = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
                .getManagedTriggerIdentityList();
          } else {
            KiraServerEntity kiraServerEntity = entry.getValue();
            String accessUrlAsString = kiraServerEntity.getAccessUrlAsString();
            IClusterInternalService clusterInternalService = KiraServerUtils
                .getClusterInternalService(accessUrlAsString);
            managedTriggerIdentityList = clusterInternalService.getManagedTriggerIdentityList();
          }

          List<TriggerIdentity> assignedTriggerIdentityListByServerId = this.timerTriggerScheduleService
              .getAssignedTriggerIdentityListByServerId(serverId);
          if (CollectionUtils.isNotEmpty(assignedServerIdBlackList)) {
            if (this.assignedServerIdBlackList.contains(serverId)) {
              if (CollectionUtils.isNotEmpty(assignedTriggerIdentityListByServerId)) {
                logger.warn(
                    "The kiraServer which is in the assignedServerIdBlackList have been assigned some triggers. serverId={} and assignedServerIdBlackList={} and assignedTriggerIdentityListByServerId={}",
                    serverId, assignedServerIdBlackList, assignedTriggerIdentityListByServerId);
              }
            }
          }

          List<TriggerIdentity> triggerIdentityListWhichNeedToBeUnscheduled = (List<TriggerIdentity>) CollectionUtils
              .subtract(managedTriggerIdentityList, assignedTriggerIdentityListByServerId);
          for (TriggerIdentity triggerIdentity : triggerIdentityListWhichNeedToBeUnscheduled) {
            this.isNormalOperationNeedToBeAborted(
                "noticeServerToUnscheduleTriggerIfNeeded during checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded for "
                    + this.getClass().getSimpleName(), true);

            try {
              this.noticeServerToUnscheduleTriggerIfNeeded(triggerIdentity, serverId,
                  allKiraServerIdKiraServerEntityMap);
            } catch (Throwable t) {
              logger.error(
                  "Error occurs when noticeServerToUnscheduleTriggerIfNeeded in checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded. triggerIdentity="
                      + triggerIdentity + " and serverId=" + serverId
                      + " and allKiraServerIdKiraServerEntityMap="
                      + allKiraServerIdKiraServerEntityMap, t);
              //do not block the loop even if error occurs.
            }
          }

          List<TriggerIdentity> triggerIdentityListWhichNeedToBeRescheduled = (List<TriggerIdentity>) CollectionUtils
              .subtract(assignedTriggerIdentityListByServerId, managedTriggerIdentityList);
          for (TriggerIdentity triggerIdentity : triggerIdentityListWhichNeedToBeRescheduled) {
            this.isNormalOperationNeedToBeAborted(
                "noticeServerToRescheduleTriggerIfNeeded during checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded for "
                    + this.getClass().getSimpleName(), true);

            try {
              String appId = triggerIdentity.getAppId();
              String triggerId = triggerIdentity.getTriggerId();
              this.noticeServerToRescheduleTriggerIfNeeded(serverId, appId, triggerId,
                  allKiraServerIdKiraServerEntityMap);
            } catch (Throwable t) {
              logger.error(
                  "Error occurs when noticeServerToRescheduleTriggerIfNeeded in checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded. triggerIdentity="
                      + triggerIdentity + " and serverId=" + serverId
                      + " and allKiraServerIdKiraServerEntityMap="
                      + allKiraServerIdKiraServerEntityMap, t);
              //do not block the loop even if error occurs.
            }
          }
        } catch (KiraWorkCanceledException k) {
          logger.warn(
              "KiraWorkCanceledException occurs when handle one entry for allKiraServerIdKiraServerEntityMap in checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded. entry="
                  + entry + " and myServerId=" + myServerId, k);
          throw k;
        } catch (Throwable t) {
          logger.error(
              "Error occurs when handle one entry for allKiraServerIdKiraServerEntityMap in checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded. entry="
                  + entry + " and myServerId=" + myServerId, t);
          //do not block the loop even if error occurs.
        }
      }
    } else {
      logger.error(
          "allKiraServerIdKiraServerEntityMap is null when checkAndNoticeServersToUnscheduleAndRescheduleTriggersIfNeeded.");
    }
  }

  @Override
  protected void handleAllOtherKiraServersChangedWhenAsLeader(
      ChangedSetHolder<KiraServerEntity> allOtherKiraServersChangedSetHolder) throws Exception {
    super.handleAllOtherKiraServersChangedWhenAsLeader(allOtherKiraServersChangedSetHolder);
    this.doLeaderRoutineWork();
  }

  private void doLoadBalance(List<String> allKiraServerIdList,
      List<String> allOtherKiraServerIdList,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap,
      int maxDoLoadBalanceRoundCount, boolean throwExceptionOut) throws Exception {
    try {
      try {
        workThreadLocal.set(WORK_DOLOADBALANCE);

        boolean needToDoLoadBalanceAgain = false;
        int doLoadBalanceRoundCount = 0;
        if (maxDoLoadBalanceRoundCount <= 0) {
          maxDoLoadBalanceRoundCount = DEFAULT_MAXDOLOADBALANCEROUNDCOUNT;
        }
        do {
          try {
            List<String> assignedKiraServerIdList = new ArrayList<String>(
                CollectionUtils.subtract(allKiraServerIdList, this.assignedServerIdBlackList));
            if (CollectionUtils.isNotEmpty(assignedKiraServerIdList)) {
              Map<String, Integer> serverIdUnassignedCountMap = new HashMap<String, Integer>();
              Map<String, Integer> serverIdAssignedCountMap = new HashMap<String, Integer>();
              this.calculateServerIdUnassignedCountMapAndServerIdAssignedCountMap(
                  assignedKiraServerIdList, serverIdUnassignedCountMap, serverIdAssignedCountMap);

              LinkedHashMap<String, Integer> descSortedServerIdAssignedCountMap = KiraCommonUtils
                  .sortByValue(serverIdAssignedCountMap, true);

              boolean triggerIdentityListWhichCanBeUnassignedNowAllEmpty = true;
              boolean isLargerThanMaxCount = false;
              Map<String, List<TriggerIdentity>> serverIdTriggerIdentityListWhichCanBeUnassignedNowMap = new HashMap<String, List<TriggerIdentity>>();
              for (Map.Entry<String, Integer> entry : serverIdUnassignedCountMap.entrySet()) {
                String serverId = entry.getKey();
                Integer maxCount = entry.getValue();

                //Set minNextFireTimeInMs as small as possible for load balance between servers no matter what the nextFireTime is.
                Date nowDate = new Date();
                long milliseconds = KiraTimerTriggerScheduleCenter.NEXT_FIRE_TIME_SPAN_LIMIT_FOR_LOADBALANCE_IN_MS;
                Date minNextFireTimeDate = KiraManagerUtils
                    .getDateAfterAddMilliseconds(nowDate, milliseconds);
                Long minNextFireTimeInMs = Long.valueOf(minNextFireTimeDate.getTime());

                if (maxCount > KiraTimerTriggerScheduleCenter.MAX_COUNT_FOR_LOADBALANCE_PER_TIME) {
                  //Do not move too many per time for it may cause next fire time near to the time when moved.
                  maxCount = KiraTimerTriggerScheduleCenter.MAX_COUNT_FOR_LOADBALANCE_PER_TIME;
                  isLargerThanMaxCount = true;
                  needToDoLoadBalanceAgain = true;
                }

                //should order by next_fire_time asc for the load balance first also the record with null next_fire_time will be put last
                List<TriggerIdentity> triggerIdentityListWhichCanBeUnassignedNow = this.timerTriggerScheduleService
                    .getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(serverId,
                        minNextFireTimeInMs, maxCount);
                if (CollectionUtils.isNotEmpty(triggerIdentityListWhichCanBeUnassignedNow)) {
                  triggerIdentityListWhichCanBeUnassignedNowAllEmpty = false;
                }
                serverIdTriggerIdentityListWhichCanBeUnassignedNowMap
                    .put(serverId, triggerIdentityListWhichCanBeUnassignedNow);
              }

              if (triggerIdentityListWhichCanBeUnassignedNowAllEmpty) {
                //No need to continue
                needToDoLoadBalanceAgain = false;
              } else {
                if (!isLargerThanMaxCount) {
                  //This means it is the last time
                  needToDoLoadBalanceAgain = false;
                }

                for (Map.Entry<String, List<TriggerIdentity>> entry : serverIdTriggerIdentityListWhichCanBeUnassignedNowMap
                    .entrySet()) {
                  String unassignedServerId = entry.getKey();
                  List<TriggerIdentity> triggerIdentityListWhichCanBeUnassignedNow = entry
                      .getValue();
                  for (TriggerIdentity triggerIdentity : triggerIdentityListWhichCanBeUnassignedNow) {
                    try {
                      this.isNormalOperationNeedToBeAborted(
                          "loop through triggerIdentityListWhichCanBeUnassignedNow in doLoadBalance for "
                              + this.getClass().getSimpleName(), true);

                      boolean foundAndHandleSuccess = false;
                      String assignedServerId = null;
                      Integer remainCount = null;

                      for (Map.Entry<String, Integer> entryOfDescSortedServerIdAssignedCountMap : descSortedServerIdAssignedCountMap
                          .entrySet()) {
                        this.isNormalOperationNeedToBeAborted(
                            "loop through descSortedServerIdAssignedCountMap in doLoadBalance for "
                                + this.getClass().getSimpleName(), true);

                        assignedServerId = entryOfDescSortedServerIdAssignedCountMap.getKey();
                        remainCount = entryOfDescSortedServerIdAssignedCountMap.getValue();
                        if (remainCount.intValue() > 0) {
                          //Found the server to be assigned
                          this.unassignAndAssignTrigger(triggerIdentity, unassignedServerId,
                              assignedServerId, allKiraServerIdKiraServerEntityMap);
                          foundAndHandleSuccess = true;
                          break;
                        }
                      }
                      if (foundAndHandleSuccess) {
                        remainCount = Integer.valueOf(remainCount.intValue() - 1);
                        if (remainCount.intValue() <= 0) {
                          descSortedServerIdAssignedCountMap.remove(assignedServerId);
                        } else {
                          descSortedServerIdAssignedCountMap
                              .put(assignedServerId, Integer.valueOf(remainCount));
                          descSortedServerIdAssignedCountMap = KiraCommonUtils
                              .sortByValue(descSortedServerIdAssignedCountMap, true);
                        }
                      }
                    } catch (KiraWorkCanceledException k) {
                      logger.warn(
                          "KiraWorkCanceledException occurs when handle one entry of triggerIdentityListWhichCanBeUnassignedNow when doLoadBalance. Will do not continue with next entry and throw this exception out. triggerIdentity="
                              + triggerIdentity, k);
                      needToDoLoadBalanceAgain = false;
                      if (throwExceptionOut) {
                        throw k;
                      } else {
                        break;
                      }
                    } catch (Exception e) {
                      logger.error(
                          "Error occurs when handle one entry of triggerIdentityListWhichCanBeUnassignedNow when doLoadBalance. But will continue with next entry. triggerIdentity="
                              + triggerIdentity, e);
                      if (throwExceptionOut) {
                        throw e;
                      }
                    }
                  }
                }

                if (descSortedServerIdAssignedCountMap.size() > 0) {
                  logger.warn(
                      "descSortedServerIdAssignedCountMap is still not empty. descSortedServerIdAssignedCountMap's size={} and serverIdTriggerIdentityListWhichCanBeUnassignedNowMap={} and serverIdAssignedCountMap={} and descSortedServerIdAssignedCountMap={}",
                      descSortedServerIdAssignedCountMap.size(),
                      serverIdTriggerIdentityListWhichCanBeUnassignedNowMap,
                      serverIdAssignedCountMap, descSortedServerIdAssignedCountMap);
                }
              }
            } else {
              logger.error(
                  "assignedKiraServerIdList is empty when try to do doLoadBalance. The servers may be in the blacklist? assignedServerIdBlackList={}",
                  this.assignedServerIdBlackList);
            }
          } finally {
            doLoadBalanceRoundCount++;
            if (doLoadBalanceRoundCount > 1) {
              logger
                  .info("doLoadBalanceRoundCount={} for doLoadBalance()", doLoadBalanceRoundCount);
            }
            this.isNormalOperationNeedToBeAborted(
                "retry doLoadBalance for " + this.getClass().getSimpleName(), true);
          }
        } while (needToDoLoadBalanceAgain && doLoadBalanceRoundCount < maxDoLoadBalanceRoundCount);
      } finally {
        workThreadLocal.remove();
      }
    } catch (KiraWorkCanceledException k) {
      logger.warn("KiraWorkCanceledException occurs when doLoadBalance. allKiraServerIdList="
          + allKiraServerIdList + " and allOtherKiraServerIdList=" + allOtherKiraServerIdList
          + " and allKiraServerIdKiraServerEntityMap=" + allKiraServerIdKiraServerEntityMap
          + " and maxDoLoadBalanceRoundCount=" + maxDoLoadBalanceRoundCount, k);
      if (throwExceptionOut) {
        throw k;
      }
    } catch (Exception e) {
      //do not make doLoadBalance block normal operation
      logger.error("KiraWorkCanceledException occurs when doLoadBalance. allKiraServerIdList="
          + allKiraServerIdList + " and allOtherKiraServerIdList=" + allOtherKiraServerIdList
          + " and allKiraServerIdKiraServerEntityMap=" + allKiraServerIdKiraServerEntityMap
          + " and maxDoLoadBalanceRoundCount=" + maxDoLoadBalanceRoundCount, e);
      if (throwExceptionOut) {
        throw e;
      }
    }
  }

  private void calculateServerIdUnassignedCountMapAndServerIdAssignedCountMap(
      List<String> assignedKiraServerIdList, Map<String, Integer> serverIdUnassignedCountMap,
      Map<String, Integer> serverIdAssignedCountMap) {
    int totalCount = 0;
    Map<String, Integer> assignedServerIdAssignedCountMapInDB = this.timerTriggerScheduleService
        .getAssignedServerIdAssignedCountMap(assignedKiraServerIdList);
    if (null != assignedServerIdAssignedCountMapInDB) {
      Collection<Integer> values = assignedServerIdAssignedCountMapInDB.values();
      if (null != values) {
        for (Integer oneValue : values) {
          totalCount = totalCount + oneValue.intValue();
        }
      }
    }

    int mod = totalCount % assignedKiraServerIdList.size();
    int avg = totalCount / assignedKiraServerIdList.size();

    for (String onessignedKiraServerId : assignedKiraServerIdList) {
      Integer assignedServercount = assignedServerIdAssignedCountMapInDB
          .get(onessignedKiraServerId);
      Integer count = Integer.valueOf(0);
      if (null == assignedServercount) {
        count = Integer.valueOf(avg);
        serverIdAssignedCountMap.put(onessignedKiraServerId, count);
      } else {
        int offsetValue = assignedServercount.intValue() - avg;

        if (0 == mod) {
          if (offsetValue > 0) {
            count = Integer.valueOf(offsetValue);
            serverIdUnassignedCountMap.put(onessignedKiraServerId, count);
          } else if (offsetValue < 0) {
            count = Integer.valueOf(-offsetValue);
            serverIdAssignedCountMap.put(onessignedKiraServerId, count);
          }
        } else {
          if (offsetValue > 0) {
            if (offsetValue > 1) {
              count = Integer.valueOf(offsetValue - 1);
              serverIdUnassignedCountMap.put(onessignedKiraServerId, count);
            } else {
              //do not handle those have one more when mod!=0
            }
          } else if (offsetValue < 0) {
            count = Integer.valueOf(-offsetValue);
            serverIdAssignedCountMap.put(onessignedKiraServerId, count);
          }
        }
      }
    }
  }

  private void unassignAndAssignTrigger(TriggerIdentity triggerIdentity, String unassignedServerId,
      String assignedServerId, Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap)
      throws Exception {
    this.unassignTrigger(triggerIdentity, unassignedServerId, allKiraServerIdKiraServerEntityMap);
    this.assignTriggerToServer(triggerIdentity, assignedServerId,
        allKiraServerIdKiraServerEntityMap);
  }

  private void unassignTrigger(TriggerIdentity triggerIdentity, String assignedServerId,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    int updateCount = this.timerTriggerScheduleService
        .updateAssignedServerForTrigger(appId, triggerId, null);
    if (updateCount <= 0) {
      logger.error(
          "updateAssignedServerForTrigger return updateCount<=0. May have some unexpected problem. appId={} and triggerId={} and assignedServerId={}",
          appId, triggerId, assignedServerId);
    } else {
      if (StringUtils.isNotBlank(assignedServerId)) {
        this.noticeServerToUnscheduleTriggerIfNeeded(triggerIdentity, assignedServerId,
            allKiraServerIdKiraServerEntityMap);
      }
    }
  }

  private void assignTriggerToServer(TriggerIdentity triggerIdentity, String assignedServerId,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    int updateCount = this.timerTriggerScheduleService
        .updateAssignedServerForTrigger(appId, triggerId, assignedServerId);
    if (updateCount <= 0) {
      String errorMessage =
          "updateAssignedServerForTrigger return updateCount<=0. May have some unexpected problem. appId="
              + appId + " and triggerId=" + triggerId + " and assignedServerId=" + assignedServerId;
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    } else {
      this.noticeServerToRescheduleTriggerIfNeeded(assignedServerId, appId, triggerId,
          allKiraServerIdKiraServerEntityMap);
    }
  }

  private void assignOrphanTriggersToServersIfNeeded(
      List<TriggerIdentity> orphanTimerTriggerScheduleList, List<String> allKiraServerIdList,
      List<String> allOtherKiraServerIdList,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap,
      boolean continueWithLoopWhenExceptionOccurs) throws Exception {
    if (CollectionUtils.isNotEmpty(orphanTimerTriggerScheduleList)) {
      //Assign to all kiraServer now and all have the same weight.
      List<String> assignedKiraServerIdList = new ArrayList<String>(
          CollectionUtils.subtract(allKiraServerIdList, this.assignedServerIdBlackList));
//			if(CollectionUtils.isNotEmpty(allOtherKiraServerIdList)) {
//				assignedKiraServerIdList = allOtherKiraServerIdList;
//			}
      this.assignOrphanTriggerIdentityListToServers(orphanTimerTriggerScheduleList,
          assignedKiraServerIdList, allKiraServerIdKiraServerEntityMap,
          continueWithLoopWhenExceptionOccurs);
    }
  }

  private void assignOrphanTriggerIdentityListToServers(
      List<TriggerIdentity> orphanTimerIdentityList, List<String> assignedKiraServerIdList,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap,
      boolean continueWithLoopWhenExceptionOccurs) throws Exception {
    if (CollectionUtils.isNotEmpty(orphanTimerIdentityList)) {
      if (CollectionUtils.isNotEmpty(assignedKiraServerIdList)) {
        //need to sort by count asc so it can keep balance when add new triggers.
        LinkedHashMap<String, Integer> ascSortedAssignedServerIdAssignedCountMap = this
            .getAscSortedAssignedServerIdAssignedCountMap(assignedKiraServerIdList);

        int sizeOfOrphanTimerTriggerScheduleList = orphanTimerIdentityList.size();
        for (int i = 0; i < sizeOfOrphanTimerTriggerScheduleList; i++) {
          this.isNormalOperationNeedToBeAborted(
              "assignOrphanTriggerIdentityListToServers for " + this.getClass().getSimpleName(),
              true);

          String newAssignedServerId = null;
          TriggerIdentity triggerIdentity = null;
          try {
            newAssignedServerId = ascSortedAssignedServerIdAssignedCountMap.entrySet().iterator()
                .next().getKey();

            triggerIdentity = orphanTimerIdentityList.get(i);
            String appId = triggerIdentity.getAppId();
            String triggerId = triggerIdentity.getTriggerId();

            int updateCount = this.timerTriggerScheduleService
                .updateAssignedServerForTrigger(appId, triggerId, newAssignedServerId);
            if (updateCount <= 0) {
              logger.error(
                  "updateAssignedServerForTrigger return updateCount<=0. May have some unexpected problem. appId={} and triggerId={} and newAssignedServerId={}",
                  appId, triggerId, newAssignedServerId);
            } else {
              this.noticeServerToRescheduleTriggerIfNeeded(newAssignedServerId, appId, triggerId,
                  allKiraServerIdKiraServerEntityMap);

              Integer oldCount = ascSortedAssignedServerIdAssignedCountMap.get(newAssignedServerId);
              ascSortedAssignedServerIdAssignedCountMap
                  .put(newAssignedServerId, Integer.valueOf(oldCount.intValue() + 1));
              ascSortedAssignedServerIdAssignedCountMap = KiraCommonUtils
                  .sortByValue(ascSortedAssignedServerIdAssignedCountMap, false);
            }
          } catch (KiraWorkCanceledException k) {
            logger.warn(
                "KiraWorkCanceledException occurs when assign Orphan TriggerIdentity to Server. Will throw this exception out. newAssignedServerId="
                    + newAssignedServerId + " and triggerIdentity=" + triggerIdentity, k);
            throw k;
          } catch (Exception e) {
            logger.error(
                "Error occurs when assign Orphan TriggerIdentity to Server. newAssignedServerId="
                    + newAssignedServerId + " and triggerIdentity=" + triggerIdentity, e);
            if (!continueWithLoopWhenExceptionOccurs) {
              throw e;
            }
          }
        }
      } else {
        logger.error(
            "assignedKiraServerIdList is empty when assignOrphanTimerTriggerScheduleListToServers for orphanTimerTriggerScheduleList={} and the servers may be in the blacklist? assignedServerIdBlackList={}",
            orphanTimerIdentityList, this.assignedServerIdBlackList);
      }
    }
  }

  private void noticeServerToRescheduleTriggerIfNeeded(String assignedServerId, String poolId,
      String triggerId, Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap)
      throws Exception {
    String myServerId = this.kiraServer.getServerId();
    if (myServerId.equals(assignedServerId)) {
      KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
          .rescheduleKiraTimerTrigger(poolId, triggerId);
    } else {
      KiraServerEntity kiraServerEntity = allKiraServerIdKiraServerEntityMap.get(assignedServerId);
      if (null != kiraServerEntity) {
        String accessUrlAsString = kiraServerEntity.getAccessUrlAsString();
        IClusterInternalService clusterInternalService = KiraServerUtils
            .getClusterInternalService(accessUrlAsString);
        clusterInternalService.rescheduleKiraTimerTrigger(poolId, triggerId);
      } else {
        String errorMessage = "Can not get kiraServerEntity by assignedServerId=" + assignedServerId
            + " during noticeServerToRescheduleTriggerIfNeeded. May have some bugs. poolId="
            + poolId + " and triggerId=" + triggerId + " and allKiraServerIdKiraServerEntityMap="
            + allKiraServerIdKiraServerEntityMap;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    }
  }

  private LinkedHashMap<String, Integer> getAscSortedAssignedServerIdAssignedCountMap(
      List<String> assignedKiraServerIdList) {
    LinkedHashMap<String, Integer> returnValue = new LinkedHashMap<String, Integer>();
    if (CollectionUtils.isNotEmpty(assignedKiraServerIdList)) {
      Map<String, Integer> assignedServerIdAssignedCountMapInDB = this.timerTriggerScheduleService
          .getAssignedServerIdAssignedCountMap(assignedKiraServerIdList);

      for (String assignedServerId : assignedKiraServerIdList) {
        Integer count = 0;
        Object object = assignedServerIdAssignedCountMapInDB.get(assignedServerId);
        if (object instanceof Integer) {
          count = (Integer) object;
        }else if (object instanceof HashMap){
          count = (Integer) ((HashMap)object).get("assignedCount");
        }
        if (null == count) {
          returnValue.put(assignedServerId, Integer.valueOf(0));
        } else {
          returnValue.put(assignedServerId, count);
        }
      }
      returnValue = KiraCommonUtils.sortByValue(returnValue, false);
    }

    return returnValue;
  }

  private List<TriggerIdentity> getOrphanTriggerIdentityList(List<String> allKiraServerIdList) {
    TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    List<String> excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId = new ArrayList<String>(
        CollectionUtils.subtract(allKiraServerIdList, this.assignedServerIdBlackList));
    timerTriggerScheduleCriteria
        .setExcludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId(
            excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId);
    //should order by next_fire_time asc so the latest will be handled first also the record with null next_fire_time will be put last
    timerTriggerScheduleCriteria
        .setOrderByClause("IF(ISNULL(next_fire_time),1,0),next_fire_time asc");
    List<TriggerIdentity> returnValue = this.timerTriggerScheduleService
        .getTriggerIdentityList(timerTriggerScheduleCriteria);
    return returnValue;
  }

  private void calculateKiraServerIdListWhenWorkAsLeader(List<String> allKiraServerIdList,
      List<String> allOtherKiraServerIdList,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) {
    allKiraServerIdList.add(this.kiraServer.getServerId());
    allKiraServerIdKiraServerEntityMap
        .put(this.kiraServer.getServerId(), this.kiraServer.getKiraServerEntity());
    if (CollectionUtils.isNotEmpty(this.allOtherKiraServers)) {
      for (KiraServerEntity kiraServerEntity : this.allOtherKiraServers) {
        String serverId = kiraServerEntity.getServerId();
        allKiraServerIdList.add(serverId);
        allOtherKiraServerIdList.add(serverId);
        allKiraServerIdKiraServerEntityMap.put(serverId, kiraServerEntity);
      }
    }
  }

  private void scanAndScheduleOrUnscheduleTriggersIfNeeded(
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    //TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    //List<TriggerIdentity> allCanBeScheduledTriggerIdentityListInTriggerMetadataTable = this.triggerMetadataService.getAllCanBeScheduledTriggerIdentityInDB(false);
    //List<TriggerIdentity> allTriggerIdentityListInTimerTriggerScheduleTable = this.timerTriggerScheduleService.getTriggerIdentityList(timerTriggerScheduleCriteria);

    //List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled = (List<TriggerIdentity>) CollectionUtils.subtract(allTriggerIdentityListInTimerTriggerScheduleTable, allCanBeScheduledTriggerIdentityListInTriggerMetadataTable);
    boolean masterZone = KiraCrossMultiZoneUtils.isMasterZone(this.kiraCrossMultiZoneRole);
    List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled = this.triggerMetadataService
        .getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(Boolean.valueOf(masterZone));
    this.handleTriggerIdentityListWhichShouldBeUnScheduled(
        triggerIdentityListWhichShouldBeUnScheduled, allKiraServerIdKiraServerEntityMap);

    //List<TriggerIdentity> triggerIdentityListWhichShouldBeScheduled = (List<TriggerIdentity>) CollectionUtils.subtract(allCanBeScheduledTriggerIdentityListInTriggerMetadataTable, allTriggerIdentityListInTimerTriggerScheduleTable);
    List<TriggerIdentity> triggerIdentityListWhichShouldBeScheduled = this.triggerMetadataService
        .getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(Boolean.valueOf(masterZone));
    this.handleTriggerIdentityListWhichShouldBeScheduled(triggerIdentityListWhichShouldBeScheduled);
  }

  private void handleTriggerIdentityListWhichShouldBeUnScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    if (CollectionUtils.isNotEmpty(triggerIdentityListWhichShouldBeUnScheduled)) {
      for (TriggerIdentity triggerIdentity : triggerIdentityListWhichShouldBeUnScheduled) {
        this.isNormalOperationNeedToBeAborted(
            "handleTriggerIdentityListWhichShouldBeUnScheduled for " + this.getClass()
                .getSimpleName(), true);

        this.unscheduleTriggerByTriggerIdentity(triggerIdentity, null,
            allKiraServerIdKiraServerEntityMap);
      }
    }
  }

  private void unscheduleTriggerByTriggerIdentity(TriggerIdentity triggerIdentity,
      String assignedServerIdToBeChecked,
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap) throws Exception {
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    TimerTriggerSchedule timerTriggerSchedule = this.timerTriggerScheduleService
        .getTimerTriggerScheduleByPoolIdAndTriggerId(appId, triggerId);
    if (null != timerTriggerSchedule) {
      String assignedServerId = timerTriggerSchedule.getAssignedServerId();
      if (StringUtils.isNotBlank(assignedServerIdToBeChecked)) {
        if (!StringUtils.equals(assignedServerIdToBeChecked, assignedServerId)) {
          logger.error(
              "Inconsistent detedted! May have some bugs. The assignedServerIdToBeChecked is not equals with the assignedServerId of timerTriggerSchedule. assignedServerIdToBeChecked={} and timerTriggerSchedule.getAssignedServerId()={}",
              assignedServerIdToBeChecked, assignedServerId);
        }
      }

      //delete the unscheduled trigger in TimerTriggerSchedule table first then unschedule them.
      int deletedCount = this.timerTriggerScheduleService.delete(timerTriggerSchedule.getId());
      if (StringUtils.isNotBlank(assignedServerId)) {
        this.noticeServerToUnscheduleTriggerIfNeeded(triggerIdentity, assignedServerId,
            allKiraServerIdKiraServerEntityMap);
      }
    }
  }

  private void noticeServerToUnscheduleTriggerIfNeeded(TriggerIdentity triggerIdentity,
      String assignedServerId, Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap)
      throws Exception {
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    if (StringUtils.isNotBlank(assignedServerId)) {
      String myServerId = this.kiraServer.getServerId();

      if (assignedServerId.equals(myServerId)) {
        //handled locally
        KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
            .unscheduleKiraTimerTrigger(appId, triggerId);
      } else {
        //need to notice the assigned server to unschedule trigger.
        //KiraServerEntity kiraServerEntity = this.kiraServer.getKiraServerEntityByServerId(assignedServerId);
        KiraServerEntity kiraServerEntity = allKiraServerIdKiraServerEntityMap
            .get(assignedServerId);
        if (null != kiraServerEntity) {
          String accessUrlAsString = kiraServerEntity.getAccessUrlAsString();
          IClusterInternalService clusterInternalService = KiraServerUtils
              .getClusterInternalService(accessUrlAsString);
          clusterInternalService.unscheduleKiraTimerTrigger(appId, triggerId);
        } else {
          String warnMessage =
              "Can not get kiraServerEntity by assignedServerId=" + assignedServerId
                  + " during noticeServerToUnscheduleTriggerIfNeeded. triggerIdentity="
                  + triggerIdentity + " and allKiraServerIdKiraServerEntityMap="
                  + allKiraServerIdKiraServerEntityMap;
          logger.warn(warnMessage);
          //The server may do not in the cluster now. so just ignore it.
          //throw new KiraHandleException(warnMessage);
        }
      }
    }
  }

  private void handleTriggerIdentityListWhichShouldBeScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeScheduled) throws Exception {
    if (CollectionUtils.isNotEmpty(triggerIdentityListWhichShouldBeScheduled)) {
      this.isNormalOperationNeedToBeAborted(
          "handleTriggerIdentityListWhichShouldBeScheduled for " + this.getClass().getSimpleName(),
          true);

      this.timerTriggerScheduleService
          .insertByTriggerIdentityList(triggerIdentityListWhichShouldBeScheduled);
    }
  }

  private void migrateQuartzScheduleDataIfNeeded() throws Exception {
    boolean isHasMigrateQuartzScheduleDataSuccessRecord = this.upgradeRoadmapService
        .isHasMigrateQuartzScheduleDataSuccessRecord();
    if (!isHasMigrateQuartzScheduleDataSuccessRecord) {
      logger.info(
          "no migrateQuartzScheduleDataSuccess record found. So need to migrateQuartzScheduleData now.");
      this.doMigrateQuartzScheduleData();
    } else {
      //Now quartz stuff had already been migrated and discarded in kira server side. Keep the code just for reference.
      //KiraManagerDataCenter.setQuartzScheduleDataMigrated(true);
    }
  }

  private void doMigrateQuartzScheduleData() throws Exception {
    logger.info("Start doMigrateQuartzScheduleData...");
    long startHandleTime = System.currentTimeMillis();
    try {
      upgradeRoadmapService.insertMigratingQuartzScheduleDataRecord();

      QrtzTriggersCriteria qrtzTriggersCriteria = new QrtzTriggersCriteria();
      List<QrtzTriggers> qrtzTriggersList = qrtzTriggersService
          .listCanBeScheduled(qrtzTriggersCriteria);
      if (CollectionUtils.isNotEmpty(qrtzTriggersList)) {
        TimerTriggerSchedule timerTriggerSchedule = null;
        for (QrtzTriggers qrtzTrigger : qrtzTriggersList) {
          this.isNormalOperationNeedToBeAborted(
              "doMigrateQuartzScheduleData for " + this.getClass().getSimpleName(), true);

          timerTriggerSchedule = new TimerTriggerSchedule();

          String appId = qrtzTrigger.getJobGroup();
          String triggerId = qrtzTrigger.getJobName();
          Long startTime = qrtzTrigger.getStartTime();
          Long prevFireTime = qrtzTrigger.getPrevFireTime();
          Long nextFireTime = qrtzTrigger.getNextFireTime();

          timerTriggerSchedule.setAppId(appId);
          timerTriggerSchedule.setTriggerId(triggerId);
          timerTriggerSchedule.setStartTime(startTime);

          if (null != prevFireTime && -1 != prevFireTime.longValue()) {
            //prevFireTime is not null and not -1 means it has been fired before.
            timerTriggerSchedule.setPreviousFireTime(prevFireTime);
          }

          timerTriggerSchedule.setNextFireTime(nextFireTime);

          QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria = new QrtzSimpleTriggersCriteria();
          qrtzSimpleTriggersCriteria.setTriggerGroup(appId);
          qrtzSimpleTriggersCriteria.setTriggerName(triggerId);
          List<QrtzSimpleTriggers> qrtzSimpleTriggersList = qrtzSimpleTriggersService
              .list(qrtzSimpleTriggersCriteria);
          if (CollectionUtils.isNotEmpty(qrtzSimpleTriggersList)) {
            QrtzSimpleTriggers qrtzSimpleTrigger = qrtzSimpleTriggersList.get(0);
            Integer timesTriggered = qrtzSimpleTrigger.getTimesTriggered();
            timerTriggerSchedule.setTimesTriggered(Long.valueOf(timesTriggered.intValue()));
          }
          Long timesTriggered = timerTriggerSchedule.getTimesTriggered();
          if (null == timesTriggered) {
            if (null != timerTriggerSchedule.getPreviousFireTime()) {
              //If prevFireTime is not null and not -1, just set it to 1
              timerTriggerSchedule.setTimesTriggered(Long.valueOf(1));
            } else {
              timerTriggerSchedule.setTimesTriggered(Long.valueOf(0));
            }
          }

          timerTriggerSchedule.setCreateTime(Long.valueOf(new Date().getTime()));

          timerTriggerScheduleService.saveByPoolIdAndTriggerId(timerTriggerSchedule);
        }
      }

      upgradeRoadmapService.insertMigrateQuartzScheduleDataSuccessRecord();
      //Now quartz stuff had already been migrated and discarded in kira server side. Keep the code just for reference.
      //KiraManagerDataCenter.setQuartzScheduleDataMigrated(true);
      logger.info("Successfully doMigrateQuartzScheduleData.");
    } catch (Exception e) {
      logger.error("Error occurs when doMigrateQuartzScheduleData", e);
      String exceptionDesc = ExceptionUtils.getFullStackTrace(e);
      upgradeRoadmapService.insertMigrateQuartzScheduleDataFailedRecord(
          "Caught exception when doMigrateQuartzScheduleData: " + exceptionDesc);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startHandleTime;
      logger
          .info("Finish doMigrateQuartzScheduleData. And it takes " + costTime + " milliseconds.");

    }
  }

  @Override
  public void initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(final String appId,
      final String triggerId) throws Exception {
    final KiraServerRoleEnum kiraServerRole = this.kiraServerRole;
    this.doWork("initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger",
        "appId=" + appId + " and triggerId=" + triggerId, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
              KiraTimerTriggerScheduleCenter.this
                  .doInitTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(appId, triggerId);
              return null;
            } else {
              throw new KiraHandleException(
                  "Can not to doInitTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger for the server is not leader. kiraServerRole="
                      + kiraServerRole + " and appId=" + appId + " and triggerId=" + triggerId);
            }
          }
        }, this.lockForScheduleRuntimeData, true);
  }

  private void doInitTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(String appId,
      String triggerId) throws Exception {
    TriggerMetadata triggerMetadata = this.triggerMetadataService
        .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
    if (null != triggerMetadata) {
      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
      List<String> allKiraServerIdList = new ArrayList<String>();
      List<String> allOtherKiraServerIdList = new ArrayList<String>();
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap = new HashMap<String, KiraServerEntity>();
      this.calculateKiraServerIdListWhenWorkAsLeader(allKiraServerIdList, allOtherKiraServerIdList,
          allKiraServerIdKiraServerEntityMap);

      this.unscheduleTriggerByTriggerIdentity(triggerIdentity, null,
          allKiraServerIdKiraServerEntityMap);

      boolean masterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
      if (KiraCommonUtils.isCanBeScheduled(triggerMetadata, masterZone)) {
        List<TriggerIdentity> triggerIdentityList = new ArrayList<TriggerIdentity>();
        triggerIdentityList.add(triggerIdentity);
        this.handleTriggerIdentityListWhichShouldBeScheduled(triggerIdentityList);
        this.assignOrphanTriggersToServersIfNeeded(triggerIdentityList, allKiraServerIdList,
            allOtherKiraServerIdList, allKiraServerIdKiraServerEntityMap, false);
      } else {
        String warnMessage =
            "This trigger may can not be scheduled now. appId=" + appId + " and triggerId="
                + triggerId + "masterZone=" + masterZone;
        logger.warn(warnMessage);
      }
    } else {
      throw new KiraHandleException(
          "Can not get triggerMetadata for doRescheduleKiraTimerTrigger by appId=" + appId
              + " and triggerId=" + triggerId);
    }
  }

  @Override
  public void handleTriggerIdentityListWhichShouldBeUnScheduled(
      final List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled)
      throws Exception {
    if (CollectionUtils.isNotEmpty(triggerIdentityListWhichShouldBeUnScheduled)) {
      final KiraServerRoleEnum kiraServerRole = this.kiraServerRole;
      this.doWork("handleTriggerIdentityListWhichShouldBeUnScheduled",
          "triggerIdentityListWhichShouldBeUnScheduled="
              + triggerIdentityListWhichShouldBeUnScheduled, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              if (KiraTimerTriggerScheduleCenter.this.isLeaderServer()) {
                KiraTimerTriggerScheduleCenter.this
                    .doHandleTriggerIdentityListWhichShouldBeUnScheduled(
                        triggerIdentityListWhichShouldBeUnScheduled);
                return null;
              } else {
                throw new KiraHandleException(
                    "Can not to handleTriggerIdentityListWhichShouldBeUnScheduled for the server is not leader. kiraServerRole="
                        + kiraServerRole + " and triggerIdentityListWhichShouldBeUnScheduled="
                        + triggerIdentityListWhichShouldBeUnScheduled);
              }
            }
          }, this.lockForScheduleRuntimeData, true);
    }
  }

  protected void doHandleTriggerIdentityListWhichShouldBeUnScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled) throws Exception {
    if (CollectionUtils.isNotEmpty(triggerIdentityListWhichShouldBeUnScheduled)) {
      List<String> allKiraServerIdList = new ArrayList<String>();
      List<String> allOtherKiraServerIdList = new ArrayList<String>();
      Map<String, KiraServerEntity> allKiraServerIdKiraServerEntityMap = new HashMap<String, KiraServerEntity>();
      this.calculateKiraServerIdListWhenWorkAsLeader(allKiraServerIdList, allOtherKiraServerIdList,
          allKiraServerIdKiraServerEntityMap);

      this.handleTriggerIdentityListWhichShouldBeUnScheduled(
          triggerIdentityListWhichShouldBeUnScheduled, allKiraServerIdKiraServerEntityMap);
    }
  }

}
