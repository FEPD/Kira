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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraTimeScheduleCallbackAdaptor implements ITimeScheduleCallback {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  public KiraTimeScheduleCallbackAdaptor() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  @Override
  public void preSubmitTask(ITimerTrigger timerTrigger) throws Exception {
  }

  @Override
  public void submitTask(ITimerTrigger timerTrigger) throws Exception {
  }

  @Override
  public void doAfterSubmitTaskSuccess(ITimerTrigger timerTrigger) throws Exception {
  }

  @Override
  public void doAfterSubmitTaskFailed(ITimerTrigger timerTrigger) throws Exception {
    logger.error("doAfterSubmitTaskFailed for " + timerTrigger.getId());
  }

  @Override
  public void doAfterTriggerBeReadyForNextTriggering(ITimerTrigger timerTrigger) throws Exception {
  }
}
