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
package com.yihaodian.architecture.kira.client.util;

import com.yihaodian.architecture.kira.client.internal.KiraClientInternalFacade;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class KiraClientConfig implements InitializingBean {

  private static Logger logger = LoggerFactory.getLogger(KiraClientConfig.class);

  private boolean appCenter;

  private boolean workWithoutKira;

  private long waitForResourceTimeoutMillisecond = KiraClientConstants.WAITFOR_RESOURCE_TIMEOUT_MILLISECOND;

  private boolean autoDeleteTriggersOnZK = true;

  private String locationsToRunJobForAllTriggers;

  //Below properties will be reported to kira server and may be dynamically changed on kira server.
  private boolean visibilityLimited;
  private String visibleForUsers;
  private boolean sendAlarmEmail;
  private String emailsToReceiveAlarm;
  private boolean sendAlarmSMS;
  private String phoneNumbersToReceiveAlarmSMS;
  private boolean keepKiraClientConfigDataOnKiraServerUnchanged;

  public KiraClientConfig() {
    KiraClientDataCenter.setKiraClientConfig(this);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public boolean isAppCenter() {
    return appCenter;
  }

  public void setAppCenter(boolean appCenter) {
    this.appCenter = appCenter;
  }

  public boolean isWorkWithoutKira() {
    return workWithoutKira;
  }

  public void setWorkWithoutKira(boolean workWithoutKira) {
    this.workWithoutKira = workWithoutKira;
  }

  public long getWaitForResourceTimeoutMillisecond() {
    return waitForResourceTimeoutMillisecond;
  }

  /**
   * The timeout in milliseconds which will be used when waiting for necessary resources to be
   * ready. The resources: For example CentralScheduleService , etc. After the timeout exceed,
   * exception will be thrown out. The default value is 120000 which means 2 minutes.
   */
  public void setWaitForResourceTimeoutMillisecond(
      long waitForResourceTimeoutMillisecond) {
    this.waitForResourceTimeoutMillisecond = waitForResourceTimeoutMillisecond;
  }

  public boolean isAutoDeleteTriggersOnZK() {
    return autoDeleteTriggersOnZK;
  }

  public void setAutoDeleteTriggersOnZK(boolean autoDeleteTriggersOnZK) {
    this.autoDeleteTriggersOnZK = autoDeleteTriggersOnZK;
  }

  public String getLocationsToRunJobForAllTriggers() {
    return locationsToRunJobForAllTriggers;
  }

  /**
   * locations which are seperated by , e.g. ip1:port1,ip2:port2,ip3:port3
   */
  public void setLocationsToRunJobForAllTriggers(
      String locationsToRunJobForAllTriggers) {
    this.locationsToRunJobForAllTriggers = locationsToRunJobForAllTriggers;
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

  /**
   * domain usernames which are seperated by , e.g. domainUserName1,domainUserName2,domainUserName3
   */
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

  /**
   * emails to receive alarm message. which are seperated by , e.g. aa@yihaodian.com,bb@yihaodian.com,cc@yihaodian.com
   */
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

  /**
   * PhoneNumbers to receive alarm SMS. which are seperated by , e.g.
   * 13688888888,13888888888,13988888888
   */
  public void setPhoneNumbersToReceiveAlarmSMS(
      String phoneNumbersToReceiveAlarmSMS) {
    this.phoneNumbersToReceiveAlarmSMS = phoneNumbersToReceiveAlarmSMS;
  }

  public boolean isKeepKiraClientConfigDataOnKiraServerUnchanged() {
    return keepKiraClientConfigDataOnKiraServerUnchanged;
  }

  /**
   * Set if need to keep KeepKiraClientConfigData on kiraServer unchanged every time pool
   * restarted.
   */
  public void setKeepKiraClientConfigDataOnKiraServerUnchanged(
      boolean keepKiraClientConfigDataOnKiraServerUnchanged) {
    this.keepKiraClientConfigDataOnKiraServerUnchanged = keepKiraClientConfigDataOnKiraServerUnchanged;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!workWithoutKira) {
      KiraClientInternalFacade.getKiraClientInternalFacade().handleForKiraClientConfig(this);
    }
  }

}
