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

import java.io.Serializable;

public class RunningEnvironmentForTimerTrigger implements Serializable {

  private static final long serialVersionUID = 1L;

  private String poolId;
  private String triggerId;
  private String environmentZNodeNameUnderEnvironmentsZNodeOfTrigger;
  private String environmentZKFullPathUnderEnvironmentsZNodeOfTrigger;
  private String centralScheduleServiceZKParentPath;

  public RunningEnvironmentForTimerTrigger() {
  }

  public RunningEnvironmentForTimerTrigger(String poolId, String triggerId,
      String environmentZNodeNameUnderEnvironmentsZNodeOfTrigger,
      String environmentZKFullPathUnderEnvironmentsZNodeOfTrigger,
      String centralScheduleServiceZKParentPath) {
    super();
    this.poolId = poolId;
    this.triggerId = triggerId;
    this.environmentZNodeNameUnderEnvironmentsZNodeOfTrigger = environmentZNodeNameUnderEnvironmentsZNodeOfTrigger;
    this.environmentZKFullPathUnderEnvironmentsZNodeOfTrigger = environmentZKFullPathUnderEnvironmentsZNodeOfTrigger;
    this.centralScheduleServiceZKParentPath = centralScheduleServiceZKParentPath;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  public String getPoolId() {
    return poolId;
  }

  public void setPoolId(String poolId) {
    this.poolId = poolId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public String getEnvironmentZNodeNameUnderEnvironmentsZNodeOfTrigger() {
    return environmentZNodeNameUnderEnvironmentsZNodeOfTrigger;
  }

  public void setEnvironmentZNodeNameUnderEnvironmentsZNodeOfTrigger(
      String environmentZNodeNameUnderEnvironmentsZNodeOfTrigger) {
    this.environmentZNodeNameUnderEnvironmentsZNodeOfTrigger = environmentZNodeNameUnderEnvironmentsZNodeOfTrigger;
  }

  public String getEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger() {
    return environmentZKFullPathUnderEnvironmentsZNodeOfTrigger;
  }

  public void setEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger(
      String environmentZKFullPathUnderEnvironmentsZNodeOfTrigger) {
    this.environmentZKFullPathUnderEnvironmentsZNodeOfTrigger = environmentZKFullPathUnderEnvironmentsZNodeOfTrigger;
  }

  public String getCentralScheduleServiceZKParentPath() {
    return centralScheduleServiceZKParentPath;
  }

  public void setCentralScheduleServiceZKParentPath(
      String centralScheduleServiceZKParentPath) {
    this.centralScheduleServiceZKParentPath = centralScheduleServiceZKParentPath;
  }

  @Override
  public String toString() {
    return "RunningEnvironmentForTimerTrigger [poolId=" + poolId
        + ", triggerId=" + triggerId
        + ", environmentZNodeNameUnderEnvironmentsZNodeOfTrigger="
        + environmentZNodeNameUnderEnvironmentsZNodeOfTrigger
        + ", environmentZKFullPathUnderEnvironmentsZNodeOfTrigger="
        + environmentZKFullPathUnderEnvironmentsZNodeOfTrigger
        + ", centralScheduleServiceZKParentPath=" + centralScheduleServiceZKParentPath + "]";
  }

}
