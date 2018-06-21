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
package com.yihaodian.architecture.kira.manager.dto;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.util.TimerTriggerStateEnum;
import java.io.Serializable;
import java.util.Date;

public class PoolTriggerStatus implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;
  private String triggerId;
  private Date startTime;
  private Date endTime;
  private Date previousFireTime;
  private Date nextFireTime;
  private Date finalFireTime;
  private TimerTriggerStateEnum triggerState;

  public PoolTriggerStatus() {
    // TODO Auto-generated constructor stub
  }

  public PoolTriggerStatus(String appId, String triggerId, Date startTime,
      Date endTime, Date previousFireTime, Date nextFireTime,
      Date finalFireTime, TimerTriggerStateEnum triggerState) {
    super();
    this.appId = appId;
    this.triggerId = triggerId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.previousFireTime = previousFireTime;
    this.nextFireTime = nextFireTime;
    this.finalFireTime = finalFireTime;
    this.triggerState = triggerState;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getStartTimeAsString() {
    return KiraCommonUtils.getDateAsString(startTime);
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getEndTimeAsString() {
    return KiraCommonUtils.getDateAsString(endTime);
  }

  public Date getPreviousFireTime() {
    return previousFireTime;
  }

  public void setPreviousFireTime(Date previousFireTime) {
    this.previousFireTime = previousFireTime;
  }

  public String getPreviousFireTimeAsString() {
    return KiraCommonUtils.getDateAsString(previousFireTime);
  }

  public Date getNextFireTime() {
    return nextFireTime;
  }

  public void setNextFireTime(Date nextFireTime) {
    this.nextFireTime = nextFireTime;
  }

  public String getNextFireTimeAsString() {
    return KiraCommonUtils.getDateAsString(nextFireTime);
  }

  public Date getFinalFireTime() {
    return finalFireTime;
  }

  public void setFinalFireTime(Date finalFireTime) {
    this.finalFireTime = finalFireTime;
  }

  public String getFinalFireTimeAsString() {
    return KiraCommonUtils.getDateAsString(finalFireTime);
  }

  public TimerTriggerStateEnum getTriggerState() {
    return triggerState;
  }

  public void setTriggerState(TimerTriggerStateEnum triggerState) {
    this.triggerState = triggerState;
  }

  public String getTriggerStateAsString() {
    return triggerState.getName();
  }

}
