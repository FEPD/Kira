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

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobCancelContextData;
import com.yihaodian.architecture.kira.common.JobCancelTask;
import com.yihaodian.architecture.kira.common.JobStatusEnum;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.impl.RemoteKiraClientEnvironment;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobItemDao;
import com.yihaodian.architecture.kira.manager.domain.JobItem;
import com.yihaodian.architecture.kira.manager.domain.JobItemDetailData;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.service.JobItemHistoryService;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerJobHandler;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;

public class JobItemServiceImpl extends Service implements JobItemService {

  private JobItemDao jobItemDao;

  private JobItemHistoryService jobItemHistoryService;

  private JobService jobService;

  public void setJobItemDao(JobItemDao jobItemDao) {
    this.jobItemDao = jobItemDao;
  }

  public void setJobItemHistoryService(JobItemHistoryService jobItemHistoryService) {
    this.jobItemHistoryService = jobItemHistoryService;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public void insert(JobItem jobItem) {
    jobItemDao.insert(jobItem);
  }

  public int update(JobItem jobItem) {
    int actualRowsAffected = 0;

    String id = jobItem.getId();

    JobItem _oldJobItem = jobItemDao.select(id);

    if (_oldJobItem != null) {
      actualRowsAffected = jobItemDao.update(jobItem);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    JobItem _oldJobItem = jobItemDao.select(id);

    if (_oldJobItem != null) {
      actualRowsAffected = jobItemDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobItem select(String id) {
    return jobItemDao.select(id);
  }

  public List<JobItem> list(JobItemCriteria jobItemCriteria) {
    return jobItemDao.list(jobItemCriteria);
  }

  public List<JobItem> listOnPage(JobItemCriteria jobItemCriteria) {
    return jobItemDao.listOnPage(jobItemCriteria);
  }

  @Override
  public void updateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData,
      List<Integer> jobStatusIdList,
      boolean createJobItemHistory, boolean updateJobIfNeeded) {
    if (createJobItemHistory) {
      jobItemHistoryService.createJobItemHistory(jobItemId, jobStatusId, resultData, new Date());
    }

    int updateCount = tryToUpdateJobItemStatus(jobItemId, jobStatusId, resultData,
        KiraServerConstants.MAX_TRY_COUNT_DB);
    if (updateCount > 0) {
      if (updateJobIfNeeded) {
        JobItem jobItem = this.select(jobItemId);
        if (null != jobItem) {
          String jobId = jobItem.getJobId();
          if (StringUtils.isNotBlank(jobId)) {
            jobService.updateStatusOfJobByJobItems(jobId);
          }
        } else {
          logger.warn("Can not find jobItem by jobItemId={}", jobItemId);
        }
      }
    }
  }

  private int tryToUpdateJobItemStatus(String jobItemId, Integer jobStatusId, String resultData,
      int remainTryCount) {
    int returnValue = 0;
    JobItem jobItem = this.select(jobItemId);
    if (null != jobItem) {
      Integer oldDataVersion = jobItem.getDataVersion();
      Integer oldJobStatusId = jobItem.getJobStatusId();
      boolean isNeedToUpdateJobStatus = KiraCommonUtils
          .isNeedToUpdateJobStatus(oldJobStatusId, jobStatusId);
      if (isNeedToUpdateJobStatus) {
        List<Integer> canBeUpdatedJobStatusIdList = KiraCommonUtils
            .getCanBeUpdatedJobStatusIdList(jobStatusId);
        int updateCount = jobItemDao
            .updateJobItemStatus(jobItemId, jobStatusId, resultData, new Date(),
                canBeUpdatedJobStatusIdList, oldDataVersion);
        if (updateCount == 0) {
          if (remainTryCount > 0) {
            //optimistically try updateJobItemStatus for many times until success
            remainTryCount--;
            returnValue = tryToUpdateJobItemStatus(jobItemId, jobStatusId, resultData,
                remainTryCount);
            if (0 == remainTryCount) {
              logger.warn(
                  "Last time to call tryToUpdateJobItemStatus. jobItemId={} and jobStatusId={} and remainTryCount={} and returnValue={}",
                  jobItemId, jobStatusId, remainTryCount, returnValue);
            }
          }
        } else {
          returnValue = updateCount;
        }
      } else {
        returnValue = -1;
        if (logger.isDebugEnabled()) {
          logger.debug(
              "isNeedToUpdateJobStatus=false for oldJobStatusId={} and jobStatusId={} of jobItemId={}",
              oldJobStatusId, jobStatusId, jobItemId);
        }
      }
    } else {
      logger.warn("Can not find jobItem by jobItemId={}", jobItemId);
    }
    return returnValue;
  }

  @Override
  public JobItem createJobItem(String jobId, String serviceUrl, String argumentsAsJsonArrayString,
      boolean createJobItemHistory) {
    JobItem jobItem = new JobItem();
    String uuid = KiraCommonUtils.getUUID();
    jobItem.setId(uuid);
    jobItem.setJobId(jobId);
    jobItem.setServiceUrl(serviceUrl);
    jobItem.setArgumentsAsJsonArrayString(argumentsAsJsonArrayString);
    Date now = new Date();
    jobItem.setCreateTime(now);
    Integer jobStatusId = JobStatusEnum.CREATED.getId();
    jobItem.setJobStatusId(jobStatusId);
    jobItemDao.insert(jobItem);
    if (createJobItemHistory) {
      jobItemHistoryService.createJobItemHistory(jobItem.getId(), jobStatusId, null, now);
    }
    return jobItem;
  }

  @Override
  public List<JobItem> createJobItemList(String jobId,
      List<String> serviceUrlList,
      Map<String, String> centralScheduleServiceUrlArgumentsAsJsonArrayStringMap,
      boolean createJobItemHistory) {
    List<JobItem> jobItemList = new ArrayList<JobItem>();
    if (null != serviceUrlList) {
      JobItem jobItem = null;
      String argumentsAsJsonArrayString = null;
      for (String serviceUrl : serviceUrlList) {
        if (StringUtils.isNotBlank(serviceUrl)) {
          argumentsAsJsonArrayString = centralScheduleServiceUrlArgumentsAsJsonArrayStringMap
              .get(serviceUrl);
          jobItem = this
              .createJobItem(jobId, serviceUrl, argumentsAsJsonArrayString, createJobItemHistory);
          jobItemList.add(jobItem);
        }
      }
    }

    return jobItemList;
  }

  @Override
  public HandleResult cancelJobItem(String jobItemId) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (StringUtils.isNotBlank(jobItemId)) {
        JobItemCriteria jobItemCriteria = new JobItemCriteria();
        jobItemCriteria.setId(jobItemId);
        List<JobItemDetailData> jobItemDetailDataList = jobItemDao
            .getJobItemDetailDataListOnPage(jobItemCriteria);
        if (!CollectionUtils.isEmpty(jobItemDetailDataList)) {
          JobItemDetailData jobItemDetailData = jobItemDetailDataList.get(0);
          String serviceUrl = jobItemDetailData.getServiceUrl();
          String appId = jobItemDetailData.getAppId();
          String triggerId = jobItemDetailData.getTriggerId();
          TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
          ICentralScheduleService centralScheduleService = KiraManagerUtils
              .getCentralScheduleService(serviceUrl);
          if (null != centralScheduleService) {
            RemoteKiraClientEnvironment environment = new RemoteKiraClientEnvironment(serviceUrl,
                centralScheduleService);
            JobCancelRequest jobCancelRequest = new JobCancelRequest(appId, triggerId, new Date());
            JobCancelContextData jobCancelContextData = new JobCancelContextData(jobCancelRequest,
                environment);
            JobCancelTask jobCancelTask = new JobCancelTask(jobCancelContextData,
                new KiraManagerJobHandler());
            List<JobCancelTask> jobCancelTaskList = new ArrayList<JobCancelTask>();
            jobCancelTaskList.add(jobCancelTask);
            if (!CollectionUtils.isEmpty(jobCancelTaskList)) {
              HandleResult innerHandleResult = KiraCommonUtils
                  .handleJobCancelTaskList(triggerIdentity, jobCancelTaskList);
              if (null != innerHandleResult) {
                resultCode = innerHandleResult.getResultCode();
                resultData = innerHandleResult.getResultData();
              } else {
                resultCode = KiraServerConstants.RESULT_CODE_FAILED;
                resultData = "innerHandleResult is empty for cancelJobItem. jobItemId=" + jobItemId;
              }
            } else {
              resultCode = KiraServerConstants.RESULT_CODE_FAILED;
              resultData = "jobCancelTaskList is empty. jobItemId=" + jobItemId;
            }
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData =
                "centralScheduleService is null for cancelJobItem. serviceUrl=" + serviceUrl
                    + " and jobItemId=" + jobItemId;
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "Can not find jobItem detail data by jobItemId=" + jobItemId;
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "jobItemId should not be blank.";
      }
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on cancelJobItem.jobItemId=" + jobItemId, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String outerExceptionDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on cancelJobItem. jobItemId=" + jobItemId + ". exceptionOccured="
                + outerExceptionDesc;
      }

      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public List<JobItemDetailData> getJobItemDetailDataListOnPage(
      JobItemCriteria jobItemCriteria) {
    return jobItemDao.getJobItemDetailDataListOnPage(jobItemCriteria);
  }

  @Override
  public void archiveForJobItemData(String newTableNameSuffix,
      Date startTime, Date endTime) {
    if (KiraManagerUtils.isNeedArchiveJobRuntimeData()) {
      int createTableCount = this.jobItemDao
          .createArchiveTableForJobItemTableIfNeeded(newTableNameSuffix);
      logger.info(
          "createArchiveTableForJobItemTableIfNeeded return createTableCount={} by newTableNameSuffix={}",
          createTableCount, newTableNameSuffix);
      Object insertResult = this.jobItemDao
          .insertDataToJobItemArchiveTable(newTableNameSuffix, startTime, endTime);
      logger.info(
          "insertDataToJobItemArchiveTable return insertResult={} by newTableNameSuffix={} startTime={} endTime={}",
          KiraCommonUtils.toString(insertResult), newTableNameSuffix,
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    deleteJobItemData(startTime, endTime);
  }

  @Override
  public int deleteJobItemData(Date startTime, Date endTime) {
    int deleteCount = this.jobItemDao.deleteJobItemData(startTime, endTime);
    logger.info("deleteJobItemData return deleteCount={} by startTime={} endTime={}", deleteCount,
        KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    return deleteCount;
  }

  @Override
  public Date getDateOfOldestJobItem() {
    return this.jobItemDao.getDateOfOldestJobItem();
  }

  @Override
  public void handleJobItemRuntimeData(ExecutorService executorService, Date startTime,
      Date endTime)
      throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("start handleJobItemRuntimeData with startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    Date dateOfOldestData = this.getDateOfOldestJobItem();
    if (logger.isDebugEnabled()) {
      logger.debug("getDateOfOldestJobItem={}", KiraCommonUtils.getDateAsString(dateOfOldestData));
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
            archiveForJobItemData(newTableNameSuffix, startTimeOfInterval, endTimeOfInterval);
            return null;
          }
        });
      }
      if (CollectionUtils.isEmpty(taskList)) {
        if (logger.isDebugEnabled()) {
          logger.debug("taskList is empty. So do nothing for handleJobItemRuntimeData");
        }
      } else {
        KiraManagerUtils.runTasksOneByOne(taskList,
            KiraManagerUtils.getSleepTimeBeforeRunNextTaskInMilliseconds(), true, executorService,
            KiraManagerUtils.getTimeOutPerTaskInMilliseconds());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("end handleJobItemRuntimeData with startTime={} and endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      }
    } else {
      logger.info("getDateOfOldestJobItem return null. So handleJobItemRuntimeData do nothing.");
    }
  }
}
