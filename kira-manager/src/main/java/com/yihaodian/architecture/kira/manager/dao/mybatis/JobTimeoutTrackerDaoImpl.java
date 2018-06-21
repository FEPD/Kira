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
package com.yihaodian.architecture.kira.manager.dao.mybatis;

import com.yihaodian.architecture.kira.manager.criteria.JobTimeoutTrackerCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobTimeoutTrackerDao;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobTimeoutTrackerDaoImpl implements JobTimeoutTrackerDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobTimeoutTracker jobTimeoutTracker) throws DataAccessException {
    sqlSession.insert("JobTimeoutTracker.insert", jobTimeoutTracker);
  }

  public int update(JobTimeoutTracker jobTimeoutTracker) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobTimeoutTracker.update", jobTimeoutTracker);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobTimeoutTracker.delete", id);

    return actualRowsAffected;
  }

  public JobTimeoutTracker select(String id) throws DataAccessException {
    return (JobTimeoutTracker) sqlSession.selectOne("JobTimeoutTracker.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobTimeoutTracker> list(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException {
    Assert.notNull(jobTimeoutTrackerCriteria, "jobTimeoutTrackerCriteria must not be null");

    return sqlSession.selectList("JobTimeoutTracker.list", jobTimeoutTrackerCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobTimeoutTracker> listOnPage(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException {
    Assert.notNull(jobTimeoutTrackerCriteria, "jobTimeoutTrackerCriteria must not be null");
    Assert.notNull(jobTimeoutTrackerCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobTimeoutTrackerCriteria);
    Paging paging = jobTimeoutTrackerCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobTimeoutTracker.list", jobTimeoutTrackerCriteria, rowBounds);
  }

  @SuppressWarnings("unchecked")
  public List<JobTimeoutTracker> listOnPageUsingLimit(
      JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria)
      throws DataAccessException {
    Assert.notNull(jobTimeoutTrackerCriteria, "jobTimeoutTrackerCriteria must not be null");
    Assert.notNull(jobTimeoutTrackerCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobTimeoutTrackerCriteria);
    Paging paging = jobTimeoutTrackerCriteria.getPaging();
    paging.setTotalResults(totalResults);

    return sqlSession.selectList("JobTimeoutTracker.listUsingLimit", jobTimeoutTrackerCriteria);
  }

  public int count(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria) throws DataAccessException {
    Assert.notNull(jobTimeoutTrackerCriteria, "jobTimeoutTrackerCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobTimeoutTracker.count", jobTimeoutTrackerCriteria))
        .intValue();
  }

  @Override
  public int createArchiveTableForJobTimeoutTrackerTableIfNeeded(
      String newTableNameSuffix) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    return sqlSession
        .update("JobTimeoutTracker.createArchiveTableForJobTimeoutTrackerTableIfNeeded", map);
  }

  @Override
  public Object insertDataToJobTimeoutTrackerArchiveTable(
      String newTableNameSuffix, Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.insert("JobTimeoutTracker.insertDataToJobTimeoutTrackerArchiveTable", map);
  }

  @Override
  public int deleteJobTimeoutTrackerData(Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.delete("JobTimeoutTracker.deleteJobTimeoutTrackerData", map);
  }

  @Override
  public Date getDateOfOldestJobTimeoutTracker() {
    Date dateOfOldestJobTimeoutTracker = null;
    dateOfOldestJobTimeoutTracker = (Date) sqlSession
        .selectOne("JobTimeoutTracker.getDateOfOldestJobTimeoutTracker");
    return dateOfOldestJobTimeoutTracker;
  }

  @Override
  public int updateJobTimeoutTrackerState(String id, String jobId, Integer state,
      Date lastUpdateStateTime, String lastUpdateStateDetails, Integer handleTimeoutFailedCount,
      List<Integer> stateList, Integer dataVersion) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id", id);
    map.put("jobId", jobId);
    map.put("state", state);
    map.put("lastUpdateStateTime", lastUpdateStateTime);
    map.put("lastUpdateStateDetails", lastUpdateStateDetails);
    map.put("lastUpdateStateDetails", lastUpdateStateDetails);
    map.put("handleTimeoutFailedCount", handleTimeoutFailedCount);
    map.put("stateList", stateList);
    map.put("dataVersion", dataVersion);
    return sqlSession.update("JobTimeoutTracker.updateJobTimeoutTrackerState", map);
  }

}
