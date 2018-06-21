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
package com.yihaodian.architecture.kira.common;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class TriggerMetadataDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;

  private String triggerId;

  private String version;

  private Integer priority;

  private String targetAppId;

  private String targetTriggerId;

  private String targetMethod;

  private String targetMethodArgTypes;

  private String argumentsAsJsonArrayString;

  private Boolean concurrent = Boolean.TRUE;

  private String triggerType;

  private Date startTime;

  private Date endTime;

  private Long startDelay;

  private Integer repeatCount;

  private Long repeatInterval;

  private String cronExpression;

  private String description;

  private Integer misfireInstruction;

  private Boolean asynchronous;

  private Boolean onlyRunOnSingleProcess;

  private String locationsToRunJob;

  private Boolean limitToSpecifiedLocations = Boolean.FALSE;

  private Boolean scheduledLocally = Boolean.FALSE;

  private Boolean disabled = Boolean.FALSE;

  private Boolean requestsRecovery = Boolean.FALSE;

  private Long runTimeThreshold;

  private Boolean copyFromMasterToSlaveZone = Boolean.FALSE;

  private Boolean onlyScheduledInMasterZone = Boolean.TRUE;

  private Boolean jobDispatchTimeoutEnabled = Boolean.FALSE;

  private Long jobDispatchTimeout;

  private String comments;

  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  private String jobType;

  private String runShellPath;

  public TriggerMetadataDetail() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setDefaultValuesIfNeeded() {
    if (StringUtils.isBlank(this.version)) {
      this.version = "0";
    }
    if (null == this.priority) {
      this.priority = KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER;
    }
    if (StringUtils.isBlank(this.argumentsAsJsonArrayString)) {
      this.argumentsAsJsonArrayString = "[]";
    }

    if (TriggerTypeEnum.isSimpleTrigger(this.triggerType)) {
      if (null == this.startDelay) {
        this.startDelay = Long.valueOf(0);
      }
      if (null == this.repeatCount) {
        this.repeatCount = Integer.valueOf(0);
      }
    }
    if (null == this.misfireInstruction) {
      this.misfireInstruction = Integer.valueOf(0);
    }
    if (null == this.asynchronous) {
      this.asynchronous = Boolean.valueOf(true);
    }
    if (null == this.onlyRunOnSingleProcess) {
      this.onlyRunOnSingleProcess = Boolean.valueOf(true);
    }
    if (null == this.disabled) {
      this.disabled = Boolean.valueOf(false);
    }
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

  public Integer getPriority() {
    if (null == priority) {
      this.setPriority(Integer.valueOf(KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER));
    }
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
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

  public String getArgumentsAsJsonArrayString() {
    return argumentsAsJsonArrayString;
  }

  public void setArgumentsAsJsonArrayString(String argumentsAsJsonArrayString) {
    this.argumentsAsJsonArrayString = argumentsAsJsonArrayString;
  }

  public Boolean getConcurrent() {
    if (null == concurrent) {
      this.setConcurrent(Boolean.TRUE);
    }
    return concurrent;
  }

  public void setConcurrent(Boolean concurrent) {
    this.concurrent = concurrent;
  }

  public String getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getStartTimeAsString() {
    return KiraCommonUtils.getDateAsString(startTime);
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getEndTimeAsString() {
    return KiraCommonUtils.getDateAsString(endTime);
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

  public String getLocationsToRunJob() {
    return locationsToRunJob;
  }

  public void setLocationsToRunJob(String locationsToRunJob) {
    this.locationsToRunJob = locationsToRunJob;
  }

  public Boolean getLimitToSpecifiedLocations() {
    if (null == limitToSpecifiedLocations) {
      setLimitToSpecifiedLocations(Boolean.FALSE);
    }

    return limitToSpecifiedLocations;
  }

  public void setLimitToSpecifiedLocations(
      Boolean limitToSpecifiedLocations) {
    this.limitToSpecifiedLocations = limitToSpecifiedLocations;
  }

  public Boolean getScheduledLocally() {
    if (null == scheduledLocally) {
      setScheduledLocally(Boolean.FALSE);
    }
    return scheduledLocally;
  }

  public void setScheduledLocally(Boolean scheduledLocally) {
    this.scheduledLocally = scheduledLocally;
  }

  public Boolean getDisabled() {
    if (null == disabled) {
      this.setDisabled(Boolean.FALSE);
    }
    return disabled;
  }

  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
  }

  public Boolean getRequestsRecovery() {
    if (null == requestsRecovery) {
      this.setRequestsRecovery(Boolean.FALSE);
    }
    return requestsRecovery;
  }

  public void setRequestsRecovery(Boolean requestsRecovery) {
    this.requestsRecovery = requestsRecovery;
  }

  public Long getRunTimeThreshold() {
    return runTimeThreshold;
  }

  public void setRunTimeThreshold(Long runTimeThreshold) {
    this.runTimeThreshold = runTimeThreshold;
  }

  public Boolean getCopyFromMasterToSlaveZone() {
    if (null == copyFromMasterToSlaveZone) {
      this.setCopyFromMasterToSlaveZone(Boolean.FALSE);
    }
    return copyFromMasterToSlaveZone;
  }

  public void setCopyFromMasterToSlaveZone(Boolean copyFromMasterToSlaveZone) {
    this.copyFromMasterToSlaveZone = copyFromMasterToSlaveZone;
  }

  public Boolean getOnlyScheduledInMasterZone() {
    if (null == onlyScheduledInMasterZone) {
      this.setOnlyScheduledInMasterZone(Boolean.TRUE);
    }
    return onlyScheduledInMasterZone;
  }

  public void setOnlyScheduledInMasterZone(Boolean onlyScheduledInMasterZone) {
    this.onlyScheduledInMasterZone = onlyScheduledInMasterZone;
  }

  public Map<Serializable, Serializable> getOtherDataMap() {
    return otherDataMap;
  }

  public void setOtherDataMap(
      Map<Serializable, Serializable> otherDataMap) {
    this.otherDataMap = otherDataMap;
  }

  public Boolean getJobDispatchTimeoutEnabled() {
    if (this.jobDispatchTimeoutEnabled == null) {
      this.setJobDispatchTimeoutEnabled(Boolean.FALSE);
    }
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

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public String getRunShellPath() {
    return runShellPath;
  }

  public void setRunShellPath(String runShellPath) {
    this.runShellPath = runShellPath;
  }

}
