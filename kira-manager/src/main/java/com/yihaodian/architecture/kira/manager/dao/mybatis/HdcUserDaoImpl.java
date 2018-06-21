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

import com.yihaodian.architecture.kira.manager.criteria.HdcUserCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcUserDao;
import com.yihaodian.architecture.kira.manager.domain.HdcUser;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class HdcUserDaoImpl implements HdcUserDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(HdcUser hdcUser) throws DataAccessException {
    sqlSession.insert("HdcUser.insert", hdcUser);
  }

  public int update(HdcUser hdcUser) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("HdcUser.update", hdcUser);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("HdcUser.delete", id);

    return actualRowsAffected;
  }

  public HdcUser select(Integer id) throws DataAccessException {
    return (HdcUser) sqlSession.selectOne("HdcUser.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<HdcUser> list(HdcUserCriteria hdcUserCriteria) throws DataAccessException {
    Assert.notNull(hdcUserCriteria, "hdcUserCriteria must not be null");

    return sqlSession.selectList("HdcUser.list", hdcUserCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<HdcUser> listOnPage(HdcUserCriteria hdcUserCriteria) throws DataAccessException {
    Assert.notNull(hdcUserCriteria, "hdcUserCriteria must not be null");
    Assert.notNull(hdcUserCriteria.getPaging(), "paging must not be null");

    int totalResults = count(hdcUserCriteria);
    Paging paging = hdcUserCriteria.getPaging();
    paging.setTotalResults(totalResults);

    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());
    return sqlSession.selectList("HdcUser.list", hdcUserCriteria, rowBounds);
  }

  public int count(HdcUserCriteria hdcUserCriteria) throws DataAccessException {
    Assert.notNull(hdcUserCriteria, "hdcUserCriteria must not be null");

    return ((Integer) sqlSession.selectOne("HdcUser.count", hdcUserCriteria)).intValue();
  }

}
