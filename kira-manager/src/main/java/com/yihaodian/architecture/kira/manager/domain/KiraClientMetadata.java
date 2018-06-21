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
package com.yihaodian.architecture.kira.manager.domain;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;

public class KiraClientMetadata implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private Boolean manuallyCreated = Boolean.FALSE;

  private String manuallyCreatedBy;

  private String manuallyCreatedDetail;

  private String kiraClientVersion;

  private Boolean visibilityLimited = Boolean.FALSE;

  private String visibleForUsers;

  private Boolean sendAlarmEmail = Boolean.FALSE;

  private String emailsToReceiveAlarm;

  private Boolean sendAlarmSMS = Boolean.FALSE;

  private String phoneNumbersToReceiveAlarmSMS;

  private Date createTime;

  private Date lastRegisterTime;

  private String lastRegisterDetail;

  private Date lastManuallyUpdateTime;

  private String lastManuallyUpdateBy;

  public KiraClientMetadata() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Boolean getManuallyCreated() {
    return manuallyCreated;
  }

  public void setManuallyCreated(Boolean manuallyCreated) {
    this.manuallyCreated = manuallyCreated;
  }

  public String getManuallyCreatedBy() {
    return manuallyCreatedBy;
  }

  public void setManuallyCreatedBy(String manuallyCreatedBy) {
    this.manuallyCreatedBy = manuallyCreatedBy;
  }

  public String getManuallyCreatedDetail() {
    return manuallyCreatedDetail;
  }

  public void setManuallyCreatedDetail(String manuallyCreatedDetail) {
    this.manuallyCreatedDetail = manuallyCreatedDetail;
  }

  public String getKiraClientVersion() {
    return kiraClientVersion;
  }

  public void setKiraClientVersion(String kiraClientVersion) {
    this.kiraClientVersion = kiraClientVersion;
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
  }

  public Date getLastRegisterTime() {
    return lastRegisterTime;
  }

  public void setLastRegisterTime(Date lastRegisterTime) {
    this.lastRegisterTime = lastRegisterTime;
  }

  public String getLastRegisterTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastRegisterTime);
  }

  public String getLastRegisterDetail() {
    return lastRegisterDetail;
  }

  public void setLastRegisterDetail(String lastRegisterDetail) {
    this.lastRegisterDetail = lastRegisterDetail;
  }

  public Date getLastManuallyUpdateTime() {
    return lastManuallyUpdateTime;
  }

  public void setLastManuallyUpdateTime(Date lastManuallyUpdateTime) {
    this.lastManuallyUpdateTime = lastManuallyUpdateTime;
  }

  public String getLastManuallyUpdateTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastManuallyUpdateTime);
  }

  public String getLastManuallyUpdateBy() {
    return lastManuallyUpdateBy;
  }

  public void setLastManuallyUpdateBy(String lastManuallyUpdateBy) {
    this.lastManuallyUpdateBy = lastManuallyUpdateBy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KiraClientMetadata other = (KiraClientMetadata) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "id=" + "'" + id + "'" +
        ")";
  }

}
