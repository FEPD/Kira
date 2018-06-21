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

import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.criteria.JobStatusCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobDao;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobDaoImpl implements JobDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(Job job) throws DataAccessException {
    sqlSession.insert("Job.insert", job);
  }

  public int update(Job job) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("Job.update", job);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("Job.delete", id);

    return actualRowsAffected;
  }

  public Job select(String id) throws DataAccessException {
    return (Job) sqlSession.selectOne("Job.select", id);
  }

  @Override
  public Job selectForUpdate(String id) throws DataAccessException {
    return (Job) sqlSession.selectOne("Job.selectForUpdate", id);
  }

  @SuppressWarnings("unchecked")
  public List<Job> list(JobCriteria jobCriteria) throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");

    return sqlSession.selectList("Job.list", jobCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<Job> listOnPage(JobCriteria jobCriteria) throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");
    Assert.notNull(jobCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobCriteria);
    Paging paging = jobCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());
    return sqlSession.selectList("Job.list", jobCriteria, rowBounds);
  }

  public int count(JobCriteria jobCriteria) throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");

    return ((Integer) sqlSession.selectOne("Job.count", jobCriteria)).intValue();
  }

  @Override
  public List<JobDetailData> getJobDetailDataListOnPage(JobCriteria jobCriteria)
      throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");
    Assert.notNull(jobCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobDetailDataList(jobCriteria);
    Paging paging = jobCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());
    return sqlSession.selectList("Job.getJobDetailDataList", jobCriteria, rowBounds);
  }

  @Override
  public List<JobDetailData> getJobDetailDataListOnPageUsingLimit(JobCriteria jobCriteria)
      throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");
    Assert.notNull(jobCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobDetailDataList(jobCriteria);
    Paging paging = jobCriteria.getPaging();
    paging.setTotalResults(totalResults);

    return sqlSession.selectList("Job.getJobDetailDataListUsingLimit", jobCriteria);
  }

  @Override
  public int countJobDetailDataList(JobCriteria jobCriteria)
      throws DataAccessException {
    Assert.notNull(jobCriteria, "jobCriteria must not be null");

    return ((Integer) sqlSession.selectOne("Job.countJobDetailDataList", jobCriteria)).intValue();
  }

  @Override
  public int updateJobStatus(String jobId, Integer jobStatusId, String resultData, Date updateTime,
      List<Integer> jobStatusIdList, Integer dataVersion) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id", jobId);
    map.put("jobStatusId", jobStatusId);
    map.put("updateTime", updateTime);
    map.put("resultData", resultData);
    map.put("jobStatusIdList", jobStatusIdList);
    map.put("dataVersion", dataVersion);
    return sqlSession.update("Job.updateJobStatus", map);
  }

  @Override
  public int count(JobStatusCriteria jobCriteria) throws DataAccessException {
    return 0;
  }

  @Override
  public int createArchiveTableAndDataForJobTable(String newTableNameSuffix, Date startTime,
      Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.update("Job.createArchiveTableAndDataForJobTable", map);
  }

  @Override
  public int createArchiveTableForJobTableIfNeeded(String newTableNameSuffix) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    return sqlSession.update("Job.createArchiveTableForJobTableIfNeeded", map);
  }

  @Override
  public Object insertDataToJobArchiveTable(String newTableNameSuffix,
      Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("newTableNameSuffix", newTableNameSuffix);
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.insert("Job.insertDataToJobArchiveTable", map);
  }

  @Override
  public int deleteJobData(Date startTime, Date endTime) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("startTime", startTime);
    map.put("endTime", endTime);
    return sqlSession.delete("Job.deleteJobData", map);
  }

  @Override
  public Date getDateOfOldestJob() {
    Date dateOfOldestJob = null;
    dateOfOldestJob = (Date)
        sqlSession.selectOne("Job.getDateOfOldestJob");
    return dateOfOldestJob;
  }
}
