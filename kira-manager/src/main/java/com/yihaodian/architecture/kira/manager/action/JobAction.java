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

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import com.yihaodian.architecture.kira.manager.dto.ManuallyReRunJobResult;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class JobAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private transient JobService jobService;
  private transient KiraClientMetadataService kiraClientMetadataService;

  private JobCriteria criteria = new JobCriteria();

  public JobAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public KiraClientMetadataService getKiraClientMetadataService() {
    return kiraClientMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public JobCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(JobCriteria criteria) {
    this.criteria = criteria;
  }

  public String list() throws Exception {
    List<Job> jobList = jobService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<Job> jobList = jobService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobList);
    return null;
  }

  public String getJobDetailDataListOnPage() throws Exception {
    addInvisiblePoolListToCriteria();
    List<JobDetailData> jobDetailDataList = jobService.getJobDetailDataListOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobDetailDataList);
    return null;
  }

  private void addInvisiblePoolListToCriteria() {
    UserContextData userContextData = SecurityUtils.getUserContextDataViaStruts2();
    List<String> invisiblePoolListForUser = kiraClientMetadataService
        .getInvisiblePoolListForUser(userContextData);
    criteria.setPoolIdListToExclude(invisiblePoolListForUser);
  }

  public String getJobDetailDataListForJobsBeingProcessedOnPage() throws Exception {
    List<JobDetailData> jobDetailDataList = jobService
        .getJobDetailDataListForJobsBeingProcessedOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, jobDetailDataList);
    return null;
  }

  public String manuallyReRunJob() throws Exception {
    String jobId = criteria.getId();
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    ManuallyReRunJobResult manuallyReRunJobResult = jobService.manuallyReRunJob(jobId, userName);
    Utils.sendHttpResponseForStruts2(criteria, manuallyReRunJobResult);
    return null;
  }

  public String cancelJob() throws Exception {
    String jobId = criteria.getId();
    String cancelJobJsonMapString = criteria.getCancelJobJsonMapString();
    HandleResult handleResult = jobService.cancelJob(jobId, cancelJobJsonMapString);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String archiveForJobData() throws Exception {
    String newTableNameSuffixForArchive = criteria.getNewTableNameSuffixForArchive();
    if (StringUtils.isBlank(newTableNameSuffixForArchive)) {
      throw new RuntimeException("newTableNameSuffixForArchive should not be blank.");
    }
    String startTimeForArchive = criteria.getStartTimeForArchive();
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeForArchive)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeForArchive, true);
    }

    String endTimeForArchive = criteria.getEndTimeForArchive();
    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeForArchive)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeForArchive, true);
    }

    jobService.archiveForJobData(newTableNameSuffixForArchive, startTime, endTime);
    Utils.sendHttpResponseForStruts2(criteria, null);
    return null;
  }

}
