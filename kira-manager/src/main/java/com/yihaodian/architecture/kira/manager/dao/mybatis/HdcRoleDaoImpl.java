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

import com.yihaodian.architecture.kira.manager.criteria.HdcRoleCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcRoleDao;
import com.yihaodian.architecture.kira.manager.domain.HdcRole;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class HdcRoleDaoImpl implements HdcRoleDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(HdcRole hdcRole) throws DataAccessException {
    sqlSession.insert("HdcRole.insert", hdcRole);
  }

  public int update(HdcRole hdcRole) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("HdcRole.update", hdcRole);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("HdcRole.delete", id);

    return actualRowsAffected;
  }

  public HdcRole select(Integer id) throws DataAccessException {
    return (HdcRole) sqlSession.selectOne("HdcRole.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<HdcRole> list(HdcRoleCriteria hdcRoleCriteria) throws DataAccessException {
    Assert.notNull(hdcRoleCriteria, "hdcRoleCriteria must not be null");
    return sqlSession.selectList("HdcRole.list", hdcRoleCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<HdcRole> listOnPage(HdcRoleCriteria hdcRoleCriteria) throws DataAccessException {
    Assert.notNull(hdcRoleCriteria, "hdcRoleCriteria must not be null");
    Assert.notNull(hdcRoleCriteria.getPaging(), "paging must not be null");

    int totalResults = count(hdcRoleCriteria);
    Paging paging = hdcRoleCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("HdcRole.list", hdcRoleCriteria, rowBounds);
  }

  public int count(HdcRoleCriteria hdcRoleCriteria) throws DataAccessException {
    Assert.notNull(hdcRoleCriteria, "hdcRoleCriteria must not be null");

    return ((Integer) sqlSession.selectOne("HdcRole.count", hdcRoleCriteria)).intValue();
  }

}
