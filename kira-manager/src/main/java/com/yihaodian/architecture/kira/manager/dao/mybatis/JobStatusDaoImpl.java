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

import com.yihaodian.architecture.kira.manager.criteria.JobStatusCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobStatusDao;
import com.yihaodian.architecture.kira.manager.domain.JobStatus;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobStatusDaoImpl implements JobStatusDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobStatus jobStatus) throws DataAccessException {
    sqlSession.insert("JobStatus.insert", jobStatus);
  }

  public int update(JobStatus jobStatus) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobStatus.update", jobStatus);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobStatus.delete", id);

    return actualRowsAffected;
  }

  public JobStatus select(Integer id) throws DataAccessException {
    return (JobStatus) sqlSession.selectOne("JobStatus.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobStatus> list(JobStatusCriteria jobStatusCriteria) throws DataAccessException {
    Assert.notNull(jobStatusCriteria, "jobStatusCriteria must not be null");

    return sqlSession.selectList("JobStatus.list", jobStatusCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobStatus> listOnPage(JobStatusCriteria jobStatusCriteria)
      throws DataAccessException {
    Assert.notNull(jobStatusCriteria, "jobStatusCriteria must not be null");
    Assert.notNull(jobStatusCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobStatusCriteria);
    Paging paging = jobStatusCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobStatus.list", jobStatusCriteria, rowBounds);
  }

  public int count(JobStatusCriteria jobStatusCriteria) throws DataAccessException {
    Assert.notNull(jobStatusCriteria, "jobStatusCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobStatus.count", jobStatusCriteria)).intValue();
  }

}
