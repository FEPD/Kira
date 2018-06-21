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
package com.yihaodian.architecture.kira.common.dto;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobItemStatusReport implements Serializable {

  private static final long serialVersionUID = 1L;

  private String jobItemId;
  private Integer jobStatusId;
  private String resultData;
  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  private Date createTime;

  public JobItemStatusReport() {
    // TODO Auto-generated constructor stub
  }

  public JobItemStatusReport(String jobItemId, Integer jobStatusId) {
    this(jobItemId, jobStatusId, null);
  }

  public JobItemStatusReport(String jobItemId, Integer jobStatusId,
      String resultData) {
    this(jobItemId, jobStatusId, resultData, new Date());
  }

  public JobItemStatusReport(String jobItemId, Integer jobStatusId,
      String resultData, Date createTime) {
    super();
    this.jobItemId = jobItemId;
    this.jobStatusId = jobStatusId;
    this.resultData = resultData;
    this.createTime = createTime;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getJobItemId() {
    return jobItemId;
  }

  public void setJobItemId(String jobItemId) {
    this.jobItemId = jobItemId;
  }

  public Integer getJobStatusId() {
    return jobStatusId;
  }

  public void setJobStatusId(Integer jobStatusId) {
    this.jobStatusId = jobStatusId;
  }

  public String getResultData() {
    return resultData;
  }

  public void setResultData(String resultData) {
    this.resultData = resultData;
  }

  public Map<Serializable, Serializable> getOtherDataMap() {
    return otherDataMap;
  }

  public void setOtherDataMap(
      Map<Serializable, Serializable> otherDataMap) {
    this.otherDataMap = otherDataMap;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((createTime == null) ? 0 : createTime.hashCode());
    result = prime * result
        + ((jobItemId == null) ? 0 : jobItemId.hashCode());
    result = prime * result
        + ((jobStatusId == null) ? 0 : jobStatusId.hashCode());
    result = prime * result
        + ((otherDataMap == null) ? 0 : otherDataMap.hashCode());
    result = prime * result
        + ((resultData == null) ? 0 : resultData.hashCode());
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
    if (!(obj instanceof JobItemStatusReport)) {
      return false;
    }
    JobItemStatusReport other = (JobItemStatusReport) obj;
    if (createTime == null) {
      if (other.createTime != null) {
        return false;
      }
    } else if (!createTime.equals(other.createTime)) {
      return false;
    }
    if (jobItemId == null) {
      if (other.jobItemId != null) {
        return false;
      }
    } else if (!jobItemId.equals(other.jobItemId)) {
      return false;
    }
    if (jobStatusId == null) {
      if (other.jobStatusId != null) {
        return false;
      }
    } else if (!jobStatusId.equals(other.jobStatusId)) {
      return false;
    }
    if (otherDataMap == null) {
      if (other.otherDataMap != null) {
        return false;
      }
    } else if (!otherDataMap.equals(other.otherDataMap)) {
      return false;
    }
    if (resultData == null) {
      if (other.resultData != null) {
        return false;
      }
    } else if (!resultData.equals(other.resultData)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JobItemStatusReport [jobItemId=" + jobItemId + ", jobStatusId="
        + jobStatusId + ", resultData=" + resultData
        + ", otherDataMap=" + otherDataMap + ", createTime="
        + createTime + "]";
  }

}
