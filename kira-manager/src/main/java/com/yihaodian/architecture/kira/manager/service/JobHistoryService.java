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

import com.yihaodian.architecture.kira.manager.criteria.JobHistoryCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobHistory;
import com.yihaodian.architecture.kira.manager.domain.JobHistoryDetailData;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface JobHistoryService {

  void insert(JobHistory jobHistory);

  int update(JobHistory jobHistory);

  int delete(String id);

  JobHistory select(String id);

  List<JobHistory> list(JobHistoryCriteria jobHistoryCriteria);

  List<JobHistory> listOnPage(JobHistoryCriteria jobHistoryCriteria);

  void createJobHistory(String jobId, Integer jobStatusId, String resultData, Date createTime);

  List<JobHistoryDetailData> getJobHistoryDetailDataListOnPage(
      JobHistoryCriteria jobHistoryCriteria);

  void archiveForJobHistoryData(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobHistoryData(Date startTime, Date endTime);

  Date getDateOfOldestJobHistory();

  void handleJobHistoryRuntimeData(ExecutorService executorService, Date startTime, Date endTime)
      throws Exception;
}
