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

public class TriggerEnvironmentDetailData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;
  private String triggerId;
  private Boolean manuallyCreated;
  private String targetAppId;
  private String targetTriggerId;
  private String triggerEnvironmentZKFullPath;
  private String centralScheduleServiceZKParentPath;

  private String host;
  private Integer port;
  private String serviceUrl;
  private Boolean serviceAvailable;

  public TriggerEnvironmentDetailData() {
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

  public Boolean getManuallyCreated() {
    return manuallyCreated;
  }

  public void setManuallyCreated(Boolean manuallyCreated) {
    this.manuallyCreated = manuallyCreated;
  }

  public String getTargetAppId() {
    return targetAppId;
  }

  public void setTargetAppId(String targetAppId) {
    this.targetAppId = targetAppId;
  }

  public String getTargetTriggerId() {
    return targetTriggerId;
  }

  public void setTargetTriggerId(String targetTriggerId) {
    this.targetTriggerId = targetTriggerId;
  }

  public String getTriggerEnvironmentZKFullPath() {
    return triggerEnvironmentZKFullPath;
  }

  public void setTriggerEnvironmentZKFullPath(String triggerEnvironmentZKFullPath) {
    this.triggerEnvironmentZKFullPath = triggerEnvironmentZKFullPath;
  }

  public String getCentralScheduleServiceZKParentPath() {
    return centralScheduleServiceZKParentPath;
  }

  public void setCentralScheduleServiceZKParentPath(
      String centralScheduleServiceZKParentPath) {
    this.centralScheduleServiceZKParentPath = centralScheduleServiceZKParentPath;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public Boolean getServiceAvailable() {
    return serviceAvailable;
  }

  public void setServiceAvailable(Boolean serviceAvailable) {
    this.serviceAvailable = serviceAvailable;
  }

  @Override
  public String toString() {
    return "TriggerEnvironmentDetailData [appId=" + appId
        + ", triggerId=" + triggerId + ", manuallyCreated="
        + manuallyCreated + ", targetAppId=" + targetAppId
        + ", targetTriggerId=" + targetTriggerId
        + ", triggerEnvironmentZKFullPath="
        + triggerEnvironmentZKFullPath
        + ", centralScheduleServiceZKParentPath="
        + centralScheduleServiceZKParentPath + ", host=" + host
        + ", port=" + port + ", serviceUrl=" + serviceUrl
        + ", serviceAvailable=" + serviceAvailable + "]";
  }

}
