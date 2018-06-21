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

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.dao.TriggerMetadataDao;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class TriggerMetadataDaoImpl implements TriggerMetadataDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(TriggerMetadata triggerMetadata) throws DataAccessException {
    sqlSession.insert("TriggerMetadata.insert", triggerMetadata);
  }

  public int update(TriggerMetadata triggerMetadata) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("TriggerMetadata.update", triggerMetadata);

    return actualRowsAffected;
  }

  public int delete(Long id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("TriggerMetadata.delete", id);

    return actualRowsAffected;
  }

  public TriggerMetadata select(Long id) throws DataAccessException {
    return (TriggerMetadata) sqlSession.selectOne("TriggerMetadata.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<TriggerMetadata> list(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectList("TriggerMetadata.list", triggerMetadataCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<TriggerMetadata> listOnPage(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = count(triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("TriggerMetadata.list", triggerMetadataCriteria, rowBounds);
  }

  public int count(TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return ((Integer) sqlSession.selectOne("TriggerMetadata.count", triggerMetadataCriteria))
        .intValue();
  }

  @Override
  public TriggerMetadata getLatestAndAvailableTriggerMetadata(String appId, String triggerId,
      String version) throws DataAccessException {
    Assert.notNull(appId, "appId must not be null");
    Assert.notNull(triggerId, "triggerId must not be null");

    Map<String, Object> param = new HashMap<String, Object>(3);
    param.put("appId", appId);
    param.put("triggerId", triggerId);
    param.put("version", version);

    return (TriggerMetadata) sqlSession
        .selectOne("TriggerMetadata.getLatestAndAvailableTriggerMetadata", param);
  }

  @Override
  public List<TriggerMetadata> listLatest(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectOne("TriggerMetadata.listLatest", triggerMetadataCriteria);
  }

  @Override
  public List<TriggerMetadata> listLatestOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = countLatest(triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("TriggerMetadata.listLatest", triggerMetadataCriteria, rowBounds);
  }

  @Override
  public List<TriggerMetadata> listLatestOnPageUsingLimit(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = countLatest(triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);

    return sqlSession.selectList("TriggerMetadata.listLatestUsingLimit", triggerMetadataCriteria);
  }

  @Override
  public int countLatest(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return ((Integer) sqlSession.selectOne("TriggerMetadata.countLatest", triggerMetadataCriteria))
        .intValue();
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityList(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectList("TriggerMetadata.getTriggerIdentityList", triggerMetadataCriteria);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = countTriggerIdentityList(triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("TriggerMetadata.getTriggerIdentityList", triggerMetadataCriteria, rowBounds);
  }

  @Override
  public int countTriggerIdentityList(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("TriggerMetadata.countTriggerIdentityList", triggerMetadataCriteria)).intValue();
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWithoutVersion(
      TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectList("TriggerMetadata.getTriggerIdentityListWithoutVersion",
        triggerMetadataCriteria);
  }

  @Override
  public List<String> getPoolIdList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectList("TriggerMetadata.getPoolIdList", triggerMetadataCriteria);
  }

  @Override
  public List<String> getTriggerIdList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return sqlSession.selectList("TriggerMetadata.getTriggerIdList", triggerMetadataCriteria);
  }

  @Override
  public int updateUnRegisteredStatusForTriggers(List<TriggerIdentity> triggerIdentityList,
      Boolean unRegistered) {
    int returnValue = 0;
    if (!CollectionUtils.isEmpty(triggerIdentityList)) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("triggerIdentityList", triggerIdentityList);
      map.put("unRegistered", unRegistered);
      map.put("unregisteredUpdateTime", new Date());
      returnValue = sqlSession.update("TriggerMetadata.updateUnRegisteredStatusForTriggers", map);
    }
    return returnValue;
  }

  @Override
  public int updateDeletedStatusForTriggers(
      List<TriggerIdentity> triggerIdentityList, Boolean deleted, String comments) {
    int returnValue = 0;
    if (!CollectionUtils.isEmpty(triggerIdentityList)) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("triggerIdentityList", triggerIdentityList);
      map.put("deleted", deleted);
      map.put("deletedUpdateTime", new Date());
      map.put("comments", comments);
      returnValue = sqlSession.update("TriggerMetadata.updateDeletedStatusForTriggers", map);
    }
    return returnValue;
  }

  @Override
  public TriggerMetadata getLatestAndAvailableTriggerMetadataByJobId(String jobId) {
    return (TriggerMetadata) sqlSession
        .selectOne("TriggerMetadata.getLatestAndAvailableTriggerMetadataByJobId", jobId);
  }

  @Override
  public int updateUnRegisteredStatus(TriggerMetadataCriteria triggerMetadataCriteria,
      Boolean unRegistered) {
    int returnValue = 0;
    if (null != triggerMetadataCriteria) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("triggerMetadataCriteria", triggerMetadataCriteria);
      map.put("unRegistered", unRegistered);
      map.put("unregisteredUpdateTime", new Date());
      returnValue = sqlSession.update("TriggerMetadata.updateUnRegisteredStatus", map);
    }
    return returnValue;
  }

  @Override
  public List<String> getPoolIdListOfTriggersWhichHasNoKiraClientMetadata() {
    return sqlSession
        .selectList("TriggerMetadata.getPoolIdListOfTriggersWhichHasNoKiraClientMetadata");
  }

  @Override
  public List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = countLatestTriggerMetadatasWhichSetTriggerAsTarget(triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession.selectList("TriggerMetadata.getLatestTriggerMetadatasWhichSetTriggerAsTarget",
        triggerMetadataCriteria, rowBounds);
  }

  @Override
  public int countLatestTriggerMetadatasWhichSetTriggerAsTarget(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("TriggerMetadata.countLatestTriggerMetadatasWhichSetTriggerAsTarget",
            triggerMetadataCriteria)).intValue();
  }

  @Override
  public List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");
    Assert.notNull(triggerMetadataCriteria.getPaging(), "paging must not be null");

    int totalResults = countLatestTriggerMetadatasWhichSetTriggersOfPoolAsTarget(
        triggerMetadataCriteria);
    Paging paging = triggerMetadataCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("TriggerMetadata.getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTarget",
            triggerMetadataCriteria, rowBounds);

  }

  @Override
  public int countLatestTriggerMetadatasWhichSetTriggersOfPoolAsTarget(
      TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException {
    Assert.notNull(triggerMetadataCriteria, "triggerMetadataCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("TriggerMetadata.countLatestTriggerMetadatasWhichSetTriggersOfPoolAsTarget",
            triggerMetadataCriteria)).intValue();
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(
      Boolean masterZone)
      throws DataAccessException {
    Assert.notNull(masterZone, "masterZone must not be null");
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("masterZone", masterZone);
    return sqlSession
        .selectList("TriggerMetadata.getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion",
            param);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(
      Boolean masterZone)
      throws DataAccessException {
    Assert.notNull(masterZone, "masterZone must not be null");
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("masterZone", masterZone);
    return sqlSession
        .selectList("TriggerMetadata.getTriggerIdentityListWhichShouldBeScheduledWithoutVersion",
            param);
  }

  @Override
  public int updateCrossMultiZoneData(String appId, String triggerId,
      String version, Boolean copyFromMasterToSlaveZone,
      Boolean onlyScheduledInMasterZone, String comments)
      throws DataAccessException {
    Assert.notNull(appId, "appId must not be null");
    Assert.notNull(triggerId, "triggerId must not be null");
    Assert.notNull(version, "version must not be null");

    int returnValue = 0;
    Map<String, Object> map = new HashMap<String, Object>(6);
    map.put("appId", appId);
    map.put("triggerId", triggerId);
    map.put("version", version);
    map.put("copyFromMasterToSlaveZone", copyFromMasterToSlaveZone);
    map.put("onlyScheduledInMasterZone", onlyScheduledInMasterZone);
    map.put("comments", comments);
    returnValue = sqlSession.update("TriggerMetadata.updateCrossMultiZoneData", map);
    return returnValue;
  }
}
