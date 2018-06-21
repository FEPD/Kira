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

import com.yihaodian.architecture.kira.manager.criteria.QrtzSimpleTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzSimpleTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzSimpleTriggers;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class QrtzSimpleTriggersDaoImpl implements QrtzSimpleTriggersDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(QrtzSimpleTriggers qrtzSimpleTriggers) throws DataAccessException {
    sqlSession.insert("QrtzSimpleTriggers.insert", qrtzSimpleTriggers);
  }

  public int update(QrtzSimpleTriggers qrtzSimpleTriggers) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("QrtzSimpleTriggers.update", qrtzSimpleTriggers);

    return actualRowsAffected;
  }

  public int delete(String triggerGroup, String triggerName) throws DataAccessException {
    int actualRowsAffected = 1;

    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("triggerGroup", triggerGroup);
    param.put("triggerName", triggerName);

    sqlSession.delete("QrtzSimpleTriggers.delete", param);

    return actualRowsAffected;
  }

  public QrtzSimpleTriggers select(String triggerGroup, String triggerName)
      throws DataAccessException {
    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("triggerGroup", triggerGroup);
    param.put("triggerName", triggerName);

    return (QrtzSimpleTriggers) sqlSession.selectOne("QrtzSimpleTriggers.select", param);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzSimpleTriggers> list(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzSimpleTriggersCriteria, "qrtzSimpleTriggersCriteria must not be null");

    return sqlSession.selectList("QrtzSimpleTriggers.list", qrtzSimpleTriggersCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<QrtzSimpleTriggers> listOnPage(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzSimpleTriggersCriteria, "qrtzSimpleTriggersCriteria must not be null");
    Assert.notNull(qrtzSimpleTriggersCriteria.getPaging(), "paging must not be null");

    int totalResults = count(qrtzSimpleTriggersCriteria);
    Paging paging = qrtzSimpleTriggersCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("QrtzSimpleTriggers.list", qrtzSimpleTriggersCriteria, rowBounds);
  }

  public int count(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria)
      throws DataAccessException {
    Assert.notNull(qrtzSimpleTriggersCriteria, "qrtzSimpleTriggersCriteria must not be null");

    return ((Integer) sqlSession.selectOne("QrtzSimpleTriggers.count", qrtzSimpleTriggersCriteria))
        .intValue();
  }

}
