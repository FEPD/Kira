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

import com.yihaodian.architecture.kira.manager.criteria.JobRunStatisticsCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobRunStatisticsDao;
import com.yihaodian.architecture.kira.manager.domain.JobRunStatistics;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class JobRunStatisticsDaoImpl implements JobRunStatisticsDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(JobRunStatistics jobRunStatistics) throws DataAccessException {
    sqlSession.insert("JobRunStatistics.insert", jobRunStatistics);
  }

  public int update(JobRunStatistics jobRunStatistics) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("JobRunStatistics.update", jobRunStatistics);

    return actualRowsAffected;
  }

  public int delete(Long id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("JobRunStatistics.delete", id);

    return actualRowsAffected;
  }

  public JobRunStatistics select(Long id) throws DataAccessException {
    return (JobRunStatistics) sqlSession.selectOne("JobRunStatistics.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobRunStatistics> list(JobRunStatisticsCriteria jobRunStatisticsCriteria)
      throws DataAccessException {
    Assert.notNull(jobRunStatisticsCriteria, "jobRunStatisticsCriteria must not be null");

    return sqlSession.selectList("JobRunStatistics.list", jobRunStatisticsCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<JobRunStatistics> listOnPage(JobRunStatisticsCriteria jobRunStatisticsCriteria)
      throws DataAccessException {
    Assert.notNull(jobRunStatisticsCriteria, "jobRunStatisticsCriteria must not be null");
    Assert.notNull(jobRunStatisticsCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobRunStatisticsCriteria);
    Paging paging = jobRunStatisticsCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("JobRunStatistics.list", jobRunStatisticsCriteria, rowBounds);
  }

  @Override
  public List<JobRunStatistics> listOnPageUsingLimit(
      JobRunStatisticsCriteria jobRunStatisticsCriteria) throws DataAccessException {
    Assert.notNull(jobRunStatisticsCriteria, "jobRunStatisticsCriteria must not be null");
    Assert.notNull(jobRunStatisticsCriteria.getPaging(), "paging must not be null");

    int totalResults = count(jobRunStatisticsCriteria);
    Paging paging = jobRunStatisticsCriteria.getPaging();
    paging.setTotalResults(totalResults);

    return sqlSession.selectList("JobRunStatistics.listUsingLimit", jobRunStatisticsCriteria);
  }

  public int count(JobRunStatisticsCriteria jobRunStatisticsCriteria) throws DataAccessException {
    Assert.notNull(jobRunStatisticsCriteria, "jobRunStatisticsCriteria must not be null");

    return ((Integer) sqlSession.selectOne("JobRunStatistics.count", jobRunStatisticsCriteria))
        .intValue();
  }

  @Override
  public void doJobRunStatistics(Date beginTime, Date endTime, String appId, String triggerId,
      Integer maxSampleCount, Date createTime) throws DataAccessException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("beginTime", beginTime);
    map.put("endTime", endTime);
    map.put("appId", appId);
    map.put("triggerId", triggerId);
    map.put("maxSampleCount", maxSampleCount);
    map.put("createTime", createTime);
    sqlSession.insert("JobRunStatistics.doJobRunStatistics", map);
  }

}
