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
package com.yihaodian.architecture.kira.common.dto;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class KiraClientRegisterData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;

  private String host;

  private Integer pid;

  private String applicationId;

  private String kiraClientVersion;

  private boolean visibilityLimited;

  private String visibleForUsers;

  private boolean sendAlarmEmail;

  private String emailsToReceiveAlarm;

  private boolean sendAlarmSMS;

  private String phoneNumbersToReceiveAlarmSMS;

  private boolean keepKiraClientConfigDataOnKiraServerUnchanged;

  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  private Date createTime = new Date();

  public KiraClientRegisterData() {
    // TODO Auto-generated constructor stub
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getKiraClientVersion() {
    return kiraClientVersion;
  }

  public void setKiraClientVersion(String kiraClientVersion) {
    this.kiraClientVersion = kiraClientVersion;
  }

  public Integer getPid() {
    return pid;
  }

  public void setPid(Integer pid) {
    this.pid = pid;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public boolean isVisibilityLimited() {
    return visibilityLimited;
  }

  public void setVisibilityLimited(boolean visibilityLimited) {
    this.visibilityLimited = visibilityLimited;
  }

  public String getVisibleForUsers() {
    return visibleForUsers;
  }

  public void setVisibleForUsers(String visibleForUsers) {
    this.visibleForUsers = visibleForUsers;
  }

  public boolean isSendAlarmEmail() {
    return sendAlarmEmail;
  }

  public void setSendAlarmEmail(boolean sendAlarmEmail) {
    this.sendAlarmEmail = sendAlarmEmail;
  }

  public String getEmailsToReceiveAlarm() {
    return emailsToReceiveAlarm;
  }

  public void setEmailsToReceiveAlarm(String emailsToReceiveAlarm) {
    this.emailsToReceiveAlarm = emailsToReceiveAlarm;
  }

  public boolean isSendAlarmSMS() {
    return sendAlarmSMS;
  }

  public void setSendAlarmSMS(boolean sendAlarmSMS) {
    this.sendAlarmSMS = sendAlarmSMS;
  }

  public String getPhoneNumbersToReceiveAlarmSMS() {
    return phoneNumbersToReceiveAlarmSMS;
  }

  public void setPhoneNumbersToReceiveAlarmSMS(
      String phoneNumbersToReceiveAlarmSMS) {
    this.phoneNumbersToReceiveAlarmSMS = phoneNumbersToReceiveAlarmSMS;
  }

  public boolean isKeepKiraClientConfigDataOnKiraServerUnchanged() {
    return keepKiraClientConfigDataOnKiraServerUnchanged;
  }

  public void setKeepKiraClientConfigDataOnKiraServerUnchanged(
      boolean keepKiraClientConfigDataOnKiraServerUnchanged) {
    this.keepKiraClientConfigDataOnKiraServerUnchanged = keepKiraClientConfigDataOnKiraServerUnchanged;
  }

  public Map<Serializable, Serializable> getOtherDataMap() {
    return otherDataMap;
  }

  public void setOtherDataMap(Map<Serializable, Serializable> otherDataMap) {
    this.otherDataMap = otherDataMap;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
  }

  @Override
  public String toString() {
    return "KiraClientRegisterData{" +
        "appId='" + appId + '\'' +
        ", host='" + host + '\'' +
        ", pid=" + pid +
        ", applicationId='" + applicationId + '\'' +
        ", kiraClientVersion='" + kiraClientVersion + '\'' +
        ", visibilityLimited=" + visibilityLimited +
        ", visibleForUsers='" + visibleForUsers + '\'' +
        ", sendAlarmEmail=" + sendAlarmEmail +
        ", emailsToReceiveAlarm='" + emailsToReceiveAlarm + '\'' +
        ", sendAlarmSMS=" + sendAlarmSMS +
        ", phoneNumbersToReceiveAlarmSMS='" + phoneNumbersToReceiveAlarmSMS + '\'' +
        ", keepKiraClientConfigDataOnKiraServerUnchanged="
        + keepKiraClientConfigDataOnKiraServerUnchanged +
        ", otherDataMap=" + otherDataMap +
        ", createTime=" + createTime +
        '}';
  }
}
