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
package com.yihaodian.architecture.kira.manager.domain;

import java.io.Serializable;

public class QrtzTriggers implements Serializable {

  private static final long serialVersionUID = 1L;

  private String triggerName;

  private String triggerGroup;

  private String jobName;

  private String jobGroup;

  private String isVolatile;

  private String description;

  private Long nextFireTime;

  private Long prevFireTime;

  private Integer priority;

  private String triggerState;

  private String triggerType;

  private Long startTime;

  private Long endTime;

  private String calendarName;

  private Integer misfireInstr;

  private byte[] jobData;

  public QrtzTriggers() {
  }

  public String getTriggerName() {
    return triggerName;
  }

  public void setTriggerName(String triggerName) {
    this.triggerName = triggerName;
  }

  public String getTriggerGroup() {
    return triggerGroup;
  }

  public void setTriggerGroup(String triggerGroup) {
    this.triggerGroup = triggerGroup;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getJobGroup() {
    return jobGroup;
  }

  public void setJobGroup(String jobGroup) {
    this.jobGroup = jobGroup;
  }

  public String getIsVolatile() {
    return isVolatile;
  }

  public void setIsVolatile(String isVolatile) {
    this.isVolatile = isVolatile;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getNextFireTime() {
    return nextFireTime;
  }

  public void setNextFireTime(Long nextFireTime) {
    this.nextFireTime = nextFireTime;
  }

  public Long getPrevFireTime() {
    return prevFireTime;
  }

  public void setPrevFireTime(Long prevFireTime) {
    this.prevFireTime = prevFireTime;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getTriggerState() {
    return triggerState;
  }

  public void setTriggerState(String triggerState) {
    this.triggerState = triggerState;
  }

  public String getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public String getCalendarName() {
    return calendarName;
  }

  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  public Integer getMisfireInstr() {
    return misfireInstr;
  }

  public void setMisfireInstr(Integer misfireInstr) {
    this.misfireInstr = misfireInstr;
  }

  public byte[] getJobData() {
    return jobData;
  }

  public void setJobData(byte[] jobData) {
    this.jobData = jobData;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((triggerGroup == null) ? 0 : triggerGroup.hashCode());
    result = prime * result + ((triggerName == null) ? 0 : triggerName.hashCode());

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
    final QrtzTriggers other = (QrtzTriggers) obj;
    if (triggerGroup == null) {
      if (other.triggerGroup != null) {
        return false;
      }
    } else if (!triggerGroup.equals(other.triggerGroup)) {
      return false;
    }
    if (triggerName == null) {
      if (other.triggerName != null) {
        return false;
      }
    } else if (!triggerName.equals(other.triggerName)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "triggerGroup=" + "'" + triggerGroup + "'" + ", " +
        "triggerName=" + "'" + triggerName + "'" +
        ")";
  }

}
