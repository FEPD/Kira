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
package com.yihaodian.architecture.kira.manager.criteria;

import com.yihaodian.architecture.kira.manager.util.Paging;

public class OtherCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String adminUserNames;

  private boolean needArchiveJobRuntimeData;

  private int minutesToKeepJobRuntimeData;

  private int minutesPerTimeToHandleJobRuntimeData;

  private long sleepTimeBeforeRunNextTaskInMilliseconds;

  private long timeOutPerTaskInMilliseconds;

  private String privateEmails;

  private String privatePhoneNumbers;

  private String adminEmails;

  private String adminPhoneNumbers;

  private String healthEventReceiverEmails;

  private String healthEventReceiverPhoneNumbers;

  private boolean useSendInnerEmailJar;

  private String znodeFullPath;

  private int jobTimeoutHandleFailedMaxCount;

  public OtherCriteria() {
    // TODO Auto-generated constructor stub
  }

  public OtherCriteria(int maxResults) {
    super(maxResults);
  }

  public OtherCriteria(Paging paging) {
    super(paging);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getAdminUserNames() {
    return adminUserNames;
  }

  public void setAdminUserNames(String adminUserNames) {
    this.adminUserNames = adminUserNames;
  }

  public boolean isNeedArchiveJobRuntimeData() {
    return needArchiveJobRuntimeData;
  }

  public void setNeedArchiveJobRuntimeData(boolean needArchiveJobRuntimeData) {
    this.needArchiveJobRuntimeData = needArchiveJobRuntimeData;
  }

  public int getMinutesToKeepJobRuntimeData() {
    return minutesToKeepJobRuntimeData;
  }

  public void setMinutesToKeepJobRuntimeData(int minutesToKeepJobRuntimeData) {
    this.minutesToKeepJobRuntimeData = minutesToKeepJobRuntimeData;
  }

  public int getMinutesPerTimeToHandleJobRuntimeData() {
    return minutesPerTimeToHandleJobRuntimeData;
  }

  public void setMinutesPerTimeToHandleJobRuntimeData(
      int minutesPerTimeToHandleJobRuntimeData) {
    this.minutesPerTimeToHandleJobRuntimeData = minutesPerTimeToHandleJobRuntimeData;
  }

  public long getSleepTimeBeforeRunNextTaskInMilliseconds() {
    return sleepTimeBeforeRunNextTaskInMilliseconds;
  }

  public void setSleepTimeBeforeRunNextTaskInMilliseconds(
      long sleepTimeBeforeRunNextTaskInMilliseconds) {
    this.sleepTimeBeforeRunNextTaskInMilliseconds = sleepTimeBeforeRunNextTaskInMilliseconds;
  }

  public long getTimeOutPerTaskInMilliseconds() {
    return timeOutPerTaskInMilliseconds;
  }

  public void setTimeOutPerTaskInMilliseconds(long timeOutPerTaskInMilliseconds) {
    this.timeOutPerTaskInMilliseconds = timeOutPerTaskInMilliseconds;
  }

  public String getPrivateEmails() {
    return privateEmails;
  }

  public void setPrivateEmails(String privateEmails) {
    this.privateEmails = privateEmails;
  }

  public String getPrivatePhoneNumbers() {
    return privatePhoneNumbers;
  }

  public void setPrivatePhoneNumbers(String privatePhoneNumbers) {
    this.privatePhoneNumbers = privatePhoneNumbers;
  }

  public String getAdminEmails() {
    return adminEmails;
  }

  public void setAdminEmails(String adminEmails) {
    this.adminEmails = adminEmails;
  }

  public String getAdminPhoneNumbers() {
    return adminPhoneNumbers;
  }

  public void setAdminPhoneNumbers(String adminPhoneNumbers) {
    this.adminPhoneNumbers = adminPhoneNumbers;
  }

  public boolean isUseSendInnerEmailJar() {
    return useSendInnerEmailJar;
  }

  public void setUseSendInnerEmailJar(boolean useSendInnerEmailJar) {
    this.useSendInnerEmailJar = useSendInnerEmailJar;
  }

  public String getHealthEventReceiverEmails() {
    return healthEventReceiverEmails;
  }

  public void setHealthEventReceiverEmails(String healthEventReceiverEmails) {
    this.healthEventReceiverEmails = healthEventReceiverEmails;
  }

  public String getHealthEventReceiverPhoneNumbers() {
    return healthEventReceiverPhoneNumbers;
  }

  public void setHealthEventReceiverPhoneNumbers(
      String healthEventReceiverPhoneNumbers) {
    this.healthEventReceiverPhoneNumbers = healthEventReceiverPhoneNumbers;
  }

  public String getZnodeFullPath() {
    return znodeFullPath;
  }

  public void setZnodeFullPath(String znodeFullPath) {
    this.znodeFullPath = znodeFullPath;
  }

  public int getJobTimeoutHandleFailedMaxCount() {
    return jobTimeoutHandleFailedMaxCount;
  }

  public void setJobTimeoutHandleFailedMaxCount(int jobTimeoutHandleFailedMaxCount) {
    this.jobTimeoutHandleFailedMaxCount = jobTimeoutHandleFailedMaxCount;
  }

}
