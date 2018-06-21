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

import com.alibaba.fastjson.JSON;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.provider.HedwigWebserviceExporter;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.KiraTimeoutException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkException;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

public class KiraCommonUtils {

  private static Logger logger = LoggerFactory.getLogger(KiraCommonUtils.class);

  private static ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public KiraCommonUtils() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @deprecated
   */
  public static String getTriggerZNodeZKPathByOldTriggerZNodeZKPath(String oldTriggerZNodeZKPath) {
    String returnValue = null;
    if (StringUtils.isNotBlank(oldTriggerZNodeZKPath)) {
      int indexOfPrefix = oldTriggerZNodeZKPath.lastIndexOf(KiraCommonConstants.ZNODE_NAME_PREFIX);
      if (-1 != indexOfPrefix) {
        if (oldTriggerZNodeZKPath.length() > 2) {
          String poolAndTriggerName = oldTriggerZNodeZKPath.substring(indexOfPrefix + 1);
          String[] poolIdTriggerIdArray = StringUtils
              .split(poolAndTriggerName, KiraCommonConstants.SPECIAL_DELIMITER);
          if (null != poolIdTriggerIdArray && poolIdTriggerIdArray.length == 2) {
            String poolIdShortPath = poolIdTriggerIdArray[0];
            String triggerIdShortPath = poolIdTriggerIdArray[1];
            returnValue =
                KiraCommonConstants.ZK_PATH_TRIGGERS + KiraCommonConstants.ZNODE_NAME_PREFIX
                    + poolIdShortPath + KiraCommonConstants.ZNODE_NAME_PREFIX + triggerIdShortPath;
          } else {
            logger.warn("The poolIdTriggerIdArray={} . It's length should be 2.",
                poolIdTriggerIdArray);
          }
        } else {
          logger.warn(
              "oldTriggerZNodeZKPath.length={} . It should be large than 2. oldTriggerZNodeZKPath={}",
              oldTriggerZNodeZKPath.length(), oldTriggerZNodeZKPath);
        }
      } else {
        logger.warn(KiraCommonConstants.ZNODE_NAME_PREFIX
            + " do not exist in oldTriggerZNodeZKPath for getTriggerZNodeZKPathByOldTriggerZNodeZKPath. oldTriggerZNodeZKPath="
            + oldTriggerZNodeZKPath);
      }
    } else {
      logger
          .warn("oldTriggerZNodeZKPath is blank for getTriggerZNodeZKPathByOldTriggerZNodeZKPath");
    }

    return returnValue;
  }

