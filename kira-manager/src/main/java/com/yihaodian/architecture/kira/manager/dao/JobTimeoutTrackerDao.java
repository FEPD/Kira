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

import com.yihaodian.architecture.kira.manager.criteria.JobTimeoutTrackerCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobTimeoutTrackerDao {

  void insert(JobTimeoutTracker jobTimeoutTracker) throws DataAccessException;

  int update(JobTimeoutTracker jobTimeoutTracker) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  JobTimeoutTracker select(String id) throws DataAccessException;

  List<JobTimeoutTracker> list(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException;

  List<JobTimeoutTracker> listOnPage(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException;

  List<JobTimeoutTracker> listOnPageUsingLimit(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException;

  int count(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria) throws DataAccessException;

  int createArchiveTableForJobTimeoutTrackerTableIfNeeded(String newTableNameSuffix);

  Object insertDataToJobTimeoutTrackerArchiveTable(String newTableNameSuffix, Date startTime,
      Date endTime);

  int deleteJobTimeoutTrackerData(Date startTime, Date endTime);

  Date getDateOfOldestJobTimeoutTracker();

  int updateJobTimeoutTrackerState(String id, String jobId, Integer state, Date lastUpdateStateTime,
      String lastUpdateStateDetails, Integer handleTimeoutFailedCount, List<Integer> stateList,
      Integer dataVersion);

}
