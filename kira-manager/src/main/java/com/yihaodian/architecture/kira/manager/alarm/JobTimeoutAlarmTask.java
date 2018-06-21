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

import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.event.jobtimeouttracker.JobTimeoutHandleFailedEvent;
import com.yihaodian.architecture.kira.manager.event.jobtimeouttracker.JobTimeoutTrackerEventType;
import com.yihaodian.architecture.kira.manager.util.JobTimeoutTrackerStateEnum;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTimeoutAlarmTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(JobTimeoutAlarmTask.class);

  private AlarmHelper alarmHelper;

  private JobTimeoutTracker jobTimeoutTracker;
  private Date createTime = new Date();

  public JobTimeoutAlarmTask() {
    // TODO Auto-generated constructor stub
  }

  public JobTimeoutAlarmTask(AlarmHelper alarmHelper, JobTimeoutTracker jobTimeoutTracker) {
    super();
    this.alarmHelper = alarmHelper;
    this.jobTimeoutTracker = jobTimeoutTracker;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public JobTimeoutTracker getJobTimeoutTracker() {
    return jobTimeoutTracker;
  }

  public void setJobTimeoutTracker(JobTimeoutTracker jobTimeoutTracker) {
    this.jobTimeoutTracker = jobTimeoutTracker;
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
      alarmHelper.handleJobTimeoutAlarmTask(this);
      List<Integer> stateList = new ArrayList<Integer>();
      stateList.add(jobTimeoutTracker.getState());
      Integer dataVersion = jobTimeoutTracker.getDataVersion();
      alarmHelper.getJobTimeoutTrackerService()
          .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobTimeoutTracker.getJobId(),
              JobTimeoutTrackerStateEnum.TIMEOUT_HANDLED_SUCCESS.getState(), new Date(),
              jobTimeoutTracker.getLastUpdateStateDetails(),
              jobTimeoutTracker.getHandleTimeoutFailedCount(), stateList, dataVersion);
    } catch (Throwable t) {
      String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
      logger.error(
          "Error occurs when running JobTimeoutAlarmTask. jobTimeoutTracker={} and exceptionDesc={}",
          jobTimeoutTracker, exceptionDesc);
      JobTimeoutHandleFailedEvent jobTimeoutHandleFailedEvent = new JobTimeoutHandleFailedEvent(
          jobTimeoutTracker, JobTimeoutTrackerEventType.JOB_TIMEOUT_HANDLE_FAILED, exceptionDesc);
      alarmHelper.getEventDispatcher().dispatch(jobTimeoutHandleFailedEvent);
    }
  }

}
