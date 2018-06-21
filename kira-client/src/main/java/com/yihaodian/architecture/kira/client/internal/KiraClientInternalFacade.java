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
package com.yihaodian.architecture.kira.client.internal;

import com.alibaba.fastjson.JSON;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.kira.client.internal.iface.IYHDCronTriggerBean;
import com.yihaodian.architecture.kira.client.internal.iface.IYHDSimpleTriggerBean;
import com.yihaodian.architecture.kira.client.internal.iface.IYHDTriggerBean;
import com.yihaodian.architecture.kira.client.internal.impl.KiraClientRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.impl.TriggerDeleteHandler;
import com.yihaodian.architecture.kira.client.internal.impl.TriggerRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRegisterContextData;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean;
import com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter;
import com.yihaodian.architecture.kira.client.util.KiraClientConfig;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobTypeEnum;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.TriggerMetadataDetail;
import com.yihaodian.architecture.kira.common.TriggerTypeEnum;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.AppCenterZNodeData;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

public class KiraClientInternalFacade implements IKiraClientInternalFacade {

  private static Logger logger = LoggerFactory
      .getLogger(KiraClientInternalFacade.class);
  private static IKiraClientInternalFacade kiraClientInternalFacade = new KiraClientInternalFacade();
  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();

  private KiraClientInternalFacade() {
  }

