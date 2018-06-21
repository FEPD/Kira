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

import com.yihaodian.architecture.kira.manager.criteria.HdcMenuCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcMenuDao;
import com.yihaodian.architecture.kira.manager.domain.HdcMenu;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class HdcMenuDaoImpl implements HdcMenuDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(HdcMenu hdcMenu) throws DataAccessException {

    sqlSession.insert("HdcMenu.insert", hdcMenu);
  }

  public int update(HdcMenu hdcMenu) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("HdcMenu.update", hdcMenu);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("HdcMenu.delete", id);

    return actualRowsAffected;
  }

  public HdcMenu select(Integer id) throws DataAccessException {
    return (HdcMenu) sqlSession.selectOne("HdcMenu.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<HdcMenu> list(HdcMenuCriteria hdcMenuCriteria) throws DataAccessException {
    Assert.notNull(hdcMenuCriteria, "hdcMenuCriteria must not be null");
    return sqlSession.selectList("HdcMenu.list", hdcMenuCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<HdcMenu> listOnPage(HdcMenuCriteria hdcMenuCriteria) throws DataAccessException {
    Assert.notNull(hdcMenuCriteria, "hdcMenuCriteria must not be null");
    Assert.notNull(hdcMenuCriteria.getPaging(), "paging must not be null");

    int totalResults = count(hdcMenuCriteria);
    Paging paging = hdcMenuCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("HdcMenu.list", hdcMenuCriteria, rowBounds);
  }

  public int count(HdcMenuCriteria hdcMenuCriteria) throws DataAccessException {
    Assert.notNull(hdcMenuCriteria, "hdcMenuCriteria must not be null");

    return ((Integer) sqlSession.selectOne("HdcMenu.count", hdcMenuCriteria)).intValue();
  }

}
