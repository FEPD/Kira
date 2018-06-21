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

public class JobItemAlarmMessage extends TriggerRunStatusAlarmMessage {

  private static final long serialVersionUID = 1L;

  private String jobItemId;

  private Date jobItemCreateTime;

  private String serviceUrl;

  public JobItemAlarmMessage() {
    // TODO Auto-generated constructor stub
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

  public Date getJobItemCreateTime() {
    return jobItemCreateTime;
  }

  public void setJobItemCreateTime(Date jobItemCreateTime) {
    this.jobItemCreateTime = jobItemCreateTime;
  }

  public String getJobItemCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(jobItemCreateTime);
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

}
