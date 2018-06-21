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

import java.util.List;

public class KiraClientMetadataCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private Boolean manuallyCreated;

  private List<String> poolIdList;

  private Boolean visibilityLimited;

  private String userNameWhichIsNotInVisibleUsers;

  private List<String> poolIdListToExclude;

  public KiraClientMetadataCriteria() {
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

  public List<String> getPoolIdList() {
    return poolIdList;
  }

  public void setPoolIdList(List<String> poolIdList) {
    this.poolIdList = poolIdList;
  }

  public Boolean getVisibilityLimited() {
    return visibilityLimited;
  }

  public void setVisibilityLimited(Boolean visibilityLimited) {
    this.visibilityLimited = visibilityLimited;
  }

  public String getUserNameWhichIsNotInVisibleUsers() {
    return userNameWhichIsNotInVisibleUsers;
  }

  public void setUserNameWhichIsNotInVisibleUsers(
      String userNameWhichIsNotInVisibleUsers) {
    this.userNameWhichIsNotInVisibleUsers = userNameWhichIsNotInVisibleUsers;
  }

  public List<String> getPoolIdListToExclude() {
    return poolIdListToExclude;
  }

  public void setPoolIdListToExclude(List<String> poolIdListToExclude) {
    this.poolIdListToExclude = poolIdListToExclude;
  }
}
