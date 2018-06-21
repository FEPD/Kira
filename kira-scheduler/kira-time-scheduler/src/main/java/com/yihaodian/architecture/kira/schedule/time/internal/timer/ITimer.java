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
package com.yihaodian.architecture.kira.schedule.time.internal.timer;

import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;

/**
 * This interface is used internally only.
 */
public interface ITimer {

  String getId();

  void start() throws Exception;

  void shutdown();

  /**
   * @return true if this timer did not already contain the specified timerTrigger
   */
  boolean addTimerTrigger(ITimerTrigger timerTrigger);

  /**
   * @param isUnscheduled whether to set the timerTrigger to be unscheduled
   * @return true if this timer contained the specified timerTrigger and be removed
   */
  boolean removeTimerTrigger(ITimerTrigger timerTrigger, boolean unscheduled);

  /**
   * @return true if it is prepared well and is ready for next triggering for the specified
   * timerTrigger
   */
  boolean prepareForNextTriggering(ITimerTrigger timerTrigger);
}
