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

package com.yihaodian.architecture.kira.manager.core.server.spi;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.dto.KiraServerDetailData;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

public interface IClusterInternalService {

  public KiraServerEntity getKiraServerEntity() throws Exception;

  public KiraServerDetailData getKiraServerDetailData() throws Exception;

  public void startKiraServer() throws Exception;

  public void shutdownKiraServer() throws Exception;

  public void restartKiraServer() throws Exception;

  public void destroyKiraServer() throws Exception;

  public void doLeaderRoutineWorkOfKiraTimerTriggerMetadataManager() throws Exception;

  public void doLeaderRoutineWorkOfKiraTimerTriggerScheduleCenter() throws Exception;

  public void initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(String poolId,
      String triggerId) throws Exception;

  public void handleTriggerIdentityListWhichShouldBeUnScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled) throws Exception;

  public void addServerIdToAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      String serverId) throws Exception;

  public void removeServerIdFromAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      String serverId) throws Exception;

  public void clearAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter() throws Exception;

  public LinkedHashSet<String> getAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter()
      throws Exception;

  public void setAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
      LinkedHashSet<String> assignedServerIdBlackList) throws Exception;

  public Date getLastUpdateAssignedServerIdBlackListTimeOfKiraTimerTriggerScheduleCenter()
      throws Exception;

  public void loadBalanceForKiraTimerTriggerSchedule(int maxDoLoadBalanceRoundCount)
      throws Exception;

  public void unscheduleKiraTimerTrigger(String poolId, String triggerId) throws Exception;

  public void rescheduleKiraTimerTrigger(String poolId, String triggerId) throws Exception;

  public List<TriggerIdentity> getManagedTriggerIdentityList() throws Exception;

  public ITimerTrigger getTimerTrigger(String timerTriggerId, boolean returnClonedObject)
      throws Exception;
}
