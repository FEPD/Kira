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
package com.yihaodian.architecture.kira.client.internal.impl;

import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.kira.client.internal.bean.Job;
import com.yihaodian.architecture.kira.client.internal.bean.JobItem;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientUtils;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.common.EnvironmentStatusEnum;
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
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class CentralScheduleService implements ICentralScheduleService {

  private static final CentralScheduleService centralScheduleService = new CentralScheduleService();
  private static Logger logger = LoggerFactory.getLogger(CentralScheduleService.class);

  private CentralScheduleService() {
    // TODO Auto-generated constructor stub
  }

  public static CentralScheduleService getCentralScheduleService() {
    return centralScheduleService;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public HandleResult runJobItem(JobItemRunRequest jobItemRunRequest) {
    HandleResult handleResult = null;
    if (KiraClientDataCenter.isWorkWithoutKira()) {
      handleResult = new HandleResult(KiraClientConstants.RESULT_CODE_FAILED,
          KiraClientConstants.ALLSCHEDULEDLOCALLY_HINT);
    } else {
      String resultCode = null;
      String resultData = null;
      Exception outerExceptionCaught = null;
      Job job = null;
      try {
        job = createJob(jobItemRunRequest);
        saveJobIfNeeded(job);
        handleJob(job);
      } catch (Exception e) {
        outerExceptionCaught = e;
        logger.error("Error occurs on runJobItem. jobItemRunRequest=" + KiraCommonUtils
            .toString(jobItemRunRequest), e);
      } finally {
        if (null != job) {
          Integer jobStatusId = job.getJobStatusId();
          Boolean asynchronous = job.getJobItemRunRequest().getAsynchronous();
          if (asynchronous) {
            if (JobStatusEnum.DELIVERY_SUCCESS.getId().equals(jobStatusId)
                || JobStatusEnum.RUNNING.getId().equals(jobStatusId)
                || JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_SUCCESS;
            } else if (JobStatusEnum.DELIVERY_PARTIAL_SUCCESS.getId().equals(jobStatusId)
                || JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_PARTIAL_SUCCESS;
            } else if (JobStatusEnum.DELIVERY_FAILED.getId().equals(jobStatusId)
                || JobStatusEnum.RUN_FAILED.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            } else if (JobStatusEnum.UNABLE_TO_DELIVER.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            }
          } else {
            if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_SUCCESS;
            } else if (JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_PARTIAL_SUCCESS;
            } else if (JobStatusEnum.RUN_FAILED.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            } else if (JobStatusEnum.UNABLE_TO_RUN.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            } else if (JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId().equals(jobStatusId)) {
              resultCode = KiraClientConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD;
            }
          }
          resultData = job.getResultData();
        } else {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          resultData = "Can not create job at kira-client side.";
        }

        if (null != outerExceptionCaught) {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          String outerExceptionDesc = ExceptionUtils.getFullStackTrace(outerExceptionCaught);
          if (null == resultData) {
            resultData = "";
          }
          resultData += " Exception occurs on runJobItem. outerExceptionDesc=" + outerExceptionDesc;
        }
        handleResult = new HandleResult(resultCode, resultData);
      }
    }

    return handleResult;
  }

  private void saveJobIfNeeded(Job job) {
    JobItemRunRequest jobItemRunRequest = job.getJobItemRunRequest();
    Boolean asynchronous = jobItemRunRequest.getAsynchronous();
    List<JobItem> jobItemList = job.getJobItemList();
    boolean isNeedToSaveJob = false;
    if (asynchronous && !CollectionUtils.isEmpty(jobItemList)) {
      isNeedToSaveJob = true;
    }
    if (isNeedToSaveJob) {
      KiraClientDataCenter.addJob(job);
    }
  }

  public Job createJob(JobItemRunRequest jobItemRunRequest) {
    String id = jobItemRunRequest.getJobItemId();
    Job job = new Job(id, JobStatusEnum.CREATED.getId(), null, jobItemRunRequest, null);
    List<JobItem> jobItemList = createJobItemList(job);
    job.setJobItemList(jobItemList);
    return job;
  }

  public List<JobItem> createJobItemList(Job job) {
    List<JobItem> jobItemList = new ArrayList<JobItem>();
    JobItemRunRequest jobItemRunRequest = job.getJobItemRunRequest();
    String appId = jobItemRunRequest.getAppId();
    String triggerId = jobItemRunRequest.getTriggerId();
    TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
    ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = KiraClientDataCenter
        .getEnvironmentTriggerContextDataMap(triggerIdentity);
    if (null != environmentTriggerContextDataMap) {
      IEnvironment environment = null;
      Boolean onlyRunOnSingleProcess = jobItemRunRequest.getOnlyRunOnSingleProcess();
      IEnvironment firstQualifiedEnvironment = null;

      JobItem jobItem = null;
      String uuid = null;
      for (Entry<IEnvironment, TriggerRegisterContextData> entry : environmentTriggerContextDataMap
          .entrySet()) {
        environment = entry.getKey();
        if (onlyRunOnSingleProcess) {
          //Prefer the running environment if onlyRunOnSingleProcess=true
          if (EnvironmentStatusEnum.RUNNING.equals(environment.getEnvironmentStatus())) {
            uuid = KiraCommonUtils.getUUID();
            jobItem = new JobItem(uuid, JobStatusEnum.CREATED.getId(), null, job.getId(),
                environment);
            jobItemList.add(jobItem);
            break;
          } else if (EnvironmentStatusEnum.SHUTDOWN_BUT_CAN_BE_START
              .equals(environment.getEnvironmentStatus())) {
            firstQualifiedEnvironment = environment;
          }
        } else {
          uuid = KiraCommonUtils.getUUID();
          jobItem = new JobItem(uuid, JobStatusEnum.CREATED.getId(), null, job.getId(),
              environment);
          jobItemList.add(jobItem);
        }
      }
      if (onlyRunOnSingleProcess && jobItemList.isEmpty() && null != firstQualifiedEnvironment) {
        uuid = KiraCommonUtils.getUUID();
        jobItem = new JobItem(uuid, JobStatusEnum.CREATED.getId(), null, job.getId(),
            firstQualifiedEnvironment);
        jobItemList.add(jobItem);
      }
    }
    return jobItemList;
  }

  public void handleJob(Job job) {
    JobItemRunRequest jobItemRunRequest = job.getJobItemRunRequest();
    Boolean asynchronous = jobItemRunRequest.getAsynchronous();
    Integer jobStatusId = null;
    String resultData = null;

    Exception outerExceptionOccured = null;
    try {
      List<JobItem> jobItemList = job.getJobItemList();
      if (!CollectionUtils.isEmpty(jobItemList)) {
        List<JobItemRunTask> jobItemRunTaskList = getJobItemRunTaskList(jobItemRunRequest,
            jobItemList);
        if (!CollectionUtils.isEmpty(jobItemRunTaskList)) {
          jobStatusId = KiraCommonUtils.getJobStatusIdWhenDeliveringOrRunning(asynchronous);
          if (!CollectionUtils.isEmpty(jobItemRunTaskList)) {
            String jobItemId = null;
            Integer jobStatusIdForJobItem = KiraCommonUtils
                .getJobStatusIdWhenDeliveringOrRunning(asynchronous);
            for (JobItemRunTask jobItemRunTask : jobItemRunTaskList) {
              jobItemId = jobItemRunTask.getJobItemRunContextData().getJobItemRunRequest()
                  .getJobItemId();
              if (asynchronous) {
                KiraClientUtils
                    .updateJobItem(KiraClientConstants.JOB_PHASE_DELIVERING, job, jobItemId,
                        new HandleResult(KiraClientConstants.RESULT_CODE_SUCCESS, null));
              } else {
                KiraClientUtils.updateJobItemStatus(job, jobItemId, jobStatusIdForJobItem, null);
              }
            }
          }

          HandleResult handleResult = handleJobItemRunTaskList(job, jobItemRunTaskList);
          if (null != handleResult) {
            String resultCode = handleResult.getResultCode();
            if (KiraClientConstants.RESULT_CODE_SUCCESS.equals(resultCode)) {
              jobStatusId = KiraCommonUtils
                  .getJobStatusIdWhenDeliverSuccessOrRunSuccess(asynchronous);
            } else if (KiraClientConstants.RESULT_CODE_PARTIAL_SUCCESS.equals(resultCode)) {
              jobStatusId = KiraCommonUtils
                  .getJobStatusIdWhenDeliverPartialSuccessOrRunPartialSuccess(asynchronous);
            } else if (KiraClientConstants.RESULT_CODE_FAILED.equals(resultCode)) {
              jobStatusId = KiraCommonUtils
                  .getJobStatusIdWhenDeliverFailedOrRunFailed(asynchronous);
            } else if (KiraClientConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD
                .equals(resultCode)) {
              jobStatusId = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
            }
            resultData = handleResult.getResultData();
          }
        } else {
          jobStatusId = KiraCommonUtils.getJobStatusIdWhenUnableToDeliverOrRun(asynchronous);
          resultData = "Can not create jobItemRunTaskList for jobItemRunRequest=" + KiraCommonUtils
              .toString(jobItemRunRequest) + " and jobItemList=" + KiraCommonUtils
              .toString(jobItemList);
        }
      } else {
        jobStatusId = KiraCommonUtils.getJobStatusIdWhenUnableToDeliverOrRun(asynchronous);
        resultData =
            "Can not create jobItemList. No environment available. May no such trigger exist at client side? jobItemRunRequest="
                + KiraCommonUtils.toString(jobItemRunRequest);
      }
    } catch (Exception e) {
      outerExceptionOccured = e;
      logger.error("Error occurs on handleJob.jobItemRunRequest=" + KiraCommonUtils
          .toString(jobItemRunRequest), e);
    } finally {
      if (null != outerExceptionOccured) {
        jobStatusId = KiraCommonUtils.getJobStatusIdWhenUnableToDeliverOrRun(asynchronous);
        String outerExceptionDesc = ExceptionUtils.getFullStackTrace(outerExceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs. outerExceptionDesc=" + outerExceptionDesc;
      }

      job.setJobStatusId(jobStatusId);
      job.setResultData(resultData);
      job.setLastUpdateTime(new Date());
    }
  }

  private HandleResult handleJobItemRunTaskList(Job job, List<JobItemRunTask> jobItemRunTaskList) {
    HandleResult outerHandleResult = null;
    ExecutorService executorService = null;
    int successCount = 0;
    int failedCount = 0;
    int partialSuccessCount = 0;
    int noNeedToRunBusinessMethodCount = 0;
    Exception outerExceptionOccured = null;
    StringBuilder aggregatedResultData = new StringBuilder();
    boolean isNeedAggregateResultData = false;
    try {
      String jobId = job.getId();
      Boolean asynchronous = job.getJobItemRunRequest().getAsynchronous();
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX + "jobItemRunTask-" + jobId + "-");
      threadFactory.setDaemon(false);
      executorService = Executors.newFixedThreadPool(jobItemRunTaskList.size(), threadFactory);
      //CompletionService<JobItemHandleResult> completionService = new ExecutorCompletionService<JobItemHandleResult>(executorService);
      List<Future<HandleResult>> futures = executorService
          .invokeAll(jobItemRunTaskList, KiraClientConstants.JOB_ITEM_HANDLE_TIMEOUT_SECOND,
              TimeUnit.SECONDS);
      Iterator<JobItemRunTask> jobItemRunTaskIterator = jobItemRunTaskList.iterator();
      JobItemRunTask jobItemRunTask = null;
      int number = 1;
      if (futures.size() > 1) {
        isNeedAggregateResultData = true;
      }
      for (Future<HandleResult> future : futures) {
        jobItemRunTask = jobItemRunTaskIterator.next();
        JobItemRunRequest jobItemRunRequest = jobItemRunTask.getJobItemRunContextData()
            .getJobItemRunRequest();
        String jobItemId = jobItemRunRequest.getJobItemId();
        Exception innerExceptionOccured = null;
        HandleResult jobItemHandleResult = null;
        String resultCodeForJobItem = null;
        String resultDataForJobItem = null;
        try {
          jobItemHandleResult = future.get();
          if (null != jobItemHandleResult) {
            resultCodeForJobItem = jobItemHandleResult.getResultCode();
            resultDataForJobItem = jobItemHandleResult.getResultData();
          } else {
            resultCodeForJobItem = KiraClientConstants.RESULT_CODE_FAILED;
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
          logger
              .error("Error occurs for jobItemRunTask=" + KiraCommonUtils.toString(jobItemRunTask),
                  e);
        } finally {
          if (null != innerExceptionOccured) {
            String exceptionDesc = ExceptionUtils.getFullStackTrace(innerExceptionOccured);
            resultDataForJobItem = "Error occurs on jobItemRunTask. exceptionDesc=" + exceptionDesc;
            resultCodeForJobItem = KiraClientConstants.RESULT_CODE_FAILED;
          }

          Integer jobStatusIdForJobItem = null;
          if (KiraClientConstants.RESULT_CODE_SUCCESS.equals(resultCodeForJobItem)) {
            successCount++;
            jobStatusIdForJobItem = JobStatusEnum.RUN_SUCCESS.getId();
          } else if (KiraClientConstants.RESULT_CODE_FAILED.equals(resultCodeForJobItem)) {
            failedCount++;
            jobStatusIdForJobItem = JobStatusEnum.RUN_FAILED.getId();
          } else if (KiraClientConstants.RESULT_CODE_PARTIAL_SUCCESS.equals(resultCodeForJobItem)) {
            partialSuccessCount++;
            jobStatusIdForJobItem = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
          } else if (KiraClientConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD
              .equals(resultCodeForJobItem)) {
            noNeedToRunBusinessMethodCount++;
            jobStatusIdForJobItem = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
          }

          if (asynchronous) {
            KiraClientUtils
                .updateJobItem(KiraClientConstants.JOB_PHASE_DELIVER_COMPLETE, job, jobItemId,
                    new HandleResult(resultCodeForJobItem, resultDataForJobItem));
          } else {
            KiraClientUtils
                .updateJobItemStatus(job, jobItemId, jobStatusIdForJobItem, resultDataForJobItem);
          }

          if (isNeedAggregateResultData) {
            aggregatedResultData.append((number + ". "))
                .append(resultDataForJobItem == null ? "" : resultDataForJobItem);
            aggregatedResultData.append(KiraClientConstants.LINE_SEPARATOR);
            number++;
          } else {
            aggregatedResultData.append(resultDataForJobItem == null ? "" : resultDataForJobItem);
          }


        }
      }
    } catch (Exception e) {
      outerExceptionOccured = e;
      logger.error("Error occurs on handlejobItemRunTaskList. job=" + KiraCommonUtils.toString(job)
          + " and jobItemRunTaskList=" + KiraCommonUtils.toString(jobItemRunTaskList), e);
    } finally {
      if (null != executorService) {
        executorService.shutdown();
      }

      String resultCode = KiraCommonUtils
          .getResultCode(successCount, failedCount, partialSuccessCount,
              noNeedToRunBusinessMethodCount, jobItemRunTaskList.size());
      String resultData = aggregatedResultData.toString();

      if (null != outerExceptionOccured) {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(outerExceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Error occurs on handleJobItemRunTaskList. exceptionDesc=" + exceptionDesc;
      }

      outerHandleResult = new HandleResult(resultCode, resultData);
    }

    return outerHandleResult;
  }

  private List<JobItemRunTask> getJobItemRunTaskList(JobItemRunRequest jobItemRunRequest,
      List<JobItem> jobItemList) {
    List<JobItemRunTask> jobItemRunTaskList = new ArrayList<JobItemRunTask>();
    JobItemRunTask jobItemRunTask = null;
    JobItemRunContextData jobItemRunContextData = null;
    IEnvironment environment = null;
    JobItemRunRequest jobItemRunRequestNew = null;
    for (JobItem jobItem : jobItemList) {
      jobItemRunRequestNew = getNewJobItemRunRequest(jobItemRunRequest, jobItem);
      environment = jobItem.getEnvironment();
      jobItemRunContextData = new JobItemRunContextData(jobItemRunRequestNew, environment);
      jobItemRunTask = new JobItemRunTask(jobItemRunContextData, new KiraClientJobHandler());
      jobItemRunTaskList.add(jobItemRunTask);
    }
    return jobItemRunTaskList;
  }

  private JobItemRunRequest getNewJobItemRunRequest(
      JobItemRunRequest jobItemRunRequest, JobItem jobItem) {
    JobItemRunRequest newJobItemRunRequest = new JobItemRunRequest();
    BeanUtils.copyProperties(jobItemRunRequest, newJobItemRunRequest);
    String jobId = jobItemRunRequest.getJobItemId();
    newJobItemRunRequest.setJobId(jobId);
    String jobItemId = jobItem.getId();
    newJobItemRunRequest.setJobItemId(jobItemId);

    return newJobItemRunRequest;
  }

  @Override
  public HandleResult cancelJob(JobCancelRequest jobCancelRequest) {
    HandleResult handleResult = null;
    if (KiraClientDataCenter.isWorkWithoutKira()) {
      handleResult = new HandleResult(KiraClientConstants.RESULT_CODE_FAILED,
          KiraClientConstants.ALLSCHEDULEDLOCALLY_HINT);
    } else {
      String resultCode = null;
      String resultData = null;
      Exception exceptionOccured = null;
      try {
        String appId = jobCancelRequest.getAppId();
        String triggerId = jobCancelRequest.getTriggerId();
        TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
        ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = KiraClientDataCenter
            .getEnvironmentTriggerContextDataMap(triggerIdentity);
        if (null != environmentTriggerContextDataMap) {
          IEnvironment environment = null;
          List<JobCancelTask> jobCancelTaskList = new ArrayList<JobCancelTask>();
          JobCancelTask jobCancelTask = null;
          for (Entry<IEnvironment, TriggerRegisterContextData> entry : environmentTriggerContextDataMap
              .entrySet()) {
            environment = entry.getKey();
            JobCancelContextData jobCancelContextData = new JobCancelContextData(jobCancelRequest,
                environment);
            jobCancelTask = new JobCancelTask(jobCancelContextData, new KiraClientJobHandler());
            jobCancelTaskList.add(jobCancelTask);
          }
          if (!CollectionUtils.isEmpty(jobCancelTaskList)) {
            HandleResult innerHandleResult = KiraCommonUtils
                .handleJobCancelTaskList(triggerIdentity, jobCancelTaskList);
            if (null != innerHandleResult) {
              resultCode = innerHandleResult.getResultCode();
              resultData = innerHandleResult.getResultData();
            } else {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
              resultData =
                  "innerHandleResult is empty for cancelJob. jobCancelRequest=" + KiraCommonUtils
                      .toString(jobCancelRequest);
            }
          } else {
            resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            resultData = "jobCancelTaskList is empty. jobCancelRequest=" + KiraCommonUtils
                .toString(jobCancelRequest);
          }
        } else {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          resultData =
              "environmentTriggerContextDataMap is null. jobCancelRequest=" + KiraCommonUtils
                  .toString(jobCancelRequest);
        }
      } catch (Exception e) {
        exceptionOccured = e;
        logger.error("Error occurs on cancelJob.jobCancelRequest=" + KiraCommonUtils
            .toString(jobCancelRequest), e);
      } finally {
        if (null != exceptionOccured) {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          String outerExceptionDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
          if (null == resultData) {
            resultData = "";
          }
          resultData += " Exception occurs. exceptionOccured=" + outerExceptionDesc;
        }
        handleResult = new HandleResult(resultCode, resultData);
      }
    }

    return handleResult;
  }

  @Override
  public Map<String, String> queryKiraClientInfoAsMap() {
    Map<String, String> returnValue = new LinkedHashMap<String, String>();
    returnValue.put("appId", KiraUtil.appId());
    returnValue.put("host", SystemUtil.getLocalhostIp());
    returnValue.put("kiraClientVersion", KiraClientDataCenter.getKiraclientversion());
    returnValue.put("kiraClientStartTime",
        KiraCommonUtils.getDateAsString(KiraClientDataCenter.getKiraclientStartTime()));
    returnValue.put("kiraClientConfig",
        KiraCommonUtils.toString(KiraClientDataCenter.getKiraClientConfig()));
    returnValue.put("jobIdJobMap", KiraCommonUtils.toString(KiraClientDataCenter.getJobIdJobMap()));
    return returnValue;
  }

}
