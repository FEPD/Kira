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
package com.yihaodian.architecture.kira.manager.action;

import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class TimerTriggerScheduleAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private TimerTriggerScheduleCriteria criteria = new TimerTriggerScheduleCriteria();

  private transient TimerTriggerScheduleService timerTriggerScheduleService;

  public TimerTriggerScheduleAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public TimerTriggerScheduleCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(TimerTriggerScheduleCriteria criteria) {
    this.criteria = criteria;
  }

  public void setTimerTriggerScheduleService(
      TimerTriggerScheduleService timerTriggerScheduleService) {
    this.timerTriggerScheduleService = timerTriggerScheduleService;
  }

  public String list() throws Exception {
    List<TimerTriggerSchedule> timerTriggerScheduleList = timerTriggerScheduleService
        .list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, timerTriggerScheduleList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<TimerTriggerSchedule> timerTriggerScheduleList = timerTriggerScheduleService
        .listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, timerTriggerScheduleList);
    return null;
  }

  public String getAllMisfiredTimerTriggerScheduleList() throws Exception {
    List<TimerTriggerSchedule> timerTriggerScheduleList = timerTriggerScheduleService
        .getAllMisfiredTimerTriggerScheduleList();
    Utils.sendHttpResponseForStruts2(criteria, timerTriggerScheduleList);
    return null;
  }

}
