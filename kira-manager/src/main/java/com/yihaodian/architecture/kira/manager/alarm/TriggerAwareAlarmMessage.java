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
package com.yihaodian.architecture.kira.manager.alarm;

public abstract class TriggerAwareAlarmMessage extends AlarmMessageBase {

  private static final long serialVersionUID = 1L;

  private String appId;
  private String triggerId;
  private String triggerVersion;
  private String triggerDescription;

  public TriggerAwareAlarmMessage() {
    // TODO Auto-generated constructor stub
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

  public String getTriggerVersion() {
    return triggerVersion;
  }

  public void setTriggerVersion(String triggerVersion) {
    this.triggerVersion = triggerVersion;
  }

  public String getTriggerDescription() {
    return triggerDescription;
  }

  public void setTriggerDescription(String triggerDescription) {
    this.triggerDescription = triggerDescription;
  }

}
