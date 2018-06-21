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

public class JobRunStatisticsCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private List<String> poolIdList;

  private String triggerId;

  private List<String> triggerIdList;

  private Date beginTime;

  private Date endTime;

  private Integer sampleCount;

  private Date createTime;

  public JobRunStatisticsCriteria() {
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

  public Date getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(Date beginTime) {
    this.beginTime = beginTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Integer getSampleCount() {
    return sampleCount;
  }

  public void setSampleCount(Integer sampleCount) {
    this.sampleCount = sampleCount;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}
