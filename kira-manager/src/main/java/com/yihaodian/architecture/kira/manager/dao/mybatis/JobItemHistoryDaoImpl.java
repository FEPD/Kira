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

import com.yihaodian.architecture.kira.manager.criteria.JobItemHistoryCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobItemHistoryDao;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistory;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistoryDetailData;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobItemHistoryDaoImpl implements JobItemHistoryDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobItemHistory jobItemHistory) throws DataAccessException {
    sqlSession.insert("JobItemHistory.insert", jobItemHistory);
  }

  public int update(JobItemHistory jobItemHistory) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobItemHistory.update", jobItemHistory);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobItemHistory.delete", id);

    return actualRowsAffected;
  }

  public JobItemHistory select(String id) throws DataAccessException {
    return (JobItemHistory) sqlSession.selectOne("JobItemHistory.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobItemHistory> list(JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException {
    Assert.notNull(jobItemHistoryCriteria, "jobItemHistoryCriteria must not be null");

    return sqlSession.selectOne("JobItemHistory.list", jobItemHistoryCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobItemHistory> listOnPage(JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException {
    Assert.notNull(jobItemHistoryCriteria, "jobItemHistoryCriteria must not be null");
    Assert.notNull(jobItemHistoryCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobItemHistoryCriteria);
    Paging paging = jobItemHistoryCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobItemHistory.list", jobItemHistoryCriteria, rowBounds);
  }

  public int count(JobItemHistoryCriteria jobItemHistoryCriteria) throws DataAccessException {
    Assert.notNull(jobItemHistoryCriteria, "jobItemHistoryCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobItemHistory.count", jobItemHistoryCriteria))
        .intValue();
  }

  @Override
  public List<JobItemHistoryDetailData> getJobItemHistoryDetailDataListOnPage(
      JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException {
    Assert.notNull(jobItemHistoryCriteria, "jobItemHistoryCriteria must not be null");
    Assert.notNull(jobItemHistoryCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobItemHistoryDetailDataList(jobItemHistoryCriteria);
    Paging paging = jobItemHistoryCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("JobItemHistory.getJobItemHistoryDetailDataList", jobItemHistoryCriteria,
            rowBounds);
  }

  @Override
  public int countJobItemHistoryDetailDataList(
      JobItemHistoryCriteria jobItemHistoryCriteria)
      throws DataAccessException {
    Assert.notNull(jobItemHistoryCriteria, "jobItemHistoryCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("JobItemHistory.countJobItemHistoryDetailDataList", jobItemHistoryCriteria))
        .intValue();
  }

  @Override
  public int createArchiveTableAndDataForJobItemHistoryTable(
      String newTableNameSuffix, Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.update("JobItemHistory.createArchiveTableAndDataForJobItemHistoryTable", map);
  }

  @Override
  public int createArchiveTableForJobItemHistoryTableIfNeeded(
      String newTableNameSuffix) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    return sqlSession
        .update("JobItemHistory.createArchiveTableForJobItemHistoryTableIfNeeded", map);
  }

  @Override
  public Object insertDataToJobItemHistoryArchiveTable(
      String newTableNameSuffix, Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.insert("JobItemHistory.insertDataToJobItemHistoryArchiveTable", map);
  }

  @Override
  public int deleteJobItemHistoryData(Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.delete("JobItemHistory.deleteJobItemHistoryData", map);
  }

  @Override
  public Date getDateOfOldestJobItemHistory() {
    Date dateOfOldestJobItemHistory = null;
    dateOfOldestJobItemHistory = (Date) sqlSession
        .selectOne("JobItemHistory.getDateOfOldestJobItemHistory");
    return dateOfOldestJobItemHistory;
  }
}
