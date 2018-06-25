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
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.dao.TimerTriggerScheduleDao;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.util.Paging;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class TimerTriggerScheduleDaoImpl implements TimerTriggerScheduleDao {

  private SqlSession sqlSession;

  public void setSqlSession(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public void insert(TimerTriggerSchedule timerTriggerSchedule) throws DataAccessException {
    sqlSession.insert("TimerTriggerSchedule.insert", timerTriggerSchedule);
  }

  public int update(TimerTriggerSchedule timerTriggerSchedule) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.update("TimerTriggerSchedule.update", timerTriggerSchedule);

    return actualRowsAffected;
  }

  public int delete(Long id) throws DataAccessException {
    int actualRowsAffected = 1;

    sqlSession.delete("TimerTriggerSchedule.delete", id);

    return actualRowsAffected;
  }

  @Override
  public int deleteByCriteria(TimerTriggerScheduleCriteria criteria)
      throws DataAccessException {
    int deleteCount = sqlSession.delete("TimerTriggerSchedule.deleteByCriteria", criteria);
    return deleteCount;
  }

  @Override
  public TimerTriggerSchedule select(Long id) throws DataAccessException {
    return (TimerTriggerSchedule) sqlSession.selectOne("TimerTriggerSchedule.select", id);
  }

  @SuppressWarnings("unchecked")
  public List<TimerTriggerSchedule> list(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria)
      throws DataAccessException {
    Assert.notNull(timerTriggerScheduleCriteria, "timerTriggerScheduleCriteria must not be null");

    return sqlSession.selectList("TimerTriggerSchedule.list", timerTriggerScheduleCriteria);
  }

  @SuppressWarnings("unchecked")
  public List<TimerTriggerSchedule> listOnPage(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) throws DataAccessException {
    Assert.notNull(timerTriggerScheduleCriteria, "timerTriggerScheduleCriteria must not be null");
    Assert.notNull(timerTriggerScheduleCriteria.getPaging(), "paging must not be null");

    int totalResults = count(timerTriggerScheduleCriteria);
    Paging paging = timerTriggerScheduleCriteria.getPaging();
    paging.setTotalResults(totalResults);
    RowBounds rowBounds = new RowBounds(paging.getFirstResult(), paging.getMaxResults());

    return sqlSession
        .selectList("TimerTriggerSchedule.list", timerTriggerScheduleCriteria, rowBounds);
  }

  @SuppressWarnings("unchecked")
  public List<TimerTriggerSchedule> listOnPageUsingLimit(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) throws DataAccessException {
    Assert.notNull(timerTriggerScheduleCriteria, "timerTriggerScheduleCriteria must not be null");
    Assert.notNull(timerTriggerScheduleCriteria.getPaging(), "paging must not be null");

    int totalResults = count(timerTriggerScheduleCriteria);
    Paging paging = timerTriggerScheduleCriteria.getPaging();
    paging.setTotalResults(totalResults);

    return sqlSession
        .selectList("TimerTriggerSchedule.listUsingLimit", timerTriggerScheduleCriteria);
  }

  public int count(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria)
      throws DataAccessException {
    Assert.notNull(timerTriggerScheduleCriteria, "timerTriggerScheduleCriteria must not be null");

    return ((Integer) sqlSession
        .selectOne("TimerTriggerSchedule.count", timerTriggerScheduleCriteria)).intValue();
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityList(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) {
    Assert.notNull(timerTriggerScheduleCriteria, "timerTriggerScheduleCriteria must not be null");

    return sqlSession
        .selectList("TimerTriggerSchedule.getTriggerIdentityList", timerTriggerScheduleCriteria);
  }

  @Override
  public int updateAssignedServerForTrigger(String appId, String triggerId,
      String newAssignedServerId) {
    int returnValue = 0;

    Map<String, Object> param = new HashMap<String, Object>(3);
    param.put("appId", appId);
    param.put("triggerId", triggerId);
    param.put("newAssignedServerId", newAssignedServerId);

    returnValue = sqlSession.update("TimerTriggerSchedule.updateAssignedServerForTrigger", param);

    return returnValue;
  }

  @Override
  public Map<String, Integer> getAssignedServerIdAssignedCountMap(
      List<String> assignedServerIdList) throws DataAccessException {
    Map<String, Integer> returnValue = new HashMap<String, Integer>();
    Map<String, Object> param = new HashMap<String, Object>(1);
    param.put("assignedServerIdList", assignedServerIdList);

    Map<String, Map<String, String>> value = sqlSession
        .selectMap("TimerTriggerSchedule.getAssignedServerIdAssignedCountMap",
            param, "assignedServerId");

    if (value != null) {
      for (Map map : value.values()) {
        Integer count = (Integer) map.get("assignedCount");
        if(count != null) {
          returnValue.put("assignedCount", count);
        }
      }
    }

    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(
      String assignedServerId, Long minNextFireTimeInMs, Integer maxCount)
      throws DataAccessException {
    Assert.notNull(assignedServerId, "assignedServerId must not be null");

    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("assignedServerId", assignedServerId);

    param.put("minNextFireTimeInMs", minNextFireTimeInMs);

    param.put("maxCount", maxCount);

    return sqlSession.selectList(
        "TimerTriggerSchedule.getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow",
        param);
  }

  @Override
  public int updateByCriteria(TimerTriggerSchedule timerTriggerSchedule,
      TimerTriggerScheduleCriteria criteria)
      throws DataAccessException {
    Assert.notNull(timerTriggerSchedule, "timerTriggerSchedule must not be null");
    Assert.notNull(criteria, "criteria must not be null");

    Long id = criteria.getId();
    String appId = criteria.getAppId();
    String triggerId = criteria.getTriggerId();
    if ((null == id) && (StringUtils.isBlank(appId) || StringUtils.isBlank(triggerId))) {
      throw new IllegalArgumentException(
          "id and/or (appId,triggerId) should not be null or blank for updateByCriteria.");
    }

    int returnValue = 0;

    Map<String, Object> param = new HashMap<String, Object>(2);
    param.put("timerTriggerSchedule", timerTriggerSchedule);
    param.put("criteria", criteria);

    returnValue = sqlSession.update("TimerTriggerSchedule.updateByCriteria", param);

    return returnValue;
  }
}
