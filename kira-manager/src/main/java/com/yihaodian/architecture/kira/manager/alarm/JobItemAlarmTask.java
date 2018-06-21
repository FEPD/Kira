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

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobItemAlarmTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(JobItemAlarmTask.class);

  private AlarmHelper alarmHelper;

  private String jobItemId;
  private Date createTime = new Date();

  public JobItemAlarmTask() {
    // TODO Auto-generated constructor stub
  }

  public JobItemAlarmTask(AlarmHelper alarmHelper, String jobItemId) {
    super();
    this.alarmHelper = alarmHelper;
    this.jobItemId = jobItemId;
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public void run() {
    try {
      alarmHelper.handleJobItemAlarmTask(this);
    } catch (Throwable t) {
      logger.error("Error occurs when running JobItemAlarmTask. jobItemId=" + jobItemId, t);
    }
  }

}
