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

import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobItemDao;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobItemDetailData;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobItemDaoImpl implements JobItemDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobItem jobItem) throws DataAccessException {
    sqlSession.insert("JobItem.insert", jobItem);
  }

  public int update(JobItem jobItem) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobItem.update", jobItem);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobItem.delete", id);

    return actualRowsAffected;
  }

  public JobItem select(String id) throws DataAccessException {
    return (JobItem) sqlSession.selectOne("JobItem.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobItem> list(JobItemCriteria jobItemCriteria) throws DataAccessException {
    Assert.notNull(jobItemCriteria, "jobItemCriteria must not be null");

    return sqlSession.selectList("JobItem.list", jobItemCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobItem> listOnPage(JobItemCriteria jobItemCriteria) throws DataAccessException {
    Assert.notNull(jobItemCriteria, "jobItemCriteria must not be null");
    Assert.notNull(jobItemCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobItemCriteria);
    Paging paging = jobItemCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobItem.list", jobItemCriteria, rowBounds);
  }

  public int count(JobItemCriteria jobItemCriteria) throws DataAccessException {
    Assert.notNull(jobItemCriteria, "jobItemCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobItem.count", jobItemCriteria)).intValue();
  }

  @Override
  public int updateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData,
      Date updateTime, List<Integer> jobStatusIdList, Integer dataVersion) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id", jobItemId);
    map.put("jobStatusId", jobStatusId);
    map.put("updateTime", updateTime);
    map.put("resultData", resultData);
    map.put("jobStatusIdList", jobStatusIdList);
    map.put("dataVersion", dataVersion);
    return sqlSession.update("JobItem.updateJobItemStatus", map);
  }

  @Override
  public List<JobItemDetailData> getJobItemDetailDataListOnPage(
      JobItemCriteria jobItemCriteria) throws DataAccessException {
    Assert.notNull(jobItemCriteria, "jobItemCriteria must not be null");
    Assert.notNull(jobItemCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobItemDetailDataList(jobItemCriteria);
    Paging paging = jobItemCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobItem.getJobItemDetailDataList", jobItemCriteria, rowBounds);
  }

  @Override
  public int countJobItemDetailDataList(JobItemCriteria jobItemCriteria)
      throws DataAccessException {
    Assert.notNull(jobItemCriteria, "jobItemCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobItem.countJobItemDetailDataList", jobItemCriteria))
        .intValue();
  }

  @Override
  public int createArchiveTableAndDataForJobItemTable(
      String newTableNameSuffix, Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.update("JobItem.createArchiveTableAndDataForJobItemTable", map);
  }

  @Override
  public int createArchiveTableForJobItemTableIfNeeded(
      String newTableNameSuffix) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    return sqlSession.update("JobItem.createArchiveTableForJobItemTableIfNeeded", map);
  }

  @Override
  public Object insertDataToJobItemArchiveTable(String newTableNameSuffix,
      Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.insert("JobItem.insertDataToJobItemArchiveTable", map);
  }

  @Override
  public int deleteJobItemData(Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.delete("JobItem.deleteJobItemData", map);
  }

  @Override
  public Date getDateOfOldestJobItem() {
    Date dateOfOldestJobItem = null;
    dateOfOldestJobItem = (Date) sqlSession.selectOne("JobItem.getDateOfOldestJobItem");
    return dateOfOldestJobItem;
  }
}