  public static IKiraClientInternalFacade getKiraClientInternalFacade() {
    return kiraClientInternalFacade;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy() {
    try {
      boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
      if (!workWithoutKira) {
        boolean isAppCenter = KiraClientDataCenter.isAppCenter();
        if (isAppCenter) {
          //need to delete the appCenter node on zk.
          CentralScheduleServiceExporter centralScheduleServiceExporter = KiraClientDataCenter
              .getCentralScheduleServiceExporter();
          if (null != centralScheduleServiceExporter) {
            ServiceProfile serviceProfile = KiraCommonUtils
                .getServiceProfile(centralScheduleServiceExporter);
            if (null != serviceProfile) {
              String appCenterZNodePath = getAppCenterZNodePath(serviceProfile);
              if (StringUtils.isNotBlank(appCenterZNodePath)) {
                if (zkClient.exists(appCenterZNodePath)) {
                  //need to recreate for it may be the dirty znode.
                  zkClient.delete(appCenterZNodePath);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      if (null != logger) {
        logger.error("Error occurs for KiraClientInternalFacade.destroy.", e);
      }
    }
  }

  @Override
  public Trigger[] handleAndGetLocallyRunTriggers(YHDSchedulerFactoryBean yhdSchedulerFactoryBean,
      Trigger[] triggers, boolean workWithoutKira) {
    List<Trigger> locallyRunTriggerList = new ArrayList<Trigger>();
    List<TriggerRegisterContextData> triggerRegisterContextDataList = new ArrayList<TriggerRegisterContextData>();
    String schedulerBeanName = yhdSchedulerFactoryBean.getBeanName();
    if (null != triggers) {
      for (Trigger oneTrigger : triggers) {
        if (oneTrigger instanceof IYHDTriggerBean) {
          IYHDTriggerBean yhdTriggerBean = ((IYHDTriggerBean) oneTrigger);

          if (!workWithoutKira) {
            TriggerRegisterContextData triggerRegisterContextData = new TriggerRegisterContextData();
            triggerRegisterContextData
                .setEnvironment(KiraClientDataCenter.getLocalProcessEnvironment());
            triggerRegisterContextData.setSchedulerBeanName(schedulerBeanName);
            TriggerMetadataDetail triggerMetaDataDetail = checkAndGetTriggerMetaDataDetail(
                yhdSchedulerFactoryBean, yhdTriggerBean);
            triggerRegisterContextData.setTriggerMetaDataDetail(triggerMetaDataDetail);
            triggerRegisterContextDataList.add(triggerRegisterContextData);
          }

          boolean isDisabled = yhdTriggerBean.isDisabled();
          boolean isScheduledLocally = yhdTriggerBean.isScheduledLocally();
          if (isScheduledLocally || workWithoutKira) {
            if (!isDisabled) {
              locallyRunTriggerList.add(oneTrigger);
            }
          }
        } else {
          locallyRunTriggerList.add(oneTrigger);
        }
      }

      for (TriggerRegisterContextData triggerRegisterContextData : triggerRegisterContextDataList) {
        KiraClientDataCenter.addTriggerRegisterContextData(triggerRegisterContextData);
      }

      for (TriggerRegisterContextData triggerRegisterContextData : triggerRegisterContextDataList) {
        TriggerRegisterContextDataHandler.getTriggerRegisterContextDataHandler()
            .handleTriggerRegisterContextData(triggerRegisterContextData);
      }
    }

    boolean isAutoDeleteTriggersOnZK = KiraClientDataCenter.isAutoDeleteTriggersOnZK();
    if (isAutoDeleteTriggersOnZK && !workWithoutKira) {
      //initialize the TriggerDeleteHandler.
      TriggerDeleteHandler.getTriggerDeleteHandler();
    }
    return locallyRunTriggerList.toArray(new Trigger[locallyRunTriggerList.size()]);
  }

  private TriggerMetadataDetail checkAndGetTriggerMetaDataDetail(
      YHDSchedulerFactoryBean yhdSchedulerFactoryBean, IYHDTriggerBean yhdTriggerBean) {
    TriggerMetadataDetail triggerMetaDataDetail = new TriggerMetadataDetail();
    String appId = KiraUtil.appId();
    if (StringUtils.isBlank(appId)) {
      throw new ValidationException("appId should not be blank.");
    }
    triggerMetaDataDetail.setAppId(appId);
    String triggerBeanName = yhdTriggerBean.getBeanName();
    if (StringUtils.isBlank(triggerBeanName)) {
      throw new ValidationException("triggerBeanName should not be blank.");
    }
    triggerMetaDataDetail.setTriggerId(triggerBeanName);
    String version = yhdTriggerBean.getVersion();
    if (StringUtils.isBlank(version)) {
      logger.info("version is found as blank so use {} as alternate.",
          KiraCommonConstants.DEFAULT_TRIGGER_VERSION);
      version = KiraCommonConstants.DEFAULT_TRIGGER_VERSION;
    } else if (StringUtils.containsIgnoreCase(version, "snapshot")) {
      throw new ValidationException("The value of version should not contains \"snapshot\".");
    }
    boolean isVersionValidForKiraTimerTrigger = KiraCommonUtils
        .isVersionValidForKiraTimerTrigger(version);
    if (!isVersionValidForKiraTimerTrigger) {
      //logger.warn("The format of version for {} should be valid for KiraTimerTrigger. Just warn this now to keep compatibility. version={} and the format should be numbers delimited by \".\". For example:\"0.0.1\".", triggerBeanName, version);
      throw new ValidationException(
          "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But version="
              + version + " now.");
    }
    triggerMetaDataDetail.setVersion(version);

    Integer priority = yhdTriggerBean.getPrioritySet();
    if (null == priority) {
      priority = Integer.valueOf(KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER);
    } else {
      if (priority.intValue() <= 0) {
        throw new ValidationException(
            "priority should be > 0");
      }
    }
    triggerMetaDataDetail.setPriority(priority);

    String targetAppId = yhdTriggerBean.getTargetAppId();
    String targetTriggerId = yhdTriggerBean.getTargetTriggerId();
    boolean isInvokeTarget =
        StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId);
    if (!isInvokeTarget) {
      if (StringUtils.isNotBlank(targetAppId) || StringUtils.isNotBlank(targetTriggerId)) {
        throw new ValidationException(
            "The targetAppId and targetTriggerId should be both blank or both not blank.");
      }
    }
    triggerMetaDataDetail.setTargetAppId(targetAppId);
    triggerMetaDataDetail.setTargetTriggerId(targetTriggerId);

    String targetMethod = null;
    String targetMethodArgTypes = null;
    String argumentsAsJsonArrayString = null;
    if (yhdTriggerBean instanceof JobDetailAwareTrigger) {
      JobDetail jobDetail = ((JobDetailAwareTrigger) yhdTriggerBean).getJobDetail();
      targetMethod = (String) jobDetail.getJobDataMap()
          .get(KiraClientConstants.JOBDATAMAP_KEY_TARGETMETHOD);
      String[] argTypes = (String[]) jobDetail.getJobDataMap()
          .get(KiraClientConstants.JOBDATAMAP_KEY_TARGETMETHODARGTYPES);
      if (null != argTypes) {
        targetMethodArgTypes = JSON.toJSONString(argTypes);
      }
      Object[] arguments = (Object[]) jobDetail.getJobDataMap()
          .get(KiraClientConstants.JOBDATAMAP_KEY_ARGUMENTS);
      if (null != arguments) {
        argumentsAsJsonArrayString = JSON.toJSONString(arguments);
      }
    }

    if (!isInvokeTarget) {
      if (StringUtils.isBlank(targetMethod)) {
        throw new ValidationException(
            "isInvokeTarget=false. targetMethod should not be blank if you want to invoke service of yourself.");
      }
      if (StringUtils.isBlank(targetMethodArgTypes)) {
        throw new ValidationException(
            "isInvokeTarget=false. targetMethodArgTypes should not be blank if you want to invoke service of yourself. The targetMethodArgTypes should be [] for method which has no params.");
      }
    }

    boolean isInvokeMyself = isInvokeTarget && StringUtils.equals(appId, targetAppId) && StringUtils
        .equals(triggerBeanName, targetTriggerId);

    if (isInvokeMyself) {
      if (StringUtils.isBlank(targetMethod)) {
        throw new ValidationException(
            "isInvokeMyself=true. targetMethod should not be blank if you want to invoke service of yourself.");
      }
      if (StringUtils.isBlank(targetMethodArgTypes)) {
        throw new ValidationException(
            "isInvokeMyself=true. targetMethodArgTypes should not be blank if you want to invoke service of yourself. The targetMethodArgTypes should be [] for method which has no params.");
      }
    }

    if (isInvokeTarget && (!isInvokeMyself)) {
      triggerMetaDataDetail.setTargetMethod(null);
      triggerMetaDataDetail.setTargetMethodArgTypes(null);
    } else {
      triggerMetaDataDetail.setTargetMethod(targetMethod);
      triggerMetaDataDetail.setTargetMethodArgTypes(targetMethodArgTypes);
    }

    if (StringUtils.isBlank(argumentsAsJsonArrayString)) {
      throw new ValidationException(
          "argumentsAsJsonArrayString should not be blank . The argumentsAsJsonArrayString should be [] for method which has no params.");
    }
    triggerMetaDataDetail.setArgumentsAsJsonArrayString(argumentsAsJsonArrayString);

    Boolean concurrent = Boolean.valueOf(yhdTriggerBean.isConcurrent());
    triggerMetaDataDetail.setConcurrent(concurrent);

    String triggerType = null;
    Long startDelay = null;
    Integer repeatCount = null;
    Long repeatInterval = null;
    String cronExpression = null;
    if (yhdTriggerBean instanceof IYHDSimpleTriggerBean) {
      triggerType = TriggerTypeEnum.SIMPLETRIGGER.getName();
      startDelay = ((IYHDSimpleTriggerBean) yhdTriggerBean).getStartDelaySet();
      repeatCount = ((IYHDSimpleTriggerBean) yhdTriggerBean).getRepeatCountSet();
      repeatInterval = ((IYHDSimpleTriggerBean) yhdTriggerBean).getRepeatIntervalSet();
      if (null == repeatInterval) {
        throw new ValidationException("repeatInterval should not be null.");
      }
    } else if (yhdTriggerBean instanceof IYHDCronTriggerBean) {
      triggerType = TriggerTypeEnum.CRONTRIGGER.getName();
      cronExpression = ((IYHDCronTriggerBean) yhdTriggerBean).getCronExpression();
      if (StringUtils.isBlank(cronExpression)) {
        throw new ValidationException("cronExpression should not be blank.");
      }
    }
    triggerMetaDataDetail.setTriggerType(triggerType);

    Date startTimeSet = yhdTriggerBean.getStartTimeSet();
    Date endTimeSet = yhdTriggerBean.getEndTimeSet();
    if (null != startTimeSet && null != endTimeSet) {
      if (startTimeSet.after(endTimeSet)) {
        String message = "The endTimeSet is before startTimeSet. startTimeSet=" + KiraCommonUtils
            .getDateAsStringToMsPrecision(startTimeSet) + " and endTimeSet=" + KiraCommonUtils
            .getDateAsStringToMsPrecision(endTimeSet) + " and appId=" + appId
            + " and triggerBeanName=" + triggerBeanName;
        logger.warn(message);
      }
    }
    triggerMetaDataDetail.setStartTime(startTimeSet);
    triggerMetaDataDetail.setEndTime(endTimeSet);

    triggerMetaDataDetail.setStartDelay(startDelay);
    triggerMetaDataDetail.setRepeatCount(repeatCount);
    triggerMetaDataDetail.setRepeatInterval(repeatInterval);
    triggerMetaDataDetail.setCronExpression(cronExpression);

    String description = yhdTriggerBean.getDescription();
    if (StringUtils.isBlank(description)) {
      throw new ValidationException(
          "The description should not be blank for trigger. triggerBeanName=" + triggerBeanName);
    }
    triggerMetaDataDetail.setDescription(description);
    if (yhdTriggerBean instanceof Trigger) {
      int misfireInstruction = ((Trigger) yhdTriggerBean).getMisfireInstruction();
      triggerMetaDataDetail.setMisfireInstruction(Integer.valueOf(misfireInstruction));
    }
    Boolean asynchronous = Boolean.valueOf(yhdTriggerBean.isAsynchronous());
    triggerMetaDataDetail.setAsynchronous(asynchronous);
    Boolean onlyRunOnSingleProcess = Boolean.valueOf(yhdTriggerBean.isOnlyRunOnSingleProcess());
    triggerMetaDataDetail.setOnlyRunOnSingleProcess(onlyRunOnSingleProcess);
    String finalLocationsToRunJob = null;
    String locationsToRunJobForAllTriggers = KiraClientDataCenter
        .getLocationsToRunJobForAllTriggers();
    String locationsToRunJobForAllTriggersOfThisScheduler = yhdSchedulerFactoryBean
        .getLocationsToRunJobForAllTriggersOfThisScheduler();
    String locationsToRunJob = yhdTriggerBean.getLocationsToRunJob();
    if (StringUtils.isNotBlank(locationsToRunJob)) {
      //Use special string empty to keep empty and ignore the value of locationsToRunJobForAllTriggers and locationsToRunJobForAllTriggersOfThisScheduler
      if (KiraClientConstants.STRING_EMPTY.equalsIgnoreCase(locationsToRunJob)) {
        finalLocationsToRunJob = null;
      } else {
        finalLocationsToRunJob = locationsToRunJob.trim();
      }
    } else {
      finalLocationsToRunJob =
          (null == locationsToRunJobForAllTriggers) ? null : locationsToRunJobForAllTriggers.trim();
      finalLocationsToRunJob = (null == locationsToRunJobForAllTriggersOfThisScheduler) ? null
          : locationsToRunJobForAllTriggersOfThisScheduler.trim();
    }
    boolean limitToSpecifiedLocations = yhdTriggerBean.isLimitToSpecifiedLocations();
    triggerMetaDataDetail.setLimitToSpecifiedLocations(Boolean.valueOf(limitToSpecifiedLocations));

    triggerMetaDataDetail.setLocationsToRunJob(finalLocationsToRunJob);
    Boolean scheduledLocally = Boolean.valueOf(yhdTriggerBean.isScheduledLocally());
    triggerMetaDataDetail.setScheduledLocally(scheduledLocally);
    Boolean disabled = Boolean.valueOf(yhdTriggerBean.isDisabled());
    triggerMetaDataDetail.setDisabled(disabled);

    Boolean requestsRecovery = yhdTriggerBean.isRequestsRecovery();
    triggerMetaDataDetail.setRequestsRecovery(requestsRecovery);

    Long runTimeThreshold = yhdTriggerBean.getRunTimeThreshold();
    if (null != runTimeThreshold) {
      if (runTimeThreshold.longValue() <= 0) {
        throw new ValidationException(
            "The runTimeThreshold must be > 0 . Now runTimeThreshold is " + runTimeThreshold);
      }
    }
    triggerMetaDataDetail.setRunTimeThreshold(runTimeThreshold);

    Boolean copyFromMasterToSlaveZone = Boolean
        .valueOf(yhdTriggerBean.isCopyFromMasterToSlaveZone());
    triggerMetaDataDetail.setCopyFromMasterToSlaveZone(copyFromMasterToSlaveZone);

    Boolean onlyScheduledInMasterZone = Boolean
        .valueOf(yhdTriggerBean.isOnlyScheduledInMasterZone());
    triggerMetaDataDetail.setOnlyScheduledInMasterZone(onlyScheduledInMasterZone);

    // validate job dispatch time out input
    Long dispatchTimeout = yhdTriggerBean.getJobDispatchTimeout();
    boolean isDispatchTimeoutEnabled = yhdTriggerBean.isJobDispatchTimeoutEnabled();
    triggerMetaDataDetail.setJobDispatchTimeoutEnabled(new Boolean(isDispatchTimeoutEnabled));
    if (isDispatchTimeoutEnabled) {
      if (dispatchTimeout == null) {
        throw new ValidationException("The jobDispatchTimeout must be specified in milliseconds.");
      }

      if (dispatchTimeout.longValue() < KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT) {
        throw new ValidationException("The jobDispatchTimeout must be >= "
            + KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT
            + " ms. Now jobDispatchTimeout is " + dispatchTimeout + " ms");
      }
    }
    triggerMetaDataDetail.setJobDispatchTimeout(dispatchTimeout);

    String shellPath = yhdTriggerBean.getRunShellPath();
    String jobType = yhdTriggerBean.getJobType();

    if (StringUtils.isBlank(shellPath) && StringUtils.isBlank(jobType)) {
      triggerMetaDataDetail.setJobType(JobTypeEnum.JAVAJOB.getName());
    } else if (StringUtils.isNotBlank(shellPath) && StringUtils.isNotBlank(jobType)) {
      triggerMetaDataDetail.setJobType(JobTypeEnum.SHELLJOB.getName());
      triggerMetaDataDetail.setRunShellPath(shellPath);
    }

    return triggerMetaDataDetail;
  }

  @Override
  public void handleForKiraClientConfig(KiraClientConfig kiraClientConfig) {
    if (kiraClientConfig.isAppCenter()) {
      Thread handleForAppCenterThread = new Thread(new Runnable() {
        public void run() {
          HandleResult handleResult = handleForAppCenter();
          if (!KiraCommonConstants.RESULT_CODE_SUCCESS.equals(handleResult.getResultCode())) {
            throw new RuntimeException(
                "Failed to handle for appCenter. reason=" + handleResult.getResultData());
          }
        }
      });
      handleForAppCenterThread.start();
    }

    KiraClientRegisterContextData kiraClientRegisterContextData = new KiraClientRegisterContextData();
    kiraClientRegisterContextData.setEnvironment(KiraClientDataCenter.getLocalProcessEnvironment());
    KiraClientRegisterData kiraClientRegisterData = checkAndGetKiraClientRegisterData(
        kiraClientConfig);
    kiraClientRegisterContextData.setKiraClientRegisterData(kiraClientRegisterData);
    KiraClientDataCenter.addKiraClientRegisterContextData(kiraClientRegisterContextData);
    KiraClientRegisterContextDataHandler.getKiraClientRegisterContextDataHandler()
        .handleKiraClientRegisterContextData(kiraClientRegisterContextData);
  }

  private HandleResult handleForAppCenter() {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionCaught = null;
    try {
      if (!zkClient.exists(KiraCommonConstants.ZK_PATH_APPCENTERS)) {
        zkClient.createPersistent(KiraCommonConstants.ZK_PATH_APPCENTERS, true);
      }

      CentralScheduleServiceExporter centralScheduleServiceExporter = KiraClientDataCenter
          .getCentralScheduleServiceExporter();
      if (null != centralScheduleServiceExporter) {
        ServiceProfile serviceProfile = KiraCommonUtils
            .getServiceProfile(centralScheduleServiceExporter);
        if (null != serviceProfile) {
          String appCenterZNodePath = getAppCenterZNodePath(serviceProfile);
          if (StringUtils.isNotBlank(appCenterZNodePath)) {
            if (zkClient.exists(appCenterZNodePath)) {
              //need to recreate for it may be the dirty znode.
              zkClient.delete(appCenterZNodePath);
            }
            String appId = KiraUtil.appId();
            String serviceUrl = serviceProfile.getServiceUrl();
            String host = serviceProfile.getHostIp();
            int port = serviceProfile.getPort();
            AppCenterZNodeData appCenterZNodeData = new AppCenterZNodeData(appId, host, port,
                serviceUrl);
            zkClient.createEphemeral(appCenterZNodePath, appCenterZNodeData);

            resultCode = KiraCommonConstants.RESULT_CODE_SUCCESS;
          } else {
            resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
            resultData = "Got the appCenterZNodePath which is blank.";
          }
        } else {
          resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
          resultData = "Got the serviceProfile which is null.";
        }
      } else {
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
        resultData = "Got the centralScheduleServiceExporter which is null.";
      }
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs when handleForAppCenter", e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData = "Exception occurs on handleForAppCenter. exceptionDesc=" + exceptionDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  private String getAppCenterZNodePath(ServiceProfile serviceProfile) throws Exception {
    String returnValue = null;
    if (null != serviceProfile) {
      String host = serviceProfile.getHostIp();
      int port = serviceProfile.getPort();
      StringBuilder nodeNameSB = new StringBuilder().append(host)
          .append(KiraCommonConstants.COLON_DELIMITER).append(port);
      String appCenterZNodeName = nodeNameSB.toString();
      returnValue = KiraCommonConstants.ZK_PATH_APPCENTERS + KiraCommonConstants.ZNODE_NAME_PREFIX
          + appCenterZNodeName;
    } else {
      logger.warn("Got the serviceProfile which is null.");
    }

    return returnValue;
  }

  private KiraClientRegisterData checkAndGetKiraClientRegisterData(
      KiraClientConfig kiraClientConfig) {
    KiraClientRegisterData returnValue = new KiraClientRegisterData();
    String appId = KiraUtil.appId();
    if (StringUtils.isBlank(appId)) {
      logger.warn("appId is not blank!");
      throw new ValidationException("appId should not be blank.");
    }
    returnValue.setAppId(appId);
    String host = SystemUtil.getLocalhostIp();
    returnValue.setHost(host);
    Integer pid = KiraClientDataCenter.getLocalProcessEnvironment().getPid();
    returnValue.setPid(pid);
    //ccjtodo: in phase 2 ,it need to set applicationId for child process
    String kiraClientVersion = KiraClientDataCenter.getKiraclientversion();
    returnValue.setKiraClientVersion(kiraClientVersion);
    boolean visibilityLimited = kiraClientConfig.isVisibilityLimited();
    returnValue.setVisibilityLimited(visibilityLimited);
    String visibleForUsers = kiraClientConfig.getVisibleForUsers();
    returnValue.setVisibleForUsers(visibleForUsers);
    boolean sendAlarmEmail = kiraClientConfig.isSendAlarmEmail();
    returnValue.setSendAlarmEmail(sendAlarmEmail);
    String emailsToReceiveAlarm = kiraClientConfig.getEmailsToReceiveAlarm();
    returnValue.setEmailsToReceiveAlarm(emailsToReceiveAlarm);
    boolean sendAlarmSMS = kiraClientConfig.isSendAlarmSMS();
    returnValue.setSendAlarmSMS(sendAlarmSMS);
    String phoneNumbersToReceiveAlarmSMS = kiraClientConfig.getPhoneNumbersToReceiveAlarmSMS();
    returnValue.setPhoneNumbersToReceiveAlarmSMS(phoneNumbersToReceiveAlarmSMS);
    boolean keepKiraClientConfigDataOnKiraServerUnchanged = kiraClientConfig
        .isKeepKiraClientConfigDataOnKiraServerUnchanged();
    returnValue.setKeepKiraClientConfigDataOnKiraServerUnchanged(
        keepKiraClientConfigDataOnKiraServerUnchanged);
    return returnValue;
  }

  @Override
  public void handleOthers() throws Exception {
    boolean isWorkWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!isWorkWithoutKira) {
      zkClient.subscribeStateChanges(new IZkStateListener() {

        @Override
        public void handleStateChanged(KeeperState state) throws Exception {
        }

        @Override
        public void handleNewSession() throws Exception {
          boolean isWorkWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
          boolean isAppCenter = KiraClientDataCenter.isAppCenter();
          try {
            logger.warn(
                "start handleForAppCenter() for handleNewSession. isWorkWithoutKira={} and isAppCenter={}",
                isWorkWithoutKira, isAppCenter);
            if (isAppCenter) {
              HandleResult handleResult = handleForAppCenter();
              logger.warn("handleForAppCenter return handleResult={}",
                  KiraCommonUtils.toString(handleResult));
            }
          } finally {
            logger.warn(
                "end handleForAppCenter() for handleNewSession. isWorkWithoutKira={} and isAppCenter={}",
                isWorkWithoutKira, isAppCenter);
          }
        }
      });
    }
  }
}
