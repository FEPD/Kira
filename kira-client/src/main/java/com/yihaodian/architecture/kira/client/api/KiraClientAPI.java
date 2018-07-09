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

package com.yihaodian.architecture.kira.client.api;

import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.kira.client.internal.util.CentralScheduleServiceContextHolder;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRoleEnum;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientUtils;
import com.yihaodian.architecture.kira.client.internal.util.MethodInvokeContextHolder;
import com.yihaodian.architecture.kira.client.util.HttpServletRequestDataWrapper;
import com.yihaodian.architecture.kira.client.util.KiraTimerTriggerBusinessRunningInstanceClientView;
import com.yihaodian.architecture.kira.client.util.TriggerMetadataClientSideView;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerTypeEnum;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class KiraClientAPI {

  public static final String TRIGGER_TYPE_SIMPLETRIGGER = TriggerTypeEnum.SIMPLETRIGGER.getName();
  public static final String TRIGGER_TYPE_CRONTRIGGER = TriggerTypeEnum.CRONTRIGGER.getName();
  private static final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private static Logger logger = LoggerFactory.getLogger(KiraClientAPI.class);

  public KiraClientAPI() {
  }

  /**
   * 设置执行定时任务相关业务方法时附带的日志信息。这个信息将会被上报并显示在kira平台界面上。请在定时任务对应的业务方法执行线程内执行此方法。
   */
  public static void setUserAddedMethodInvokeLog(String userAddedMethodInvokeLog) {
    MethodInvokeContextHolder.setUserAddedMethodInvokeLog(userAddedMethodInvokeLog);
  }

  /**
   * 获取HttpServletRequestDataWrapper对象。包含对HttpServletRequest对象数据的封装。
   */
  public static HttpServletRequestDataWrapper getHttpServletRequestDataWrapper() {
    return CentralScheduleServiceContextHolder.getHttpServletRequestDataWrapper();
  }

  /**
   * 获取定时任务当前配置信息
   *
   * @return triggerMetadataClientSideView or null if trigger do not exist.
   */
  public static TriggerMetadataClientSideView getTriggerMetadataClientSideView(String appId,
      String triggerId) throws KiraHandleException {
    TriggerMetadataClientSideView returnValue = null;
    if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      if (StringUtils.isBlank(appId)) {
        throw new KiraHandleException("appId should not be blank.");
      }
      if (StringUtils.isBlank(triggerId)) {
        throw new KiraHandleException("triggerId should not be blank.");
      }
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
      try {
        if (zkClient.exists(triggerZNodeZKPath)) {
          TriggerMetadataZNodeData triggerMetadataZNodeData = zkClient
              .readData(triggerZNodeZKPath, true);
          if (null != triggerMetadataZNodeData) {
            returnValue = new TriggerMetadataClientSideView();
            BeanUtils.copyProperties(triggerMetadataZNodeData, returnValue);
          } else {
            logger.warn(
                "triggerMetadataZNodeData for getTriggerMetadataClientSideView return null . appId={} and triggerId={} and triggerZNodeZKPath={}",
                appId, triggerId, triggerZNodeZKPath);
          }
        } else {
          logger.warn(
              "The trigger for getTriggerMetadataClientSideView do not exist. appId={} and triggerId={} and triggerZNodeZKPath={}",
              appId, triggerId, triggerZNodeZKPath);
        }
      } catch (Exception e) {
        logger.error("Exception occurs for getTriggerMetadataClientSideView. appId=" + appId
            + " and triggerId=" + triggerId + " and triggerZNodeZKPath=" + triggerZNodeZKPath, e);
        throw new KiraHandleException(e);
      }
    } else if (KiraClientRoleEnum.CHILD_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      //ccjtodo: phase2, need to ask manager for TriggerMetadataClientSideView
      throw new KiraHandleException("Not implemented yet.");
    }

    return returnValue;
  }

  private static void checkCommonDataForTriggerMetadataClientSideView(
      TriggerMetadataClientSideView triggerMetadataClientSideView) throws KiraHandleException {
    String appId = triggerMetadataClientSideView.getAppId();
    String triggerId = triggerMetadataClientSideView.getTriggerId();
    if (StringUtils.isBlank(appId)) {
      throw new KiraHandleException("appId should not be blank.");
    }
    if (StringUtils.isBlank(triggerId)) {
      throw new KiraHandleException("triggerId should not be blank.");
    }
    int priority = triggerMetadataClientSideView.getPriority().intValue();
    if (priority <= 0) {
      throw new KiraHandleException(
          "priority should be > 0");
    }

    String targetAppId = triggerMetadataClientSideView.getTargetAppId();
    String targetTriggerId = triggerMetadataClientSideView.getTargetTriggerId();
    boolean isInvokeTarget =
        StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId);

    if (!isInvokeTarget) {
      if (StringUtils.isBlank(targetAppId) || StringUtils.isBlank(targetTriggerId)) {
        throw new KiraHandleException(
            "The targetAppId and targetTriggerId should be both blank or both not blank.");
      }
    }

    String targetMethod = triggerMetadataClientSideView.getTargetMethod();
    String targetMethodArgTypes = triggerMetadataClientSideView.getTargetMethodArgTypes();
    if (!isInvokeTarget) {
      if (StringUtils.isBlank(targetMethod)) {
        throw new KiraHandleException(
            "isInvokeTarget=false. targetMethod should not be blank if you want to invoke service of yourself.");
      }
      if (StringUtils.isBlank(targetMethodArgTypes)) {
        throw new KiraHandleException(
            "isInvokeTarget=false. targetMethodArgTypes should not be blank if you want to invoke service of yourself. The targetMethodArgTypes should be [] for method which has no params.");
      }
    }

    boolean isInvokeMyself = isInvokeTarget && StringUtils.equals(appId, targetAppId) && StringUtils
        .equals(triggerId, targetTriggerId);
    if (isInvokeMyself) {
      if (StringUtils.isBlank(targetMethod)) {
        throw new KiraHandleException(
            "isInvokeMyself=true. targetMethod should not be blank if you want to invoke service of yourself.");
      }
      if (StringUtils.isBlank(targetMethodArgTypes)) {
        throw new KiraHandleException(
            "isInvokeMyself=true. targetMethodArgTypes should not be blank if you want to invoke service of yourself. The targetMethodArgTypes should be [] for method which has no params.");
      }
    }

    if (isInvokeTarget && (!isInvokeMyself)) {
      if (StringUtils.isNotBlank(targetMethod)) {
        throw new KiraHandleException(
            "isInvokeTarget=true and isInvokeMyself=false. targetMethod should be blank if you want to invoke other service.");
      }
      if (StringUtils.isNotBlank(targetMethodArgTypes)) {
        throw new KiraHandleException(
            "isInvokeTarget=true and isInvokeMyself=false. targetMethodArgTypes should be blank if you want to invoke other service.");
      }
    }

    Boolean manuallyCreated = triggerMetadataClientSideView.getManuallyCreated();
    if (Boolean.TRUE.equals(manuallyCreated)) {
      if (!isInvokeTarget) {
        throw new KiraHandleException(
            "The manually created trigger must config the targetAppId and targetTriggerId.");
      }

      if (isInvokeMyself) {
        throw new KiraHandleException("The manually created trigger can not invoke itself.");
      }

      if (StringUtils.isNotBlank(targetMethod)) {
        throw new KiraHandleException(
            "The value of targetMethod should be blank for manually created trigger.");
      }

      if (StringUtils.isNotBlank(targetMethodArgTypes)) {
        throw new KiraHandleException(
            "The value of targetMethodArgTypes should be blank for manually created trigger.");
      }
    }

    if (StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId)) {
      TriggerMetadataClientSideView targetTriggerMetadataClientSideView = KiraClientAPI
          .getTriggerMetadataClientSideView(targetAppId, targetTriggerId);
      if (null == targetTriggerMetadataClientSideView) {
        throw new KiraHandleException(
            "The trigger with the value of targetAppId and targetTriggerId do not exist. targetAppId="
                + targetAppId + " and targetTriggerId=" + targetTriggerId);
      } else {
        Boolean isTargetManuallyCreated = targetTriggerMetadataClientSideView.getManuallyCreated();
        if (null != isTargetManuallyCreated && isTargetManuallyCreated.booleanValue()) {
          throw new KiraHandleException(
              "The manually created trigger can not be set as target trigger.");
        }
      }
    }

    String argumentsAsJsonArrayString = triggerMetadataClientSideView
        .getArgumentsAsJsonArrayString();
    if (StringUtils.isBlank(argumentsAsJsonArrayString)) {
      throw new KiraHandleException(
          "The param values of the method call should not be blank. If that method has no params you need to see the value to []");
    }

    Boolean concurrent = triggerMetadataClientSideView.getConcurrent();
    if (null == concurrent) {
      throw new KiraHandleException("The value of concurrent should not be null.");
    }

    String triggerType = triggerMetadataClientSideView.getTriggerType();
    if (StringUtils.isBlank(triggerType)) {
      throw new KiraHandleException("triggerType should not be blank.");
    }
    TriggerTypeEnum triggerTypeEnum = TriggerTypeEnum.getTriggerTypeEnumByTriggerType(triggerType);
    if (null == triggerTypeEnum) {
      throw new KiraHandleException(
          "The value of triggerType is not valid. triggerType=" + triggerType
              + ". The valid triggerType is " + KiraClientAPI.TRIGGER_TYPE_SIMPLETRIGGER + " or "
              + KiraClientAPI.TRIGGER_TYPE_CRONTRIGGER);
    }
    Long repeatInterval = triggerMetadataClientSideView.getRepeatInterval();
    Integer repeatCount = triggerMetadataClientSideView.getRepeatCount();
    Long startDelay = triggerMetadataClientSideView.getStartDelay();
    String cronExpression = triggerMetadataClientSideView.getCronExpression();
    Integer misfireInstruction = triggerMetadataClientSideView.getMisfireInstruction();
    if (null == misfireInstruction) {
      throw new KiraHandleException("The value of misfireInstruction should not be null.");
    }
    if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      if (StringUtils.isNotBlank(cronExpression)) {
        throw new KiraHandleException(
            "The value of cronExpression should be blank for " + triggerType);
      }
      if (null == repeatInterval) {
        throw new KiraHandleException(
            "The value of repeatInterval should not be null for " + triggerType);
      } else {
        long repeatIntervalLongValue = repeatInterval.longValue();
        if (repeatIntervalLongValue < 0) {
          throw new KiraHandleException("Repeat interval must be >= 0");
        }
      }
      if (null != repeatCount) {
        int repeatCountIntValue = repeatCount.intValue();
        if (repeatCountIntValue < 0 && repeatCountIntValue != SimpleTrigger.REPEAT_INDEFINITELY) {
          throw new KiraHandleException(
              "Repeat count must be >= 0, use the " + SimpleTrigger.REPEAT_INDEFINITELY
                  + " for infinite.");
        }
      }
      if (!KiraClientUtils
          .validateMisfireInstructionForSimpleTrigger(misfireInstruction.intValue())) {
        throw new KiraHandleException(
            "The value of misfireInstruction is not valid for " + triggerType);
      }
    } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (null != startDelay) {
        throw new KiraHandleException("The value of startDelay should be null for " + triggerType);
      }
      if (null != repeatCount) {
        throw new KiraHandleException("The value of repeatCount should be null for " + triggerType);
      }
      if (null != repeatInterval) {
        throw new KiraHandleException(
            "The value of repeatInterval should be null for " + triggerType);
      }
      if (StringUtils.isBlank(cronExpression)) {
        throw new KiraHandleException("cronExpression should not be blank.");
      } else {
        try {
          CronExpression cronExpressionObj = new CronExpression(cronExpression);
        } catch (ParseException e) {
          throw new KiraHandleException(
              "The value of cronExpression is not valid : " + e.getMessage());
        }
      }
      if (!KiraClientUtils
          .validateMisfireInstructionForCronTrigger(misfireInstruction.intValue())) {
        throw new KiraHandleException(
            "The value of misfireInstruction is not valid for " + triggerType);
      }
    }

    Boolean asynchronous = triggerMetadataClientSideView.getAsynchronous();
    if (null == asynchronous) {
      throw new KiraHandleException("The value of asynchronous should not be null.");
    }

    Boolean onlyRunOnSingleProcess = triggerMetadataClientSideView.getOnlyRunOnSingleProcess();
    if (null == onlyRunOnSingleProcess) {
      throw new KiraHandleException("The value of onlyRunOnSingleProcess should not be null.");
    }

    Boolean limitToSpecifiedLocations = triggerMetadataClientSideView
        .getLimitToSpecifiedLocations();
    if (null == limitToSpecifiedLocations) {
      throw new KiraHandleException("The value of limitToSpecifiedLocations should not be null.");
    }

    Boolean disabled = triggerMetadataClientSideView.getDisabled();
    if (null == disabled) {
      throw new KiraHandleException("The value of disabled should not be null.");
    }

    Boolean requestsRecovery = triggerMetadataClientSideView.getRequestsRecovery();
    if (null == requestsRecovery) {
      throw new KiraHandleException("The value of requestsRecovery should not be null.");
    }

    Long runTimeThreshold = triggerMetadataClientSideView.getRunTimeThreshold();
    if (null != runTimeThreshold) {
      if (runTimeThreshold.longValue() <= 0) {
        throw new KiraHandleException(
            "The runTimeThreshold must be > 0 . Now runTimeThreshold is " + runTimeThreshold);
      }
    }

    Date startTimeSet = triggerMetadataClientSideView.getStartTime();
    Date endTimeSet = triggerMetadataClientSideView.getEndTime();
    if (null != startTimeSet && null != endTimeSet) {
      if (startTimeSet.after(endTimeSet)) {
        String message = "The endTimeSet is before startTimeSet. startTimeSet=" + KiraCommonUtils
            .getDateAsStringToMsPrecision(startTimeSet) + " and endTimeSet=" + KiraCommonUtils
            .getDateAsStringToMsPrecision(endTimeSet) + " and appId=" + appId + " and triggerId="
            + triggerId;
        logger.warn(message);
      }
    }

    Boolean copyFromMasterToSlaveZone = triggerMetadataClientSideView
        .getCopyFromMasterToSlaveZone();
    if (null == copyFromMasterToSlaveZone) {
      throw new KiraHandleException("The value of copyFromMasterToSlaveZone should not be null.");
    }

    Boolean onlyScheduledInMasterZone = triggerMetadataClientSideView
        .getOnlyScheduledInMasterZone();
    if (null == onlyScheduledInMasterZone) {
      throw new KiraHandleException("The value of onlyScheduledInMasterZone should not be null.");
    }

    Boolean jobDispatchTimeoutEnabled = triggerMetadataClientSideView
        .getJobDispatchTimeoutEnabled();
    if (null == jobDispatchTimeoutEnabled) {
      throw new KiraHandleException("The value of jobDispatchTimeoutEnabled should not be null.");
    }

    Long jobDispatchTimeout = triggerMetadataClientSideView.getJobDispatchTimeout();
    if (jobDispatchTimeoutEnabled.booleanValue()) {
      if (null == jobDispatchTimeout) {
        throw new KiraHandleException(
            "The value of jobDispatchTimeout should not be null if the jobDispatchTimeoutEnabled is set to true.");
      } else if (jobDispatchTimeout.longValue()
          < KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT) {
        throw new KiraHandleException("The value of jobDispatchTimeout should be >= "
            + KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT);
      }
    }

  }

  /**
   * 更新定时任务配置信息。 请先调用KiraClientAPI。getTriggerMetadataClientSideView()方法获取TriggerMetadataClientSideView的实例后，并修改其相关属性后调用此方法更新定时任务配置信息。
   *
   * @return the new TriggerMetadataClientSideView if success
   * @throws KiraHandleException if failed
   */
  public static TriggerMetadataClientSideView updateTrigger(
      TriggerMetadataClientSideView triggerMetadataClientSideView) throws KiraHandleException {
    TriggerMetadataClientSideView returnValue = null;
    if (null != triggerMetadataClientSideView) {
      if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
        try {
          triggerMetadataClientSideView.setDefaultValuesIfNeeded();
          KiraClientAPI
              .checkCommonDataForTriggerMetadataClientSideView(triggerMetadataClientSideView);

          String appId = triggerMetadataClientSideView.getAppId();
          String triggerId = triggerMetadataClientSideView.getTriggerId();

          Boolean oldScheduledLocally = triggerMetadataClientSideView.getScheduledLocally();
          if (Boolean.TRUE.equals(oldScheduledLocally)) {
            throw new KiraHandleException(
                "Can not dynamically update the trigger to be scheduled locally.");
          }

          String description = triggerMetadataClientSideView.getDescription();
          if (StringUtils.isBlank(description)) {
            throw new ValidationException(
                "The description should not be blank for trigger. appId=" + appId
                    + " and triggerId=" + triggerId);
          }

          String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
          if (zkClient.exists(triggerZNodeZKPath)) {
            TriggerMetadataZNodeData newTriggerMetadataZNodeData = new TriggerMetadataZNodeData();
            BeanUtils.copyProperties(triggerMetadataClientSideView, newTriggerMetadataZNodeData);
            String newVersion = KiraCommonConstants.DEFAULT_TRIGGER_VERSION;
            String oldVersion = triggerMetadataClientSideView.getVersion();
            Date now = new Date();
            if (StringUtils.isNotBlank(oldVersion)) {
              newVersion = KiraCommonUtils.getNewVersion(oldVersion, now);
            }
            boolean isVersionValidForKiraTimerTrigger = KiraCommonUtils
                .isVersionValidForKiraTimerTrigger(newVersion);
            if (!isVersionValidForKiraTimerTrigger) {
              //logger.warn("The format of new version for {} should be valid for KiraTimerTrigger for updateTrigger. Just warn this now to keep compatibility. oldVersion={} and newVersion={} and the format should be numbers delimited by \".\". For example:\"0.0.1\".", oldVersion, triggerId, newVersion);
              throw new ValidationException(
                  "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But version="
                      + oldVersion + " now.");
            }
            newTriggerMetadataZNodeData.setVersion(newVersion);
            newTriggerMetadataZNodeData.setCreateTime(now);
            String updatedBy = SystemUtil.getLocalhostIp();
            String comments = KiraCommonConstants.SPECIAL_DELIMITER + "Updated on " + updatedBy;
            String specifiedComments = newTriggerMetadataZNodeData.getComments();
            if (StringUtils.isNotBlank(specifiedComments)) {
              //If it start with ":::Updated by " it may be added by kira server side.
              //If it start with ":::Updated on " it may be added by kira client side.
              if ((!specifiedComments.trim()
                  .startsWith(KiraCommonConstants.SPECIAL_DELIMITER + "Updated by "))
                  && (!specifiedComments.trim()
                  .startsWith(KiraCommonConstants.SPECIAL_DELIMITER + "Updated on "))) {
                //It may be the comments specified by the user
                comments = comments + ". " + specifiedComments.trim();
              }
            }
            newTriggerMetadataZNodeData.setComments(comments);
            zkClient.writeData(triggerZNodeZKPath, newTriggerMetadataZNodeData);
            returnValue = new TriggerMetadataClientSideView();
            BeanUtils.copyProperties(newTriggerMetadataZNodeData, returnValue);
            logger.warn(
                "updateTrigger success for triggerMetadataClientSideView={} on triggerZNodeZKPath={} and newTriggerMetadataZNodeData={}",
                KiraCommonUtils.toString(triggerMetadataClientSideView), triggerZNodeZKPath,
                KiraCommonUtils.toString(newTriggerMetadataZNodeData));
          } else {
            throw new KiraHandleException(
                "Failed to update trigger. The trigger do not exist on triggerZNodeZKPath"
                    + triggerZNodeZKPath + " and appId=" + appId + " and triggerId=" + triggerId);
          }
        } catch (KiraHandleException kiraHandleException) {
          logger.error(
              "kiraHandleException occurs when update trigger. triggerMetadataClientSideView="
                  + KiraCommonUtils.toString(triggerMetadataClientSideView), kiraHandleException);
          throw kiraHandleException;
        } catch (Exception e) {
          logger.error("Exception occurs when update trigger. triggerMetadataClientSideView="
              + KiraCommonUtils.toString(triggerMetadataClientSideView), e);
          throw new KiraHandleException(e);
        }
      } else if (KiraClientRoleEnum.CHILD_PROCESS
          .equals(KiraClientDataCenter.getKiraClientRole())) {
        //ccjtodo: phase2, need to ask manager to update trigger
        throw new KiraHandleException("Not implemented yet.");
      }
    } else {
      String errorMessage = "The specified triggerMetadataClientSideView should not be null.";
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    return returnValue;
  }

  /**
   * 创建定时任务
   *
   * @return the new TriggerMetadataClientSideView if success
   */
  public static TriggerMetadataClientSideView createTrigger(
      TriggerMetadataClientSideView triggerMetadataClientSideView) throws KiraHandleException {
    TriggerMetadataClientSideView returnValue = null;
    if (null != triggerMetadataClientSideView) {
      if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
        try {
          triggerMetadataClientSideView.setManuallyCreated(Boolean.TRUE);
          triggerMetadataClientSideView.setDefaultValuesIfNeeded();
          triggerMetadataClientSideView.setTargetMethod(null);
          triggerMetadataClientSideView.setTargetMethodArgTypes(null);
          KiraClientAPI
              .checkCommonDataForTriggerMetadataClientSideView(triggerMetadataClientSideView);

          String version = triggerMetadataClientSideView.getVersion();
          boolean isVersionValidForKiraTimerTrigger = KiraCommonUtils
              .isVersionValidForKiraTimerTrigger(version);
          if (!isVersionValidForKiraTimerTrigger) {
            //logger.warn("The format of version for {} should be valid for KiraTimerTrigger for createTrigger. Just warn this now to keep compatibility. version={} and the format should be numbers delimited by \".\". For example:\"0.0.1\".", triggerMetadataClientSideView.getTriggerId(), version);
            throw new ValidationException(
                "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But version="
                    + version + " now.");
          }

          String appId = triggerMetadataClientSideView.getAppId();
          String triggerId = triggerMetadataClientSideView.getTriggerId();
          String myAppId = KiraUtil.appId();
          if (!StringUtils.equals(myAppId, appId)) {
            throw new KiraHandleException(
                "Your can only create the trigger of your pool by api. myAppId=" + myAppId
                    + " and to be created trigger's appId=" + appId);
          }

          Boolean oldScheduledLocally = triggerMetadataClientSideView.getScheduledLocally();
          if (Boolean.TRUE.equals(oldScheduledLocally)) {
            throw new KiraHandleException(
                "Can not dynamically create the trigger to be scheduled locally.");
          }

          String description = triggerMetadataClientSideView.getDescription();
          if (StringUtils.isBlank(description)) {
            throw new ValidationException(
                "The description should not be blank for trigger. appId=" + appId
                    + " and triggerId=" + triggerId);
          }

          String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
          if (!zkClient.exists(triggerZNodeZKPath)) {
            TriggerMetadataZNodeData newTriggerMetadataZNodeData = new TriggerMetadataZNodeData();
            BeanUtils.copyProperties(triggerMetadataClientSideView, newTriggerMetadataZNodeData);

            Date now = new Date();
            newTriggerMetadataZNodeData.setCreateTime(now);
            String updatedBy = SystemUtil.getLocalhostIp();
            String comments = KiraCommonConstants.SPECIAL_DELIMITER + "Created on " + updatedBy;
            String specifiedComments = newTriggerMetadataZNodeData.getComments();
            if (StringUtils.isNotBlank(specifiedComments)) {
              //It may be the comments specified by the user
              comments = comments + ". " + specifiedComments.trim();
            }
            newTriggerMetadataZNodeData.setComments(comments);

            zkClient.createPersistent(triggerZNodeZKPath, newTriggerMetadataZNodeData);
            returnValue = new TriggerMetadataClientSideView();
            BeanUtils.copyProperties(newTriggerMetadataZNodeData, returnValue);
            logger.warn(
                "createTrigger success for triggerMetadataClientSideView={} on triggerZNodeZKPath={} and newTriggerMetadataZNodeData={}",
                KiraCommonUtils.toString(triggerMetadataClientSideView), triggerZNodeZKPath,
                KiraCommonUtils.toString(newTriggerMetadataZNodeData));
          } else {
            throw new KiraHandleException(
                "Failed to create trigger. The trigger already exist on triggerZNodeZKPath"
                    + triggerZNodeZKPath + " and appId=" + appId + " and triggerId=" + triggerId);
          }
        } catch (KiraHandleException kiraHandleException) {
          logger.error(
              "kiraHandleException occurs when create trigger. triggerMetadataClientSideView="
                  + KiraCommonUtils.toString(triggerMetadataClientSideView), kiraHandleException);
          throw kiraHandleException;
        } catch (Exception e) {
          logger.error("Exception occurs when create trigger. triggerMetadataClientSideView="
              + KiraCommonUtils.toString(triggerMetadataClientSideView), e);
          throw new KiraHandleException(e);
        }
      } else if (KiraClientRoleEnum.CHILD_PROCESS
          .equals(KiraClientDataCenter.getKiraClientRole())) {
        //ccjtodo: phase2, need to ask manager to create trigger
        throw new KiraHandleException("Not implemented yet.");
      }
    } else {
      String errorMessage = "The specified triggerMetadataClientSideView should not be null for createTrigger.";
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }
    return returnValue;
  }

  /**
   * 删除定时任务
   *
   * @return True if it is success to delete the trigger or the trigger already do not exist now.
   * False if failed to delete the trigger.
   */
  public static boolean deleteTrigger(String appId, String triggerId) throws KiraHandleException {
    boolean returnValue = false;
    if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      if (StringUtils.isBlank(appId)) {
        throw new KiraHandleException("appId should not be blank.");
      }
      if (StringUtils.isBlank(triggerId)) {
        throw new KiraHandleException("triggerId should not be blank.");
      }
      String myAppId = KiraUtil.appId();
      if (!StringUtils.equals(myAppId, appId)) {
        throw new KiraHandleException(
            "Your can only delete the trigger of your pool by api. myAppId=" + myAppId
                + " and to be deleted trigger's appId=" + appId);
      }
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
      try {
        if (zkClient.exists(triggerZNodeZKPath)) {
          zkClient.deleteRecursive(triggerZNodeZKPath);
          logger.warn(
              "Success for deleteTrigger. appId={} and triggerId={} and triggerZNodeZKPath={}",
              appId, triggerId, triggerZNodeZKPath);
        } else {
          logger.warn(
              "The trigger to be deleted do not exist. appId={} and triggerId={} and triggerZNodeZKPath={}",
              appId, triggerId, triggerZNodeZKPath);
        }
        returnValue = true;
      } catch (Exception e) {
        logger.error(
            "Exception occurs for deleteTrigger. appId=" + appId + " and triggerId=" + triggerId
                + " and triggerZNodeZKPath=" + triggerZNodeZKPath, e);
        throw new KiraHandleException(e);
      }
    } else if (KiraClientRoleEnum.CHILD_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      //ccjtodo: phase2, need to ask manager for TriggerMetadataClientSideView
      throw new KiraHandleException("Not implemented yet.");
    }

    return returnValue;
  }

  /**
   * 定时任务对应的业务方法是否正在执行中。
   */
  public static boolean isKiraTimerTriggerBusinessRunning(String appId, String triggerId)
      throws KiraHandleException {
    boolean returnValue = false;

    returnValue = KiraCommonUtils.isKiraTimerTriggerBusinessRunning(appId, triggerId);

    return returnValue;
  }

  /**
   * 获取定时任务对应的当前正在执行的业务方法的实例列表。
   */
  public static List<KiraTimerTriggerBusinessRunningInstanceClientView> getKiraTimerTriggerBusinessRunningInstanceClientViewList(
      String appId, String triggerId) throws Throwable {
    List<KiraTimerTriggerBusinessRunningInstanceClientView> returnValue = new ArrayList<KiraTimerTriggerBusinessRunningInstanceClientView>();

    try {
      List<KiraTimerTriggerBusinessRunningInstance> kiraTimerTriggerBusinessRunningInstanceList = KiraCommonUtils
          .getKiraTimerTriggerBusinessRunningInstanceList(appId, triggerId);
      if (null != kiraTimerTriggerBusinessRunningInstanceList) {
        returnValue = new ArrayList<KiraTimerTriggerBusinessRunningInstanceClientView>();
        for (KiraTimerTriggerBusinessRunningInstance kiraTimerTriggerBusinessRunningInstance : kiraTimerTriggerBusinessRunningInstanceList) {
          if (null != kiraTimerTriggerBusinessRunningInstance) {
            KiraTimerTriggerBusinessRunningInstanceClientView kiraTimerTriggerBusinessRunningInstanceClientView = new KiraTimerTriggerBusinessRunningInstanceClientView();
            BeanUtils.copyProperties(kiraTimerTriggerBusinessRunningInstance,
                kiraTimerTriggerBusinessRunningInstanceClientView);
            returnValue.add(kiraTimerTriggerBusinessRunningInstanceClientView);
          }
        }

      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when getKiraTimerTriggerBusinessRunningInstanceClientViewList. appId="
              + appId + " and triggerId=" + triggerId, t);
      throw t;
    }
    return returnValue;
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

  }

}
