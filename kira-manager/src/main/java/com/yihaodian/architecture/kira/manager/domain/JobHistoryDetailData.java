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

public class JobHistoryDetailData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String jobId;
  private Integer jobStatusId;
  private String resultData;
  private Date createTime;

  private String jobStatusName;

  public JobHistoryDetailData() {
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

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
  }

  public String getJobStatusName() {
    return jobStatusName;
  }

  public void setJobStatusName(String jobStatusName) {
    this.jobStatusName = jobStatusName;
  }

  @Override
  public String toString() {
    return "JobHistoryDetailData [id=" + id + ", jobId=" + jobId
        + ", jobStatusId=" + jobStatusId + ", resultData=" + resultData
        + ", createTime=" + createTime + ", jobStatusName="
        + jobStatusName + "]";
  }

}
