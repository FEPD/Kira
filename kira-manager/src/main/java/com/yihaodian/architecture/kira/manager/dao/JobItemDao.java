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
package com.yihaodian.architecture.kira.manager.dao;

import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobItemDetailData;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobItemDao {

  void insert(JobItem jobItem) throws DataAccessException;

  int update(JobItem jobItem) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  JobItem select(String id) throws DataAccessException;

  List<JobItem> list(JobItemCriteria jobItemCriteria) throws DataAccessException;

  List<JobItem> listOnPage(JobItemCriteria jobItemCriteria) throws DataAccessException;

  int count(JobItemCriteria jobItemCriteria) throws DataAccessException;

  List<JobItemDetailData> getJobItemDetailDataListOnPage(JobItemCriteria jobItemCriteria)
      throws DataAccessException;

  int countJobItemDetailDataList(JobItemCriteria jobItemCriteria) throws DataAccessException;

  int updateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData, Date updateTime,
      List<Integer> jobStatusIdList, Integer dataVersion);

  int createArchiveTableAndDataForJobItemTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int createArchiveTableForJobItemTableIfNeeded(String newTableNameSuffix);

  Object insertDataToJobItemArchiveTable(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobItemData(Date startTime, Date endTime);

  Date getDateOfOldestJobItem();
}
