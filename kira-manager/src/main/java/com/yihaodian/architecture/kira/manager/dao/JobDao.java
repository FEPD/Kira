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

import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.criteria.JobStatusCriteria;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobDao {

  void insert(Job job) throws DataAccessException;

  int update(Job job) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  Job select(String id) throws DataAccessException;

  Job selectForUpdate(String id);

  List<Job> list(JobCriteria jobCriteria) throws DataAccessException;

  List<Job> listOnPage(JobCriteria jobCriteria) throws DataAccessException;

  int count(JobStatusCriteria jobCriteria) throws DataAccessException;

  List<JobDetailData> getJobDetailDataListOnPage(JobCriteria jobCriteria)
      throws DataAccessException;

  List<JobDetailData> getJobDetailDataListOnPageUsingLimit(JobCriteria jobCriteria)
      throws DataAccessException;

  int countJobDetailDataList(JobCriteria jobCriteria) throws DataAccessException;

  int updateJobStatus(String jobId, Integer jobStatusId, String resultData, Date updateTime,
      List<Integer> jobStatusIdList, Integer dataVersion);

  int createArchiveTableAndDataForJobTable(String newTableNameSuffix, Date startTime, Date endTime);

  int createArchiveTableForJobTableIfNeeded(String newTableNameSuffix);

  Object insertDataToJobArchiveTable(String newTableNameSuffix, Date startTime, Date endTime);

  int deleteJobData(Date startTime, Date endTime);

  Date getDateOfOldestJob();
}
