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

public class KiraClientMetadataUpdateContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private Boolean visibilityLimited;

  private String visibleForUsers;

  private Boolean sendAlarmEmail;

  private String emailsToReceiveAlarm;

  private Boolean sendAlarmSMS;

  private String phoneNumbersToReceiveAlarmSMS;

  public KiraClientMetadataUpdateContent() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Boolean getVisibilityLimited() {
    return visibilityLimited;
  }

  public void setVisibilityLimited(Boolean visibilityLimited) {
    this.visibilityLimited = visibilityLimited;
  }

  public String getVisibleForUsers() {
    return visibleForUsers;
  }

  public void setVisibleForUsers(String visibleForUsers) {
    this.visibleForUsers = visibleForUsers;
  }

  public Boolean getSendAlarmEmail() {
    return sendAlarmEmail;
  }

  public void setSendAlarmEmail(Boolean sendAlarmEmail) {
    this.sendAlarmEmail = sendAlarmEmail;
  }

  public String getEmailsToReceiveAlarm() {
    return emailsToReceiveAlarm;
  }

  public void setEmailsToReceiveAlarm(String emailsToReceiveAlarm) {
    this.emailsToReceiveAlarm = emailsToReceiveAlarm;
  }

  public Boolean getSendAlarmSMS() {
    return sendAlarmSMS;
  }

  public void setSendAlarmSMS(Boolean sendAlarmSMS) {
    this.sendAlarmSMS = sendAlarmSMS;
  }

  public String getPhoneNumbersToReceiveAlarmSMS() {
    return phoneNumbersToReceiveAlarmSMS;
  }

  public void setPhoneNumbersToReceiveAlarmSMS(
      String phoneNumbersToReceiveAlarmSMS) {
    this.phoneNumbersToReceiveAlarmSMS = phoneNumbersToReceiveAlarmSMS;
  }

}
