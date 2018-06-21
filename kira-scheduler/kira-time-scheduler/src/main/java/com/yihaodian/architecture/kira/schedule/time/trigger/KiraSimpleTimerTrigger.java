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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import java.util.Date;

public class KiraSimpleTimerTrigger extends AbstractTimerTrigger {

  /**
   * Used to indicate the 'repeat count' of the trigger is indefinite. Or in other words, the
   * trigger should repeat continually until the trigger's ending timestamp.
   */
  public static final int REPEAT_INDEFINITELY = -1;
  //below copied from quartz to keep compatibility
  public static final int MISFIRE_INSTRUCTION_FIRE_NOW = 1;
  public static final int MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT = 2;
  public static final int MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT = 3;
  public static final int MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT = 4;
  public static final int MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT = 5;
  private static final long serialVersionUID = 1L;
  private int repeatCount = 0;
  private long repeatIntervalInMs = 0;

  public KiraSimpleTimerTrigger() {
  }

  /**
   * @param name which should not be blank
   * @param group which should not be blank
   * @param priority which should be larger than 0. The higher priority will be handled first.
   * @param nextFireTime nextFireTime should be null when previousFireTime is null.
   * @param repeatCount -1 means unlimited repeat times.
   * @param repeatIntervalInMs repeat intervals in milliseconds
   * @param identityInTrackingSystem the identityInTrackingSystem in tracking system, it will be
   * null when no tracking system exist.
   */
  public KiraSimpleTimerTrigger(String name, String group,
      ITimeScheduleCallback timeScheduleCallback,
      int priority, Date startTime, Date endTime, Date previousFireTime, Date nextFireTime,
      long timesHasBeenTriggered, int repeatCount, long repeatIntervalInMs,
      String identityInTrackingSystem, int misfireInstruction, boolean requestsRecovery)
      throws ValidationException {
    super(name, group, timeScheduleCallback, priority, startTime, endTime, previousFireTime,
        timesHasBeenTriggered, identityInTrackingSystem, misfireInstruction, requestsRecovery);

    if (repeatCount != REPEAT_INDEFINITELY && repeatCount < 0) {
      throw new ValidationException(
          "Repeat count must be >= 0, use the "
              + "constant REPEAT_INDEFINITELY for infinite.");
    }
    this.repeatCount = repeatCount;

    if (repeatIntervalInMs < 0) {
      throw new ValidationException(
          "Repeat interval must be >= 0");
    }

    this.repeatIntervalInMs = repeatIntervalInMs;

    this.initializeIfNeeded(previousFireTime, nextFireTime);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void computeFirstFireTime() {
    if (null != endTime && (startTime.compareTo(endTime) >= 0)) {
      nextFireTime = null;
    } else {
      nextFireTime = startTime;
    }
  }

  @Override
  protected Date getFireTimeBefore(Date comparedTime) {
    if (comparedTime.getTime() < this.startTime.getTime()) {
      return null;
    }

    int numFires = computeNumTimesFiredBetween(this.startTime, comparedTime);

    return new Date(this.startTime.getTime() + (numFires * this.repeatIntervalInMs));
  }

  private int computeNumTimesFiredBetween(Date start, Date end) {
    int returnValue = 0;

    if (this.repeatIntervalInMs > 1) {
      long time = end.getTime() - start.getTime();
      returnValue = (int) (time / this.repeatIntervalInMs);
    }

    return returnValue;
  }

  @Override
  protected Date computeFinalFireTime() {
    Date returnValue = null;
    if (repeatCount == 0) {
      returnValue = this.startTime;
    } else if (repeatCount == REPEAT_INDEFINITELY) {
      returnValue = (this.endTime == null) ? null : this.getFireTimeBefore(this.endTime);
    } else {
      long lastTrigger = startTime.getTime() + (repeatCount * this.repeatIntervalInMs);

      if ((this.endTime == null) || (lastTrigger < this.endTime.getTime())) {
        returnValue = new Date(lastTrigger);
      } else {
        returnValue = this.getFireTimeBefore(this.endTime);
      }
    }

    return returnValue;
  }

  @Override
  public Date calculateAndGetNextFireTimeAfter(Date afterTime) {
    if ((repeatCount != REPEAT_INDEFINITELY)
        && (timesHasBeenTriggered > repeatCount)) {
      return null;
    }

    if (afterTime == null) {
      afterTime = new Date();
    }

    if (repeatCount == 0 && afterTime.compareTo(startTime) >= 0) {
      return null;
    }

    long startMillis = startTime.getTime();
    long afterMillis = afterTime.getTime();
    long endMillis = (endTime == null) ? Long.MAX_VALUE : endTime.getTime();

    if (endMillis <= afterMillis) {
      return null;
    }

    if (afterMillis < startMillis) {
      return new Date(startMillis);
    }

    long numberOfTimesExecuted = ((afterMillis - startMillis) / repeatIntervalInMs) + 1;

    if ((numberOfTimesExecuted > repeatCount) &&
        (repeatCount != REPEAT_INDEFINITELY)) {
      return null;
    }

    Date time = new Date(startMillis + (numberOfTimesExecuted * repeatIntervalInMs));

    if (endMillis <= time.getTime()) {
      return null;
    }

    return time;
  }

  public String toDetailString() {
    return "KiraSimpleTimerTrigger [ getId()=" + getId()
        + ", startTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(startTime)
        + ", endTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(endTime)
        + ", nextFireTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTime)
        + ", previousFireTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(previousFireTime)
        + ", repeatCount=" + repeatCount
        + ", repeatIntervalInMs=" + repeatIntervalInMs
        + ", getName()=" + getName() + ", getGroup()=" + getGroup()
        + ", getTimeScheduleCallback()=" + getTimeScheduleCallback()
        + ", getPriority()=" + getPriority()
        + ", isUnscheduled()=" + isUnscheduled()
        + ", timesHasBeenTriggered=" + timesHasBeenTriggered
        + ", identityInTrackingSystem=" + identityInTrackingSystem
        + ", misfireThresholdInMs=" + misfireThresholdInMs
        + ", misfireFoundWhenInstantiated=" + misfireFoundWhenInstantiated
        + ", misfireHandled=" + misfireHandled
        + ", originalNextFireTimeWhenMisfireFound=" + KiraCommonUtils
        .getDateAsString(originalNextFireTimeWhenMisfireFound)
        + ", getLifeOnHashedWheelInMs()=" + getLifeOnHashedWheelInMs()
        + ", getIndexOnHashedWheel()=" + getIndexOnHashedWheel()
        + ", getRemainingRoundsOnHashedWheel()=" + getRemainingRoundsOnHashedWheel() + "]";
  }

  @Override
  public String toString() {
    return "KiraSimpleTimerTrigger [ getId()=" + getId()
        + ", repeatCount=" + repeatCount
        + ", repeatIntervalInMs=" + repeatIntervalInMs
        + ", startTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(startTime)
        + ", endTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(endTime)
        + ", nextFireTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTime)
        + ", previousFireTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(previousFireTime)
        + ", getName()=" + getName()
        + ", getGroup()=" + getGroup()
        + ", getTimeScheduleCallback()=" + getTimeScheduleCallback()
        + ", getPriority()=" + getPriority()
        + ", isUnscheduled()=" + isUnscheduled()
        + ", timesHasBeenTriggered=" + timesHasBeenTriggered
        + ", identityInTrackingSystem=" + identityInTrackingSystem
        + ", misfireThresholdInMs=" + misfireThresholdInMs
        + ", misfireFoundWhenInstantiated=" + misfireFoundWhenInstantiated
        + ", misfireHandled=" + misfireHandled
        + ", originalNextFireTimeWhenMisfireFound=" + KiraCommonUtils
        .getDateAsString(originalNextFireTimeWhenMisfireFound)
        + ", getLifeOnHashedWheelInMs()=" + getLifeOnHashedWheelInMs()
        + ", getIndexOnHashedWheel()=" + getIndexOnHashedWheel()
        + ", getRemainingRoundsOnHashedWheel()=" + getRemainingRoundsOnHashedWheel() + "]";
  }

}
