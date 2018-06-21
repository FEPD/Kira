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
package com.yihaodian.architecture.kira.manager.service;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.ManuallyReRunJobResult;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface JobService {

  void insert(Job job);

  int update(Job job);

  int delete(String id);

  Job select(String id);

  Job selectForUpdate(String id);

  List<Job> list(JobCriteria jobCriteria);

  List<Job> listOnPage(JobCriteria jobCriteria);

  List<JobDetailData> getJobDetailDataListOnPage(JobCriteria jobCriteria);

  List<JobDetailData> getJobDetailDataListForJobsBeingProcessedOnPage(JobCriteria jobCriteria);

  Job createJob(String poolId, String triggerId, String version, Boolean manuallyScheduled,
      String createdBy, Date triggerTime, Date runAtTime, boolean createJobHistory);

  void updateJobStatus(String jobId, Integer jobStatusId, String resultData, Date updateTime,
      List<Integer> jobStatusIdList);

  void updateJobStatusAndCreateHistoryIfNeeded(String jobId, Integer jobStatusId, String resultData,
      Date updateTime, List<Integer> jobStatusIdList, boolean createJobHistory);

  void updateStatusOfJobByJobItems(String jobId);

  HandleResult cancelJob(String jobId, String cancelJobJsonMapString);

  Map<String, String> getCentralScheduleServiceUrlArgumentsAsJsonArrayStringMap(Job job,
      List<String> centralScheduleServiceUrlList);

  void runJob(TriggerMetadata triggerMetadata, Job job, Boolean manuallyScheduled);

  ManuallyReRunJobResult manuallyReRunJob(String jobId, String userName);

  void archiveForJobData(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobData(Date startTime, Date endTime);

  Date getDateOfOldestJob();

  void handleJobRuntimeData(ExecutorService executorService, Date startTime, Date endTime)
      throws Exception;

  boolean isJobCompleted(String jobId, Integer jobStatusId);
}
