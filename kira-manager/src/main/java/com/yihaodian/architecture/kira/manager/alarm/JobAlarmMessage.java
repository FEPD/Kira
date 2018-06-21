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
package com.yihaodian.architecture.kira.manager.alarm;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.util.Date;

public class JobAlarmMessage extends TriggerRunStatusAlarmMessage {

  private static final long serialVersionUID = 1L;

  private String jobId;

  private String createdBy;

  private Boolean manuallyScheduled;

  private Date jobCreateTime;

  public JobAlarmMessage() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Boolean getManuallyScheduled() {
    return manuallyScheduled;
  }

  public void setManuallyScheduled(Boolean manuallyScheduled) {
    this.manuallyScheduled = manuallyScheduled;
  }

  public Date getJobCreateTime() {
    return jobCreateTime;
  }

  public void setJobCreateTime(Date jobCreateTime) {
    this.jobCreateTime = jobCreateTime;
  }

  public String getJobCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(jobCreateTime);
  }

}
