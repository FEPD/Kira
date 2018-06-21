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

public class Job implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private Long triggerMetadataId;

  private String appId;

  private String triggerId;

  private String version;

  private Date createTime;

  private Date lastUpdateTime;

  private Boolean manuallyScheduled;

  private String createdBy;

  private Date runAtTime;

  private Integer jobStatusId;

  private String argumentsAsJsonArrayString;

  private String resultData;

  private Integer dataVersion;

  public Job() {
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
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

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
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

  public Integer getJobStatusId() {
    return jobStatusId;
  }

  public void setJobStatusId(Integer jobStatusId) {
    this.jobStatusId = jobStatusId;
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

  public Integer getDataVersion() {
    return dataVersion;
  }

  public void setDataVersion(Integer dataVersion) {
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
    final Job other = (Job) obj;
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
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "id=" + "'" + id + "'" +
        ")";
  }

}
