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
package com.yihaodian.architecture.kira.client.quartz;

import com.yihaodian.architecture.kira.client.internal.iface.IYHDCronTriggerBean;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import org.springframework.scheduling.quartz.CronTriggerBean;

public class YHDCronTriggerBean extends CronTriggerBean implements IYHDCronTriggerBean {

  private static final long serialVersionUID = 4576700838128915902L;

  private String version;
  private Integer prioritySet;
  private boolean asynchronous = true;
  private boolean onlyRunOnSingleProcess = true;
  private String locationsToRunJob;
  private boolean limitToSpecifiedLocations;
  private boolean scheduledLocally;
  private boolean disabled;
  private boolean requestsRecovery;
  private String targetAppId;
  private String targetTriggerId;

  private volatile boolean concurrent = true;

  private Date startTimeSet;
  private Date endTimeSet;

  private Long runTimeThreshold;

  //Cross multi-Zone stuff
  private boolean copyFromMasterToSlaveZone;
  private boolean onlyScheduledInMasterZone = true;

  private String beanName;

  //Job dispatch time out feature support
  private boolean jobDispatchTimeoutEnabled;
  private Long jobDispatchTimeout;

  //The start time and end time will be called for many times. So use below to make sure the startTimeSet and endTimeSet are the value set by spring property injection.
  private transient CountDownLatch startTimeSpringPropertyInjectionSignal = new CountDownLatch(1);
  private transient CountDownLatch endTimeSpringPropertyInjectionSignal = new CountDownLatch(1);

  private String jobType;

  private String runShellPath;

  public YHDCronTriggerBean() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  ;

  @Override
  public void setPriority(int priority) {
    super.setPriority(priority);
    this.prioritySet = Integer.valueOf(priority);
  }

  @Override
  public Integer getPrioritySet() {
    return prioritySet;
  }

  @Override
  public String getDescription() {
    String description = super.getDescription();
    return description;
  }

  @Override
  public boolean isAsynchronous() {
    return asynchronous;
  }

  public void setAsynchronous(boolean asynchronous) {
    this.asynchronous = asynchronous;
  }

  @Override
  public boolean isOnlyRunOnSingleProcess() {
    return onlyRunOnSingleProcess;
  }

  /**
   * 触发时是否只派送到一个执行地点执行
   */
  public void setOnlyRunOnSingleProcess(boolean onlyRunOnSingleProcess) {
    this.onlyRunOnSingleProcess = onlyRunOnSingleProcess;
  }

  @Override
  public String getLocationsToRunJob() {
    return locationsToRunJob;
  }

  /**
   * locations which are seperated by , e.g. ip1:port1,ip2:port2,ip3:port3
   *
   * Or you can set this value to special string empty to make locationsToRunJob be empty to ignore
   * the value of locationsToRunJobForAllTriggers and locationsToRunJobForAllTriggersOfThisScheduler.
   */
  public void setLocationsToRunJob(String locationsToRunJob) {
    this.locationsToRunJob = locationsToRunJob;
  }

  @Override
  public boolean isLimitToSpecifiedLocations() {
    return limitToSpecifiedLocations;
  }

  /**
   * Set if limit to run job on the specified locations. If locationsToRunJob is null, this
   * parameter makes no sense.
   */
  public void setLimitToSpecifiedLocations(
      boolean limitToSpecifiedLocations) {
    this.limitToSpecifiedLocations = limitToSpecifiedLocations;
  }

  @Override
  public boolean isScheduledLocally() {
    return scheduledLocally;
  }

  public void setScheduledLocally(boolean scheduledLocally) {
    this.scheduledLocally = scheduledLocally;
  }

  @Override
  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  @Override
  public boolean isRequestsRecovery() {
    return requestsRecovery;
  }

  /**
   * Set whether or not the the kira server should re-execute the job of this trigger if crash
   * occurs on kira server side when firing this job of this trigger. The default value is false
   */
  public void setRequestsRecovery(boolean requestsRecovery) {
    this.requestsRecovery = requestsRecovery;
  }

  @Override
  public String getTargetAppId() {
    return targetAppId;
  }

  /**
   * 设置将被调用的服务方法所在的appId,如果为空则表示调用自己appId暴露的服务方法。
   */
  public void setTargetAppId(String targetAppId) {
    this.targetAppId = targetAppId;
  }

  @Override
  public String getTargetTriggerId() {
    return targetTriggerId;
  }

