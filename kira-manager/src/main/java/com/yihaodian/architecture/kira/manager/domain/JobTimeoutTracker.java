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
import java.util.Date;

public class JobTimeoutTracker implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private Date createTime;

  private String jobId;

  private Long rumTimeThreshold;

  private Date expectTimeoutTime;

  private Integer state;

  private Date lastUpdateStateTime;

  private String lastUpdateStateDetails;

  private Integer handleTimeoutFailedCount;

  private Integer dataVersion;

  public JobTimeoutTracker() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public Long getRumTimeThreshold() {
    return rumTimeThreshold;
  }

  public void setRumTimeThreshold(Long rumTimeThreshold) {
    this.rumTimeThreshold = rumTimeThreshold;
  }

  public Date getExpectTimeoutTime() {
    return expectTimeoutTime;
  }

  public void setExpectTimeoutTime(Date expectTimeoutTime) {
    this.expectTimeoutTime = expectTimeoutTime;
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

  public Date getLastUpdateStateTime() {
    return lastUpdateStateTime;
  }

  public void setLastUpdateStateTime(Date lastUpdateStateTime) {
    this.lastUpdateStateTime = lastUpdateStateTime;
  }

  public String getLastUpdateStateDetails() {
    return lastUpdateStateDetails;
  }

  public void setLastUpdateStateDetails(String lastUpdateStateDetails) {
    this.lastUpdateStateDetails = lastUpdateStateDetails;
  }

  public Integer getHandleTimeoutFailedCount() {
    return handleTimeoutFailedCount;
  }

  public void setHandleTimeoutFailedCount(Integer handleTimeoutFailedCount) {
    this.handleTimeoutFailedCount = handleTimeoutFailedCount;
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
    final JobTimeoutTracker other = (JobTimeoutTracker) obj;
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
    return "JobTimeoutTracker [id=" + id + ", createTime=" + createTime
        + ", jobId=" + jobId + ", rumTimeThreshold=" + rumTimeThreshold
        + ", expectTimeoutTime=" + expectTimeoutTime + ", state="
        + state + ", lastUpdateStateTime=" + lastUpdateStateTime
        + ", lastUpdateStateDetails=" + lastUpdateStateDetails
        + ", handleTimeoutFailedCount=" + handleTimeoutFailedCount
        + ", dataVersion=" + dataVersion + "]";
  }

}
