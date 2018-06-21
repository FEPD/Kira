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

import com.yihaodian.architecture.kira.manager.criteria.JobRunStatisticsCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobRunStatistics;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface JobRunStatisticsDao {

  void insert(JobRunStatistics jobRunStatistics) throws DataAccessException;

  int update(JobRunStatistics jobRunStatistics) throws DataAccessException;

  int delete(Long id) throws DataAccessException;

  JobRunStatistics select(Long id) throws DataAccessException;

  List<JobRunStatistics> list(JobRunStatisticsCriteria jobRunStatisticsCriteria)
      throws DataAccessException;

  List<JobRunStatistics> listOnPage(JobRunStatisticsCriteria jobRunStatisticsCriteria)
      throws DataAccessException;

  List<JobRunStatistics> listOnPageUsingLimit(JobRunStatisticsCriteria jobRunStatisticsCriteria)
      throws DataAccessException;

  int count(JobRunStatisticsCriteria jobRunStatisticsCriteria) throws DataAccessException;

  void doJobRunStatistics(Date beginTime, Date endTime, String appId, String triggerId,
      Integer maxSampleCount, Date createTime) throws DataAccessException;

}
