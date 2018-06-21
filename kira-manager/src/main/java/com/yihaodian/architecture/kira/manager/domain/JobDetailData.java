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
import java.util.Date;

public class JobDetailData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private Long triggerMetadataId;
  private String createdBy;
  private Date createTime;
  private Date runAtTime;
  private Integer jobStatusId;
  private Date lastUpdateTime;
  private Boolean manuallyScheduled;
  private String argumentsAsJsonArrayString;
  private String resultData;

  private String jobStatusName;

  private String appId;
  private String triggerId;
  private String version;

  public JobDetailData() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTriggerMetadataId() {
    return triggerMetadataId;
  }

  public void setTriggerMetadataId(Long triggerMetadataId) {
    this.triggerMetadataId = triggerMetadataId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
  }

  public Date getRunAtTime() {
    return runAtTime;
  }

  public void setRunAtTime(Date runAtTime) {
    this.runAtTime = runAtTime;
  }

  public String getRunAtTimeAsString() {
    return KiraCommonUtils.getDateAsString(runAtTime);
  }

  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Date lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getLastUpdateTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastUpdateTime);
  }

  public Boolean getManuallyScheduled() {
    return manuallyScheduled;
  }

  public void setManuallyScheduled(Boolean manuallyScheduled) {
    this.manuallyScheduled = manuallyScheduled;
  }

  public String getArgumentsAsJsonArrayString() {
    return argumentsAsJsonArrayString;
  }

  public void setArgumentsAsJsonArrayString(String argumentsAsJsonArrayString) {
    this.argumentsAsJsonArrayString = argumentsAsJsonArrayString;
  }

  public String getResultData() {
    return resultData;
  }

  public void setResultData(String resultData) {
    this.resultData = resultData;
  }

  public Integer getJobStatusId() {
    return jobStatusId;
  }

  public void setJobStatusId(Integer jobStatusId) {
    this.jobStatusId = jobStatusId;
  }

  public String getJobStatusName() {
    return jobStatusName;
  }

  public void setJobStatusName(String jobStatusName) {
    this.jobStatusName = jobStatusName;
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
