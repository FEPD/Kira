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
package com.yihaodian.architecture.kira.schedule.time.callback;

import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;

/**
 * User can implements this interface to add callback logic during timeTrigger firing process. You
 * can use com.yihaodian.architecture.kira.schedule.time.callback.KiraTimeScheduleCallbackAdaptor
 * for convenience.
 */
public interface ITimeScheduleCallback {

  /**
   * Called before submit the task. <p/>e.g. Mark the trigger which will be fired in the store.<p/>
   *
   * @param timerTrigger. The timerTrigger object which is related to this callback.
   * @throws Exception exception when any error occurs.
   */
  void preSubmitTask(ITimerTrigger timerTrigger) throws Exception;

  /**
   * This will be called only after preSubmitTask is called and do not throw any exception.
   *
   * Submit the task to run when triggered.
   *
   * Caution: Should run the task asynchronous or it will block the time schedule process. So this
   * method is named as submitTask instead of runTask for special purpose.
   *
   * @param timerTrigger. The timerTrigger object which is related to this callback.
   * @throws Exception exception when any error occurs.
   */
  void submitTask(ITimerTrigger timerTrigger) throws Exception;

  /**
   * Called after submitTask is called and do not throw any exception. <p/>e.g. Delete the previous
   * marked trigger in the store.<p/>
   *
   * @param timerTrigger. The timerTrigger object which is related to this callback.
   * @throws Exception exception when any error occurs.
   */
  void doAfterSubmitTaskSuccess(ITimerTrigger timerTrigger) throws Exception;

  /**
   * Called after submitTask is called and throw some exception. <p/>e.g. Log this in the
   * store.<p/>
   *
   * @param timerTrigger. The timerTrigger object which is related to this callback.
   * @throws Exception exception when any error occurs.
   */
  void doAfterSubmitTaskFailed(ITimerTrigger timerTrigger) throws Exception;

  /**
   * Called after the trigger is ready for next triggering which mean that trigger has already
   * successfully submit the task and do not throw any exception and has update the data of itself
   * after calculated the next trigger time. <p/>e.g. Store the trigger data and/or mark the trigger
   * as completed in store.<p/>
   *
   * @param timerTrigger. The timerTrigger object which is related to this callback.
   * @throws Exception exception when any error occurs.
   */
  void doAfterTriggerBeReadyForNextTriggering(ITimerTrigger timerTrigger) throws Exception;
}
