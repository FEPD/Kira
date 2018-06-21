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

import com.yihaodian.architecture.kira.manager.criteria.QrtzTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzTriggers;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class QrtzTriggersDaoImpl implements QrtzTriggersDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(QrtzTriggers qrtzTriggers) throws DataAccessException {
    sqlSession.insert("QrtzTriggers.insert", qrtzTriggers);
  }

  public int update(QrtzTriggers qrtzTriggers) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("QrtzTriggers.update", qrtzTriggers);

    return actualRowsAffected;
  }

  public int delete(String triggerGroup, String triggerName) throws DataAccessException {
    int actualRowsAffected = 1;

    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("triggerGroup", triggerGroup);
    param.put("triggerName", triggerName);

    sqlSession.delete("QrtzTriggers.delete", param);

    return actualRowsAffected;
  }

  public QrtzTriggers select(String triggerGroup, String triggerName) throws DataAccessException {
    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("triggerGroup", triggerGroup);
    param.put("triggerName", triggerName);

    return (QrtzTriggers) sqlSession.selectOne("QrtzTriggers.select", param);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzTriggers> list(QrtzTriggersCriteria qrtzTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzTriggersCriteria, "qrtzTriggersCriteria must not be null");

    return sqlSession.selectList("QrtzTriggers.list", qrtzTriggersCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzTriggers> listOnPage(QrtzTriggersCriteria qrtzTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzTriggersCriteria, "qrtzTriggersCriteria must not be null");
    Assert.notNull(qrtzTriggersCriteria.getPaging(), "paging must not be null");

    int totalResults = count(qrtzTriggersCriteria);
    Paging paging = qrtzTriggersCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("QrtzTriggers.list", qrtzTriggersCriteria, rowBounds);
  }

  public int count(QrtzTriggersCriteria qrtzTriggersCriteria) throws DataAccessException {
    Assert.notNull(qrtzTriggersCriteria, "qrtzTriggersCriteria must not be null");

    return ((Integer) sqlSession.selectOne("QrtzTriggers.count", qrtzTriggersCriteria)).intValue();
  }

  @Override
  public List<QrtzTriggers> listCanBeScheduled(QrtzTriggersCriteria qrtzTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzTriggersCriteria, "qrtzTriggersCriteria must not be null");

    return sqlSession.selectList("QrtzTriggers.listCanBeScheduled", qrtzTriggersCriteria);
  }

}
