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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.dao.TimerTriggerScheduleDao;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class TimerTriggerScheduleServiceImpl extends Service implements
    TimerTriggerScheduleService {

  private TimerTriggerScheduleDao timerTriggerScheduleDao;

  public void setTimerTriggerScheduleDao(TimerTriggerScheduleDao timerTriggerScheduleDao) {
    this.timerTriggerScheduleDao = timerTriggerScheduleDao;
  }

  public void insert(TimerTriggerSchedule timerTriggerSchedule) {
    timerTriggerScheduleDao.insert(timerTriggerSchedule);
  }

  public int update(TimerTriggerSchedule timerTriggerSchedule) {
    int actualRowsAffected = 0;

    Long id = timerTriggerSchedule.getId();

    TimerTriggerSchedule _oldTimerTriggerSchedule = timerTriggerScheduleDao.select(id);

    if (_oldTimerTriggerSchedule != null) {
      actualRowsAffected = timerTriggerScheduleDao.update(timerTriggerSchedule);
    }

    return actualRowsAffected;
  }

  public int delete(Long id) {
    if (null == id) {
      throw new IllegalArgumentException("id should not be null for delete.");
    }
    int deleteCount = timerTriggerScheduleDao.delete(id);

    return deleteCount;
  }

  @Override
  public int deleteByCriteria(TimerTriggerScheduleCriteria criteria) {
    int deleteCount = timerTriggerScheduleDao.deleteByCriteria(criteria);
    return deleteCount;
  }

  @Override
  public int deleteByPoolIdAndTriggerId(String appId, String triggerId) {
    TimerTriggerScheduleCriteria criteria = new TimerTriggerScheduleCriteria();
    if (StringUtils.isBlank(appId) || StringUtils.isBlank(triggerId)) {
      throw new IllegalArgumentException(
          "appId and triggerId should both not be blank for deleteByPoolIdAndTriggerId.");
    }
    criteria.setAppId(appId);
    criteria.setTriggerId(triggerId);
    return timerTriggerScheduleDao.deleteByCriteria(criteria);
  }

  public TimerTriggerSchedule select(Long id) {
    return timerTriggerScheduleDao.select(id);
  }

  @Override
  public TimerTriggerSchedule getTimerTriggerScheduleByPoolIdAndTriggerId(String appId,
      String triggerId) {
    TimerTriggerSchedule returnValue = null;
    if (StringUtils.isBlank(appId) || StringUtils.isBlank(triggerId)) {
      throw new IllegalArgumentException(
          "appId and triggerId should both not be blank for getTimerTriggerScheduleByPoolIdAndTriggerId.");
    }
    TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    timerTriggerScheduleCriteria.setAppId(appId);
    timerTriggerScheduleCriteria.setTriggerId(triggerId);
    List<TimerTriggerSchedule> timerTriggerScheduleList = timerTriggerScheduleDao
        .list(timerTriggerScheduleCriteria);
    if (CollectionUtils.isNotEmpty(timerTriggerScheduleList)) {
      returnValue = timerTriggerScheduleList.get(0);
    }
    return returnValue;
  }

  public List<TimerTriggerSchedule> list(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) {
    return timerTriggerScheduleDao.list(timerTriggerScheduleCriteria);
  }

  public List<TimerTriggerSchedule> listOnPage(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) {
    return timerTriggerScheduleDao.listOnPageUsingLimit(timerTriggerScheduleCriteria);
  }

  @Override
  public List<TimerTriggerSchedule> getAllMisfiredTimerTriggerScheduleList() {
    TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    long oneMinuteInMs = 60000L;
    Date now = new Date();
    Date misfireTime = KiraManagerUtils.getDateAfterAddMilliseconds(now, -oneMinuteInMs);
    timerTriggerScheduleCriteria.setMisfireTime(misfireTime);

    List<TimerTriggerSchedule> returnValue = timerTriggerScheduleDao
        .list(timerTriggerScheduleCriteria);
    return returnValue;
  }

  @Override
  public int count(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) {
    return timerTriggerScheduleDao.count(timerTriggerScheduleCriteria);
  }

  @Override
  public void saveByPoolIdAndTriggerId(TimerTriggerSchedule timerTriggerSchedule) {
    String appId = timerTriggerSchedule.getAppId();
    String triggerId = timerTriggerSchedule.getTriggerId();
    TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    timerTriggerScheduleCriteria.setAppId(appId);
    timerTriggerScheduleCriteria.setTriggerId(triggerId);
    int count = timerTriggerScheduleDao.count(timerTriggerScheduleCriteria);
    if (count <= 0) {
      timerTriggerScheduleDao.insert(timerTriggerSchedule);
    } else {
      timerTriggerScheduleDao.updateByCriteria(timerTriggerSchedule, timerTriggerScheduleCriteria);
    }
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityList(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) {
    List<TriggerIdentity> returnValue = null;
    returnValue = timerTriggerScheduleDao.getTriggerIdentityList(timerTriggerScheduleCriteria);
    if (null == returnValue) {
      returnValue = new ArrayList<TriggerIdentity>();
    }

    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getAssignedTriggerIdentityListByServerId(String assignedServerId) {
    TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
    timerTriggerScheduleCriteria.setAssignedServerId(assignedServerId);
    List<TriggerIdentity> assignedTriggerIdentityListByServerId = this
        .getTriggerIdentityList(timerTriggerScheduleCriteria);

    return assignedTriggerIdentityListByServerId;
  }

  @Override
  public void insertByTriggerIdentityList(
      List<TriggerIdentity> triggerIdentityList) {
    if (CollectionUtils.isNotEmpty(triggerIdentityList)) {
      TimerTriggerSchedule timerTriggerSchedule = null;
      for (TriggerIdentity triggerIdentity : triggerIdentityList) {
        timerTriggerSchedule = new TimerTriggerSchedule();
        String appId = triggerIdentity.getAppId();
        String triggerId = triggerIdentity.getTriggerId();
        timerTriggerSchedule.setAppId(appId);
        timerTriggerSchedule.setTriggerId(triggerId);
        timerTriggerSchedule.setTimesTriggered(Long.valueOf(0));
        timerTriggerSchedule.setCreateTime(Long.valueOf(new Date().getTime()));

        timerTriggerScheduleDao.insert(timerTriggerSchedule);
      }
    }
  }

  @Override
  public int deleteByTriggerIdentityList(
      List<TriggerIdentity> triggerIdentityList) {
    int returnValue = 0;
    if (CollectionUtils.isNotEmpty(triggerIdentityList)) {
      for (TriggerIdentity triggerIdentity : triggerIdentityList) {
        String appId = triggerIdentity.getAppId();
        String triggerId = triggerIdentity.getTriggerId();
        int deleteCount = this.deleteByPoolIdAndTriggerId(appId, triggerId);
        returnValue = returnValue + deleteCount;
      }
    }
    return returnValue;
  }

  @Override
  public int updateAssignedServerForTrigger(String appId, String triggerId,
      String newAssignedServerId) {
    return timerTriggerScheduleDao
        .updateAssignedServerForTrigger(appId, triggerId, newAssignedServerId);
  }

  @Override
  public Map<String, Integer> getAssignedServerIdAssignedCountMap(
      List<String> assignedServerIdList) {
    return timerTriggerScheduleDao.getAssignedServerIdAssignedCountMap(assignedServerIdList);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(
      String assignedServerId, Long minNextFireTimeInMs, Integer maxCount) {
    List<TriggerIdentity> returnValue = timerTriggerScheduleDao
        .getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(assignedServerId,
            minNextFireTimeInMs, maxCount);
    if (null == returnValue) {
      returnValue = new ArrayList<TriggerIdentity>();
    }
    return returnValue;
  }

  @Override
  public int updateByCriteria(TimerTriggerSchedule timerTriggerSchedule,
      TimerTriggerScheduleCriteria criteria) {
    return timerTriggerScheduleDao.updateByCriteria(timerTriggerSchedule, criteria);
  }
}
