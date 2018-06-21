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

import com.yihaodian.architecture.kira.manager.criteria.JobHistoryCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobHistory;
import com.yihaodian.architecture.kira.manager.domain.JobHistoryDetailData;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobHistoryDao {

  void insert(JobHistory jobHistory) throws DataAccessException;

  int update(JobHistory jobHistory) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  JobHistory select(String id) throws DataAccessException;

  List<JobHistory> list(JobHistoryCriteria jobHistoryCriteria) throws DataAccessException;

  List<JobHistory> listOnPage(JobHistoryCriteria jobHistoryCriteria) throws DataAccessException;

  int count(JobHistoryCriteria jobHistoryCriteria) throws DataAccessException;

  List<JobHistoryDetailData> getJobHistoryDetailDataListOnPage(
      JobHistoryCriteria jobHistoryCriteria) throws DataAccessException;

  int countJobHistoryDetailDataList(JobHistoryCriteria jobHistoryCriteria)
      throws DataAccessException;

  int createArchiveTableAndDataForJobHistoryTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int createArchiveTableForJobHistoryTableIfNeeded(String newTableNameSuffix);

  Object insertDataToJobHistoryArchiveTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int deleteJobHistoryData(Date startTime, Date endTime);

  Date getDateOfOldestJobHistory();
}
