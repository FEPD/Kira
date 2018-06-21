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
import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobItemDetailData;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface JobItemService {

  void insert(JobItem jobItem);

  int update(JobItem jobItem);

  int delete(String id);

  JobItem select(String id);

  List<JobItem> list(JobItemCriteria jobItemCriteria);

  List<JobItem> listOnPage(JobItemCriteria jobItemCriteria);

  List<JobItemDetailData> getJobItemDetailDataListOnPage(JobItemCriteria jobItemCriteria);

  void updateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData,
      List<Integer> jobStatusIdList, boolean createJobItemHistory, boolean updateJobIfNeeded);

  JobItem createJobItem(String jobId, String serviceUrl, String argumentsAsJsonArrayString,
      boolean createJobItemHistory);

  List<JobItem> createJobItemList(String jobId, List<String> serviceUrlList,
      Map<String, String> centralScheduleServiceUrlArgumentsAsJsonArrayStringMap,
      boolean createJobItemHistory);

  HandleResult cancelJobItem(String jobItemId);

  void archiveForJobItemData(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobItemData(Date startTime, Date endTime);

  Date getDateOfOldestJobItem();

  void handleJobItemRuntimeData(ExecutorService executorService, Date startTime, Date endTime)
      throws Exception;
}
