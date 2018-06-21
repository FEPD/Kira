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

import com.alibaba.fastjson.JSON;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobCancelContextData;
import com.yihaodian.architecture.kira.common.JobCancelTask;
import com.yihaodian.architecture.kira.common.JobItemRunContextData;
import com.yihaodian.architecture.kira.common.JobItemRunTask;
import com.yihaodian.architecture.kira.common.JobStatusEnum;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import com.yihaodian.architecture.kira.common.impl.RemoteKiraClientEnvironment;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import com.yihaodian.architecture.kira.manager.alarm.AlarmCenter;
import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobDao;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.ManuallyReRunJobResult;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.health.monitor.task.CreateAndRunJobForTimerTriggerMonitorTask;
import com.yihaodian.architecture.kira.manager.service.JobHistoryService;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.JobTimeoutTrackerStateEnum;
import com.yihaodian.architecture.kira.manager.util.KiraManagerJobHandler;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.TriggerEnvironment;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class JobServiceImpl extends Service implements JobService {

  private TriggerMetadataService triggerMetadataService;

  private JobDao jobDao;

  private JobHistoryService jobHistoryService;

  private JobItemService jobItemService;

  private AlarmCenter alarmCenter;

  private KiraClientMetadataService kiraClientMetadataService;

  private JobTimeoutTrackerService jobTimeoutTrackerService;

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public void setJobDao(JobDao jobDao) {
    this.jobDao = jobDao;
  }

  public void setJobHistoryService(JobHistoryService jobHistoryService) {
    this.jobHistoryService = jobHistoryService;
  }

  public void setJobItemService(JobItemService jobItemService) {
    this.jobItemService = jobItemService;
  }

  public void setAlarmCenter(AlarmCenter alarmCenter) {
    this.alarmCenter = alarmCenter;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public void setJobTimeoutTrackerService(
      JobTimeoutTrackerService jobTimeoutTrackerService) {
    this.jobTimeoutTrackerService = jobTimeoutTrackerService;
  }

  public void insert(Job job) {
    jobDao.insert(job);
  }

  public int update(Job job) {
    int actualRowsAffected = 0;

    String id = job.getId();

    Job _oldJob = jobDao.select(id);

    if (_oldJob != null) {
      actualRowsAffected = jobDao.update(job);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    Job _oldJob = jobDao.select(id);

    if (_oldJob != null) {
      actualRowsAffected = jobDao.delete(id);
    }

    return actualRowsAffected;
  }

  public Job select(String id) {
    return jobDao.select(id);
  }

  @Override
  public Job selectForUpdate(String id) {
    return jobDao.selectForUpdate(id);
  }

  public List<Job> list(JobCriteria jobCriteria) {
    return jobDao.list(jobCriteria);
  }

  public List<Job> listOnPage(JobCriteria jobCriteria) {
    return jobDao.listOnPage(jobCriteria);
  }

  @Override
  public List<JobDetailData> getJobDetailDataListOnPage(JobCriteria jobCriteria) {
    return jobDao.getJobDetailDataListOnPageUsingLimit(jobCriteria);
  }

  @Override
  public List<JobDetailData> getJobDetailDataListForJobsBeingProcessedOnPage(
      JobCriteria jobCriteria) {
    List<JobDetailData> returnValue = new ArrayList<JobDetailData>();
    try {
      if (null != jobCriteria) {
        List<Integer> jobStatusIdList = getJobStatusIdListOfJobsBeingProcessed();
        jobCriteria.setJobStatusIdList(jobStatusIdList);
        List<JobDetailData> jobDetailDataListOnPage = jobDao
            .getJobDetailDataListOnPageUsingLimit(jobCriteria);
        if (null != jobDetailDataListOnPage) {
          returnValue = jobDetailDataListOnPage;
        }
      } else {
        logger.warn("jobCriteria is null for getJobDetailDataListForJobsBeingProcessedOnPage.");
      }
    } catch (Exception e) {
      logger.error("Error occurs on getJobDetailDataListForJobsBeingProcessedOnPage.", e);
    }

    return returnValue;
  }

  private List<Integer> getJobStatusIdListOfJobsBeingProcessed() {
    List<Integer> returnValue = new ArrayList<Integer>();
    returnValue.add(JobStatusEnum.DELIVERING.getId());
    returnValue.add(JobStatusEnum.DELIVERY_SUCCESS.getId());
    returnValue.add(JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId());
    returnValue.add(JobStatusEnum.RUNNING.getId());

    //Now regard RUN_PARTIAL_SUCCESS as not being processed.
    //returnValue.add(JobStatusEnum.RUN_PARTIAL_SUCCESS.getId());

    return returnValue;
  }

  @Override
  public Job createJob(String appId, String triggerId, String version, Boolean manuallyScheduled,
      String createdBy, Date triggerTime, Date runAtTime, boolean createJobHistory) {
    Job job = null;

    TriggerMetadataCriteria triggerMetadataCriteria = new TriggerMetadataCriteria();
    triggerMetadataCriteria.setAppId(appId);
    triggerMetadataCriteria.setTriggerId(triggerId);
    triggerMetadataCriteria.setVersion(version);
    List<TriggerMetadata> triggerMetadataList = triggerMetadataService
        .list(triggerMetadataCriteria);
    if (!CollectionUtils.isEmpty(triggerMetadataList)) {
      TriggerMetadata triggerMetadata = triggerMetadataList.get(0);
      Long triggerMetadataId = triggerMetadata.getId();
      job = new Job();
      String uuid = KiraCommonUtils.getUUID();
      job.setId(uuid);
      job.setTriggerMetadataId(triggerMetadataId);
      job.setAppId(appId);
      job.setTriggerId(triggerId);
      job.setVersion(version);
      job.setCreateTime(triggerTime);
      job.setManuallyScheduled(manuallyScheduled);
      job.setCreatedBy(createdBy);
      job.setRunAtTime(runAtTime);
      Integer jobStatusId = JobStatusEnum.CREATED.getId();
      job.setJobStatusId(jobStatusId);
      String argumentsAsJsonArrayString = triggerMetadata.getArgumentsAsJsonArrayString();
      job.setArgumentsAsJsonArrayString(argumentsAsJsonArrayString);

      jobDao.insert(job);
      if (createJobHistory) {
        jobHistoryService.createJobHistory(job.getId(), jobStatusId, null, triggerTime);
      }
    } else {
      logger.warn("Can not get triggerMetadataList by triggerMetadataCriteria={}",
          KiraCommonUtils.toString(triggerMetadataCriteria));
    }
    return job;
  }

  @Override
  public void updateJobStatus(String jobId, Integer jobStatusId,
      String resultData, Date updateTime, List<Integer> jobStatusIdList) {
    jobDao.updateJobStatus(jobId, jobStatusId, resultData, updateTime, jobStatusIdList, null);
  }

  @Override
  public void updateJobStatusAndCreateHistoryIfNeeded(String jobId, Integer jobStatusId,
      String resultData, Date updateTime, List<Integer> jobStatusIdList,
      boolean createJobHistory) {
    if (createJobHistory) {
      jobHistoryService.createJobHistory(jobId, jobStatusId, resultData, updateTime);
    }
    this.updateJobStatus(jobId, jobStatusId, resultData, updateTime, jobStatusIdList);
  }

  @Override
  public void updateStatusOfJobByJobItems(String jobId) {
    int updateCount = tryToUpdateStatusOfJobByJobItems(jobId, KiraServerConstants.MAX_TRY_COUNT_DB);
    if (logger.isDebugEnabled()) {
      logger.debug("Final got updateCount={} for jobId={} by tryToUpdateStatusOfJobByJobItems.",
          updateCount, jobId);
    }
  }

  private int tryToUpdateStatusOfJobByJobItems(String jobId, int remainTryCount) {
    int returnValue = 0;
    if (StringUtils.isNotBlank(jobId)) {
      Job job = this.select(jobId);
      if (null != job) {
        Integer oldJobStatusId = job.getJobStatusId();
        Integer oldDataVersion = job.getDataVersion();
        Integer newJobStatusIdOfJob = null;
        JobItemCriteria jobItemCriteria = new JobItemCriteria();
        jobItemCriteria.setJobId(jobId);
        List<JobItem> jobItemList = jobItemService.list(jobItemCriteria);
        if (!CollectionUtils.isEmpty(jobItemList)) {
          //It only need to check the run status now, because the status for deliver or cancel can be handled locally.

          int jobItemListSize = jobItemList.size();

          int runningCount = 0;
          int runSuccessCount = 0;
          int runFailedCount = 0;
          int runPartialSuccess = 0;
          int noNeedToRunBusinessMethodCount = 0;

          for (JobItem jobItem : jobItemList) {
            Integer jobStatusIdOfJobItem = jobItem.getJobStatusId();
            if (JobStatusEnum.RUNNING.getId().equals(jobStatusIdOfJobItem)) {
              runningCount++;
            } else if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusIdOfJobItem)) {
              runSuccessCount++;
            } else if (JobStatusEnum.RUN_FAILED.getId().equals(jobStatusIdOfJobItem)) {
              runFailedCount++;
            } else if (JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusIdOfJobItem)) {
              runPartialSuccess++;
            } else if (JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId()
                .equals(jobStatusIdOfJobItem)) {
              noNeedToRunBusinessMethodCount++;
            }
          }

          if (runSuccessCount > 0
              && (runSuccessCount + noNeedToRunBusinessMethodCount) == jobItemListSize) {
            //执行成功
            newJobStatusIdOfJob = JobStatusEnum.RUN_SUCCESS.getId();
          } else if (runFailedCount > 0
              && (runFailedCount + noNeedToRunBusinessMethodCount) == jobItemListSize) {
            //执行失败
            newJobStatusIdOfJob = JobStatusEnum.RUN_FAILED.getId();
          } else if (runSuccessCount > 0
              || runPartialSuccess > 0) {
            //部分执行成功
            newJobStatusIdOfJob = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
          } else if (noNeedToRunBusinessMethodCount > 0
              && noNeedToRunBusinessMethodCount == jobItemListSize) {
            //无需执行业务方法
            newJobStatusIdOfJob = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
          } else if (runningCount > 0) {
            //正在执行
            newJobStatusIdOfJob = JobStatusEnum.RUNNING.getId();
          } else if (runFailedCount > 0) {
            //执行失败
            newJobStatusIdOfJob = JobStatusEnum.RUN_FAILED.getId();
          }
          if (null != newJobStatusIdOfJob) {
            boolean isNeedToUpdateJobStatus = KiraCommonUtils
                .isNeedToUpdateJobStatus(oldJobStatusId, newJobStatusIdOfJob);
            String resultData = null;
            Date now = new Date();
            if (isNeedToUpdateJobStatus) {
              List<Integer> canBeUpdatedJobStatusIdList = KiraCommonUtils
                  .getCanBeUpdatedJobStatusIdList(newJobStatusIdOfJob);
              int updateCount = jobDao.updateJobStatus(jobId, newJobStatusIdOfJob, resultData, now,
                  canBeUpdatedJobStatusIdList, oldDataVersion);
              if (updateCount == 0) {
                if (remainTryCount > 0) {
                  //optimistically try updateStatusOfJobByJobItems for many times until success
                  remainTryCount--;
                  returnValue = tryToUpdateStatusOfJobByJobItems(jobId, remainTryCount);
                  if (0 == remainTryCount) {
                    logger.warn(
                        "Last time to call tryToUpdateStatusOfJobByJobItems. jobId={} and oldJobStatusId={} and remainTryCount={} and returnValue={}",
                        jobId, oldJobStatusId, remainTryCount, returnValue);
                  }
                }
              } else {
                returnValue = updateCount;
                if (updateCount > 0) {
                  jobHistoryService.createJobHistory(jobId, newJobStatusIdOfJob, resultData, now);
                }
              }
            } else {
              returnValue = -1;
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "isNeedToUpdateJobStatus=false for oldJobStatusId={} and newJobStatusIdOfJob={} of jobId={}",
                    oldJobStatusId, newJobStatusIdOfJob, jobId);
              }
            }
          } else {
            logger.warn(
                "jobStatusIdOfJob can not be determinded. May some case is not covered? jobId={},jobItemList={},oldJobStatusId={},runningCount={},runSuccessCount={},runFailedCount={},runPartialSuccess={}",
                jobId, KiraCommonUtils.toString(jobItemList), oldJobStatusId, runningCount,
                runSuccessCount, runFailedCount, runPartialSuccess);
          }
        }
      }
    }
    return returnValue;
  }

  @Override
  public HandleResult cancelJob(String jobId, String cancelJobJsonMapString) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (StringUtils.isNotBlank(jobId)) {
        JobCriteria jobCriteria = new JobCriteria();
        jobCriteria.setId(jobId);
        List<JobDetailData> jobDetailDataListOnPage = this.getJobDetailDataListOnPage(jobCriteria);
        if (!CollectionUtils.isEmpty(jobDetailDataListOnPage)) {
          JobDetailData jobDetailData = jobDetailDataListOnPage.get(0);
          Integer jobStatusId = jobDetailData.getJobStatusId();
          if (JobStatusEnum.RUNNING.getId().equals(jobStatusId)) {
            Long triggerMetadataId = jobDetailData.getTriggerMetadataId();
            TriggerMetadata triggerMetadata = this.triggerMetadataService.select(triggerMetadataId);
            if (null != triggerMetadata) {
              String targetAppId = triggerMetadata.getTargetAppId();
              String targetTriggerId = triggerMetadata.getTargetTriggerId();
              boolean isInvokeTarget =
                  StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId);
              String appId = jobDetailData.getAppId();
              String triggerId = jobDetailData.getTriggerId();
              TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
              JobItemCriteria jobItemCriteria = new JobItemCriteria();
              jobItemCriteria.setJobId(jobId);
              List<JobItem> jobItemList = this.jobItemService.list(jobItemCriteria);
              if (!CollectionUtils.isEmpty(jobItemList)) {
                String serviceUrl = null;
                ICentralScheduleService centralScheduleService = null;
                JobCancelRequest jobCancelRequest = null;
                if (isInvokeTarget) {
                  jobCancelRequest = new JobCancelRequest(targetAppId, targetTriggerId, new Date());
                  jobCancelRequest.setSponsorPoolId(appId);
                  jobCancelRequest.setSponsorTriggerId(triggerId);
                } else {
                  jobCancelRequest = new JobCancelRequest(appId, triggerId, new Date());
                }

                if (StringUtils.isNotBlank(cancelJobJsonMapString)) {
                  Map cancelJobJsonMap = JSON.parseObject(cancelJobJsonMapString.trim(), Map.class);
                  jobCancelRequest.setMethodParamDataMap(cancelJobJsonMap);
                }
                RemoteKiraClientEnvironment environment = null;
                JobCancelContextData jobCancelContextData = null;
                JobCancelTask jobCancelTask = null;
                List<JobCancelTask> jobCancelTaskList = new ArrayList<JobCancelTask>();
                for (JobItem jobItem : jobItemList) {
                  serviceUrl = jobItem.getServiceUrl();
                  centralScheduleService = KiraManagerUtils.getCentralScheduleService(serviceUrl);
                  environment = new RemoteKiraClientEnvironment(serviceUrl, centralScheduleService);
                  jobCancelContextData = new JobCancelContextData(jobCancelRequest, environment);
                  jobCancelTask = new JobCancelTask(jobCancelContextData,
                      new KiraManagerJobHandler());
                  jobCancelTaskList.add(jobCancelTask);
                }
                if (!CollectionUtils.isEmpty(jobCancelTaskList)) {
                  HandleResult innerHandleResult = KiraCommonUtils
                      .handleJobCancelTaskList(triggerIdentity, jobCancelTaskList);
                  if (null != innerHandleResult) {
                    resultCode = innerHandleResult.getResultCode();
                    resultData = innerHandleResult.getResultData();
                  } else {
                    resultCode = KiraServerConstants.RESULT_CODE_FAILED;
                    resultData = "innerHandleResult is empty for cancelJob. jobId=" + jobId;
                  }
                } else {
                  resultCode = KiraServerConstants.RESULT_CODE_FAILED;
                  resultData = "jobCancelTaskList is empty. jobId=" + jobId;
                }
              } else {
                resultCode = KiraServerConstants.RESULT_CODE_FAILED;
                resultData = "Can not find job details data by jobId=" + jobId;
              }
            } else {
              resultCode = KiraServerConstants.RESULT_CODE_FAILED;
              resultData = "Can not find triggerMetadata by triggerMetadataId=" + triggerMetadataId;
            }
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData = "Can not cancel the job for it is not in running state.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "Can not find job details data.";
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "jobId should not be blank.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on cancelJob.jobId=" + jobId, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String outerExceptionDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on cancelJob. jobId=" + jobId + ". exceptionOccured="
            + outerExceptionDesc;
      }

      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public Map<String, String> getCentralScheduleServiceUrlArgumentsAsJsonArrayStringMap(Job job,
      List<String> centralScheduleServiceUrlList) {
    Map<String, String> returnValue = new LinkedHashMap<String, String>();
    if (null != job) {
      String argumentsAsJsonArrayString = job.getArgumentsAsJsonArrayString();
      if (StringUtils.isNotBlank(argumentsAsJsonArrayString)) {
        if (null != centralScheduleServiceUrlList) {
          for (String centralScheduleServiceUrl : centralScheduleServiceUrlList) {
            if (StringUtils.isNotBlank(centralScheduleServiceUrl)) {
              returnValue.put(centralScheduleServiceUrl, argumentsAsJsonArrayString);
            }
          }
        }
      }
    }

    return returnValue;
  }

  @Override
  public void runJob(TriggerMetadata triggerMetadata, Job job, Boolean manuallyScheduled) {
    long startHandleTime = System.currentTimeMillis();
    String appId = triggerMetadata.getAppId();
    String triggerId = triggerMetadata.getTriggerId();
    String jobId = job.getId();
    Throwable lastThrowable = null;
    try {
      // the start time for dispatching job
      long startTimeForJobDispatch = System.currentTimeMillis();

      Boolean onlyRunOnSingleProcess = triggerMetadata.getOnlyRunOnSingleProcess();
      Boolean concurrent = triggerMetadata.getConcurrent();
      boolean isNeedToEnsureNotConcurrent = KiraCommonUtils
          .isNeedToEnsureNotConcurrent(onlyRunOnSingleProcess, concurrent);
      if (isNeedToEnsureNotConcurrent) {
        boolean isKiraTimerTriggerBusinessRunning = KiraCommonUtils
            .isKiraTimerTriggerBusinessRunning(appId, triggerId);
        if (isKiraTimerTriggerBusinessRunning) {
          Date now = new Date();
          String resultData =
              "No need to deliver because onlyRunOnSingleProcess=true and concurrent=false and there is job for this trigger is running in some place now. Now="
                  + KiraCommonUtils.getDateAsStringToMsPrecision(now);
          Integer jobStatusIdWhenNoNeedToDeliver = JobStatusEnum.NO_NEED_TO_DELIVER.getId();
          this.updateJobStatusAndCreateHistoryIfNeeded(job.getId(), jobStatusIdWhenNoNeedToDeliver,
              resultData, now,
              KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenNoNeedToDeliver), true);
          return;
        }
      }

      String locationsToRunJob = triggerMetadata.getLocationsToRunJob();
      Boolean limitToSpecifiedLocations = triggerMetadata.getLimitToSpecifiedLocations();
      Boolean asynchronous = triggerMetadata.getAsynchronous();
      String targetAppId = triggerMetadata.getTargetAppId();
      String targetTriggerId = triggerMetadata.getTargetTriggerId();

      List<TriggerEnvironment> triggerEnvironmentList = new ArrayList<TriggerEnvironment>();
      boolean isInvokeTarget =
          StringUtils.isNotBlank(targetAppId) && StringUtils.isNotBlank(targetTriggerId);
      TriggerMetadata targetTriggerMetadata = null;
      if (isInvokeTarget) {
        targetTriggerMetadata = triggerMetadataService
            .getLatestAndAvailableTriggerMetadata(targetAppId, targetTriggerId, null);
      }

      Integer jobStatusIdWhenUnableToDeliverOrRun = KiraCommonUtils
          .getJobStatusIdWhenUnableToDeliverOrRun(asynchronous);
      if (isInvokeTarget && null == targetTriggerMetadata) {
        Date now = new Date();
        String resultData =
            "The specified target trigger can not be found now. targetAppId=" + targetAppId
                + " targetTriggerId=" + targetTriggerId;
        updateJobStatusAndCreateHistoryIfNeeded(job.getId(), jobStatusIdWhenUnableToDeliverOrRun,
            resultData, now,
            KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun),
            true);
        try {
          //Should not throw exception out if submit alarm throws exception.
          alarmCenter.alarmForJobIfNeeded(appId, jobId, jobStatusIdWhenUnableToDeliverOrRun);
        } catch (Exception ex) {
          logger.error(
              "Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId, ex);
        }
        logger.info("The specified target trigger can not be found now for triggerMetadata={}",
            KiraCommonUtils.toString(triggerMetadata));
      } else {
        if (isInvokeTarget) {
          triggerEnvironmentList = KiraManagerUtils
              .getTriggerEnvironmentList(targetAppId, targetTriggerId, false);
        } else {
          triggerEnvironmentList = KiraManagerUtils
              .getTriggerEnvironmentList(appId, triggerId, false);
        }
        if (!CollectionUtils.isEmpty(triggerEnvironmentList)) {
          List<String> runningAndAvailableCentralScheduleServiceUrlList = new ArrayList<String>();
          if (isInvokeTarget) {
            runningAndAvailableCentralScheduleServiceUrlList = KiraManagerUtils
                .getRunningAndAvailableCentralScheduleServiceUrlList(targetAppId, locationsToRunJob,
                    onlyRunOnSingleProcess, triggerEnvironmentList, limitToSpecifiedLocations);
          } else {
            runningAndAvailableCentralScheduleServiceUrlList = KiraManagerUtils
                .getRunningAndAvailableCentralScheduleServiceUrlList(appId, locationsToRunJob,
                    onlyRunOnSingleProcess, triggerEnvironmentList, limitToSpecifiedLocations);
          }
          if (!CollectionUtils.isEmpty(runningAndAvailableCentralScheduleServiceUrlList)) {
            Map<String, String> centralScheduleServiceUrlArgumentsAsJsonArrayStringMap = this
                .getCentralScheduleServiceUrlArgumentsAsJsonArrayStringMap(job,
                    runningAndAvailableCentralScheduleServiceUrlList);
            List<JobItem> jobItemList = jobItemService
                .createJobItemList(jobId, runningAndAvailableCentralScheduleServiceUrlList,
                    centralScheduleServiceUrlArgumentsAsJsonArrayStringMap, true);
            if (!CollectionUtils.isEmpty(jobItemList)) {
              if (!asynchronous && isNeedToEnsureNotConcurrent) {
                if (jobItemList.size() > 1) {
                  logger.warn(
                      "When isNeedToEnsureNotConcurrent=true and asynchronous=false, the jobItemList's size should be<=1. Metadata may be changed just now? jobItemList="
                          + KiraCommonUtils.toString(jobItemList));
                }
              }
              this.deliverOrRunJobItemList(startTimeForJobDispatch, triggerMetadata, job,
                  jobItemList, isInvokeTarget, targetTriggerMetadata);
            } else {
              Date now = new Date();
              String resultData = "Can not created jobitems.";
              updateJobStatusAndCreateHistoryIfNeeded(job.getId(),
                  jobStatusIdWhenUnableToDeliverOrRun, resultData, now, KiraCommonUtils
                      .getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun), true);
              try {
                //Should not throw exception out if submit alarm throws exception.
                alarmCenter.alarmForJobIfNeeded(appId, jobId, jobStatusIdWhenUnableToDeliverOrRun);
              } catch (Exception ex) {
                logger.error(
                    "Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId,
                    ex);
              }
              logger.warn(resultData
                      + "triggerMetadata={} and jobId={} and runningAndAvailableCentralScheduleServiceUrlList={}",
                  KiraCommonUtils.toString(triggerMetadata), jobId,
                  KiraCommonUtils.toString(runningAndAvailableCentralScheduleServiceUrlList));
            }
          } else {
            Date now = new Date();
            String resultData = "No running and available centralScheduleService found.";
            updateJobStatusAndCreateHistoryIfNeeded(job.getId(),
                jobStatusIdWhenUnableToDeliverOrRun, resultData, now,
                KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun),
                true);
            try {
              //Should not throw exception out if submit alarm throws exception.
              alarmCenter.alarmForJobIfNeeded(appId, jobId, jobStatusIdWhenUnableToDeliverOrRun);
            } catch (Exception ex) {
              logger.error(
                  "Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId,
                  ex);
            }
            logger
                .info(resultData + "triggerMetadata={}", KiraCommonUtils.toString(triggerMetadata));
          }
        } else {
          Date now = new Date();
          String resultData = "No environments found for the trigger.";
          updateJobStatusAndCreateHistoryIfNeeded(job.getId(), jobStatusIdWhenUnableToDeliverOrRun,
              resultData, now,
              KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun),
              true);
          try {
            //Should not throw exception out if submit alarm throws exception.
            alarmCenter.alarmForJobIfNeeded(appId, jobId, jobStatusIdWhenUnableToDeliverOrRun);
          } catch (Exception ex) {
            logger.error(
                "Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId,
                ex);
          }
          logger.info("No environments found for triggerMetadata={}",
              KiraCommonUtils.toString(triggerMetadata));
        }
      }
    } catch (Throwable t) {
      lastThrowable = t;
      String throwableDesc = ExceptionUtils.getFullStackTrace(t);
      String resultData = "Exception occurs. throwableDesc=" + throwableDesc;
      Job latestJob = this.select(job.getId());
      Integer finalJobStatusId = null;
      if (null != latestJob) {
        Integer latestJobStatusId = latestJob.getJobStatusId();
        boolean isBadAndFinalJobStatus = KiraCommonUtils.isBadAndFinalJobStatus(latestJobStatusId);
        finalJobStatusId = latestJobStatusId;
        if (!isBadAndFinalJobStatus) {
          //Do not override the exact cause.
          finalJobStatusId = JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId();
          updateJobStatusAndCreateHistoryIfNeeded(job.getId(), finalJobStatusId, resultData,
              new Date(), KiraCommonUtils.getCanBeUpdatedJobStatusIdList(finalJobStatusId), true);
        }
        try {
          //Always alarm
          //Should not throw exception out if submit alarm throws exception.
          alarmCenter.alarmForJobIfNeeded(appId, jobId, finalJobStatusId);
        } catch (Exception ex) {
          logger.error(
              "Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId, ex);
        }
      }
      logger.error("Error occurs when try to runJob job=" + KiraCommonUtils.toString(job)
          + " and triggerMetadata=" + KiraCommonUtils.toString(triggerMetadata)
          + " and finalJobStatusId=" + finalJobStatusId, t);
    } finally {
      if (!manuallyScheduled && null == lastThrowable) {
        //If arrive here and is not manuallyScheduled and lastThrowable is null. it means the scheduler scheme is healthy. For the zk operation will be blocked if zk is unavailable which will cause the timeoutException occurs.
        CreateAndRunJobForTimerTriggerMonitorTask.lastSuccessfullyCreateAndRunJobTime = new Date();
      }
      if (logger.isDebugEnabled()) {
        long handleCostTime = System.currentTimeMillis() - startHandleTime;
        logger.debug("It takes {} milliseconds for runJob. poolId={} and triggerId={} and jobId={}",
            handleCostTime, appId, triggerId, jobId);
      }
    }
  }

  private void deliverOrRunJobItemList(long startTimeForJobDispatch,
      TriggerMetadata triggerMetadata, Job job, List<JobItem> jobItemList, boolean isInvokeTarget,
      TriggerMetadata targetTriggerMetadata) throws Exception {
    String appId = triggerMetadata.getAppId();
    String triggerId = triggerMetadata.getTriggerId();
    String version = triggerMetadata.getVersion();
    String jobId = job.getId();
    String targetMethod = triggerMetadata.getTargetMethod();
    String targetMethodArgTypes = triggerMetadata.getTargetMethodArgTypes();
    Boolean asynchronous = triggerMetadata.getAsynchronous();
    String targetAppId = triggerMetadata.getTargetAppId();
    String targetTriggerId = triggerMetadata.getTargetTriggerId();
    Boolean onlyRunOnSingleProcess = triggerMetadata.getOnlyRunOnSingleProcess();
    Boolean concurrent = triggerMetadata.getConcurrent();

    Integer jobStatusIdWhenUnableToDeliverOrRun = KiraCommonUtils
        .getJobStatusIdWhenUnableToDeliverOrRun(asynchronous);
    List<Integer> canBeUpdatedJobStatusIdListWhenUnableToDeliverOrRun = KiraCommonUtils
        .getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun);
    Integer jobStatusIdWhenDeliveringOrRunning = KiraCommonUtils
        .getJobStatusIdWhenDeliveringOrRunning(asynchronous);
    List<Integer> canBeUpdatedJobStatusIdListWhenDeliveringOrRunning = KiraCommonUtils
        .getCanBeUpdatedJobStatusIdList(jobStatusIdWhenDeliveringOrRunning);

    JobItemRunTask jobItemRunTask = null;
    List<JobItemRunTask> jobItemRunTaskList = new ArrayList<JobItemRunTask>();
    for (JobItem jobItem : jobItemList) {
      if (null != jobItem) {
        String serviceUrl = null;
        ICentralScheduleService centralScheduleService = null;
        RemoteKiraClientEnvironment environment = null;
        try {
          serviceUrl = jobItem.getServiceUrl();
          centralScheduleService = KiraManagerUtils.getCentralScheduleService(serviceUrl);
          if (null != centralScheduleService) {
            updateJobItemStatus(jobItem.getId(), jobStatusIdWhenDeliveringOrRunning, null,
                canBeUpdatedJobStatusIdListWhenDeliveringOrRunning, true, false);
            environment = new RemoteKiraClientEnvironment(serviceUrl, centralScheduleService);
            JobItemRunRequest jobItemRunRequest = new JobItemRunRequest();
            String jobItemId = jobItem.getId();
            jobItemRunRequest.setJobItemId(jobItemId);
            jobItemRunRequest.setJobId(jobId);
            if (isInvokeTarget) {
              jobItemRunRequest.setAppId(targetAppId);
              jobItemRunRequest.setTriggerId(targetTriggerId);
              String versionOfTarget = targetTriggerMetadata.getVersion();
              jobItemRunRequest.setVersion(versionOfTarget);
              String targetMethodOfTarget = targetTriggerMetadata.getTargetMethod();
              jobItemRunRequest.setTargetMethod(targetMethodOfTarget);
              String targetMethodArgTypesOfTarget = targetTriggerMetadata.getTargetMethodArgTypes();
              jobItemRunRequest.setTargetMethodArgTypes(targetMethodArgTypesOfTarget);
              jobItemRunRequest.setSponsorPoolId(appId);
              jobItemRunRequest.setSponsorTriggerId(triggerId);
            } else {
              jobItemRunRequest.setAppId(appId);
              jobItemRunRequest.setTriggerId(triggerId);
              jobItemRunRequest.setVersion(version);
              jobItemRunRequest.setTargetMethod(targetMethod);
              jobItemRunRequest.setTargetMethodArgTypes(targetMethodArgTypes);
            }
            String argumentsAsJsonArrayString = job.getArgumentsAsJsonArrayString();
            jobItemRunRequest.setArgumentsAsJsonArrayString(argumentsAsJsonArrayString);
            jobItemRunRequest.setConcurrent(concurrent);
            jobItemRunRequest.setAsynchronous(asynchronous);
            jobItemRunRequest.setOnlyRunOnSingleProcess(onlyRunOnSingleProcess);

            JobItemRunContextData jobItemRunContextData = new JobItemRunContextData(
                jobItemRunRequest, environment);
            jobItemRunTask = new JobItemRunTask(jobItemRunContextData, new KiraManagerJobHandler());
            jobItemRunTaskList.add(jobItemRunTask);
          } else {
            String resultData = "Can not get centralScheduleService.";
            updateJobItemStatus(jobItem.getId(), jobStatusIdWhenUnableToDeliverOrRun, resultData,
                canBeUpdatedJobStatusIdListWhenUnableToDeliverOrRun, true, false);
            try {
              //Should not throw exception out if submit alarm throws exception.
              alarmCenter.alarmForJobItemIfNeeded(appId, jobItem.getId(),
                  jobStatusIdWhenUnableToDeliverOrRun);
            } catch (Exception ex) {
              logger.error(
                  "Error occurs when alarmForJobItemIfNeeded. appId=" + appId + " and jobItem="
                      + jobItem, ex);
            }
            logger.error(resultData + "serviceUrl={},jobItem={}", serviceUrl,
                KiraCommonUtils.toString(jobItem));
          }
        } catch (Exception e) {
          String exceptionDesc = ExceptionUtils.getFullStackTrace(e);
          String resultData = "Exception occurs. exceptionDesc=" + exceptionDesc;
          updateJobItemStatus(jobItem.getId(), jobStatusIdWhenUnableToDeliverOrRun, resultData,
              canBeUpdatedJobStatusIdListWhenUnableToDeliverOrRun, true, false);
          try {
            //Should not throw exception out if submit alarm throws exception.
            this.alarmCenter.alarmForJobItemIfPoolNeeded(appId, jobItem.getId());
          } catch (Exception ex) {
            logger.error("Error occurs when alarmForJobItemIfPoolNeeded. jobItem=" + KiraCommonUtils
                .toString(jobItem), ex);
          }

          logger
              .error("Error occurs when try to handle jobItem=" + KiraCommonUtils.toString(jobItem),
                  e);
        }
      }
    }
    if (!CollectionUtils.isEmpty(jobItemRunTaskList)) {
      updateJobStatusAndCreateHistoryIfNeeded(job.getId(), jobStatusIdWhenDeliveringOrRunning, null,
          new Date(),
          KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenDeliveringOrRunning), true);
      initJobTimeoutTrackerDataIfNeeded(triggerMetadata, job);

      // If jobDispatchTimeoutEnabled is true and the job dispatching time has exceeded the specified threshold, don't run any job tasks anymore.
      if (triggerMetadata.getJobDispatchTimeoutEnabled()
          && triggerMetadata.getJobDispatchTimeout() != null
          && (System.currentTimeMillis() - startTimeForJobDispatch) > triggerMetadata
          .getJobDispatchTimeout()) {
        logger.warn(
            "The job dispatch time for job {} has exceeded the specified threshold, no task will be run.",
            KiraCommonUtils.toString(job.getId()));
        throw new Exception("The job dispatch time for job " + job.getId()
            + " has exceeded the specified threshold, no task will be run.");
      }

      this.handleJobItemRunTaskList(triggerMetadata, job, jobItemRunTaskList);
    } else {
      Date now = new Date();
      String resultData = "Can not created jobItemRunTaskList.";
      updateJobStatusAndCreateHistoryIfNeeded(job.getId(), jobStatusIdWhenUnableToDeliverOrRun,
          resultData, now,
          KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdWhenUnableToDeliverOrRun),
          true);
      try {
        //Should not throw exception out if submit alarm throws exception.
        alarmCenter.alarmForJobIfNeeded(appId, jobId, jobStatusIdWhenUnableToDeliverOrRun);
      } catch (Exception ex) {
        logger
            .error("Error occurs when alarmForJobIfNeeded. appId=" + appId + " and jobId=" + jobId,
                ex);
      }
      logger.warn(resultData + "triggerMetadata={} and jobItemList={}",
          KiraCommonUtils.toString(triggerMetadata), KiraCommonUtils.toString(jobItemList));
    }
  }

  private void initJobTimeoutTrackerDataIfNeeded(TriggerMetadata triggerMetadata, Job job) {
    Long runTimeThreshold = triggerMetadata.getRunTimeThreshold();
    if (null != runTimeThreshold) {
      if (runTimeThreshold.longValue() > 0) {
        String appId = triggerMetadata.getAppId();
        boolean isPoolNeedAlarmAndAlarmReceiverSet = kiraClientMetadataService
            .isPoolNeedAlarmAndAlarmReceiverSet(appId);
        if (isPoolNeedAlarmAndAlarmReceiverSet) {
          String uuIDWithTimePrefix = KiraCommonUtils.getUUIDWithTimePrefix();
          Date createTime = new Date();
          String jobId = job.getId();
          Date expectTimeoutTime = KiraManagerUtils
              .getDateAfterAddMilliseconds(createTime, runTimeThreshold.longValue());

          JobTimeoutTracker jobTimeoutTracker = new JobTimeoutTracker();
          jobTimeoutTracker.setId(uuIDWithTimePrefix);
          jobTimeoutTracker.setCreateTime(createTime);
          jobTimeoutTracker.setJobId(jobId);
          jobTimeoutTracker.setRumTimeThreshold(runTimeThreshold);
          jobTimeoutTracker.setExpectTimeoutTime(expectTimeoutTime);
          jobTimeoutTracker.setState(JobTimeoutTrackerStateEnum.INITIAL.getState());

          jobTimeoutTrackerService.insert(jobTimeoutTracker);
        } else {
          logger.info(
              "No need to init jobTimeoutTrackerData although runTimeThreshold is set. isPoolNeedAlarmAndAlarmReceiverSet={} and triggerMetadata={} and job={}",
              isPoolNeedAlarmAndAlarmReceiverSet, KiraCommonUtils.toString(triggerMetadata),
              KiraCommonUtils.toString(job));
        }
      } else {
        logger.info(
            "runTimeThreshold is not larger than 0 . So do not init jobTimeoutTrackerData. triggerMetadata={} and job={}",
            KiraCommonUtils.toString(triggerMetadata), KiraCommonUtils.toString(job));
      }
    }
  }

  private void updateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData,
      List<Integer> jobStatusIdList, boolean createJobItemHistory, boolean updateJobIfNeeded) {
    jobItemService.updateJobItemStatus(jobItemId, jobStatusId, resultData, jobStatusIdList,
        createJobItemHistory, updateJobIfNeeded);
  }

  private void handleJobItemRunTaskList(TriggerMetadata triggerMetadata, Job job,
      List<JobItemRunTask> jobItemRunTaskList) {
    if (!CollectionUtils.isEmpty(jobItemRunTaskList)) {
      String poolId = triggerMetadata.getAppId();
      Boolean asynchronous = triggerMetadata.getAsynchronous();
      ExecutorService executorService = null;
      int successCount = 0;
      int failedCount = 0;
      int partialSuccessCount = 0;
      int noNeedToRunBusinessMethodCount = 0;
      Exception outerExceptionOccured = null;
      try {
        String jobId = job.getId();
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
            "kira-manager-jobItemRunTask-" + jobId + "-");
        threadFactory.setDaemon(false);
        executorService = Executors.newFixedThreadPool(jobItemRunTaskList.size(), threadFactory);
        //CompletionService<JobItemHandleResult> completionService = new ExecutorCompletionService<JobItemHandleResult>(executorService);
        List<Future<HandleResult>> futures = executorService
            .invokeAll(jobItemRunTaskList, KiraServerConstants.JOB_ITEM_HANDLE_TIMEOUT_SECOND,
                TimeUnit.SECONDS);
        Iterator<JobItemRunTask> jobItemRunTaskIterator = jobItemRunTaskList.iterator();
        JobItemRunTask jobItemRunTask = null;
        for (Future<HandleResult> future : futures) {
          jobItemRunTask = jobItemRunTaskIterator.next();
          JobItemRunRequest jobItemRunRequest = jobItemRunTask.getJobItemRunContextData()
              .getJobItemRunRequest();
          String jobItemId = jobItemRunRequest.getJobItemId();
          Exception innerExceptionOccured = null;
          String resultCodeForJobItem = null;
          String resultDataForJobItem = null;
          try {
            HandleResult jobItemRunResult = future.get();
            if (null != jobItemRunResult) {
              resultCodeForJobItem = jobItemRunResult.getResultCode();
              resultDataForJobItem = jobItemRunResult.getResultData();
            } else {
              resultCodeForJobItem = KiraServerConstants.RESULT_CODE_FAILED;
              resultDataForJobItem =
                  "jobItemHandleResult is null. jobItemRunRequest=" + KiraCommonUtils
                      .toString(jobItemRunRequest);
            }
          } catch (InterruptedException e) {
            innerExceptionOccured = e;
            logger.error("InterruptedException occurs for jobItemRunTask=" + KiraCommonUtils
                .toString(jobItemRunTask), e);
            Thread.currentThread().interrupt();
          } catch (Exception e) {
            innerExceptionOccured = e;
            logger.error(
                "Error occurs for jobItemRunTask=" + KiraCommonUtils.toString(jobItemRunTask), e);
          } finally {
            if (null != innerExceptionOccured) {
              String exceptionDesc = ExceptionUtils.getFullStackTrace(innerExceptionOccured);
              resultDataForJobItem =
                  "Error occurs on jobItemRunTask. exceptionDesc=" + exceptionDesc;
              resultCodeForJobItem = KiraServerConstants.RESULT_CODE_FAILED;
            }

            Integer jobStatusIdForJobItem = null;

            if (asynchronous) {
              if (KiraServerConstants.RESULT_CODE_SUCCESS.equals(resultCodeForJobItem)) {
                successCount++;
                jobStatusIdForJobItem = JobStatusEnum.DELIVERY_SUCCESS.getId();
              } else if (KiraServerConstants.RESULT_CODE_FAILED.equals(resultCodeForJobItem)) {
                failedCount++;
                jobStatusIdForJobItem = JobStatusEnum.DELIVERY_FAILED.getId();
              } else if (KiraServerConstants.RESULT_CODE_PARTIAL_SUCCESS
                  .equals(resultCodeForJobItem)) {
                partialSuccessCount++;
                jobStatusIdForJobItem = JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId();
              }
            } else {
              if (KiraServerConstants.RESULT_CODE_SUCCESS.equals(resultCodeForJobItem)) {
                successCount++;
                jobStatusIdForJobItem = JobStatusEnum.RUN_SUCCESS.getId();
              } else if (KiraServerConstants.RESULT_CODE_FAILED.equals(resultCodeForJobItem)) {
                failedCount++;
                jobStatusIdForJobItem = JobStatusEnum.RUN_FAILED.getId();
              } else if (KiraServerConstants.RESULT_CODE_PARTIAL_SUCCESS
                  .equals(resultCodeForJobItem)) {
                partialSuccessCount++;
                jobStatusIdForJobItem = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
              } else if (KiraServerConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD
                  .equals(resultCodeForJobItem)) {
                noNeedToRunBusinessMethodCount++;
                jobStatusIdForJobItem = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
              }
            }

            updateJobItemStatus(jobItemId, jobStatusIdForJobItem, resultDataForJobItem,
                KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdForJobItem), true, false);
            try {
              //Should not throw exception out if submit alarm throws exception.
              alarmCenter.alarmForJobItemIfNeeded(poolId, jobItemId, jobStatusIdForJobItem);
            } catch (Exception ex) {
              logger.error(
                  "Error occurs when alarmForJobItemIfNeeded. poolId=" + poolId + " and jobItemId="
                      + jobItemId, ex);
            }
          }
        }
      } catch (Exception e) {
        outerExceptionOccured = e;
        logger.error("Error occurs for handleJobItemRunTaskList. triggerMetadata=" + KiraCommonUtils
            .toString(triggerMetadata) + " and job=" + KiraCommonUtils.toString(job)
            + " and jobItemRunTaskList=" + KiraCommonUtils.toString(jobItemRunTaskList), e);
      } finally {
        try {
          if (null != executorService) {
            executorService.shutdown();
          }

          Integer jobStatusIdForJob = null;
          String resultDataForJob = null;
          if (failedCount > 0) {
            if (successCount > 0 || partialSuccessCount > 0) {
              //部分成功
              jobStatusIdForJob = KiraCommonUtils
                  .getJobStatusIdWhenDeliverPartialSuccessOrRunPartialSuccess(asynchronous);
            } else {
              //失败
              jobStatusIdForJob = KiraCommonUtils
                  .getJobStatusIdWhenDeliverFailedOrRunFailed(asynchronous);
            }
          } else if (successCount > 0) {
            if (partialSuccessCount > 0) {
              //部分成功
              jobStatusIdForJob = KiraCommonUtils
                  .getJobStatusIdWhenDeliverPartialSuccessOrRunPartialSuccess(asynchronous);
            } else {
              //成功
              jobStatusIdForJob = KiraCommonUtils
                  .getJobStatusIdWhenDeliverSuccessOrRunSuccess(asynchronous);
            }
          } else if (partialSuccessCount > 0) {
            //部分成功
            jobStatusIdForJob = KiraCommonUtils
                .getJobStatusIdWhenDeliverPartialSuccessOrRunPartialSuccess(asynchronous);
          } else if (noNeedToRunBusinessMethodCount > 0 && (noNeedToRunBusinessMethodCount
              == jobItemRunTaskList.size())) {
            jobStatusIdForJob = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
          }

          if (null != outerExceptionOccured) {
            String exceptionDesc = ExceptionUtils.getFullStackTrace(outerExceptionOccured);
            resultDataForJob = "Error occurs.exceptionDesc=" + exceptionDesc;
          }
          String jobId = job.getId();
          updateJobStatusAndCreateHistoryIfNeeded(jobId, jobStatusIdForJob, resultDataForJob,
              new Date(), KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusIdForJob), true);
          if (null != outerExceptionOccured) {
            try {
              //Should not throw exception out if submit alarm throws exception.
              alarmCenter.alarmForJobIfNeeded(poolId, jobId, jobStatusIdForJob);
            } catch (Exception ex) {
              logger.error(
                  "Error occurs when alarmForJobIfNeeded. poolId=" + poolId + " and jobId=" + jobId,
                  ex);
            }
          }
        } catch (Exception e) {
          String exceptionDesc = ExceptionUtils.getFullStackTrace(e);
          logger.error("Final error occurs exceptionDesc=" + exceptionDesc);
        }
      }
    }
  }

  @Override
  public ManuallyReRunJobResult manuallyReRunJob(String jobId, String userName) {
    Date triggerTime = new Date();
    String resultCode = null;
    String resultData = null;
    String newJobId = null;
    try {
      if (StringUtils.isBlank(jobId)) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "jobId should not be blank.";
      } else {
        final TriggerMetadata triggerMetadata = this.triggerMetadataService
            .getLatestAndAvailableTriggerMetadataByJobId(jobId);
        if (null != triggerMetadata) {
          Job newJob = this.triggerMetadataService
              .createAndRunJobByTriggerMetadata(triggerMetadata, Boolean.TRUE, userName,
                  triggerTime);
          if (null != newJob) {
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
            newJobId = newJob.getId();
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData = "Can not create job.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "Can not find latest and available triggerMetadata by jobId=" + jobId;
        }
      }
    } catch (Exception e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      String exceptionDesc = ExceptionUtils.getFullStackTrace(e);
      resultData = "Error occurs. exceptionDesc=" + exceptionDesc;
      logger
          .error("Error occurs for manuallyReRunJob. jobId=" + jobId + " and userName=" + userName,
              e);
    }

    ManuallyReRunJobResult manuallyReRunJobResult = new ManuallyReRunJobResult(jobId, resultCode,
        resultData, newJobId);
    return manuallyReRunJobResult;
  }

  @Override
  public void archiveForJobData(String newTableNameSuffix, Date startTime, Date endTime) {
    if (KiraManagerUtils.isNeedArchiveJobRuntimeData()) {
      int createTableCount = this.jobDao.createArchiveTableForJobTableIfNeeded(newTableNameSuffix);
      logger.info(
          "createArchiveTableForJobTableIfNeeded return createTableCount={} by newTableNameSuffix={}",
          createTableCount, newTableNameSuffix);
      Object insertResult = this.jobDao
          .insertDataToJobArchiveTable(newTableNameSuffix, startTime, endTime);
      logger.info(
          "insertDataToJobArchiveTable return insertResult={} by newTableNameSuffix={} startTime={} endTime={}",
          KiraCommonUtils.toString(insertResult), newTableNameSuffix,
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    deleteJobData(startTime, endTime);
  }

  @Override
  public int deleteJobData(Date startTime, Date endTime) {
    int deleteCount = this.jobDao.deleteJobData(startTime, endTime);
    logger.info("deleteJobData return deleteCount={} by startTime={} endTime={}", deleteCount,
        KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    return deleteCount;
  }

  @Override
  public Date getDateOfOldestJob() {
    return this.jobDao.getDateOfOldestJob();
  }

  @Override
  public void handleJobRuntimeData(ExecutorService executorService, Date startTime, Date endTime)
      throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("start handleJobRuntimeData with startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    Date dateOfOldestData = this.getDateOfOldestJob();
    if (logger.isDebugEnabled()) {
      logger.debug("getDateOfOldestJob={}", KiraCommonUtils.getDateAsString(dateOfOldestData));
    }

    if (null != dateOfOldestData) {
      List<TimeInterval> splittedTimeIntervalList = KiraManagerUtils
          .getSplittedTimeIntervalListToHandleJobRuntimeData(startTime, endTime, dateOfOldestData);

      List<Callable<Void>> taskList = new ArrayList<Callable<Void>>();
      for (final TimeInterval oneTimeInterval : splittedTimeIntervalList) {
        taskList.add(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            // [startTime,endTime)
            Date startTimeOfInterval = oneTimeInterval.getStartTime();
            Date endTimeOfInterval = oneTimeInterval.getEndTime();
            String newTableNameSuffix = KiraManagerUtils
                .getNewTableNameSuffixForJobRuntimeData(startTimeOfInterval);
            archiveForJobData(newTableNameSuffix, startTimeOfInterval, endTimeOfInterval);
            return null;
          }
        });
      }
      if (CollectionUtils.isEmpty(taskList)) {
        if (logger.isDebugEnabled()) {
          logger.debug("taskList is empty. So do nothing for handleJobRuntimeData");
        }
      } else {
        KiraManagerUtils.runTasksOneByOne(taskList,
            KiraManagerUtils.getSleepTimeBeforeRunNextTaskInMilliseconds(), true, executorService,
            KiraManagerUtils.getTimeOutPerTaskInMilliseconds());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("end handleJobRuntimeData with startTime={} and endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      }
    } else {
      logger.info("getDateOfOldestJob return null. So handleJobRuntimeData do nothing.");
    }
  }

  @Override
  public boolean isJobCompleted(String jobId, Integer jobStatusId) {
    boolean returnValue = false;

    if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusId)
        || JobStatusEnum.RUN_FAILED.getId().equals(jobStatusId)
        || JobStatusEnum.UNABLE_TO_RUN.getId().equals(jobStatusId)
        || JobStatusEnum.DELIVERY_FAILED.getId().equals(jobStatusId)
        || JobStatusEnum.UNABLE_TO_DELIVER.getId().equals(jobStatusId)
        || JobStatusEnum.NO_NEED_TO_DELIVER.getId().equals(jobStatusId)
        || JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId().equals(jobStatusId)
        || JobStatusEnum.EXCEPTION_CAUGHT_DURING_SCHEDULE.getId().equals(jobStatusId)) {
      returnValue = true;
    } else if (JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusId)) {
      if (StringUtils.isNotBlank(jobId)) {
        JobItemCriteria jobItemCriteria = new JobItemCriteria();
        jobItemCriteria.setJobId(jobId);
        List<JobItem> jobItemList = this.jobItemService.list(jobItemCriteria);
        if (!CollectionUtils.isEmpty(jobItemList)) {
          boolean isJobItemAllCompleted = true;
          for (JobItem jobItem : jobItemList) {
            Integer jobStatusIdOfJobItem = jobItem.getJobStatusId();
            boolean isJobItemCompleted = KiraCommonUtils.isJobItemCompleted(jobStatusIdOfJobItem);
            if (!isJobItemCompleted) {
              isJobItemAllCompleted = false;
              break;
            }
          }
          returnValue = isJobItemAllCompleted;
        } else {
          //regard it as complete if no jobItems.
          returnValue = true;
        }
      } else {
        logger.error("jobId should not be blank for isJobCompleted. jobStatusId={}", jobStatusId);
      }
    }

    return returnValue;
  }

}
