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
package com.yihaodian.architecture.kira.manager.criteria;

import java.util.Date;
import java.util.List;

public class TimerTriggerScheduleCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private List<String> poolIdList;

  private String triggerId;

  private List<String> triggerIdList;

  private String assignedServerId;

  private List<String> assignedServerIdList;

  private List<String> excludedAssignedServerIdList;

  private List<String> excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId;

  private Date misfireTime;

  private Long dataVersion;

  public TimerTriggerScheduleCriteria() {
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

  public void setAppId(String poolId) {
    this.appId = appId;
  }

  public List<String> getPoolIdList() {
    return poolIdList;
  }

  public void setPoolIdList(List<String> poolIdList) {
    this.poolIdList = poolIdList;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public List<String> getTriggerIdList() {
    return triggerIdList;
  }

  public void setTriggerIdList(List<String> triggerIdList) {
    this.triggerIdList = triggerIdList;
  }

  public String getAssignedServerId() {
    return assignedServerId;
  }

  public void setAssignedServerId(String assignedServerId) {
    this.assignedServerId = assignedServerId;
  }

  public List<String> getAssignedServerIdList() {
    return assignedServerIdList;
  }

  public void setAssignedServerIdList(List<String> assignedServerIdList) {
    this.assignedServerIdList = assignedServerIdList;
  }

  public List<String> getExcludedAssignedServerIdList() {
    return excludedAssignedServerIdList;
  }

  public void setExcludedAssignedServerIdList(
      List<String> excludedAssignedServerIdList) {
    this.excludedAssignedServerIdList = excludedAssignedServerIdList;
  }

  public List<String> getExcludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId() {
    return excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId;
  }

  public void setExcludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId(
      List<String> excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId) {
    this.excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId = excludedAssignedServerIdListAndIncludeThoseWithNullAssignedServerId;
  }

  public Date getMisfireTime() {
    return misfireTime;
  }

  public void setMisfireTime(Date misfireTime) {
    this.misfireTime = misfireTime;
  }

  public Long getMisfireTimeAsLong() {
    if (null != this.misfireTime) {
      return Long.valueOf(misfireTime.getTime());
    } else {
      return null;
    }
  }

  public Long getDataVersion() {
    return dataVersion;
  }

  public void setDataVersion(Long dataVersion) {
    this.dataVersion = dataVersion;
  }

}
