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

public class JobRunStatistics implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private String triggerId;

  private Date beginTime;

  private Date endTime;

  private Integer sampleCount;

  private Integer maxInSeconds;

  private Integer minInSeconds;

  private Integer avgInSeconds;

  private Date createTime;

  public JobRunStatistics() {
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

  public Integer getMaxInSeconds() {
    return maxInSeconds;
  }

  public void setMaxInSeconds(Integer maxInSeconds) {
    this.maxInSeconds = maxInSeconds;
  }

  public Integer getMinInSeconds() {
    return minInSeconds;
  }

  public void setMinInSeconds(Integer minInSeconds) {
    this.minInSeconds = minInSeconds;
  }

  public Integer getAvgInSeconds() {
    return avgInSeconds;
  }

  public void setAvgInSeconds(Integer avgInSeconds) {
    this.avgInSeconds = avgInSeconds;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
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
    final JobRunStatistics other = (JobRunStatistics) obj;
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
