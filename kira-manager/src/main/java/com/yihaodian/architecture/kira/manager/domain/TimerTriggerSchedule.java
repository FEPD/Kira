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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;

public class TimerTriggerSchedule implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private String triggerId;

  private Long startTime;

  private Long previousFireTime;

  private Long nextFireTime;

  private Long timesTriggered;

  private String assignedServerId;

  private Long createTime;

  private Long dataVersion;

  public TimerTriggerSchedule() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public Long getPreviousFireTime() {
    return previousFireTime;
  }

  public void setPreviousFireTime(Long previousFireTime) {
    this.previousFireTime = previousFireTime;
  }

  public Long getNextFireTime() {
    return nextFireTime;
  }

  public void setNextFireTime(Long nextFireTime) {
    this.nextFireTime = nextFireTime;
  }

  public Long getTimesTriggered() {
    return timesTriggered;
  }

  public void setTimesTriggered(Long timesTriggered) {
    this.timesTriggered = timesTriggered;
  }

  public String getAssignedServerId() {
    return assignedServerId;
  }

  public void setAssignedServerId(String assignedServerId) {
    this.assignedServerId = assignedServerId;
  }

  public Long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsDateFormatString() {
    String returnValue = KiraCommonUtils.getLongInMsOfDateAsString(this.createTime);
    return returnValue;
  }

  public Long getDataVersion() {
    return dataVersion;
  }

  public void setDataVersion(Long dataVersion) {
    this.dataVersion = dataVersion;
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
    TimerTriggerSchedule other = (TimerTriggerSchedule) obj;
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
  public String toString() {
    return "TimerTriggerSchedule [id=" + id + ", appId=" + appId
        + ", triggerId=" + triggerId + ", startTime=" + KiraCommonUtils
        .getLongInMsOfDateAsString(startTime)
        + ", previousFireTime=" + KiraCommonUtils.getLongInMsOfDateAsString(previousFireTime)
        + ", nextFireTime="
        + KiraCommonUtils.getLongInMsOfDateAsString(nextFireTime) + ", timesTriggered="
        + timesTriggered
        + ", assignedServerId=" + assignedServerId + ", createTime="
        + KiraCommonUtils.getLongInMsOfDateAsString(createTime) + ", dataVersion=" + dataVersion
        + "]";
  }

}
