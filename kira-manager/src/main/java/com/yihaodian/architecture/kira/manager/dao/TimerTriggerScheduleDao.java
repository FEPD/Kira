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
package com.yihaodian.architecture.kira.manager.dao;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;

public interface TimerTriggerScheduleDao {

  void insert(TimerTriggerSchedule timerTriggerSchedule) throws DataAccessException;

  int update(TimerTriggerSchedule timerTriggerSchedule) throws DataAccessException;

  int delete(Long id) throws DataAccessException;

  int deleteByCriteria(TimerTriggerScheduleCriteria criteria) throws DataAccessException;

  TimerTriggerSchedule select(Long id) throws DataAccessException;

  List<TimerTriggerSchedule> list(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria)
      throws DataAccessException;

  List<TimerTriggerSchedule> listOnPage(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria)
      throws DataAccessException;

  List<TimerTriggerSchedule> listOnPageUsingLimit(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) throws DataAccessException;

  int count(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityList(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria) throws DataAccessException;

  int updateAssignedServerForTrigger(String poolId, String triggerId, String newAssignedServerId)
      throws DataAccessException;

  Map<String, Integer> getAssignedServerIdAssignedCountMap(List<String> assignedServerIdList)
      throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(
      String assignedServerId, Long minNextFireTimeInMs, Integer maxCount)
      throws DataAccessException;

  int updateByCriteria(TimerTriggerSchedule timerTriggerSchedule,
      TimerTriggerScheduleCriteria criteria) throws DataAccessException;

}
