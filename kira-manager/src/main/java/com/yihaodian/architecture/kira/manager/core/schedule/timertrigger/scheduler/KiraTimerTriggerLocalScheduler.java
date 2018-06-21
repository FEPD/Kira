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
package com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.scheduler;

import com.caucho.hessian.client.HessianRuntimeException;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.util.FiringTimerTriggerZNodeData;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.util.LocalSchedulerStartedZNodeData;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerUtils;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.event.RunTimerTriggerTaskFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.RunTimerTriggerTaskRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.TimerTriggerScheduleFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.TimerTriggerScheduleRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerConstants;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.schedule.time.callback.KiraTimeScheduleCallbackAdaptor;
import com.yihaodian.architecture.kira.schedule.time.scheduler.ITimerTriggerScheduler;
import com.yihaodian.architecture.kira.schedule.time.scheduler.KiraTimerTriggerScheduler;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.utils.TimeScheduleUtils;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import com.yihaodian.architecture.kira.server.util.KiraServerEventHandleComponent;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraTimerTriggerLocalScheduler extends KiraServerEventHandleComponent implements
    IKiraTimerTriggerLocalScheduler {

  private final ReadWriteLock lockForTimerTriggerScheduler = new ReentrantReadWriteLock();
  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  //runtimeData for schedule
  protected ThreadPoolExecutor timerTriggerTaskHandleThreadPoolExecutor;
  protected MonitorContext runTimerTriggerTaskMonitorContext = new MonitorContext(
      "Run TimerTriggerTask", "");
  protected MonitorContext timerTriggerScheduleMonitorContext = new MonitorContext(
      "Schedule TimerTrigger", "");
  private KiraClientMetadataService kiraClientMetadataService;
  private TriggerMetadataService triggerMetadataService;
  private TimerTriggerScheduleService timerTriggerScheduleService;
  private ScheduledExecutorService scheduledExecutorService;
  private ITimerTriggerScheduler timerTriggerScheduler;

  public KiraTimerTriggerLocalScheduler(IKiraServer kiraServer,
      KiraClientMetadataService kiraClientMetadataService,
      TriggerMetadataService triggerMetadataService,
      TimerTriggerScheduleService timerTriggerScheduleService)
      throws Exception {
    super(kiraServer);
    this.kiraClientMetadataService = kiraClientMetadataService;
    this.triggerMetadataService = triggerMetadataService;
    this.timerTriggerScheduleService = timerTriggerScheduleService;

    this.init();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void init() throws Exception {
    logger.info("Initializing KiraTimerTriggerLocalScheduler...");
    long startTime = System.currentTimeMillis();
    try {
      super.init();
      logger.info("Successfully initialize KiraTimerTriggerLocalScheduler.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraTimerTriggerLocalScheduler.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraTimerTriggerLocalScheduler. And it takes " + costTime
          + " milliseconds.");
    }
  }

  private void startTimerTriggerRecoveryMonitorIfNeeded() throws Exception {
    if (null != this.scheduledExecutorService) {
      this.scheduledExecutorService.shutdown();
    }

    String threadNamePrefix = "TimerTriggerRecoveryMonitorThread-";
    CustomizedThreadFactory customizedThreadFactory = new CustomizedThreadFactory(threadNamePrefix);
    this.scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(customizedThreadFactory);
    //scheduled to run every 1 minute after 1 minute delay
    this.scheduledExecutorService
        .scheduleAtFixedRate(new TimerTriggerRecoveryMonitorTask(this), 1L, 1L, TimeUnit.MINUTES);
  }

  @Override
  protected void startByKiraServerStartedEvent(KiraServerStartedEvent kiraServerStartedEvent)
      throws Exception {
    super.startByKiraServerStartedEvent(kiraServerStartedEvent);
    this.prepareForTimerTriggerScheduler();
  }

  private String prepareForTimerTriggerBattlefieldIfNeeded(boolean recoverForTimerTriggerEnabled,
      String serverId, Date lastStartedTime) throws Exception {
    String returnValue = null;

    if (recoverForTimerTriggerEnabled) {
      if (!zkClient.exists(KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD)) {
        try {
          zkClient.createPersistent(KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD, true);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }

      if (StringUtils.isNotBlank(serverId)) {
        String serverIdForTimerTriggerBattlefieldZNodeZKPathZNodeZKPath =
            KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD
                + KiraCommonConstants.ZNODE_NAME_PREFIX + KiraUtil.filterString(serverId);
        if (!zkClient.exists(serverIdForTimerTriggerBattlefieldZNodeZKPathZNodeZKPath)) {
          try {
            zkClient.createPersistent(serverIdForTimerTriggerBattlefieldZNodeZKPathZNodeZKPath,
                serverId);
          } catch (ZkNodeExistsException nodeExistsException) {
            logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                + ". Just ignore this exception. This node may be created by someone.");
          }
        }

        String lastStartedTimeAsString = null;
        if (null != lastStartedTime) {
          lastStartedTimeAsString = KiraCommonUtils.getDateAsString(lastStartedTime,
              KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION);

          if (StringUtils.isNotBlank(lastStartedTimeAsString)) {
            String localSchedulerZNodeZKPath =
                serverIdForTimerTriggerBattlefieldZNodeZKPathZNodeZKPath
                    + KiraCommonConstants.ZNODE_NAME_PREFIX + KiraUtil
                    .filterString(lastStartedTimeAsString);
            if (!zkClient.exists(localSchedulerZNodeZKPath)) {
              try {
                KiraServerEntity kiraServerEntity = this.kiraServer.getKiraServerEntity();
                if (null != kiraServerEntity) {
                  LocalSchedulerStartedZNodeData localSchedulerStartedZNodeData = new LocalSchedulerStartedZNodeData(
                      lastStartedTime, kiraServerEntity);
                  zkClient
                      .createPersistent(localSchedulerZNodeZKPath, localSchedulerStartedZNodeData);
                } else {
                  logger.error(
                      "kiraServerEntity is null when prepareForTimerTriggerBattlefieldIfNeeded. May have some bugs.");
                }
              } catch (ZkNodeExistsException nodeExistsException) {
                logger.info(
                    "ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                        + ". This node may be created by someone.");
              }
            }

            returnValue = localSchedulerZNodeZKPath;
          } else {
            logger.warn(
                "lastStartedTimeAsString is blank when prepareForTimerTriggerBattlefieldIfNeeded. So do not create it on zk.");
          }
        } else {
          logger.warn(
              "lastStartedTime is null. The server may have not be started or may have some bugs.");
        }
      } else {
        logger.warn("return null for KiraManagerDataCenter.getServerId(). May have some bugs.");
      }
    } else {
      logger.warn(
          "recoverForTimerTriggerEnabled is not enabled. so do not continue with prepareForTimerTriggerBattlefieldIfNeeded.");
    }

    return returnValue;
  }

  private void prepareForTimerTriggerScheduler() throws Exception {
    lockForTimerTriggerScheduler.writeLock().lock();
    try {
      this.prepareForTimerTriggerTaskHandleThreadPoolExecutor();
      this.restartKiraTimerTriggerSchedulerAndCreateItIfNeeded();
    } finally {
      lockForTimerTriggerScheduler.writeLock().unlock();
    }
  }

  @Override
  protected void afterStartByKiraServerStartedEventSuccess() throws Exception {
    KiraManagerDataCenter.setKiraTimerTriggerLocalScheduler(this);
    this.tryPrepareForTimerTriggerBattlefieldIfNeeded();
    this.startTimerTriggerRecoveryMonitorIfNeeded();
    super.afterStartByKiraServerStartedEventSuccess();
  }

  private void tryPrepareForTimerTriggerBattlefieldIfNeeded() throws Exception {
    this.isNormalOperationNeedToBeAborted(
        "tryPrepareForTimerTriggerBattlefieldIfNeeded for " + this.getClass().getSimpleName(),
        true);

    boolean recoverForTimerTriggerEnabled = KiraManagerDataCenter.isRecoverForTimerTriggerEnabled();
    String serverId = KiraManagerDataCenter.getServerId();
    Date lastStartedTime = this.lastStartedTime;
    this.prepareForTimerTriggerBattlefieldIfNeeded(recoverForTimerTriggerEnabled, serverId,
        lastStartedTime);
  }

  @Override
  protected void doShutdown() {
    logger.info("Will doShutdown for " + this.getClass().getSimpleName() + ".");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        logger.info("Shutting down " + this.getClass().getSimpleName() + "...");
        this.clearAllRunTimeData();

        if (null != this.scheduledExecutorService) {
          this.scheduledExecutorService.shutdown();
        }

        this.lockForTimerTriggerScheduler.writeLock().lock();
        try {
          this.shutdownKiraTimerTriggerSchedulerIfStarted();
          if (null != timerTriggerTaskHandleThreadPoolExecutor) {
            timerTriggerTaskHandleThreadPoolExecutor.shutdown();
          }
        } finally {
          this.lockForTimerTriggerScheduler.writeLock().unlock();
        }

        logger.info("Successfully shutdown " + this.getClass().getSimpleName() + ".");
      } catch (Exception e) {
        logger.error("Error occurs when shutting down " + this.getClass().getSimpleName() + ".", e);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish shutdown " + this.getClass().getSimpleName() + ". It takes {} milliseconds.",
            costTime);
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  @Override
  public void updateForTimerTriggerScheduleMonitorContextWhenSuccess(ITimerTrigger timerTrigger) {
    if (null != timerTrigger) {
      boolean isUnscheduled = timerTrigger.isUnscheduled();
      if (!isUnscheduled) {
        try {
          boolean result = true;
          MonitorNoticeInfo monitorNoticeInfo = KiraTimerTriggerLocalScheduler.this.timerTriggerScheduleMonitorContext
              .updateAndGetMonitorNoticeInfoIfNeeded(!result, null);
          if (null != monitorNoticeInfo) {
            TimerTriggerScheduleRecoveredEvent timerTriggerScheduleRecoveredEvent = new TimerTriggerScheduleRecoveredEvent(
                KiraManagerHealthEventType.TIMER_TRIGGER_SCHEDULE_RECOVERED,
                SystemUtil.getLocalhostIp(), monitorNoticeInfo);
            KiraManagerHealthUtils
                .dispatchKiraManagerHealthEvent(timerTriggerScheduleRecoveredEvent);
          }
        } catch (Throwable t) {
          String errorMessage = "Error occurs when call updateForTimerTriggerScheduleMonitorContextWhenSuccess.";
          if (null != logger) {
            logger.error(errorMessage, t);
          } else {
            String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
            //Just want to see the log on rare occasions.
            System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
          }
        }
      }
    }
  }

  @Override
  public void updateForTimerTriggerScheduleMonitorContextWhenFailed(ITimerTrigger timerTrigger) {
    if (null != timerTrigger) {
      boolean isUnscheduled = timerTrigger.isUnscheduled();
      if (!isUnscheduled) {
        String appId = null;
        String triggerId = null;
        Date nextFireTime = null;
        try {
          appId = timerTrigger.getGroup();
          triggerId = timerTrigger.getName();
          nextFireTime = timerTrigger.getNextFireTime();

          String monitorDetails =
              "appId=" + appId + " and triggerId=" + triggerId + " and nextFireTime="
                  + KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTime);
          boolean result = false;
          MonitorNoticeInfo monitorNoticeInfo = KiraTimerTriggerLocalScheduler.this.timerTriggerScheduleMonitorContext
              .updateAndGetMonitorNoticeInfoIfNeeded(!result, monitorDetails);
          if (null != monitorNoticeInfo) {
            TimerTriggerScheduleFailedEvent timerTriggerScheduleFailedEvent = new TimerTriggerScheduleFailedEvent(
                KiraManagerHealthEventType.TIMER_TRIGGER_SCHEDULE_FAILED,
                SystemUtil.getLocalhostIp(), monitorNoticeInfo);
            KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(timerTriggerScheduleFailedEvent);
          }
        } catch (Throwable t) {
          String errorMessage =
              "Error occurs when call updateForTimerTriggerScheduleMonitorContextWhenFailed. appId="
                  + appId + " and triggerId=" + triggerId + " and nextFireTime=" + KiraCommonUtils
                  .getDateAsStringToMsPrecision(nextFireTime);
          if (null != logger) {
            logger.error(errorMessage, t);
          } else {
            String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
            //Just want to see the log on rare occasions.
            System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
          }
        }
      }
    }
  }

  @Override
  public void monitorTimerTriggerRecovery() throws Exception {
    this.doWork("monitorTimerTriggerRecovery", "", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        KiraTimerTriggerLocalScheduler.this.doMonitorTimerTriggerRecovery();
        return null;
      }
    }, null, false);
  }

  private void doMonitorTimerTriggerRecovery() throws Exception {
    if (this.isLeaderServer()) {
      //do recover only on leader server
      if (zkClient.exists(KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD)) {
        List<String> serverShortPathList = zkClient
            .getChildren(KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD);
        if (!CollectionUtils.isEmpty(serverShortPathList)) {
          Set<TriggerIdentity> totalHandledTriggerIdentitySet = new HashSet<TriggerIdentity>();
          try {
            for (String serverShortPath : serverShortPathList) {
              this.isNormalOperationNeedToBeAborted(
                  "loop through serverIdShortPathList during doMonitorTimerTriggerRecovery for "
                      + this.getClass().getSimpleName(), true);

              if (!KiraManagerDataCenter.isRecoverForTimerTriggerEnabled()) {
                if (logger.isDebugEnabled()) {
                  logger.debug(
                      "recoverForTimerTriggerEnabled return false. So do not continue with loop through serverShortPathList in doMonitorTimerTriggerRecovery.");
                }
                break;
              }

              String serverFullPath = KiraUtil
                  .getChildFullPath(KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD,
                      serverShortPath);
              String serverId = zkClient.readData(serverFullPath, true);
              if (StringUtils.isNotBlank(serverId)) {
                List<String> everStartedLocalSchedulerShortPathList = zkClient
                    .getChildren(serverFullPath);
                if (!CollectionUtils.isEmpty(everStartedLocalSchedulerShortPathList)) {
                  Collections.sort(everStartedLocalSchedulerShortPathList);

                  String latestEverStartedLocalSchedulerShortPath = everStartedLocalSchedulerShortPathList
                      .get(everStartedLocalSchedulerShortPathList.size() - 1);
                  Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> triggerIdentityFiringTimerTriggerListMapByLatestEverStartedLocalScheduler = new HashMap<TriggerIdentity, List<FiringTimerTriggerZNodeData>>();
                  boolean latestEverStartedLocalSchedulerOutOfDate = this
                      .handleLatestEverStartedLocalSchedulerFullPath(
                          latestEverStartedLocalSchedulerShortPath,
                          triggerIdentityFiringTimerTriggerListMapByLatestEverStartedLocalScheduler,
                          serverFullPath, serverId);

                  List<String> nonLatestEverStartedLocalSchedulerShortPathList = everStartedLocalSchedulerShortPathList
                      .subList(0, everStartedLocalSchedulerShortPathList.size() - 1);
                  Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> triggerIdentityFiringTimerTriggerListMapByNonLatestEverStartedLocalSchedulerList = this
                      .handleEverStartedLocalSchedulerShortPathList(
                          nonLatestEverStartedLocalSchedulerShortPathList, serverFullPath,
                          serverId);

                  Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> totalTriggerIdentityFiringTimerTriggerListMap = KiraCommonUtils
                      .getMergedMap(
                          triggerIdentityFiringTimerTriggerListMapByLatestEverStartedLocalScheduler,
                          triggerIdentityFiringTimerTriggerListMapByNonLatestEverStartedLocalSchedulerList);
                  Set<TriggerIdentity> willBeHandledTriggerIdentitySet = new HashSet<TriggerIdentity>();
                  if (null != totalTriggerIdentityFiringTimerTriggerListMap) {
                    //make sure the trigger only be handled once in cluster.
                    for (TriggerIdentity handledTriggerIdentity : totalHandledTriggerIdentitySet) {
                      totalTriggerIdentityFiringTimerTriggerListMap.remove(handledTriggerIdentity);
                    }

                    willBeHandledTriggerIdentitySet = totalTriggerIdentityFiringTimerTriggerListMap
                        .keySet();
                  }
                  totalHandledTriggerIdentitySet.addAll(willBeHandledTriggerIdentitySet);

                  Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> unHandledTriggerIdentityFiringTimerTriggerListMap = this
                      .handleTotalTriggerIdentityFiringTimerTriggerListMap(
                          totalTriggerIdentityFiringTimerTriggerListMap);
                  Set<TriggerIdentity> unHandledTriggerIdentitySet = new HashSet<TriggerIdentity>();
                  if (null != unHandledTriggerIdentityFiringTimerTriggerListMap) {
                    unHandledTriggerIdentitySet = unHandledTriggerIdentityFiringTimerTriggerListMap
                        .keySet();
                  }
                  totalHandledTriggerIdentitySet.removeAll(unHandledTriggerIdentitySet);

                  if (null != unHandledTriggerIdentityFiringTimerTriggerListMap
                      && unHandledTriggerIdentityFiringTimerTriggerListMap.size() > 1) {
                    logger.warn(
                        "There are some timerTrigger which have not been recovered success. unHandledTriggerIdentityFiringTimerTriggerListMap={}",
                        unHandledTriggerIdentityFiringTimerTriggerListMap);
                  } else {
                    List<String> everStartedLocalSchedulerShortPathListWhichNeedToBeDeleted = new ArrayList<String>();
                    if (latestEverStartedLocalSchedulerOutOfDate) {
                      everStartedLocalSchedulerShortPathListWhichNeedToBeDeleted
                          .add(latestEverStartedLocalSchedulerShortPath);
                    }

                    if (null != nonLatestEverStartedLocalSchedulerShortPathList) {
                      everStartedLocalSchedulerShortPathListWhichNeedToBeDeleted
                          .addAll(nonLatestEverStartedLocalSchedulerShortPathList);
                    }
                    for (String everStartedLocalSchedulerShortPathWhichNeedToBeDeleted : everStartedLocalSchedulerShortPathListWhichNeedToBeDeleted) {
                      String everStartedLocalSchedulerFullPathWhichNeedToBeDeleted = KiraUtil
                          .getChildFullPath(serverFullPath,
                              everStartedLocalSchedulerShortPathWhichNeedToBeDeleted);
                      if (zkClient.exists(everStartedLocalSchedulerFullPathWhichNeedToBeDeleted)) {
                        logger.warn(
                            "Will deleteRecursive everStartedLocalSchedulerFullPathWhichNeedToBeDeleted={}",
                            everStartedLocalSchedulerFullPathWhichNeedToBeDeleted);
                        zkClient
                            .deleteRecursive(everStartedLocalSchedulerFullPathWhichNeedToBeDeleted);
                      } else {
                        logger.warn(
                            "everStartedLocalSchedulerFullPathWhichNeedToBeDeleted do not exist. It may be deleted by others. everStartedLocalSchedulerFullPathWhichNeedToBeDeleted={}",
                            everStartedLocalSchedulerFullPathWhichNeedToBeDeleted);
                      }
                    }
                  }
                } else {
                  if (logger.isDebugEnabled()) {
                    logger.debug(
                        "No everStartedLocalScheduler under serverIdFullPath={} but just keep it there",
                        serverFullPath);
                  }
                }
              } else {
                logger.error(
                    "serverId is blank when doMonitorTimerTriggerRecovery. serverIdFullPath={}",
                    serverFullPath);
              }
            }
          } finally {
            if (CollectionUtils.isNotEmpty(totalHandledTriggerIdentitySet)) {
              logger.warn(
                  "Summary for doMonitorTimerTriggerRecovery: recovered totalHandledTriggerIdentitySet={}",
                  totalHandledTriggerIdentitySet);
            }
          }
        }
      } else {
        logger.warn(
            "TIMERTRIGGER_BATTLEFIELD do not exist when doMonitorTimerTriggerRecovery. KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD={}",
            KiraManagerConstants.ZK_PATH_TIMERTRIGGER_BATTLEFIELD);
      }
    }
  }

  private Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> handleTotalTriggerIdentityFiringTimerTriggerListMap(
      Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> totalTriggerIdentityFiringTimerTriggerListMap)
      throws Exception {
    Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> unHandledTriggerIdentityFiringTimerTriggerListMap = new HashMap<TriggerIdentity, List<FiringTimerTriggerZNodeData>>(
        totalTriggerIdentityFiringTimerTriggerListMap);

    Set<TriggerIdentity> handledTriggerIdentitySet = new HashSet<TriggerIdentity>();
    if (null != totalTriggerIdentityFiringTimerTriggerListMap) {
      for (Entry<TriggerIdentity, List<FiringTimerTriggerZNodeData>> entry : totalTriggerIdentityFiringTimerTriggerListMap
          .entrySet()) {
        this.isNormalOperationNeedToBeAborted(
            "loop through totalTriggerIdentityFiringTimerTriggerListMap during handleTotalTriggerIdentityFiringTimerTriggerListMap for "
                + this.getClass().getSimpleName(), true);

        boolean recoverForTimerTriggerEnabled = KiraManagerDataCenter
            .isRecoverForTimerTriggerEnabled();
        if (!recoverForTimerTriggerEnabled) {
          throw new KiraHandleException(
              "recoverForTimerTriggerEnabled return false. So do not continue with handleTotalTriggerIdentityFiringTimerTriggerListMap.");
        }

        TriggerIdentity triggerIdentity = entry.getKey();
        List<FiringTimerTriggerZNodeData> firingTimerTriggerZNodeDataList = entry.getValue();
        if (!CollectionUtils.isEmpty(firingTimerTriggerZNodeDataList)) {
          Collections
              .sort(firingTimerTriggerZNodeDataList, new Comparator<FiringTimerTriggerZNodeData>() {
                @Override
                public int compare(FiringTimerTriggerZNodeData o1,
                    FiringTimerTriggerZNodeData o2) {
                  //desc sort, so the first one will be the latest one.
                  int returnValue = 0;

                  Date fireTimeFromNextFireTimeOfO1 = o1.getFireTimeFromNextFireTime();
                  Date fireTimeFromNextFireTimeOfO2 = o2.getFireTimeFromNextFireTime();
                  if (null != fireTimeFromNextFireTimeOfO1
                      && null != fireTimeFromNextFireTimeOfO2) {
                    returnValue = fireTimeFromNextFireTimeOfO2
                        .compareTo(fireTimeFromNextFireTimeOfO1);
                  } else if (null == fireTimeFromNextFireTimeOfO1
                      && null == fireTimeFromNextFireTimeOfO2) {
                    returnValue = 0;
                  } else {
                    if (null == fireTimeFromNextFireTimeOfO1) {
                      returnValue = -1;
                    } else if (null == fireTimeFromNextFireTimeOfO2) {
                      returnValue = 1;
                    }
                  }
                  return returnValue;
                }
              });

          //Just recover once even if there are more than one.
          FiringTimerTriggerZNodeData firingTimerTriggerZNodeData = firingTimerTriggerZNodeDataList
              .get(0);
          if (firingTimerTriggerZNodeDataList.size() > 1) {
            logger.warn(
                "firingTimerTriggerZNodeDataList is more than 1, but will just recover once. firingTimerTriggerZNodeDataList={}",
                firingTimerTriggerZNodeDataList);
          }
          boolean recoverSuccess = false;
          try {
            this.submitTimerTriggerRecoveryTask(recoverForTimerTriggerEnabled,
                firingTimerTriggerZNodeData);
            recoverSuccess = true;
          } finally {
            if (recoverSuccess) {
              handledTriggerIdentitySet.add(triggerIdentity);
              for (FiringTimerTriggerZNodeData oneFiringTimerTriggerZNodeData : firingTimerTriggerZNodeDataList) {
                String fullPathOnZK = oneFiringTimerTriggerZNodeData.getFullPathOnZK();
                if (StringUtils.isNotBlank(fullPathOnZK)) {
                  if (zkClient.exists(fullPathOnZK)) {
                    zkClient.delete(fullPathOnZK);
                  } else {
                    logger.info(
                        "fullPathOnZK do not exist. It may be deleted by others. fullPathOnZK={}",
                        fullPathOnZK);
                  }
                } else {
                  logger.warn(
                      "fullPathOnZK is blank. May have some bugs. oneFiringTimerTriggerZNodeData={}",
                      oneFiringTimerTriggerZNodeData);
                }
              }
            }
          }
        } else {
          //Just mark it as handled.
          handledTriggerIdentitySet.add(triggerIdentity);
        }
      }

      unHandledTriggerIdentityFiringTimerTriggerListMap.keySet()
          .removeAll(handledTriggerIdentitySet);
    }

    return unHandledTriggerIdentityFiringTimerTriggerListMap;
  }

  /**
   * @return true if everStartedLocalScheduler is out-of-date
   */
  private boolean handleLatestEverStartedLocalSchedulerFullPath(
      String latestEverStartedLocalSchedulerShortPath,
      Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> triggerIdentityFiringTimerTriggerListMapByLatestEverStartedLocalScheduler,
      String serverFullPath, String serverId) throws Exception {
    boolean isEverStartedLocalSchedulerOutOfDate = true;

    this.isNormalOperationNeedToBeAborted(
        "handleLatestEverStartedLocalSchedulerFullPath for " + this.getClass().getSimpleName(),
        true);

    String latestEverStartedLocalSchedulerFullPath = KiraUtil
        .getChildFullPath(serverFullPath, latestEverStartedLocalSchedulerShortPath);
    isEverStartedLocalSchedulerOutOfDate = this
        .isEverStartedLocalSchedulerOutOfDate(latestEverStartedLocalSchedulerFullPath);
    if (isEverStartedLocalSchedulerOutOfDate) {
      logger.warn(
          "The latestEverStartedLocalSchedulerFullPath is out of date. So will handle it now. latestEverStartedLocalSchedulerFullPath={}",
          latestEverStartedLocalSchedulerFullPath);
      List<String> everStartedLocalSchedulerShortPathList = new ArrayList<String>();
      everStartedLocalSchedulerShortPathList.add(latestEverStartedLocalSchedulerShortPath);
      Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> triggerIdentityFiringTimerTriggerListMap = this
          .handleEverStartedLocalSchedulerShortPathList(everStartedLocalSchedulerShortPathList,
              serverFullPath, serverId);
      if (null != triggerIdentityFiringTimerTriggerListMap) {
        triggerIdentityFiringTimerTriggerListMapByLatestEverStartedLocalScheduler
            .putAll(triggerIdentityFiringTimerTriggerListMap);
      }
    }

    return isEverStartedLocalSchedulerOutOfDate;
  }

  private boolean isEverStartedLocalSchedulerOutOfDate(String everStartedLocalSchedulerFullPath)
      throws Exception {
    boolean returnValue = true;

    LocalSchedulerStartedZNodeData localSchedulerStartedZNodeData = zkClient
        .readData(everStartedLocalSchedulerFullPath, true);
    if (null != localSchedulerStartedZNodeData) {
      Date localSchedulerLastStartedTimeOnZK = localSchedulerStartedZNodeData.getLastStartedTime();
      if (null != localSchedulerLastStartedTimeOnZK) {
        KiraServerEntity kiraServerEntity = localSchedulerStartedZNodeData.getKiraServerEntity();
        if (null != kiraServerEntity) {
          try {
            String serviceUrl = kiraServerEntity.getAccessUrlAsString();
            IClusterInternalService clusterInternalService = KiraServerUtils
                .getClusterInternalService(serviceUrl);
            KiraServerEntity remoteCurrentKiraServerEntity = null;
            try {
              remoteCurrentKiraServerEntity = clusterInternalService.getKiraServerEntity();
            } catch (HessianRuntimeException hessianRuntimeException) {
              boolean isKiraServerMayExistInClusterWhenHessianRuntimeExceptionOccurs = this
                  .isKiraServerMayExistInClusterWhenHessianRuntimeExceptionOccurs(
                      everStartedLocalSchedulerFullPath, kiraServerEntity, hessianRuntimeException);
              return !isKiraServerMayExistInClusterWhenHessianRuntimeExceptionOccurs;
            } catch (Exception e) {
              logger.error(
                  "Error occurs for clusterInternalService.getKiraServerEntity(); everStartedLocalSchedulerFullPath"
                      + everStartedLocalSchedulerFullPath + " and kiraServerEntity="
                      + kiraServerEntity, e);
              //Just throw out and do not continue
              throw e;
            }

            if (null != remoteCurrentKiraServerEntity) {
              if (remoteCurrentKiraServerEntity.equals(kiraServerEntity)) {
                //That server is accessible
                returnValue = false;
              } else {
                logger.warn(
                    "remoteCurrentKiraServerEntity is not equals with kiraServerEntity. kiraServerEntity={} and remoteCurrentKiraServerEntity={},kiraServerEntity,remoteCurrentKiraServerEntity");
              }
            } else {
              //The server is accessable but may be shutdown now.
              logger.warn(
                  "remoteCurrentKiraServerEntity is null for isEverStartedLocalSchedulerOutOfDate. The server is accessable but may be shutdown now. everStartedLocalSchedulerFullPath={}",
                  everStartedLocalSchedulerFullPath);
            }
          } catch (Exception e) {
            logger.error(
                "Error occurs for isEverStartedLocalSchedulerOutOfDate. everStartedLocalSchedulerFullPath="
                    + everStartedLocalSchedulerFullPath, e);

            //Just throw out and do not continue
            throw e;
          }
        } else {
          logger.error(
              "kiraServerEntity is null when handleLatestEverStartedLocalSchedulerFullPath. everStartedLocalSchedulerFullPath={}",
              everStartedLocalSchedulerFullPath);
        }
      } else {
        logger.error(
            "localSchedulerLastStartedTimeOnZK is null when doMonitorTimerTriggerRecovery. everStartedLocalSchedulerFullPath={}",
            everStartedLocalSchedulerFullPath);
      }
    } else {
      logger.error(
          "localSchedulerStartedZNodeData is null when doMonitorTimerTriggerRecovery. May have some bugs. everStartedLocalSchedulerFullPath={}",
          everStartedLocalSchedulerFullPath);
    }

    return returnValue;
  }

  /**
   * @param hessianRuntimeException occurs
   * @return true if the kiraServer exist in cluster although HessianRuntimeException occurs
   */
  private boolean isKiraServerMayExistInClusterWhenHessianRuntimeExceptionOccurs(
      String everStartedLocalSchedulerFullPath, KiraServerEntity kiraServerEntity,
      HessianRuntimeException hessianRuntimeException) throws Exception {
    boolean kiraServerMayExistInCluster = false;

    Throwable t = hessianRuntimeException.getRootCause();
    if (t instanceof ConnectException) {
      //java.net.ConnectException: Connection refused: connect
      //java.net.ConnectException: Connection refused
      String message = t.getMessage();
      if (StringUtils.isNotBlank(message) && StringUtils
          .containsIgnoreCase(message, "Connection refused")) {
        //need to double check it from ZK to see if that server still do not exist in the cluster.
        LinkedHashSet<KiraServerEntity> allKiraServerEntitysInCluster = this.kiraServer
            .getAllKiraServerEntitysInCluster();
        if (CollectionUtils.isNotEmpty(allKiraServerEntitysInCluster)) {
          if (allKiraServerEntitysInCluster.contains(kiraServerEntity)) {
            kiraServerMayExistInCluster = true;
            logger.warn(
                "The server seems exist in the cluster by ZK even HessianRuntimeException occurs, but that server is unaccessable by me now. So do not treat it as out-of-date. everStartedLocalSchedulerFullPath={} and kiraServerEntity={}",
                everStartedLocalSchedulerFullPath, kiraServerEntity);
          }
        }

        return kiraServerMayExistInCluster;
      }
    }

    throw hessianRuntimeException;
  }

  private Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> handleEverStartedLocalSchedulerShortPathList(
      List<String> everStartedLocalSchedulerShortPathList, String serverFullPath, String serverId)
      throws Exception {
    Map<TriggerIdentity, List<FiringTimerTriggerZNodeData>> returnValue = new HashMap<TriggerIdentity, List<FiringTimerTriggerZNodeData>>();

    if (!CollectionUtils.isEmpty(everStartedLocalSchedulerShortPathList)) {
      for (String everStartedLocalSchedulerShortPath : everStartedLocalSchedulerShortPathList) {
        this.isNormalOperationNeedToBeAborted(
            "loop through handleEverStartedLocalSchedulerShortPathList for " + this.getClass()
                .getSimpleName(), true);

        String everStartedLocalSchedulerFullPath = KiraUtil
            .getChildFullPath(serverFullPath, everStartedLocalSchedulerShortPath);
        List<String> poolShortPathList = zkClient.getChildren(everStartedLocalSchedulerFullPath);
        if (!CollectionUtils.isEmpty(poolShortPathList)) {
          for (String poolShortPath : poolShortPathList) {
            this.isNormalOperationNeedToBeAborted(
                "loop through poolShortPathList in handleEverStartedLocalSchedulerShortPathList for "
                    + this.getClass().getSimpleName(), true);

            if (!KiraManagerDataCenter.isRecoverForTimerTriggerEnabled()) {
              throw new KiraHandleException(
                  "recoverForTimerTriggerEnabled return false. So do not continue with loop through poolShortPathList in handleEverStartedLocalSchedulerShortPathList.");
            }

            String poolFullPath = KiraUtil
                .getChildFullPath(everStartedLocalSchedulerFullPath, poolShortPath);
            List<String> triggerShortPathList = zkClient.getChildren(poolFullPath);
            if (!CollectionUtils.isEmpty(triggerShortPathList)) {
              for (String triggerShortPath : triggerShortPathList) {
                this.isNormalOperationNeedToBeAborted(
                    "loop through triggerShortPathList in handleEverStartedLocalSchedulerShortPathList for "
                        + this.getClass().getSimpleName(), true);

                if (!KiraManagerDataCenter.isRecoverForTimerTriggerEnabled()) {
                  throw new KiraHandleException(
                      "recoverForTimerTriggerEnabled return false. So do not continue with loop through triggerShortPathList in handleEverStartedLocalSchedulerShortPathList.");
                }

                String triggerFullPath = KiraUtil.getChildFullPath(poolFullPath, triggerShortPath);

                List<String> firingTriggerShortPathList = zkClient.getChildren(triggerFullPath);
                if (!CollectionUtils.isEmpty(firingTriggerShortPathList)) {
                  for (String firingTriggerShortPath : firingTriggerShortPathList) {
                    this.isNormalOperationNeedToBeAborted(
                        "loop through firingTriggerShortPathList in handleEverStartedLocalSchedulerShortPathList for "
                            + this.getClass().getSimpleName(), true);

                    if (!KiraManagerDataCenter.isRecoverForTimerTriggerEnabled()) {
                      throw new KiraHandleException(
                          "recoverForTimerTriggerEnabled return false. So do not continue with loop through firingTriggerShortPathList in handleEverStartedLocalSchedulerShortPathList.");
                    }

                    String firingTriggerFullPath = KiraUtil
                        .getChildFullPath(triggerFullPath, firingTriggerShortPath);
                    FiringTimerTriggerZNodeData firingTimerTriggerZNodeData = zkClient
                        .readData(firingTriggerFullPath, true);
                    if (null != firingTimerTriggerZNodeData) {
                      String appId = firingTimerTriggerZNodeData.getAppId();
                      String triggerId = firingTimerTriggerZNodeData.getTriggerId();
                      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);

                      List<FiringTimerTriggerZNodeData> firingTimerTriggerZNodeDataList = returnValue
                          .get(triggerIdentity);
                      if (null == firingTimerTriggerZNodeDataList) {
                        firingTimerTriggerZNodeDataList = new ArrayList<FiringTimerTriggerZNodeData>();
                        returnValue.put(triggerIdentity, firingTimerTriggerZNodeDataList);
                      }
                      firingTimerTriggerZNodeDataList.add(firingTimerTriggerZNodeData);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    return returnValue;
  }

  @Override
  public void unscheduleKiraTimerTrigger(final String appId, final String triggerId)
      throws Exception {
    this.doWork("unscheduleKiraTimerTrigger", "appId=" + appId + " and triggerId=" + triggerId,
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            KiraTimerTriggerLocalScheduler.this.doUnscheduleKiraTimerTrigger(appId, triggerId);
            return null;
          }
        }, this.lockForTimerTriggerScheduler, false);
  }

  @Override
  public void rescheduleKiraTimerTrigger(final String appId, final String triggerId)
      throws Exception {
    this.doWork("rescheduleKiraTimerTrigger", "appId=" + appId + " and triggerId=" + triggerId,
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            KiraTimerTriggerLocalScheduler.this.doRescheduleKiraTimerTrigger(appId, triggerId);
            return null;
          }
        }, this.lockForTimerTriggerScheduler, false);
  }

  @Override
  public int getManagedTimerTriggerCount() throws Exception {
    Integer returnValue = this.doWork("getManagedTimerTriggerCount", "", new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        int managedTimerTriggerCount = KiraTimerTriggerLocalScheduler.this
            .doGetManagedTimerTriggerCount();
        return Integer.valueOf(managedTimerTriggerCount);
      }
    }, this.lockForTimerTriggerScheduler, false);
    return returnValue;
  }

  private int doGetManagedTimerTriggerCount() throws Exception {
    return this.timerTriggerScheduler.getManagedTimerTriggerCount();
  }

  @Override
  public List<TriggerIdentity> getManagedTriggerIdentityList()
      throws Exception {
    List<TriggerIdentity> returnValue = this
        .doWork("getManagedTriggerIdentityList", "", new Callable<List<TriggerIdentity>>() {
          @Override
          public List<TriggerIdentity> call() throws Exception {
            List<TriggerIdentity> managedTriggerIdentityList = KiraTimerTriggerLocalScheduler.this
                .doGetManagedTriggerIdentityList();
            return managedTriggerIdentityList;
          }
        }, this.lockForTimerTriggerScheduler, false);
    return returnValue;
  }

  @Override
  public ITimerTrigger getTimerTrigger(final String timerTriggerId,
      final boolean returnClonedObject) throws Exception {
    ITimerTrigger returnValue = this.doWork("getTimerTrigger",
        "timerTriggerId=" + timerTriggerId + " and returnClonedObject=" + returnClonedObject,
        new Callable<ITimerTrigger>() {
          @Override
          public ITimerTrigger call() throws Exception {
            ITimerTrigger timerTrigger = KiraTimerTriggerLocalScheduler.this
                .doGetTimerTrigger(timerTriggerId, returnClonedObject);
            return timerTrigger;
          }
        }, this.lockForTimerTriggerScheduler, false);
    return returnValue;
  }

  @Override
  public Collection<ITimerTrigger> getManagedTimerTriggers(boolean accurate)
      throws Exception {
    Collection<ITimerTrigger> returnValue = null;
    if (null != this.timerTriggerScheduler) {
      returnValue = this.timerTriggerScheduler.getManagedTimerTriggers(accurate);
    }

    return returnValue;
  }

  private String createAndGetTriggerOfPoolForLocalSchedulerZNodeZKPathForFireTimerTriggerTask(
      boolean recoverForTimerTriggerEnabled, String serverId, Date lastStartedTime, String appId,
      String triggerId) throws Exception {
    String returnValue = null;
    if (StringUtils.isNotBlank(serverId) && null != lastStartedTime) {
      //need to add node on zk under /kira/timerTriggerBattlefield/{serverId}/{lastStartedTime}/{identityOfKiraTimerTriggerLocalScheduler}/{poolId}/{triggerId}/{fireTime}
      String localSchedulerZNodeZKPath = KiraTimerTriggerLocalScheduler.this
          .prepareForTimerTriggerBattlefieldIfNeeded(recoverForTimerTriggerEnabled, serverId,
              lastStartedTime);

      if (zkClient.exists(localSchedulerZNodeZKPath)) {
        String poolForLocalSchedulerZNodeZKPath =
            localSchedulerZNodeZKPath + KiraCommonConstants.ZNODE_NAME_PREFIX + KiraUtil
                .filterString(appId);
        if (!zkClient.exists(poolForLocalSchedulerZNodeZKPath)) {
          try {
            zkClient.createPersistent(poolForLocalSchedulerZNodeZKPath, appId);
          } catch (ZkNodeExistsException nodeExistsException) {
            logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                + ". Just ignore this exception. This node may be created by someone.");
          }
        }

        String triggerOfPoolForLocalSchedulerZNodeZKPath =
            poolForLocalSchedulerZNodeZKPath + KiraCommonConstants.ZNODE_NAME_PREFIX + KiraUtil
                .filterString(triggerId);
        if (!zkClient.exists(triggerOfPoolForLocalSchedulerZNodeZKPath)) {
          try {
            TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
            zkClient.createPersistent(triggerOfPoolForLocalSchedulerZNodeZKPath, triggerIdentity);
          } catch (ZkNodeExistsException nodeExistsException) {
            logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                + ". Just ignore this exception. This node may be created by someone.");
          }
        }

        returnValue = triggerOfPoolForLocalSchedulerZNodeZKPath;
      } else {
        logger.warn(
            "localSchedulerZNodeZKPath do not exist after call retainTheSceneForFireTimerTriggerTask, i will give up. recoverForTimerTriggerEnabled={} and localSchedulerZNodeZKPath={} and serverId={} and lastStartedTime={} and appId={} and triggerId={}",
            recoverForTimerTriggerEnabled, localSchedulerZNodeZKPath, serverId, KiraCommonUtils
                .getDateAsString(lastStartedTime,
                    KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION), appId,
            triggerId);
      }
    } else {
      if (null != logger) {
        logger.error(
            "serverId is blank or lastStartedTime is null when retainTheSceneForFireTimerTriggerTask. May have some bugs. recoverForTimerTriggerEnabled={} and serverId={} and lastStartedTime={} and appId={} and triggerId={}",
            recoverForTimerTriggerEnabled, serverId, KiraCommonUtils
                .getDateAsString(lastStartedTime,
                    KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION), appId,
            triggerId);
      }
    }

    return returnValue;
  }

  private void completeFireTimerTriggerTask(boolean recoverForTimerTriggerEnabled,
      String currentFiredTriggerForLocalSchedulerZNodeZKPath) {
    try {
      if (StringUtils.isNotBlank(currentFiredTriggerForLocalSchedulerZNodeZKPath)) {
        //need to remove node on zk.
        if (zkClient.exists(currentFiredTriggerForLocalSchedulerZNodeZKPath)) {
          zkClient.delete(currentFiredTriggerForLocalSchedulerZNodeZKPath);
        } else {
          logger.warn(
              "currentFiredTriggerForLocalSchedulerZNodeZKPath do not exist. This should not happen. May have some bugs. currentFiredTriggerForLocalSchedulerZNodeZKPath={} and serverId={} and lastStartedTime={} and recoverForTimerTriggerEnabled={}",
              currentFiredTriggerForLocalSchedulerZNodeZKPath, KiraCommonUtils
                  .getDateAsString(lastStartedTime,
                      KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION),
              recoverForTimerTriggerEnabled);
        }
      } else {
        logger.warn(
            "currentFiredTriggerForLocalSchedulerZNodeZKPath is blank. So do not continue with completeFireTimerTriggerTask. recoverForTimerTriggerEnabled and lastStartedTime={}",
            recoverForTimerTriggerEnabled, KiraCommonUtils.getDateAsString(lastStartedTime,
                KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION));
      }
    } catch (Throwable t) {
      String errorMessage =
          "Error occurs when completeFireTimerTriggerTask. lastStartedTime=" + KiraCommonUtils
              .getDateAsString(lastStartedTime,
                  KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION);
      if (null != logger) {
        logger.error(errorMessage, t);
      } else {
        String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
        //Just want to see the log on rare occasions.
        System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
      }
    }
  }

  private void submitTimerTriggerRecoveryTask(final boolean recoverForTimerTriggerEnabled,
      final FiringTimerTriggerZNodeData firingTimerTriggerZNodeData) throws Exception {
    this.isNormalOperationNeedToBeAborted(
        "submitTimerTriggerRecoveryTask for firingTimerTriggerZNodeData="
            + firingTimerTriggerZNodeData, true);

    timerTriggerTaskHandleThreadPoolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        KiraTimerTriggerLocalScheduler.this
            .runTimerTriggerRecoveryTask(recoverForTimerTriggerEnabled,
                firingTimerTriggerZNodeData);
      }
    });
  }

  private void runTimerTriggerRecoveryTask(boolean recoverForTimerTriggerEnabled,
      FiringTimerTriggerZNodeData firingTimerTriggerZNodeData) {
    if (null != firingTimerTriggerZNodeData) {
      Date triggerTime = new Date();
      String currentFiredTriggerForLocalSchedulerZNodeZKPath = null;
      try {
        if (recoverForTimerTriggerEnabled) {
          currentFiredTriggerForLocalSchedulerZNodeZKPath = preFireTimerTriggerRecoveryTask(
              triggerTime, recoverForTimerTriggerEnabled, firingTimerTriggerZNodeData);
        }

        this.fireTimerTriggerRecoveryTask(triggerTime, firingTimerTriggerZNodeData);
      } catch (Throwable t) {
        String errorMessage =
            "Error occurs when runing runTimerTriggerRecoveryTask. firingTimerTriggerZNodeData="
                + firingTimerTriggerZNodeData;
        if (null != logger) {
          logger.error(errorMessage, t);
        } else {
          String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
          //Just want to see the log on rare occasions.
          System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
        }
      } finally {
        if (recoverForTimerTriggerEnabled) {
          KiraTimerTriggerLocalScheduler.this
              .completeFireTimerTriggerTask(recoverForTimerTriggerEnabled,
                  currentFiredTriggerForLocalSchedulerZNodeZKPath);
        }
      }
    }
  }

  private void fireTimerTriggerRecoveryTask(Date triggerTime,
      FiringTimerTriggerZNodeData firingTimerTriggerZNodeData) throws Throwable {
    String appId = firingTimerTriggerZNodeData.getAppId();
    String triggerId = firingTimerTriggerZNodeData.getTriggerId();

    TriggerMetadata triggerMetadata = triggerMetadataService
        .getLatestAndAvailableTriggerMetadata(appId
            , triggerId, null);
    if (null != triggerMetadata) {
      boolean masterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
      boolean canBeScheduled = KiraManagerUtils.isCanBeScheduled(triggerMetadata, masterZone);
      if (canBeScheduled) {
        //String createdBy = SystemUtil.getLocalhostIp();
        String createdBy = this.getCreatedBy() + ".REC=" + KiraCommonUtils
            .getDateAsString(firingTimerTriggerZNodeData.getFireTimeFromNextFireTime());

        Job job = triggerMetadataService
            .createAndRunJobByTriggerMetadata(triggerMetadata, Boolean.FALSE, createdBy,
                triggerTime);
        if (null == job) {
          logger.error(
              "!!!Can not create job when fireTimerTriggerRecoveryTask. for firingTimerTriggerZNodeData={} and triggerMetadata={}",
              firingTimerTriggerZNodeData, KiraCommonUtils.toString(triggerMetadata));
        } else {
          logger.warn("Recovery handled for firingTimerTriggerZNodeData={}",
              firingTimerTriggerZNodeData);
        }
      } else {
        logger.warn(
            "!!!canBeScheduled is false for triggerMetadata now. So do not run createAndRunJobByTriggerMetadata in fireTimerTriggerRecoveryTask. masterZone={}",
            masterZone);
      }
    } else {
      logger.warn(
          "Can not find triggerMetadata when fireTimerTriggerRecoveryTask. firingTimerTriggerZNodeData={}",
          firingTimerTriggerZNodeData);
    }
  }

  private String getCreatedBy() {
    String returnValue = null;
    try {
      returnValue = KiraManagerDataCenter.getServerId();
    } catch (Exception e) {
      logger.error("Error occurs during getCreatedBy().", e);
    } finally {
      if (StringUtils.isBlank(returnValue)) {
        returnValue = SystemUtil.getLocalhostIp();
      }
    }
    return returnValue;
  }

  private String preFireTimerTriggerRecoveryTask(Date triggerTime,
      boolean recoverForTimerTriggerEnabled,
      FiringTimerTriggerZNodeData firingTimerTriggerZNodeData) throws Exception {
    String returnValue = null;

    try {
      String appId = firingTimerTriggerZNodeData.getAppId();
      String triggerId = firingTimerTriggerZNodeData.getTriggerId();
      String serverId = KiraManagerDataCenter.getServerId();
      String triggerOfPoolForLocalSchedulerZNodeZKPath = this
          .createAndGetTriggerOfPoolForLocalSchedulerZNodeZKPathForFireTimerTriggerTask(
              recoverForTimerTriggerEnabled, serverId, this.lastStartedTime, appId, triggerId);
      if (StringUtils.isNotBlank(triggerOfPoolForLocalSchedulerZNodeZKPath)) {
        boolean forMisfire = firingTimerTriggerZNodeData.isForMisfire();
        if (forMisfire) {
          logger.warn("!!!Will recover for last misfired trigger. firingTimerTriggerZNodeData={}",
              firingTimerTriggerZNodeData);
        }

        boolean forRecovery = firingTimerTriggerZNodeData.isForRecovery();
        if (forRecovery) {
          logger.warn("!!!Will recover for last recovered trigger. firingTimerTriggerZNodeData={}",
              firingTimerTriggerZNodeData);
        }

        String currentFiredTriggerForLocalSchedulerZNodeName = KiraUtil.filterString(KiraCommonUtils
            .getDateAsString(triggerTime,
                KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)) + "-Recover" + (
            forRecovery ? "-ForRecovery" : "") + (forMisfire ? "-ForMisfire" : "");
        returnValue =
            triggerOfPoolForLocalSchedulerZNodeZKPath + KiraCommonConstants.ZNODE_NAME_PREFIX
                + currentFiredTriggerForLocalSchedulerZNodeName;

        FiringTimerTriggerZNodeData newFiringTimerTriggerZNodeData = new FiringTimerTriggerZNodeData(
            returnValue, serverId, lastStartedTime, appId, triggerId,
            firingTimerTriggerZNodeData.getFireTimeFromNextFireTime(), triggerTime, forMisfire,
            true);
        try {
          zkClient.createPersistent(returnValue, newFiringTimerTriggerZNodeData);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.error(
              "ZkNodeExistsException occurs when preFireTimerTriggerRecoveryTask. This should not happen. May have some bugs for the trigger may be fired more than once for the same nexfireTime. message="
                  + nodeExistsException.getMessage() + ".  firingTriggerZNodeData="
                  + firingTimerTriggerZNodeData);
        }
      } else {
        logger.warn(
            "triggerOfPoolForLocalSchedulerZNodeZKPath is blank when preFireTimerTriggerRecoveryTask. Why? recoverForTimerTriggerEnabled={} and serverId={} and lastStartedTime={} and firingTimerTriggerZNodeData={}",
            recoverForTimerTriggerEnabled, serverId, KiraCommonUtils
                .getDateAsString(lastStartedTime,
                    KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION),
            firingTimerTriggerZNodeData);
      }
    } catch (Throwable t) {
      String errorMessage =
          "Error occurs when preFireTimerTriggerRecoveryTask." + " and triggerTime="
              + KiraCommonUtils.getDateAsString(triggerTime,
              KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)
              + " and recoverForTimerTriggerEnabled=" + recoverForTimerTriggerEnabled
              + " and firingTimerTriggerZNodeData=" + firingTimerTriggerZNodeData;
      if (null != logger) {
        logger.error(errorMessage, t);
      } else {
        String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
        //Just want to see the log on rare occasions.
        System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
      }
    }

    return returnValue;
  }

  @Override
  public void submitTimerTriggerTask(final String serverId, final Date lastStartedTime,
      final ITimerTrigger timerTrigger) throws Exception {
    boolean isUnscheduled = timerTrigger.isUnscheduled();
    if (isUnscheduled) {
      logger.info(
          "The trigger is unscheduled. So do not continue with submitTimerTriggerTask. timerTrigger={} in timer={}",
          timerTrigger.getId());
    } else {
      this.isNormalOperationNeedToBeAborted(
          "submitTimerTriggerTask for timerTrigger=" + timerTrigger.getId(), true);

      final ITimerTrigger clonedTimerTrigger = (ITimerTrigger) timerTrigger.clone();
      final Date fireTimeFromNextFireTime = clonedTimerTrigger.getNextFireTime();

      timerTriggerTaskHandleThreadPoolExecutor.submit(new Runnable() {
        @Override
        public void run() {
          Date triggerTime = new Date();

          boolean recoverForTimerTriggerEnabled = KiraManagerDataCenter
              .isRecoverForTimerTriggerEnabled();
          boolean requestsRecovery = clonedTimerTrigger.isRequestsRecovery();
          String currentFiredTriggerForLocalSchedulerZNodeZKPath = null;
          try {
            if (recoverForTimerTriggerEnabled && requestsRecovery) {

              boolean misfireFoundWhenInstantiated = timerTrigger.isMisfireFoundWhenInstantiated();
              boolean misfireHandled = timerTrigger.isMisfireHandled();
              boolean forMisfire = misfireFoundWhenInstantiated && !misfireHandled;
              currentFiredTriggerForLocalSchedulerZNodeZKPath = this
                  .preFireTimerTriggerTask(recoverForTimerTriggerEnabled, forMisfire);
            }

            this.fireTimerTriggerTask(triggerTime);
          } catch (Throwable t) {
            String errorMessage = "Error occurs when runing timerTriggerTask. serverId=" + serverId
                + " and lastStartedTime=" + KiraCommonUtils.getDateAsString(lastStartedTime,
                KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION) + " and timerTrigger="
                + timerTrigger;
            if (null != logger) {
              logger.error(errorMessage, t);
            } else {
              String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
              //Just want to see the log on rare occasions.
              System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
            }
          } finally {
            if (recoverForTimerTriggerEnabled && requestsRecovery) {
              KiraTimerTriggerLocalScheduler.this
                  .completeFireTimerTriggerTask(recoverForTimerTriggerEnabled,
                      currentFiredTriggerForLocalSchedulerZNodeZKPath);
            }
          }
        }

        private void updateForRunTimerTriggerTaskMonitorContextWhenSuccess() {
          try {
            boolean result = true;
            MonitorNoticeInfo monitorNoticeInfo = KiraTimerTriggerLocalScheduler.this.runTimerTriggerTaskMonitorContext
                .updateAndGetMonitorNoticeInfoIfNeeded(!result, null);
            if (null != monitorNoticeInfo) {
              RunTimerTriggerTaskRecoveredEvent runTimerTriggerTaskRecoveredEvent = new RunTimerTriggerTaskRecoveredEvent(
                  KiraManagerHealthEventType.RUN_TIMER_TRIGGER_TASK_RECOVERED,
                  SystemUtil.getLocalhostIp(), monitorNoticeInfo);
              KiraManagerHealthUtils
                  .dispatchKiraManagerHealthEvent(runTimerTriggerTaskRecoveredEvent);
            }
          } catch (Throwable t) {
            String errorMessage = "Error occurs when call updateForRunTimerTriggerTaskMonitorContextWhenSuccess.";
            if (null != logger) {
              logger.error(errorMessage, t);
            } else {
              String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
              //Just want to see the log on rare occasions.
              System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
            }
          }
        }

        private void updateForRunTimerTriggerTaskMonitorContextWhenFailed(String appId,
            String triggerId, Date triggerTime, Throwable throwable) {
          try {
            String monitorDetails =
                "appId=" + appId + " and triggerId=" + triggerId + " and triggerTime="
                    + KiraCommonUtils.getDateAsStringToMsPrecision(triggerTime)
                    + " and exceptionDetails=" + ExceptionUtils.getFullStackTrace(throwable);
            boolean result = false;
            MonitorNoticeInfo monitorNoticeInfo = KiraTimerTriggerLocalScheduler.this.runTimerTriggerTaskMonitorContext
                .updateAndGetMonitorNoticeInfoIfNeeded(!result, monitorDetails);
            if (null != monitorNoticeInfo) {
              RunTimerTriggerTaskFailedEvent runTimerTriggerTaskFailedEvent = new RunTimerTriggerTaskFailedEvent(
                  KiraManagerHealthEventType.RUN_TIMER_TRIGGER_TASK_FAILED,
                  SystemUtil.getLocalhostIp(), monitorNoticeInfo);
              KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(runTimerTriggerTaskFailedEvent);
            }
          } catch (Throwable t) {
            String errorMessage = "Error occurs when call updateForRunTimerTriggerTaskMonitorContextWhenFailed.";
            if (null != logger) {
              logger.error(errorMessage, t);
            } else {
              String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
              //Just want to see the log on rare occasions.
              System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
            }
          }
        }

        private String preFireTimerTriggerTask(boolean recoverForTimerTriggerEnabled,
            boolean forMisfire) {
          String returnValue = null;

          try {
            if (null == fireTimeFromNextFireTime) {
              if (null != logger) {
                logger.error(
                    "fireTimeFromNextFireTime is null. May have some bugs. clonedTimerTrigger={}",
                    clonedTimerTrigger);
              }
            } else {
              String appId = clonedTimerTrigger.getGroup();
              String triggerId = clonedTimerTrigger.getName();
              String triggerOfPoolForLocalSchedulerZNodeZKPath = KiraTimerTriggerLocalScheduler.this
                  .createAndGetTriggerOfPoolForLocalSchedulerZNodeZKPathForFireTimerTriggerTask(
                      recoverForTimerTriggerEnabled, serverId, lastStartedTime, appId, triggerId);
              if (StringUtils.isNotBlank(triggerOfPoolForLocalSchedulerZNodeZKPath)) {
                String currentFiredTriggerForLocalSchedulerZNodeName = KiraUtil.filterString(
                    KiraCommonUtils.getDateAsString(fireTimeFromNextFireTime,
                        KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)) + (forMisfire
                    ? "-Misfire" : "");
                String currentFiredTriggerForLocalSchedulerZNodeZKPath =
                    triggerOfPoolForLocalSchedulerZNodeZKPath
                        + KiraCommonConstants.ZNODE_NAME_PREFIX
                        + currentFiredTriggerForLocalSchedulerZNodeName;
                FiringTimerTriggerZNodeData firingTimerTriggerZNodeData = new FiringTimerTriggerZNodeData(
                    currentFiredTriggerForLocalSchedulerZNodeZKPath, serverId, lastStartedTime,
                    appId, triggerId, clonedTimerTrigger.getPreviousFireTime(),
                    fireTimeFromNextFireTime, forMisfire, false);
                try {
                  zkClient.createPersistent(currentFiredTriggerForLocalSchedulerZNodeZKPath,
                      firingTimerTriggerZNodeData);
                } catch (ZkNodeExistsException nodeExistsException) {
                  logger.error(
                      "ZkNodeExistsException occurs when preFireTimerTriggerTask. This should not happen. May have some bugs for the trigger may be fired more than once for the same nexfireTime. message="
                          + nodeExistsException.getMessage()
                          + ". This node may be created by someone. firingTriggerZNodeData="
                          + firingTimerTriggerZNodeData);
                }
                returnValue = currentFiredTriggerForLocalSchedulerZNodeZKPath;
              } else {
                logger.warn(
                    "triggerOfPoolForLocalSchedulerZNodeZKPath is blank when preFireTimerTriggerTask. Why? recoverForTimerTriggerEnabled={} and serverId={} and lastStartedTime={} and clonedTimerTrigger={}",
                    recoverForTimerTriggerEnabled, serverId, KiraCommonUtils
                        .getDateAsString(lastStartedTime,
                            KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION),
                    clonedTimerTrigger);
              }
            }
          } catch (Throwable t) {
            String errorMessage = "Error occurs when preFireTimerTriggerTask. serverId=" + serverId
                + " and lastStartedTime=" + KiraCommonUtils.getDateAsString(lastStartedTime,
                KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)
                + " and clonedTimerTrigger=" + clonedTimerTrigger;
            if (null != logger) {
              logger.error(errorMessage, t);
            } else {
              String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
              //Just want to see the log on rare occasions.
              System.out.println(errorMessage + " and exceptionDesc=" + exceptionDesc);
            }
          }

          return returnValue;
        }

        private void fireTimerTriggerTask(Date triggerTime) throws Throwable {
          String appId = null;
          String triggerId = null;
          try {
            appId = timerTrigger.getGroup();
            triggerId = timerTrigger.getName();

            boolean isUnscheduled = timerTrigger.isUnscheduled();
            if (isUnscheduled) {
              logger.warn(
                  "!!!The trigger is unscheduled but it is in the run method now after submit successfully. So still continue. This may cause duplicate run if it occurs when move this trigger to other server. timerTrigger={}",
                  timerTrigger);
            }

            TriggerMetadata triggerMetadata = triggerMetadataService
                .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
            if (null != triggerMetadata) {
              boolean masterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
              boolean canBeScheduled = KiraManagerUtils
                  .isCanBeScheduled(triggerMetadata, masterZone);
              if (canBeScheduled) {
                //String createdBy = SystemUtil.getLocalhostIp();
                String createdBy = KiraTimerTriggerLocalScheduler.this.getCreatedBy();

                boolean misfireFoundWhenInstantiated = timerTrigger
                    .isMisfireFoundWhenInstantiated();
                boolean misfireHandled = timerTrigger.isMisfireHandled();
                int misfireInstruction = timerTrigger.getMisfireInstruction();
                if (misfireFoundWhenInstantiated && !misfireHandled) {
                  if (ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING == misfireInstruction) {
                    //Even if it tell me do nothing. I will log it here.
                    createdBy = createdBy + ".MFN=" + KiraCommonUtils
                        .getDateAsString(timerTrigger.getOriginalNextFireTimeWhenMisfireFound());
                  } else {
                    createdBy = createdBy + ".MF=" + KiraCommonUtils
                        .getDateAsString(timerTrigger.getOriginalNextFireTimeWhenMisfireFound());
                  }
                }

                Job job = triggerMetadataService
                    .createAndRunJobByTriggerMetadata(triggerMetadata, Boolean.FALSE, createdBy,
                        triggerTime);
                if (null == job) {
                  logger.warn(
                      "Can not create job for triggerMetadata={} , this version of trigger may not be exist?",
                      KiraCommonUtils.toString(triggerMetadata));
                } else {
                  if (misfireFoundWhenInstantiated && !misfireHandled) {
                    //Also mark it as handled even if it is of type ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
                    timerTrigger.setMisfireHandled(true);
                    if (ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING == misfireInstruction) {
                      logger
                          .warn("Misfired trigger was detected but do nothing for timerTrigger={}",
                              timerTrigger);
                    } else {
                      logger.warn("Misfire handled for timerTrigger={}", timerTrigger);
                    }
                  }
                }
              } else {
                logger.warn(
                    "canBeScheduled is false for triggerMetadata now. So do not run createAndRunJobByTriggerMetadata in fireTimerTriggerTask. masterZone={}",
                    masterZone);
              }
            } else {
              logger.warn("Can not find triggerMetadata when submitTask. timerTrigger={}",
                  timerTrigger);
            }
            this.updateForRunTimerTriggerTaskMonitorContextWhenSuccess();
          } catch (Throwable t) {
            this.updateForRunTimerTriggerTaskMonitorContextWhenFailed(appId, triggerId, triggerTime,
                t);
            throw t;
          }
        }
      });
    }
  }

  private ITimerTrigger doGetTimerTrigger(String timerTriggerId, boolean returnClonedObject)
      throws Exception {
    return this.timerTriggerScheduler.getTimerTrigger(timerTriggerId, returnClonedObject);
  }

  private List<TriggerIdentity> doGetManagedTriggerIdentityList() throws Exception {
    List<TriggerIdentity> returnValue = this.timerTriggerScheduler.getManagedTriggerIdentityList();
    return returnValue;
  }

  private void prepareForTimerTriggerTaskHandleThreadPoolExecutor() throws Exception {
    if (null != timerTriggerTaskHandleThreadPoolExecutor) {
      timerTriggerTaskHandleThreadPoolExecutor.shutdown();
    }

    int maxConcurrentTimerTriggerCount = KiraManagerUtils.getMaxConcurrentTimerTriggerCount();
    int corePoolSize = maxConcurrentTimerTriggerCount / 5;
    int maxPoolSize = maxConcurrentTimerTriggerCount * 10;
    long KeepAliveTimeInSeconds = 60L;
    int queueCapacity = 0;

    String threadNamePrefix = "TimerTriggerTaskHandleThread-";
    CustomizedThreadFactory customizedThreadFactory = new CustomizedThreadFactory(threadNamePrefix);
    BlockingQueue<Runnable> queue = null;
    if (queueCapacity > 0) {
      queue = new LinkedBlockingQueue<Runnable>(queueCapacity);
    } else {
      queue = new SynchronousQueue<Runnable>(true);
    }
    TimerTriggerTaskHandleThreadPoolExecutorRunsPolicy timerTriggerTaskHandleThreadPoolExecutorRunsPolicy = new TimerTriggerTaskHandleThreadPoolExecutorRunsPolicy();
    timerTriggerTaskHandleThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
        KeepAliveTimeInSeconds, TimeUnit.SECONDS, queue, customizedThreadFactory,
        timerTriggerTaskHandleThreadPoolExecutorRunsPolicy);
  }

  private void restartKiraTimerTriggerSchedulerAndCreateItIfNeeded() throws Exception {
    int maxConcurrentTimerTriggerCount = KiraManagerUtils.getMaxConcurrentTimerTriggerCount();
    if (null == timerTriggerScheduler) {
      timerTriggerScheduler = new KiraTimerTriggerScheduler(maxConcurrentTimerTriggerCount);
    }

    if (timerTriggerScheduler.isStarted()) {
      timerTriggerScheduler.shutdown();
    }
    timerTriggerScheduler.start();
  }

  private void shutdownKiraTimerTriggerSchedulerIfStarted() {
    if (null != timerTriggerScheduler) {
      if (timerTriggerScheduler.isStarted()) {
        timerTriggerScheduler.shutdown();
      }
    }
  }

  private void doUnscheduleKiraTimerTrigger(String appId, String triggerId) throws Exception {
    logger.info("doUnscheduleKiraTimerTrigger for appId={} and triggerId={}", appId, triggerId);
    String timerTriggerId = TimeScheduleUtils.getIdForTimerTrigger(triggerId, appId);
    this.timerTriggerScheduler.unscheduleTimerTrigger(timerTriggerId);
  }

  private void doRescheduleKiraTimerTrigger(String appId, String triggerId) throws Exception {
    logger.info("doRescheduleKiraTimerTrigger for appId={} and triggerId={}", appId, triggerId);

    TriggerMetadata triggerMetadata = this.triggerMetadataService
        .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
    if (null != triggerMetadata) {
      TimerTriggerSchedule timerTriggerSchedule = this.timerTriggerScheduleService
          .getTimerTriggerScheduleByPoolIdAndTriggerId(appId, triggerId);

      if (null != timerTriggerSchedule) {
        KiraTimeScheduleCallback kiraTimeScheduleCallback = new KiraTimeScheduleCallback(
            KiraManagerDataCenter.getServerId(), this.lastStartedTime, kiraServer,
            kiraClientMetadataService, triggerMetadataService, timerTriggerScheduleService, this);
        ITimerTrigger timerTrigger = KiraManagerUtils
            .getTimerTrigger(triggerMetadata, timerTriggerSchedule, kiraTimeScheduleCallback);
        if (null != timerTrigger) {
          this.initTimerTriggerScheduleDataInDBIfNeeded(triggerMetadata, timerTriggerSchedule,
              timerTrigger);

          this.timerTriggerScheduler.rescheduleTimerTrigger(timerTrigger);
        }
      } else {
        String errorMessage =
            "doRescheduleKiraTimerTrigger failed. For timerTriggerScheduleService.select return null by appId="
                + appId + " and triggerId=" + triggerId;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } else {
      String errorMessage =
          "doRescheduleKiraTimerTrigger failed. For getLatestTriggerMetadata return null by appId="
              + appId + " and triggerId=" + triggerId;
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }
  }

  private void initTimerTriggerScheduleDataInDBIfNeeded(TriggerMetadata triggerMetadata,
      TimerTriggerSchedule timerTriggerSchedule, ITimerTrigger timerTrigger) {
    if (null != timerTriggerSchedule && null != timerTrigger) {
      Long startTimeOfTimerTriggerSchedule = timerTriggerSchedule.getStartTime();
      //Need to try to update the schedule info in db if the startTimeOfTimerTriggerSchedule is null which means this trigger has never been scheduled before.
      if (null == startTimeOfTimerTriggerSchedule) {
        Date startTimeNow = timerTrigger.getStartTime();
        Date nextFireTimeNow = timerTrigger.getNextFireTime();
        Date previousFireTimeNow = timerTrigger.getPreviousFireTime();
        if (null != previousFireTimeNow) {
          logger.warn(
              "previousFireTimeNow should be null when previousFireTimeNow is null. May have some bugs. startTimeNow={} and previousFireTimeNow={} and nextFireTimeNow={} and triggerMetadata={} and timerTriggerSchedule={} and timerTrigger={}",
              KiraCommonUtils.getDateAsStringToMsPrecision(startTimeNow),
              KiraCommonUtils.getDateAsStringToMsPrecision(previousFireTimeNow),
              KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTimeNow),
              KiraCommonUtils.toString(triggerMetadata),
              KiraCommonUtils.toString(timerTriggerSchedule), timerTrigger);
        }

        if (null == startTimeNow) {
          logger.warn(
              "startTimeNow should not be null when previousFireTimeNow is null. May have some bugs. startTimeNow={} and previousFireTimeNow={} and nextFireTimeNow={} and triggerMetadata={} and timerTriggerSchedule={} and timerTrigger={}",
              KiraCommonUtils.getDateAsStringToMsPrecision(startTimeNow),
              KiraCommonUtils.getDateAsStringToMsPrecision(previousFireTimeNow),
              KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTimeNow),
              KiraCommonUtils.toString(triggerMetadata),
              KiraCommonUtils.toString(timerTriggerSchedule), timerTrigger);
        }

        Long startTimeNowAsLong = null;
        if (null != startTimeNow) {
          startTimeNowAsLong = Long.valueOf(startTimeNow.getTime());
        }

        Long nextFireTimeNowAsLong = null;
        if (null != nextFireTimeNow) {
          nextFireTimeNowAsLong = Long.valueOf(nextFireTimeNow.getTime());
        }

        timerTriggerSchedule.setStartTime(startTimeNowAsLong);
        timerTriggerSchedule.setNextFireTime(nextFireTimeNowAsLong);
        int updateCount = this.timerTriggerScheduleService.update(timerTriggerSchedule);
        if (updateCount != 1) {
          logger.warn(
              "Update timerTriggerSchedule do not return 1 which may be caused by recover? updateCount={} and timerTriggerSchedule={} and triggerMetadata={} and timerTrigger={}",
              updateCount, timerTriggerSchedule, KiraCommonUtils.toString(triggerMetadata),
              timerTrigger);
        }
      }
    } else {
      logger.warn(
          "timerTriggerSchedule and timerTrigger should both be not null. May have some bugs. timerTriggerSchedule={} and triggerMetadata={} and timerTrigger={}",
          timerTriggerSchedule, KiraCommonUtils.toString(triggerMetadata), timerTrigger);
    }
  }

  private String getIdentityOfKiraTimerTriggerLocalScheduler() {
    String returnValue = null;
    String serverId = KiraManagerDataCenter.getServerId();
    if (StringUtils.isNotBlank(serverId)) {
      returnValue = serverId.trim();
      if (null != this.lastStartedTime) {
        returnValue = returnValue + KiraCommonConstants.SPECIAL_DELIMITER + String
            .valueOf(this.lastStartedTime.getTime());
      } else {
        logger.warn(
            "lastStartedTime is null when getIdentityOfKiraTimerTriggerLocalScheduler. May have some bugs.");
      }
    } else {
      logger.warn(
          "serverId is null when getIdentityOfKiraTimerTriggerLocalScheduler. May have some bugs.");
    }

    return returnValue;
  }

  @Override
  protected <T> T doWorkWhenStartedAndAsLeader(String workDescription,
      String workContextInfo, Callable<T> work) throws Exception {
    return doTimerTriggerSchedulerRelatedWork(workDescription, workContextInfo, work);
  }

  @Override
  protected <T> T doWorkWhenStartedAndAsFollower(String workDescription,
      String workContextInfo, Callable<T> work) throws Exception {
    return doTimerTriggerSchedulerRelatedWork(workDescription, workContextInfo, work);
  }

  private <T> T doTimerTriggerSchedulerRelatedWork(String workDescription, String workContextInfo,
      Callable<T> work) throws Exception {
    T returnValue = null;
    if (null != timerTriggerScheduler) {
      if (timerTriggerScheduler.isStarted()) {
        returnValue = work.call();
      } else {
        String errorMessage = "timerTriggerScheduler is not started. so can not " + workDescription
            + ". May have some bug. " + workContextInfo;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } else {
      String errorMessage =
          "timerTriggerScheduler is null. so can not " + workDescription + ". May have some bug. "
              + workContextInfo;
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    return returnValue;
  }

  private static class KiraTimeScheduleCallback extends KiraTimeScheduleCallbackAdaptor {

    private String serverId;
    private Date lastStartedTime;
    private IKiraServer kiraServer;
    private KiraClientMetadataService kiraClientMetadataService;
    private TriggerMetadataService triggerMetadataService;
    private TimerTriggerScheduleService timerTriggerScheduleService;
    private IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler;

    public KiraTimeScheduleCallback(String serverId, Date lastStartedTime, IKiraServer kiraServer,
        KiraClientMetadataService kiraClientMetadataService,
        TriggerMetadataService triggerMetadataService,
        TimerTriggerScheduleService timerTriggerScheduleService,
        IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler) {
      super();
      this.serverId = serverId;
      this.lastStartedTime = lastStartedTime;
      this.kiraServer = kiraServer;
      this.kiraClientMetadataService = kiraClientMetadataService;
      this.triggerMetadataService = triggerMetadataService;
      this.timerTriggerScheduleService = timerTriggerScheduleService;
      this.kiraTimerTriggerLocalScheduler = kiraTimerTriggerLocalScheduler;
    }

    @Override
    public void preSubmitTask(ITimerTrigger timerTrigger)
        throws Exception {
      super.preSubmitTask(timerTrigger);
    }

    @Override
    public void submitTask(final ITimerTrigger timerTrigger)
        throws Exception {
      super.submitTask(timerTrigger);
      if (logger.isDebugEnabled()) {
        logger.debug("submitTask called for timerTrigger={}", timerTrigger);
      }
      kiraTimerTriggerLocalScheduler
          .submitTimerTriggerTask(serverId, lastStartedTime, timerTrigger);
    }

    @Override
    public void doAfterSubmitTaskSuccess(ITimerTrigger timerTrigger)
        throws Exception {
      this.kiraTimerTriggerLocalScheduler
          .updateForTimerTriggerScheduleMonitorContextWhenSuccess(timerTrigger);
    }

    @Override
    public void doAfterSubmitTaskFailed(ITimerTrigger timerTrigger)
        throws Exception {
      this.kiraTimerTriggerLocalScheduler
          .updateForTimerTriggerScheduleMonitorContextWhenFailed(timerTrigger);
    }

    @Override
    public void doAfterTriggerBeReadyForNextTriggering(
        ITimerTrigger timerTrigger) throws Exception {
      String appId = timerTrigger.getGroup();
      String triggerId = timerTrigger.getName();

      String identityInTrackingSystem = timerTrigger.getIdentityInTrackingSystem();
      TimerTriggerSchedule timerTriggerSchedule = timerTriggerScheduleService
          .select(Long.valueOf(identityInTrackingSystem));
      if (null != timerTriggerSchedule) {
        Date startTimeAsDate = timerTrigger.getStartTime();
        Long startTime = (null == startTimeAsDate) ? null : Long.valueOf(startTimeAsDate.getTime());

        Date previousFireTimeAsDate = timerTrigger.getPreviousFireTime();
        Long previousFireTime = (null == previousFireTimeAsDate) ? null
            : Long.valueOf(previousFireTimeAsDate.getTime());

        Date nextFireTimeAsDate = timerTrigger.getNextFireTime();
        Long nextFireTime =
            (null == nextFireTimeAsDate) ? null : Long.valueOf(nextFireTimeAsDate.getTime());

        Long timesTriggered = Long.valueOf(timerTrigger.getTimesHasBeenTriggered());

        timerTriggerSchedule.setStartTime(startTime);
        timerTriggerSchedule.setPreviousFireTime(previousFireTime);
        timerTriggerSchedule.setNextFireTime(nextFireTime);
        timerTriggerSchedule.setTimesTriggered(timesTriggered);

        TimerTriggerScheduleCriteria criteria = new TimerTriggerScheduleCriteria();
        criteria.setId(Long.valueOf(identityInTrackingSystem));
        int updateCount = timerTriggerScheduleService
            .updateByCriteria(timerTriggerSchedule, criteria);
        if (0 == updateCount) {
          logger.warn(
              "Failed to updateByCriteria for TimerTriggerSchedule. The dataVersion or createTime may be changed or that trigger may be no need to schedule now. timerTrigger={} and timerTriggerSchedule={}",
              timerTrigger, KiraCommonUtils.toString(timerTriggerSchedule));
        } else {
          if (null == nextFireTime) {
            TriggerMetadata triggerMetadata = this.triggerMetadataService
                .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
            if (null != triggerMetadata) {
              triggerMetadata.setFinalizedTime(previousFireTimeAsDate);
              triggerMetadataService.update(triggerMetadata);
            } else {
              logger.warn(
                  "Can not find the latestTriggerMetadata when try to update the finalizedTime after found the nextFireTime is null during doAfterTriggerBeReadyForNextTriggering. timerTrigger={}.",
                  timerTrigger);
            }
          }
        }
      } else {
        logger.warn(
            "Can not find TimerTriggerSchedule from db. It may be no need to schedule now. timerTrigger={}",
            timerTrigger);
      }
    }
  }

  public static class TimerTriggerTaskHandleThreadPoolExecutorRunsPolicy implements
      RejectedExecutionHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      logger.warn(
          "TimerTriggerTaskHandleThreadPoolExecutorRunsPolicy is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
          runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
          executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
      if (!executor.isShutdown()) {
        long startHandleTime = System.currentTimeMillis();
        try {
          runnable.run();
        } finally {
          long handleCostTime = System.currentTimeMillis() - startHandleTime;
          logger.warn("It takes " + handleCostTime
                  + " milliseconds for TimerTriggerTaskHandleThreadPoolExecutorRunsPolicy to run. runnable={} in thread={}",
              runnable, Thread.currentThread().getName());
        }
      } else {
        logger.warn("executor is shutdown. So no need to run runnable=" + runnable);
      }
    }
  }

  private static class TimerTriggerRecoveryMonitorTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(TimerTriggerRecoveryMonitorTask.class);


    private IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler;

    public TimerTriggerRecoveryMonitorTask(
        IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler) {
      super();
      this.kiraTimerTriggerLocalScheduler = kiraTimerTriggerLocalScheduler;
    }

    @Override
    public void run() {
      try {
        kiraTimerTriggerLocalScheduler.monitorTimerTriggerRecovery();
      } catch (Exception e) {
        logger.error("Error occurs when monitorTimerTriggerRecovery.", e);
      }
    }

  }
}
