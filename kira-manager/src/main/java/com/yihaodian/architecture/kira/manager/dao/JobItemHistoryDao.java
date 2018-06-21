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

import com.yihaodian.architecture.kira.manager.criteria.JobItemHistoryCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistory;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistoryDetailData;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobItemHistoryDao {

  void insert(JobItemHistory jobItemHistory) throws DataAccessException;

  int update(JobItemHistory jobItemHistory) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  JobItemHistory select(String id) throws DataAccessException;

  List<JobItemHistory> list(JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException;

  List<JobItemHistory> listOnPage(JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException;

  int count(JobItemHistoryCriteria jobItemHistoryCriteria) throws DataAccessException;

  List<JobItemHistoryDetailData> getJobItemHistoryDetailDataListOnPage(
      JobItemHistoryCriteria jobItemHistoryCriteria) throws DataAccessException;

  int countJobItemHistoryDetailDataList(JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException;

  int createArchiveTableAndDataForJobItemHistoryTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int createArchiveTableForJobItemHistoryTableIfNeeded(String newTableNameSuffix);

  Object insertDataToJobItemHistoryArchiveTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int deleteJobItemHistoryData(Date startTime, Date endTime);

  Date getDateOfOldestJobItemHistory();
}
