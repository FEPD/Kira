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
package com.yihaodian.architecture.kira.client.internal.util;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.hedwig.provider.AppProfile;
import com.yihaodian.architecture.kira.client.akka.ProducerMessageActor;
import com.yihaodian.architecture.kira.client.internal.bean.Job;
import com.yihaodian.architecture.kira.client.internal.bean.JobItem;
import com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobStatusEnum;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import com.yihaodian.architecture.kira.common.dto.JobItemStatusReport;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.common.lock.iface.IDistributedSimpleLock;
import com.yihaodian.architecture.kira.common.lock.impl.zk.ZKImplementedDistributedSimpleLock;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class KiraClientUtils {

  private static Logger logger = LoggerFactory.getLogger(KiraClientUtils.class);
  private static ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public KiraClientUtils() {
    // TODO Auto-generated constructor stub
  }

  public static String getEnvironmentZNodeZKPathForTrigger(String triggerZNodeZKPath) {
    String hostEnvironmentZNodeName = getHostEnvironmentZNodeName();
    String environmentZNodeZKPathForTrigger =
        triggerZNodeZKPath + KiraClientConstants.ZNODE_NAME_ENVIRONMENTS
            + KiraCommonConstants.ZNODE_NAME_PREFIX + hostEnvironmentZNodeName;
    return environmentZNodeZKPathForTrigger;
  }

  public static String getHostEnvironmentZNodeName() {
    AppProfile appProfile = KiraClientDataCenter.getAppProfileForCentralScheduleServiceExporter();
    String hostEnvironmentZNodeName =
        appProfile.getDomainName() + KiraClientConstants.SPECIAL_DELIMITER + appProfile
            .getServiceAppName() + KiraClientConstants.SPECIAL_DELIMITER + KiraUtil
            .filterString(KiraClientConstants.SERVICE_NAME_CENTRAL_SCHEDULE_SERVICE)
            + KiraClientConstants.SPECIAL_DELIMITER + KiraUtil
            .filterString(KiraClientConstants.SERVICEVERSION_CENTRALSCHEDULESERVICE);
    return hostEnvironmentZNodeName;
  }

  public static void createOrUpdateEnvironmentZNodeForTriggerIfNeeded(
      TriggerMetadataZNodeData triggerMetaDataZNodeData) throws Exception {
    if (null != triggerMetaDataZNodeData) {
      String appId = triggerMetaDataZNodeData.getAppId();
      String triggerId = triggerMetaDataZNodeData.getTriggerId();
      String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
      String environmentZNodeZKPathForTrigger = KiraClientUtils
          .getEnvironmentZNodeZKPathForTrigger(triggerZNodeZKPath);
      String centralScheduleServiceZNodePath = KiraClientDataCenter
          .getCentralScheduleServiceZNodePath();
      if (zkClient.exists(triggerZNodeZKPath)) {
        TriggerMetadataZNodeData triggerMetadataZNodeDataOnZK = zkClient
            .readData(triggerZNodeZKPath, true);
        if (null != triggerMetadataZNodeDataOnZK) {
          String environmentsZNodeZKPathForTrigger =
              triggerZNodeZKPath + KiraCommonConstants.ZNODE_NAME_ENVIRONMENTS;
          String environmentZNodeZKShortPathForTrigger = KiraClientUtils
              .getHostEnvironmentZNodeName();
          KiraCommonUtils.createEnvironmentZNodeZKPathForTrigger(environmentsZNodeZKPathForTrigger,
              environmentZNodeZKShortPathForTrigger, centralScheduleServiceZNodePath);
        } else {
          logger.warn(
              "triggerMetadataZNodeDataOnZK is null on triggerZNodeZKPath. So do not create the enviornment.");
        }
      } else {
        logger.warn(
            "Can not create environmentZNodeZKPathForTrigger because the triggerZNodeZKPath do not exist. environmentZNodeZKPathForTrigger={} and triggerZNodeZKPath={} and centralScheduleServiceZNodePath={}",
            environmentZNodeZKPathForTrigger, triggerZNodeZKPath, centralScheduleServiceZNodePath);
      }
    }
  }

  public static void updateJobItemStatus(Job job, String jobItemId, Integer jobStatusId,
      String resultData) {
    for (JobItem jobItem : job.getJobItemList()) {
      if (jobItemId.equals(jobItem.getId())) {
        jobItem.setJobStatusId(jobStatusId);
        jobItem.setResultData(resultData);
        jobItem.setLastUpdateTime(new Date());
      }
    }
  }

  public static void updateJobItem(String jobPhase, Job job, String jobItemId,
      HandleResult handleResult) {
    if (null != job) {
      List<JobItem> jobItemList = job.getJobItemList();
      if (!CollectionUtils.isEmpty(jobItemList)) {
        boolean found = false;
        for (JobItem jobItem : jobItemList) {
          if (jobItemId.equals(jobItem.getId())) {
            found = true;
            String resultCode = handleResult.getResultCode();
            String resultData = handleResult.getResultData();
            Integer jobStatusId = null;
            if (KiraClientConstants.JOB_PHASE_DELIVERING.equals(jobPhase)) {
              try {
                jobStatusId = JobStatusEnum.DELIVERING.getId();
                jobItem.setJobStatusId(jobStatusId);
                jobItem.setResultData(resultData);
                jobItem.setLastUpdateTime(new Date());
              } finally {
                jobItem.getDeliveringPhaseEventHandledSignal().countDown();
              }
            } else if (KiraClientConstants.JOB_PHASE_DELIVER_COMPLETE.equals(jobPhase)) {
              try {
                jobItem.getDeliveringPhaseEventHandledSignal()
                    .await(KiraClientConstants.WAITFOR_STATECHANGE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                logger.warn(
                    "Wait for state of jobitem changed to delivering timeout, but just continue now. jobPhase=+"
                        + jobPhase + " and job=" + job + " and jobItemId=" + jobItemId
                        + " and handleResult=" + KiraCommonUtils.toString(handleResult));
              } finally {
                jobStatusId = KiraCommonUtils
                    .getJobStatusIdWhenCompleteDeliverByResultCode(resultCode);
                jobItem.setJobStatusId(jobStatusId);
                jobItem.setResultData(resultData);
                jobItem.setLastUpdateTime(new Date());
                jobItem.getDeliverCompletePhaseEventHandledSignal().countDown();
              }
            } else if (KiraClientConstants.JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD
                .equals(jobPhase)) {
              try {
                jobItem.getDeliverCompletePhaseEventHandledSignal()
                    .await(KiraClientConstants.WAITFOR_STATECHANGE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                logger.warn(
                    "Wait for state of jobitem changed to complete deliver timeout for JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD, but just continue now. jobPhase=+"
                        + jobPhase + " and job=" + job + " and jobItemId=" + jobItemId
                        + " and handleResult=" + KiraCommonUtils.toString(handleResult));
              } finally {
                jobStatusId = JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId();
                jobItem.setJobStatusId(jobStatusId);
                jobItem.setResultData(resultData);
                jobItem.setLastUpdateTime(new Date());
                jobItem.getNoNeedToRunBusinessMethodPhaseEventHandledSignal().countDown();
              }
            } else if (KiraClientConstants.JOB_PHASE_RUNNING.equals(jobPhase)) {
              try {
                jobItem.getDeliverCompletePhaseEventHandledSignal()
                    .await(KiraClientConstants.WAITFOR_STATECHANGE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                logger.warn(
                    "Wait for state of jobitem changed to complete deliver timeout for JOB_PHASE_RUNNING, but just continue now. jobPhase=+"
                        + jobPhase + " and job=" + job + " and jobItemId=" + jobItemId
                        + " and handleResult=" + KiraCommonUtils.toString(handleResult));
              } finally {
                jobStatusId = JobStatusEnum.RUNNING.getId();
                jobItem.setJobStatusId(jobStatusId);
                jobItem.setResultData(resultData);
                jobItem.setLastUpdateTime(new Date());
                jobItem.getRunningPhaseEventHandledSignal().countDown();
              }
            } else if (KiraClientConstants.JOB_PHASE_RUN_COMPLETE.equals(jobPhase)) {
              try {
                jobItem.getRunningPhaseEventHandledSignal()
                    .await(KiraClientConstants.WAITFOR_STATECHANGE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                logger.warn(
                    "Wait for state of jobitem changed to running timeout, but just continue now. jobPhase=+"
                        + jobPhase + " and job=" + job + " and jobItemId=" + jobItemId
                        + " and handleResult=" + KiraCommonUtils.toString(handleResult));
              } finally {
                jobStatusId = KiraCommonUtils.getJobStatusIdWhenCompleteRunByResultCode(resultCode);
                jobItem.setJobStatusId(jobStatusId);
                jobItem.setResultData(resultData);
                jobItem.setLastUpdateTime(new Date());
                jobItem.getRunCompletePhaseEventHandledSignal().countDown();
              }
            }
            break;
          }
        }
        if (!found) {
          logger.warn("can not find jobitem for jobPhase=+" + jobPhase + " and job=" + job
              + " and jobItemId=" + jobItemId + " and handleResult=" + KiraCommonUtils
              .toString(handleResult));
        } else {
          checkAllJobItemsStatusForJob(jobPhase, job);
        }
      } else {
        logger.warn(
            "jobItemList is empty for jobPhase=+" + jobPhase + " and job=" + job + " and jobItemId="
                + jobItemId + " and handleResult=" + KiraCommonUtils.toString(handleResult));
      }
    } else {
      logger.warn(
          "Can not find job for jobPhase=+" + jobPhase + " and job=" + job + " and jobItemId="
              + jobItemId + " and handleResult=" + KiraCommonUtils.toString(handleResult));
    }
  }

  public static void checkAllJobItemsStatusForJob(String jobPhase, Job job) {
    if (KiraClientConstants.JOB_PHASE_DELIVERING.equals(jobPhase)
        || KiraClientConstants.JOB_PHASE_DELIVER_COMPLETE.equals(jobPhase)) {
      return;
    }
    if (null != job) {
      List<JobItem> jobItemList = job.getJobItemList();
      if (!CollectionUtils.isEmpty(jobItemList)) {
        int runningCount = 0;
        int successRunCount = 0;
        int failedRunCount = 0;
        int partialSuccessRunCount = 0;
        int noNeedToRunBusinessMethodCount = 0;
        Integer jobStatusIdForJobItem = null;
        StringBuilder aggregatedResultData = new StringBuilder();
        String resultDataForJobItem = null;
        int number = 1;
        boolean isNeedAggregateResultData = false;
        if (jobItemList.size() > 1) {
          isNeedAggregateResultData = true;
        }
        for (JobItem jobItem : jobItemList) {
          resultDataForJobItem = jobItem.getResultData();
          if (KiraClientConstants.JOB_PHASE_RUNNING.equals(jobPhase)) {
            jobStatusIdForJobItem = jobItem.getJobStatusId();
            if (JobStatusEnum.RUNNING.getId().equals(jobStatusIdForJobItem)
                || JobStatusEnum.RUN_SUCCESS.getId().equals(jobStatusIdForJobItem)
                || JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobStatusIdForJobItem)
                || JobStatusEnum.RUN_FAILED.getId().equals(jobStatusIdForJobItem)) {
              runningCount++;
            }
          } else if (KiraClientConstants.JOB_PHASE_RUN_COMPLETE.equals(jobPhase)) {
            if (JobStatusEnum.RUN_SUCCESS.getId().equals(jobItem.getJobStatusId())) {
              successRunCount++;
            } else if (JobStatusEnum.RUN_PARTIAL_SUCCESS.getId().equals(jobItem.getJobStatusId())) {
              partialSuccessRunCount++;
            } else if (JobStatusEnum.RUN_FAILED.getId().equals(jobItem.getJobStatusId())) {
              failedRunCount++;
            }
          } else if (KiraClientConstants.JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD
              .equals(jobPhase)) {
            noNeedToRunBusinessMethodCount++;
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

        String aggregatedResultDataAsString = aggregatedResultData.toString();

        int jobItemCount = jobItemList.size();
        if (KiraClientConstants.JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD.equals(jobPhase)) {
          if (noNeedToRunBusinessMethodCount > 0
              && noNeedToRunBusinessMethodCount == jobItemCount) {
            //All jobItems are no need to run
            if (job.getNoNeedToRunBusinessMethodPhaseEventHandledSignal().getCount() > 0) {
              try {
                job.setJobStatusId(JobStatusEnum.NO_NEED_TO_RUN_BUSINESS_METHOD.getId());
                job.setResultData(aggregatedResultDataAsString);
                job.setLastUpdateTime(new Date());
                sendJobStatus(jobPhase, job);
              } finally {
                job.getNoNeedToRunBusinessMethodPhaseEventHandledSignal().countDown();
                KiraClientDataCenter.removeJob(job);
              }
            }
          }
        } else if (KiraClientConstants.JOB_PHASE_RUNNING.equals(jobPhase)) {
          if (runningCount > 0 && jobItemCount == (runningCount + noNeedToRunBusinessMethodCount)) {
            if (job.getRunningPhaseEventHandledSignal().getCount() > 0) {
              try {
                job.setJobStatusId(JobStatusEnum.RUNNING.getId());
                job.setResultData(aggregatedResultDataAsString);
                job.setLastUpdateTime(new Date());
                sendJobStatus(jobPhase, job);
              } finally {
                job.getRunningPhaseEventHandledSignal().countDown();
              }
            }
          }
        } else if (KiraClientConstants.JOB_PHASE_RUN_COMPLETE.equals(jobPhase)) {
          if (jobItemCount > 0 && (jobItemCount == (successRunCount + failedRunCount
              + partialSuccessRunCount + noNeedToRunBusinessMethodCount))) {
            //has complete to run all jobitems
            if (job.getRunCompletePhaseEventHandledSignal().getCount() > 0) {
              Integer jobStatusId = null;
              try {
                if (failedRunCount > 0) {
                  if (successRunCount > 0 || partialSuccessRunCount > 0) {
                    //部分成功
                    jobStatusId = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
                  } else {
                    //失败
                    jobStatusId = JobStatusEnum.RUN_FAILED.getId();
                  }
                } else if (successRunCount > 0) {
                  if (partialSuccessRunCount > 0) {
                    //部分成功
                    jobStatusId = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
                  } else {
                    //成功
                    jobStatusId = JobStatusEnum.RUN_SUCCESS.getId();
                  }
                } else if (partialSuccessRunCount > 0) {
                  jobStatusId = JobStatusEnum.RUN_PARTIAL_SUCCESS.getId();
                }
                try {
                  job.getRunningPhaseEventHandledSignal()
                      .await(KiraClientConstants.WAITFOR_STATECHANGE_TIMEOUT_SECONDS,
                          TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                  logger.warn(
                      "Wait for state of job changed to running timeout, but just continue now. jobPhase=+"
                          + jobPhase + " and jobStatusId=" + jobStatusId + " and job=" + job);
                }
              } finally {
                if (null != jobStatusId) {
                  try {
                    job.setJobStatusId(jobStatusId);
                    job.setResultData(aggregatedResultDataAsString);
                    job.setLastUpdateTime(new Date());
                    sendJobStatus(jobPhase, job);
                  } finally {
                    job.getRunCompletePhaseEventHandledSignal().countDown();
                    KiraClientDataCenter.removeJob(job);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private static void sendJobStatus(String jobPhase, Job job) {
    try {
      if (KiraClientDataCenter.isInMainProcess()) {
        String jobId = job.getId();
        Integer jobStatusId = job.getJobStatusId();
        String resultData = job.getResultData();
        JobItemStatusReport jobItemStatusReport = new JobItemStatusReport(jobId, jobStatusId,
            resultData);
        ProducerMessageActor.clientSendJobStatus(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS,
            jobItemStatusReport);
        //JumperMessageSender.sendJobItemStatusReport(jobItemStatusReport);
      } else if (KiraClientDataCenter.isInChildProcess()) {
        //ccjtodo: in phase2
      }

    } catch (Exception e) {
      logger.error(
          "Error occurs when sendJobItemStatusReport for jobPhase=" + jobPhase + " and job="
              + KiraCommonUtils.toString(job), e);
    }
  }

  public static String tryToAddUserAddedMethodInvokeLogAndReturnUpdatedResultData(
      String resultData) {
    String returnValue = resultData;

    try {
      String userAddedMethodInvokeLog = MethodInvokeContextHolder.getUserAddedMethodInvokeLog();
      if (StringUtils.isNotBlank(userAddedMethodInvokeLog)) {
        StringBuilder returnSB = new StringBuilder();
        if (null != resultData) {
          returnSB.append(resultData);
          returnSB.append(KiraCommonConstants.LINE_SEPARATOR);
        }
        returnSB.append(" --start userAddedMethodInvokeLog-- ");
        returnSB.append(KiraCommonConstants.LINE_SEPARATOR);
        returnSB.append(userAddedMethodInvokeLog);
        returnSB.append(KiraCommonConstants.LINE_SEPARATOR);
        returnSB.append(" --end userAddedMethodInvokeLog-- ");

        returnValue = returnSB.toString();
      }
    } finally {
      MethodInvokeContextHolder.removeUserAddedMethodInvokeLog();
    }

    return returnValue;
  }

  public static boolean validateMisfireInstructionForSimpleTrigger(int misfireInstruction) {
    if (misfireInstruction < Trigger.MISFIRE_INSTRUCTION_SMART_POLICY) {
      return false;
    }

    if (misfireInstruction
        > SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT) {
      return false;
    }

    return true;
  }

  public static boolean validateMisfireInstructionForCronTrigger(int misfireInstruction) {
    if (misfireInstruction < Trigger.MISFIRE_INSTRUCTION_SMART_POLICY) {
      return false;
    }

    if (misfireInstruction > CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING) {
      return false;
    }

    return true;
  }

  public static HandleResult invokeMethod(JobItemRunRequest jobItemRunRequest, Object targetObject,
      Method methodObject, Object[] arguments) throws Exception {
    HandleResult handleResult = null;
    Boolean asynchronous = jobItemRunRequest.getAsynchronous();
    String jobId = jobItemRunRequest.getJobId();
    String jobItemId = jobItemRunRequest.getJobItemId();
    Job job = KiraClientDataCenter.getJobIdJobMap().get(jobId);
    Boolean onlyRunOnSingleProcess = jobItemRunRequest.getOnlyRunOnSingleProcess();
    Boolean concurrent = jobItemRunRequest.getConcurrent();
    boolean isNeedToEnsureNotConcurrent = KiraCommonUtils
        .isNeedToEnsureNotConcurrent(onlyRunOnSingleProcess, concurrent);
    if (isNeedToEnsureNotConcurrent) {
      //Need to do business in lock
      String triggerZNodeZKPath = KiraCommonUtils
          .getTriggerZNodeZKPath(jobItemRunRequest.getAppId(), jobItemRunRequest.getTriggerId());
      boolean autoCreateParentZNode = false;
      String parentZNodeNameForLocks = KiraCommonConstants.PARENT_ZNODE_NAME_FOR_LOCKS_TO_CONTROL_CONCURRENT_RUN_BUSINESS_METHOD;
      IDistributedSimpleLock distributedSimpleLock = new ZKImplementedDistributedSimpleLock(
          triggerZNodeZKPath, autoCreateParentZNode, parentZNodeNameForLocks);
      Date now = new Date();
      if (distributedSimpleLock.tryLock()) {
        try {
          handleResult = doInvokeMethod(jobItemRunRequest, targetObject, methodObject, arguments);
        } finally {
          distributedSimpleLock.unlock();
        }
      } else {
        String resultCode = KiraCommonConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD;
        String resultData =
            "No need to run business method for this trigger. Because onlyRunOnSingleProcess=true and concurrent=false and there is job for this trigger is running in some place now. Now="
                + KiraCommonUtils.getDateAsStringToMsPrecision(now);
        handleResult = new HandleResult(resultCode, resultData);
        logger.debug(
            resultData + " jobItemRunRequest=" + KiraCommonUtils.toString(jobItemRunRequest));
        if (asynchronous) {
          KiraClientUtils
              .updateJobItem(KiraClientConstants.JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD, job,
                  jobItemId, handleResult);
        }
      }
    } else {
      handleResult = doInvokeMethod(jobItemRunRequest, targetObject, methodObject, arguments);
    }
    return handleResult;
  }

  private static HandleResult doInvokeMethod(JobItemRunRequest jobItemRunRequest,
      Object targetObject, Method methodObject, Object[] arguments) throws Exception {
    HandleResult handleResult = null;
    Boolean asynchronous = jobItemRunRequest.getAsynchronous();
    String jobId = jobItemRunRequest.getJobId();
    String jobItemId = jobItemRunRequest.getJobItemId();
    Job job = KiraClientDataCenter.getJobIdJobMap().get(jobId);
    KiraTimerTriggerBusinessRunningInstance kiraTimerTriggerBusinessRunningInstance = null;
    final Lock lockForState = new ReentrantLock();
    final Map<String, String> invokeMethodProcessMap = new LinkedHashMap<String, String>();
    final String PROCESS_KEY_NAME_INVOKE_COMPLETE = "invokeComplete";
    final String PROCESS_KEY_VALUE_YES = "YES";
    IZkStateListener zkStateListener = null;
    try {
      kiraTimerTriggerBusinessRunningInstance = KiraClientUtils
          .recordKiraTimerTriggerBusinessRunningInstance(jobItemRunRequest, targetObject,
              methodObject, arguments);
      final KiraTimerTriggerBusinessRunningInstance _kiraTimerTriggerBusinessRunningInstance = kiraTimerTriggerBusinessRunningInstance;
      zkStateListener = new IZkStateListener() {
        @Override
        public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
        }

        @Override
        public void handleNewSession() throws Exception {
          lockForState.lock();
          try {
            boolean isMethodInvokeComplete = PROCESS_KEY_VALUE_YES
                .equals(invokeMethodProcessMap.get(PROCESS_KEY_NAME_INVOKE_COMPLETE));
            if ((!isMethodInvokeComplete) && (null != _kiraTimerTriggerBusinessRunningInstance)) {
              String zkFullPath = _kiraTimerTriggerBusinessRunningInstance.getZkPath();
              if (!zkClient.exists(zkFullPath)) {
                String parentZKFullPath = KiraCommonUtils.getParentZKFullPath(zkFullPath);
                if (zkClient.exists(parentZKFullPath)) {
                  zkClient.createEphemeral(zkFullPath, _kiraTimerTriggerBusinessRunningInstance);
                }
              }
            }
          } finally {
            lockForState.unlock();
          }
        }
      };
      zkClient.subscribeStateChanges(zkStateListener);

      //Update job status to running just before call business method.
      if (asynchronous) {
        KiraClientUtils.updateJobItem(KiraClientConstants.JOB_PHASE_RUNNING, job, jobItemId,
            new HandleResult(KiraClientConstants.RESULT_CODE_SUCCESS, null));
      }
      handleResult = KiraCommonUtils.invokeMethod(targetObject, methodObject, arguments);
      if (null != handleResult) {
        String normalResultData = handleResult.getResultData();
        String finalResultData = KiraClientUtils
            .tryToAddUserAddedMethodInvokeLogAndReturnUpdatedResultData(normalResultData);
        handleResult.setResultData(finalResultData);
      }
    } finally {
      lockForState.lock();
      try {
        invokeMethodProcessMap.put(PROCESS_KEY_NAME_INVOKE_COMPLETE, PROCESS_KEY_VALUE_YES);
        try {
          if (null != zkStateListener) {
            zkClient.unsubscribeStateChanges(zkStateListener);
          }
        } finally {
          KiraClientUtils.deleteKiraTimerTriggerBusinessRunningInstance(
              kiraTimerTriggerBusinessRunningInstance);
        }
      } finally {
        lockForState.unlock();
      }
    }

    return handleResult;
  }

  public static KiraTimerTriggerBusinessRunningInstance recordKiraTimerTriggerBusinessRunningInstance(
      JobItemRunRequest jobItemRunRequest, Object targetObject,
      Method methodObject, Object[] arguments) {
    KiraTimerTriggerBusinessRunningInstance returnValue = null;
    try {
      if (null != jobItemRunRequest) {
        String appId = jobItemRunRequest.getAppId();
        String triggerId = jobItemRunRequest.getTriggerId();
        if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId)) {
          Date createTime = new Date();
          String createTimeAsString = KiraCommonUtils.getDateAsString(createTime,
              KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION);
          String host = SystemUtil.getLocalhostIp();
          CentralScheduleServiceExporter centralScheduleServiceExporter = KiraClientDataCenter
              .getCentralScheduleServiceExporter();
          if (null != centralScheduleServiceExporter) {
            ServiceProfile serviceProfile = KiraCommonUtils
                .getServiceProfile(centralScheduleServiceExporter);
            if (null != serviceProfile) {
              Integer port = Integer.valueOf(serviceProfile.getPort());
              String serviceUrl = serviceProfile.getServiceUrl();
              Integer pid = KiraClientDataCenter.getLocalProcessEnvironment().getPid();

              String jobItemId = jobItemRunRequest.getJobItemId();
              String jobId = jobItemRunRequest.getJobId();
              String version = jobItemRunRequest.getVersion();
              String sponsorPoolId = jobItemRunRequest.getSponsorPoolId();
              String sponsorTriggerId = jobItemRunRequest.getSponsorTriggerId();
              String argumentsAsJsonArrayString = jobItemRunRequest.getArgumentsAsJsonArrayString();

              String zkNodeName = host + KiraCommonConstants.COLON_DELIMITER + port.toString()
                  + KiraCommonConstants.SPECIAL_DELIMITER + createTimeAsString
                  + KiraCommonConstants.SPECIAL_DELIMITER + jobId;
              String filteredZKNodeName = KiraUtil.filterString(zkNodeName);
              String zkPath = null;
              String triggerZKPath = null;
              String runningZKPath = null;
              if (StringUtils.isBlank(sponsorPoolId) && StringUtils.isBlank(sponsorTriggerId)) {
                triggerZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
                runningZKPath = triggerZKPath + KiraCommonConstants.ZNODE_NAME_RUNNING;
                zkPath = runningZKPath + KiraCommonConstants.ZNODE_NAME_PREFIX + filteredZKNodeName;
              } else if (StringUtils.isNotBlank(sponsorPoolId) && StringUtils
                  .isNotBlank(sponsorTriggerId)) {
                triggerZKPath = KiraCommonUtils
                    .getTriggerZNodeZKPath(sponsorPoolId, sponsorTriggerId);
                runningZKPath = triggerZKPath + KiraCommonConstants.ZNODE_NAME_RUNNING;
                zkPath = runningZKPath + KiraCommonConstants.ZNODE_NAME_PREFIX + filteredZKNodeName;
              } else {
                logger.error(
                    "Failed to recordKiraTimerTriggerBusinessRunningInstance. sponsorPoolId and sponsorTriggerId should either be both not-blank or blank. jobItemRunRequest={}",
                    KiraCommonUtils.toString(jobItemRunRequest));
              }

              if (null != zkPath) {
                returnValue = new KiraTimerTriggerBusinessRunningInstance();
                returnValue.setHost(host);
                returnValue.setPort(port);
                returnValue.setServiceUrl(serviceUrl);
                returnValue.setPid(pid);
                returnValue.setCreateTime(createTime);
                returnValue.setJobItemId(jobItemId);
                returnValue.setJobId(jobId);
                returnValue.setAppId(appId);
                returnValue.setTriggerId(triggerId);
                returnValue.setVersion(version);
                returnValue.setSponsorPoolId(sponsorPoolId);
                returnValue.setSponsorTriggerId(sponsorTriggerId);
                returnValue.setArgumentsAsJsonArrayString(argumentsAsJsonArrayString);
                returnValue.setZkPath(zkPath);

                if (zkClient.exists(triggerZKPath)) {
                  if (!zkClient.exists(runningZKPath)) {
                    try {
                      zkClient.createPersistent(runningZKPath);
                    } catch (ZkNodeExistsException nodeExistsException) {
                      logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException
                          .getMessage()
                          + ". Just ignore this exception. This node may be created by someone.");
                    }
                  }

                  zkClient.createEphemeral(zkPath, returnValue);
                } else {
                  logger.info(
                      "parentZKPath do not exist now for some reason. so do not create zkNode for recordKiraTimerTriggerBusinessRunningInstance. jobItemRunRequest={}",
                      KiraCommonUtils.toString(jobItemRunRequest));
                }
              }
            } else {
              logger.error(
                  "serviceProfile is null , so can not recordKiraTimerTriggerBusinessRunningInstance. jobItemRunRequest={}",
                  KiraCommonUtils.toString(jobItemRunRequest));
            }
          } else {
            logger.error(
                "centralScheduleServiceExporter is null , so can not recordKiraTimerTriggerBusinessRunningInstance. jobItemRunRequest={}",
                KiraCommonUtils.toString(jobItemRunRequest));
          }
        } else {
          logger.error(
              "Failed to recordKiraTimerTriggerBusinessRunningInstance. appId and triggerId should either be both not-blank or blank. jobItemRunRequest={}",
              KiraCommonUtils.toString(jobItemRunRequest));
        }
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when recordKiraTimerTriggerBusinessRunningInstance. jobItemRunRequest="
              + KiraCommonUtils.toString(jobItemRunRequest), t);
    }
    return returnValue;
  }

  public static void deleteKiraTimerTriggerBusinessRunningInstance(
      KiraTimerTriggerBusinessRunningInstance kiraTimerTriggerBusinessRunningInstance) {
    try {
      if (null != kiraTimerTriggerBusinessRunningInstance) {
        String zkPath = kiraTimerTriggerBusinessRunningInstance.getZkPath();
        if (StringUtils.isNotBlank(zkPath)) {
          if (zkClient.exists(zkPath)) {
            zkClient.delete(zkPath);
          }
        } else {
          logger.error(
              "zkPath should not be blank for deleteKiraTimerTriggerBusinessRunningInstance. May have some bugs. kiraTimerTriggerBusinessRunningInstance={}",
              kiraTimerTriggerBusinessRunningInstance);
        }
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when deleteKiraTimerTriggerBusinessRunningInstance. kiraTimerTriggerBusinessRunningInstance="
              + kiraTimerTriggerBusinessRunningInstance, t);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
