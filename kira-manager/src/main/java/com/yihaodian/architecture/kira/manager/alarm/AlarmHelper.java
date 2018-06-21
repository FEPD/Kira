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
package com.yihaodian.architecture.kira.manager.alarm;

import com.yihaodian.architecture.kira.common.JobStatusEnum;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.event.EventDispatcher;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.EmailUtils;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.SMSUtils;
import com.yihaodian.architecture.kira.manager.util.VelocityUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class AlarmHelper {

  private static Logger logger = LoggerFactory.getLogger(AlarmHelper.class);

  private TriggerMetadataService triggerMetadataService;
  private JobService jobService;
  private JobItemService jobItemService;
  private KiraClientMetadataService kiraClientMetadataService;
  private JobTimeoutTrackerService jobTimeoutTrackerService;
  private VelocityUtils velocityUtils;
  private EventDispatcher eventDispatcher;

  public AlarmHelper() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public void setJobItemService(JobItemService jobItemService) {
    this.jobItemService = jobItemService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public JobTimeoutTrackerService getJobTimeoutTrackerService() {
    return jobTimeoutTrackerService;
  }

  public void setJobTimeoutTrackerService(
      JobTimeoutTrackerService jobTimeoutTrackerService) {
    this.jobTimeoutTrackerService = jobTimeoutTrackerService;
  }

  public void setVelocityUtils(VelocityUtils velocityUtils) {
    this.velocityUtils = velocityUtils;
  }

  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
  }

  public boolean isNeedToAlarm(Integer jobStatusId) {
    boolean returnValue = KiraCommonUtils.isBadAndFinalJobStatus(jobStatusId);
    return returnValue;
  }

  public void handleJobTimeoutAlarmTask(JobTimeoutAlarmTask jobAlarmTask) throws Throwable {
    JobTimeoutTracker jobTimeoutTracker = jobAlarmTask.getJobTimeoutTracker();
    JobAlarmMessage jobTimeoutAlarmMessage = getJobTimeoutAlarmMessage(jobTimeoutTracker.getJobId(),
        jobAlarmTask.getCreateTime(), jobTimeoutTracker.getRumTimeThreshold());
    if (null != jobTimeoutAlarmMessage) {
      handleTriggerAwareAlarmMessage(jobTimeoutAlarmMessage);
    }
  }

  public void handleJobAlarmTask(JobAlarmTask jobAlarmTask) throws Throwable {
    JobAlarmMessage jobAlarmMessage = getJobAlarmMessage(jobAlarmTask.getJobId(),
        jobAlarmTask.getCreateTime());
    if (null != jobAlarmMessage) {
      handleTriggerAwareAlarmMessage(jobAlarmMessage);
    }
  }

  public void handleJobItemAlarmTask(JobItemAlarmTask jobItemAlarmTask) throws Throwable {
    JobItemAlarmMessage jobItemAlarmMessage = getJobItemAlarmMessage(
        jobItemAlarmTask.getJobItemId(), jobItemAlarmTask.getCreateTime());
    if (null != jobItemAlarmMessage) {
      handleTriggerAwareAlarmMessage(jobItemAlarmMessage);
    }
  }

  private void handleTriggerAwareAlarmMessage(TriggerAwareAlarmMessage triggerAwareAlarmMessage)
      throws Throwable {
    if (null != triggerAwareAlarmMessage) {
      String appId = triggerAwareAlarmMessage.getAppId();
      List<String> poolIdList = new ArrayList<String>();
      poolIdList.add(appId);
      List<KiraClientMetadata> kiraClientMetadataList = kiraClientMetadataService
          .getKiraClientMetadataListByPoolIdList(poolIdList);
      if (CollectionUtils.isNotEmpty(kiraClientMetadataList)) {
        KiraClientMetadata kiraClientMetadata = kiraClientMetadataList.get(0);
        if (null != kiraClientMetadata) {
          String emailsToReceiveAlarm = kiraClientMetadata.getEmailsToReceiveAlarm();
          String phoneNumbersToReceiveAlarmSMS = kiraClientMetadata
              .getPhoneNumbersToReceiveAlarmSMS();
          boolean sendAlarmEmail = kiraClientMetadata.getSendAlarmEmail();
          List<String> emailsToReceiveAlarmAsList = KiraCommonUtils
              .getStringListByDelmiter(emailsToReceiveAlarm, KiraCommonConstants.COMMA_DELIMITER);
          boolean sendAlarmSMS = kiraClientMetadata.getSendAlarmSMS();
          List<String> phoneNumbersToReceiveAlarmSMSAsList = KiraCommonUtils
              .getStringListByDelmiter(phoneNumbersToReceiveAlarmSMS,
                  KiraCommonConstants.COMMA_DELIMITER);

          if (sendAlarmEmail && CollectionUtils.isNotEmpty(emailsToReceiveAlarmAsList)) {
            sendAlarmEmail(triggerAwareAlarmMessage, emailsToReceiveAlarmAsList);
          }

          if (sendAlarmSMS && CollectionUtils.isNotEmpty(phoneNumbersToReceiveAlarmSMSAsList)) {
            sendAlarmSMS(triggerAwareAlarmMessage, phoneNumbersToReceiveAlarmSMSAsList);
          }
        }
      }
    }
  }

  private void sendAlarmEmail(TriggerAwareAlarmMessage triggerAwareAlarmMessage,
      List<String> emailsToReceiveAlarmAsList) throws Throwable {
    try {
      String emailSubject = null;
      String emailContent = null;
      if (triggerAwareAlarmMessage instanceof JobTimeoutAlarmMessage) {
        emailSubject = getEmailSubjectForJobTimeoutAlarmMessage();
        JobTimeoutAlarmMessage jobTimeoutAlarmMessage = (JobTimeoutAlarmMessage) triggerAwareAlarmMessage;
        emailContent = getEmailContentForJobTimeoutAlarmMessage(jobTimeoutAlarmMessage);
      } else if (triggerAwareAlarmMessage instanceof JobAlarmMessage) {
        emailSubject = getEmailSubjectForJobAlarmMessage();
        JobAlarmMessage jobAlarmMessage = (JobAlarmMessage) triggerAwareAlarmMessage;
        emailContent = getEmailContentForJobAlarmMessage(jobAlarmMessage);
      } else if (triggerAwareAlarmMessage instanceof JobItemAlarmMessage) {
        emailSubject = getEmailSubjectForJobItemAlarmMessage();
        JobItemAlarmMessage jobItemAlarmMessage = (JobItemAlarmMessage) triggerAwareAlarmMessage;
        emailContent = getEmailContentForJobItemAlarmMessage(jobItemAlarmMessage);
      }

      EmailUtils.sendInnerEmail(emailsToReceiveAlarmAsList, emailContent, emailSubject);

      //ccjtodo: need to remove below if needed
      List<String> adminEmailsAsList = KiraCommonUtils
          .getStringListByDelmiter(KiraManagerUtils.getAdminEmails(),
              KiraCommonConstants.COMMA_DELIMITER);
      if (CollectionUtils.isNotEmpty(adminEmailsAsList)) {
        EmailUtils.sendInnerEmail(adminEmailsAsList, emailContent, emailSubject);
      }
    } catch (Throwable t) {
      logger.error("Error occurs for sendAlarmEmail. triggerAwareAlarmMessage=" + KiraCommonUtils
          .toString(triggerAwareAlarmMessage) + " and emailsToReceiveAlarmAsList="
          + emailsToReceiveAlarmAsList, t);
      throw t;
    }
  }

  private void sendAlarmSMS(TriggerAwareAlarmMessage triggerAwareAlarmMessage,
      List<String> phoneNumbersToReceiveAlarmSMSAsList) throws Throwable {
    try {
      String smsContent = null;
      if (triggerAwareAlarmMessage instanceof JobTimeoutAlarmMessage) {
        JobTimeoutAlarmMessage jobTimeoutAlarmMessage = (JobTimeoutAlarmMessage) triggerAwareAlarmMessage;
        smsContent = getSMSContentForJobTimeoutAlarmMessage(jobTimeoutAlarmMessage);
      } else if (triggerAwareAlarmMessage instanceof JobAlarmMessage) {
        JobAlarmMessage jobAlarmMessage = (JobAlarmMessage) triggerAwareAlarmMessage;
        smsContent = getSMSContentForJobAlarmMessage(jobAlarmMessage);
      } else if (triggerAwareAlarmMessage instanceof JobItemAlarmMessage) {
        JobItemAlarmMessage jobItemAlarmMessage = (JobItemAlarmMessage) triggerAwareAlarmMessage;
        smsContent = getSMSContentForJobItemAlarmMessage(jobItemAlarmMessage);
      }
      SMSUtils.sendSMS(phoneNumbersToReceiveAlarmSMSAsList, smsContent);

      //ccjtodo: need to remove below if needed
      List<String> adminPhoneNumbersAsList = KiraCommonUtils
          .getStringListByDelmiter(KiraManagerUtils.getAdminPhoneNumbers(),
              KiraCommonConstants.COMMA_DELIMITER);
      if (CollectionUtils.isNotEmpty(adminPhoneNumbersAsList)) {
        SMSUtils.sendSMS(adminPhoneNumbersAsList, smsContent);
      }
    } catch (Throwable t) {
      logger.error("Error occurs for sendAlarmSMS. triggerAwareAlarmMessage=" + KiraCommonUtils
          .toString(triggerAwareAlarmMessage) + " and phoneNumbersToReceiveAlarmSMSAsList="
          + phoneNumbersToReceiveAlarmSMSAsList, t);
      throw t;
    }
  }

  private JobTimeoutAlarmMessage getJobTimeoutAlarmMessage(String jobId, Date createTime,
      Long rumTimeThreshold) {
    JobTimeoutAlarmMessage returnValue = null;
    JobAlarmMessage jobAlarmMessage = getJobAlarmMessage(jobId, createTime);
    if (null != jobAlarmMessage) {
      returnValue = new JobTimeoutAlarmMessage();
      BeanUtils.copyProperties(jobAlarmMessage, returnValue);
      returnValue.setRumTimeThreshold(rumTimeThreshold);
    }

    return returnValue;
  }

  private JobAlarmMessage getJobAlarmMessage(String jobId, Date createTime) {
    JobAlarmMessage returnValue = null;
    Job job = jobService.select(jobId);
    if (null != job) {
      Long triggerMetadataId = job.getTriggerMetadataId();
      TriggerMetadata triggerMetadata = triggerMetadataService.select(triggerMetadataId);
      if (null != triggerMetadata) {
        returnValue = new JobAlarmMessage();
        returnValue.setJobId(jobId);
        returnValue.setCreatedBy(job.getCreatedBy());
        returnValue.setManuallyScheduled(job.getManuallyScheduled());
        returnValue.setJobCreateTime(job.getCreateTime());

        Integer jobStatusId = job.getJobStatusId();
        returnValue.setJobStatusId(jobStatusId);
        String jobStatusName = JobStatusEnum.getJobStatusNameById(jobStatusId);
        returnValue.setJobStatusName(jobStatusName);

        returnValue.setAppId(triggerMetadata.getAppId());
        returnValue.setTriggerId(triggerMetadata.getTriggerId());
        returnValue.setTriggerVersion(triggerMetadata.getVersion());
        returnValue.setTriggerDescription(triggerMetadata.getDescription());

        returnValue.setAlarmCreateTime(createTime);
        returnValue.setAlarmMessage(job.getResultData());
      }
    }
    return returnValue;
  }

  private JobItemAlarmMessage getJobItemAlarmMessage(String jobItemId, Date createTime) {
    JobItemAlarmMessage returnValue = null;
    JobItem jobItem = jobItemService.select(jobItemId);
    if (null != jobItem) {
      String jobId = jobItem.getJobId();
      Job job = jobService.select(jobId);
      if (null != job) {
        Long triggerMetadataId = job.getTriggerMetadataId();
        TriggerMetadata triggerMetadata = triggerMetadataService.select(triggerMetadataId);
        if (null != triggerMetadata) {
          returnValue = new JobItemAlarmMessage();
          returnValue.setJobItemId(jobItemId);
          returnValue.setJobItemCreateTime(jobItem.getCreateTime());
          returnValue.setServiceUrl(jobItem.getServiceUrl());

          Integer jobStatusId = jobItem.getJobStatusId();
          returnValue.setJobStatusId(jobStatusId);
          String jobStatusName = JobStatusEnum.getJobStatusNameById(jobStatusId);
          returnValue.setJobStatusName(jobStatusName);

          returnValue.setAppId(triggerMetadata.getAppId());
          returnValue.setTriggerId(triggerMetadata.getTriggerId());
          returnValue.setTriggerVersion(triggerMetadata.getVersion());
          returnValue.setTriggerDescription(triggerMetadata.getDescription());

          returnValue.setAlarmCreateTime(createTime);
          returnValue.setAlarmMessage(jobItem.getResultData());
        }
      }
    }
    return returnValue;
  }

  private String getEmailSubjectForJobTimeoutAlarmMessage() {
    StringBuilder sb = new StringBuilder();
    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    sb.append("[Jd]-[Kira]");
    if (StringUtils.isNotBlank(env)) {
      sb.append("-[").append(env).append("环境]");
    }
    sb.append("-定时任务执行超时报警!");
    return sb.toString();
  }

  private String getEmailSubjectForJobAlarmMessage() {
    StringBuilder sb = new StringBuilder();
    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    sb.append("[Jd]-[Kira]");
    if (StringUtils.isNotBlank(env)) {
      sb.append("-[").append(env).append("环境]");
    }
    sb.append("-定时任务执行报警!");
    return sb.toString();
  }

  private String getEmailSubjectForJobItemAlarmMessage() {
    StringBuilder sb = new StringBuilder();
    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    sb.append("[Jd]-[Kira]");
    if (StringUtils.isNotBlank(env)) {
      sb.append("-[").append(env).append("环境]");
    }
    sb.append("-定时任务子任务执行报警!");
    return sb.toString();
  }

  private String getEmailContentForJobTimeoutAlarmMessage(
      JobTimeoutAlarmMessage jobTimeoutAlarmMessage) throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobTimeoutAlarmMessage", jobTimeoutAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOB_TIMEOUT_ALARM_EMAIL);
    } catch (Throwable t) {
      logger.error(
          "Error occurs for getEmailContentForJobTimeoutAlarmMessage. jobTimeoutAlarmMessage="
              + KiraCommonUtils.toString(jobTimeoutAlarmMessage), t);
      throw t;
    }

    return returnValue;
  }

  private String getSMSContentForJobTimeoutAlarmMessage(
      JobTimeoutAlarmMessage jobTimeoutAlarmMessage) throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobTimeoutAlarmMessage", jobTimeoutAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOB_TIMEOUT_ALARM_SMS);
    } catch (Throwable t) {
      logger.error(
          "Error occurs for getSMSContentForJobTimeoutAlarmMessage. jobTimeoutAlarmMessage="
              + KiraCommonUtils.toString(jobTimeoutAlarmMessage), t);
      throw t;
    }
    return returnValue;
  }

  private String getEmailContentForJobAlarmMessage(JobAlarmMessage jobAlarmMessage)
      throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobAlarmMessage", jobAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOB_ALARM_EMAIL);
    } catch (Throwable t) {
      logger.error(
          "Error occurs for getEmailContentForJobAlarmMessage. jobAlarmMessage=" + KiraCommonUtils
              .toString(jobAlarmMessage), t);
      throw t;
    }

    return returnValue;
  }

  private String getSMSContentForJobAlarmMessage(JobAlarmMessage jobAlarmMessage) throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobAlarmMessage", jobAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOB_ALARM_SMS);
    } catch (Throwable t) {
      logger.error(
          "Error occurs for getSMSContentForJobAlarmMessage. jobAlarmMessage=" + KiraCommonUtils
              .toString(jobAlarmMessage), t);
      throw t;
    }
    return returnValue;
  }

  private String getEmailContentForJobItemAlarmMessage(JobItemAlarmMessage jobItemAlarmMessage)
      throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobItemAlarmMessage", jobItemAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOBITEM_ALARM_EMAIL);
    } catch (Throwable t) {
      logger.error("Error occurs for getEmailContentForJobItemAlarmMessage. jobItemAlarmMessage="
          + KiraCommonUtils.toString(jobItemAlarmMessage), t);
      throw t;
    }
    return returnValue;
  }

  private String getSMSContentForJobItemAlarmMessage(JobItemAlarmMessage jobItemAlarmMessage)
      throws Throwable {
    String returnValue = null;
    try {
      VelocityContext velocityContext = new VelocityContext();
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      velocityContext.put("env", env);
      String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
      velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);
      velocityContext.put("jobItemAlarmMessage", jobItemAlarmMessage);
      returnValue = velocityUtils
          .getMergedContent(velocityContext, VelocityUtils.TEMPLATE_NAME_JOBITEM_ALARM_SMS);
    } catch (Throwable t) {
      logger.error("Error occurs for getSMSContentForJobItemAlarmMessage. jobItemAlarmMessage="
          + KiraCommonUtils.toString(jobItemAlarmMessage), t);
      throw t;
    }
    return returnValue;
  }

}