  /**
   * 设置将被调用的服务方法所在的targetTriggerId,如果为空则表示调用自己pool的此定时任务暴露的服务方法。
   */
  public void setTargetTriggerId(String targetTriggerId) {
    this.targetTriggerId = targetTriggerId;
  }

  @Override
  public boolean isConcurrent() {
    return concurrent;
  }

  /**
   * 是否允许业务方法并发执行。默认为true，即不管现在是否有此定时任务对应的业务方法在执行，到了下次触发时间点都会触发执行此定时任务的业务方法。
   */
  public void setConcurrent(boolean concurrent) {
    this.concurrent = concurrent;
  }

  @Override
  public void setStartTime(Date startTime) {
    super.setStartTime(startTime);
    if (null != startTimeSpringPropertyInjectionSignal) {
      //need to add null check for it is called by default construtor of super class in that moment it is null.
      if (startTimeSpringPropertyInjectionSignal.getCount() > 0) {
        startTimeSpringPropertyInjectionSignal.countDown();
        this.startTimeSet = startTime;
      }
    }
  }

  @Override
  public Date getStartTimeSet() {
    return startTimeSet;
  }

  @Override
  public Date getEndTimeSet() {
    return endTimeSet;
  }

  @Override
  public void setEndTime(Date endTime) {
    super.setEndTime(endTime);
    if (endTimeSpringPropertyInjectionSignal.getCount() > 0) {
      endTimeSpringPropertyInjectionSignal.countDown();
      this.endTimeSet = endTime;
    }
  }

  @Override
  public Long getRunTimeThreshold() {
    return runTimeThreshold;
  }

  /**
   * 设置此定时任务的运行时间阀值，如果运行的任务超过此值，则平台将根据此阀值发送相关超时报警提醒。 如果为空则表示不进行此类报警。 单位：毫秒
   */
  public void setRunTimeThreshold(Long runTimeThreshold) {
    this.runTimeThreshold = runTimeThreshold;
  }

  @Override
  public boolean isCopyFromMasterToSlaveZone() {
    return copyFromMasterToSlaveZone;
  }

  /**
   * 设置此定时任务的配置信息是否从MasterZone到SlaveZone进行复制。 默认为false，即不进行复制。
   */
  public void setCopyFromMasterToSlaveZone(boolean copyFromMasterToSlaveZone) {
    this.copyFromMasterToSlaveZone = copyFromMasterToSlaveZone;
  }

  @Override
  public boolean isOnlyScheduledInMasterZone() {
    return onlyScheduledInMasterZone;
  }

  /**
   * 设置此定时任务是否只在Master Zone中被调度执行。 默认为true。 如果想在Slave Zone中也被调度执行，请把此属性设置为false.
   */
  public void setOnlyScheduledInMasterZone(boolean onlyScheduledInMasterZone) {
    this.onlyScheduledInMasterZone = onlyScheduledInMasterZone;
  }

  @Override
  public String getBeanName() {
    return beanName;
  }

  @Override
  public void setBeanName(String beanName) {
    this.beanName = beanName;
    super.setBeanName(beanName);
  }

  @Override
  public boolean isJobDispatchTimeoutEnabled() {
    return this.jobDispatchTimeoutEnabled;
  }

  /**
   * 设置此定时任务是否允许派送超时。 默认为false，即该定时任务不允许派送超时。
   */
  public void setJobDispatchTimeoutEnabled(boolean jobDispatchTimeoutEnabled) {
    this.jobDispatchTimeoutEnabled = jobDispatchTimeoutEnabled;
  }

  @Override
  public Long getJobDispatchTimeout() {
    return this.jobDispatchTimeout;
  }

  /**
   * 设置此定时任务的派送超时时间(>=120000毫秒)，此设置仅当jobDispatchTimeoutEnabled设置为true时才有效
   */
  public void setJobDispatchTimeout(Long jobDispatchTimeout) {
    this.jobDispatchTimeout = jobDispatchTimeout;
  }

  @Override
  public String getCronExpression() {
    return super.getCronExpression();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (startTimeSpringPropertyInjectionSignal.getCount() > 0) {
      startTimeSpringPropertyInjectionSignal.countDown();
    }
    if (endTimeSpringPropertyInjectionSignal.getCount() > 0) {
      endTimeSpringPropertyInjectionSignal.countDown();
    }
    super.afterPropertiesSet();
  }

  @Override
  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  @Override
  public String getRunShellPath() {
    return runShellPath;
  }

  public void setRunShellPath(String runShellPath) {
    this.runShellPath = runShellPath;
  }
}
