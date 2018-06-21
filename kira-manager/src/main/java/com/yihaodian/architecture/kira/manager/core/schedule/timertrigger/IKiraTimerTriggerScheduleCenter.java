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

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.server.util.IKiraServerEventHandleComponent;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

public interface IKiraTimerTriggerScheduleCenter extends IKiraServerEventHandleComponent {

  public void initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(String appId,
      String triggerId) throws Exception;

  public void handleTriggerIdentityListWhichShouldBeUnScheduled(
      List<TriggerIdentity> triggerIdentityListWhichShouldBeUnScheduled) throws Exception;

  public void addServerIdToAssignedServerIdBlackList(String serverId) throws Exception;

  public void removeServerIdFromAssignedServerIdBlackList(String serverId) throws Exception;

  public LinkedHashSet<String> getAssignedServerIdBlackList() throws Exception;

  public void setAssignedServerIdBlackList(LinkedHashSet<String> assignedServerIdBlackList)
      throws Exception;

  public Date getLastUpdateAssignedServerIdBlackListTime() throws Exception;

  public void clearAssignedServerIdBlackList() throws Exception;

  public void handleZoneRoleChange(final KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception;

  public void loadBalanceForKiraTimerTriggerSchedule(int maxDoLoadBalanceRoundCount)
      throws Exception;
}
