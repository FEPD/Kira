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

public class TriggerMetadataUpdateContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String targetAppId;

  private String targetTriggerId;

  private String targetMethod;

  private String targetMethodArgTypes;

  private String triggerType;

  private String startTimeAsString;

  private String endTimeAsString;

  private Long startDelay;

  private Integer repeatCount;

  private Long repeatInterval;

  private String cronExpression;

  private String description;

  private Integer misfireInstruction;

  private Boolean asynchronous;

  private Boolean onlyRunOnSingleProcess;

  private Boolean limitToSpecifiedLocations;

  private String locationsToRunJob;

  private Boolean disabled;

  private String argumentsAsJsonArrayString;

  private Boolean concurrent;

  private Long runTimeThreshold;

  private Integer priority;

  private Boolean requestsRecovery;

  private Boolean copyFromMasterToSlaveZone;

  private Boolean onlyScheduledInMasterZone;

  private Boolean jobDispatchTimeoutEnabled = Boolean.FALSE;

  private Long jobDispatchTimeout;

  public TriggerMetadataUpdateContent() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  public String getStartTimeAsString() {
    return startTimeAsString;
  }

  public void setStartTimeAsString(String startTimeAsString) {
    this.startTimeAsString = startTimeAsString;
  }

  public String getEndTimeAsString() {
    return endTimeAsString;
  }

  public void setEndTimeAsString(String endTimeAsString) {
    this.endTimeAsString = endTimeAsString;
  }

  public Long getStartDelay() {
    return startDelay;
  }

  public void setStartDelay(Long startDelay) {
    this.startDelay = startDelay;
  }

  public Integer getRepeatCount() {
    return repeatCount;
  }

  public void setRepeatCount(Integer repeatCount) {
    this.repeatCount = repeatCount;
  }

  public Long getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatInterval(Long repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getMisfireInstruction() {
    return misfireInstruction;
  }

  public void setMisfireInstruction(Integer misfireInstruction) {
    this.misfireInstruction = misfireInstruction;
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

  public Boolean getLimitToSpecifiedLocations() {
    return limitToSpecifiedLocations;
  }

  public void setLimitToSpecifiedLocations(Boolean limitToSpecifiedLocations) {
    this.limitToSpecifiedLocations = limitToSpecifiedLocations;
  }

  public String getLocationsToRunJob() {
    return locationsToRunJob;
  }

  public void setLocationsToRunJob(String locationsToRunJob) {
    this.locationsToRunJob = locationsToRunJob;
  }

  public Boolean getDisabled() {
    return disabled;
  }

  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
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

  public Long getRunTimeThreshold() {
    return runTimeThreshold;
  }

  public void setRunTimeThreshold(Long runTimeThreshold) {
    this.runTimeThreshold = runTimeThreshold;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
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

  public Boolean getJobDispatchTimeoutEnabled() {
    return this.jobDispatchTimeoutEnabled;
  }

  public void setJobDispatchTimeoutEnabled(Boolean jobDispatchTimeoutEnabled) {
    this.jobDispatchTimeoutEnabled = jobDispatchTimeoutEnabled;
  }

  public Long getJobDispatchTimeout() {
    return this.jobDispatchTimeout;
  }

  public void setJobDispatchTimeout(Long jobDispatchTimeout) {
    this.jobDispatchTimeout = jobDispatchTimeout;
  }

}
