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
package com.yihaodian.architecture.kira.schedule.time.internal.service.helper;

import com.yihaodian.architecture.kira.common.exception.KiraWorkCanceledException;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.ITimer;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraTimerTriggerToBeFiredHandleTask implements Callable<Object> {

  private static Logger logger = LoggerFactory.getLogger(KiraTimerTriggerToBeFiredHandleTask.class);

  private ITimer timer;
  private ITimerTrigger timerTrigger;

  public KiraTimerTriggerToBeFiredHandleTask(ITimer timer, ITimerTrigger timerTrigger) {
    this.timer = timer;
    this.timerTrigger = timerTrigger;
  }

  @Override
  public Object call() throws Exception {
    boolean isUnscheduled = timerTrigger.isUnscheduled();
    if (isUnscheduled) {
      logger.info(
          "The trigger is unscheduled. So do not handleTimerTriggerToBeFired. timerTrigger={} in timer={}",
          timerTrigger.getId(), timer.getId());
    } else {
      this.handleTimerTriggerToBeFired(timerTrigger);
    }

    return null;
  }

  private void handleTimerTriggerToBeFired(ITimerTrigger timerTrigger) {
    long startHandleTime = System.currentTimeMillis();
    try {
      ITimeScheduleCallback timeScheduleCallback = timerTrigger.getTimeScheduleCallback();
      boolean preSubmitTaskHandleSuccess = preSubmitTask(timeScheduleCallback, timerTrigger);
      if (preSubmitTaskHandleSuccess) {
        boolean submitHandleSuccess = submitTask(timeScheduleCallback, timerTrigger);
        if (submitHandleSuccess) {
          doAfterSubmitTaskSuccess(timeScheduleCallback, timerTrigger);

          //only need to move forward the trigger when submit success. So the misfire and recover scheme will work correctly.
          prepareForNextTriggering(timeScheduleCallback, timerTrigger);
        } else {
          boolean isUnscheduled = timerTrigger.isUnscheduled();
          if (isUnscheduled) {
            logger.info(
                "The trigger is unscheduled. So no need to call doAfterSubmitTaskFailed. timerTrigger={} in timer={}",
                timerTrigger.getId(), timer.getId());
          } else {
            doAfterSubmitTaskFailed(timeScheduleCallback, timerTrigger);
          }
        }
      } else {
        logger.warn(
            "preSubmitTask handle failed. So do not move forward the trigger. timerTrigger={}",
            timerTrigger);
      }
    } catch (KiraWorkCanceledException kiraWorkCanceledException) {
      logger.warn(
          "KiraWorkCanceledException occurs for KiraTimerTriggerToBeFiredHandleTask.handleTimerTriggerToBeFired. It may be caused by some reasonable reason. timerTrigger="
              + timerTrigger, kiraWorkCanceledException);
    } catch (Exception e) {
      logger.error("Exception occurs for handleTimerTriggerToBeFired. timerTrigger=" + timerTrigger,
          e);
    } finally {
      boolean unscheduled = timerTrigger.isUnscheduled();
      if (unscheduled) {
        logger.info(
            "The trigger was unscheduled so no need to call addTimerTrigger. timer's id={} and timerTrigger={}",
            timer.getId(), timerTrigger);
      } else {
        //always need to reschedule the trigger. Even if submit failed or KiraWorkCanceledException occurs, in that case the trigger will have the chance to be fired in the next time.
        this.timer.addTimerTrigger(timerTrigger);
      }

      if (logger.isDebugEnabled()) {
        long handleCostTime = System.currentTimeMillis() - startHandleTime;
        logger.debug("It takes " + handleCostTime
                + " milliseconds for handleTimerTriggerToBeFired. timerTrigger={}",
            timerTrigger.getId());
      }
    }
  }

  private void prepareForNextTriggering(ITimeScheduleCallback timeScheduleCallback,
      ITimerTrigger timerTrigger) {
    boolean isReadyForNextTriggering = this.timer.prepareForNextTriggering(timerTrigger);

    if (isReadyForNextTriggering) {
      try {
        timeScheduleCallback.doAfterTriggerBeReadyForNextTriggering(timerTrigger);
      } catch (Exception e) {
        logger.error(
            "Exception occurs when do something after it is ready for next triggerring. timerTrigger="
                + timerTrigger + " and timeScheduleCallback=" + timeScheduleCallback, e);
      }
    }
  }

  private void doAfterSubmitTaskSuccess(ITimeScheduleCallback timeScheduleCallback,
      ITimerTrigger timerTrigger) {
    try {
      timeScheduleCallback.doAfterSubmitTaskSuccess(timerTrigger);
    } catch (Exception e) {
      logger.error(
          "Exception occurs for timeScheduleCallback.doAfterSubmitTaskSuccess. timerTrigger="
              + timerTrigger + " and timeScheduleCallback=" + timeScheduleCallback, e);
    }
  }

  private void doAfterSubmitTaskFailed(ITimeScheduleCallback timeScheduleCallback,
      ITimerTrigger timerTrigger) {
    try {
      timeScheduleCallback.doAfterSubmitTaskFailed(timerTrigger);
    } catch (Exception e) {
      logger.error(
          "Exception occurs for timeScheduleCallback.doAfterSubmitTaskFailed. timerTrigger="
              + timerTrigger + " and timeScheduleCallback=" + timeScheduleCallback, e);
    }
  }

  private boolean submitTask(ITimeScheduleCallback timeScheduleCallback, ITimerTrigger timerTrigger)
      throws KiraWorkCanceledException {
    boolean returnValue = true;
    try {
      boolean isUnscheduled = timerTrigger.isUnscheduled();
      if (isUnscheduled) {
        logger.info(
            "The trigger is unscheduled. So do not call timeScheduleCallback.submitTask. timerTrigger={} in timer={}",
            timerTrigger.getId(), timer.getId());
        returnValue = false;
      } else {
        timeScheduleCallback.submitTask(timerTrigger);
      }
    } catch (KiraWorkCanceledException kiraWorkCanceledException) {
      logger.warn(
          "KiraWorkCanceledException occurs for timeScheduleCallback.submitTask. Do not regard it as failed,So just throw it out to let others know. timerTrigger="
              + timerTrigger + " and timeScheduleCallback=" + timeScheduleCallback,
          kiraWorkCanceledException);
      //Do not regard it as failed,So just throw it out to let others known
      throw kiraWorkCanceledException;
    } catch (Exception e) {
      logger.error(
          "Exception occurs for timeScheduleCallback.submitTask. timerTrigger=" + timerTrigger
              + " and timeScheduleCallback=" + timeScheduleCallback, e);
      returnValue = false;
    }
    return returnValue;
  }

  private boolean preSubmitTask(ITimeScheduleCallback timeScheduleCallback,
      ITimerTrigger timerTrigger) {
    boolean returnValue = false;
    try {
      boolean isUnscheduled = timerTrigger.isUnscheduled();
      if (isUnscheduled) {
        logger.info(
            "The trigger is unscheduled. So do not call timeScheduleCallback.preSubmitTask. timerTrigger={} in timer={}",
            timerTrigger.getId(), timer.getId());
      } else {
        timeScheduleCallback.preSubmitTask(timerTrigger);
        returnValue = true;
      }
    } catch (Exception e) {
      logger.error(
          "Exception occurs for timeScheduleCallback.preSubmitTask. timerTrigger=" + timerTrigger
              + " and timeScheduleCallback=" + timeScheduleCallback, e);
    }

    return returnValue;
  }

}
