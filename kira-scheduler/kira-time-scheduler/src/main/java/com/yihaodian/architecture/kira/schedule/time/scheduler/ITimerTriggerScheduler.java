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
package com.yihaodian.architecture.kira.schedule.time.scheduler;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import java.util.Collection;
import java.util.List;

public interface ITimerTriggerScheduler {

  String getId();

  /**
   * @throws Exception exception when any error occurs.
   */
  void start() throws Exception;

  void shutdown();

  boolean isStarted();

  boolean isShutdown();


  /**
   * Reschedule timerTrigger which will unscheduleTimerTrigger first and then scheduleTimerTrigger.
   *
   * @return true if this timerTriggerScheduler has successfully scheduled the timerTrigger.
   * @throws Exception exception when any error occurs.
   */
  boolean rescheduleTimerTrigger(ITimerTrigger timerTrigger) throws Exception;

  /**
   * @return the unscheduled timerTrigger. return null if it do not exist or scheduler is not
   * started.
   * @throws Exception exception when any error occurs.
   */
  ITimerTrigger unscheduleTimerTrigger(String timerTriggerId) throws Exception;

  /**
   * @return the cloned or unCloned ITimerTrigger object. return null if it do not exist or
   * scheduler is not started.
   * @throws Exception exception when any error occurs.
   */
  ITimerTrigger getTimerTrigger(String timerTriggerId, boolean returnClonedObject) throws Exception;

  /**
   * @return the managed timerTriggerCount. return 0 if scheduler is not started.
   * @throws Exception exception when any error occurs.
   */
  int getManagedTimerTriggerCount() throws Exception;

  List<TriggerIdentity> getManagedTriggerIdentityList() throws Exception;

  Collection<ITimerTrigger> getManagedTimerTriggers(boolean accurate) throws Exception;
}
