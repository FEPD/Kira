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

public class KiraTimerTriggerBusinessRunningInstance implements Serializable {

  private static final long serialVersionUID = 1L;
  protected String serviceUrl;
  private String host;
  private Integer port;
  private Integer pid;
  private Date createTime;

  private String jobItemId;
  private String jobId;
  private String appId;
  private String triggerId;
  private String version;
  private String sponsorPoolId;
  private String sponsorTriggerId;
  private String argumentsAsJsonArrayString;

  private String zkPath;

  public KiraTimerTriggerBusinessRunningInstance() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

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

  public Integer getPid() {
    return pid;
  }

  public void setPid(Integer pid) {
    this.pid = pid;
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

  public String getArgumentsAsJsonArrayString() {
    return argumentsAsJsonArrayString;
  }

  public void setArgumentsAsJsonArrayString(String argumentsAsJsonArrayString) {
    this.argumentsAsJsonArrayString = argumentsAsJsonArrayString;
  }

  public String getZkPath() {
    return zkPath;
  }

  public void setZkPath(String zkPath) {
    this.zkPath = zkPath;
  }

  @Override
  public String toString() {
    return "KiraTimerTriggerBusinessRunningInstance [host=" + host
        + ", port=" + port + ", serviceUrl=" + serviceUrl + ", pid="
        + pid + ", createTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(createTime)
        + ", jobItemId=" + jobItemId
        + ", jobId=" + jobId + ", appId=" + appId + ", triggerId="
        + triggerId + ", version=" + version + ", sponsorPoolId="
        + sponsorPoolId + ", sponsorTriggerId=" + sponsorTriggerId
        + ", argumentsAsJsonArrayString=" + argumentsAsJsonArrayString
        + ", zkPath=" + zkPath + "]";
  }

}
