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

import com.yihaodian.architecture.kira.manager.criteria.KiraClientMetadataCriteria;
import com.yihaodian.architecture.kira.manager.dao.KiraClientMetadataDao;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class KiraClientMetadataDaoImpl implements KiraClientMetadataDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(KiraClientMetadata kiraClientMetadata) throws DataAccessException {
    sqlSession.insert("KiraClientMetadata.insert", kiraClientMetadata);
  }

  public int update(KiraClientMetadata kiraClientMetadata) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("KiraClientMetadata.update", kiraClientMetadata);

    return actualRowsAffected;
  }

  public int delete(Long id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("KiraClientMetadata.delete", id);

    return actualRowsAffected;
  }

  public KiraClientMetadata select(Long id) throws DataAccessException {
    return (KiraClientMetadata) sqlSession.selectOne("KiraClientMetadata.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<KiraClientMetadata> list(KiraClientMetadataCriteria kiraClientMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(kiraClientMetadataCriteria, "kiraClientMetadataCriteria must not be null");

    return sqlSession.selectList("KiraClientMetadata.list", kiraClientMetadataCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<KiraClientMetadata> listOnPage(KiraClientMetadataCriteria kiraClientMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(kiraClientMetadataCriteria, "kiraClientMetadataCriteria must not be null");
    Assert.notNull(kiraClientMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = count(kiraClientMetadataCriteria);
    Paging paging = kiraClientMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("KiraClientMetadata.list", kiraClientMetadataCriteria, rowBounds);
  }

  public int count(KiraClientMetadataCriteria kiraClientMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(kiraClientMetadataCriteria, "kiraClientMetadataCriteria must not be null");

    return ((Integer) sqlSession.selectOne("KiraClientMetadata.count", kiraClientMetadataCriteria))
        .intValue();
  }

  @Override
  public List<String> getPoolIdList(
      KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    Assert.notNull(kiraClientMetadataCriteria, "kiraClientMetadataCriteria must not be null");

    return sqlSession.selectList("KiraClientMetadata.getPoolIdList", kiraClientMetadataCriteria);
  }

}
