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

import com.yihaodian.architecture.kira.client.api.KiraClientAPI;
import com.yihaodian.architecture.kira.client.iface.IJobCancelable;
import com.yihaodian.architecture.kira.client.util.JobCancelResult;
import com.yihaodian.architecture.kira.client.util.TriggerMetadataClientSideView;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneConstants;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.common.event.EventDispatcher;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.criteria.JobTimeoutTrackerCriteria;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraZoneContextData;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.RunningEnvironmentForTimerTrigger;
import com.yihaodian.architecture.kira.manager.dto.TriggerPredictReportLineData;
import com.yihaodian.architecture.kira.manager.event.jobtimeouttracker.JobTimeoutEvent;
import com.yihaodian.architecture.kira.manager.event.jobtimeouttracker.JobTimeoutTrackerEventType;
import com.yihaodian.architecture.kira.manager.health.monitor.task.ExternalOverallMonitorForTimerTriggerTask;
import com.yihaodian.architecture.kira.manager.service.JobHistoryService;
import com.yihaodian.architecture.kira.manager.service.JobItemHistoryService;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.JobRunStatisticsService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.KiraServerService;
import com.yihaodian.architecture.kira.manager.service.OtherService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.EmailUtils;
import com.yihaodian.architecture.kira.manager.util.JobTimeoutTrackerStateEnum;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.SMSUtils;
import com.yihaodian.architecture.kira.manager.util.excel.ExcelUtil;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelCellDescriptor;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelExportDataHolder;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelSheetData;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class OtherServiceImpl extends Service implements OtherService, ILifecycle, IJobCancelable {

  private static final String INCONSISTENTPOOLID = "InConsistentPoolId";
  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private JobService jobService;
  private JobHistoryService jobHistoryService;
  private JobItemService jobItemService;
  private JobItemHistoryService jobItemHistoryService;
  private JobTimeoutTrackerService jobTimeoutTrackerService;
  private KiraServerService kiraServerService;
  private TriggerMetadataService triggerMetadataService;
  private KiraClientMetadataService kiraClientMetadataService;
  private JobRunStatisticsService jobRunStatisticsService;
  private EventDispatcher eventDispatcher;
  private ExecutorService executorServiceForHandleJobRuntimeData;

  public OtherServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public JobService getJobService() {
    return jobService;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public JobHistoryService getJobHistoryService() {
    return jobHistoryService;
  }

  public void setJobHistoryService(JobHistoryService jobHistoryService) {
    this.jobHistoryService = jobHistoryService;
  }

  public JobItemService getJobItemService() {
    return jobItemService;
  }

  public void setJobItemService(JobItemService jobItemService) {
    this.jobItemService = jobItemService;
  }

  public JobItemHistoryService getJobItemHistoryService() {
    return jobItemHistoryService;
  }

  public void setJobItemHistoryService(
      JobItemHistoryService jobItemHistoryService) {
    this.jobItemHistoryService = jobItemHistoryService;
  }

  public JobTimeoutTrackerService getJobTimeoutTrackerService() {
    return jobTimeoutTrackerService;
  }

  public void setJobTimeoutTrackerService(
      JobTimeoutTrackerService jobTimeoutTrackerService) {
    this.jobTimeoutTrackerService = jobTimeoutTrackerService;
  }

  public KiraServerService getKiraServerService() {
    return kiraServerService;
  }

  public void setKiraServerService(KiraServerService kiraServerService) {
    this.kiraServerService = kiraServerService;
  }

  public KiraClientMetadataService getKiraClientMetadataService() {
    return kiraClientMetadataService;
  }

  public void setKiraClientMetadataService(KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public TriggerMetadataService getTriggerMetadataService() {
    return triggerMetadataService;
  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public JobRunStatisticsService getJobRunStatisticsService() {
    return jobRunStatisticsService;
  }

  public void setJobRunStatisticsService(JobRunStatisticsService jobRunStatisticsService) {
    this.jobRunStatisticsService = jobRunStatisticsService;
  }

  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
  }

  @Override
  public void init() {
  }

  @Override
  public void destroy() {
    if (logger.isDebugEnabled()) {
      logger.debug("destroy OtherServiceImpl...");
    }
    if (null != executorServiceForHandleJobRuntimeData) {
      executorServiceForHandleJobRuntimeData.shutdownNow();
    }
    if (logger.isDebugEnabled()) {
      logger.debug("destroy OtherServiceImpl finished.");
    }
  }

  @Override
  public void testSucess() {
    Date now = new Date();
    if (logger.isDebugEnabled()) {
      logger.debug("testSucess called now=" + KiraCommonUtils.getDateAsString(now));
    }
  }

  @Override
  public void testFailed() {
    Date now = new Date();
    if (logger.isDebugEnabled()) {
      logger.debug("testFailed called now=" + KiraCommonUtils.getDateAsString(now));
    }
    throw new RuntimeException("testFailed throw exception intentionally");
  }

  @Override
  public List<String> getPoolValueNeedRectifiedPoolsUnderTriggers() throws Exception {
    List<String> returnValue = new ArrayList<String>();
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolShortPathList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolShortPathList)) {
        String onePoolPath = null;
        for (String onePoolShortPath : poolShortPathList) {
          onePoolPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolPath, true);
          if (poolZNodeData instanceof String) {
            //it is of type String. regard it as poolZNode
            String poolIdValueOfPoolZNode = (String) poolZNodeData;
            String exactPoolIdOfAllTriggersUnderPoolZNode = getExactPoolIdOfAllTriggersUnderPoolZNode(
                onePoolPath);
            if (null != exactPoolIdOfAllTriggersUnderPoolZNode && !INCONSISTENTPOOLID
                .equals(exactPoolIdOfAllTriggersUnderPoolZNode)) {
              if (!StringUtils
                  .equals(poolIdValueOfPoolZNode, exactPoolIdOfAllTriggersUnderPoolZNode)) {
                logger.warn(
                    "NeedRectifiedPool found. poolIdValueOfPoolZNode={} and exactPoolIdOfAllTriggersUnderPoolZNode={}",
                    poolIdValueOfPoolZNode, exactPoolIdOfAllTriggersUnderPoolZNode);
                returnValue.add(exactPoolIdOfAllTriggersUnderPoolZNode);
              }
            }
          }
        }
      }
    }
    return returnValue;
  }

  private String getExactPoolIdOfAllTriggersUnderPoolZNode(String onePoolPath) throws Exception {
    String returnValue = null;
    if (zkClient.exists(onePoolPath)) {
      List<String> triggerList = zkClient.getChildren(onePoolPath);
      if (!CollectionUtils.isEmpty(triggerList)) {
        String oneTriggerPath = null;
        TriggerMetadataZNodeData triggerMetadataZNodeData = null;
        for (String oneTriggerShortPath : triggerList) {
          oneTriggerPath = KiraUtil.getChildFullPath(onePoolPath, oneTriggerShortPath);
          triggerMetadataZNodeData = zkClient.readData(oneTriggerPath, true);
          if (null != triggerMetadataZNodeData) {
            String poolId = triggerMetadataZNodeData.getAppId();
            if (null == returnValue) {
              returnValue = poolId;
            } else {
              if (!StringUtils.equals(returnValue, poolId)) {
                logger.warn("InConsistent poolId found under {} poolId1={} and poolId2={}",
                    onePoolPath, returnValue, poolId);
                returnValue = INCONSISTENTPOOLID;
                break;
              }
            }
          }
        }
      }
    }
    return returnValue;
  }

  @Override
  public HandleResult rectifyValueOfPoolNodesUnderTriggers() {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      List<String> poolIds = getPoolValueNeedRectifiedPoolsUnderTriggers();
      if (!CollectionUtils.isEmpty(poolIds)) {
        for (String poolId : poolIds) {
          if (StringUtils.isNotBlank(poolId)) {
            String onePoolShortPath = KiraUtil.filterString(poolId);
            String onePoolPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
            if (zkClient.exists(onePoolPath)) {
              zkClient.writeData(onePoolPath, poolId);
              resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
            }
          }
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "getPoolValueNeedRectifiedPoolsUnderTriggers returns empty.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on rectifyValueOfPoolNodesUnderTriggers.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on rectifyValueOfPoolNodesUnderTriggers. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public void doLeaderRoutineWork() {
    HandleResult handleResult = kiraServerService.doLeaderRoutineWork();
    if (KiraServerConstants.RESULT_CODE_SUCCESS.equals(handleResult.getResultCode())) {
      if (logger.isDebugEnabled()) {
        logger.debug("doLeaderRoutineWork is called successfully.");
      }
    } else {
      String hintMessage =
          "doLeaderRoutineWork is called without success. handleResult=" + KiraCommonUtils
              .toString(handleResult);
      logger.warn(hintMessage);
      throw new RuntimeException(hintMessage);
    }
  }

  @Override
  public List<String> getTriggersWhichWillBeTriggeredInScope(
      LinkedHashMap<String, String> paramsAsMap) throws Exception {
    List<String> returnValue = new ArrayList<String>();
    String startTimeAsString = paramsAsMap.get("startTime");
    String endTimeAsString = paramsAsMap.get("endTime");
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }

    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("startTime={} and endTime={} for getTriggersWhichWillBeTriggeredInScope",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }

    String poolIdListAsString = paramsAsMap.get("poolIdList");
    List<String> poolIdList = KiraCommonUtils
        .getStringListByDelmiter(poolIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    String triggerIdListAsString = paramsAsMap.get("triggerIdList");
    List<String> triggerIdList = KiraCommonUtils
        .getStringListByDelmiter(triggerIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    List<TriggerIdentity> triggerIdentityListWhichWillBeTriggeredInScope = this.triggerMetadataService
        .getTriggerIdentityListWhichWillBeTriggeredInScope(startTime, endTime, poolIdList,
            triggerIdList);
    if (!CollectionUtils.isEmpty(triggerIdentityListWhichWillBeTriggeredInScope)) {
      for (TriggerIdentity oneTriggerIdentity : triggerIdentityListWhichWillBeTriggeredInScope) {
        String appId = oneTriggerIdentity.getAppId();
        String triggerId = oneTriggerIdentity.getTriggerId();
        String triggerAsString = appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId;
        returnValue.add(triggerAsString);
      }
    }
    return returnValue;
  }

  @Override
  public Map<String, List<String>> getTriggerTriggeredTimeListMapInScope(
      LinkedHashMap<String, String> paramsAsMap) throws Exception {
    Map<String, List<String>> returnValue = new LinkedHashMap<String, List<String>>();
    String startTimeAsString = paramsAsMap.get("startTime");
    String endTimeAsString = paramsAsMap.get("endTime");
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }

    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("startTime={} and endTime={} for getTriggerTriggeredTimeListMapInScope",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }

    String poolIdListAsString = paramsAsMap.get("poolIdList");
    List<String> poolIdList = KiraCommonUtils
        .getStringListByDelmiter(poolIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    String triggerIdListAsString = paramsAsMap.get("triggerIdList");
    List<String> triggerIdList = KiraCommonUtils
        .getStringListByDelmiter(triggerIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    Integer maxCountPerTrigger = null;
    String maxCountPerTriggerAsString = paramsAsMap.get("maxCountPerTrigger");
    if (StringUtils.isNotBlank(maxCountPerTriggerAsString)) {
      maxCountPerTrigger = Integer.valueOf(maxCountPerTriggerAsString);
    }

    Map<TriggerIdentity, List<Date>> triggerIdentityTriggeredTimeListMapInScope = this.triggerMetadataService
        .getTriggerIdentityTriggeredTimeListMapInScope(startTime, endTime, poolIdList,
            triggerIdList, maxCountPerTrigger);
    if (MapUtils.isNotEmpty(triggerIdentityTriggeredTimeListMapInScope)) {
      for (Map.Entry<TriggerIdentity, List<Date>> entry : triggerIdentityTriggeredTimeListMapInScope
          .entrySet()) {
        TriggerIdentity triggerIdentity = entry.getKey();
        List<Date> dateList = entry.getValue();
        if (!CollectionUtils.isEmpty(dateList)) {
          String appId = triggerIdentity.getAppId();
          String triggerId = triggerIdentity.getTriggerId();
          String triggerAsString = appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId;
          List<String> dateStringList = new ArrayList<String>();
          for (Date oneDate : dateList) {
            String oneDateAsString = KiraCommonUtils.getDateAsString(oneDate);
            dateStringList.add(oneDateAsString);
          }
          returnValue.put(triggerAsString, dateStringList);
        }
      }
    }
    return returnValue;
  }

  @Override
  public void handleJobRuntimeData(LinkedHashMap<String, String> paramsAsMap) throws Throwable {
    String startTimeAsString = paramsAsMap.get("startTime");
    String endTimeAsString = paramsAsMap.get("endTime");
    if (logger.isDebugEnabled()) {
      logger
          .debug("startTimeAsString={} and endTimeAsString={}", startTimeAsString, endTimeAsString);
    }
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }

    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }

    if (null != startTime && null != endTime) {
      if (startTime.after(endTime)) {
        StringBuilder sb = new StringBuilder();
        sb.append("The startTime should not be later than endTime.");
        sb.append("startTime=" + KiraCommonUtils.getDateAsString(startTime));
        sb.append("endTime=" + KiraCommonUtils.getDateAsString(endTime));
        String message = sb.toString();
        logger.warn(message);
        throw new ValidationException(message);
      }
    }

    if (null == startTime) {
      startTime = getDefaultStartTimeForHandleJobRuntimeData();
    }
    Date defaultEndTimeForHandleJobRuntimeData = getDefaultEndTimeForHandleJobRuntimeData();
    if (null == endTime) {
      endTime = defaultEndTimeForHandleJobRuntimeData;
    } else {
      if (endTime.after(defaultEndTimeForHandleJobRuntimeData)) {
        StringBuilder sb = new StringBuilder();
        sb.append("EndTime=");
        sb.append(KiraCommonUtils.getDateAsString(endTime));
        sb.append(". It should not be later than ");
        sb.append(KiraCommonUtils.getDateAsString(defaultEndTimeForHandleJobRuntimeData));
        String message = sb.toString();
        logger.warn(message);
        throw new ValidationException(message);
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("startTime={} and endTime={}", KiraCommonUtils.getDateAsString(startTime),
          KiraCommonUtils.getDateAsString(endTime));
    }

    Throwable throwableCaught = null;
    long handleStartTime = System.currentTimeMillis();
    if (null == executorServiceForHandleJobRuntimeData) {
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-executorServiceForHandleJobRuntimeData-");
      threadFactory.setDaemon(true);
      executorServiceForHandleJobRuntimeData = Executors.newSingleThreadExecutor(threadFactory);
    }
    try {
      jobService.handleJobRuntimeData(executorServiceForHandleJobRuntimeData, startTime, endTime);
      jobHistoryService
          .handleJobHistoryRuntimeData(executorServiceForHandleJobRuntimeData, startTime, endTime);
      jobItemService
          .handleJobItemRuntimeData(executorServiceForHandleJobRuntimeData, startTime, endTime);
      jobItemHistoryService
          .handleJobItemHistoryRuntimeData(executorServiceForHandleJobRuntimeData, startTime,
              endTime);
      jobTimeoutTrackerService
          .handleJobTimeoutTrackerRuntimeData(executorServiceForHandleJobRuntimeData, startTime,
              endTime);
    } catch (Throwable t) {
      throwableCaught = t;
      logger.error("Error occurs for handleJobRuntimeData. startTime=" + KiraCommonUtils
              .getDateAsString(startTime) + " and endTime=" + KiraCommonUtils.getDateAsString(endTime),
          t);
      throw t;
    } finally {
      if (logger.isDebugEnabled()) {
        long costTime = System.currentTimeMillis() - handleStartTime;
        StringBuilder sb = new StringBuilder();
        sb.append("handleJobRuntimeData finished");
        if (null != throwableCaught) {
          sb.append(" with error.");
        } else {
          sb.append(" successfully.");
        }
        sb.append(" It takes {} milliseconds. startTime={} and endTime={}");
        logger.debug(sb.toString(), costTime, KiraCommonUtils.getDateAsString(startTime),
            KiraCommonUtils.getDateAsString(endTime));
      }
      if (null != executorServiceForHandleJobRuntimeData) {
        executorServiceForHandleJobRuntimeData.shutdownNow();
        executorServiceForHandleJobRuntimeData = null;
      }
    }
  }

  private Date getDefaultStartTimeForHandleJobRuntimeData() {
    Date returnValue = null;
    return returnValue;
  }

  private Date getDefaultEndTimeForHandleJobRuntimeData() {
    Date returnValue = null;
    int minutesToKeepJobRuntimeData = KiraManagerUtils.getMinutesToKeepJobRuntimeData();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, -minutesToKeepJobRuntimeData);
    returnValue = new Date(cal.getTimeInMillis());
    return returnValue;
  }

  @Override
  public JobCancelResult cancelJob(Map<Serializable, Serializable> dataMap) {
    if (null != executorServiceForHandleJobRuntimeData) {
      executorServiceForHandleJobRuntimeData.shutdownNow();
    }
    JobCancelResult jobCancelResult = new JobCancelResult(JobCancelResult.RESULT_CODE_SUCCESS,
        null);
    return jobCancelResult;
  }

  @Override
  public void scanJobTimeoutTrackerData(LinkedHashMap<String, String> paramsAsMap)
      throws Exception {
    int jobTimeoutTrackerHandleMaxCountPerTime = 100;
    String jobTimeoutTrackerHandleMaxCountPerTimeAsString = paramsAsMap
        .get("jobTimeoutTrackerHandleMaxCountPerTime");
    if (StringUtils.isNotBlank(jobTimeoutTrackerHandleMaxCountPerTimeAsString)) {
      jobTimeoutTrackerHandleMaxCountPerTime = Integer
          .valueOf(jobTimeoutTrackerHandleMaxCountPerTimeAsString).intValue();
    }

    List<JobTimeoutTracker> jobTimeoutTrackerList = this
        .getNextJobTimeoutTrackerList(jobTimeoutTrackerHandleMaxCountPerTime);
    if (!CollectionUtils.isEmpty(jobTimeoutTrackerList)) {
      handleJobTimeoutTrackerList(jobTimeoutTrackerList);
    }
  }

  private void handleJobTimeoutTrackerList(List<JobTimeoutTracker> jobTimeoutTrackerList) {
    if (!CollectionUtils.isEmpty(jobTimeoutTrackerList)) {
      for (JobTimeoutTracker jobTimeoutTracker : jobTimeoutTrackerList) {
        JobTimeoutEvent jobTimeoutEvent = new JobTimeoutEvent(jobTimeoutTracker,
            JobTimeoutTrackerEventType.JOB_TIMEOUT);
        eventDispatcher.dispatch(jobTimeoutEvent);
      }
    }
  }

  private List<JobTimeoutTracker> getNextJobTimeoutTrackerList(
      int jobTimeoutTrackerScanMaxCountPerTime) {
    JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria = new JobTimeoutTrackerCriteria();
    jobTimeoutTrackerCriteria.getPaging().setMaxResults(jobTimeoutTrackerScanMaxCountPerTime);
    List<Integer> stateList = new ArrayList<Integer>();
    stateList.add(JobTimeoutTrackerStateEnum.INITIAL.getState());
    stateList.add(JobTimeoutTrackerStateEnum.TIMEOUT_HANDLED_FAILED.getState());
    jobTimeoutTrackerCriteria.setStateList(stateList);
    jobTimeoutTrackerCriteria.setCheckTime(new Date());
    List<JobTimeoutTracker> jobTimeoutTrackerList = this.jobTimeoutTrackerService
        .listOnPage(jobTimeoutTrackerCriteria);

    return jobTimeoutTrackerList;
  }

  @Override
  public void setPrivateEmails(String privateEmails) throws Exception {
    KiraManagerUtils.setPrivateEmails(privateEmails);
  }

  @Override
  public void setPrivatePhoneNumbers(String privatePhoneNumbers)
      throws Exception {
    KiraManagerUtils.setPrivatePhoneNumbers(privatePhoneNumbers);
  }

  @Override
  public void setAdminEmails(String adminEmails) throws Exception {
    KiraManagerUtils.setAdminEmails(adminEmails);
  }

  @Override
  public void setAdminPhoneNumbers(String adminPhoneNumbers) throws Exception {
    KiraManagerUtils.setAdminPhoneNumbers(adminPhoneNumbers);
  }

  @Override
  public void setHealthEventReceiverEmails(String healthEventReceiverEmails)
      throws Exception {
    KiraManagerUtils.setHealthEventReceiverEmails(healthEventReceiverEmails);
  }

  @Override
  public void setHealthEventReceiverPhoneNumbers(
      String healthEventReceiverPhoneNumbers) throws Exception {
    KiraManagerUtils.setHealthEventReceiverPhoneNumbers(healthEventReceiverPhoneNumbers);
  }

  @Override
  public void healthCheckForTimerTriggerScheduleForKiraManagerCluster() throws Exception {
    ExternalOverallMonitorForTimerTriggerTask.lastSuccessfullyTriggeredTime = new Date();
    if (logger.isDebugEnabled()) {
      logger.debug("healthCheckForTimerTriggerScheduleForKiraManagerCluster() is called.");
    }
  }

  @Override
  public List<RunningEnvironmentForTimerTrigger> getInValidRunningEnvironmentListForTimerTrigger()
      throws Exception {
    List<RunningEnvironmentForTimerTrigger> returnValue = new ArrayList<RunningEnvironmentForTimerTrigger>();
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolFullPath = null;
        String onePoolId = null;
        for (String onePoolShortPath : poolList) {
          onePoolFullPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
          if (poolZNodeData instanceof String) {
            onePoolId = (String) poolZNodeData;
            List<String> triggerList = zkClient.getChildren(onePoolFullPath);
            if (!CollectionUtils.isEmpty(triggerList)) {
              String oneTriggerFullPath = null;
              TriggerMetadataZNodeData triggerMetadataZNodeData = null;
              String oneTriggerId = null;
              String oneTriggerZNodeZKFullPath = null;
              for (String oneTriggerShortPath : triggerList) {
                oneTriggerFullPath = KiraUtil
                    .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                if (null != triggerMetadataZNodeData) {
                  oneTriggerId = triggerMetadataZNodeData.getTriggerId();
                  if (StringUtils.isNotBlank(oneTriggerId)) {
                    oneTriggerZNodeZKFullPath = KiraCommonUtils
                        .getTriggerZNodeZKPath(onePoolId, oneTriggerId);
                    String triggerEnvironmentsZKPath =
                        oneTriggerZNodeZKFullPath + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
                    if (zkClient.exists(triggerEnvironmentsZKPath)) {
                      List<String> triggerEnvironmentChildPathList = zkClient
                          .getChildren(triggerEnvironmentsZKPath);
                      if (!CollectionUtils.isEmpty(triggerEnvironmentChildPathList)) {
                        String oneTriggerEnvironmentZKFullPath = null;
                        String centralScheduleServiceZKParentPath = null;
                        for (String oneTriggerEnvironmentChildZNodeName : triggerEnvironmentChildPathList) {
                          oneTriggerEnvironmentZKFullPath = KiraUtil
                              .getChildFullPath(triggerEnvironmentsZKPath,
                                  oneTriggerEnvironmentChildZNodeName);
                          centralScheduleServiceZKParentPath = zkClient
                              .readData(oneTriggerEnvironmentZKFullPath, true);
                          if (StringUtils.isNotBlank(centralScheduleServiceZKParentPath)) {
                            if (centralScheduleServiceZKParentPath.contains("//")) {
                              logger.warn(
                                  "The centralScheduleServiceZKParentPath contains // which means the serviceAppName may be blank and it is not valid path for zk, so ignore it. poolId={} and triggerId={} and oneTriggerEnvironmentZKFullPath={} and centralScheduleServiceZKParentPath={}",
                                  onePoolId, oneTriggerId, oneTriggerEnvironmentZKFullPath,
                                  centralScheduleServiceZKParentPath);
                            } else {
                              if (!zkClient.exists(centralScheduleServiceZKParentPath)) {
                                RunningEnvironmentForTimerTrigger inValidRunningEnvironmentForTimerTrigger = new RunningEnvironmentForTimerTrigger(
                                    onePoolId, oneTriggerId, oneTriggerEnvironmentChildZNodeName,
                                    oneTriggerEnvironmentZKFullPath,
                                    centralScheduleServiceZKParentPath);
                                returnValue.add(inValidRunningEnvironmentForTimerTrigger);
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    return returnValue;
  }

  @Override
  public List<String> cleanInValidEnvironmentsForTimerTrigger() throws Exception {
    List<String> returnValue = new ArrayList<String>();

    List<RunningEnvironmentForTimerTrigger> inValidRunningEnvironmentListForTimerTrigger = this
        .getInValidRunningEnvironmentListForTimerTrigger();
    logger.warn("Found inValidRunningEnvironmentListForTimerTrigger={}",
        inValidRunningEnvironmentListForTimerTrigger);
    if (!CollectionUtils.isEmpty(inValidRunningEnvironmentListForTimerTrigger)) {
      String oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger = null;
      for (RunningEnvironmentForTimerTrigger oneRunningEnvironmentForTimerTrigger : inValidRunningEnvironmentListForTimerTrigger) {
        oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger = oneRunningEnvironmentForTimerTrigger
            .getEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger();
        if (zkClient.exists(oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger)) {
          zkClient.deleteRecursive(oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger);
          returnValue.add(oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger);
          logger.warn("Delete oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger={}",
              oneEnvironmentZKFullPathUnderEnvironmentsZNodeOfTrigger);
        }
      }
    }

    //Do not log now. For the log may too long to store in db's text column data type.
//		StringBuilder sb = new StringBuilder();
//		sb.append("inValidRunningEnvironmentListForTimerTrigger=");
//		sb.append(KiraCommonUtils.toString(inValidRunningEnvironmentListForTimerTrigger,true));
//		sb.append("/r/n####################/r/n");
//		sb.append("cleanedInValidEnvironmentsForTimerTrigger=");
//		sb.append(KiraCommonUtils.toString(returnValue));
//		
//		String userAddedMethodInvokeLog = sb.toString();
//		KiraClientAPI.setUserAddedMethodInvokeLog(userAddedMethodInvokeLog);

    return returnValue;
  }

  @Override
  public void setKiraMasterZone(String newKiraMasterZone) throws Exception {
    if (StringUtils.isNotBlank(newKiraMasterZone)) {
      String oldKiraMasterZone = null;

      if (zkClient.exists(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE)) {
        oldKiraMasterZone = zkClient
            .readData(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE, true);
      }
      KiraManagerCrossMultiZoneUtils.setKiraMasterZone(newKiraMasterZone);

      String userAddedMethodInvokeLog =
          "oldKiraMasterZone=" + oldKiraMasterZone + " and newKiraMasterZone=" + newKiraMasterZone;

      KiraClientAPI.setUserAddedMethodInvokeLog(userAddedMethodInvokeLog);

      logger.warn("setKiraMasterZone finished. {}", userAddedMethodInvokeLog);
    } else {
      throw new KiraHandleException("newKiraMasterZone should not be blank for setKiraMasterZone.");
    }
  }

  @Override
  public void setAdminUserNames(String adminUserNames) throws Exception {
    KiraManagerUtils.setAdminUserNames(adminUserNames);
  }

  @Override
  public Set<String> getPoolIdsWhichHaveNoRunningEnvironments() throws Exception {
    Set<String> returnValue = new LinkedHashSet<String>();

    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolFullPath = null;
        String onePoolId = null;
        for (String onePoolShortPath : poolList) {
          onePoolFullPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
          if (poolZNodeData instanceof String) {
            onePoolId = (String) poolZNodeData;
            returnValue.add(onePoolId);
            List<String> triggerList = zkClient.getChildren(onePoolFullPath);
            if (!CollectionUtils.isEmpty(triggerList)) {
              String oneTriggerFullPath = null;
              TriggerMetadataZNodeData triggerMetadataZNodeData = null;
              outerLoop:
              for (String oneTriggerShortPath : triggerList) {
                oneTriggerFullPath = KiraUtil
                    .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                if (null != triggerMetadataZNodeData) {
                  String appId = triggerMetadataZNodeData.getAppId();
                  String triggerId = triggerMetadataZNodeData.getTriggerId();
                  String targetAppId = triggerMetadataZNodeData.getTargetAppId();
                  String targetTriggerId = triggerMetadataZNodeData.getTargetTriggerId();
                  boolean isInvokeTarget = StringUtils.isNotBlank(targetAppId) && StringUtils
                      .isNotBlank(targetTriggerId);
                  String triggerEnvironmentsZKPath = null;
                  if (isInvokeTarget) {
                    triggerEnvironmentsZKPath =
                        KiraCommonUtils.getTriggerZNodeZKPath(targetAppId, targetTriggerId)
                            + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
                  } else {
                    triggerEnvironmentsZKPath =
                        KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId)
                            + KiraServerConstants.ZNODE_NAME_ENVIRONMENTS;
                  }
                  if (StringUtils.isNotBlank(triggerEnvironmentsZKPath) && zkClient
                      .exists(triggerEnvironmentsZKPath)) {
                    List<String> triggerEnvironmentChildPathList = zkClient
                        .getChildren(triggerEnvironmentsZKPath);
                    if (!CollectionUtils.isEmpty(triggerEnvironmentChildPathList)) {
                      String triggerEnvironmentZKFullPath = null;
                      String centralScheduleServiceZKParentPath = null;
                      for (String triggerEnvironmentChildZNodeName : triggerEnvironmentChildPathList) {
                        triggerEnvironmentZKFullPath = KiraUtil
                            .getChildFullPath(triggerEnvironmentsZKPath,
                                triggerEnvironmentChildZNodeName);
                        centralScheduleServiceZKParentPath = zkClient
                            .readData(triggerEnvironmentZKFullPath, true);
                        if (StringUtils.isNotBlank(centralScheduleServiceZKParentPath)) {
                          if (centralScheduleServiceZKParentPath.contains("//")) {
                            logger.warn(
                                "The centralScheduleServiceZKParentPath contains // which means the serviceAppName may be blank and it is not valid path for zk, so treat it as invalid environment. appId={} and triggerId={} and centralScheduleServiceZKParentPath={}",
                                appId, triggerId, centralScheduleServiceZKParentPath);
                          } else {
                            if (zkClient.exists(centralScheduleServiceZKParentPath)) {
                              List<String> centralScheduleServiceChildPathList = zkClient
                                  .getChildren(centralScheduleServiceZKParentPath);
                              if (!CollectionUtils.isEmpty(centralScheduleServiceChildPathList)) {
                                returnValue.remove(appId);
                                break outerLoop;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                  //May just need to check one trigger for each pool.
                  //break outerLoop;
                }
              }
            }
          }
        }
      }
    }

    logger.warn("poolIdsWhichHaveNoRunningEnvironments = {} and size={}", returnValue,
        returnValue.size());

    return returnValue;
  }

  @Override
  public KiraZoneContextData getKiraZoneContextData() throws Exception {
    String currentKiraZoneId = null;
    try {
      currentKiraZoneId = KiraCrossMultiZoneUtils.getCurrentKiraZoneId();
    } catch (Exception e) {
      logger.error(
          "Error occurs when call KiraCrossMultiZoneUtils.getCurrentKiraZoneId() in getKiraZoneContextData()",
          e);
    }

    String kiraCrossMultiZoneRoleName = null;
    try {
      KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole = KiraManagerCrossMultiZoneUtils
          .getKiraCrossMultiZoneRole(false);
      if (null != kiraCrossMultiZoneRole) {
        kiraCrossMultiZoneRoleName = kiraCrossMultiZoneRole.getName();
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when call KiraManagerCrossMultiZoneUtils.getKiraCrossMultiZoneRole(false) in getKiraZoneContextData() and currentKiraZoneId="
              + currentKiraZoneId, e);
    }

    String kiraMasterZoneId = null;
    try {
      kiraMasterZoneId = KiraCrossMultiZoneUtils.getKiraMasterZoneId(false);
    } catch (Exception e) {
      logger.error(
          "Error occurs when call KiraCrossMultiZoneUtils.getKiraMasterZoneId(false) in getKiraZoneContextData() and currentKiraZoneId="
              + currentKiraZoneId + " and kiraCrossMultiZoneRoleName=" + kiraCrossMultiZoneRoleName,
          e);
    }

    KiraZoneContextData returnValue = new KiraZoneContextData(currentKiraZoneId,
        kiraCrossMultiZoneRoleName, kiraMasterZoneId);

    return returnValue;
  }

  @Override
  public List<String> getTriggersWhosePoolIdOrTriggerIdStartWithBlankOrEndWithBlank()
      throws Exception {
    List<String> returnValue = new ArrayList<String>();

    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolFullPath = null;
        for (String onePoolShortPath : poolList) {
          onePoolFullPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
          if (poolZNodeData instanceof String) {
            List<String> triggerList = zkClient.getChildren(onePoolFullPath);
            if (!CollectionUtils.isEmpty(triggerList)) {
              String oneTriggerFullPath = null;
              TriggerMetadataZNodeData triggerMetadataZNodeData = null;
              for (String oneTriggerShortPath : triggerList) {
                oneTriggerFullPath = KiraUtil
                    .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                if (null != triggerMetadataZNodeData) {
                  String appId = triggerMetadataZNodeData.getAppId();
                  String triggerId = triggerMetadataZNodeData.getTriggerId();

                  if ((appId.startsWith(" ") || appId.endsWith(" "))
                      || (triggerId.startsWith(" ") || triggerId.endsWith(" "))
                      ) {
                    String triggerAsString =
                        appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId;
                    returnValue.add(triggerAsString);
                  }
                }
              }
            }
          }
        }
      }
    }

    logger.warn("getTriggersWhosePoolIdOrTriggerIdStartWithBlankOrEndWithBlank()={} and size={}",
        KiraCommonUtils.toString(returnValue), returnValue.size());

    return returnValue;
  }

  public List<String> getTriggersWhoseVersionAreNotValid() throws Exception {
    List<String> returnValue = new ArrayList<String>();

    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolFullPath = null;
        for (String onePoolShortPath : poolList) {
          onePoolFullPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
          if (poolZNodeData instanceof String) {
            List<String> triggerList = zkClient.getChildren(onePoolFullPath);
            if (!CollectionUtils.isEmpty(triggerList)) {
              String oneTriggerFullPath = null;
              TriggerMetadataZNodeData triggerMetadataZNodeData = null;
              for (String oneTriggerShortPath : triggerList) {
                oneTriggerFullPath = KiraUtil
                    .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                if (null != triggerMetadataZNodeData) {
                  String appId = triggerMetadataZNodeData.getAppId();
                  String triggerId = triggerMetadataZNodeData.getTriggerId();
                  String version = triggerMetadataZNodeData.getVersion();

                  boolean isVersionValidForKiraTimerTrigger = KiraCommonUtils
                      .isVersionValidForKiraTimerTrigger(version);

                  if (!isVersionValidForKiraTimerTrigger) {
                    String triggerAsString =
                        appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId
                            + KiraCommonConstants.SPECIAL_DELIMITER + version;
                    returnValue.add(triggerAsString);
                  }
                }
              }
            }
          }
        }
      }
    }

    logger.warn("getTriggersWhoseVersionAreNotValid()={} and size={}",
        KiraCommonUtils.toString(returnValue), returnValue.size());

    return returnValue;
  }

  @Override
  public void doJobRunStatistics(LinkedHashMap<String, String> paramsAsMap) throws Exception {
    Date beginTime = null;
    Date endTime = null;
    Integer daysInterval = null;
    Integer maxSampleCount = null;

    String beginTimeAsString = paramsAsMap.get("beginTime");
    String endTimeAsString = paramsAsMap.get("endTime");

    if (StringUtils.isNotBlank(beginTimeAsString)) {
      beginTime = KiraCommonUtils.getDateFromString(beginTimeAsString, true);
    }
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }
    String daysIntervalAsString = paramsAsMap.get("daysInterval");
    if (StringUtils.isNotBlank(daysIntervalAsString)) {
      daysInterval = Integer.valueOf(daysIntervalAsString);
    }
    String maxSampleCountAsString = paramsAsMap.get("maxSampleCount");
    if (StringUtils.isNotBlank(maxSampleCountAsString)) {
      maxSampleCount = Integer.valueOf(maxSampleCountAsString);
    }

    if (null == endTime) {
      endTime = new Date();
    }
    if (null == daysInterval) {
      daysInterval = Integer.valueOf(32);
    }
    if (null == beginTime) {
      beginTime = KiraManagerUtils.getDateAfterAddDays(endTime, -daysInterval);
    }
    if (null == maxSampleCount) {
      maxSampleCount = Integer.valueOf(10);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "beginTime={} and endTime={} and daysInterval={} and maxSampleCount={} for doJobRunStatistics",
          KiraCommonUtils.getDateAsString(beginTime), KiraCommonUtils.getDateAsString(endTime),
          daysInterval, maxSampleCount);
    }

    String contextLog = "paramsAsMap=" + paramsAsMap + ". Finally beginTime=" + KiraCommonUtils
        .getDateAsString(beginTime) + " and endTime=" + KiraCommonUtils.getDateAsString(endTime)
        + " and daysInterval=" + daysInterval + " and maxSampleCount=" + maxSampleCount;
    KiraClientAPI.setUserAddedMethodInvokeLog(contextLog);

    this.jobRunStatisticsService.doJobRunStatistics(beginTime, endTime, maxSampleCount);

  }

  @Override
  public void doTriggersPredictReport(LinkedHashMap<String, String> paramsAsMap) throws Exception {
    String startTimeAsString = paramsAsMap.get("startTime");
    String endTimeAsString = paramsAsMap.get("endTime");
    Date startTime = null;
    if (StringUtils.isNotBlank(startTimeAsString)) {
      startTime = KiraCommonUtils.getDateFromString(startTimeAsString, true);
    }

    Date endTime = null;
    if (StringUtils.isNotBlank(endTimeAsString)) {
      endTime = KiraCommonUtils.getDateFromString(endTimeAsString, true);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("startTime={} and endTime={} for getTriggerPredictReportLineDataList",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }

    String poolIdListAsString = paramsAsMap.get("poolIdList");
    List<String> poolIdList = KiraCommonUtils
        .getStringListByDelmiter(poolIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    String triggerIdListAsString = paramsAsMap.get("triggerIdList");
    List<String> triggerIdList = KiraCommonUtils
        .getStringListByDelmiter(triggerIdListAsString, KiraCommonConstants.COMMA_DELIMITER);

    List<TriggerPredictReportLineData> triggerPredictReportLineDataList = this.triggerMetadataService
        .getTriggerPredictReportLineDataList(startTime, endTime, poolIdList, triggerIdList);

    ExcelSheetData excelSheetDataForTriggersPredictReport = this
        .getExcelSheetDataForTriggersPredictReport(startTime, endTime,
            triggerPredictReportLineDataList);

    List<ExcelSheetData> excelSheetDataListForTriggersPredictReport = new LinkedList<ExcelSheetData>();
    excelSheetDataListForTriggersPredictReport.add(excelSheetDataForTriggersPredictReport);
    byte[] excelFileByteDataForTriggersPredictReport = ExcelUtil
        .getExcelFileByteData(excelSheetDataListForTriggersPredictReport);

    //Update cache
    KiraManagerDataCenter.excelFileByteDataForTriggersPredictReport = excelFileByteDataForTriggersPredictReport;
  }

  private ExcelSheetData getExcelSheetDataForTriggersPredictReport(Date startTime, Date endTime,
      List<TriggerPredictReportLineData> triggerPredictReportLineDataList) {
    ExcelSheetData returnValue = null;
    String sheetNamePrefix = "定时任务预执行报告";
    List<ExcelExportDataHolder> excelExportDataHolderList = new ArrayList<ExcelExportDataHolder>();
    ExcelExportDataHolder excelExportDataHolderForDescribeTriggersPredictReport = this
        .getExcelExportDataHolderForDescribeTriggersPredictReport(startTime, endTime,
            triggerPredictReportLineDataList);
    excelExportDataHolderList.add(excelExportDataHolderForDescribeTriggersPredictReport);
    ExcelExportDataHolder excelExportDataHolderForTriggersPredictReport = this
        .getExcelExportDataHolderForTriggersPredictReport(triggerPredictReportLineDataList);
    excelExportDataHolderList.add(excelExportDataHolderForTriggersPredictReport);
    returnValue = new ExcelSheetData(sheetNamePrefix, excelExportDataHolderList);
    return returnValue;
  }

  private ExcelExportDataHolder getExcelExportDataHolderForDescribeTriggersPredictReport(
      Date startTime, Date endTime,
      List<TriggerPredictReportLineData> triggerPredictReportLineDataList) {
    ExcelExportDataHolder returnValue = null;
    List<String> headerLineList = getHeaderLineListForDescribeTriggersPredictReport();
    List<List<String>> dataList = getDataListForDescribeTriggersPredictReport(startTime, endTime);
    List<ExcelCellDescriptor> dataCellDescriptorList = this
        .getDataCellDescriptorListForDescribeTriggersPredictReport();
    returnValue = new ExcelExportDataHolder(headerLineList, dataList, dataCellDescriptorList);
    return returnValue;
  }

  private List<ExcelCellDescriptor> getDataCellDescriptorListForDescribeTriggersPredictReport() {
    List<ExcelCellDescriptor> dataCellDescriptorList = new ArrayList<ExcelCellDescriptor>();
    ExcelCellDescriptor startTimeExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(startTimeExcelCellDescriptor);
    ExcelCellDescriptor endTimeExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(endTimeExcelCellDescriptor);
    ExcelCellDescriptor descriptionExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(descriptionExcelCellDescriptor);
    return dataCellDescriptorList;
  }

  private List<String> getHeaderLineListForDescribeTriggersPredictReport() {
    List<String> returnList = new ArrayList<String>();
    returnList.add("时间区间(开始)");
    returnList.add("时间区间(结束)");
    returnList.add("功能描述");
    return returnList;
  }

  private List<List<String>> getDataListForDescribeTriggersPredictReport(Date startTime,
      Date endTime) {
    List<List<String>> returnValue = new ArrayList<List<String>>();
    List<String> oneLineList = toListForDescribeTriggersPredictReport(startTime, endTime);
    returnValue.add(oneLineList);
    return returnValue;
  }

  private List<String> toListForDescribeTriggersPredictReport(Date startTime, Date endTime) {
    List<String> result = new ArrayList<String>();
    String startTimeAsString = KiraCommonUtils.getDateAsString(startTime);
    result.add(startTimeAsString);
    String endTimeAsString = KiraCommonUtils.getDateAsString(endTime);
    result.add(endTimeAsString);
    String description = "预计未来在该时间区间内将被触发执行的定时任务统计信息列表。";
    result.add(description);
    return result;
  }

  private ExcelExportDataHolder getExcelExportDataHolderForTriggersPredictReport(
      List<TriggerPredictReportLineData> triggerPredictReportLineDataList) {
    ExcelExportDataHolder returnValue = null;
    List<String> headerLineList = getHeaderLineListForTriggersPredictReport();
    List<List<String>> dataList = getDataListForTriggersPredictReport(
        triggerPredictReportLineDataList);
    List<ExcelCellDescriptor> dataCellDescriptorList = this
        .getDataCellDescriptorListForTriggersPredictReport();
    returnValue = new ExcelExportDataHolder(headerLineList, dataList, dataCellDescriptorList);
    return returnValue;
  }

  private List<ExcelCellDescriptor> getDataCellDescriptorListForTriggersPredictReport() {
    List<ExcelCellDescriptor> dataCellDescriptorList = new ArrayList<ExcelCellDescriptor>();
    ExcelCellDescriptor poolIdExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(poolIdExcelCellDescriptor);
    ExcelCellDescriptor triggerIdExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(triggerIdExcelCellDescriptor);
    ExcelCellDescriptor descriptionExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(descriptionExcelCellDescriptor);
    ExcelCellDescriptor firstTriggeredTimeInTheFutureExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_STRING, HSSFCellStyle.ALIGN_LEFT);
    dataCellDescriptorList.add(firstTriggeredTimeInTheFutureExcelCellDescriptor);
    ExcelCellDescriptor maxHistoryRuntimeInSecondsExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_NUMERIC, HSSFCellStyle.ALIGN_RIGHT);
    dataCellDescriptorList.add(maxHistoryRuntimeInSecondsExcelCellDescriptor);
    ExcelCellDescriptor minHistoryRuntimeInSecondsExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_NUMERIC, HSSFCellStyle.ALIGN_RIGHT);
    dataCellDescriptorList.add(minHistoryRuntimeInSecondsExcelCellDescriptor);
    ExcelCellDescriptor avgHistoryRuntimeInSecondsExcelCellDescriptor = new ExcelCellDescriptor(
        HSSFCell.CELL_TYPE_NUMERIC, HSSFCellStyle.ALIGN_RIGHT);
    dataCellDescriptorList.add(avgHistoryRuntimeInSecondsExcelCellDescriptor);
    return dataCellDescriptorList;
  }

  private List<List<String>> getDataListForTriggersPredictReport(
      List<TriggerPredictReportLineData> triggerPredictReportLineDataList) {
    List<List<String>> returnValue = new ArrayList<List<String>>();
    if (null != triggerPredictReportLineDataList) {
      for (TriggerPredictReportLineData oneTriggerPredictReportLineData : triggerPredictReportLineDataList) {
        List<String> oneLineList = toListForTriggersPredictReport(oneTriggerPredictReportLineData);
        returnValue.add(oneLineList);
      }
    }
    return returnValue;
  }

  private List<String> toListForTriggersPredictReport(
      TriggerPredictReportLineData triggerPredictReportLineData) {
    List<String> result = new ArrayList<String>();
    if (triggerPredictReportLineData != null) {
      result.add(triggerPredictReportLineData.getPoolId());
      result.add(triggerPredictReportLineData.getTriggerId());
      result.add(triggerPredictReportLineData.getDescription());
      Date firstTriggeredTimeInTheFuture = triggerPredictReportLineData
          .getFirstTriggeredTimeInTheFuture();
      String firstTriggeredTimeInTheFutureAsString = KiraCommonUtils
          .getDateAsString(firstTriggeredTimeInTheFuture);
      result.add(firstTriggeredTimeInTheFutureAsString);
      Integer maxHistoryRuntimeInSeconds = triggerPredictReportLineData
          .getMaxHistoryRuntimeInSeconds();
      if (null != maxHistoryRuntimeInSeconds) {
        result.add(maxHistoryRuntimeInSeconds.toString());
      } else {
        result.add(null);
      }
      Integer minHistoryRuntimeInSeconds = triggerPredictReportLineData
          .getMinHistoryRuntimeInSeconds();
      if (null != minHistoryRuntimeInSeconds) {
        result.add(minHistoryRuntimeInSeconds.toString());
      } else {
        result.add(null);
      }
      Integer avgHistoryRuntimeInSeconds = triggerPredictReportLineData
          .getAvgHistoryRuntimeInSeconds();
      if (null != avgHistoryRuntimeInSeconds) {
        result.add(avgHistoryRuntimeInSeconds.toString());
      } else {
        result.add(null);
      }
    }
    return result;
  }

  private List<String> getHeaderLineListForTriggersPredictReport() {
    List<String> returnList = new ArrayList<String>();
    returnList.add("AppId");
    returnList.add("定时任务Id");
    returnList.add("描述");
    returnList.add("预计未来在该时间区间内第一次触发执行的时间");
    returnList.add("历史最大执行耗时（秒）");
    returnList.add("历史最小执行耗时（秒）");
    returnList.add("历史平均执行耗时（秒）");
    return returnList;
  }

  @Override
  public List<String> getTriggersWhichCanBeScheduledWhenInOldNoZoneSystem() throws Exception {
    List<String> returnValue = new ArrayList<String>();

    boolean isMasterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(true);

    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolFullPath = null;
        for (String onePoolShortPath : poolList) {
          onePoolFullPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
          if (poolZNodeData instanceof String) {
            List<String> triggerList = zkClient.getChildren(onePoolFullPath);
            if (!CollectionUtils.isEmpty(triggerList)) {
              String oneTriggerFullPath = null;
              TriggerMetadataZNodeData triggerMetadataZNodeData = null;
              for (String oneTriggerShortPath : triggerList) {
                oneTriggerFullPath = KiraUtil
                    .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                if (null != triggerMetadataZNodeData) {
                  if (KiraCommonUtils.isCanBeScheduledInOldNoZoneSystem(triggerMetadataZNodeData)) {
                    String appId = triggerMetadataZNodeData.getAppId();
                    String triggerId = triggerMetadataZNodeData.getTriggerId();

                    String triggerAsString =
                        appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId;
                    returnValue.add(triggerAsString);
                  }
                }
              }
            }
          }
        }
      }
    }

    logger.warn(
        "getTriggersWhichCanBeScheduledWhenInOldNoZoneSystem()={} and size={} and isMasterZone={}",
        KiraCommonUtils.toString(returnValue), returnValue.size(), isMasterZone);

    return returnValue;
  }

  /**
   * Warning: This can only be run once and when upgrade the kira system version from
   * noZoneOldSystem to ZoneSystem.
   *
   * @return the triggers which are handled.
   */
  @Override
  public List<String> migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      boolean isMasterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(true);
      List<String> returnValue = new ArrayList<String>();

      if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
        List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
        if (!CollectionUtils.isEmpty(poolList)) {
          String onePoolFullPath = null;
          for (String onePoolShortPath : poolList) {
            onePoolFullPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
            Object poolZNodeData = zkClient.readData(onePoolFullPath, true);
            if (poolZNodeData instanceof String) {
              List<String> triggerList = zkClient.getChildren(onePoolFullPath);
              if (!CollectionUtils.isEmpty(triggerList)) {
                String oneTriggerFullPath = null;
                TriggerMetadataZNodeData triggerMetadataZNodeData = null;
                for (String oneTriggerShortPath : triggerList) {
                  oneTriggerFullPath = KiraUtil
                      .getChildFullPath(onePoolFullPath, oneTriggerShortPath);
                  triggerMetadataZNodeData = zkClient.readData(oneTriggerFullPath, true);
                  if (null != triggerMetadataZNodeData) {
                    final String appId = triggerMetadataZNodeData.getAppId();
                    final String triggerId = triggerMetadataZNodeData.getTriggerId();
                    String versionOnZK = triggerMetadataZNodeData.getVersion();
                    Boolean copyFromMasterToSlaveZoneOnZK = triggerMetadataZNodeData
                        .getCopyFromMasterToSlaveZone();
                    Boolean onlyScheduledInMasterZoneOnZK = triggerMetadataZNodeData
                        .getOnlyScheduledInMasterZone();
                    try {
                      //1. get info from db first
                      //2. compare info in db with zk data check if need to handle this trigger.
                      //3. If need to handle. Update the zk data and then wait for the new version trigger found in db and then update onlyScheduledInMasterZone in db.
                      TriggerMetadata triggerMetadataInDB = triggerMetadataService
                          .getLatestAndAvailableTriggerMetadata(appId, triggerId, null);
                      if (null != triggerMetadataInDB) {
                        String versionInDB = triggerMetadataInDB.getVersion();
                        if (StringUtils.equals(versionOnZK, versionInDB)) {
                          //Continue only if the version is identical
                          Boolean copyFromMasterToSlaveZoneInDB = triggerMetadataInDB
                              .getCopyFromMasterToSlaveZone();
                          Boolean onlyScheduledInMasterZoneInDB = triggerMetadataInDB
                              .getOnlyScheduledInMasterZone();

                          logger.warn(
                              "appId={} and triggerId={} and copyFromMasterToSlaveZoneOnZK={} and copyFromMasterToSlaveZoneInDB={} and onlyScheduledInMasterZoneOnZK={} and onlyScheduledInMasterZoneInDB={}",
                              appId, triggerId, copyFromMasterToSlaveZoneOnZK,
                              copyFromMasterToSlaveZoneInDB, onlyScheduledInMasterZoneOnZK,
                              onlyScheduledInMasterZoneInDB);

                          if (copyFromMasterToSlaveZoneOnZK.equals(copyFromMasterToSlaveZoneInDB)
                              && onlyScheduledInMasterZoneOnZK
                              .equals(onlyScheduledInMasterZoneInDB)) {
                            if (!isMasterZone) {
                              if (KiraCommonUtils
                                  .isCanBeScheduledInOldNoZoneSystem(triggerMetadataZNodeData)) {
                                if (onlyScheduledInMasterZoneOnZK) {
                                  //Just handle those onlyScheduledInMasterZoneOnZK
                                  TriggerMetadataClientSideView triggerMetadataClientSideView = KiraClientAPI
                                      .getTriggerMetadataClientSideView(appId, triggerId);
                                  if (null != triggerMetadataClientSideView) {
                                    triggerMetadataClientSideView
                                        .setOnlyScheduledInMasterZone(Boolean.FALSE);
                                    TriggerMetadataClientSideView newTriggerMetadataClientSideView = KiraClientAPI
                                        .updateTrigger(triggerMetadataClientSideView);
                                    final String newVersion = newTriggerMetadataClientSideView
                                        .getVersion();

                                    TriggerMetadata finalNewTriggerMetadata = KiraCommonUtils
                                        .retryUntilNotNullOrTimeout(
                                            new Callable<TriggerMetadata>() {
                                              @Override
                                              public TriggerMetadata call()
                                                  throws Exception {
                                                TriggerMetadata newTriggerMetadata = triggerMetadataService
                                                    .getLatestAndAvailableTriggerMetadata(appId,
                                                        triggerId, newVersion);
                                                return newTriggerMetadata;
                                              }

                                            }, 10000L);

                                    if (null != finalNewTriggerMetadata) {
                                      //Update onlyScheduledInMasterZone in db
                                      String comments = KiraCommonConstants.SPECIAL_DELIMITER
                                          + "Updated for migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem. onlyScheduledInMasterZone is set to false "
                                          + " on " + KiraCommonUtils
                                          .getDateAsStringToMsPrecision(new Date());
                                      int updateCount = triggerMetadataService
                                          .updateCrossMultiZoneData(appId, triggerId, newVersion,
                                              null, Boolean.FALSE, comments);
                                      if (updateCount > 0) {
                                        String triggerAsString =
                                            appId + KiraCommonConstants.SPECIAL_DELIMITER
                                                + triggerId;
                                        returnValue.add(triggerAsString);
                                      }
                                    }

                                  }
                                }
                              }
                            }
                          } else {
                            //Have difference in db and zk, always need to update the data in db from data on zk.
                            String comments = KiraCommonConstants.SPECIAL_DELIMITER
                                + "Updated for migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem. copyFromMasterToSlaveZone is set to "
                                + copyFromMasterToSlaveZoneOnZK
                                + " and onlyScheduledInMasterZone is set to "
                                + onlyScheduledInMasterZoneOnZK + " on " + KiraCommonUtils
                                .getDateAsStringToMsPrecision(new Date());
                            int updateCount = triggerMetadataService
                                .updateCrossMultiZoneData(appId, triggerId, versionInDB,
                                    copyFromMasterToSlaveZoneOnZK, onlyScheduledInMasterZoneOnZK,
                                    comments);
                            if (updateCount > 0) {
                              String triggerAsString =
                                  appId + KiraCommonConstants.SPECIAL_DELIMITER + triggerId;
                              returnValue.add(triggerAsString);
                            }
                          }
                        }
                      }
                    } catch (Exception e) {
                      //Do not block the process even if the error occurs.
                      logger.error(
                          "Error occurs when migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem. appId="
                              + appId + " and triggerId=" + triggerId, e);
                    }
                  }
                }
              }
            }
          }
        }
      }
      logger.warn(
          "migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem()={} and size={} and isMasterZone={}",
          KiraCommonUtils.toString(returnValue), returnValue.size(), isMasterZone);
      return returnValue;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn("It takes " + costTime
          + " milliseconds to migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem.");
    }
  }

  @Override
  public void testSendEmail() {
    String privateEmail = KiraManagerUtils.getPrivateEmails();
    String[] emailsArr = privateEmail.split(",");
    List<String> sendEmailList = new ArrayList<String>();
    for (int i = 0; i < emailsArr.length; i++) {
      sendEmailList.add(emailsArr[i]);
    }
    try {
      EmailUtils.sendInnerEmail(sendEmailList, "kira send email test !" + new Date().toString(),
          "Kira Test Send Email");
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  @Override
  public void testSendSMS() {
    String privatePhoneNum = KiraManagerUtils.getPrivatePhoneNumbers();
    String[] phoneNumArr = privatePhoneNum.split(",");
    List<String> phoneNumList = new ArrayList<String>();
    for (int i = 0; i < phoneNumArr.length; i++) {
      phoneNumList.add(phoneNumArr[i]);
    }

    try {
      SMSUtils.sendSMS(phoneNumList, "kira Job SMS Report ");
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

}
