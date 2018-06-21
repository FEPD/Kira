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

import com.yihaodian.architecture.kira.common.IComponent;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.server.event.KiraServerEvent;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface IKiraTimerTriggerLocalScheduler extends IComponent, EventHandler<KiraServerEvent> {

  public void unscheduleKiraTimerTrigger(String poolId, String triggerId) throws Exception;

  public void rescheduleKiraTimerTrigger(String poolId, String triggerId) throws Exception;

  public int getManagedTimerTriggerCount() throws Exception;

  public List<TriggerIdentity> getManagedTriggerIdentityList() throws Exception;

  public ITimerTrigger getTimerTrigger(String timerTriggerId, boolean returnClonedObject)
      throws Exception;

  public Collection<ITimerTrigger> getManagedTimerTriggers(boolean accurate) throws Exception;

  public void submitTimerTriggerTask(String serverId, Date lastStartedTime,
      ITimerTrigger timerTrigger) throws Exception;

  public void updateForTimerTriggerScheduleMonitorContextWhenSuccess(ITimerTrigger timerTrigger);

  public void updateForTimerTriggerScheduleMonitorContextWhenFailed(ITimerTrigger timerTrigger);

  public void monitorTimerTriggerRecovery() throws Exception;
}
