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

import com.yihaodian.architecture.kira.common.event.AbstractEvent;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import java.util.Date;

public class JobTimeoutTrackerEvent extends AbstractEvent<JobTimeoutTrackerEventType> {

  protected JobTimeoutTracker jobTimeoutTracker;

  public JobTimeoutTrackerEvent(JobTimeoutTracker jobTimeoutTracker,
      JobTimeoutTrackerEventType eventType) {
    super(eventType);
    this.jobTimeoutTracker = jobTimeoutTracker;
  }

  public JobTimeoutTrackerEvent(JobTimeoutTracker jobTimeoutTracker,
      JobTimeoutTrackerEventType eventType,
      Date eventTime) {
    super(eventType, eventTime);
    this.jobTimeoutTracker = jobTimeoutTracker;
  }

  public JobTimeoutTracker getJobTimeoutTracker() {
    return jobTimeoutTracker;
  }

  @Override
  public String toString() {
    return super.toString() + " and jobTimeoutTracker=" + jobTimeoutTracker;
  }
}
