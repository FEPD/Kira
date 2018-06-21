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
package com.yihaodian.architecture.kira.schedule.time.trigger;

import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import java.io.Serializable;
import java.util.Date;

public interface ITimerTrigger extends IHashedWheelTimerAwareScheduleTarget, Serializable,
    Cloneable, Comparable<ITimerTrigger> {

  //The misfireInstruction keep the same values as quartz, but only parts are useful and rules are different.
  int MISFIRE_INSTRUCTION_RUN_ONCE_NOW = 0;
  int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

  /**
   * The id is used to identify a trigger.
   */
  String getId();

  String getName();

  String getGroup();

  /**
   * The higher priority will be handled first
   */
  int getPriority();

  /**
   * return the TimeScheduleCallback which will be used during trigger process.
   */
  ITimeScheduleCallback getTimeScheduleCallback();

  /**
   * @return the startTime which should not be null
   */
  Date getStartTime();

  Date getEndTime();

  Date calculateAndGetNextFireTimeAfter(Date afterTime);

  /**
   * @return the final fire time. May return null which means the trigger will be triggered
   * infinitely or will never be triggered in the future.
   */
  Date getFinalFireTime();

  /**
   * If the trigger has not ever fired, null will be returned.
   *
   * @return the previous fire time.
   */
  Date getPreviousFireTime();

  /**
   * If the trigger will not fire again, null will be returned.
   *
   * @return the next fire time.
   */
  Date getNextFireTime();

  long getTimesHasBeenTriggered();

  String getIdentityInTrackingSystem();

  /**
   * Follow the rules below: a. value of ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW means if
   * misfire found, it will be fired right now. b. value of ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
   * means if misfire found, it will not be fired now and need to compute the nextFireTime based on
   * the current time. c. other values will follow the rule of a .
   */
  int getMisfireInstruction();

  /**
   * When a trigger has not been fired after this threshold after the nextFireTime it is regarded as
   * misfired.
   *
   * @return the threshold in milliseconds which means the trigger has been misfired
   */
  Long getMisfireThresholdInMs();

  boolean isMisfireFoundWhenInstantiated();

  boolean isMisfireHandled();

  void setMisfireHandled(boolean misfireHandled);

  Date getOriginalNextFireTimeWhenMisfireFound();

  /**
   * @return whether or not the the scheduler should re-execute the job of this trigger if crash
   * occurs when scheduler firing this job of this trigger.
   */
  boolean isRequestsRecovery();

  /**
   * give the trigger a chance to update itself for its next triggering
   */
  void triggered();

  /**
   * If this timerTrigger is scheduled by the scheduler.
   *
   * Caution: Used internally only. The user should not call this method directly.
   */
  boolean isUnscheduled();

  /**
   * Set if this timerTrigger is scheduled by the scheduler.
   *
   * Caution: Used internally only. The user should not call this method directly.
   */
  void setUnscheduled(boolean unscheduled);

  Object clone();
}
