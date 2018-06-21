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
package com.yihaodian.architecture.kira.manager.core.server.impl;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.dto.KiraServerDetailData;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.dto.KiraServerInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class ClusterInternalService implements IClusterInternalService {

  private static Logger logger = LoggerFactory.getLogger(ClusterInternalService.class);

  public ClusterInternalService() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public KiraServerEntity getKiraServerEntity() throws Exception {
    KiraServerEntity returnValue = KiraManagerDataCenter.getKiraServer().getKiraServerEntity();
    return returnValue;
  }

  @Override
  public KiraServerDetailData getKiraServerDetailData() throws Exception {
    KiraServerInfo kiraServerInfo = KiraManagerDataCenter.getKiraServer().getKiraServerInfo();

    KiraServerDetailData returnValue = new KiraServerDetailData();
    Date serverBirthTime = KiraManagerDataCenter.getServerBirthTime();
    returnValue.setServerBirthTime(serverBirthTime);
    BeanUtils.copyProperties(kiraServerInfo, returnValue);

    returnValue.setCurrentKiraZoneId(KiraCrossMultiZoneUtils.getCurrentKiraZoneId());
    returnValue.setKiraMasterZoneId(KiraCrossMultiZoneUtils.getKiraMasterZoneId(false));
    returnValue
        .setKiraCrossMultiZoneRole(KiraManagerCrossMultiZoneUtils.getKiraCrossMultiZoneRole(false));
    returnValue.setLastSetKiraCrossMultiZoneRoleTime(
        KiraManagerCrossMultiZoneUtils.getLastSetKiraCrossMultiZoneRoleTime());

    int managedTimerTriggerCount = 0;
    if (KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler().isStarted(false)) {
      managedTimerTriggerCount = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
          .getManagedTimerTriggerCount();
    }
    returnValue.setManagedTimerTriggerCount(managedTimerTriggerCount);

    List<TriggerIdentity> managedTriggerIdentityList = new ArrayList<TriggerIdentity>();
    if (KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler().isStarted(false)) {
      managedTriggerIdentityList = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
          .getManagedTriggerIdentityList();
    }
    returnValue.setManagedTriggerIdentityList(managedTriggerIdentityList);

    LinkedHashSet<String> assignedServerIdBlackList = new LinkedHashSet<String>();
    if (KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter().isStarted(false)) {
      assignedServerIdBlackList = KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
          .getAssignedServerIdBlackList();
    }
    returnValue.setAssignedServerIdBlackList(assignedServerIdBlackList);

    Date lastUpdateAssignedServerIdBlackListTime = null;
    if (KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter().isStarted(false)) {
      lastUpdateAssignedServerIdBlackListTime = KiraManagerDataCenter
          .getKiraTimerTriggerScheduleCenter().getLastUpdateAssignedServerIdBlackListTime();
    }
    returnValue.setLastUpdateAssignedServerIdBlackListTime(lastUpdateAssignedServerIdBlackListTime);

    return returnValue;
  }

  @Override
  public void startKiraServer() throws Exception {
    try {
      logger.info("startKiraServer called.");
      Thread startKiraServerThread = new Thread(new Runnable() {
        public void run() {
          try {
            logger.info("startKiraServerThread start to run.");

            KiraManagerDataCenter.getKiraManagerCoreBootstrap().start();

          } catch (Exception e) {
            logger.error("Error occurs for startKiraServer.", e);
          }
        }
      });
      startKiraServerThread.setDaemon(true);
      startKiraServerThread.start();
      startKiraServerThread.join();
    } finally {
      logger.info("startKiraServer finished.");
    }
  }

  @Override
  public void shutdownKiraServer() throws Exception {
    try {
      logger.info("shutdownKiraServer called.");
      Thread shutdownKiraServerThread = new Thread(new Runnable() {
        public void run() {
          try {
            logger.info("shutdownKiraServerThread start to run.");

            KiraManagerDataCenter.getKiraManagerCoreBootstrap().shutdown();

          } catch (Exception e) {
            logger.error("Error occurs for shutdownKiraServer.", e);
          }
        }
      });
      shutdownKiraServerThread.setDaemon(true);
      shutdownKiraServerThread.start();
      shutdownKiraServerThread.join();
    } finally {
      logger.info("shutdownKiraServer finished.");
    }
  }

  @Override
  public void restartKiraServer() throws Exception {
    try {
      logger.info("restartKiraServer called.");
      Thread restartKiraServerThread = new Thread(new Runnable() {
        public void run() {
          try {
            logger.info("restartKiraServerThread start to run.");

            KiraManagerDataCenter.getKiraManagerCoreBootstrap().restart();
          } catch (Exception e) {
            logger.error("Error occurs for restartKiraServer.", e);
          }
        }
      });
      restartKiraServerThread.setDaemon(true);
      restartKiraServerThread.start();
      restartKiraServerThread.join();
    } finally {
      logger.info("restartKiraServer finished.");
    }
  }

  @Override
  public void destroyKiraServer() throws Exception {
    try {
      logger.info("destroyKiraServer called.");

      Thread destroyKiraServerThread = new Thread(new Runnable() {
        public void run() {
          try {
            logger.info("destroyKiraServerThread start to run.");
            KiraManagerDataCenter.getKiraManagerBootstrap().destroy();
          } catch (Exception e) {
            logger.error("Error occurs for destroyKiraServer.", e);
          }
        }
      });
      destroyKiraServerThread.setDaemon(true);
      destroyKiraServerThread.start();
      destroyKiraServerThread.join();
    } finally {
      logger.info("destroyKiraServer finished.");
    }
  }

  @Override
  public void doLeaderRoutineWorkOfKiraTimerTriggerMetadataManager()
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerMetadataManager().tryToDoLeaderRoutineWork();
  }

  @Override
  public void doLeaderRoutineWorkOfKiraTimerTriggerScheduleCenter()
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter().tryToDoLeaderRoutineWork();
  }

  @Override
  public void initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(
      String poolId, String triggerId) throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(poolId, triggerId);
  }

  @Override
  public void handleTriggerIdentityListWhichShouldBeUnScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled)
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .handleTriggerIdentityListWhichShouldBeUnScheduled(
            triggerIdentityListWhichShouldBeUnScheduled);
  }

  @Override
  public void addServerIdToAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      String serverId) throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .addServerIdToAssignedServerIdBlackList(serverId);
  }

  @Override
  public void removeServerIdFromAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      String serverId) throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .removeServerIdFromAssignedServerIdBlackList(serverId);
  }

  @Override
  public void clearAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter() throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter().clearAssignedServerIdBlackList();
  }

  @Override
  public LinkedHashSet<String> getAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter()
      throws Exception {
    return KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter().getAssignedServerIdBlackList();
  }

  @Override
  public void setAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      LinkedHashSet<String> assignedServerIdBlackList) throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .setAssignedServerIdBlackList(assignedServerIdBlackList);
  }

  @Override
  public Date getLastUpdateAssignedServerIdBlackListTimeOfKiraTimerTriggerScheduleCenter()
      throws Exception {
    return KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .getLastUpdateAssignedServerIdBlackListTime();
  }

  @Override
  public void loadBalanceForKiraTimerTriggerSchedule(int maxDoLoadBalanceRoundCount)
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerScheduleCenter()
        .loadBalanceForKiraTimerTriggerSchedule(maxDoLoadBalanceRoundCount);
  }

  @Override
  public void unscheduleKiraTimerTrigger(String poolId, String triggerId)
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
        .unscheduleKiraTimerTrigger(poolId, triggerId);
  }

  @Override
  public void rescheduleKiraTimerTrigger(String poolId, String triggerId)
      throws Exception {
    KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
        .rescheduleKiraTimerTrigger(poolId, triggerId);
  }

  @Override
  public List<TriggerIdentity> getManagedTriggerIdentityList()
      throws Exception {
    return KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
        .getManagedTriggerIdentityList();
  }

  @Override
  public ITimerTrigger getTimerTrigger(String timerTriggerId,
      boolean returnClonedObject) throws Exception {
    return KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
        .getTimerTrigger(timerTriggerId, returnClonedObject);
  }
}
