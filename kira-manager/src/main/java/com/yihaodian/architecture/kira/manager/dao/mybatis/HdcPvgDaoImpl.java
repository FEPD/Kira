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

import com.yihaodian.architecture.kira.manager.criteria.HdcPvgCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcPvgDao;
import com.yihaodian.architecture.kira.manager.domain.HdcPvg;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class HdcPvgDaoImpl implements HdcPvgDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(HdcPvg hdcPvg) throws DataAccessException {
    sqlSession.insert("HdcPvg.insert", hdcPvg);

  }

  public int update(HdcPvg hdcPvg) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("HdcPvg.update", hdcPvg);

    return actualRowsAffected;
  }

  public int delete(Integer id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("HdcPvg.delete", id);

    return actualRowsAffected;
  }

  public HdcPvg select(Integer id) throws DataAccessException {
    return (HdcPvg) sqlSession.selectOne("HdcPvg.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<HdcPvg> list(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException {
    Assert.notNull(hdcPvgCriteria, "hdcPvgCriteria must not be null");
    return sqlSession.selectList("HdcPvg.list", hdcPvgCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<HdcPvg> listOnPage(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException {
    Assert.notNull(hdcPvgCriteria, "hdcPvgCriteria must not be null");
    Assert.notNull(hdcPvgCriteria.getPaging(), "paging must not be null");

    int totalResults = count(hdcPvgCriteria);
    Paging paging = hdcPvgCriteria.getPaging();
    paging.setTotalResults(totalResults);

    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("HdcPvg.list", hdcPvgCriteria, rowBounds);
  }

  public int count(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException {
    Assert.notNull(hdcPvgCriteria, "hdcPvgCriteria must not be null");

    return ((Integer) sqlSession.selectOne("HdcPvg.count", hdcPvgCriteria)).intValue();
  }

}
