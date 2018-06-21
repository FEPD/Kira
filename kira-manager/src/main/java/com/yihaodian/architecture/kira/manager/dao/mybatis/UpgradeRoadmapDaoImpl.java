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

import com.yihaodian.architecture.kira.manager.criteria.UpgradeRoadmapCriteria;
import com.yihaodian.architecture.kira.manager.dao.UpgradeRoadmapDao;
import com.yihaodian.architecture.kira.manager.domain.UpgradeRoadmap;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class UpgradeRoadmapDaoImpl implements UpgradeRoadmapDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(UpgradeRoadmap upgradeRoadmap) throws DataAccessException {
    sqlSession.insert("UpgradeRoadmap.insert", upgradeRoadmap);
  }

  public int update(UpgradeRoadmap upgradeRoadmap) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("UpgradeRoadmap.update", upgradeRoadmap);

    return actualRowsAffected;
  }

  public int delete(Long id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("UpgradeRoadmap.delete", id);

    return actualRowsAffected;
  }

  public UpgradeRoadmap select(Long id) throws DataAccessException {
    return (UpgradeRoadmap) sqlSession.selectOne("UpgradeRoadmap.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<UpgradeRoadmap> list(UpgradeRoadmapCriteria upgradeRoadmapCriteria)
      throws DataAccessException {
    Assert.notNull(upgradeRoadmapCriteria, "upgradeRoadmapCriteria must not be null");

    return sqlSession.selectList("UpgradeRoadmap.list", upgradeRoadmapCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<UpgradeRoadmap> listOnPage(UpgradeRoadmapCriteria upgradeRoadmapCriteria)
      throws DataAccessException {
    Assert.notNull(upgradeRoadmapCriteria, "upgradeRoadmapCriteria must not be null");
    Assert.notNull(upgradeRoadmapCriteria.getPaging(), "paging must not be null");

    int totalResults = count(upgradeRoadmapCriteria);
    Paging paging = upgradeRoadmapCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("UpgradeRoadmap.list", upgradeRoadmapCriteria, rowBounds);
  }

  public int count(UpgradeRoadmapCriteria upgradeRoadmapCriteria) throws DataAccessException {
    Assert.notNull(upgradeRoadmapCriteria, "upgradeRoadmapCriteria must not be null");

    return ((Integer) sqlSession.selectOne("UpgradeRoadmap.count", upgradeRoadmapCriteria))
        .intValue();
  }

}
