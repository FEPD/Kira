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

import com.yihaodian.architecture.kira.manager.criteria.OperationCriteria;
import com.yihaodian.architecture.kira.manager.dao.OperationDao;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class OperationDaoImpl implements OperationDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(Operation operation) throws DataAccessException {
    sqlSession.insert("Operation.insert", operation);
  }

  public int update(Operation operation) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("Operation.update", operation);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("Operation.delete", id);

    return actualRowsAffected;
  }

  public Operation select(Integer id) throws DataAccessException {
    return (Operation) sqlSession.selectOne("Operation.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<Operation> list(OperationCriteria operationCriteria) throws DataAccessException {
    Assert.notNull(operationCriteria, "operationCriteria must not be null");

    return sqlSession.selectList("Operation.list", operationCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<Operation> listOnPage(OperationCriteria operationCriteria)
      throws DataAccessException {
    Assert.notNull(operationCriteria, "operationCriteria must not be null");
    Assert.notNull(operationCriteria.getPaging(), "paging must not be null");

    int totalResults = count(operationCriteria);
    Paging paging = operationCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("Operation.list", operationCriteria, rowBounds);
  }

  public int count(OperationCriteria operationCriteria) throws DataAccessException {
    Assert.notNull(operationCriteria, "operationCriteria must not be null");

    return ((Integer) sqlSession.selectOne("Operation.count", operationCriteria)).intValue();
  }

}
