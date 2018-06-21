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
import java.util.TimeZone;

public class KiraCronTimerTrigger extends AbstractTimerTrigger {

  public static final int YEAR_TO_GIVEUP_SCHEDULING_AT = 2299;
  //below copied from quartz to keep compatibility
  public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;
  private static final long serialVersionUID = 1L;
  private CronExpression cronEx;
  private TimeZone timeZone = TimeZone.getDefault();

  public KiraCronTimerTrigger() {

  }

  /**
   * @param name which should not be blank
   * @param group which should not be blank
   * @param priority which should be larger than 0. The higher priority will be handled first.
   * @param nextFireTime nextFireTime should be null when previousFireTime is null.
   * @param identityInTrackingSystem the identityInTrackingSystem in tracking system, it will be
   * null when no tracking system exist.
   */
  public KiraCronTimerTrigger(String name, String group, ITimeScheduleCallback timeScheduleCallback,
      int priority, Date startTime, Date endTime, Date previousFireTime, Date nextFireTime,
      long timesHasBeenTriggered, String cronExpression, String identityInTrackingSystem,
      int misfireInstruction, boolean requestsRecovery) throws ValidationException {
    super(name, group, timeScheduleCallback, priority, startTime, endTime, previousFireTime,
        timesHasBeenTriggered, identityInTrackingSystem, misfireInstruction, requestsRecovery);
    this.cronEx = new CronExpression(cronExpression, timeZone);

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
    nextFireTime = calculateAndGetNextFireTimeAfter(new Date(startTime.getTime()));
  }

  @Override
  protected Date getFireTimeBefore(Date comparedTime) {
    return (cronEx == null) ? null : cronEx.getTimeBefore(comparedTime);
  }

  @Override
  protected Date computeFinalFireTime() {
    Date returnValue = null;
    if (this.endTime != null) {
      returnValue = getFireTimeBefore(new Date(this.endTime.getTime()));
    } else {
      returnValue = (cronEx == null) ? null : cronEx.getFinalFireTime();
    }

    if ((returnValue != null) && (this.startTime != null) && (returnValue.before(this.startTime))) {
      returnValue = null;
    }

    return returnValue;
  }

  @Override
  public Date calculateAndGetNextFireTimeAfter(Date afterTime) {
    if (null == afterTime) {
      afterTime = new Date();
    }

    if (startTime.after(afterTime)) {
      //afterTime = new Date(startTime.getTime() - 1000L); //should not use this or the nextFireTime may be the same with previousFireTime for sometimes.
      afterTime = new Date(startTime.getTime());
    }

    if (null != endTime && (afterTime.compareTo(endTime) >= 0)) {
      return null;
    }

    Date time = getTimeAfter(afterTime);
    if (null != endTime && null != time && time.after(endTime)) {
      return null;
    }

    return time;
  }

  private Date getTimeAfter(Date afterTime) {
    return (cronEx == null) ? null : cronEx.getTimeAfter(afterTime);
  }

  public String toDetailString() {
    return "KiraCronTimerTrigger [ getId()=" + getId()
        + ", cronEx=" + cronEx
        + ", timeZone=" + timeZone
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

  @Override
  public String toString() {
    return "KiraCronTimerTrigger [ getId()=" + getId()
        + ", cronEx=" + cronEx
        + ", timeZone=" + timeZone.getID()
        + ", startTime=" + startTime
        + ", endTime=" + endTime
        + ", nextFireTime=" + nextFireTime
        + ", previousFireTime=" + previousFireTime
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

  @Override
  public Object clone() {
    KiraCronTimerTrigger copy = (KiraCronTimerTrigger) super.clone();
    if (cronEx != null) {
      copy.setCronExpression((CronExpression) cronEx.clone());
    }
    return copy;
  }

  public void setCronExpression(CronExpression cronExpression) {
    this.cronEx = cronExpression;
  }

}
