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
package com.yihaodian.architecture.kira.manager.action;

import com.yihaodian.architecture.kira.manager.criteria.JobTimeoutTrackerCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class JobTimeoutTrackerAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private JobTimeoutTrackerCriteria criteria = new JobTimeoutTrackerCriteria();

  private transient JobTimeoutTrackerService jobTimeoutTrackerService;

  public JobTimeoutTrackerAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public JobTimeoutTrackerCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(JobTimeoutTrackerCriteria criteria) {
    this.criteria = criteria;
  }

  public JobTimeoutTrackerService getJobTimeoutTrackerService() {
    return jobTimeoutTrackerService;
  }

  public void setJobTimeoutTrackerService(
      JobTimeoutTrackerService jobTimeoutTrackerService) {
    this.jobTimeoutTrackerService = jobTimeoutTrackerService;
  }

  public String list() throws Exception {
    List<JobTimeoutTracker> jobTimeoutTrackerList = jobTimeoutTrackerService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobTimeoutTrackerList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<JobTimeoutTracker> jobTimeoutTrackerList = jobTimeoutTrackerService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobTimeoutTrackerList);
    return null;
  }

}
