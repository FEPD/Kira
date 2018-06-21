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
package com.yihaodian.architecture.kira.manager.event.jobtimeouttracker;

import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import java.util.Date;

public class JobTimeoutHandleFailedEvent extends JobTimeoutTrackerEvent {

  private String jobTimeoutHandleFailedDetails;

  public JobTimeoutHandleFailedEvent(JobTimeoutTracker jobTimeoutTracker,
      JobTimeoutTrackerEventType eventType, String jobTimeoutHandleFailedDetails) {
    super(jobTimeoutTracker, eventType);
    this.jobTimeoutHandleFailedDetails = jobTimeoutHandleFailedDetails;
  }

  public JobTimeoutHandleFailedEvent(JobTimeoutTracker jobTimeoutTracker,
      JobTimeoutTrackerEventType eventType, Date eventTime, String jobTimeoutHandleFailedDetails) {
    super(jobTimeoutTracker, eventType, eventTime);
    this.jobTimeoutHandleFailedDetails = jobTimeoutHandleFailedDetails;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getJobTimeoutHandleFailedDetails() {
    return jobTimeoutHandleFailedDetails;
  }

  public void setJobTimeoutHandleFailedDetails(
      String jobTimeoutHandleFailedDetails) {
    this.jobTimeoutHandleFailedDetails = jobTimeoutHandleFailedDetails;
  }

  @Override
  public String toString() {
    return "JobTimeoutHandleFailedEvent [jobTimeoutHandleFailedDetails="
        + jobTimeoutHandleFailedDetails + ", super.toString()="
        + super.toString() + "]";
  }

}
