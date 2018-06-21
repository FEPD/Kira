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
package com.yihaodian.architecture.kira.manager.service;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import java.util.List;
import java.util.Map;

public interface TimerTriggerScheduleService {

  void insert(TimerTriggerSchedule timerTriggerSchedule);

  int update(TimerTriggerSchedule timerTriggerSchedule);

  int delete(Long id);

  int deleteByCriteria(TimerTriggerScheduleCriteria criteria);

  int deleteByPoolIdAndTriggerId(String poolId, String triggerId);

  TimerTriggerSchedule select(Long id);

  TimerTriggerSchedule getTimerTriggerScheduleByPoolIdAndTriggerId(String poolId, String triggerId);

  List<TimerTriggerSchedule> list(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria);

  List<TimerTriggerSchedule> listOnPage(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria);

  List<TimerTriggerSchedule> getAllMisfiredTimerTriggerScheduleList();

  int count(TimerTriggerScheduleCriteria timerTriggerScheduleCriteria);

  void saveByPoolIdAndTriggerId(TimerTriggerSchedule timerTriggerSchedule);

  List<TriggerIdentity> getTriggerIdentityList(
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria);

  List<TriggerIdentity> getAssignedTriggerIdentityListByServerId(String assignedServerId);

  void insertByTriggerIdentityList(List<TriggerIdentity> triggerIdentityList);

  int deleteByTriggerIdentityList(List<TriggerIdentity> triggerIdentityList);

  int updateAssignedServerForTrigger(String poolId, String triggerId, String newAssignedServerId);

  Map<String, Integer> getAssignedServerIdAssignedCountMap(List<String> assignedServerIdList);

  List<TriggerIdentity> getTriggerIdentityListWithinAssignedServerWhichCanBeUnassignedNow(
      String assignedServerId, Long minNextFireTimeInMs, Integer maxCount);

  int updateByCriteria(TimerTriggerSchedule timerTriggerSchedule,
      TimerTriggerScheduleCriteria criteria);
}
