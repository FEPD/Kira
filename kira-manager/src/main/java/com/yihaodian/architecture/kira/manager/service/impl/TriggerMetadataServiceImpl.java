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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.TriggerTypeEnum;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.IKiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.util.TimerTriggerStateEnum;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerUtils;
import com.yihaodian.architecture.kira.manager.criteria.JobRunStatisticsCriteria;
import com.yihaodian.architecture.kira.manager.criteria.TimerTriggerScheduleCriteria;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.dao.TriggerMetadataDao;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobRunStatistics;
import com.yihaodian.architecture.kira.manager.domain.TimerTriggerSchedule;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.PoolTriggerStatus;
import com.yihaodian.architecture.kira.manager.dto.TriggerEnvironmentDetailData;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.dto.TriggerPredictReportLineData;
import com.yihaodian.architecture.kira.manager.dto.UpdateTriggerResult;
import com.yihaodian.architecture.kira.manager.service.JobRunStatisticsService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerConstants;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import com.yihaodian.architecture.kira.schedule.time.callback.KiraTimeScheduleCallbackAdaptor;
import com.yihaodian.architecture.kira.schedule.time.trigger.CronExpression;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraCronTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraSimpleTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.utils.TimeScheduleUtils;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class TriggerMetadataServiceImpl extends Service implements TriggerMetadataService {

  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private TriggerMetadataDao triggerMetadataDao;
  private JobService jobService;
  private KiraClientMetadataService kiraClientMetadataService;
  private TriggerMetadataService triggerMetadataService;
  private TimerTriggerScheduleService timerTriggerScheduleService;
  private JobRunStatisticsService jobRunStatisticsService;

  public void setTriggerMetadataDao(TriggerMetadataDao triggerMetadataDao) {
    this.triggerMetadataDao = triggerMetadataDao;
  }

  public JobService getJobService() {
    return jobService;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public KiraClientMetadataService getKiraClientMetadataService() {
    return kiraClientMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public TriggerMetadataService getTriggerMetadataService() {
    return triggerMetadataService;
  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public TimerTriggerScheduleService getTimerTriggerScheduleService() {
    return timerTriggerScheduleService;
  }

  public void setTimerTriggerScheduleService(
      TimerTriggerScheduleService timerTriggerScheduleService) {
    this.timerTriggerScheduleService = timerTriggerScheduleService;
  }

  public JobRunStatisticsService getJobRunStatisticsService() {
    return jobRunStatisticsService;
  }

  public void setJobRunStatisticsService(JobRunStatisticsService jobRunStatisticsService) {
    this.jobRunStatisticsService = jobRunStatisticsService;
  }

  public void insert(TriggerMetadata triggerMetadata) {
    triggerMetadataDao.insert(triggerMetadata);
  }

  public int update(TriggerMetadata triggerMetadata) {
    int actualRowsAffected = 0;

    Long id = triggerMetadata.getId();

    TriggerMetadata _oldTriggerMetadata = triggerMetadataDao.select(id);

    if (_oldTriggerMetadata != null) {
      actualRowsAffected = triggerMetadataDao.update(triggerMetadata);
    }

    return actualRowsAffected;
  }

  public int delete(Long id) {
    int actualRowsAffected = 0;

    TriggerMetadata _oldTriggerMetadata = triggerMetadataDao.select(id);

    if (_oldTriggerMetadata != null) {
      actualRowsAffected = triggerMetadataDao.delete(id);
    }

    return actualRowsAffected;
  }

  public TriggerMetadata select(Long id) {
    return triggerMetadataDao.select(id);
  }

  public List<TriggerMetadata> list(TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.list(triggerMetadataCriteria);
  }

  public List<TriggerMetadata> listOnPage(TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.listOnPage(triggerMetadataCriteria);
  }

  @Override
  public List<TriggerMetadata> listLatest(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.listLatest(triggerMetadataCriteria);
  }

  @Override
  public List<TriggerMetadata> listLatestOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.listLatestOnPageUsingLimit(triggerMetadataCriteria);
  }

  @Override
  public Map<String, Set<String>> getPoolIdTriggerIdSetMapOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    Map<String, Set<String>> poolIdTriggerIdSetMap = new LinkedHashMap<String, Set<String>>();
    List<TriggerIdentity> triggerIdentityList = triggerMetadataDao
        .getTriggerIdentityListOnPage(triggerMetadataCriteria);
    if (!CollectionUtils.isEmpty(triggerIdentityList)) {
      String appId = null;
      String triggerId = null;
      for (TriggerIdentity triggerIdentity : triggerIdentityList) {
        appId = triggerIdentity.getAppId();
        triggerId = triggerIdentity.getTriggerId();
        Set<String> triggerIdSet = poolIdTriggerIdSetMap.get(appId);
        if (null == triggerIdSet) {
          triggerIdSet = new LinkedHashSet<String>();
        }
        triggerIdSet.add(triggerId);
        poolIdTriggerIdSetMap.put(appId, triggerIdSet);
      }
    }

    return poolIdTriggerIdSetMap;
  }

  @Override
  public PoolTriggerStatus getPoolTriggerStatus(TriggerMetadataCriteria criteria) {
    String appId = criteria.getAppId();
    String triggerId = criteria.getTriggerId();
    PoolTriggerStatus returnValue = this.getPoolTriggerStatus(appId, triggerId);
    return returnValue;
  }

  private PoolTriggerStatus getPoolTriggerStatus(String appId, String triggerId) {
    PoolTriggerStatus returnValue = null;

    try {
      TimerTriggerSchedule timerTriggerSchedule = timerTriggerScheduleService
          .getTimerTriggerScheduleByPoolIdAndTriggerId(appId, triggerId);
      if (null == timerTriggerSchedule) {
        TriggerMetadata triggerMetadata = this.triggerMetadataService
            .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
        if (null != triggerMetadata) {
          boolean masterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
          boolean canBeScheduled = KiraManagerUtils.isCanBeScheduled(triggerMetadata, masterZone);
          if (canBeScheduled) {
            returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
                TimerTriggerStateEnum.STATE_ERROR);
            logger.warn(
                "The trigger can be scheduled but it it not under schedule now. appId={} and triggerId={}",
                appId, triggerId);
          } else {
            returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
                TimerTriggerStateEnum.STATE_NONE);
          }
        } else {
          logger.warn(
              "Can not get triggerMetadata by appId=" + appId + " and triggerId=" + triggerId);
          returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
              TimerTriggerStateEnum.STATE_ERROR);
        }
      } else {
        String assignedServerId = timerTriggerSchedule.getAssignedServerId();
        if (StringUtils.isBlank(assignedServerId)) {
          logger.warn(
              "The assignedServerId for the trigger should not be null. assignedServerId={} and appId={} and triggerId={}",
              assignedServerId, appId, triggerId);
          returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
              TimerTriggerStateEnum.STATE_ERROR);
        } else {
          boolean kiraTimerTriggerLocalSchedulerStarted = KiraManagerDataCenter
              .getKiraTimerTriggerLocalScheduler().isStarted();
          String myKiraServerId = null;
          if (kiraTimerTriggerLocalSchedulerStarted) {
            myKiraServerId = KiraManagerDataCenter.getKiraServer().getServerId();
          }

          ITimerTrigger timerTrigger = null;
          String timerTriggerId = TimeScheduleUtils.getIdForTimerTrigger(triggerId, appId);
          if (StringUtils.endsWithIgnoreCase(assignedServerId, myKiraServerId)) {
            timerTrigger = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
                .getTimerTrigger(timerTriggerId, true);
            returnValue = getPoolTriggerStatusByTimerTrigger(timerTrigger);
          } else {
            KiraServerEntity kiraServerEntity = KiraManagerDataCenter.getKiraServer()
                .getKiraServerEntityByServerId(assignedServerId);
            if (null != kiraServerEntity) {
              String accessUrlAsString = kiraServerEntity.getAccessUrlAsString();
              IClusterInternalService clusterInternalService = KiraServerUtils
                  .getClusterInternalService(accessUrlAsString);
              timerTrigger = clusterInternalService.getTimerTrigger(timerTriggerId, true);
              returnValue = getPoolTriggerStatusByTimerTrigger(timerTrigger);
            } else {
              logger.warn(
                  "The trigger is assigned to assignedServerId={} but i can not get kiraServerEntity by it now. appId={} and triggerId={}",
                  assignedServerId, appId, triggerId);
              returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
                  TimerTriggerStateEnum.STATE_ERROR);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when getPoolTriggerStatus. appId=" + appId + " and triggerId=" + triggerId,
          e);
    } finally {
      if (null == returnValue) {
        returnValue = new PoolTriggerStatus(appId, triggerId, null, null, null, null, null,
            TimerTriggerStateEnum.STATE_ERROR);
      }
    }

    return returnValue;
  }

  private PoolTriggerStatus getPoolTriggerStatusByTimerTrigger(ITimerTrigger timerTrigger) {
    PoolTriggerStatus returnValue = null;
    if (null != timerTrigger) {
      String appId = timerTrigger.getGroup();
      String triggerId = timerTrigger.getName();
      Date startTime = timerTrigger.getStartTime();
      Date endTime = timerTrigger.getEndTime();
      Date previousFireTime = timerTrigger.getPreviousFireTime();
      Date nextFireTime = timerTrigger.getNextFireTime();
      Date finalFireTime = timerTrigger.getFinalFireTime();

      TimerTriggerStateEnum triggerState = TimerTriggerStateEnum.STATE_NORMAL;
      if (null == nextFireTime) {
        triggerState = TimerTriggerStateEnum.STATE_COMPLETE;
      }
      returnValue = new PoolTriggerStatus(appId, triggerId, startTime, endTime, previousFireTime,
          nextFireTime, finalFireTime, triggerState);
    }

    return returnValue;
  }

  @Override
  public List<TriggerEnvironmentDetailData> getTriggerEnvironmentDetailDataList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    List<TriggerEnvironmentDetailData> returnValue = new ArrayList<TriggerEnvironmentDetailData>();

    String appId = triggerMetadataCriteria.getAppId();
    String triggerId = triggerMetadataCriteria.getTriggerId();
    try {
      TriggerMetadataCriteria triggerMetadataCriteriaForQueryTriggerMetadata = new TriggerMetadataCriteria();
      triggerMetadataCriteriaForQueryTriggerMetadata.setAppId(appId);
      triggerMetadataCriteriaForQueryTriggerMetadata.setTriggerId(triggerId);
      TriggerMetadata triggerMetadata = triggerMetadataService
          .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
      if (null != triggerMetadata) {
        String targetAppId = triggerMetadata.getTargetAppId();
        String targetTriggerId = triggerMetadata.getTargetTriggerId();
        Boolean manuallyCreated = triggerMetadata.getManuallyCreated();
        boolean isInvokeTarget =
            StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId);
        String triggerEnvironmentsZKPath = null;
        if (isInvokeTarget) {
          triggerEnvironmentsZKPath =
              KiraCommonUtils.getTriggerZNodeZKPath(targetAppId, targetTriggerId)
                  + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
        } else {
          triggerEnvironmentsZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId)
              + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
        }

        if (StringUtils.isNotBlank(triggerEnvironmentsZKPath) && zkClient
            .exists(triggerEnvironmentsZKPath)) {
          List<String> triggerEnvironmentChildPathList = zkClient
              .getChildren(triggerEnvironmentsZKPath);
          if (!CollectionUtils.isEmpty(triggerEnvironmentChildPathList)) {
            TriggerEnvironmentDetailData triggerEnvironmentDetailData = null;
            String triggerEnvironmentZKFullPath = null;
            String centralScheduleServiceZKParentPath = null;
            for (String triggerEnvironmentChildZNodeName : triggerEnvironmentChildPathList) {
              triggerEnvironmentZKFullPath = KiraUtil
                  .getChildFullPath(triggerEnvironmentsZKPath, triggerEnvironmentChildZNodeName);
              centralScheduleServiceZKParentPath = zkClient
                  .readData(triggerEnvironmentZKFullPath, true);
              if (StringUtils.isNotBlank(centralScheduleServiceZKParentPath)) {
                if (centralScheduleServiceZKParentPath.contains("//")) {
                  logger.warn(
                      "The centralScheduleServiceZKParentPath contains // which means the serviceAppName may be blank and it is not valid path for zk, so treat it as invalid environment. poolId={} and triggerId={} and centralScheduleServiceZKParentPath={}",
                      appId, triggerId, centralScheduleServiceZKParentPath);
                  triggerEnvironmentDetailData = new TriggerEnvironmentDetailData();
                  triggerEnvironmentDetailData.setAppId(appId);
                  triggerEnvironmentDetailData.setTriggerId(triggerId);
                  triggerEnvironmentDetailData.setManuallyCreated(manuallyCreated);
                  triggerEnvironmentDetailData.setTargetAppId(targetAppId);
                  triggerEnvironmentDetailData.setTargetTriggerId(targetTriggerId);
                  triggerEnvironmentDetailData
                      .setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);
                  triggerEnvironmentDetailData
                      .setCentralScheduleServiceZKParentPath(centralScheduleServiceZKParentPath);

                  triggerEnvironmentDetailData.setServiceUrl(
                      "Failed to get ServiceUrl for centralScheduleServiceZKParentPath is not valid. centralScheduleServiceZKParentPath="
                          + centralScheduleServiceZKParentPath);

                  returnValue.add(triggerEnvironmentDetailData);
                } else {
                  if (zkClient.exists(centralScheduleServiceZKParentPath)) {
                    List<String> centralScheduleServiceChildPathList = zkClient
                        .getChildren(centralScheduleServiceZKParentPath);
                    if (!CollectionUtils.isEmpty(centralScheduleServiceChildPathList)) {
                      ServiceProfile centralScheduleServiceProfile = null;
                      for (String centralScheduleServiceChildZNodeName : centralScheduleServiceChildPathList) {
                        String centralScheduleServiceFullPath = KiraUtil
                            .getChildFullPath(centralScheduleServiceZKParentPath,
                                centralScheduleServiceChildZNodeName);
                        centralScheduleServiceProfile = zkClient
                            .readData(centralScheduleServiceFullPath, true);
                        if (null != centralScheduleServiceProfile) {
                          triggerEnvironmentDetailData = new TriggerEnvironmentDetailData();
                          triggerEnvironmentDetailData.setAppId(appId);
                          triggerEnvironmentDetailData.setTriggerId(triggerId);
                          triggerEnvironmentDetailData.setManuallyCreated(manuallyCreated);
                          triggerEnvironmentDetailData.setTargetAppId(targetAppId);
                          triggerEnvironmentDetailData.setTargetTriggerId(targetTriggerId);
                          triggerEnvironmentDetailData
                              .setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);
                          triggerEnvironmentDetailData.setCentralScheduleServiceZKParentPath(
                              centralScheduleServiceZKParentPath);

                          String hostIp = centralScheduleServiceProfile.getHostIp();
                          triggerEnvironmentDetailData.setHost(hostIp);
                          int port = centralScheduleServiceProfile.getPort();
                          triggerEnvironmentDetailData.setPort(Integer.valueOf(port));
                          String serviceUrl = centralScheduleServiceProfile.getServiceUrl();
                          triggerEnvironmentDetailData.setServiceUrl(serviceUrl);
                          boolean isAvailable = centralScheduleServiceProfile.isAvailable();
                          triggerEnvironmentDetailData
                              .setServiceAvailable(Boolean.valueOf(isAvailable));

                          returnValue.add(triggerEnvironmentDetailData);
                        } else {
                          logger.warn(
                              "centralScheduleServiceProfile is null for centralScheduleServiceFullPath={}",
                              centralScheduleServiceFullPath);
                        }
                      }
                    } else {
                      triggerEnvironmentDetailData = new TriggerEnvironmentDetailData();
                      triggerEnvironmentDetailData.setAppId(appId);
                      triggerEnvironmentDetailData.setTriggerId(triggerId);
                      triggerEnvironmentDetailData.setManuallyCreated(manuallyCreated);
                      triggerEnvironmentDetailData.setTargetAppId(targetAppId);
                      triggerEnvironmentDetailData.setTargetTriggerId(targetTriggerId);
                      triggerEnvironmentDetailData
                          .setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);

                      triggerEnvironmentDetailData.setCentralScheduleServiceZKParentPath(
                          centralScheduleServiceZKParentPath);
                      returnValue.add(triggerEnvironmentDetailData);
                    }
                  } else {
                    triggerEnvironmentDetailData = new TriggerEnvironmentDetailData();
                    triggerEnvironmentDetailData.setAppId(appId);
                    triggerEnvironmentDetailData.setTriggerId(triggerId);
                    triggerEnvironmentDetailData.setManuallyCreated(manuallyCreated);
                    triggerEnvironmentDetailData.setTargetAppId(targetAppId);
                    triggerEnvironmentDetailData.setTargetTriggerId(targetTriggerId);
                    triggerEnvironmentDetailData
                        .setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);

                    triggerEnvironmentDetailData
                        .setCentralScheduleServiceZKParentPath(centralScheduleServiceZKParentPath);
                    returnValue.add(triggerEnvironmentDetailData);
                  }
                }
              } else {
                triggerEnvironmentDetailData = new TriggerEnvironmentDetailData();
                triggerEnvironmentDetailData.setAppId(appId);
                triggerEnvironmentDetailData.setTriggerId(triggerId);
                triggerEnvironmentDetailData.setManuallyCreated(manuallyCreated);
                triggerEnvironmentDetailData.setTargetAppId(targetAppId);
                triggerEnvironmentDetailData.setTargetTriggerId(targetTriggerId);
                triggerEnvironmentDetailData
                    .setTriggerEnvironmentZKFullPath(triggerEnvironmentZKFullPath);

                returnValue.add(triggerEnvironmentDetailData);
              }
            }
          }
        }
      } else {
        logger.warn(
            "triggerMetadata is not available when getTriggerEnvironmentDetailDataList. appId={} and triggerId={}",
            appId, triggerId);
      }
    } catch (Exception e) {
      logger.error("Error occurs when getTriggerEnvironmentDetailDataList. appId=" + appId
          + " and triggerId=" + triggerId, e);
    }
    return returnValue;
  }

  @Override
  public List<String> getPoolIdList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.getPoolIdList(triggerMetadataCriteria);
  }

  @Override
  public List<String> getTriggerIdList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.getTriggerIdList(triggerMetadataCriteria);
  }

  @Override
  public int updateUnRegisteredStatusForTriggers(List<TriggerIdentity> triggerIdentityList,
      Boolean unRegistered) {
    return triggerMetadataDao
        .updateUnRegisteredStatusForTriggers(triggerIdentityList, unRegistered);
  }

  @Override
  public int updateDeletedStatusForTriggers(List<TriggerIdentity> triggerIdentityList,
      Boolean deleted, String comments) {
    return triggerMetadataDao
        .updateDeletedStatusForTriggers(triggerIdentityList, deleted, comments);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.getTriggerIdentityList(triggerMetadataCriteria);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWithoutVersion(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    return triggerMetadataDao.getTriggerIdentityListWithoutVersion(triggerMetadataCriteria);
  }

  @Override
  public List<TriggerIdentity> getAllRegisteredAndUnDeletedTriggerIdentityInDB(
      boolean includeVersion) throws Exception {
    List<TriggerIdentity> returnValue = null;
    try {
      //get all registered and undeleted triggers from db
      TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();

      if (includeVersion) {
        returnValue = triggerMetadataService.getTriggerIdentityList(triggerMetadataCriteria);
      } else {
        returnValue = triggerMetadataService
            .getTriggerIdentityListWithoutVersion(triggerMetadataCriteria);
      }
    } catch (Exception e) {
      logger.error("Error occurs when getAllRegisteredAndUnDeletedTriggerIdentityInDB.", e);
      throw e;
    }

    if (null == returnValue) {
      returnValue = new ArrayList<TriggerIdentity>();
    }
    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getAllCanBeScheduledTriggerIdentityInDB(boolean includeVersion)
      throws Exception {
    List<TriggerIdentity> returnValue = null;
    try {
      TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
      triggerMetadataCriteria.setCanBeScheduled(Boolean.TRUE);
      if (includeVersion) {
        returnValue = triggerMetadataService.getTriggerIdentityList(triggerMetadataCriteria);
      } else {
        returnValue = triggerMetadataService
            .getTriggerIdentityListWithoutVersion(triggerMetadataCriteria);
      }
    } catch (Exception e) {
      logger.error("Error occurs when getAllCanBeScheduledTriggerIdentityInDB.", e);
      throw e;
    }

    if (null == returnValue) {
      returnValue = new ArrayList<TriggerIdentity>();
    }
    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(
      Boolean masterZone) throws Exception {
    return this.triggerMetadataDao
        .getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(masterZone);
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(
      Boolean masterZone) throws Exception {
    return this.triggerMetadataDao
        .getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(masterZone);
  }

  @Override
  public TriggerMetadata registerTriggerMetadataZNode(
      TriggerMetadataZNodeData triggerMetadataZNodeData) {
    String appId = triggerMetadataZNodeData.getAppId();
    String triggerId = triggerMetadataZNodeData.getTriggerId();
    String version = triggerMetadataZNodeData.getVersion();

    //update existing with the same poolId+triggerId as unRegistered first
    TriggerMetadataCriteria triggerMetadataCriteria2 = new TriggerMetadataCriteria();
    triggerMetadataCriteria2.setAppId(appId);
    triggerMetadataCriteria2.setTriggerId(triggerId);
    triggerMetadataCriteria2.setUnregistered(Boolean.FALSE);
    triggerMetadataCriteria2.setDeleted(null);
    int updateCount1 = triggerMetadataDao
        .updateUnRegisteredStatus(triggerMetadataCriteria2, Boolean.TRUE);

    TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
    triggerMetadataCriteria.setAppId(appId);
    triggerMetadataCriteria.setTriggerId(triggerId);
    triggerMetadataCriteria.setVersion(version);
    //should search for all data including unregistered and deleted.
    triggerMetadataCriteria.setUnregistered(null);
    triggerMetadataCriteria.setDeleted(null);
    List<TriggerMetadata> triggerMetadataList = triggerMetadataDao.list(triggerMetadataCriteria);
    TriggerMetadata triggerMetadata = null;
    if (CollectionUtils.isEmpty(triggerMetadataList)) {
      triggerMetadata = new TriggerMetadata();
      BeanUtils.copyProperties(triggerMetadataZNodeData, triggerMetadata);
      triggerMetadata.setCreateTime(new Date());
      triggerMetadataDao.insert(triggerMetadata);
    } else {
      //It is re-registered
      triggerMetadata = triggerMetadataList.get(0);
      BeanUtils.copyProperties(triggerMetadataZNodeData, triggerMetadata);
      Date now = new Date();
      triggerMetadata.setCreateTime(now);
      triggerMetadata.setUnregistered(Boolean.FALSE);
      triggerMetadata.setDeleted(Boolean.FALSE);

      int updateCount2 = triggerMetadataDao.update(triggerMetadata);
    }
    return triggerMetadata;
  }

  @Override
  public List<TriggerMetadata> registerTriggerMetadataZNodeDataList(
      List<TriggerMetadataZNodeData> triggerMetadataZNodeDataList) {
    List<TriggerMetadata> returnValue = new ArrayList<TriggerMetadata>();

    if (!CollectionUtils.isEmpty(triggerMetadataZNodeDataList)) {
      for (TriggerMetadataZNodeData triggerMetadataZNodeData : triggerMetadataZNodeDataList) {
        try {
          TriggerMetadata triggerMetadata = registerTriggerMetadataZNode(triggerMetadataZNodeData);
          returnValue.add(triggerMetadata);
        } finally {
          String appId = triggerMetadataZNodeData.getAppId();
          String triggerId = triggerMetadataZNodeData.getTriggerId();
          KiraManagerCrossMultiZoneUtils
              .cascadeCrossZoneSaveTimerTriggerIfNeeded(appId, triggerId, triggerMetadataZNodeData);
        }
      }
    }

    return returnValue;
  }

  @Override
  public TriggerMetadata getLatestAndAvailableTriggerMetadata(String appId,
      String triggerId, String version) {
    TriggerMetadata returnValue = triggerMetadataDao
        .getLatestAndAvailableTriggerMetadata(appId, triggerId, version);
    return returnValue;
  }

  @Override
  public TriggerMetadata getLatestTriggerMetadata(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    TriggerMetadata returnValue = null;
    List<TriggerMetadata> triggerMetadataList = triggerMetadataDao
        .listLatestOnPageUsingLimit(triggerMetadataCriteria);
    if (!CollectionUtils.isEmpty(triggerMetadataList)) {
      returnValue = triggerMetadataList.get(0);
    }
    return returnValue;
  }

  @Override
  public TriggerMetadata getLatestAndAvailableTriggerMetadataByJobId(String jobId) {
    return triggerMetadataDao.getLatestAndAvailableTriggerMetadataByJobId(jobId);
  }

  @Override
  public List<PoolTriggerStatus> getPoolTriggerStatusListOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    List<PoolTriggerStatus> returnValue = new ArrayList<PoolTriggerStatus>();
    List<TriggerIdentity> triggerIdentityList = triggerMetadataDao
        .getTriggerIdentityListOnPage(triggerMetadataCriteria);
    if (!CollectionUtils.isEmpty(triggerIdentityList)) {
      PoolTriggerStatus poolTriggerStatus = null;
      for (TriggerIdentity triggerIdentity : triggerIdentityList) {
        String appId = triggerIdentity.getAppId();
        String triggerId = triggerIdentity.getTriggerId();
        poolTriggerStatus = this.getPoolTriggerStatus(appId, triggerId);
        returnValue.add(poolTriggerStatus);
      }
    }
    return returnValue;
  }

  @Override
  public void unRegisterTrigger(TriggerIdentity triggerIdentity) throws Exception {
    List<TriggerIdentity> unRegisteredTriggerIdentityList = new ArrayList<TriggerIdentity>();
    unRegisteredTriggerIdentityList.add(triggerIdentity);
    this.updateUnRegisteredStatusForTriggers(unRegisteredTriggerIdentityList, Boolean.TRUE);
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    boolean success = this.unscheduleJobIfNeeded(appId, triggerId);
    if (success) {
      KiraManagerDataCenter.getKiraTimerTriggerMetadataManager()
          .unsubscribeDataChangesForTrigger(triggerIdentity);
    }
  }

  @Override
  public void registerTrigger(TriggerIdentity triggerIdentity,
      IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager)
      throws Exception {
    try {
      //need to update db then update quartz
      List<TriggerIdentity> registeredTriggerIdentityList = new ArrayList<TriggerIdentity>();
      registeredTriggerIdentityList.add(triggerIdentity);
      List<TriggerMetadataZNodeData> triggerMetadataZNodeDataList = KiraManagerUtils
          .getTriggerMetadataZNodeDataList(registeredTriggerIdentityList);
      if (!CollectionUtils.isEmpty(triggerMetadataZNodeDataList)) {
        this.registerTriggerMetadataZNodeDataList(triggerMetadataZNodeDataList);
      }
      boolean success = this.scheduleTriggerIfNeeded(triggerIdentity);
      if (!success) {
        logger
            .error("scheduleTriggerIfNeeded() returns false in registerTrigger. triggerIdentity={}",
                KiraCommonUtils.toString(triggerIdentity));
      }
    } finally {
      //always need to subscribeDataChangesForTriggerOnZK even if scheduled failded.
      if (null != triggerIdentity) {
        //should not use KiraManagerDataCenter.getKiraTimerTriggerMetadataManager() because this method will be called during start.
        //KiraManagerDataCenter.getKiraTimerTriggerMetadataManager().subscribeDataChangesForTrigger(triggerIdentity);
        kiraTimerTriggerMetadataManager.subscribeDataChangesForTrigger(triggerIdentity);
      }
    }
  }

  @Override
  public void handleTriggerZKDataChange(
      TriggerMetadataZNodeData newTriggerMetadataZNodeData)
      throws Exception {
    String appId = newTriggerMetadataZNodeData.getAppId();
    String triggerId = newTriggerMetadataZNodeData.getTriggerId();
    String version = newTriggerMetadataZNodeData.getVersion();
    boolean isTheTriggerWithTheSameVersionExistInDB = isHasTheSameVersionWithTheLatestTriggerInDB(
        new TriggerIdentity(appId, triggerId, version));
    if (isTheTriggerWithTheSameVersionExistInDB) {
      logger.info(
          "isTheTriggerWithTheSameVersionExistInDB=true. So no need to handle newTriggerMetadataZNodeData={}",
          KiraCommonUtils.toString(newTriggerMetadataZNodeData));
    } else {
      List<TriggerMetadataZNodeData> triggerMetadataZNodeDataList = new ArrayList<TriggerMetadataZNodeData>();
      triggerMetadataZNodeDataList.add(newTriggerMetadataZNodeData);
      this.registerTriggerMetadataZNodeDataList(triggerMetadataZNodeDataList);

      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
      boolean success = this.scheduleTriggerIfNeeded(triggerIdentity);
      if (!success) {
        logger.error(
            "scheduleTriggerIfNeeded() returns false in handleTriggerZKDataChange. newTriggerMetadataZNodeData={}",
            KiraCommonUtils.toString(newTriggerMetadataZNodeData));
      }
    }
  }

  private boolean isHasTheSameVersionWithTheLatestTriggerInDB(TriggerIdentity triggerIdentity) {
    boolean returnValue = false;
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    String version = triggerIdentity.getVersion();
    TriggerMetadata triggerMetadata = this
        .getLatestAndAvailableTriggerMetadata(appId, triggerId, version);
    if (null != triggerMetadata) {
      returnValue = true;
    }
    return returnValue;
  }

  @Override
  public boolean doRescheduleJob(TriggerIdentity triggerIdentity) throws Exception {
    boolean returnValue = false;

    IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
        .getKiraTimerTriggerScheduleCenter();
    String appId = triggerIdentity.getAppId();
    String triggerId = triggerIdentity.getTriggerId();
    if (kiraTimerTriggerScheduleCenter.isLeaderServer(true)) {
      kiraTimerTriggerScheduleCenter
          .initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(appId, triggerId);
      returnValue = true;
    } else {
      IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
          .getClusterInternalServiceOfLeader();
      if (null != leaderServerClusterInternalService) {
        leaderServerClusterInternalService
            .initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(appId, triggerId);
        returnValue = true;
      } else {
        throw new KiraHandleException("Can not get the information for leader server.");
      }
    }

    return returnValue;
  }

  @Override
  public HandleResult rescheduleJob(TriggerMetadataCriteria triggerMetadataCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      String appId = triggerMetadataCriteria.getAppId();
      String triggerId = triggerMetadataCriteria.getTriggerId();
      if (StringUtils.isNotBlank(appId)) {
        if (StringUtils.isNotBlank(triggerId)) {
          TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
          boolean success = this.doRescheduleJob(triggerIdentity);
          if (!success) {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData = "doRescheduleJob failed.";
            logger.warn("doRescheduleJob() returns false in rescheduleJob. triggerIdentity={}",
                KiraCommonUtils.toString(triggerIdentity));
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "triggerId should not be empty.";
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "appId should not be empty.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on rescheduleJob. triggerMetadataCriteria=" + KiraCommonUtils
          .toString(triggerMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on rescheduleJob. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public boolean unscheduleJobIfNeeded(String appId, String triggerId) throws Exception {
    boolean returnValue = false;
    try {
      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
      List<TriggerIdentity> triggerIdentityList = new ArrayList<TriggerIdentity>();
      triggerIdentityList.add(triggerIdentity);

      IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
          .getKiraTimerTriggerScheduleCenter();
      if (kiraTimerTriggerScheduleCenter.isLeaderServer(true)) {
        kiraTimerTriggerScheduleCenter
            .handleTriggerIdentityListWhichShouldBeUnScheduled(triggerIdentityList);
        returnValue = true;
      } else {
        IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
            .getClusterInternalServiceOfLeader();
        if (null != leaderServerClusterInternalService) {
          leaderServerClusterInternalService
              .handleTriggerIdentityListWhichShouldBeUnScheduled(triggerIdentityList);
          returnValue = true;
        } else {
          String errorMessage =
              "Can not get the information for leader server. when unscheduleJobIfNeeded for appId="
                  + appId + " triggerId=" + triggerId;
          logger.error(errorMessage);
          throw new KiraHandleException(errorMessage);
        }
      }
    } catch (KiraHandleException e) {
      throw e;
    } catch (Exception e) {
      logger.error(
          "Error occurs for unscheduleJobIfNeeded. appId=" + appId + " and triggerId=" + triggerId,
          e);
      throw e;
    }
    return returnValue;
  }

  @Override
  public void doDeleteTrigger(String appId, String triggerId, String version, String deletedBy)
      throws Exception {
    this.deleteTimerTriggerInDirectly(appId, triggerId, version, deletedBy, true,
        KiraManagerConstants.UI_TIMEOUT_INMILLISECONDS);
  }

  private void deleteTimerTriggerInDirectly(final String appId, final String triggerId,
      final String version, final String deletedBy, boolean waitForSuccess,
      long timeoutInMilliseconds)
      throws Exception {
    try {
      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId, version);
      KiraCommonUtils.deleteTriggerZKNode(triggerIdentity);
    } catch (Exception e) {
      logger.error("Error occurs when deleteTriggerInDirectly. appId=" + appId + " and triggerId="
          + triggerId + " and version=" + version, e);
      throw e;
    }

    if (waitForSuccess) {
      try {
        KiraCommonUtils.retryUntilNotNullOrTimeout(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId, version);
            boolean isExistInDB = TriggerMetadataServiceImpl.this
                .isHasTheSameVersionWithTheLatestTriggerInDB(triggerIdentity);
            if (!isExistInDB) {
              List<TriggerIdentity> triggerIdentityList = new ArrayList<TriggerIdentity>();
              triggerIdentityList.add(triggerIdentity);
              TriggerMetadataServiceImpl.this
                  .updateDeletedStatusForTriggers(triggerIdentityList, Boolean.TRUE,
                      "Deleted by " + deletedBy);
              return Boolean.TRUE;
            } else {
              return null;
            }
          }
        }, timeoutInMilliseconds);
      } catch (Exception e) {
        logger.error(
            "Error occurs when retryUntilNotNullOrTimeout for deleteTriggerInDirectly. appId="
                + appId + " and triggerId=" + triggerId + "and version=" + version, e);
        //do not throw out exception for the znode is already deleted successfully here.
      }
    }
  }

  @Override
  public HandleResult deleteTrigger(
      TriggerMetadataCriteria triggerMetadataCriteria, String deletedBy) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      String appId = triggerMetadataCriteria.getAppId();
      String triggerId = triggerMetadataCriteria.getTriggerId();
      String version = triggerMetadataCriteria.getVersion();
      if (StringUtils.isNotBlank(appId)) {
        if (StringUtils.isNotBlank(triggerId)) {
          if (StringUtils.isNotBlank(version)) {
            TriggerMetadata triggerMetadata = this
                .getLatestAndAvailableTriggerMetadata(appId, triggerId, version);
            if (null != triggerMetadata) {
              Boolean manuallyCreated = triggerMetadata.getManuallyCreated();
              boolean isAdminUser = KiraManagerUtils.isAdminUser(deletedBy);
              if (Boolean.TRUE.equals(manuallyCreated) || isAdminUser) {
                this.doDeleteTrigger(appId, triggerId, version, deletedBy);
                resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
              } else {
                resultCode = KiraServerConstants.RESULT_CODE_FAILED;
                resultData = "Only the manually created trigger can be deleted by non-admin user.";
              }
            } else {
              resultCode = KiraServerConstants.RESULT_CODE_FAILED;
              resultData = "Trigger do not exist. appId=" + appId + " and triggerId=" + triggerId
                  + " and version=" + version;
            }
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData = "version should not be empty.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "triggerId should not be empty.";
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "appId should not be empty.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on deleteTrigger. triggerMetadataCriteria=" + KiraCommonUtils
          .toString(triggerMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on deleteTrigger. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public boolean scheduleTriggerIfNeeded(TriggerIdentity triggerIdentity) throws Exception {
    boolean returnValue = false;
    try {
      String appId = triggerIdentity.getAppId();
      String triggerId = triggerIdentity.getTriggerId();
      TriggerMetadata triggerMetadata = this
          .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
      if (null != triggerMetadata) {
        boolean masterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
        if (KiraCommonUtils.isCanBeScheduled(triggerMetadata, masterZone)) {
          IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
              .getKiraTimerTriggerScheduleCenter();
          if (kiraTimerTriggerScheduleCenter.isLeaderServer(true)) {
            kiraTimerTriggerScheduleCenter
                .initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(appId, triggerId);
            returnValue = true;
          } else {
            IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
                .getClusterInternalServiceOfLeader();
            if (null != leaderServerClusterInternalService) {
              leaderServerClusterInternalService
                  .initTimerTriggerScheduleDataAndRescheduleKiraTimerTrigger(appId, triggerId);
              returnValue = true;
            } else {
              String errorMessage =
                  "Can not get the information for leader server. when scheduleTrigger for appId="
                      + appId + " triggerId=" + triggerId;
              logger.error(errorMessage);
              throw new KiraHandleException(errorMessage);
            }
          }
        } else {
          logger.info(
              "Need to unscheduleTrigger for it can not be scheduled now. appId={} and triggerId={} and masterZone={}",
              appId, triggerId, masterZone);
          this.unscheduleJobIfNeeded(appId, triggerId);
          returnValue = true;
        }
      } else {
        String errorMessage =
            "Can not getLatestTriggerMetadata for triggerIdentity=" + KiraCommonUtils
                .toString(triggerIdentity);
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } catch (KiraHandleException e) {
      throw e;
    } catch (Exception e) {
      logger
          .error("Error occurs for scheduleTriggerIfNeeded. triggerIdentity=" + triggerIdentity, e);
      throw e;
    }
    return returnValue;
  }

  @Override
  public Job createAndRunJobByTriggerMetadata(final TriggerMetadata triggerMetadata,
      final Boolean manuallyScheduled, String createdBy, Date triggerTime) {
    String appId = triggerMetadata.getAppId();
    String triggerId = triggerMetadata.getTriggerId();
    String version = triggerMetadata.getVersion();
    final Job job = jobService
        .createJob(appId, triggerId, version, manuallyScheduled, createdBy, triggerTime,
            triggerTime, true);
    if (null != job) {
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-manager-runJob-" + job.getId() + "-");
      threadFactory.setDaemon(false);
      ExecutorService executorService = null;
      try {
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        executorService.submit(new Runnable() {
          @Override
          public void run() {
            jobService.runJob(triggerMetadata, job, manuallyScheduled);
          }
        });
      } finally {
        if (null != executorService) {
          executorService.shutdown();
        }
      }
    }
    return job;
  }

  @Override
  public HandleResult manuallyRunJobByTriggerMetadata(
      TriggerMetadataCriteria triggerMetadataCriteria, String createdBy) {
    Date triggerTime = new Date();
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      String appId = triggerMetadataCriteria.getAppId();
      String triggerId = triggerMetadataCriteria.getTriggerId();
      if (StringUtils.isNotBlank(appId)) {
        if (StringUtils.isNotBlank(triggerId)) {
          TriggerMetadata triggerMetadata = this
              .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
          if (null != triggerMetadata) {
            Job job = this
                .createAndRunJobByTriggerMetadata(triggerMetadata, Boolean.TRUE, createdBy,
                    triggerTime);
            if (null != job) {
              resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
            } else {
              resultCode = KiraServerConstants.RESULT_CODE_FAILED;
              resultData = "Can not create job.";
            }
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData =
                "Can not find triggerMetadata by appId=" + appId + " and triggerId=" + triggerId;
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "triggerId should not be empty.";
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "appId should not be empty.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on manuallyRunJobByTriggerMetadata. triggerMetadataCriteria="
          + KiraCommonUtils.toString(triggerMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on manuallyRunJobByTriggerMetadata. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  private void checkArgumentsAsJsonArrayString(String argumentsAsJsonArrayString) {
    if (StringUtils.isBlank(argumentsAsJsonArrayString)) {
      throw new ValidationException(
          "The param values of the method call should not be blank. If that method has no params you need to see the value to []");
    }
  }

  private void checkDescription(String description) {
    if (StringUtils.isBlank(description)) {
      throw new ValidationException("The description should not be blank for trigger.");
    }
  }

  private void checkConcurrent(Boolean concurrent) {
    if (null == concurrent) {
      throw new ValidationException("The value of concurrent should not be null.");
    }
  }

  private void checkTriggerType(String triggerType) {
    if (StringUtils.isBlank(triggerType)) {
      throw new ValidationException("The value of triggerType should not be blank.");
    }

    TriggerTypeEnum triggerTypeEnum = TriggerTypeEnum.getTriggerTypeEnumByTriggerType(triggerType);
    if (null == triggerTypeEnum) {
      throw new ValidationException("The value of triggerType is not valid.");
    }
  }

  private void checkStartTimeAsString(String startTimeAsString) {
    if (StringUtils.isNotBlank(startTimeAsString)) {
      try {
        Date startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
      } catch (ParseException e) {
        throw new ValidationException(
            "The value of startTimeAsString is not valid. It should have the format like "
                + KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }

  private void checkEndTimeAsString(String endTimeAsString) {
    if (StringUtils.isNotBlank(endTimeAsString)) {
      try {
        Date endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
      } catch (ParseException e) {
        throw new ValidationException(
            "The value of endTimeAsString is not valid. It should have the format like "
                + KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }

  private void checkStartTimeAsStringAndEndTimeAsString(String startTimeAsString,
      String endTimeAsString) {
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      try {
        startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
      } catch (ParseException e) {
        throw new ValidationException(
            "The value of startTimeAsString is not valid. It should have the format like "
                + KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }

    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeAsString)) {
      try {
        endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
      } catch (ParseException e) {
        throw new ValidationException(
            "The value of endTimeAsString is not valid. It should have the format like "
                + KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }

    if (null != startTime && null != endTime) {
      if (startTime.after(endTime)) {
        throw new ValidationException(
            "The endTime should be after startTime. startTimeAsString=" + startTimeAsString
                + " and endTimeAsString" + endTimeAsString);
      }
    }
  }

  private void checkStartDelay(Long startDelay, String triggerType) {
    if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (null != startDelay) {
        throw new ValidationException("The value of startDelay should be null for " + triggerType);
      }
    }
  }

  private void checkRepeatCount(Integer repeatCount, String triggerType) {
    if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      if (null != repeatCount) {
        int repeatCountIntValue = repeatCount.intValue();
        if (repeatCountIntValue < 0
            && repeatCountIntValue != KiraSimpleTimerTrigger.REPEAT_INDEFINITELY) {
          throw new ValidationException(
              "Repeat count must be >= 0, use the " + KiraSimpleTimerTrigger.REPEAT_INDEFINITELY
                  + " for infinite.");
        }
      }
    } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (null != repeatCount) {
        throw new ValidationException("The value of repeatCount should be null for " + triggerType);
      }
    }
  }

  private void checkRepeatInterval(Long repeatInterval, String triggerType) {
    if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      if (null == repeatInterval) {
        throw new ValidationException(
            "The value of repeatInterval should not be null for " + triggerType);
      } else {
        long repeatIntervalLongValue = repeatInterval.longValue();
        if (repeatIntervalLongValue < 0) {
          throw new ValidationException("Repeat interval must be >= 0");
        }
      }
    } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (null != repeatInterval) {
        throw new ValidationException(
            "The value of repeatInterval should be null for " + triggerType);
      }
    }
  }

  private void checkCronExpression(String cronExpression, String triggerType) {
    if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      if (StringUtils.isNotBlank(cronExpression)) {
        throw new ValidationException(
            "The value of cronExpression should be blank for " + triggerType);
      }
    } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (StringUtils.isBlank(cronExpression)) {
        throw new ValidationException(
            "The value of cronExpression should not be blank for " + triggerType);
      } else {
        try {
          CronExpression cronExpressionObj = new CronExpression(cronExpression);
        } catch (ValidationException e) {
          throw new ValidationException(
              "The value of cronExpression is not valid : " + e.getMessage());
        }
      }
    }
  }

  private void checkMisfireInstruction(Integer misfireInstruction, String triggerType) {
    if (null == misfireInstruction) {
      throw new ValidationException("The value of misfireInstruction should not be null.");
    }

    if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
      if (!KiraManagerUtils
          .validateMisfireInstructionForSimpleTrigger(misfireInstruction.intValue())) {
        throw new ValidationException(
            "The value of misfireInstruction is not valid for " + triggerType);
      }
    } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
      if (!KiraManagerUtils
          .validateMisfireInstructionForCronTrigger(misfireInstruction.intValue())) {
        throw new ValidationException(
            "The value of misfireInstruction is not valid for " + triggerType);
      }
    }
  }

  private void checkAsynchronous(Boolean asynchronous) {
    if (null == asynchronous) {
      throw new ValidationException("The value of asynchronous should not be null.");
    }
  }

  private void checkLimitToSpecifiedLocations(Boolean limitToSpecifiedLocations) {
    if (null == limitToSpecifiedLocations) {
      throw new ValidationException("The value of limitToSpecifiedLocations should not be null.");
    }
  }

  private void checkOnlyRunOnSingleProcess(Boolean onlyRunOnSingleProcess) {
    if (null == onlyRunOnSingleProcess) {
      throw new ValidationException("The value of onlyRunOnSingleProcess should not be null.");
    }
  }

  private void checkDisabled(Boolean disabled) {
    if (null == disabled) {
      throw new ValidationException("The value of disabled should not be null.");
    }
  }

  private void checkPriority(Integer priority) {
    if (null == priority) {
      throw new ValidationException("The value of priority should not be null.");
    } else {
      if (priority.intValue() <= 0) {
        throw new ValidationException("The value of priority should be >0");
      }
    }
  }

  private void checkJobDispatchTimeout(Boolean jobDispatchTimeoutEnabled, Long jobDispatchTimeout) {
    if (null == jobDispatchTimeoutEnabled) {
      throw new ValidationException("The value of jobDispatchTimeoutEnabled should not be null.");
    }
    if (jobDispatchTimeoutEnabled) {
      if (null == jobDispatchTimeout) {
        throw new ValidationException(
            "The value of jobDispatchTimeout should not be null when jobDispatchTimeoutEnabled is set to true.");
      } else if (jobDispatchTimeout.longValue()
          < KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT) {
        throw new ValidationException("The value of jobDispatchTimeout should be >= "
            + KiraCommonConstants.JOB_DISPATCH_TIMEOUT_LOW_LIMIT);
      }
    }
  }

  private void checkRequestsRecovery(Boolean requestsRecovery) {
    if (null == requestsRecovery) {
      throw new ValidationException("The value of requestsRecovery should not be null.");
    }
  }

  private void checkCopyFromMasterToSlaveZone(Boolean copyFromMasterToSlaveZone) {
    if (null == copyFromMasterToSlaveZone) {
      throw new ValidationException("The value of copyFromMasterToSlaveZone should not be null.");
    }
  }

  private void checkOnlyScheduledInMasterZone(Boolean onlyScheduledInMasterZone) {
    if (null == onlyScheduledInMasterZone) {
      throw new ValidationException("The value of onlyScheduledInMasterZone should not be null.");
    }
  }

  private void validateTriggerMetadataUpdateContent(
      TriggerMetadataUpdateContent triggerMetadataUpdateContent) {
    if (null == triggerMetadataUpdateContent) {
      throw new ValidationException("triggerMetadataUpdateContent object should not be null.");
    }

    Long id = triggerMetadataUpdateContent.getId();
    if (null == id) {
      throw new ValidationException("The value of id should not be null.");
    }

    TriggerMetadata oldTriggerMetadata = this.select(id);
    if (null == oldTriggerMetadata) {
      throw new ValidationException("Can not find TriggerMetadata by id=" + id);
    }

    Boolean oldScheduledLocally = oldTriggerMetadata.getScheduledLocally();
    if (Boolean.TRUE.equals(oldScheduledLocally)) {
      throw new ValidationException("Can not update the trigger which is scheduled locally.");
    }

    String newTargetAppId = triggerMetadataUpdateContent.getTargetAppId();
    String newTargetTriggerId = triggerMetadataUpdateContent.getTargetTriggerId();
    boolean isInvokeTarget =
        StringUtils.isNotBlank(newTargetAppId) && StringUtils.isNotBlank(newTargetTriggerId);
    if (!isInvokeTarget) {
      if (StringUtils.isNotBlank(newTargetAppId) || StringUtils.isNotBlank(newTargetTriggerId)) {
        throw new ValidationException(
            "The targetAppId and targetTriggerId should be both blank or both not blank.");
      }
    }

    String appId = oldTriggerMetadata.getAppId();
    String triggerId = oldTriggerMetadata.getTriggerId();

    Boolean manuallyCreated = oldTriggerMetadata.getManuallyCreated();
    if (Boolean.TRUE.equals(manuallyCreated)) {
      if (!isInvokeTarget) {
        throw new ValidationException(
            "The manually created trigger must config the targetAppId and targetTriggerId.");
      }

      boolean isInvokeMyself =
          isInvokeTarget && StringUtils.equals(appId, newTargetAppId) && StringUtils
              .equals(triggerId, newTargetTriggerId);
      if (isInvokeMyself) {
        throw new ValidationException("The manually created trigger can not invoke itself.");
      }

      String targetMethod = triggerMetadataUpdateContent.getTargetMethod();
      if (StringUtils.isNotBlank(targetMethod)) {
        throw new ValidationException(
            "The value of targetMethod should be blank for manually created trigger.");
      }

      String targetMethodArgTypes = triggerMetadataUpdateContent.getTargetMethodArgTypes();
      if (StringUtils.isNotBlank(targetMethodArgTypes)) {
        throw new ValidationException(
            "The value of targetMethodArgTypes should be blank for manually created trigger.");
      }
    }

    if (!isInvokeTarget) {
      String targetMethod = triggerMetadataUpdateContent.getTargetMethod();
      if (StringUtils.isBlank(targetMethod)) {
        throw new ValidationException("The value of targetMethod should not be blank.");
      }

      String targetMethodArgTypes = triggerMetadataUpdateContent.getTargetMethodArgTypes();
      if (StringUtils.isBlank(targetMethodArgTypes)) {
        throw new ValidationException("The value of targetMethodArgTypes should not be blank.");
      }
    }

    checkTargetPoolIdAndTargetTriggerId(newTargetAppId, newTargetTriggerId);

    String argumentsAsJsonArrayString = triggerMetadataUpdateContent
        .getArgumentsAsJsonArrayString();
    checkArgumentsAsJsonArrayString(argumentsAsJsonArrayString);

    Boolean concurrent = triggerMetadataUpdateContent.getConcurrent();
    checkConcurrent(concurrent);

    String triggerType = triggerMetadataUpdateContent.getTriggerType();
    checkTriggerType(triggerType);

    String startTimeAsString = triggerMetadataUpdateContent.getStartTimeAsString();
    String endTimeAsString = triggerMetadataUpdateContent.getEndTimeAsString();
    this.checkStartTimeAsStringAndEndTimeAsString(startTimeAsString, endTimeAsString);

    Long startDelay = triggerMetadataUpdateContent.getStartDelay();
    checkStartDelay(startDelay, triggerType);

    Integer repeatCount = triggerMetadataUpdateContent.getRepeatCount();
    checkRepeatCount(repeatCount, triggerType);

    Long repeatInterval = triggerMetadataUpdateContent.getRepeatInterval();
    checkRepeatInterval(repeatInterval, triggerType);

    String cronExpression = triggerMetadataUpdateContent.getCronExpression();
    checkCronExpression(cronExpression, triggerType);

    Integer misfireInstruction = triggerMetadataUpdateContent.getMisfireInstruction();
    checkMisfireInstruction(misfireInstruction, triggerType);

    Boolean asynchronous = triggerMetadataUpdateContent.getAsynchronous();
    checkAsynchronous(asynchronous);

    Boolean onlyRunOnSingleProcess = triggerMetadataUpdateContent.getOnlyRunOnSingleProcess();
    checkOnlyRunOnSingleProcess(onlyRunOnSingleProcess);

    Boolean limitToSpecifiedLocations = triggerMetadataUpdateContent.getLimitToSpecifiedLocations();
    checkLimitToSpecifiedLocations(limitToSpecifiedLocations);

    Boolean disabled = triggerMetadataUpdateContent.getDisabled();
    checkDisabled(disabled);

    Integer priority = triggerMetadataUpdateContent.getPriority();
    checkPriority(priority);

    Boolean jobDispatchTimeoutEnabled = triggerMetadataUpdateContent.getJobDispatchTimeoutEnabled();
    Long jobDispatchTimeout = triggerMetadataUpdateContent.getJobDispatchTimeout();
    checkJobDispatchTimeout(jobDispatchTimeoutEnabled, jobDispatchTimeout);

    Boolean requestsRecovery = triggerMetadataUpdateContent.getRequestsRecovery();
    checkRequestsRecovery(requestsRecovery);

    Boolean copyFromMasterToSlaveZone = triggerMetadataUpdateContent.getCopyFromMasterToSlaveZone();
    checkCopyFromMasterToSlaveZone(copyFromMasterToSlaveZone);

    Boolean onlyScheduledInMasterZone = triggerMetadataUpdateContent.getOnlyScheduledInMasterZone();
    checkOnlyScheduledInMasterZone(onlyScheduledInMasterZone);

    String description = triggerMetadataUpdateContent.getDescription();
    checkDescription(description);
  }

  @Override
  public UpdateTriggerResult updateTrigger(
      TriggerMetadataUpdateContent triggerMetadataUpdateContent, String updatedBy) {
    UpdateTriggerResult updateTriggerResult = null;
    Long oldId = null;
    String oldVersion = null;
    String resultCode = null;
    String resultData = null;
    Long newId = null;
    String newVersion = null;
    Exception exceptionOccured = null;
    try {
      validateTriggerMetadataUpdateContent(triggerMetadataUpdateContent);
      Long id = triggerMetadataUpdateContent.getId();
      TriggerMetadata oldTriggerMetadata = this.select(id);
      if (null != oldTriggerMetadata) {
        oldId = oldTriggerMetadata.getId();
        oldVersion = oldTriggerMetadata.getVersion();
        TriggerMetadata newTriggerMetadata = doUpdateTrigger(oldTriggerMetadata,
            triggerMetadataUpdateContent, updatedBy);
        resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
        if (null != newTriggerMetadata) {
          newId = newTriggerMetadata.getId();
          newVersion = newTriggerMetadata.getVersion();
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "Can not find TriggerMetadata by id=" + id;
      }
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on updateTrigger. triggerMetadataUpdateContent=" + KiraCommonUtils
          .toString(triggerMetadataUpdateContent) + " and updatedBy=" + updatedBy, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on updateTrigger. exceptionOccured=" + exceptionOccuredDesc;
      }
      updateTriggerResult = new UpdateTriggerResult(oldId, oldVersion, resultCode, resultData,
          newId, newVersion);
    }
    return updateTriggerResult;
  }

  private TriggerMetadataZNodeData getTriggerMetadataZNodeDataForUpdate(
      TriggerMetadata oldTriggerMetadata, TriggerMetadataUpdateContent triggerMetadataUpdateContent,
      String updatedBy) throws Exception {
    TriggerMetadata newTriggerMetadata = null;
    TriggerMetadataZNodeData newTriggerMetadataZNodeData = new TriggerMetadataZNodeData();
    Date now = new Date();
    BeanUtils.copyProperties(oldTriggerMetadata, newTriggerMetadataZNodeData);
    BeanUtils.copyProperties(triggerMetadataUpdateContent, newTriggerMetadataZNodeData);

    String appId = oldTriggerMetadata.getAppId();
    String triggerId = oldTriggerMetadata.getTriggerId();

    String newTargetPoolId = triggerMetadataUpdateContent.getTargetAppId();
    String newTargetTriggerId = triggerMetadataUpdateContent.getTargetTriggerId();
    boolean isInvokeTarget =
        StringUtils.isNotBlank(newTargetPoolId) && StringUtils.isNotBlank(newTargetTriggerId);
    if (isInvokeTarget) {
      //If invoke target, then need to set back the targetMethod and targetMethodArgTypes
      String oldTargetMethod = oldTriggerMetadata.getTargetMethod();
      String oldTargetMethodArgTypes = oldTriggerMetadata.getTargetMethodArgTypes();
      newTriggerMetadataZNodeData.setTargetMethod(oldTargetMethod);
      newTriggerMetadataZNodeData.setTargetMethodArgTypes(oldTargetMethodArgTypes);
    }

    String startTimeAsString = triggerMetadataUpdateContent.getStartTimeAsString();
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }
    newTriggerMetadataZNodeData.setStartTime(startTime);

    Date endTime = null;
    String endTimeAsString = triggerMetadataUpdateContent.getEndTimeAsString();
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }
    newTriggerMetadataZNodeData.setEndTime(endTime);

    String oldVersion = oldTriggerMetadata.getVersion();
    String newVersion = KiraCommonUtils.getNewVersion(oldVersion, now);
    newTriggerMetadataZNodeData.setVersion(newVersion);
    newTriggerMetadataZNodeData.setCreateTime(now);
    String comments = KiraCommonConstants.SPECIAL_DELIMITER + "Updated by " + updatedBy;
    newTriggerMetadataZNodeData.setComments(comments);

    return newTriggerMetadataZNodeData;
  }

  @Override
  public TriggerMetadata doUpdateTrigger(TriggerMetadata oldTriggerMetadata,
      TriggerMetadataUpdateContent triggerMetadataUpdateContent, String updatedBy)
      throws Exception {
    TriggerMetadata newTriggerMetadata = updateTimerTriggerInDirectly(oldTriggerMetadata,
        triggerMetadataUpdateContent, updatedBy, true,
        KiraManagerConstants.UI_TIMEOUT_INMILLISECONDS);
    return newTriggerMetadata;
  }

  private TriggerMetadata updateTimerTriggerInDirectly(TriggerMetadata oldTriggerMetadata,
      TriggerMetadataUpdateContent triggerMetadataUpdateContent, String updatedBy,
      boolean waitForSuccess, long timeoutInMilliseconds) throws Exception {
    TriggerMetadata newTriggerMetadata = null;
    TriggerMetadataZNodeData newTriggerMetadataZNodeData = getTriggerMetadataZNodeDataForUpdate(
        oldTriggerMetadata, triggerMetadataUpdateContent, updatedBy);
    final String appId = oldTriggerMetadata.getAppId();
    final String triggerId = oldTriggerMetadata.getTriggerId();
    String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
    if (zkClient.exists(triggerZNodeZKPath)) {
      zkClient.writeData(triggerZNodeZKPath, newTriggerMetadataZNodeData);
    } else {
      logger.warn(
          "Can not updateTrigger on ZK. Because the triggerMetadata do not exist on ZK. triggerZNodeZKPath={} and oldTriggerMetadata={} and triggerMetadataUpdateContent={}",
          triggerZNodeZKPath, oldTriggerMetadata, triggerMetadataUpdateContent);
      throw new RuntimeException(
          "Can not update trigger on ZK. Because the triggerMetadata do not exist on ZK. triggerZNodeZKPath="
              + triggerZNodeZKPath);
    }

    if (waitForSuccess) {
      try {
        final String version = newTriggerMetadataZNodeData.getVersion();
        newTriggerMetadata = KiraCommonUtils
            .retryUntilNotNullOrTimeout(new Callable<TriggerMetadata>() {
              @Override
              public TriggerMetadata call() throws Exception {
                return TriggerMetadataServiceImpl.this
                    .getTriggerMetadataByPoolIdAndTriggerIdOrVersion(appId, triggerId, version);
              }
            }, timeoutInMilliseconds);
      } catch (Exception e) {
        logger.error(
            "Error occurs when retryUntilNotNullOrTimeout for updateTimerTriggerInDirectly. triggerMetadataUpdateContent="
                + KiraCommonUtils.toString(triggerMetadataUpdateContent), e);
        //do not throw out exception for the znode is already updated successfully here.
      }
    }

    return newTriggerMetadata;
  }

  @Override
  public HandleResult deleteTriggerEnvironment(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      String triggerEnvironmentZKFullPath = triggerMetadataCriteria
          .getTriggerEnvironmentZKFullPath();
      if (StringUtils.isNotBlank(triggerEnvironmentZKFullPath)) {
        if (zkClient.exists(triggerEnvironmentZKFullPath)) {
          zkClient.delete(triggerEnvironmentZKFullPath);
        } else {
          resultData = "The triggerEnvironment node do not exist, so no need to delete it.";
        }
        resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "triggerEnvironmentZKFullPath should not be empty.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error(
          "Error occurs on deleteTriggerEnvironment. triggerMetadataCriteria=" + KiraCommonUtils
              .toString(triggerMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on deleteTriggerEnvironment. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public TriggerMetadata getTriggerMetadataById(Long id) {
    TriggerMetadata returnValue = null;
    if (null != id) {
      returnValue = this.select(id);
    }
    return returnValue;
  }

  @Override
  public List<String> getTriggerTypeList() {
    List<String> returnValue = new ArrayList<String>();
    TriggerTypeEnum[] triggerTypeEnumValues = TriggerTypeEnum.values();
    if (null != triggerTypeEnumValues) {
      String name = null;
      for (TriggerTypeEnum triggerTypeEnum : triggerTypeEnumValues) {
        name = triggerTypeEnum.getName();
        returnValue.add(name);
      }
    }
    return returnValue;
  }

  @Override
  public List<Integer> getMisfireInstructionList(TriggerMetadataCriteria triggerMetadataCriteria) {
    List<Integer> returnValue = new ArrayList<Integer>();
    String triggerType = triggerMetadataCriteria.getTriggerType();
    if (StringUtils.isNotBlank(triggerType)) {
      if (TriggerTypeEnum.isSimpleTrigger(triggerType)) {
        returnValue.add(Integer.valueOf(ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW));
        returnValue.add(Integer.valueOf(KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_FIRE_NOW));
        returnValue.add(Integer.valueOf(
            KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT));
        returnValue.add(Integer.valueOf(
            KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT));
        returnValue.add(Integer.valueOf(
            KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT));
        returnValue.add(Integer.valueOf(
            KiraSimpleTimerTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT));
      } else if (TriggerTypeEnum.isCronTrigger(triggerType)) {
        returnValue.add(Integer.valueOf(ITimerTrigger.MISFIRE_INSTRUCTION_RUN_ONCE_NOW));
        returnValue.add(Integer.valueOf(KiraCronTimerTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW));
        returnValue.add(Integer.valueOf(ITimerTrigger.MISFIRE_INSTRUCTION_DO_NOTHING));
      }
    }
    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getAllRegisteredAndUndeletedTriggerIdentitysForPoolInDB(String appId)
      throws Exception {
    List<TriggerIdentity> allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB = null;
    if (StringUtils.isNotBlank(appId)) {
      //get all unregistered and undeleted TriggerIdentity by poolId in db
      TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
      triggerMetadataCriteria.setAppId(appId);
      allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB = this
          .getTriggerIdentityList(triggerMetadataCriteria);
    }
    if (null == allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB) {
      allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB = new ArrayList<TriggerIdentity>();
    }
    return allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB;
  }

  @Override
  public int unRegisterAllRegisteredTriggersOfPool(String appId) throws Exception {
    int returnValue = 0;
    if (StringUtils.isNotBlank(appId)) {
      TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
      triggerMetadataCriteria.setAppId(appId);
      triggerMetadataCriteria.setTriggerId(null);
      triggerMetadataCriteria.setUnregistered(Boolean.FALSE);
      triggerMetadataCriteria.setDeleted(null);
      returnValue = triggerMetadataDao
          .updateUnRegisteredStatus(triggerMetadataCriteria, Boolean.TRUE);
    }
    return returnValue;
  }

  @Override
  public void handlePoolDeletedOnZK(String appId) throws Exception {
    try {
      this.unRegisterAllRegisteredTriggersOfPool(appId);
      KiraManagerDataCenter.getKiraTimerTriggerMetadataManager()
          .unsubscribeDataChangesForTriggersOfPool(appId);
      KiraManagerDataCenter.getKiraTimerTriggerMetadataManager()
          .unsubscribeChildChangesForPoolByPoolId(appId);
    } finally {
      //Do not cascade pool deleted event because i do not know if this pool will cascade this to other zones.
      //KiraManagerCrossMultiZoneUtils.cascadeCrossZoneDeletePoolOfTimerTrigger(poolId);
    }
  }

  private TriggerMetadataZNodeData getTriggerMetadataZNodeDataByTriggerMetadataCreateContent(
      TriggerMetadataCreateContent triggerMetadataCreateContent, String userName) throws Exception {
    triggerMetadataCreateContent.setManuallyCreated(Boolean.TRUE);
    triggerMetadataCreateContent.setManuallyCreatedBy(userName);
    triggerMetadataCreateContent.setTargetMethod(null);
    triggerMetadataCreateContent.setTargetMethodArgTypes(null);

    Date createTime = triggerMetadataCreateContent.getCreateTime();
    if (null == createTime) {
      createTime = new Date();
      triggerMetadataCreateContent.setCreateTime(createTime);
    }

    TriggerMetadataZNodeData newTriggerMetadataZNodeData = new TriggerMetadataZNodeData();
    BeanUtils.copyProperties(triggerMetadataCreateContent, newTriggerMetadataZNodeData);

    String startTimeAsString = triggerMetadataCreateContent.getStartTimeAsString();
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }
    newTriggerMetadataZNodeData.setStartTime(startTime);

    Date endTime = null;
    String endTimeAsString = triggerMetadataCreateContent.getEndTimeAsString();
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }
    newTriggerMetadataZNodeData.setEndTime(endTime);

    return newTriggerMetadataZNodeData;
  }

  @Override
  public TriggerMetadata doCreateTrigger(TriggerMetadataCreateContent triggerMetadataCreateContent,
      String userName) throws Exception {
    TriggerMetadata returnValue = createTimerTriggerInDirectly(triggerMetadataCreateContent,
        userName, true, KiraManagerConstants.UI_TIMEOUT_INMILLISECONDS);
    return returnValue;
  }

  private TriggerMetadata createTimerTriggerInDirectly(
      final TriggerMetadataCreateContent triggerMetadataCreateContent, String userName,
      boolean waitForSuccess, long timeoutInMilliseconds) throws Exception {
    TriggerMetadata returnValue = null;
    try {
      TriggerMetadataZNodeData triggerMetadataZNodeData = getTriggerMetadataZNodeDataByTriggerMetadataCreateContent(
          triggerMetadataCreateContent, userName);
      KiraCommonUtils.createOrUpdateTriggerZNode(triggerMetadataZNodeData, false);
    } catch (Exception e) {
      logger.error("Error occurs on createTimerTriggerInDirectly. triggerMetadataCreateContent="
          + KiraCommonUtils.toString(triggerMetadataCreateContent), e);
      throw e;
    }

    if (waitForSuccess) {
      try {
        returnValue = KiraCommonUtils.retryUntilNotNullOrTimeout(new Callable<TriggerMetadata>() {
          @Override
          public TriggerMetadata call() throws Exception {
            String appId = triggerMetadataCreateContent.getAppId();
            String triggerId = triggerMetadataCreateContent.getTriggerId();
            String version = triggerMetadataCreateContent.getVersion();
            return TriggerMetadataServiceImpl.this
                .getTriggerMetadataByPoolIdAndTriggerIdOrVersion(appId, triggerId, version);
          }
        }, timeoutInMilliseconds);
      } catch (Exception e) {
        logger.error(
            "Error occurs when retryUntilNotNullOrTimeout for createTimerTriggerInDirectly. triggerMetadataCreateContent="
                + KiraCommonUtils.toString(triggerMetadataCreateContent), e);
        //do not throw out exception for the znode is already created or updated successfully here.
      }
    }

    return returnValue;
  }

  @Override
  public HandleResult createTrigger(
      TriggerMetadataCreateContent newTriggerMetadata, String userName) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      validateTriggerMetadataCreateContent(newTriggerMetadata);

      String appId = newTriggerMetadata.getAppId();
      boolean isKiraClientMetadataExistForPool = kiraClientMetadataService
          .isKiraClientMetadataExistForPool(appId);
      HandleResult handleResultOfCreateKiraClientMetadata = null;
      if (!isKiraClientMetadataExistForPool) {
        KiraClientMetadataCreateContent kiraClientMetadataCreateContent = new KiraClientMetadataCreateContent();
        kiraClientMetadataCreateContent.setAppId(appId);
        kiraClientMetadataCreateContent.setCreateTime(new Date());
        kiraClientMetadataCreateContent.setManuallyCreated(Boolean.TRUE);
        String createdBy = SystemUtil.getLocalhostIp();
        kiraClientMetadataCreateContent.setManuallyCreatedBy(userName);
        kiraClientMetadataCreateContent.setManuallyCreatedDetail(
            "Created by kira system on server " + createdBy + " when manually create trigger "
                + newTriggerMetadata.getTriggerId() + " by " + userName + " on " + KiraCommonUtils
                .getDateAsString(new Date()));

        handleResultOfCreateKiraClientMetadata = kiraClientMetadataService
            .createKiraClientMetadata(kiraClientMetadataCreateContent);
      }

      if (!isKiraClientMetadataExistForPool && (!KiraServerConstants.RESULT_CODE_SUCCESS
          .equals(handleResultOfCreateKiraClientMetadata.getResultCode()))) {
        resultCode = handleResultOfCreateKiraClientMetadata.getResultCode();
        resultData = handleResultOfCreateKiraClientMetadata.getResultData();
      } else {
        this.doCreateTrigger(newTriggerMetadata, userName);
        resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
      }
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on createTrigger. newTriggerMetadata=" + KiraCommonUtils
          .toString(newTriggerMetadata), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on createTrigger. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  private void validateTriggerMetadataCreateContent(
      TriggerMetadataCreateContent triggerMetadataCreateContent) {
    if (null == triggerMetadataCreateContent) {
      throw new ValidationException("triggerMetadataCreateContent object should not be null.");
    }

    String appId = triggerMetadataCreateContent.getAppId();
    if (StringUtils.isBlank(appId)) {
      throw new ValidationException("The value of appId should not be blank.");
    } else {
      if (appId.startsWith(" ") || appId.endsWith(" ")) {
        throw new ValidationException(
            "The value of appId should not start with blank or end with blank.");
      }
    }

    String triggerId = triggerMetadataCreateContent.getTriggerId();
    if (StringUtils.isBlank(triggerId)) {
      throw new ValidationException("The value of triggerId should not be blank.");
    } else {
      if (triggerId.startsWith(" ") || triggerId.endsWith(" ")) {
        throw new ValidationException(
            "The value of triggerId should not start with blank or end with blank.");
      }
    }

    if (isLatestTriggerExist(appId, triggerId)) {
      throw new ValidationException("There is trigger with the same appId and triggerId exist.");
    }

    String version = triggerMetadataCreateContent.getVersion();
    if (StringUtils.isBlank(version)) {
      logger.info("version is found as blank so use {} as alternate.",
          KiraCommonConstants.DEFAULT_TRIGGER_VERSION);
      version = KiraCommonConstants.DEFAULT_TRIGGER_VERSION;
      triggerMetadataCreateContent.setVersion(version);
    } else if (version.startsWith(" ") || version.endsWith(" ")) {
      throw new ValidationException(
          "The value of version should not start with blank or end with blank.");
    } else if (StringUtils.containsIgnoreCase(version, "snapshot")) {
      throw new ValidationException("The value of version should not contains \"snapshort\".");
    }

    boolean isVersionValidForKiraTimerTrigger = KiraCommonUtils
        .isVersionValidForKiraTimerTrigger(version);
    if (!isVersionValidForKiraTimerTrigger) {
      throw new ValidationException(
          "The format of version should be valid for KiraTimerTrigger. The format should be numbers delimited by \".\". For example:\"0.0.1\". But version="
              + version + " now.");
    }

    String targetAppId = triggerMetadataCreateContent.getTargetAppId();
    if (StringUtils.isBlank(targetAppId)) {
      throw new ValidationException("The value of targetAppId should not be blank.");
    }

    String targetTriggerId = triggerMetadataCreateContent.getTargetTriggerId();
    if (StringUtils.isBlank(targetTriggerId)) {
      throw new ValidationException("The value of targetTriggerId should not be blank.");
    }

    checkTargetPoolIdAndTargetTriggerId(targetAppId, targetTriggerId);

    String argumentsAsJsonArrayString = triggerMetadataCreateContent
        .getArgumentsAsJsonArrayString();
    checkArgumentsAsJsonArrayString(argumentsAsJsonArrayString);

    Boolean concurrent = triggerMetadataCreateContent.getConcurrent();
    checkConcurrent(concurrent);

    String triggerType = triggerMetadataCreateContent.getTriggerType();
    checkTriggerType(triggerType);

    String startTimeAsString = triggerMetadataCreateContent.getStartTimeAsString();
    String endTimeAsString = triggerMetadataCreateContent.getEndTimeAsString();
    this.checkStartTimeAsStringAndEndTimeAsString(startTimeAsString, endTimeAsString);

    Long startDelay = triggerMetadataCreateContent.getStartDelay();
    checkStartDelay(startDelay, triggerType);

    Integer repeatCount = triggerMetadataCreateContent.getRepeatCount();
    checkRepeatCount(repeatCount, triggerType);

    Long repeatInterval = triggerMetadataCreateContent.getRepeatInterval();
    checkRepeatInterval(repeatInterval, triggerType);

    String cronExpression = triggerMetadataCreateContent.getCronExpression();
    checkCronExpression(cronExpression, triggerType);

    Integer misfireInstruction = triggerMetadataCreateContent.getMisfireInstruction();
    checkMisfireInstruction(misfireInstruction, triggerType);

    Boolean asynchronous = triggerMetadataCreateContent.getAsynchronous();
    checkAsynchronous(asynchronous);

    Boolean onlyRunOnSingleProcess = triggerMetadataCreateContent.getOnlyRunOnSingleProcess();
    checkOnlyRunOnSingleProcess(onlyRunOnSingleProcess);

    Boolean limitToSpecifiedLocations = triggerMetadataCreateContent.getLimitToSpecifiedLocations();
    checkLimitToSpecifiedLocations(limitToSpecifiedLocations);

    Boolean disabled = triggerMetadataCreateContent.getDisabled();
    checkDisabled(disabled);

    Boolean jobDispatchTimeoutEnabled = triggerMetadataCreateContent.getJobDispatchTimeoutEnabled();
    Long jobDispatchTimeout = triggerMetadataCreateContent.getJobDispatchTimeout();
    checkJobDispatchTimeout(jobDispatchTimeoutEnabled, jobDispatchTimeout);

    Integer priority = triggerMetadataCreateContent.getPriority();
    checkPriority(priority);

    String description = triggerMetadataCreateContent.getDescription();
    checkDescription(description);
  }

  private void checkTargetPoolIdAndTargetTriggerId(String targetAppId, String targetTriggerId) {
    if (StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId)) {
      TriggerMetadata targetTriggerMetadata = this
          .getTriggerMetadataByPoolIdAndTriggerIdOrVersion(targetAppId, targetTriggerId, null);
      if (null == targetTriggerMetadata) {
        throw new ValidationException(
            "The trigger with the value of targetAppId and targetTriggerId do not exist.");
      } else {
        Boolean isTargetManuallyCreated = targetTriggerMetadata.getManuallyCreated();
        if (null != isTargetManuallyCreated && isTargetManuallyCreated.booleanValue()) {
          throw new ValidationException(
              "The manually created trigger can not be set as targetTriggerId.");
        }
      }
    }
  }

  private boolean isLatestTriggerExist(String appId, String triggerId) {
    boolean returnValue = false;

    TriggerMetadata triggerMetadata = getTriggerMetadataByPoolIdAndTriggerIdOrVersion(appId,
        triggerId, null);
    if (null != triggerMetadata) {
      returnValue = true;
    }
    return returnValue;
  }

  private TriggerMetadata getTriggerMetadataByPoolIdAndTriggerIdOrVersion(String appId,
      String triggerId, String version) {
    TriggerMetadata returnValue = null;
    if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId)) {
      TriggerMetadata triggerMetadata = this
          .getLatestAndAvailableTriggerMetadata(appId, triggerId, version);
      if (null != triggerMetadata) {
        returnValue = triggerMetadata;
      }
    }

    return returnValue;
  }

  @Override
  public void checkAndAddDefaultKiraClientMetadataForTriggers() throws Exception {
    List<String> poolIdList = triggerMetadataDao
        .getPoolIdListOfTriggersWhichHasNoKiraClientMetadata();
    if (null != poolIdList) {
      for (String poolId : poolIdList) {
        boolean isKiraClientMetadataExistForPool = kiraClientMetadataService
            .isKiraClientMetadataExistForPool(poolId);
        if (!isKiraClientMetadataExistForPool) {
          addDefaultKiraClientMetadata(poolId);
        }
      }
    }
  }

  private void addDefaultKiraClientMetadata(String appId) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start addDefaultKiraClientMetadata...");
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent = new KiraClientMetadataCreateContent();
      kiraClientMetadataCreateContent.setAppId(appId);
      kiraClientMetadataCreateContent.setCreateTime(new Date());
      kiraClientMetadataCreateContent.setManuallyCreated(Boolean.TRUE);
      String createdBy = SystemUtil.getLocalhostIp();
      kiraClientMetadataCreateContent.setManuallyCreatedBy(createdBy);
      kiraClientMetadataCreateContent.setManuallyCreatedDetail(
          "Created by kira system on server " + createdBy
              + " when check triggers which has no related kiraClientMetada found on "
              + KiraCommonUtils.getDateAsString(new Date()));
      HandleResult handleResultOfCreateKiraClientMetadata = kiraClientMetadataService
          .createKiraClientMetadata(kiraClientMetadataCreateContent);
      if (!KiraServerConstants.RESULT_CODE_SUCCESS
          .equals(handleResultOfCreateKiraClientMetadata.getResultCode())) {
        logger.error(
            "Error occurs when addDefaultKiraClientMetadata for appId={}. handleResultOfCreateKiraClientMetadata={}",
            appId, handleResultOfCreateKiraClientMetadata);
      }
    } catch (Exception e) {
      logger.error("Error occurs when addDefaultKiraClientMetadata.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("It takes " + costTime + " milliseconds to addDefaultKiraClientMetadata.");
    }


  }

  @Override
  public List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(
      TriggerMetadataCriteria criteria) {
    List<TriggerMetadata> latestTriggerMetadatasWhichSetTriggerAsTargetOnPage = triggerMetadataDao
        .getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(criteria);

    return latestTriggerMetadatasWhichSetTriggerAsTargetOnPage;
  }

  @Override
  public List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(
      TriggerMetadataCriteria criteria) {
    List<TriggerMetadata> latestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage = triggerMetadataDao
        .getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(criteria);

    return latestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage;
  }

  @Override
  public void doDeleteTriggersOfPool(String appId, String deletedBy) throws Exception {
    TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
    triggerMetadataCriteria.setAppId(appId);
    List<TriggerIdentity> allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB = triggerMetadataDao
        .getTriggerIdentityList(triggerMetadataCriteria);
    if (!CollectionUtils.isEmpty(allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB)) {
      for (TriggerIdentity oneTriggerIdentity : allUnregisteredAndUndeletedTriggerIdentitysForPoolInDB) {
        String oneAppId = oneTriggerIdentity.getAppId();
        String triggerId = oneTriggerIdentity.getTriggerId();
        String version = oneTriggerIdentity.getVersion();
        this.doDeleteTrigger(oneAppId, triggerId, version, deletedBy);
      }
    }
  }

  @Override
  public void cascadeCrossZoneDeleteTimerTrigger(TriggerIdentity triggerIdentity) {
    try {
      String appId = triggerIdentity.getAppId();
      String triggerId = triggerIdentity.getTriggerId();
      TriggerMetadata triggerMetadata = this.triggerMetadataService
          .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
      if (null != triggerMetadata) {
        Boolean copyFromMasterToSlaveZone = triggerMetadata.getCopyFromMasterToSlaveZone();
        KiraManagerCrossMultiZoneUtils.cascadeCrossZoneDeleteTimerTriggerIfNeeded(appId, triggerId,
            copyFromMasterToSlaveZone);
      } else {
        logger.warn(
            "triggerMetadata is null for cascadeCrossZoneDeleteTimerTrigger. triggerIdentity={}",
            KiraCommonUtils.toString(triggerIdentity));
      }
    } catch (Throwable t) {
      logger.error("Error occurs when cascadeCrossZoneDeleteTimerTrigger. triggerIdentity="
          + triggerIdentity, t);
    }
  }

  @Override
  public int updateCrossMultiZoneData(String appId, String triggerId,
      String version, Boolean copyFromMasterToSlaveZone,
      Boolean onlyScheduledInMasterZone, String comments) {
    return triggerMetadataDao
        .updateCrossMultiZoneData(appId, triggerId, version, copyFromMasterToSlaveZone,
            onlyScheduledInMasterZone, comments);
  }

  @Override
  public List<KiraTimerTriggerBusinessRunningInstance> getKiraTimerTriggerBusinessRunningInstanceList(
      TriggerMetadataCriteria triggerMetadataCriteria) {
    List<KiraTimerTriggerBusinessRunningInstance> returnValue = new ArrayList<KiraTimerTriggerBusinessRunningInstance>();

    String appId = triggerMetadataCriteria.getAppId();
    String triggerId = triggerMetadataCriteria.getTriggerId();
    try {
      returnValue = KiraCommonUtils
          .getKiraTimerTriggerBusinessRunningInstanceList(appId, triggerId);
    } catch (Throwable t) {
      logger.error(
          "Error occurs when getKiraTimerTriggerBusinessRunningInstanceList. appId=" + appId
              + " and triggerId=" + triggerId, t);
    }
    if (null != returnValue) {
      triggerMetadataCriteria.getPaging().setTotalResults(returnValue.size());
    }
    return returnValue;
  }

  @Override
  public List<TriggerPredictReportLineData> getTriggerPredictReportLineDataList(Date startTime,
      Date endTime, List<String> poolIdList, List<String> triggerIdList) throws Exception {
    List<TriggerPredictReportLineData> returnValue = new ArrayList<TriggerPredictReportLineData>();
    if (null != startTime && null != endTime) {
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
      timerTriggerScheduleCriteria.getPaging().setMaxResults(Integer.MAX_VALUE);
      timerTriggerScheduleCriteria.setOrderByClause("app_id,trigger_id");
      timerTriggerScheduleCriteria.setPoolIdList(poolIdList);
      timerTriggerScheduleCriteria.setTriggerIdList(triggerIdList);
      List<TimerTriggerSchedule> timerTriggerScheduleList = this.timerTriggerScheduleService
          .list(timerTriggerScheduleCriteria);
      if (!CollectionUtils.isEmpty(timerTriggerScheduleList)) {
        ITimeScheduleCallback timeScheduleCallback = new KiraTimeScheduleCallbackAdaptor();
        for (TimerTriggerSchedule oneTimerTriggerSchedule : timerTriggerScheduleList) {
          String appId = oneTimerTriggerSchedule.getAppId();
          String triggerId = oneTimerTriggerSchedule.getTriggerId();

          TriggerMetadata triggerMetadata = this.triggerMetadataService
              .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
          if (null != triggerMetadata) {
            ITimerTrigger timerTrigger = KiraManagerUtils
                .getTimerTrigger(triggerMetadata, oneTimerTriggerSchedule, timeScheduleCallback);
            if (null != timerTrigger) {
              Date nextFireTimeAfter = timerTrigger.calculateAndGetNextFireTimeAfter(startTime);
              if (null != nextFireTimeAfter && (nextFireTimeAfter.before(endTime)
                  || nextFireTimeAfter.equals(endTime))) {
                //Range: [startTime, endTime]
                String description = triggerMetadata.getDescription();
                Date fromFutureTime = startTime;
                Date toFutureTime = endTime;
                Date firstTriggeredTimeInTheFuture = nextFireTimeAfter;
                Integer maxHistoryRuntimeInSeconds = null;
                Integer minHistoryRuntimeInSeconds = null;
                Integer avgHistoryRuntimeInSeconds = null;
                JobRunStatisticsCriteria jobRunStatisticsCriteria = new JobRunStatisticsCriteria();
                jobRunStatisticsCriteria.setAppId(appId);
                jobRunStatisticsCriteria.setTriggerId(triggerId);
                JobRunStatistics jobRunStatistics = this.jobRunStatisticsService
                    .getJobRunStatistics(appId, triggerId);
                if (null != jobRunStatistics) {
                  maxHistoryRuntimeInSeconds = jobRunStatistics.getMaxInSeconds();
                  minHistoryRuntimeInSeconds = jobRunStatistics.getMinInSeconds();
                  avgHistoryRuntimeInSeconds = jobRunStatistics.getAvgInSeconds();
                }

                TriggerPredictReportLineData triggerPredictReportLineData = new TriggerPredictReportLineData(
                    appId, triggerId, description, fromFutureTime, toFutureTime,
                    firstTriggeredTimeInTheFuture, maxHistoryRuntimeInSeconds,
                    minHistoryRuntimeInSeconds, avgHistoryRuntimeInSeconds);
                returnValue.add(triggerPredictReportLineData);
              }
            }
          }
        }
      }
    } else {
      throw new KiraHandleException(
          "Both startTime and endTime should not be null for getTriggerPredictReportLineDataList.");
    }

    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getTriggerIdentityListWhichWillBeTriggeredInScope(Date startTime,
      Date endTime, List<String> poolIdList, List<String> triggerIdList) throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    if (null != startTime && null != endTime) {
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
      timerTriggerScheduleCriteria.getPaging().setMaxResults(Integer.MAX_VALUE);
      timerTriggerScheduleCriteria.setOrderByClause("app_id,trigger_id");
      timerTriggerScheduleCriteria.setPoolIdList(poolIdList);
      timerTriggerScheduleCriteria.setTriggerIdList(triggerIdList);
      List<TimerTriggerSchedule> timerTriggerScheduleList = this.timerTriggerScheduleService
          .list(timerTriggerScheduleCriteria);
      if (!CollectionUtils.isEmpty(timerTriggerScheduleList)) {
        ITimeScheduleCallback timeScheduleCallback = new KiraTimeScheduleCallbackAdaptor();
        for (TimerTriggerSchedule oneTimerTriggerSchedule : timerTriggerScheduleList) {
          String appId = oneTimerTriggerSchedule.getAppId();
          String triggerId = oneTimerTriggerSchedule.getTriggerId();
          TriggerMetadata triggerMetadata = this.triggerMetadataService
              .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
          ITimerTrigger timerTrigger = KiraManagerUtils
              .getTimerTrigger(triggerMetadata, oneTimerTriggerSchedule, timeScheduleCallback);
          if (null != timerTrigger) {
            Date nextFireTimeAfter = timerTrigger.calculateAndGetNextFireTimeAfter(startTime);
            if (null != nextFireTimeAfter && (nextFireTimeAfter.before(endTime) || nextFireTimeAfter
                .equals(endTime))) {
              TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
              returnValue.add(triggerIdentity);
            }
          }
        }
      }
    } else {
      throw new KiraHandleException(
          "Both startTime and endTime should not be null for getTriggerIdentityListWhichWillBeTriggeredInScope.");
    }

    return returnValue;
  }

  @Override
  public Map<TriggerIdentity, List<Date>> getTriggerIdentityTriggeredTimeListMapInScope(
      Date startTime, Date endTime, List<String> poolIdList, List<String> triggerIdList,
      Integer maxCountPerTrigger) throws Exception {
    Map<TriggerIdentity, List<Date>> returnValue = new LinkedHashMap<TriggerIdentity, List<Date>>();
    if (null != startTime && null != endTime) {
      if (null == maxCountPerTrigger || maxCountPerTrigger.intValue() <= 0) {
        maxCountPerTrigger = Integer.valueOf(10);
      }
      TimerTriggerScheduleCriteria timerTriggerScheduleCriteria = new TimerTriggerScheduleCriteria();
      timerTriggerScheduleCriteria.getPaging().setMaxResults(Integer.MAX_VALUE);
      timerTriggerScheduleCriteria.setOrderByClause("app_id,trigger_id");
      timerTriggerScheduleCriteria.setPoolIdList(poolIdList);
      timerTriggerScheduleCriteria.setTriggerIdList(triggerIdList);
      List<TimerTriggerSchedule> timerTriggerScheduleList = this.timerTriggerScheduleService
          .list(timerTriggerScheduleCriteria);
      if (!CollectionUtils.isEmpty(timerTriggerScheduleList)) {
        ITimeScheduleCallback timeScheduleCallback = new KiraTimeScheduleCallbackAdaptor();
        for (TimerTriggerSchedule oneTimerTriggerSchedule : timerTriggerScheduleList) {
          String appId = oneTimerTriggerSchedule.getAppId();
          String triggerId = oneTimerTriggerSchedule.getTriggerId();
          TriggerMetadata triggerMetadata = this.triggerMetadataService
              .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
          ITimerTrigger timerTrigger = KiraManagerUtils
              .getTimerTrigger(triggerMetadata, oneTimerTriggerSchedule, timeScheduleCallback);
          if (null != timerTrigger) {
            Date nextFireTimeAfter = timerTrigger.calculateAndGetNextFireTimeAfter(startTime);
            int count = 0;
            while (null != nextFireTimeAfter && (nextFireTimeAfter.before(endTime)
                || nextFireTimeAfter.equals(endTime))) {
              TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
              List<Date> nextFireTimeAfterList = returnValue.get(triggerIdentity);
              if (null == nextFireTimeAfterList) {
                nextFireTimeAfterList = new ArrayList<Date>();
              }
              nextFireTimeAfterList.add(nextFireTimeAfter);
              returnValue.put(triggerIdentity, nextFireTimeAfterList);
              count++;
              if (count >= maxCountPerTrigger.intValue()) {
                //Do not store too many values.
                break;
              }
              nextFireTimeAfter = timerTrigger.calculateAndGetNextFireTimeAfter(nextFireTimeAfter);
            }
          }
        }
      }
    } else {
      throw new KiraHandleException(
          "Both startTime and endTime should not be null for getTriggerIdentityTriggeredTimeListMapInScope.");
    }
    return returnValue;
  }

}