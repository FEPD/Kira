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
import com.yihaodian.architecture.kira.schedule.time.utils.TimeScheduleUtils;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTimerTrigger implements ITimerTrigger {

  public static final Long DEFAULT_MISFIRE_THRESHOLD_IN_MS = Long.valueOf(60000L);
  private static final long serialVersionUID = 1L;

  public static int DEFAULT_MISFIRE_INSTRUCTION = ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW;
  protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
  protected Date startTime;
  protected Date endTime;
  protected Date nextFireTime;
  protected Date previousFireTime;
  protected volatile long timesHasBeenTriggered;
  protected String identityInTrackingSystem;
  protected int misfireInstruction = AbstractTimerTrigger.DEFAULT_MISFIRE_INSTRUCTION;
  protected volatile Long misfireThresholdInMs = DEFAULT_MISFIRE_THRESHOLD_IN_MS;
  protected volatile boolean misfireFoundWhenInstantiated;
  protected volatile boolean misfireHandled;
  protected volatile Date originalNextFireTimeWhenMisfireFound;
  protected volatile boolean requestsRecovery;
  protected volatile boolean unscheduled;
  private String id;
  private String name;
  private String group;
  private transient ITimeScheduleCallback timeScheduleCallback;
  private int priority;
  private volatile long lifeOnHashedWheelInMs;
  private volatile int indexOnHashedWheel;
  private volatile long remainingRoundsOnHashedWheel;

  public AbstractTimerTrigger() {

  }

  public AbstractTimerTrigger(String name, String group, ITimeScheduleCallback timeScheduleCallback,
      int priority, Date startTime, Date endTime, Date previousFireTime, long timesHasBeenTriggered,
      String identityInTrackingSystem, int misfireInstruction, boolean requestsRecovery) {
    super();

    if (StringUtils.isBlank(name)) {
      throw new ValidationException(
          "name should not be blank.");
    }
    this.name = name;

    if (StringUtils.isBlank(group)) {
      throw new ValidationException(
          "group should not be blank.");
    }
    this.group = group;

    this.id = TimeScheduleUtils.getIdForTimerTrigger(name, group);

    if (null == timeScheduleCallback) {
      throw new ValidationException(
          "timeScheduleCallback should not be null. this.id=" + this.id);
    }
    this.timeScheduleCallback = timeScheduleCallback;

    if (priority < 0) {
      throw new ValidationException(
          "priority should be >= 0 . this.id=" + this.id);
    }
    this.priority = priority;

    if (startTime == null) {
      this.startTime = new Date();
    } else {
      this.startTime = startTime;
    }
    this.endTime = endTime;
    if (this.endTime != null && this.endTime.before(this.startTime)) {
      String message =
          "The end time is before start time. this.id=" + this.id + " and this.startTime="
              + KiraCommonUtils.getDateAsStringToMsPrecision(this.startTime) + " and this.endTime="
              + KiraCommonUtils.getDateAsStringToMsPrecision(this.endTime);
      logger.warn(message);
      //Do not throw exception now for it will block the schedule process. Exception should be thrown at higher level(Entrance for example).
      //throw new ValidationException(errorMessage);
    }

    if (timesHasBeenTriggered < 0) {
      throw new ValidationException(
          "timesHasBeenTriggered should be >=0 . this.id=" + this.id);
    }
    if ((null == previousFireTime && timesHasBeenTriggered > 0)
        || (null != previousFireTime && 0 == timesHasBeenTriggered)) {
      throw new ValidationException(
          "When previousFireTime is null,the timesHasBeenTriggered should be <=0. When previousFireTime is not null, the timesHasBeenTriggered should be >0 . this.id="
              + this.id);
    }
    this.previousFireTime = previousFireTime;
    this.timesHasBeenTriggered = timesHasBeenTriggered;

    this.identityInTrackingSystem = identityInTrackingSystem;

    this.misfireInstruction = misfireInstruction;

    this.requestsRecovery = requestsRecovery;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public ITimeScheduleCallback getTimeScheduleCallback() {
    return timeScheduleCallback;
  }

  public Date getStartTime() {
    return this.startTime;
  }

  public Date getEndTime() {
    return this.endTime;
  }

  @Override
  public Date getFinalFireTime() {
    Date returnValue = computeFinalFireTime();
    return returnValue;
  }

  protected void initializeIfNeeded(Date previousFireTime, Date nextFireTime) {
    if (null == previousFireTime && null == nextFireTime) {
      //If previousFireTime is null and nextFireTime is null, it means the first time to trigger, so need to initialize all data.
      computeFirstFireTime();
    } else {
      Date calculatedNextFireTime = nextFireTime;
      if (null != nextFireTime) {
        Long misfireThresholdInMs = this.getMisfireThresholdInMs();
        if (null != misfireThresholdInMs) {
          boolean isMisfired = KiraCommonUtils.isExpired(nextFireTime, misfireThresholdInMs);
          if (isMisfired) {
            this.misfireFoundWhenInstantiated = true;
            this.misfireHandled = false;
            this.originalNextFireTimeWhenMisfireFound = nextFireTime;

            if (ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING == this.misfireInstruction) {
              calculatedNextFireTime = this.calculateAndGetNextFireTimeAfter(new Date());
              logger.warn(
                  "Misfired trigger found but misfireInstruction tell me to do nothing. So reset the nextFireTime to the calcuated nextFireTime from the time now. this="
                      + this.toString());
            } else {
              calculatedNextFireTime = new Date();
              logger.warn(
                  "Misfired trigger found. So reset the nextFireTime to the current time to let it run once right now. this="
                      + this.toString());
            }
          }
        }
      }

      this.nextFireTime = calculatedNextFireTime;
    }
  }

  /**
   * Called by the scheduler at the time a Trigger is first added to the scheduler, in order to have
   * the <code>Trigger</code> compute its first fire time.
   *
   * After this method has been called, getNextFireTime() should return a valid answer.
   */
  protected abstract void computeFirstFireTime();

  protected abstract Date getFireTimeBefore(Date comparedTime);

  /**
   *
   */
  protected abstract Date computeFinalFireTime();

  @Override
  public Date getPreviousFireTime() {
    return previousFireTime;
  }

  @Override
  public Date getNextFireTime() {
    return nextFireTime;
  }

  @Override
  public long getTimesHasBeenTriggered() {
    return timesHasBeenTriggered;
  }

  @Override
  public String getIdentityInTrackingSystem() {
    return identityInTrackingSystem;
  }

  /**
   * The default value is AbstractTimerTrigger.DEFAULT_MISFIRE_INSTRUCTION
   */
  @Override
  public int getMisfireInstruction() {
    return misfireInstruction;
  }

  /**
   * The default value is 60000 milliseconds.
   */
  @Override
  public Long getMisfireThresholdInMs() {
    return misfireThresholdInMs;
  }

  @Override
  public boolean isMisfireFoundWhenInstantiated() {
    return misfireFoundWhenInstantiated;
  }

  @Override
  public boolean isMisfireHandled() {
    return misfireHandled;
  }

  @Override
  public void setMisfireHandled(boolean misfireHandled) {
    this.misfireHandled = misfireHandled;
  }

  @Override
  public Date getOriginalNextFireTimeWhenMisfireFound() {
    return originalNextFireTimeWhenMisfireFound;
  }

  @Override
  public boolean isRequestsRecovery() {
    return requestsRecovery;
  }

  @Override
  public void triggered() {
    timesHasBeenTriggered = timesHasBeenTriggered + 1;
    previousFireTime = nextFireTime;
    nextFireTime = calculateAndGetNextFireTimeAfter(nextFireTime);
  }

  @Override
  public boolean isUnscheduled() {
    return unscheduled;
  }

  @Override
  public void setUnscheduled(boolean unscheduled) {
    this.unscheduled = unscheduled;
  }

  @Override
  public long getLifeOnHashedWheelInMs() {
    return lifeOnHashedWheelInMs;
  }

  @Override
  public void setLifeOnHashedWheelInMs(long lifeOnHashedWheelInMs) {
    this.lifeOnHashedWheelInMs = lifeOnHashedWheelInMs;
  }

  @Override
  public int getIndexOnHashedWheel() {
    return indexOnHashedWheel;
  }

  @Override
  public void setIndexOnHashedWheel(int indexOnHashedWheel) {
    this.indexOnHashedWheel = indexOnHashedWheel;
  }

  @Override
  public long getRemainingRoundsOnHashedWheel() {
    return remainingRoundsOnHashedWheel;
  }

  @Override
  public void setRemainingRoundsOnHashedWheel(
      long remainingRoundsOnHashedWheel) {
    this.remainingRoundsOnHashedWheel = remainingRoundsOnHashedWheel;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
    AbstractTimerTrigger other = (AbstractTimerTrigger) obj;
    if (id == null) {
			if (other.id != null) {
				return false;
			}
    } else if (!id.equals(other.id)) {
			return false;
		}
    return true;
  }

  @Override
  public Object clone() {
    Object returnValue = null;
    try {
      returnValue = super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Not Cloneable.");
    }
    return returnValue;
  }

  /**
   * Do not work with ConcurrentSkipListSet and TreeSet which strictly require to obey the rules
   * described in Comparable. But i just want to use the id to identify the trigger. In this case no
   * way to obey that rule perfectly. This can now only work with Collections.sort
   */
  @Override
  public int compareTo(ITimerTrigger other) {
    if (null == other) {
      return 1;
    }

    if (this.equals(other)) {
      return 0;
    }

    Date myNextFireTime = getNextFireTime();
    Date otherNextFireTime = other.getNextFireTime();

    if (null == myNextFireTime && null == otherNextFireTime) {
      return 0;
    }

    if (null == myNextFireTime) {
      return 1;
    }

    if (null == otherNextFireTime) {
      return -1;
    }

    int nextFireTimeResult = myNextFireTime.compareTo(otherNextFireTime);
    if (0 != nextFireTimeResult) {
      return nextFireTimeResult;
    } else {
      int nextFireTimeCompareResult = myNextFireTime.compareTo(otherNextFireTime);
      if (0 != nextFireTimeCompareResult) {
        return nextFireTimeCompareResult;
      } else {
        int priorityCompareResult = this.getPriority() - other.getPriority();
        return -priorityCompareResult;
      }
    }
  }

}
