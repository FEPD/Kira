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

public class JobCancelRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;
  private String triggerId;
  private String sponsorPoolId;
  private String sponsorTriggerId;
  private Map<Serializable, Serializable> methodParamDataMap = new LinkedHashMap<Serializable, Serializable>();
  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  private Date createTime = new Date();

  public JobCancelRequest() {
    // TODO Auto-generated constructor stub
  }

  public JobCancelRequest(String appId, String triggerId, Date createTime) {
    super();
    this.appId = appId;
    this.triggerId = triggerId;
    this.createTime = createTime;
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

  public Map<Serializable, Serializable> getMethodParamDataMap() {
    return methodParamDataMap;
  }

  public void setMethodParamDataMap(
      Map<Serializable, Serializable> methodParamDataMap) {
    this.methodParamDataMap = methodParamDataMap;
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
