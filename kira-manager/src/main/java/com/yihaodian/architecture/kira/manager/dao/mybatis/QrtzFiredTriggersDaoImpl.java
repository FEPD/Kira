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

import com.yihaodian.architecture.kira.manager.criteria.QrtzFiredTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzFiredTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzFiredTriggers;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class QrtzFiredTriggersDaoImpl implements QrtzFiredTriggersDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(QrtzFiredTriggers qrtzFiredTriggers) throws DataAccessException {
    sqlSession.insert("QrtzFiredTriggers.insert", qrtzFiredTriggers);
  }

  public int update(QrtzFiredTriggers qrtzFiredTriggers) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("QrtzFiredTriggers.update", qrtzFiredTriggers);

    return actualRowsAffected;
  }

  public int delete(String entryId) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("QrtzFiredTriggers.delete", entryId);

    return actualRowsAffected;
  }

  public QrtzFiredTriggers select(String entryId) throws DataAccessException {
    return (QrtzFiredTriggers) sqlSession.selectOne("QrtzFiredTriggers.select", entryId);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzFiredTriggers> list(QrtzFiredTriggersCriteria qrtzFiredTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzFiredTriggersCriteria, "qrtzFiredTriggersCriteria must not be null");

    return sqlSession.selectList("QrtzFiredTriggers.list", qrtzFiredTriggersCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzFiredTriggers> listOnPage(QrtzFiredTriggersCriteria qrtzFiredTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzFiredTriggersCriteria, "qrtzFiredTriggersCriteria must not be null");
    Assert.notNull(qrtzFiredTriggersCriteria.getPaging(), "paging must not be null");

    int totalResults = count(qrtzFiredTriggersCriteria);
    Paging paging = qrtzFiredTriggersCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("QrtzFiredTriggers.list", qrtzFiredTriggersCriteria, rowBounds);
  }

  public int count(QrtzFiredTriggersCriteria qrtzFiredTriggersCriteria) throws DataAccessException {
    Assert.notNull(qrtzFiredTriggersCriteria, "qrtzFiredTriggersCriteria must not be null");

    return ((Integer) sqlSession.selectOne("QrtzFiredTriggers.count", qrtzFiredTriggersCriteria))
        .intValue();
  }

}
