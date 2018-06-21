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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxyFactory;
import com.yihaodian.architecture.kira.common.InternalConstants;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.TriggerTypeEnum;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.criteria.Criteria;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import com.yihaodian.architecture.kira.schedule.time.trigger.AbstractTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraCronTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraSimpleTimerTrigger;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerUtils {

  private static final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private static Logger logger = LoggerFactory.getLogger(KiraManagerUtils.class);
  //ccjtodo: need to remove if needed.
  private static volatile String privateUserNames =
      LoadProertesContainer.provider().getProperty(InternalConstants.KIRA_PRIVATE_USERNAME, "");
  //ccjtodo: need to remove if needed.
  private static volatile String privateEmails =
      LoadProertesContainer.provider().getProperty(InternalConstants.KIRA_PRIVATE_EMAILS, "");
  //ccjtodo: need to remove if needed.
  private static volatile String privatePhoneNumbers =
      LoadProertesContainer.provider()
          .getProperty(InternalConstants.KIRA_PRIVATE_PHONE_NUMBERS, "");
  //ccjtodo: need to remove if needed.
  private static volatile String adminUserNames =
      LoadProertesContainer.provider().getProperty(InternalConstants.KIRA_ADMIN_USERNAMES, "");
  //ccjtodo: need to remove if needed.
  private static volatile String adminEmails =
      LoadProertesContainer.provider().getProperty(InternalConstants.KIRA_ADMIN_EMAILS, "");
  //ccjtodo: need to remove if needed.
  private static volatile String adminPhoneNumbers =
      LoadProertesContainer.provider().getProperty(InternalConstants.KIRA_ADMIN_PHONE_NUMBERS, "");
  //ccjtodo: need to remove if needed.
  private static volatile String healthEventReceiverEmails =
      LoadProertesContainer.provider()
          .getProperty(InternalConstants.KIRA_HEALTH_EVENT_RECEIVER_EMAILS, "");
  //ccjtodo: need to remove if needed.
  private static volatile String healthEventReceiverPhoneNumbers =
      LoadProertesContainer.provider()
          .getProperty(InternalConstants.KIRA_HEALTH_EVENT_RECEIVER_PHONE_NUMBERS, "");
  //ccjtodo: need to remove if needed.
  private static volatile boolean needArchiveJobRuntimeData = false;
  //ccjtodo: need to remove if needed.
  private static volatile int minutesToKeepJobRuntimeData;
  //ccjtodo: need to remove if needed.
  private static volatile int minutesPerTimeToHandleJobRuntimeData;
  //ccjtodo: need to remove if needed.
  private static volatile long sleepTimeBeforeRunNextTaskInMilliseconds = 60000L; //1 minute
  //ccjtodo: need to remove if needed.
  private static volatile long timeOutPerTaskInMilliseconds = 60000L; //1 minute
  //ccjtodo: need to remove if needed.
  private static volatile int jobTimeoutHandleFailedMaxCount = 30;

  public KiraManagerUtils() {
    // TODO Auto-generated constructor stub
  }

  public static String getPrivateEmails() {
    return privateEmails;
  }

  public static void setPrivateEmails(String privateEmails) {
    KiraManagerUtils.privateEmails = privateEmails;
  }

  public static String getPrivatePhoneNumbers() {
    return privatePhoneNumbers;
  }

  public static void setPrivatePhoneNumbers(String privatePhoneNumbers) {
    KiraManagerUtils.privatePhoneNumbers = privatePhoneNumbers;
  }

  public static String getAdminUserNames() {
    return adminUserNames;
  }

  public static void setAdminUserNames(String adminUserNames) {
    List<String> adminUserNamekList = KiraCommonUtils
        .getStringListByDelmiter(adminUserNames, KiraCommonConstants.COMMA_DELIMITER);
    LinkedHashSet<String> adminUserNameListAsSet = new LinkedHashSet<String>(adminUserNamekList);

    List<String> privateUserNameList = KiraCommonUtils
        .getStringListByDelmiter(privateUserNames, KiraCommonConstants.COMMA_DELIMITER);
    LinkedHashSet<String> privateUserNameListAsSet = new LinkedHashSet<String>(privateUserNameList);

    //Always need to add privateUserNames for safety reason.
    adminUserNameListAsSet.addAll(privateUserNameListAsSet);

    adminUserNames = StringUtils.join(adminUserNameListAsSet, KiraCommonConstants.COMMA_DELIMITER);

    KiraManagerUtils.adminUserNames = adminUserNames;
    if (null != logger) {
      logger.warn("setAdminUserNames called. adminUserNames={}", KiraManagerUtils.adminUserNames);
    }
  }

  public static boolean isAdminUser(String userName) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(KiraManagerUtils.adminUserNames)) {
      if (adminUserNames.contains(userName)) {
        returnValue = true;
      }
    }

    return returnValue;
  }

  public static String getAdminEmails() {
    return adminEmails;
  }

  public static void setAdminEmails(String adminEmails) {
    KiraManagerUtils.adminEmails = adminEmails;
  }

  public static String getAdminPhoneNumbers() {
    return adminPhoneNumbers;
  }

  public static void setAdminPhoneNumbers(String adminPhoneNumbers) {
    KiraManagerUtils.adminPhoneNumbers = adminPhoneNumbers;
  }

  public static String getHealthEventReceiverEmails() {
    return healthEventReceiverEmails;
  }

  public static void setHealthEventReceiverEmails(String healthEventReceiverEmails) {
    KiraManagerUtils.healthEventReceiverEmails = healthEventReceiverEmails;
  }

  public static String getHealthEventReceiverPhoneNumbers() {
    return healthEventReceiverPhoneNumbers;
  }

  public static void setHealthEventReceiverPhoneNumbers(
      String healthEventReceiverPhoneNumbers) {
    KiraManagerUtils.healthEventReceiverPhoneNumbers = healthEventReceiverPhoneNumbers;
  }

  public static boolean isNeedArchiveJobRuntimeData() {
    return needArchiveJobRuntimeData;
  }

  public static void setNeedArchiveJobRuntimeData(
      boolean needArchiveJobRuntimeData) {
    KiraManagerUtils.needArchiveJobRuntimeData = needArchiveJobRuntimeData;
  }

  public static int getMinutesToKeepJobRuntimeData() {
    return minutesToKeepJobRuntimeData;
  }

  public static void setMinutesToKeepJobRuntimeData(
      int minutesToKeepJobRuntimeData) {
    KiraManagerUtils.minutesToKeepJobRuntimeData = minutesToKeepJobRuntimeData;
  }

  public static int getMinutesPerTimeToHandleJobRuntimeData() {
    return minutesPerTimeToHandleJobRuntimeData;
  }

  public static void setMinutesPerTimeToHandleJobRuntimeData(
      int minutesPerTimeToHandleJobRuntimeData) {
    KiraManagerUtils.minutesPerTimeToHandleJobRuntimeData = minutesPerTimeToHandleJobRuntimeData;
  }

  public static long getSleepTimeBeforeRunNextTaskInMilliseconds() {
    return sleepTimeBeforeRunNextTaskInMilliseconds;
  }

  public static void setSleepTimeBeforeRunNextTaskInMilliseconds(
      long sleepTimeBeforeRunNextTaskInMilliseconds) {
    KiraManagerUtils.sleepTimeBeforeRunNextTaskInMilliseconds = sleepTimeBeforeRunNextTaskInMilliseconds;
  }

  public static long getTimeOutPerTaskInMilliseconds() {
    return timeOutPerTaskInMilliseconds;
  }

  public static void setTimeOutPerTaskInMilliseconds(
      long timeOutPerTaskInMilliseconds) {
    KiraManagerUtils.timeOutPerTaskInMilliseconds = timeOutPerTaskInMilliseconds;
  }

  public static int getJobTimeoutHandleFailedMaxCount() {
    return jobTimeoutHandleFailedMaxCount;
  }

  public static void setJobTimeoutHandleFailedMaxCount(
      int jobTimeoutHandleFailedMaxCount) {
    KiraManagerUtils.jobTimeoutHandleFailedMaxCount = jobTimeoutHandleFailedMaxCount;
  }

  public static List<TriggerEnvironment> getTriggerEnvironmentList(String poolId, String triggerId,
      boolean onlyReturnRunningAndAvailable) {
    List<TriggerEnvironment> returnValue = new ArrayList<TriggerEnvironment>();
    try {
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(poolId, triggerId);
      String triggerEnvironmentsZKPath =
          triggerZNodeZKPath + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
      List<String> triggerEnvironmentChildPathList = null;
      if (zkClient.exists(triggerEnvironmentsZKPath)) {
        triggerEnvironmentChildPathList = zkClient.getChildren(triggerEnvironmentsZKPath);
      }
      if (!CollectionUtils.isEmpty(triggerEnvironmentChildPathList)) {
        TriggerEnvironment triggerEnvironment = null;
        String triggerEnvironmentZKFullPath = null;
        String centralScheduleServiceZKParentPath = null;
        for (String triggerEnvironmentChildZNodeName : triggerEnvironmentChildPathList) {
          triggerEnvironment = new TriggerEnvironment();
          triggerEnvironment.setPoolId(poolId);
          triggerEnvironment.setTriggerId(triggerId);
          triggerEnvironmentZKFullPath = KiraUtil
              .getChildFullPath(triggerEnvironmentsZKPath, triggerEnvironmentChildZNodeName);
          triggerEnvironment.setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);
          centralScheduleServiceZKParentPath = zkClient
              .readData(triggerEnvironmentZKFullPath, true);
          if (StringUtils.isNotBlank(centralScheduleServiceZKParentPath)) {
            if (centralScheduleServiceZKParentPath.contains("//")) {
              logger.warn(
                  "The centralScheduleServiceZKParentPath contains // which means the serviceAppName may be blank and it is not valid path for zk, so ignore it. poolId={} and triggerId={} and centralScheduleServiceZKParentPath={}",
                  poolId, triggerId, centralScheduleServiceZKParentPath);
            } else {
              triggerEnvironment
                  .setCentralScheduleServiceZKParentPath(centralScheduleServiceZKParentPath);
              if (zkClient.exists(centralScheduleServiceZKParentPath)) {
                List<String> centralScheduleServiceChildPathList = zkClient
                    .getChildren(centralScheduleServiceZKParentPath);
                if (!CollectionUtils.isEmpty(centralScheduleServiceChildPathList)) {
                  List<ServiceProfile> centralScheduleServiceProfileList = new ArrayList<ServiceProfile>();
                  List<String> centralScheduleServiceUrlList = new ArrayList<String>();
                  ServiceProfile centralScheduleServiceProfile = null;
                  String centralScheduleServiceUrl = null;
                  for (String centralScheduleServiceChildPath : centralScheduleServiceChildPathList) {
                    String centralScheduleServiceFullPath = KiraUtil
                        .getChildFullPath(centralScheduleServiceZKParentPath,
                            centralScheduleServiceChildPath);
                    centralScheduleServiceProfile = zkClient
                        .readData(centralScheduleServiceFullPath, true);
                    if (null != centralScheduleServiceProfile) {
                      boolean isAvailable = centralScheduleServiceProfile.isAvailable();
                      if (!isAvailable && onlyReturnRunningAndAvailable) {
                        logger.info(
                            "The centralScheduleService is regarded as not available on centralScheduleServiceFullPath={} for {} and onlyReturnAvailable now.",
                            centralScheduleServiceFullPath,
                            "poolId=" + poolId + ",triggerId=" + triggerId);
                      } else {
                        centralScheduleServiceProfileList.add(centralScheduleServiceProfile);
                        centralScheduleServiceUrl = centralScheduleServiceProfile.getServiceUrl();
                        centralScheduleServiceUrlList.add(centralScheduleServiceUrl);
                      }
                    } else {
                      logger.warn(
                          "No centralScheduleServiceProfile data on centralScheduleServiceFullPath={} for {}",
                          centralScheduleServiceFullPath,
                          "poolId=" + poolId + ",triggerId=" + triggerId);
                    }
                  }
                  triggerEnvironment
                      .setCentralScheduleServiceProfileList(centralScheduleServiceProfileList);
                  triggerEnvironment
                      .setCentralScheduleServiceUrlList(centralScheduleServiceUrlList);
                } else {
                  logger.info(
                      "There is no running centralScheduleService centralScheduleServiceZKParentPath={} for {}",
                      centralScheduleServiceZKParentPath,
                      "poolId=" + poolId + ",triggerId=" + triggerId);
                }
              } else {
                logger.info(
                    "ZKPath do not exist: centralScheduleServiceZKParentPath={} and this path data is read from triggerEnvironmentZKFullPath={} for {}",
                    centralScheduleServiceZKParentPath, triggerEnvironmentZKFullPath,
                    "poolId=" + poolId + ",triggerId=" + triggerId);
              }
            }
          } else {
            logger.warn(
                "No centralScheduleServiceZKParentPath data on triggerEnvironmentZKFullPath={} for {}",
                triggerEnvironmentZKFullPath, "poolId=" + poolId + ",triggerId=" + triggerId);
          }
          boolean isNotAvailable =
              CollectionUtils.isEmpty(triggerEnvironment.getCentralScheduleServiceProfileList())
                  || CollectionUtils.isEmpty(triggerEnvironment.getCentralScheduleServiceUrlList());
          if (isNotAvailable && onlyReturnRunningAndAvailable) {
            logger.info(
                "No running and available centralScheduleService found for triggerEnvironmentZKFullPath={} for {}",
                triggerEnvironmentZKFullPath, "poolId=" + poolId + ",triggerId=" + triggerId);
          } else {
            returnValue.add(triggerEnvironment);
          }
        }
      } else {
        logger.info("No available environments for {}",
            "poolId=" + poolId + ",triggerId=" + triggerId);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when getTriggerEnvironmentList for poolId=" + poolId + ",triggerId="
              + triggerId + ",onlyReturnRunningAndAvailable=" + onlyReturnRunningAndAvailable, e);
    }
    return returnValue;
  }

  public static Set<String> getRandomSelectedServiceUrlSet(List<String> availableServiceUrlList,
      List<String> secondaryServiceUrlList, int countExpectedToBeSelected) {
    Set<String> returnValue = new LinkedHashSet<String>();
    if (!CollectionUtils.isEmpty(availableServiceUrlList)) {
      List<String> clonedAvailableServiceUrlList = new LinkedList<String>(
          new LinkedHashSet<String>(availableServiceUrlList));

      int totalCount = clonedAvailableServiceUrlList.size();
      if (totalCount == countExpectedToBeSelected) {
        returnValue.addAll(clonedAvailableServiceUrlList);
      } else if (totalCount < countExpectedToBeSelected) {
        returnValue.addAll(clonedAvailableServiceUrlList);
        logger.warn("totalCount={} which is less than countExpectedToBeSelected={}", totalCount,
            countExpectedToBeSelected);
      } else {
        //need to filter out the ones which is not included in availableServiceUrlList
        List<String> clonedSecondaryServiceUrlList = new LinkedList<String>();
        if (null != secondaryServiceUrlList) {
          clonedSecondaryServiceUrlList = new LinkedList<String>(
              new LinkedHashSet<String>(secondaryServiceUrlList));
        }
        clonedSecondaryServiceUrlList = (List<String>) CollectionUtils
            .retainAll(clonedSecondaryServiceUrlList, clonedAvailableServiceUrlList);

        List<String> preferServiceUrlList = clonedAvailableServiceUrlList;
        if (!CollectionUtils.isEmpty(clonedSecondaryServiceUrlList)) {
          preferServiceUrlList = (List<String>) CollectionUtils
              .subtract(clonedAvailableServiceUrlList, clonedSecondaryServiceUrlList);
        }

        int preferCount = preferServiceUrlList.size();
        if (preferCount == countExpectedToBeSelected) {
          returnValue.addAll(preferServiceUrlList);
        } else if (preferCount < countExpectedToBeSelected) {
          returnValue.addAll(preferServiceUrlList);
          int remainCountExpectedToBeSelected = countExpectedToBeSelected - preferCount;
          if (remainCountExpectedToBeSelected > 0) {
            if (!CollectionUtils.isEmpty(clonedSecondaryServiceUrlList)) {
              //add remains from secondary
              Collections.shuffle(clonedSecondaryServiceUrlList);
              for (int i = 0, secondaryServiceSize = clonedSecondaryServiceUrlList.size();
                  i < secondaryServiceSize; i++) {
                String oneSecondaryServiceUrl = clonedSecondaryServiceUrlList.get(i);
                returnValue.add(oneSecondaryServiceUrl);

                remainCountExpectedToBeSelected--;
                if (remainCountExpectedToBeSelected <= 0) {
                  break;
                }
              }
            }

            if (remainCountExpectedToBeSelected > 0) {
              logger.warn(
                  "remainCountExpectedToBeSelected={} and totalCount={} and countExpectedToBeSelected={} and preferCount={}",
                  remainCountExpectedToBeSelected, totalCount, countExpectedToBeSelected,
                  preferCount);
            }
          }
        } else if (preferCount > countExpectedToBeSelected) {
          int remainCountExpectedToBeSelected = countExpectedToBeSelected;
          if (remainCountExpectedToBeSelected > 0) {
            List<String> clonedPreferServiceUrlList = new LinkedList<String>(preferServiceUrlList);
            Collections.shuffle(clonedPreferServiceUrlList);
            for (int i = 0, preferServiceUrlSize = clonedPreferServiceUrlList.size();
                i < preferServiceUrlSize; i++) {
              String onePreferServiceUrl = clonedPreferServiceUrlList.get(i);
              returnValue.add(onePreferServiceUrl);

              remainCountExpectedToBeSelected--;
              if (remainCountExpectedToBeSelected <= 0) {
                break;
              }
            }

            if (remainCountExpectedToBeSelected > 0) {
              logger.warn(
                  "remainCountExpectedToBeSelected={} and totalCount={} and countExpectedToBeSelected={} and preferCount={}",
                  remainCountExpectedToBeSelected, totalCount, countExpectedToBeSelected,
                  preferCount);
            }
          }
        }
      }
    }

    return returnValue;
  }

  public static List<String> getRunningAndAvailableCentralScheduleServiceUrlList(String poolId,
      String locationsToRunJob, Boolean onlyRunOnSingleProcess,
      List<TriggerEnvironment> triggerEnvironmentList, Boolean limitToSpecifiedLocations) {
    Set<String> returnValueAsSet = new LinkedHashSet<String>();

    List<String> locationsToRunJobAsList = KiraCommonUtils
        .getStringListByDelmiter(locationsToRunJob, KiraCommonConstants.COMMA_DELIMITER);
    Map<String, String> locationValidServiceUrlMap = new HashMap<String, String>();
    outerLoop:
    for (TriggerEnvironment triggerEnvironment : triggerEnvironmentList) {
      List<ServiceProfile> centralScheduleServiceProfileList = triggerEnvironment
          .getCentralScheduleServiceProfileList();
      if (null != centralScheduleServiceProfileList) {
        String serviceUrl = null;
        for (ServiceProfile serviceProfile : centralScheduleServiceProfileList) {
          boolean available = serviceProfile.isAvailable();
          if (available) {
            serviceUrl = serviceProfile.getServiceUrl();
            if (!StringUtils.isBlank(serviceUrl)) {
              String location = new StringBuilder(serviceProfile.getHostIp()).append(":")
                  .append(serviceProfile.getPort()).toString();
              locationValidServiceUrlMap.put(location, serviceUrl);

              if (onlyRunOnSingleProcess) {
                if (CollectionUtils.isEmpty(locationsToRunJobAsList)) {
                  //returnValueAsSet.add(serviceUrl);
                  //break outerLoop;
                } else {
                  if (1 == locationsToRunJobAsList.size()) {
                    //if only has one location specified when found no need to continue.
                    boolean isServiceUrlMatchLocationList = KiraManagerUtils
                        .isServiceUrlMatchLocationList(serviceUrl, locationsToRunJobAsList);
                    if (isServiceUrlMatchLocationList) {
                      returnValueAsSet.add(serviceUrl);
                      break outerLoop;
                    }
                  }
                }
              } else {
                if (CollectionUtils.isEmpty(locationsToRunJobAsList)) {
                  returnValueAsSet.add(serviceUrl);
                } else {
                  boolean isServiceUrlMatchLocationList = KiraManagerUtils
                      .isServiceUrlMatchLocationList(serviceUrl, locationsToRunJobAsList);
                  if (isServiceUrlMatchLocationList) {
                    returnValueAsSet.add(serviceUrl);
                  }
                }
              }
            }
          }
        }
      }
    }

    if (onlyRunOnSingleProcess && returnValueAsSet.isEmpty()) {
      Set<String> allValidLocationSet = locationValidServiceUrlMap.keySet();
      //select the first one from locationsToRunJob which is the most preferred one
      if (CollectionUtils.isNotEmpty(locationsToRunJobAsList) && CollectionUtils
          .isNotEmpty(allValidLocationSet)) {
        String selectedServiceUrl = null;
        String firstFoundValidServiceUrl = null;

        for (String oneLocationToRunJob : locationsToRunJobAsList) {
          if (allValidLocationSet.contains(oneLocationToRunJob)) {
            String foundValidServiceUrl = locationValidServiceUrlMap.get(oneLocationToRunJob);
            if (StringUtils.isBlank(firstFoundValidServiceUrl)) {
              firstFoundValidServiceUrl = foundValidServiceUrl;
            }
            List<String> lastSelectedServiceUrlsToRunJob = KiraManagerDataCenter
                .getLastSelectedServiceUrlsToRunJobByPoolId(poolId);
            if (CollectionUtils.isEmpty(lastSelectedServiceUrlsToRunJob)
                || (CollectionUtils.isNotEmpty(lastSelectedServiceUrlsToRunJob)
                && !lastSelectedServiceUrlsToRunJob.contains(foundValidServiceUrl))
                ) {
              //Found the specified location which is valid and not be used last time.
              selectedServiceUrl = foundValidServiceUrl;
              break;
            }
          }
        }

        if (StringUtils.isBlank(selectedServiceUrl)) {
          if (StringUtils.isNotBlank(firstFoundValidServiceUrl)) {
            selectedServiceUrl = firstFoundValidServiceUrl;
          }
        }

        if (StringUtils.isNotBlank(selectedServiceUrl)) {
          returnValueAsSet.add(selectedServiceUrl.trim());
        }
      }
    }

    Collection<String> allValidServiceUrlSet = locationValidServiceUrlMap.values();
    if (returnValueAsSet.isEmpty() && CollectionUtils.isNotEmpty(allValidServiceUrlSet)) {
      if (limitToSpecifiedLocations && CollectionUtils.isNotEmpty(locationsToRunJobAsList)) {
        logger.info(
            "limitToSpecifiedLocations={} and locationsToRunJob is specified. So no need to try other available locations. poolId={} and locationsToRunJob={} and onlyRunOnSingleProcess={} and allValidServiceUrlSet={}",
            limitToSpecifiedLocations, poolId, locationsToRunJob, onlyRunOnSingleProcess,
            allValidServiceUrlSet);
      } else {
        int countExpectedToBeSelected = 1;
        if (!onlyRunOnSingleProcess) {
          countExpectedToBeSelected = allValidServiceUrlSet.size();
        }
        Set<String> selectedServiceUrlSet = KiraManagerUtils
            .getRandomSelectedServiceUrlSet(new ArrayList<String>(allValidServiceUrlSet),
                KiraManagerDataCenter.getLastSelectedServiceUrlsToRunJobByPoolId(poolId),
                countExpectedToBeSelected);
        returnValueAsSet.addAll(selectedServiceUrlSet);

        if (onlyRunOnSingleProcess) {
          if (StringUtils.isNotBlank(locationsToRunJob)) {
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "Can not find valid serviceUrl by locationsToRunJob. So use the RandomSelected serviceUrl. poolId={} and locationsToRunJob={} and onlyRunOnSingleProcess={} and selectedServiceUrlSet={} and allValidServiceUrlSet={}",
                  poolId, locationsToRunJob, onlyRunOnSingleProcess, selectedServiceUrlSet,
                  allValidServiceUrlSet);
            }
          }
        } else {
          if (StringUtils.isNotBlank(locationsToRunJob)) {
            if (logger.isDebugEnabled()) {
              logger.debug(
                  "Can not find valid serviceUrls by locationsToRunJob. So use the RandomSelected serviceUrls. poolId={} and locationsToRunJob={} and onlyRunOnSingleProcess={} and selectedServiceUrlSet={} and allValidServiceUrlSet={}",
                  poolId, locationsToRunJob, onlyRunOnSingleProcess, selectedServiceUrlSet,
                  allValidServiceUrlSet);
            }
          }
        }
      }
    }

    if (!CollectionUtils.isEmpty(returnValueAsSet)) {
      KiraManagerDataCenter.setLastSelectedServiceUrlsToRunJobForPool(poolId,
          new LinkedList<String>(returnValueAsSet));
    }

    List<String> returnValue = new ArrayList<String>(returnValueAsSet);
    return returnValue;
  }

  private static boolean isServiceUrlMatchLocationList(String serviceUrl,
      List<String> locationsToRunJobAsList) {
    boolean returnValue = false;
    if ((!CollectionUtils.isEmpty(locationsToRunJobAsList)) && StringUtils.isNotBlank(serviceUrl)) {
      for (String oneLocation : locationsToRunJobAsList) {
        if (StringUtils.isNotBlank(oneLocation)) {
          boolean contains = serviceUrl.contains(oneLocation);
          if (contains) {
            returnValue = true;
            break;
          }
        }
      }
    }
    return returnValue;
  }

  public static ICentralScheduleService getCentralScheduleService(String serviceUrl) {
    ICentralScheduleService returnValue = null;
    try {
      if (StringUtils.isNotBlank(serviceUrl)) {
        HedwigHessianProxyFactory proxyFactory = new HedwigHessianProxyFactory();
        proxyFactory.setHessian2Request(true);
        proxyFactory.setHessian2Reply(true);
        proxyFactory.setChunkedPost(false);
        proxyFactory.setOverloadEnabled(false);
        proxyFactory.setReadTimeout(
            KiraServerConstants.CENTRAL_SCHEDULE_SERVICE_HEDWIG_HESSIAN_READTIMEOUT_MILLISECOND);
        proxyFactory.setUser(KiraServerConstants.CENTRAL_SCHEDULE_SERVICE_AUTH_USERNAME);
        proxyFactory.setPassword(KiraServerConstants.CENTRAL_SCHEDULE_SERVICE_AUTH_PASSWORD);
        returnValue = (ICentralScheduleService) proxyFactory
            .create(ICentralScheduleService.class, serviceUrl);
      }
    } catch (Exception e) {
      logger.error("Error occurs when getCentralScheduleService for serviceUrl=" + serviceUrl, e);
    }

    return returnValue;
  }

  public static void updateCriteria(Criteria oldCriteria, Criteria newCriteria) {
    oldCriteria.getPaging().setTotalResults(newCriteria.getPaging().getTotalResults());
  }

  public static List<TriggerMetadataZNodeData> getTriggerMetadataZNodeDataList(
      Collection<TriggerIdentity> triggerIdentityList) {
    List<TriggerMetadataZNodeData> returnValue = new ArrayList<TriggerMetadataZNodeData>();
    try {
      if (!CollectionUtils.isEmpty(triggerIdentityList)) {
        TriggerMetadataZNodeData triggerMetadataZNodeData = null;
        for (TriggerIdentity triggerIdentity : triggerIdentityList) {
          String appId = triggerIdentity.getAppId();
          String triggerId = triggerIdentity.getTriggerId();
          String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
          if (zkClient.exists(triggerZNodeZKPath)) {
            triggerMetadataZNodeData = zkClient.readData(triggerZNodeZKPath, true);
            if (null != triggerMetadataZNodeData) {
              returnValue.add(triggerMetadataZNodeData);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for getTriggerMetadataZNodeDataList. triggerIdentityList=" + KiraCommonUtils
              .toString(triggerIdentityList));
    }

    return returnValue;
  }

  public static String getKiraServerLeaderZNodePath() {
    String kiraServerLeaderZNodePath = null;
    try {
      if (zkClient.exists(KiraServerConstants.ZK_PATH_SCHEDULE_SERVER_LEADER)) {
        List<String> kiraServerLeaderZNodeNameList = zkClient
            .getChildren(KiraServerConstants.ZK_PATH_SCHEDULE_SERVER_LEADER);
        if (!CollectionUtils.isEmpty(kiraServerLeaderZNodeNameList)) {
          String kiraServerLeaderZNodeName = kiraServerLeaderZNodeNameList.get(0);
          kiraServerLeaderZNodePath = KiraUtil
              .getChildFullPath(KiraServerConstants.ZK_PATH_SCHEDULE_SERVER_LEADER,
                  kiraServerLeaderZNodeName);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when getKiraServerLeaderZNodePath.", e);
    }

    return kiraServerLeaderZNodePath;
  }

  public static Date getZeroTimeOfFirstDayOfThisMonth() {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static Date getZeroTimeOfFirstDayOfLastMonth() {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.add(Calendar.MONTH, -1);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static Date getZeroTimeOfToday() {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static String getYearMonthDayPartString(Date date) {
    String returnValue = null;
    returnValue = KiraCommonUtils.getDateAsString(date, "yyyyMMdd");
    return returnValue;
  }

  public static String getYearMonthPartString(Date date) {
    String returnValue = null;
    returnValue = KiraCommonUtils.getDateAsString(date, "yyyyMM");
    return returnValue;
  }

  public static String getYearPartString(Date date) {
    String returnValue = null;
    returnValue = KiraCommonUtils.getDateAsString(date, "yyyy");
    return returnValue;
  }

  public static Date getZeroTimeOfFirstDayOfThatMonthOfDate(Date date) {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static Date getZeroTimeOfFirstDayOfNextMonthOfDate(Date date) {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MONTH, 1);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static List<Date> getZeroTimeOfFirstDayOfMonthBetweenStartTimeAndEndTime(Date startTime,
      Date endTime) {
    List<Date> returnValue = new ArrayList<Date>();
    if (null != startTime && null != endTime) {
      if (startTime.after(endTime)) {
        logger.warn("startTime={} should not be later than endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      } else {
        Set<Date> returnValueAsSet = new LinkedHashSet<Date>();
        Date theZeroTimeOfFirstDayOfThatMonthOfStartTime = KiraManagerUtils
            .getZeroTimeOfFirstDayOfThatMonthOfDate(startTime);
        Date theZeroTimeOfFirstDayOfThatMonthOfEndTime = KiraManagerUtils
            .getZeroTimeOfFirstDayOfThatMonthOfDate(endTime);
        if (theZeroTimeOfFirstDayOfThatMonthOfStartTime
            .before(theZeroTimeOfFirstDayOfThatMonthOfEndTime)) {
          returnValueAsSet.add(theZeroTimeOfFirstDayOfThatMonthOfStartTime);
          Date zeroTimeOfFirstDayOfNextMonthOfStart = KiraManagerUtils
              .getZeroTimeOfFirstDayOfNextMonthOfDate(startTime);
          while (zeroTimeOfFirstDayOfNextMonthOfStart
              .before(theZeroTimeOfFirstDayOfThatMonthOfEndTime)) {
            returnValueAsSet.add(zeroTimeOfFirstDayOfNextMonthOfStart);
            zeroTimeOfFirstDayOfNextMonthOfStart = KiraManagerUtils
                .getZeroTimeOfFirstDayOfNextMonthOfDate(zeroTimeOfFirstDayOfNextMonthOfStart);
          }
          returnValueAsSet.add(theZeroTimeOfFirstDayOfThatMonthOfEndTime);
        } else if (theZeroTimeOfFirstDayOfThatMonthOfStartTime
            .equals(theZeroTimeOfFirstDayOfThatMonthOfEndTime)) {
          returnValueAsSet.add(theZeroTimeOfFirstDayOfThatMonthOfStartTime);
        } else if (theZeroTimeOfFirstDayOfThatMonthOfStartTime
            .after(theZeroTimeOfFirstDayOfThatMonthOfEndTime)) {
          logger.warn(
              "theZeroTimeOfFirstDayOfThatMonthOfStartTime={} should not be later than theZeroTimeOfFirstDayOfThatMonthOfEndTime={}",
              KiraCommonUtils.getDateAsString(theZeroTimeOfFirstDayOfThatMonthOfStartTime),
              KiraCommonUtils.getDateAsString(theZeroTimeOfFirstDayOfThatMonthOfEndTime));
        }

        returnValue = new ArrayList<Date>(returnValueAsSet);
      }
    } else {
      logger.warn("One may be null. startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    return returnValue;
  }

  public static Date getDateAfterAddMilliseconds(Date date, long milliseconds) {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MILLISECOND, (int) milliseconds);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static Date getDateAfterAddDays(Date date, long days) {
    Date returnValue = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, (int) days);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  public static List<TimeInterval> getSplittedTimeIntervalListBySpecialSplitPointList(
      Date startTime, Date endTime, List<Date> specialSplitPointList) {
    List<TimeInterval> returnValue = new ArrayList<TimeInterval>();

    if (null != startTime && null != endTime) {
      if (startTime.after(endTime)) {
        logger.warn("startTime={} should not be later than endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      } else {
        List<Date> inDateList = new ArrayList<Date>();
        for (Date oneDatePoint : specialSplitPointList) {
          if (oneDatePoint.after(startTime) && oneDatePoint.before(endTime)) {
            inDateList.add(oneDatePoint);
          }
        }
        Collections.sort(inDateList);
        Date lastStart = startTime;
        Date lastEnd = endTime;
        for (Date oneDatePoint : inDateList) {
          lastEnd = oneDatePoint;
          TimeInterval timeInterval = new TimeInterval(lastStart, lastEnd);
          returnValue.add(timeInterval);
          lastStart = oneDatePoint;
        }
        if (lastStart.before(endTime)) {
          TimeInterval timeInterval = new TimeInterval(lastStart, endTime);
          returnValue.add(timeInterval);
        }
      }
    } else {
      logger.warn("One may be null. startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }

    return returnValue;
  }

  public static List<TimeInterval> getSplittedTimeIntervalList(Date startTime, Date endTime,
      long splitUnitInMilliseconds, List<Date> specialSplitPointList) {
    List<TimeInterval> returnValue = new ArrayList<TimeInterval>();

    if (null != startTime && null != endTime) {
      if (startTime.after(endTime)) {
        logger.warn("startTime={} should not be later than endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      } else {
        Date lastStart = startTime;
        Date lastEnd = KiraManagerUtils
            .getDateAfterAddMilliseconds(startTime, splitUnitInMilliseconds);

        while (lastEnd.before(endTime)) {
          List<TimeInterval> splittedTimeIntervalListBySpecialSplitPointList = getSplittedTimeIntervalListBySpecialSplitPointList(
              lastStart, lastEnd, specialSplitPointList);
          returnValue.addAll(splittedTimeIntervalListBySpecialSplitPointList);

          lastStart = lastEnd;
          lastEnd = KiraManagerUtils.getDateAfterAddMilliseconds(lastEnd, splitUnitInMilliseconds);
        }

        if (lastStart.before(endTime)) {
          List<TimeInterval> splittedTimeIntervalListBySpecialSplitPointList = getSplittedTimeIntervalListBySpecialSplitPointList(
              lastStart, endTime, specialSplitPointList);
          returnValue.addAll(splittedTimeIntervalListBySpecialSplitPointList);
        }
      }
    } else {
      logger.warn("One may be null. startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    return returnValue;
  }

  public static List<TimeInterval> getSplittedTimeIntervalListToHandleJobRuntimeData(Date startTime,
      Date endTime, Date dateOfOldestData) {
    boolean dateOfOldestDataAfterStartTime = true;
    if (null != startTime) {
      dateOfOldestDataAfterStartTime = dateOfOldestData.after(startTime);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("dateOfOldestDataAfterStartTime={}", dateOfOldestDataAfterStartTime);
    }

    Date finalStartTime = startTime;
    if (dateOfOldestDataAfterStartTime) {
      finalStartTime = dateOfOldestData;
    }

    List<Date> specialSplitPointList = KiraManagerUtils
        .getZeroTimeOfFirstDayOfMonthBetweenStartTimeAndEndTime(finalStartTime, endTime);
    long perTimeToHandleJobRuntimeDataInMilliseconds =
        60000L * KiraManagerUtils.getMinutesPerTimeToHandleJobRuntimeData();
    List<TimeInterval> splittedTimeIntervalList = KiraManagerUtils
        .getSplittedTimeIntervalList(finalStartTime, endTime,
            perTimeToHandleJobRuntimeDataInMilliseconds, specialSplitPointList);
    if (logger.isDebugEnabled()) {
      logger.debug(
          "splittedTimeIntervalList={} by finalStartTime={} and endTime={} and perTimeToHandleJobRuntimeDataInMilliseconds={} and specialSplitPointList={}",
          splittedTimeIntervalList, finalStartTime, endTime,
          perTimeToHandleJobRuntimeDataInMilliseconds, splittedTimeIntervalList);
    }

    return splittedTimeIntervalList;
  }

  public static void runTasksOneByOne(List<Callable<Void>> taskList,
      long sleepTimeBeforeRunNextTaskInMilliseconds, boolean runTaskInExecutorService,
      ExecutorService executorService, long timeOutPerTaskInMilliseconds) throws Exception {
    if (!CollectionUtils.isEmpty(taskList)) {
      boolean isFirstTask = true;
      for (Callable<Void> oneTask : taskList) {
        if (!isFirstTask) {
          if (logger.isDebugEnabled()) {
            logger.debug("Will sleep for {} milliseconds to run next task.");
          }
          Thread.sleep(sleepTimeBeforeRunNextTaskInMilliseconds);
        }
        if (runTaskInExecutorService) {
          List<Callable<Void>> oneBatchTaskList = new ArrayList<Callable<Void>>();
          oneBatchTaskList.add(oneTask);
          executorService
              .invokeAll(oneBatchTaskList, timeOutPerTaskInMilliseconds, TimeUnit.MILLISECONDS);
        } else {
          oneTask.call();
        }
        isFirstTask = false;
      }
    }
  }

  public static String getNewTableNameSuffixForJobRuntimeData(Date date) {
    Date zeroTimeOfFirstDayOfThatMonthOfDate = KiraManagerUtils
        .getZeroTimeOfFirstDayOfThatMonthOfDate(date);
    String newTableNameSuffix = KiraManagerUtils
        .getYearMonthPartString(zeroTimeOfFirstDayOfThatMonthOfDate);

    return newTableNameSuffix;
  }

  public static String getKiraManagerUrlPath() {
    String returnValue = "";
    //Need to get by ycc
    returnValue = KiraManagerConfigCenter
        .getProperty(KiraManagerConstants.PROPERTY_KIRA_SERVER_PORTAL_URL, null);
    if (StringUtils.isBlank(returnValue)) {
      //Fall back to default implementation.
      returnValue = getKiraManagerUrlPathByDefault();
    }

    return returnValue;
  }

  public static String getKiraManagerUrlPathByDefault() {
    String returnValue = "";
    if (KiraManagerUtils.isProductionEnvironment()) {
      returnValue = "http://kira.xxx.com.cn/kira";
    } else if (KiraManagerUtils.isStagingEnvironment()) {
      returnValue = "http://kira-stg.xxx.com.cn/kira";
    } else if (KiraManagerUtils.isTestEnvironment()) {
      returnValue = "http://xxx:80/kira";
    } else if (KiraManagerUtils.isDevEnvironment()) {
      returnValue = "http://localhost:8080/kira";
    } else {
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      logger.warn("Unknown environment found: env={} so use the default kiraManagerUrlPath={}", env,
          returnValue);
    }

    return returnValue;
  }

  public static int getMaxConcurrentTimerTriggerCount() {
    int returnValue = 200;

    if (KiraManagerUtils.isProductionEnvironment()) {
      returnValue = 1000;
    } else if (KiraManagerUtils.isStagingEnvironment()) {
      returnValue = 200;
    } else if (KiraManagerUtils.isTestEnvironment()) {
      //returnValue = 1000;
      returnValue = 20;
    } else if (KiraManagerUtils.isDevEnvironment()) {
      returnValue = 100;
    } else {
      String env = KiraManagerUtils.getEnvWithZoneIfPossible();
      logger.warn(
          "Unknown environment found: env={} so use the default maxConcurrentTimerTriggerCount={}",
          env, returnValue);
    }

    return returnValue;
  }

  public static boolean isDevEnvironment() {
    //String env = YccGlobalPropertyConfigurer.getEnv();
    String env = LoadProertesContainer.provider()
        .getProperty(InternalConstants.CURRENT_SYSTEM_ENV, "dev");
    boolean returnValue = false;
    if ("dev".equalsIgnoreCase(env)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static boolean isTestEnvironment() {
    //String env = YccGlobalPropertyConfigurer.getEnv();
    String env = LoadProertesContainer.provider()
        .getProperty(InternalConstants.CURRENT_SYSTEM_ENV, "null");
    boolean returnValue = false;
    if ("test".equalsIgnoreCase(env)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static boolean isStagingEnvironment() {
    //String env = YccGlobalPropertyConfigurer.getEnv();
    String env = LoadProertesContainer.provider()
        .getProperty(InternalConstants.CURRENT_SYSTEM_ENV, "null");
    boolean returnValue = false;
    if ("staging".equalsIgnoreCase(env)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static boolean isProductionEnvironment() {
    //String env = YccGlobalPropertyConfigurer.getEnv();
    String env = LoadProertesContainer.provider()
        .getProperty(InternalConstants.CURRENT_SYSTEM_ENV, "null");
    boolean returnValue = false;
    if ("production".equalsIgnoreCase(env)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static void addAdminEmailsToList(List<String> emailList) {
    Set<String> emailSet = new LinkedHashSet<String>();
    emailSet.addAll(emailList);
    List<String> adminEmailsAsList = KiraCommonUtils
        .getStringListByDelmiter(adminEmails, KiraCommonConstants.COMMA_DELIMITER);
    emailSet.addAll(adminEmailsAsList);
    emailList.clear();
    emailList.addAll(emailSet);
  }

  public static void addAdminPhoneNumbersToList(List<String> phoneNumberList) {
    Set<String> phoneNumberSet = new LinkedHashSet<String>();
    phoneNumberSet.addAll(phoneNumberList);
    List<String> adminPhoneNumbersAsList = KiraCommonUtils
        .getStringListByDelmiter(adminPhoneNumbers, KiraCommonConstants.COMMA_DELIMITER);
    phoneNumberSet.addAll(adminPhoneNumbersAsList);
    phoneNumberList.clear();
    phoneNumberList.addAll(phoneNumberSet);
  }

  public static boolean isCanBeScheduled(TriggerMetadata triggerMetadata, boolean masterZone) {
    boolean returnValue = false;
    if (null != triggerMetadata) {
      Boolean unregistered = triggerMetadata.getUnregistered();
      Boolean deleted = triggerMetadata.getDeleted();
      Boolean disabled = triggerMetadata.getDisabled();
      Boolean scheduledLocally = triggerMetadata.getScheduledLocally();
      Boolean onlyScheduledInMasterZone = triggerMetadata.getOnlyScheduledInMasterZone();
      if ((!onlyScheduledInMasterZone || (onlyScheduledInMasterZone && masterZone))
          && (!unregistered) && (!deleted) && (!disabled) && (!scheduledLocally)) {
        returnValue = true;
      }
    }
    return returnValue;
  }

  public static boolean validateMisfireInstructionForSimpleTrigger(int misfireInstruction) {
    if (misfireInstruction < ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW) {
      return false;
    }

    return misfireInstruction
        <= KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT;

  }

  public static boolean validateMisfireInstructionForCronTrigger(int misfireInstruction) {
    if (misfireInstruction < ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW) {
      return false;
    }

    return misfireInstruction <= ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;

  }

  public static ITimerTrigger getTimerTrigger(TriggerMetadata triggerMetadata,
      TimerTriggerSchedule timerTriggerSchedule, ITimeScheduleCallback timeScheduleCallback)
      throws Exception {
    ITimerTrigger returnValue = null;

    String appId = triggerMetadata.getAppId();
    String triggerId = triggerMetadata.getTriggerId();
    Integer priority = triggerMetadata.getPriority();
    Date startTime = triggerMetadata.getStartTime();
    Date endTime = triggerMetadata.getEndTime();

    Long startTimeOfTimerTriggerSchedule = timerTriggerSchedule.getStartTime();
    if (null != startTimeOfTimerTriggerSchedule) {
      startTime = new Date(startTimeOfTimerTriggerSchedule.longValue());
    }

    Date previousFireTime = null;
    Long previousFireTimeOfTimerTriggerSchedule = timerTriggerSchedule.getPreviousFireTime();
    if (null != previousFireTimeOfTimerTriggerSchedule) {
      previousFireTime = new Date(previousFireTimeOfTimerTriggerSchedule.longValue());
    }

    Date nextFireTime = null;
    Long nextFireTimeOfTimerTriggerSchedule = timerTriggerSchedule.getNextFireTime();
    if (null != nextFireTimeOfTimerTriggerSchedule) {
      nextFireTime = new Date(nextFireTimeOfTimerTriggerSchedule.longValue());
    }

    long timesHasBeenTriggered = 0;
    Long timesHasBeenTriggeredOfTimerTriggerSchedule = timerTriggerSchedule.getTimesTriggered();
    if (null != timesHasBeenTriggeredOfTimerTriggerSchedule) {
      timesHasBeenTriggered = timesHasBeenTriggeredOfTimerTriggerSchedule.longValue();
    }

    //String identityOfKiraTimerTriggerLocalScheduler = this.getIdentityOfKiraTimerTriggerLocalScheduler();

    String identityInTrackingSystem = String.valueOf(timerTriggerSchedule.getId());

    Integer misfireInstruction = triggerMetadata.getMisfireInstruction();
    if (null == misfireInstruction) {
      misfireInstruction = Integer.valueOf(AbstractTimerTrigger.DEFAULT_MISFIRE_INSTRUCTION);
      logger.warn(
          "misfireInstruction should not be null here. This should not happen. May have bugs. Will use the default value now. triggerMetadata={}",
          triggerMetadata);
    }

    Boolean requestsRecovery = triggerMetadata.getRequestsRecovery();
    if (null == requestsRecovery) {
      requestsRecovery = Boolean.FALSE;
      logger.warn(
          "requestsRecovery should not be null here. This should not happen. May have bugs. Will use the default value now. triggerMetadata={}",
          triggerMetadata);
    }

    String triggerType = triggerMetadata.getTriggerType();
    if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      String cronExpression = triggerMetadata.getCronExpression();
      returnValue = new KiraCronTimerTrigger(triggerId, appId, timeScheduleCallback,
          (null == priority) ? KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER
              : priority.intValue(), startTime, endTime, previousFireTime, nextFireTime,
          timesHasBeenTriggered, cronExpression, identityInTrackingSystem,
          misfireInstruction.intValue(), requestsRecovery.booleanValue());
    } else if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      Integer repeatCount = triggerMetadata.getRepeatCount();
      Long repeatInterval = triggerMetadata.getRepeatInterval();
      Long startDelay = triggerMetadata.getStartDelay();
      if (null == startTime) {
        startTime = new Date(
            System.currentTimeMillis() + ((null == startDelay) ? 0L : startDelay.longValue()));
      }

      returnValue = new KiraSimpleTimerTrigger(triggerId, appId, timeScheduleCallback,
          (null == priority) ? KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER
              : priority.intValue(), startTime, endTime, previousFireTime, nextFireTime,
          timesHasBeenTriggered, (null == repeatCount) ? 0 : repeatCount.intValue(),
          (null == repeatInterval) ? 0L : repeatInterval.longValue(), identityInTrackingSystem,
          misfireInstruction.intValue(), requestsRecovery.booleanValue());
    } else {
      String errorMessage =
          "Faild to getTimerTrigger for the unknown triggerType. triggerType=" + triggerType
              + " and triggerMetadata=" + KiraCommonUtils.toString(triggerMetadata)
              + " and timerTriggerSchedule=" + KiraCommonUtils.toString(timerTriggerSchedule);
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    return returnValue;
  }

  public static String getEnvWithZoneIfPossible() {
    String returnValue = null;
    String env = LoadProertesContainer.provider()
        .getProperty(InternalConstants.CURRENT_SYSTEM_ENV, null);
    String zone = LoadProertesContainer.provider()
        .getProperty(InternalConstants.KIRA_CURRENT_ZONE, null);
    if (StringUtils.isNotBlank(zone)) {
      returnValue = env + "-" + zone;
    } else {
      returnValue = env;
    }
    return returnValue;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

}