  public static List<String> getAllPoolIdOfTriggerOnZK() throws Exception {
    List<String> returnValue = new ArrayList<String>();
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolPath = null;
        for (String onePoolShortPath : poolList) {
          onePoolPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolPath, true);
          if (poolZNodeData instanceof String) {
            //it is of type String. regard it as poolZNode
            returnValue.add((String) poolZNodeData);
          }
        }
      }
    }
    return returnValue;
  }

  /**
   * @deprecated
   */
  public static String getTriggerZNodeZKPath_Old(String appId, String triggerId) {
    String filteredAppId = KiraUtil.filterString(appId);
    String filteredTriggerId = KiraUtil.filterString(triggerId);
    String triggerZNodeZKPath =
        KiraCommonConstants.ZK_PATH_TRIGGERS + KiraCommonConstants.ZNODE_NAME_PREFIX + filteredAppId
            + KiraCommonConstants.SPECIAL_DELIMITER + filteredTriggerId;
    return triggerZNodeZKPath;
  }

  public static String getTriggerZNodeZKPath(String appId, String triggerId) {
    String poolZNodeZKPathForTrigger = KiraCommonUtils.getPoolZNodeZKPathForTrigger(appId);
    String filteredTriggerId = KiraUtil.filterString(triggerId);
    String triggerZNodeZKPath =
        poolZNodeZKPathForTrigger + KiraCommonConstants.ZNODE_NAME_PREFIX + filteredTriggerId;
    return triggerZNodeZKPath;
  }

  public static String getPoolZNodeZKPathForTrigger(String appId) {
    String filteredAppId = KiraUtil.filterString(appId);
    String triggerZNodeZKPath =
        KiraCommonConstants.ZK_PATH_TRIGGERS + KiraCommonConstants.ZNODE_NAME_PREFIX
            + filteredAppId;
    return triggerZNodeZKPath;
  }

  public static void createOrUpdateTriggerZNode(TriggerMetadataZNodeData triggerMetaDataZNodeData,
      boolean sleepForSubscribeChildChangesPoolId) throws Exception {
    if (null != triggerMetaDataZNodeData) {
      String appId = triggerMetaDataZNodeData.getAppId();
      String triggerId = triggerMetaDataZNodeData.getTriggerId();

      if (!zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
        try {
          zkClient.createPersistent(KiraCommonConstants.ZK_PATH_TRIGGERS, true);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }

      String poolZNodeZKPathForTrigger = KiraCommonUtils.getPoolZNodeZKPathForTrigger(appId);
      if (!zkClient.exists(poolZNodeZKPathForTrigger)) {
        try {
          zkClient.createPersistent(poolZNodeZKPathForTrigger, appId);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
        if (sleepForSubscribeChildChangesPoolId) {
          try {
            //Sleep for 5s to let server subscribe child changes for poolId.
            Thread.sleep(5000L);
          } catch (InterruptedException e) {
            logger.error(
                "InterruptedException occurs when wait for server to subscribe child changes for poolId.",
                e);
          }
        }
      } else {
        Object poolZNodeData = zkClient.readData(poolZNodeZKPathForTrigger, true);
        if (poolZNodeData instanceof String) {
          String storedPoolIdOnZK = (String) poolZNodeData;
          if (!storedPoolIdOnZK.equals(appId)) {
            logger.warn(
                "Found inconsistent appId. poolZNodeZKPathForTrigger={} and storedPoolIdOnZK={} and appId={}",
                poolZNodeZKPathForTrigger, storedPoolIdOnZK, appId);
          }
        } else {
          logger.warn(
              "StoredPoolIdOnZK is not of String or null. poolZNodeZKPathForTrigger={} and poolZNodeData={} and appId={}",
              poolZNodeZKPathForTrigger, poolZNodeData, appId);
        }
      }
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
      if (!zkClient.exists(triggerZNodeZKPath)) {
        try {
          zkClient.createPersistent(triggerZNodeZKPath, triggerMetaDataZNodeData);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      } else {
        zkClient.writeData(triggerZNodeZKPath, triggerMetaDataZNodeData);
      }
    }
  }

  public static boolean isNeedToWriteTriggerZnodeData(
      TriggerMetadataZNodeData triggerMetaDataZNodeData) throws Exception {
    boolean returnValue = false;
    if (null != triggerMetaDataZNodeData) {
      String poolId = triggerMetaDataZNodeData.getAppId();
      String triggerId = triggerMetaDataZNodeData.getTriggerId();
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(poolId, triggerId);
      TriggerMetadataZNodeData existTriggerMetadataZNodeData = zkClient
          .readData(triggerZNodeZKPath, true);
      if (null != existTriggerMetadataZNodeData) {
        String oldVersion = existTriggerMetadataZNodeData.getVersion();
        String newVersion = triggerMetaDataZNodeData.getVersion();
        int compareResult = KiraCommonUtils
            .compareVersionForKiraTimerTrigger(newVersion, oldVersion);
        if (compareResult > 0) {
          returnValue = true;
        }
      } else {
        returnValue = true;
      }
    }
    return returnValue;
  }

  public static void createEnvironmentZNodeZKPathForTrigger(
      String environmentsZNodeZKPathForTrigger, String environmentZNodeZKShortPathForTrigger,
      String znodeData)
      throws ZkException, IllegalArgumentException, ZkException, HedwigException, RuntimeException {
    if (StringUtils.isNotBlank(environmentsZNodeZKPathForTrigger)) {
      if (!zkClient.exists(environmentsZNodeZKPathForTrigger)) {
        try {
          zkClient.createPersistent(environmentsZNodeZKPathForTrigger, true);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }
      if (StringUtils.isNotBlank(environmentZNodeZKShortPathForTrigger)) {
        if (StringUtils.isNotBlank(znodeData)) {
          String environmentZNodeZKPathForTrigger = KiraUtil
              .getChildFullPath(environmentsZNodeZKPathForTrigger,
                  environmentZNodeZKShortPathForTrigger);
          if (!zkClient.exists(environmentZNodeZKPathForTrigger)) {
            try {
              zkClient.createPersistent(environmentZNodeZKPathForTrigger, znodeData);
            } catch (ZkNodeExistsException nodeExistsException) {
              logger.info(
                  "ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                      + ". Just ignore this exception. This node may be created by someone.");
            }
          } else {
            zkClient.writeData(environmentZNodeZKPathForTrigger, znodeData);
          }
        } else {
          logger.warn(
              "znodeData is blank. for createEnvironmentZNodeZKPathForTrigger. So do not override the value environmentsZNodeZKPathForTrigger={} and environmentZNodeZKShortPathForTrigger={} and znodeData={}",
              environmentsZNodeZKPathForTrigger, environmentZNodeZKShortPathForTrigger, znodeData);
        }
      } else {
        logger.warn(
            "environmentZNodeZKShortPathForTrigger is blank. for createEnvironmentZNodeZKPathForTrigger. environmentsZNodeZKPathForTrigger={} and environmentZNodeZKShortPathForTrigger={} and znodeData={}",
            environmentsZNodeZKPathForTrigger, environmentZNodeZKShortPathForTrigger, znodeData);
      }

    } else {
      logger.warn(
          "environmentZNodeZKPathForTrigger is blank. for createEnvironmentZNodeZKPathForTrigger. environmentsZNodeZKPathForTrigger={} and environmentZNodeZKShortPathForTrigger={} and znodeData={}",
          environmentsZNodeZKPathForTrigger, environmentZNodeZKShortPathForTrigger, znodeData);
    }
  }

  public static HandleResult invokeMethod(Object targetObject, Method methodObject,
      Object[] arguments) throws Exception {
    Object returnObject = methodObject.invoke(targetObject, arguments);
    String resultCode = KiraCommonConstants.RESULT_CODE_SUCCESS;
    String resultData = null;
    Class<?> returnType = methodObject.getReturnType();
    if (returnType != void.class) {
      try {
        resultData = JSON.toJSONString(returnObject, true);
      } catch (Exception e) {
        logger.error(
            "Error occurs for JSON.toJSONString(returnObject,true); returnObject=" + returnObject,
            e);
        resultData = (null == returnObject) ? null : returnObject.toString();
      }
    }
    HandleResult handleResult = new HandleResult(resultCode, resultData);
    return handleResult;
  }

  public static boolean isNeedToEnsureNotConcurrent(Boolean onlyRunOnSingleProcess,
      Boolean concurrent) {
    boolean returnValue = false;
    if (Boolean.TRUE.equals(onlyRunOnSingleProcess) && Boolean.FALSE.equals(concurrent)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static String toString(Object object) {
    String returnValue = KiraCommonUtils.toString(object, false);
    return returnValue;
  }

  public static String toString(Object object, boolean printNullStringIfEmpty) {
    String returnValue = null;
    Object jsonObject = null;
    try {
      jsonObject = JSON.toJSON(object);
    } catch (Exception e) {
      logger.error(
          "Error occurs for JSON.toJSON(object); object=" + object + " and printNullStringIfEmpty="
              + printNullStringIfEmpty, e);
      jsonObject = (null == object) ? null : object.toString();
    }
    if (null == jsonObject) {
      if (printNullStringIfEmpty) {
        returnValue = "null";
      } else {
        returnValue = "";
      }
    } else {
      returnValue = jsonObject.toString();
    }
    return returnValue;
  }

  public static String getUUID() {
    String uuid = UUID.randomUUID().toString().toUpperCase();
    return uuid;
  }

  public static String getUUIDWithTimePrefix() {
    String uuid = KiraCommonUtils.getUUID();
    Date now = new Date();
    String dateString = KiraCommonUtils
        .getDateAsString(now, KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION);
    String returnValue = dateString + KiraCommonConstants.SPECIAL_DELIMITER + uuid;
    return returnValue;
  }

  public static boolean isVersionValidForKiraTimerTrigger(String version) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(version)) {
      List<Integer> partsAsIntegerListIfVersionForKiraTimerTriggerValidForVersion = getPartsAsIntegerListIfVersionForKiraTimerTriggerValid(
          version);
      returnValue = org.apache.commons.collections.CollectionUtils
          .isNotEmpty(partsAsIntegerListIfVersionForKiraTimerTriggerValidForVersion);
    }
    return returnValue;
  }

  /**
   * Compare the version
   *
   * @return a positive integer, zero, or a negative integer as the oneVersion is greater than,
   * equal to, or less than anotherVersion, ignoring case considerations.
   */
  public static int compareVersion(String oneVersion, String anotherVersion) {
    return oneVersion.compareToIgnoreCase(anotherVersion);
  }

  public static int compareVersionForKiraTimerTrigger(String newVersion, String oldVersion)
      throws Exception {
    int returnValue = 0;

    if (StringUtils.isNotBlank(newVersion) && StringUtils.isNotBlank(oldVersion)) {
      List<Integer> partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion = getPartsAsIntegerListIfVersionForKiraTimerTriggerValid(
          newVersion);
      List<Integer> partsAsIntegerListIfVersionForKiraTimerTriggerValidForOldVersion = getPartsAsIntegerListIfVersionForKiraTimerTriggerValid(
          oldVersion);
      boolean isNewVersionValid = org.apache.commons.collections.CollectionUtils
          .isNotEmpty(partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion);
      boolean isOldVersionValid = org.apache.commons.collections.CollectionUtils
          .isNotEmpty(partsAsIntegerListIfVersionForKiraTimerTriggerValidForOldVersion);

      if (!isNewVersionValid) {
        //logger.warn("The format of new version should be valid for KiraTimerTrigger. Just warn this now to keep compatibility. newVersion={} and the format should be numbers delimited by \".\". For example:\"0.0.1\".", newVersion);
        throw new ValidationException(
            "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But newVersion="
                + newVersion + " now.");
//				if(!isOldVersionValid) {
//					//If both the new and old version is not valid, just use the old compare logic to keep compatibility.
//					//logger.warn("Both the format of new version and old version are not valid. Just use the old comparation logic which is simple string comparation to keep compatibility. newVersion={} and oldVersion={}", newVersion, oldVersion);
//					returnValue = newVersion.compareToIgnoreCase(oldVersion);
//				} else {
//					//If the old version is valid but the new version is not valid, just regard it as equal to make it can not be upgraded.
//					logger.warn("The format of the old version is valid but the new version is not valid, just regard it as equal to make it can not be upgraded. newVersion={} and oldVersion={}", newVersion, oldVersion);
//					returnValue = 0;
//				}
      } else {
        if (!isOldVersionValid) {
          throw new ValidationException(
              "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But oldVersion="
                  + oldVersion + " now.");
//					//If the old version is not valid but the new version is valid, just regard it as upgrade.
//					returnValue = 1;
        } else {
          //If both old and new version are valid, just use the new compare logic.
          int partCountOfNewVersion = partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion
              .size();
          int partCountOfOldVersion = partsAsIntegerListIfVersionForKiraTimerTriggerValidForOldVersion
              .size();
          //Padding the two list with 0 before compare them
          if (partCountOfNewVersion > partCountOfOldVersion) {
            for (int i = 0; i < (partCountOfNewVersion - partCountOfOldVersion); i++) {
              partsAsIntegerListIfVersionForKiraTimerTriggerValidForOldVersion
                  .add(Integer.valueOf(0));
            }
          } else if (partCountOfNewVersion < partCountOfOldVersion) {
            for (int i = 0; i < (partCountOfOldVersion - partCountOfNewVersion); i++) {
              partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion
                  .add(Integer.valueOf(0));
            }
          }

          for (int i = 0;
              i < partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion.size(); i++) {
            Integer onePartOfNewVersion = partsAsIntegerListIfVersionForKiraTimerTriggerValidForNewVersion
                .get(i);
            Integer onePartOfOldVersion = partsAsIntegerListIfVersionForKiraTimerTriggerValidForOldVersion
                .get(i);
            int compareValueOfOnePart = onePartOfNewVersion.compareTo(onePartOfOldVersion);
            if (0 != compareValueOfOnePart) {
              returnValue = compareValueOfOnePart;
              break;
            }
          }
        }
      }
    } else {
      //logger.error("Both the old and new version should not be blank for KiraTimerTrigger while compare them. newVersion={} and oldVersion={}", newVersion, oldVersion);
      throw new ValidationException(
          "Both the old and new version should not be blank for KiraTimerTrigger while compare them. newVersion="
              + newVersion + " and oldVersion=" + oldVersion);
    }

    return returnValue;
  }

  public static List<Integer> getPartsAsIntegerListIfVersionForKiraTimerTriggerValid(
      String version) {
    List<Integer> returnValue = null;
    if (StringUtils.isNotBlank(version)) {
      String versionWithoutSNAPSHOT = getVersionWithoutSNAPSHOTForKiraTimerTrigger(version);
      if (StringUtils.isNotBlank(versionWithoutSNAPSHOT)) {
        List<String> stringListByDelimiterForVersionOfKiraTimerTrigger = getDelimitedStringList(
            versionWithoutSNAPSHOT, KiraCommonConstants.KIRA_TIMER_TRIGGER_VERSION_DELIMITER,
            false);
        if (org.apache.commons.collections.CollectionUtils
            .isNotEmpty(stringListByDelimiterForVersionOfKiraTimerTrigger)) {
          for (String onePartOfVersionAsString : stringListByDelimiterForVersionOfKiraTimerTrigger) {
            Integer onePartOfVersionAsInteger = getOnePartOfVersionAsInteger(
                onePartOfVersionAsString);
            if (null != onePartOfVersionAsInteger && 0 <= onePartOfVersionAsInteger.intValue()) {
              if (null == returnValue) {
                returnValue = new ArrayList<Integer>();
              }
              returnValue.add(onePartOfVersionAsInteger);
            } else {
              //Need to return null now if not valid.
              returnValue = null;
              break;
            }
          }
        } else {
          logger.warn(
              "stringListByDelimiterForVersionOfKiraTimerTrigger should not be empty. version={} and stringListByDelimiterForVersionOfKiraTimerTrigger={}",
              version, stringListByDelimiterForVersionOfKiraTimerTrigger);
        }
      } else {
        logger.warn(
            "versionWithoutSNAPSHOT for KiraTimerTrigger should not be blank. version={} and versionWithoutSNAPSHOT={}",
            version, versionWithoutSNAPSHOT);
      }
    } else {
      logger.warn("version for KiraTimerTrigger should not be blank. version={}", version);
    }
    return returnValue;
  }

  private static Integer getOnePartOfVersionAsInteger(String onePartOfVersionAsString) {
    Integer returnValue = null;
    if (null != onePartOfVersionAsString) {
      try {
        returnValue = Integer.valueOf(onePartOfVersionAsString);
      } catch (NumberFormatException e) {
        //Do not print the log out
        //logger.error("onePartOfVersionAsString may not be valid. It should be an integer. onePartOfVersionAsString={}",onePartOfVersionAsString, e);
      }
    }
    return returnValue;
  }

  public static List<String> getDelimitedStringList(String stringValue, String delimiter,
      boolean needTrim) {
    List<String> returnValue = null;
    if (null != stringValue) {
      String[] stringValueAsArray = StringUtils.split(stringValue, delimiter);
      if (null != stringValueAsArray) {
        for (String oneString : stringValueAsArray) {
          if (null != oneString) {
            if (null == returnValue) {
              returnValue = new ArrayList<String>();
            }
            if (needTrim) {
              returnValue.add(oneString.trim());
            } else {
              returnValue.add(oneString);
            }
          }
        }
      }
    }
    return returnValue;
  }

  public static String getVersionWithoutSNAPSHOTForKiraTimerTrigger(String version) {
    String returnValue = null;
    if (null != version) {
      int snapshotIndex = version.indexOf(KiraCommonConstants.STRING_SNAPSHOT);
      if (-1 == snapshotIndex) {
        returnValue = version;
      } else {
        returnValue = version.substring(0, snapshotIndex);
      }
    }
    return returnValue;
  }

  public static boolean isCanBeScheduledInOldNoZoneSystem(
      TriggerMetadataDetail triggerMetadataDetail) {
    boolean returnValue = false;
    if (null != triggerMetadataDetail) {
      Boolean disabled = triggerMetadataDetail.getDisabled();
      Boolean scheduledLocally = triggerMetadataDetail.getScheduledLocally();
      if ((!disabled) && (!scheduledLocally)) {
        returnValue = true;
        //Always schedule trigger even if start time > end time
//				Date endTime = triggerMetadataDetail.getEndTime();
//				if(null!=endTime) {
//					boolean isOutOfDate = endTime.before(new Date());
//					if(!isOutOfDate) {
//						returnValue = true;
//					}
//				} else {
//					returnValue = true;
//				}
      }
    }
    return returnValue;
  }

  public static boolean isCanBeScheduled(TriggerMetadataDetail triggerMetadataDetail,
      boolean masterZone) {
    boolean returnValue = false;
    if (null != triggerMetadataDetail) {
      Boolean disabled = triggerMetadataDetail.getDisabled();
      Boolean scheduledLocally = triggerMetadataDetail.getScheduledLocally();
      Boolean onlyScheduledInMasterZone = triggerMetadataDetail.getOnlyScheduledInMasterZone();
      if ((!onlyScheduledInMasterZone || (onlyScheduledInMasterZone && masterZone)) && (!disabled)
          && (!scheduledLocally)) {
        returnValue = true;
        //Always schedule trigger even if start time > end time
//				Date endTime = triggerMetadataDetail.getEndTime();
//				if(null!=endTime) {
//					boolean isOutOfDate = endTime.before(new Date());
//					if(!isOutOfDate) {
//						returnValue = true;
//					}
//				} else {
//					returnValue = true;
//				}
      }
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenUnableToDeliverOrRun(Boolean asynchronous) {
    Integer returnValue = null;
    if (asynchronous) {
      returnValue = JobStatusEnum.UNABLE_TO_DELIVER.getId();
    } else {
      returnValue = JobStatusEnum.UNABLE_TO_RUN.getId();
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenDeliveringOrRunning(Boolean asynchronous) {
    Integer returnValue = null;
    if (asynchronous) {
      returnValue = JobStatusEnum.DELIVERING.getId();
    } else {
      returnValue = JobStatusEnum.RUNNING.getId();
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenDeliverFailedOrRunFailed(Boolean asynchronous) {
    Integer returnValue = null;
    if (asynchronous) {
      returnValue = JobStatusEnum.DELIVERY_FAILED.getId();
    } else {
      returnValue = JobStatusEnum.RUN_FAILED.getId();
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenDeliverSuccessOrRunSuccess(Boolean asynchronous) {
    Integer returnValue = null;
    if (asynchronous) {
      returnValue = JobStatusEnum.DELIVERY_SUCCESS.getId();
    } else {
      returnValue = JobStatusEnum.RUN_SUCCESS.getId();
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenDeliverPartialSuccessOrRunPartialSuccess(
      Boolean asynchronous) {
    Integer returnValue = null;
    if (asynchronous) {
      returnValue = JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId();
    } else {
      returnValue = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
    }
    return returnValue;
  }

  public static List<Integer> getCanBeUpdatedJobStatusIdList(Integer jobStatusId) {
    List<Integer> returnValue = new ArrayList<Integer>();
    if (null != jobStatusId) {
      if (JobStatusEnum.CREATED.getId().equals(jobStatusId)) {
        //returnValue.add(JobStatusEnum.CREATED.getId());
      } else if (JobStatusEnum.UPDATED.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
      } else if (JobStatusEnum.UNABLE_TO_DELIVER.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        //returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
      } else if (JobStatusEnum.DELIVERING.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        //returnValue.add(JobStatusEnum.DELIVERING.getId());
      } else if (JobStatusEnum.DELIVERY_SUCCESS.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
      } else if (JobStatusEnum.DELIVERY_FAILED.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        //returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
      } else if (JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        //returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
      } else if (JobStatusEnum.UNABLE_TO_RUN.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        //returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
      } else if (JobStatusEnum.RUNNING.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        //returnValue.add(JobStatusEnum.RUNNING.getId());
      } else if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.RUNNING.getId());
        returnValue.add(JobStatusEnum.RUN_PARTIAL_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.RUN_SUCCESS.getId());
      } else if (JobStatusEnum.RUN_FAILED.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.RUNNING.getId());
        //returnValue.add(JobStatusEnum.RUN_PARTIAL_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.RUN_FAILED.getId());
      } else if (JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.RUNNING.getId());
        //returnValue.add(JobStatusEnum.RUN_PARTIAL_SUCCESS.getId());
      } else if (JobStatusEnum.NO_NEED_TO_DELIVER.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        //returnValue.add(JobStatusEnum.NO_NEED_TO_DELIVER.getId());
      } else if (JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId().equals(jobStatusId)) {
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        returnValue.add(JobStatusEnum.RUNNING.getId());
        //returnValue.add(JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId());
      } else if (JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId().equals(jobStatusId)) {
        //Do not override the original failed cause.
        returnValue.add(JobStatusEnum.CREATED.getId());
        returnValue.add(JobStatusEnum.UPDATED.getId());
        //returnValue.add(JobStatusEnum.UNABLE_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.DELIVERING.getId());
        //returnValue.add(JobStatusEnum.DELIVERY_FAILED.getId());
        returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.UNABLE_TO_RUN.getId());
        returnValue.add(JobStatusEnum.RUNNING.getId());
        returnValue.add(JobStatusEnum.RUN_PARTIAL_SUCCESS.getId());
        returnValue.add(JobStatusEnum.RUN_SUCCESS.getId());
        //returnValue.add(JobStatusEnum.RUN_FAILED.getId());
        returnValue.add(JobStatusEnum.NO_NEED_TO_DELIVER.getId());
        returnValue.add(JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId());
        //returnValue.add(JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId());
      }
    }
    return returnValue;
  }

  public static boolean isBadAndFinalJobStatus(Integer jobStatusId) {
    boolean returnValue = false;
    if (JobStatusEnum.UNABLE_TO_DELIVER.getId().equals(jobStatusId)
        || JobStatusEnum.DELIVERY_FAILED.getId().equals(jobStatusId)
        || JobStatusEnum.UNABLE_TO_RUN.getId().equals(jobStatusId)
        || JobStatusEnum.RUN_FAILED.getId().equals(jobStatusId)
        || JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId().equals(jobStatusId)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static boolean isNeedToUpdateJobStatus(Integer oldJobStatusId, Integer newJobStatusId) {
    boolean returnValue = false;
    List<Integer> canBeUpdatedJobStatusIdList = KiraCommonUtils
        .getCanBeUpdatedJobStatusIdList(newJobStatusId);
    if (canBeUpdatedJobStatusIdList.contains(oldJobStatusId)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static Integer getJobStatusIdWhenCompleteDeliverByResultCode(String resultCode) {
    Integer returnValue = null;
    if (KiraCommonConstants.RESULT_CODE_SUCCESS.equals(resultCode)) {
      returnValue = JobStatusEnum.DELIVERY_SUCCESS.getId();
    } else if (KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS.equals(resultCode)) {
      returnValue = JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId();
    } else if (KiraCommonConstants.RESULT_CODE_FAILED.equals(resultCode)) {
      returnValue = JobStatusEnum.DELIVERY_FAILED.getId();
      ;
    }

    return returnValue;
  }

  public static Integer getJobStatusIdWhenCompleteRunByResultCode(String resultCode) {
    Integer returnValue = null;
    if (KiraCommonConstants.RESULT_CODE_SUCCESS.equals(resultCode)) {
      returnValue = JobStatusEnum.RUN_SUCCESS.getId();
    } else if (KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS.equals(resultCode)) {
      returnValue = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
    } else if (KiraCommonConstants.RESULT_CODE_FAILED.equals(resultCode)) {
      returnValue = JobStatusEnum.RUN_FAILED.getId();
      ;
    }

    return returnValue;
  }

  public static boolean isJobItemCompleted(Integer jobStatusIdOfJobItem) {
    boolean returnValue = false;
    if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.RUN_FAILED.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.UNABLE_TO_RUN.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.DELIVERY_FAILED.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.UNABLE_TO_DELIVER.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.NO_NEED_TO_DELIVER.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId().equals(jobStatusIdOfJobItem)
        || JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId().equals(jobStatusIdOfJobItem)) {
      returnValue = true;
    }
    return returnValue;
  }

  public static ServiceProfile getServiceProfile(HedwigWebserviceExporter targetObject) {
    ServiceProfile serviceProfile = null;
    Field profileField = ReflectionUtils
        .findField(HedwigWebserviceExporter.class, "profile", ServiceProfile.class);
    if (null != profileField) {
      ReflectionUtils.makeAccessible(profileField);
      Object profileObject = ReflectionUtils.getField(profileField, targetObject);
      if (null != profileObject) {
        if (profileObject instanceof ServiceProfile) {
          serviceProfile = (ServiceProfile) profileObject;
        } else {
          logger.error("Got profileObject which is not of type ServiceProfile.");
        }
      } else {
        logger.error("Got profileObject which is null.");
      }
    } else {
      logger.error("Got profileField of type ServiceProfile which is null.");
    }

    return serviceProfile;
  }

  public static String getResultCode(int successCount, int failedCount, int partialSuccessCount,
      int noNeedToRunBusinessMethodCount, int itemSize) {
    String resultCode = null;
    if (failedCount > 0) {
      if (successCount > 0 || partialSuccessCount > 0) {
        //部分成功
        resultCode = KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS;
      } else {
        //失败
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
      }
    } else if (successCount > 0) {
      if (partialSuccessCount > 0) {
        //部分成功
        resultCode = KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS;
      } else {
        //成功
        resultCode = KiraCommonConstants.RESULT_CODE_SUCCESS;
      }
    } else if (partialSuccessCount > 0) {
      //部分成功
      resultCode = KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS;
    } else if (noNeedToRunBusinessMethodCount > 0 && noNeedToRunBusinessMethodCount == itemSize) {
      resultCode = KiraCommonConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD;
    }
    return resultCode;
  }

  public static HandleResult handleJobCancelTaskList(TriggerIdentity triggerIdentity,
      List<JobCancelTask> jobCancelTaskList) {
    HandleResult outerHandleResult = null;
    ExecutorService executorService = null;
    int successCount = 0;
    int failedCount = 0;
    int partialSuccessCount = 0;
    Exception outerExceptionOccured = null;
    StringBuilder aggregatedResultData = new StringBuilder();
    String triggerId = triggerIdentity.getTriggerId();
    try {
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-jobCancelTaskList-" + triggerId + "-");
      threadFactory.setDaemon(false);
      executorService = Executors.newFixedThreadPool(jobCancelTaskList.size(), threadFactory);
      //CompletionService<JobItemHandleResult> completionService = new ExecutorCompletionService<JobItemHandleResult>(executorService);
      List<Future<HandleResult>> futures = executorService
          .invokeAll(jobCancelTaskList, KiraCommonConstants.JOB_ITEM_HANDLE_TIMEOUT_SECOND,
              TimeUnit.SECONDS);
      Iterator<JobCancelTask> jobCancelTaskIterator = jobCancelTaskList.iterator();
      JobCancelTask jobCancelTask = null;
      int number = 1;
      boolean isNeedAggregateResultData = false;
      if (futures.size() > 1) {
        isNeedAggregateResultData = true;
      }
      for (Future<HandleResult> future : futures) {
        jobCancelTask = jobCancelTaskIterator.next();
        JobCancelRequest jobCancelRequest = jobCancelTask.getJobCancelContextData()
            .getJobCancelRequest();
        Exception innerExceptionOccured = null;
        HandleResult handleResult = null;
        String resultCode = null;
        String resultData = null;
        try {
          handleResult = future.get();
          if (null != handleResult) {
            resultCode = handleResult.getResultCode();
            resultData = handleResult.getResultData();
          } else {
            resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
            resultData = "handleResult is null. jobCancelRequest=" + KiraCommonUtils
                .toString(jobCancelRequest);
          }
        } catch (InterruptedException e) {
          innerExceptionOccured = e;
          logger.error("InterruptedException occurs for jobCancelRequest=" + KiraCommonUtils
              .toString(jobCancelTask), e);
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          innerExceptionOccured = e;
          logger
              .error("Error occurs for jobCancelRequest=" + KiraCommonUtils.toString(jobCancelTask),
                  e);
        } finally {
          if (null != innerExceptionOccured) {
            String exceptionDesc = ExceptionUtils.getFullStackTrace(innerExceptionOccured);
            resultData = "Error occurs on jobCancelTask. exceptionDesc=" + exceptionDesc;
            resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
          }

          if (KiraCommonConstants.RESULT_CODE_SUCCESS.equals(resultCode)) {
            successCount++;
          } else if (KiraCommonConstants.RESULT_CODE_FAILED.equals(resultCode)) {
            failedCount++;
          } else if (KiraCommonConstants.RESULT_CODE_PARTIAL_SUCCESS.equals(resultCode)) {
            partialSuccessCount++;
          }

          if (isNeedAggregateResultData) {
            aggregatedResultData.append((number + ". "))
                .append(resultData == null ? "" : resultData);
            aggregatedResultData.append(KiraCommonConstants.LINE_SEPARATOR);
            number++;
          } else {
            aggregatedResultData.append(resultData == null ? "" : resultData);
          }
        }
      }
    } catch (Exception e) {
      outerExceptionOccured = e;
      logger.error("Error occurs on handleJobCancelTaskList. triggerIdentity=" + KiraCommonUtils
          .toString(triggerIdentity) + " and jobCancelTaskList=" + KiraCommonUtils
          .toString(jobCancelTaskList), e);
    } finally {
      if (null != executorService) {
        executorService.shutdown();
      }

      String resultCode = KiraCommonUtils
          .getResultCode(successCount, failedCount, partialSuccessCount, 0,
              jobCancelTaskList.size());
      String resultData = aggregatedResultData.toString();

      if (null != outerExceptionOccured) {
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(outerExceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Error occurs on handleJobCancelTaskList. exceptionDesc=" + exceptionDesc;
      }

      outerHandleResult = new HandleResult(resultCode, resultData);
    }

    return outerHandleResult;
  }

  public static String getDateAsStringToMsPrecision(Date date) {
    String returnValue = getDateAsString(date, KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS);
    return returnValue;
  }

  public static String getDateAsString(Date date) {
    String returnValue = getDateAsString(date, KiraCommonConstants.DATEFORMAT_DEFAULT);
    return returnValue;
  }

  public static String getDateAsString(Date date, String dateFormat) {
    String returnValue = null;
    try {
      if (null != date) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        returnValue = sdf.format(date);
      }
    } catch (Exception e) {
      logger
          .error("Error occurs when getDateAsString date=" + date + " and dateFormat=" + dateFormat,
              e);
    }
    return returnValue;
  }

  public static String getLongInMsOfDateAsString(Long dateInMs) {
    String returnValue = null;
    if (null != dateInMs) {
      Date date = new Date(dateInMs.longValue());
      returnValue = KiraCommonUtils.getDateAsString(date);
    }

    return returnValue;
  }

  public static Date getDateFromString(String dateAsString, boolean throwOutException)
      throws ParseException {
    Date returnValue = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(KiraCommonConstants.DATEFORMAT_DEFAULT);
      returnValue = sdf.parse(dateAsString);
    } catch (ParseException e) {
      logger.error("Error occurs when getDateFromString. dateAsString=" + dateAsString + ",format="
          + KiraCommonConstants.DATEFORMAT_DEFAULT, e);
      if (throwOutException) {
        throw e;
      }
    }
    return returnValue;
  }

  /**
   * @deprecated
   */
  public static List<TriggerIdentity> getAllTriggerIdentityOfPoolOnZK_Old(String requestPoolId) {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
        List<String> triggerList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
        if (!CollectionUtils.isEmpty(triggerList)) {
          String oneTriggerPath = null;
          TriggerMetadataZNodeData triggerMetadataZNodeData = null;
          TriggerIdentity triggerIdentity = null;
          for (String oneTriggerShortPath : triggerList) {
            oneTriggerPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, oneTriggerShortPath);
            triggerMetadataZNodeData = zkClient.readData(oneTriggerPath, true);
            if (null != triggerMetadataZNodeData) {
              String poolId = triggerMetadataZNodeData.getAppId();
              if (requestPoolId.equals(poolId)) {
                String triggerId = triggerMetadataZNodeData.getTriggerId();
                //Take version info here for some purpose.
                String version = triggerMetadataZNodeData.getVersion();
                triggerIdentity = new TriggerIdentity(poolId, triggerId, version);
                returnValue.add(triggerIdentity);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs for getAllTriggerIdentityOfPoolOnZK_Old.", e);
    }

    return returnValue;
  }

  public static List<TriggerIdentity> getAllTriggerIdentityOfPoolOnZK(String requestPoolId,
      boolean includeVersion, boolean filterOutManuallyCreated) throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      String filteredPoolId = KiraUtil.filterString(requestPoolId);
      String onePoolPath = KiraUtil
          .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, filteredPoolId);
      if (zkClient.exists(onePoolPath)) {
        List<String> triggerList = zkClient.getChildren(onePoolPath);
        if (!CollectionUtils.isEmpty(triggerList)) {
          String oneTriggerPath = null;
          TriggerMetadataZNodeData triggerMetadataZNodeData = null;
          TriggerIdentity triggerIdentity = null;
          for (String oneTriggerShortPath : triggerList) {
            oneTriggerPath = KiraUtil.getChildFullPath(onePoolPath, oneTriggerShortPath);
            triggerMetadataZNodeData = zkClient.readData(oneTriggerPath, true);
            if (null != triggerMetadataZNodeData) {
              if (filterOutManuallyCreated) {
                Boolean manuallyCreated = triggerMetadataZNodeData.getManuallyCreated();
                if (Boolean.TRUE.equals(manuallyCreated)) {
                  continue;
                }
              }

              String poolId = triggerMetadataZNodeData.getAppId();
              if (requestPoolId.equals(poolId)) {
                String triggerId = triggerMetadataZNodeData.getTriggerId();
                if (includeVersion) {
                  //Take version info here for some purpose.
                  String version = triggerMetadataZNodeData.getVersion();
                  triggerIdentity = new TriggerIdentity(poolId, triggerId, version);
                } else {
                  triggerIdentity = new TriggerIdentity(poolId, triggerId);
                }

                returnValue.add(triggerIdentity);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for getAllTriggerIdentityOfPoolOnZK. requestPoolId=" + requestPoolId
              + " and includeVersion=" + includeVersion, e);
      throw e;
    }

    return returnValue;
  }

  /**
   * @deprecated
   */
  public static List<TriggerIdentity> getAllTriggerIdentityOnZK_Old(boolean includeVersion)
      throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
        List<String> triggerList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
        if (!CollectionUtils.isEmpty(triggerList)) {
          String oneTriggerPath = null;
          TriggerMetadataZNodeData triggerMetadataZNodeData = null;
          TriggerIdentity triggerIdentity = null;
          for (String oneTriggerShortPath : triggerList) {
            oneTriggerPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, oneTriggerShortPath);
            triggerMetadataZNodeData = zkClient.readData(oneTriggerPath, true);
            if (null != triggerMetadataZNodeData) {
              String poolId = triggerMetadataZNodeData.getAppId();
              String triggerId = triggerMetadataZNodeData.getTriggerId();
              if (includeVersion) {
                //Take version info here for some purpose.
                String version = triggerMetadataZNodeData.getVersion();
                triggerIdentity = new TriggerIdentity(poolId, triggerId, version);
              } else {
                triggerIdentity = new TriggerIdentity(poolId, triggerId);
              }

              returnValue.add(triggerIdentity);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs for getAllTriggerIdentityOnZK.", e);
      throw e;
    }

    return returnValue;
  }

  public static List<TriggerIdentity> getAllTriggerIdentityUnderPoolZNode(String poolZNodePath,
      boolean includeVersion) throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      if (zkClient.exists(poolZNodePath)) {
        List<String> triggerList = zkClient.getChildren(poolZNodePath);
        if (!CollectionUtils.isEmpty(triggerList)) {
          String oneTriggerPath = null;
          TriggerMetadataZNodeData triggerMetadataZNodeData = null;
          TriggerIdentity triggerIdentity = null;
          for (String oneTriggerShortPath : triggerList) {
            oneTriggerPath = KiraUtil.getChildFullPath(poolZNodePath, oneTriggerShortPath);
            triggerMetadataZNodeData = zkClient.readData(oneTriggerPath, true);
            if (null != triggerMetadataZNodeData) {
              String poolId = triggerMetadataZNodeData.getAppId();
              String triggerId = triggerMetadataZNodeData.getTriggerId();
              if (includeVersion) {
                //Take version info here for some purpose.
                String version = triggerMetadataZNodeData.getVersion();
                triggerIdentity = new TriggerIdentity(poolId, triggerId, version);
              } else {
                triggerIdentity = new TriggerIdentity(poolId, triggerId);
              }

              returnValue.add(triggerIdentity);
            }
          }
        }
      } else {
        logger.warn(
            "poolZNodePath do not exist for getAllTriggerIdentityUnderPoolZNode. poolZNodePath={} and includeVersion={}",
            poolZNodePath, includeVersion);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for getAllTriggerIdentityUnderPoolZNode. poolZNodePath=" + poolZNodePath
              + " and includeVersion=" + includeVersion, e);
      throw e;
    }
    return returnValue;
  }

  public static Set<TriggerIdentity> getVersionClearedTriggerIdentitySet(
      Collection<TriggerIdentity> triggerIdentitys) {
    Set<TriggerIdentity> returnValue = new LinkedHashSet<TriggerIdentity>();
    if (!CollectionUtils.isEmpty(triggerIdentitys)) {
      TriggerIdentity newTriggerIdentity = null;
      for (TriggerIdentity triggerIdentity : triggerIdentitys) {
        newTriggerIdentity = new TriggerIdentity();
        BeanUtils.copyProperties(triggerIdentity, newTriggerIdentity);
        newTriggerIdentity.setVersion(null);
        returnValue.add(newTriggerIdentity);
      }
    }
    return returnValue;
  }

  public static boolean isExpired(Date checkedTime, long thresholdMillisecond) {
    boolean returnValue = false;
    if (null != checkedTime) {
      Date currentTime = new Date();
      Date expiredTime = new Date(checkedTime.getTime() + thresholdMillisecond);
      if (currentTime.after(expiredTime)) {
        returnValue = true;
      }
    }
    return returnValue;
  }

  public static boolean isAfterExpectedFutureTime(Date checkedTime, long intervalMillisecond) {
    boolean returnValue = false;
    if (null != checkedTime) {
      Date futureTime = new Date(System.currentTimeMillis() + intervalMillisecond);
      if (checkedTime.after(futureTime)) {
        returnValue = true;
      }
    }
    return returnValue;
  }

  public static boolean isMoreThanTheTimeIntervalInMs(Date comparater, Date comparatee,
      long timeInterValInMs) throws Exception {
    boolean returnValue = false;
    if (null != comparater && null != comparatee) {
      if (timeInterValInMs < 0) {
        throw new ValidationException(
            "timeInterValInMs should be >=0 . timeInterValInMs=" + timeInterValInMs);
      }

      long comparaterTimeInMs = comparater.getTime();
      long comparateeTimeInMs = comparatee.getTime();
      if ((comparaterTimeInMs - comparateeTimeInMs) > timeInterValInMs) {
        returnValue = true;
      }
    } else {
      throw new ValidationException(
          "both comparater and comparatee should not be null. comparater=" + comparater
              + " and comparatee=" + comparatee);
    }

    return returnValue;
  }

  public static void deleteTriggerZKNode(TriggerIdentity triggerIdentity) throws Exception {
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
    if (zkClient.exists(triggerZNodeZKPath)) {
      zkClient.deleteRecursive(triggerZNodeZKPath);
      logger.info("Delete the trigger from zk: triggerIdentity={}", triggerIdentity);
    } else {
      logger
          .info("When try to delete the trigger from zk: triggerIdentity={} , but it do not exist.",
              triggerIdentity);
    }
  }

  public static void deletePoolZKNode(String appId) throws Exception {
    String poolZNodeZKPath = KiraCommonUtils.getPoolZNodeZKPathForTrigger(appId);
    if (zkClient.exists(poolZNodeZKPath)) {
      zkClient.deleteRecursive(poolZNodeZKPath);
      logger.info("Delete the trigger from zk: appId={} and poolZNodeZKPath={}", appId,
          poolZNodeZKPath);
    } else {
      logger.info(
          "When try to delete the trigger from zk: appId={} and poolZNodeZKPath={} , but it do not exist.",
          appId, poolZNodeZKPath);
    }
  }

  /**
   * @param date which is used to calculate timestamp
   * @return the new version which is based on oldVersion. With the format like:
   * XXXX-SNAPSHOT-yyyyMMdd.HHmmss.SSS
   */
  public static String getNewVersion(String oldVersion, Date date) {
    String returnValue = null;
    if (StringUtils.isNotBlank(oldVersion)) {
      String versionWithoutSNAPSHOT = getVersionWithoutSNAPSHOTForKiraTimerTrigger(oldVersion);
      returnValue = versionWithoutSNAPSHOT + KiraCommonConstants.STRING_SNAPSHOT + KiraCommonUtils
          .getDateAsString(date, KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION);
    }
    return returnValue;
  }

  public static List<String> getStringListByDelmiter(String stringValue, String delimiter) {
    List<String> returnValue = new ArrayList<String>();
    Set<String> returnValueAsSet = new LinkedHashSet<String>();
    if (StringUtils.isNotBlank(stringValue)) {
      String[] stringValueAsArray = StringUtils.split(stringValue.trim(), delimiter);
      if (null != stringValueAsArray) {
        for (String oneString : stringValueAsArray) {
          if (null != oneString) {
            returnValueAsSet.add(oneString.trim());
          }
        }
      }
    }
    returnValue.addAll(returnValueAsSet);
    return returnValue;
  }

  public static URL getCodeSourceLocationAsURL(Class theClass) {
    URL returnValue = null;
    try {
      returnValue = theClass.getProtectionDomain().getCodeSource().getLocation();
    } catch (Exception e) {
      logger.error("Error occurs for getCodeSourceLocationAsURL by theClass=" + theClass, e);
    }
    return returnValue;
  }

  public static String getCodeSourceLocationPathAsString(Class theClass) {
    String returnValue = null;
    try {
      URL url = KiraCommonUtils.getCodeSourceLocationAsURL(theClass);
      if (null != url) {
        String defaultCharsetAsString = Charset.defaultCharset().name();
        logger.info("defaultCharsetAsString={}", defaultCharsetAsString);
        returnValue = URLDecoder.decode(url.getPath(), defaultCharsetAsString);
      }
    } catch (Exception e) {
      logger.error("Error occurs for getCodeSourceLocationAsString by theClass=" + theClass, e);
    }
    return returnValue;
  }

  public static String getValueFromManifestInZipFileofClassByKey(Class theClass, String key) {
    String returnValue = null;
    String codeSourceLocationPathAsString = KiraCommonUtils
        .getCodeSourceLocationPathAsString(theClass);
    if (StringUtils.isNotBlank(codeSourceLocationPathAsString)) {
      returnValue = KiraCommonUtils
          .getValueFromManifestInZipFileByKey(codeSourceLocationPathAsString, key);
    }
    return returnValue;
  }

  public static Map<String, String> getManifestInZipFileOfClassAsMap(Class theClass) {
    Map<String, String> returnValue = new LinkedHashMap<String, String>();
    String codeSourceLocationPathAsString = KiraCommonUtils
        .getCodeSourceLocationPathAsString(theClass);
    if (StringUtils.isNotBlank(codeSourceLocationPathAsString)) {
      returnValue = KiraCommonUtils.getManifestInZipFileAsMap(codeSourceLocationPathAsString);
    }
    return returnValue;
  }

  /**
   * @param zipFilePath e.g. "/C:/XXX/kira-client-0.4.0-SNAPSHOT.jar";
   * @param key the key in MANIFEST.MF file
   */
  public static String getValueFromManifestInZipFileByKey(String zipFilePath, String key) {
    String returnValue = null;
    Map<String, String> keyValueMap = KiraCommonUtils.getManifestInZipFileAsMap(zipFilePath);
    returnValue = keyValueMap.get(key);
    return returnValue;
  }

  /**
   * @param zipFilePath e.g. "/C:/XXX/kira-client-0.4.0-SNAPSHOT.jar";
   */
  public static Map<String, String> getManifestInZipFileAsMap(String zipFilePath) {
    Map<String, String> returnValue = new LinkedHashMap<String, String>();
    try {
      ZipFile zipFile = null;
      InputStream zipEntryInputStream = null;
      BufferedReader bufferedReader = null;
      try {
        zipFile = new ZipFile(zipFilePath);
        if (null != zipFile) {
          ZipEntry zipEntry = zipFile.getEntry("META-INF/MANIFEST.MF");
          if (null != zipEntry) {
            zipEntryInputStream = zipFile.getInputStream(zipEntry);
            if (null != zipEntryInputStream) {
              bufferedReader = new BufferedReader(
                  new InputStreamReader(zipEntryInputStream, KiraCommonConstants.CHARSET_UTF8));
              if (null != bufferedReader) {
                String oneLine;
                String key = null;
                StringBuilder finalValueSB = null;
                while ((oneLine = bufferedReader.readLine()) != null) {
                  if (StringUtils.isNotBlank(oneLine)) {
                    String[] splitArray = oneLine.split(":", 2);
                    if (null != splitArray) {
                      if (splitArray.length == 2) {
                        if (StringUtils.isNotBlank(splitArray[0])) {
                          //end last scan
                          if (StringUtils.isNotBlank(key)) {
                            returnValue.put(key, finalValueSB.toString());
                          }

                          //initialize data first
                          key = null;
                          finalValueSB = new StringBuilder();

                          key = splitArray[0].trim();
                        }
                        if (null != splitArray[1]) {
                          finalValueSB.append(splitArray[1].trim());
                        }
                      } else {
                        if (null != key) {
                          finalValueSB.append(oneLine.trim());
                        }
                      }
                    }
                  }
                }

                //end final scan
                if (StringUtils.isNotBlank(key)) {
                  returnValue.put(key, finalValueSB.toString());
                }
              }
            }
          }
        }
      } finally {
        if (null != bufferedReader) {
          try {
            bufferedReader.close();
          } catch (IOException e) {
            logger
                .error("IOException for bufferedReader.close() and zipFilePath=" + zipFilePath, e);
          }
        }
        if (null != zipEntryInputStream) {
          try {
            zipEntryInputStream.close();
          } catch (IOException e) {
            logger
                .error("IOException for zipEntryInputStream.close() and zipFilePath=" + zipFilePath,
                    e);
          }
        }
        if (null != zipFile) {
          try {
            zipFile.close();
          } catch (IOException e) {
            logger.error("IOException for zipFile.close() and zipFilePath=" + zipFilePath, e);
          }
        }
      }

      if (logger.isDebugEnabled()) {
        for (Map.Entry<String, String> entry : returnValue.entrySet()) {
          if (null != entry) {
            logger.debug("{}={}", entry.getKey(), entry.getValue());
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs for getManifestInZipFileAsMap and zipFilePath=" + zipFilePath, e);
    }

    return returnValue;
  }

  public static <T> T retryUntilNotNullOrTimeout(Callable<T> callable, long timeoutInMilliseconds)
      throws KiraTimeoutException, KiraHandleException {
    T returnValue = null;
    if (null == callable) {
      throw new KiraHandleException("callable should not be null for retryUntilTimeout");
    }

    try {
      int waittime = 0;
      while (null == returnValue) {
        if (waittime > timeoutInMilliseconds) {
          throw new KiraTimeoutException(
              "retryUntilNotNullOrTimeout can not finish in " + timeoutInMilliseconds
                  + " milliseconds.");
        }
        returnValue = callable.call();
        if (null == returnValue) {
          Thread.sleep(100);
          waittime += 100;
        }
      }
    } catch (KiraTimeoutException e) {
      logger.error("TimeoutException occurs when retryUntilTimeout.", e);
      throw e;
    } catch (Exception e) {
      logger.error("Exception occurs when retryUntilTimeout.", e);
      throw new KiraHandleException(e);
    }

    return returnValue;
  }

  public static LinkedHashMap sortByValue(Map map, final boolean reverse) {
    List list = new LinkedList(map.entrySet());
    Collections.sort(list, new Comparator() {
      public int compare(Object o1, Object o2) {
        if (reverse) {
          return -((Comparable) ((Map.Entry) (o1)).getValue())
              .compareTo(((Map.Entry) (o2)).getValue());
        }
        return ((Comparable) ((Map.Entry) (o1)).getValue())
            .compareTo(((Map.Entry) (o2)).getValue());
      }
    });

    LinkedHashMap result = new LinkedHashMap();
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }

  public static <K, V> Map<K, List<V>> getMergedMap(Map<K, List<V>> map1, Map<K, List<V>> map2) {
    Map<K, List<V>> returnValue = null;
    if (null != map1) {
      returnValue = new HashMap<K, List<V>>(map1);
    }

    if (null != map2) {
      for (Entry<K, List<V>> entry : map2.entrySet()) {
        K key = entry.getKey();
        List<V> value = entry.getValue();

        if (null != value) {
          if (returnValue.containsKey(key)) {
            returnValue.get(key).addAll(value);
          } else {
            returnValue.put(key, value);
          }
        }
      }
    }

    if (null == returnValue) {
      returnValue = new HashMap<K, List<V>>();
    }
    return returnValue;
  }

  public static Object getCallableTaskObjectFromFutureTask(FutureTask futureTask) throws Exception {
    Object returnValue = null;
    if (null != futureTask) {
      Class<?> futureTaskClass = FutureTask.class;
      Field syncField = futureTaskClass.getDeclaredField("sync");
      if (null != syncField) {
        syncField.setAccessible(true);
        Object syncObject = syncField.get(futureTask);
        if (null != syncObject) {
          Class<?> syncFieldDeclaringClass = syncField.getType();
          if (null != syncFieldDeclaringClass) {
            Field callablecField = syncFieldDeclaringClass.getDeclaredField("callable");
            if (null != callablecField) {
              callablecField.setAccessible(true);
              returnValue = callablecField.get(syncObject);
            }
          }
        }
      }
    }

    return returnValue;
  }

  /**
   * 定时任务对应的业务方法是否正在执行中。
   */
  public static boolean isKiraTimerTriggerBusinessRunning(String appId, String triggerId)
      throws KiraHandleException {
    boolean returnValue = false;

    try {
      if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId)) {
        String parentZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId)
            + KiraCommonConstants.ZNODE_NAME_RUNNING;
        if (zkClient.exists(parentZKPath)) {
          List<String> instanceShortPathList = zkClient.getChildren(parentZKPath);
          if (!CollectionUtils.isEmpty(instanceShortPathList)) {
            returnValue = true;
          }
        }
      } else {
        throw new KiraHandleException(
            "Both appId and triggerId should be not blank for calling isKiraTimerTriggerBusinessRunning. appId="
                + appId + " and triggerId=" + triggerId);
      }
    } catch (KiraHandleException k) {
      throw k;
    } catch (Throwable t) {
      logger.error("Error occurs for calling isKiraTimerTriggerBusinessRunning. appId=" + appId
          + " and triggerId=" + triggerId, t);
      throw new KiraHandleException(t);
    }

    return returnValue;
  }

  /**
   * 获取正在执行定时任务业务方法的执行实例列表。
   */
  public static List<KiraTimerTriggerBusinessRunningInstance> getKiraTimerTriggerBusinessRunningInstanceList(
      String appId, String triggerId) throws KiraHandleException {
    List<KiraTimerTriggerBusinessRunningInstance> returnValue = new ArrayList<KiraTimerTriggerBusinessRunningInstance>();
    try {
      if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId)) {
        String parentZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId)
            + KiraCommonConstants.ZNODE_NAME_RUNNING;
        if (zkClient.exists(parentZKPath)) {
          List<String> instanceShortPathList = zkClient.getChildren(parentZKPath);
          if (!CollectionUtils.isEmpty(instanceShortPathList)) {
            String oneInstanceFullPath = null;
            KiraTimerTriggerBusinessRunningInstance kiraTimerTriggerBusinessRunningInstance = null;
            for (String oneInstanceShortPath : instanceShortPathList) {
              oneInstanceFullPath = KiraUtil.getChildFullPath(parentZKPath, oneInstanceShortPath);
              kiraTimerTriggerBusinessRunningInstance = zkClient
                  .readData(oneInstanceFullPath, true);
              if (null != kiraTimerTriggerBusinessRunningInstance) {
                returnValue.add(kiraTimerTriggerBusinessRunningInstance);
              } else {
                logger.warn(
                    "kiraTimerTriggerBusinessRunningInstance is null. May have some bugs. poolId={} and triggerId={} and oneInstanceFullPath={}",
                    appId, triggerId, oneInstanceFullPath);
              }
            }
          }
        }
      } else {
        throw new KiraHandleException(
            "Both appId and triggerId should be not blank. appId=" + appId + " and triggerId="
                + triggerId);
      }
    } catch (KiraHandleException k) {
      throw k;
    } catch (Throwable t) {
      logger.error(
          "Error occurs when getKiraTimerTriggerBusinessRunningInstanceList. appId=" + appId
              + " and triggerId=" + triggerId, t);
      throw new KiraHandleException(t);
    }

    return returnValue;
  }

  public static String getTrimedZKPath(String originalZKPath) {
    String returnValue = null;
    if (null != originalZKPath) {
      returnValue = originalZKPath.trim();
      if (returnValue.endsWith(KiraCommonConstants.ZNODE_NAME_PREFIX) && returnValue.length() > 1) {
        returnValue = returnValue.substring(0, returnValue.length() - 1);
      }
    }
    return returnValue;
  }

  public static String getParentZKFullPath(String zkFullPath) {
    String returnValue = null;
    if (null != zkFullPath) {
      String trimedZKFullPath = zkFullPath.trim();
      int trimedZKFullPathLength = trimedZKFullPath.length();
      while (trimedZKFullPathLength > 1 && trimedZKFullPath.endsWith("/")) {
        trimedZKFullPath = trimedZKFullPath.substring(0, trimedZKFullPathLength - 1);
        trimedZKFullPathLength = trimedZKFullPath.length();
      }
      int lastIndexOfPathCharacter = trimedZKFullPath.lastIndexOf("/");
      if (-1 == lastIndexOfPathCharacter) {
        returnValue = trimedZKFullPath;
      } else {
        if (0 == lastIndexOfPathCharacter) {
          //Top level path
          returnValue = "/";
        } else {
          returnValue = trimedZKFullPath.substring(0, lastIndexOfPathCharacter);
        }
      }
    }

    return returnValue;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

  }

}
