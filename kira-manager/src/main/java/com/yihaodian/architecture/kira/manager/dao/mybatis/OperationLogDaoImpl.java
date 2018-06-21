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

import com.yihaodian.architecture.kira.manager.criteria.OperationLogCriteria;
import com.yihaodian.architecture.kira.manager.dao.OperationLogDao;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class OperationLogDaoImpl implements OperationLogDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(OperationLog operationLog) throws DataAccessException {
    sqlSession.insert("OperationLog.insert", operationLog);
  }

  public int update(OperationLog operationLog) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("OperationLog.update", operationLog);

    return actualRowsAffected;
  }

  public int delete(String id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("OperationLog.delete", id);

    return actualRowsAffected;
  }

  public OperationLog select(String id) throws DataAccessException {
    return (OperationLog) sqlSession.selectOne("OperationLog.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<OperationLog> list(OperationLogCriteria operationLogCriteria)
      throws DataAccessException {
    Assert.notNull(operationLogCriteria, "operationLogCriteria must not be null");

    return sqlSession.selectList("OperationLog.list", operationLogCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<OperationLog> listOnPage(OperationLogCriteria operationLogCriteria)
      throws DataAccessException {
    Assert.notNull(operationLogCriteria, "operationLogCriteria must not be null");
    Assert.notNull(operationLogCriteria.getPaging(), "paging must not be null");

    int totalResults = count(operationLogCriteria);
    Paging paging = operationLogCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("OperationLog.list", operationLogCriteria, rowBounds);
  }

  public int count(OperationLogCriteria operationLogCriteria) throws DataAccessException {
    Assert.notNull(operationLogCriteria, "operationLogCriteria must not be null");

    return ((Integer) sqlSession.selectOne("OperationLog.count", operationLogCriteria)).intValue();
  }

  @Override
  public List<OperationLog> getOperationLogDetailDataListOnPage(
      OperationLogCriteria operationLogCriteria)
      throws DataAccessException {
    Assert.notNull(operationLogCriteria, "operationLogCriteria must not be null");
    Assert.notNull(operationLogCriteria.getPaging(), "paging must not be null");

    int totalResults = countJobDetailDataList(operationLogCriteria);
    Paging paging = operationLogCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("OperationLog.getOperationLogDetailDataList", operationLogCriteria, rowBounds);
  }

  @Override
  public int countJobDetailDataList(OperationLogCriteria operationLogCriteria)
      throws DataAccessException {
    Assert.notNull(operationLogCriteria, "operationLogCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("OperationLog.countOperationLogDetailDataList", operationLogCriteria))
        .intValue();
  }

}
