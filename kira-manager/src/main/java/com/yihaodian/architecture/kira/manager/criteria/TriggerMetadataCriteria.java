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


public class TriggerMetadataCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String appId;

  private String triggerId;

  private String version;

  private Boolean manuallyCreated;

  private Boolean unregistered = Boolean.FALSE;

  private Boolean deleted = Boolean.FALSE;

  private String triggerEnvironmentZKFullPath;

  private String triggerType;

  private List<String> poolIdListToExclude;

  private Boolean canBeScheduled;

  private Boolean requestsRecovery;

  private Boolean copyFromMasterToSlaveZone;

  private Boolean onlyScheduledInMasterZone;

  public TriggerMetadataCriteria() {
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

  public Boolean getManuallyCreated() {
    return manuallyCreated;
  }

  public void setManuallyCreated(Boolean manuallyCreated) {
    this.manuallyCreated = manuallyCreated;
  }

  public Boolean getUnregistered() {
    return unregistered;
  }

  public void setUnregistered(Boolean unregistered) {
    this.unregistered = unregistered;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public String getTriggerEnvironmentZKFullPath() {
    return triggerEnvironmentZKFullPath;
  }

  public void setTriggerEnvironmentZKFullPath(String triggerEnvironmentZKFullPath) {
    this.triggerEnvironmentZKFullPath = triggerEnvironmentZKFullPath;
  }

  public String getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  public List<String> getPoolIdListToExclude() {
    return poolIdListToExclude;
  }

  public void setPoolIdListToExclude(List<String> poolIdListToExclude) {
    this.poolIdListToExclude = poolIdListToExclude;
  }

  public Boolean getCanBeScheduled() {
    return canBeScheduled;
  }

  public void setCanBeScheduled(Boolean canBeScheduled) {
    this.canBeScheduled = canBeScheduled;
  }

  public Boolean getRequestsRecovery() {
    return requestsRecovery;
  }

  public void setRequestsRecovery(Boolean requestsRecovery) {
    this.requestsRecovery = requestsRecovery;
  }

  public Boolean getCopyFromMasterToSlaveZone() {
    return copyFromMasterToSlaveZone;
  }

  public void setCopyFromMasterToSlaveZone(Boolean copyFromMasterToSlaveZone) {
    this.copyFromMasterToSlaveZone = copyFromMasterToSlaveZone;
  }

  public Boolean getOnlyScheduledInMasterZone() {
    return onlyScheduledInMasterZone;
  }

  public void setOnlyScheduledInMasterZone(Boolean onlyScheduledInMasterZone) {
    this.onlyScheduledInMasterZone = onlyScheduledInMasterZone;
  }

}
