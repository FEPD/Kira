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

import com.yihaodian.architecture.kira.manager.criteria.JobHistoryCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobHistoryDao;
import com.yihaodian.architecture.kira.manager.domain.JobHistory;
import com.yihaodian.architecture.kira.manager.domain.JobHistoryDetailData;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobHistoryDaoImpl implements JobHistoryDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobHistory jobHistory) throws DataAccessException {
    sqlSession.insert("JobHistory.insert", jobHistory);
  }

  public int update(JobHistory jobHistory) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobHistory.update", jobHistory);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobHistory.delete", id);

    return actualRowsAffected;
  }

  public JobHistory select(String id) throws DataAccessException {
    return (JobHistory) sqlSession.selectOne("JobHistory.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobHistory> list(JobHistoryCriteria jobHistoryCriteria) throws DataAccessException {
    Assert.notNull(jobHistoryCriteria, "jobHistoryCriteria must not be null");

    return sqlSession.selectList("JobHistory.list", jobHistoryCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobHistory> listOnPage(JobHistoryCriteria jobHistoryCriteria)
      throws DataAccessException {
    Assert.notNull(jobHistoryCriteria, "jobHistoryCriteria must not be null");
    Assert.notNull(jobHistoryCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobHistoryCriteria);
    Paging paging = jobHistoryCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobHistory.list", jobHistoryCriteria, rowBounds);
  }

  public int count(JobHistoryCriteria jobHistoryCriteria) throws DataAccessException {
    Assert.notNull(jobHistoryCriteria, "jobHistoryCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobHistory.count", jobHistoryCriteria)).intValue();
  }

  @Override
  public List<JobHistoryDetailData> getJobHistoryDetailDataListOnPage(
      JobHistoryCriteria jobHistoryCriteria) throws DataAccessException {
    Assert.notNull(jobHistoryCriteria, "jobHistoryCriteria must not be null");
    Assert.notNull(jobHistoryCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobHistoryDetailDataList(jobHistoryCriteria);
    Paging paging = jobHistoryCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("JobHistory.getJobHistoryDetailDataList", jobHistoryCriteria, rowBounds);
  }

  @Override
  public int countJobHistoryDetailDataList(
      JobHistoryCriteria jobHistoryCriteria) throws DataAccessException {
    Assert.notNull(jobHistoryCriteria, "jobHistoryCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("JobHistory.countJobHistoryDetailDataList", jobHistoryCriteria)).intValue();
  }

  @Override
  public int createArchiveTableAndDataForJobHistoryTable(
      String newTableNameSuffix, Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.update("JobHistory.createArchiveTableAndDataForJobHistoryTable", map);
  }

  @Override
  public int createArchiveTableForJobHistoryTableIfNeeded(
      String newTableNameSuffix) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    return sqlSession.update("JobHistory.createArchiveTableForJobHistoryTableIfNeeded", map);
  }

  @Override
  public Object insertDataToJobHistoryArchiveTable(String newTableNameSuffix,
      Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.insert("JobHistory.insertDataToJobHistoryArchiveTable", map);
  }

  @Override
  public int deleteJobHistoryData(Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.delete("JobHistory.deleteJobHistoryData", map);
  }

  @Override
  public Date getDateOfOldestJobHistory() {
    Date dateOfOldestJobHistory = null;
    dateOfOldestJobHistory = (Date) sqlSession.selectOne("JobHistory.getDateOfOldestJobHistory");
    return dateOfOldestJobHistory;
  }
}
