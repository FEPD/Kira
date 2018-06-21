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

import com.yihaodian.architecture.kira.manager.criteria.JobItemHistoryCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistory;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistoryDetailData;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface JobItemHistoryService {

  void insert(JobItemHistory jobItemHistory);

  int update(JobItemHistory jobItemHistory);

  int delete(String id);

  JobItemHistory select(String id);

  List<JobItemHistory> list(JobItemHistoryCriteria jobItemHistoryCriteria);

  List<JobItemHistory> listOnPage(JobItemHistoryCriteria jobItemHistoryCriteria);

  void createJobItemHistory(String jobItemId, Integer jobStatusId, String resultData,
      Date createTime);

  List<JobItemHistoryDetailData> getJobItemHistoryDetailDataListOnPage(
      JobItemHistoryCriteria jobItemHistoryCriteria);

  void archiveForJobItemHistoryData(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobItemHistoryData(Date startTime, Date endTime);

  Date getDateOfOldestJobItemHistory();

  void handleJobItemHistoryRuntimeData(ExecutorService executorService, Date startTime,
      Date endTime) throws Exception;
}
