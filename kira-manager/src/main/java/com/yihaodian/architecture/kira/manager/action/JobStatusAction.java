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

import com.yihaodian.architecture.kira.manager.criteria.JobStatusCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobStatus;
import com.yihaodian.architecture.kira.manager.service.JobStatusService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class JobStatusAction extends BaseAction {

  private static final long serialVersionUID = -8556726383682267989L;

  private transient JobStatusService jobStatusService;

  private JobStatusCriteria criteria = new JobStatusCriteria();

  public JobStatusAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setJobStatusService(JobStatusService jobStatusService) {
    this.jobStatusService = jobStatusService;
  }

  public JobStatusCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(JobStatusCriteria criteria) {
    this.criteria = criteria;
  }

  public String list() throws Exception {
    criteria.getPaging().setMaxResults(Integer.MAX_VALUE);
    List<JobStatus> jobStatusList = jobStatusService
        .listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobStatusList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<JobStatus> jobStatusList = jobStatusService
        .listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobStatusList);
    return null;
  }
}
