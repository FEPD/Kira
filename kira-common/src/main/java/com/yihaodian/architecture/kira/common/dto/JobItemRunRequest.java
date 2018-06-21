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

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobItemRunRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String jobItemId;
  private String jobId;
  private String appId;
  private String triggerId;
  private String version;
  private String targetMethod;
  private String targetMethodArgTypes;
  private String argumentsAsJsonArrayString;
  private Boolean concurrent = Boolean.TRUE;
  private Boolean asynchronous;
  private Boolean onlyRunOnSingleProcess;
  private String sponsorPoolId;
  private String sponsorTriggerId;
  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  private Date createTime = new Date();

  public JobItemRunRequest() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getJobItemId() {
    return jobItemId;
  }

  public void setJobItemId(String jobItemId) {
    this.jobItemId = jobItemId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getTargetMethod() {
    return targetMethod;
  }

  public void setTargetMethod(String targetMethod) {
    this.targetMethod = targetMethod;
  }

  public String getTargetMethodArgTypes() {
    return targetMethodArgTypes;
  }

  public void setTargetMethodArgTypes(String targetMethodArgTypes) {
    this.targetMethodArgTypes = targetMethodArgTypes;
  }

  public String getArgumentsAsJsonArrayString() {
    return argumentsAsJsonArrayString;
  }

  public void setArgumentsAsJsonArrayString(String argumentsAsJsonArrayString) {
    this.argumentsAsJsonArrayString = argumentsAsJsonArrayString;
  }

  public Boolean getConcurrent() {
    return concurrent;
  }

  public void setConcurrent(Boolean concurrent) {
    this.concurrent = concurrent;
  }

  public Boolean getAsynchronous() {
    return asynchronous;
  }

  public void setAsynchronous(Boolean asynchronous) {
    this.asynchronous = asynchronous;
  }

  public Boolean getOnlyRunOnSingleProcess() {
    return onlyRunOnSingleProcess;
  }

  public void setOnlyRunOnSingleProcess(Boolean onlyRunOnSingleProcess) {
    this.onlyRunOnSingleProcess = onlyRunOnSingleProcess;
  }

  public String getSponsorPoolId() {
    return sponsorPoolId;
  }

  public void setSponsorPoolId(String sponsorPoolId) {
    this.sponsorPoolId = sponsorPoolId;
  }

  public String getSponsorTriggerId() {
    return sponsorTriggerId;
  }

  public void setSponsorTriggerId(String sponsorTriggerId) {
    this.sponsorTriggerId = sponsorTriggerId;
  }

  public Map<Serializable, Serializable> getOtherDataMap() {
    return otherDataMap;
  }

  public void setOtherDataMap(
      Map<Serializable, Serializable> otherDataMap) {
    this.otherDataMap = otherDataMap;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

}
