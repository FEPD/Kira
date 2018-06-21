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

public class JobAlarmTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(JobAlarmTask.class);

  private AlarmHelper alarmHelper;

  private String jobId;
  private Date createTime = new Date();

  public JobAlarmTask() {
    // TODO Auto-generated constructor stub
  }

  public JobAlarmTask(AlarmHelper alarmHelper, String jobId) {
    super();
    this.alarmHelper = alarmHelper;
    this.jobId = jobId;
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public void run() {
    try {
      alarmHelper.handleJobAlarmTask(this);
    } catch (Throwable t) {
      logger.error("Error occurs when running JobAlarmTask. jobId=" + jobId, t);
    }
  }

}
