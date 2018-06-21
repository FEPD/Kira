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

public class QrtzFiredTriggers implements Serializable {

  private static final long serialVersionUID = 1L;

  private String entryId;

  private String triggerName;

  private String triggerGroup;

  private String isVolatile;

  private String instanceName;

  private Long firedTime;

  private Integer priority;

  private String state;

  private String jobName;

  private String jobGroup;

  private String isStateful;

  private String requestsRecovery;

  public QrtzFiredTriggers() {
  }

  public String getEntryId() {
    return entryId;
  }

  public void setEntryId(String entryId) {
    this.entryId = entryId;
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

  public String getIsVolatile() {
    return isVolatile;
  }

  public void setIsVolatile(String isVolatile) {
    this.isVolatile = isVolatile;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public Long getFiredTime() {
    return firedTime;
  }

  public void setFiredTime(Long firedTime) {
    this.firedTime = firedTime;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
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

  public String getIsStateful() {
    return isStateful;
  }

  public void setIsStateful(String isStateful) {
    this.isStateful = isStateful;
  }

  public String getRequestsRecovery() {
    return requestsRecovery;
  }

  public void setRequestsRecovery(String requestsRecovery) {
    this.requestsRecovery = requestsRecovery;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entryId == null) ? 0 : entryId.hashCode());

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
    final QrtzFiredTriggers other = (QrtzFiredTriggers) obj;
    if (entryId == null) {
      if (other.entryId != null) {
        return false;
      }
    } else if (!entryId.equals(other.entryId)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "entryId=" + "'" + entryId + "'" +
        ")";
  }

}
