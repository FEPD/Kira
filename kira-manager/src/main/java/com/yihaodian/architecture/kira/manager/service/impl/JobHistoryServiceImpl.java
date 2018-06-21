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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.criteria.JobHistoryCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobHistoryDao;
import com.yihaodian.architecture.kira.manager.domain.JobHistory;
import com.yihaodian.architecture.kira.manager.domain.JobHistoryDetailData;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.service.JobHistoryService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.springframework.util.CollectionUtils;

public class JobHistoryServiceImpl extends Service implements JobHistoryService {

  private JobHistoryDao jobHistoryDao;

  public void setJobHistoryDao(JobHistoryDao jobHistoryDao) {
    this.jobHistoryDao = jobHistoryDao;
  }

  public void insert(JobHistory jobHistory) {
    jobHistoryDao.insert(jobHistory);
  }

  public int update(JobHistory jobHistory) {
    int actualRowsAffected = 0;

    String id = jobHistory.getId();

    JobHistory _oldJobHistory = jobHistoryDao.select(id);

    if (_oldJobHistory != null) {
      actualRowsAffected = jobHistoryDao.update(jobHistory);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    JobHistory _oldJobHistory = jobHistoryDao.select(id);

    if (_oldJobHistory != null) {
      actualRowsAffected = jobHistoryDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobHistory select(String id) {
    return jobHistoryDao.select(id);
  }

  public List<JobHistory> list(JobHistoryCriteria jobHistoryCriteria) {
    return jobHistoryDao.list(jobHistoryCriteria);
  }

  public List<JobHistory> listOnPage(JobHistoryCriteria jobHistoryCriteria) {
    return jobHistoryDao.listOnPage(jobHistoryCriteria);
  }

  @Override
  public void createJobHistory(String jobId, Integer jobStatusId, String resultData,
      Date createTime) {
    JobHistory jobHistory = new JobHistory();
    String uuid = KiraCommonUtils.getUUID();
    jobHistory.setId(uuid);
    jobHistory.setJobId(jobId);
    jobHistory.setJobStatusId(jobStatusId);
    jobHistory.setResultData(resultData);
    jobHistory.setCreateTime(createTime);
    jobHistoryDao.insert(jobHistory);
  }

  @Override
  public List<JobHistoryDetailData> getJobHistoryDetailDataListOnPage(
      JobHistoryCriteria jobHistoryCriteria) {
    return jobHistoryDao.getJobHistoryDetailDataListOnPage(jobHistoryCriteria);
  }

  @Override
  public void archiveForJobHistoryData(String newTableNameSuffix,
      Date startTime, Date endTime) {
    if (KiraManagerUtils.isNeedArchiveJobRuntimeData()) {
      int createTableCount = this.jobHistoryDao
          .createArchiveTableForJobHistoryTableIfNeeded(newTableNameSuffix);
      logger.info(
          "createArchiveTableForJobHistoryTableIfNeeded return createTableCount={} by newTableNameSuffix={}",
          createTableCount, newTableNameSuffix);
      Object insertResult = this.jobHistoryDao
          .insertDataToJobHistoryArchiveTable(newTableNameSuffix, startTime, endTime);
      logger.info(
          "insertDataToJobHistoryArchiveTable return insertResult={} by newTableNameSuffix={} startTime={} endTime={}",
          KiraCommonUtils.toString(insertResult), newTableNameSuffix,
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    deleteJobHistoryData(startTime, endTime);
  }

  @Override
  public int deleteJobHistoryData(Date startTime, Date endTime) {
    int deleteCount = this.jobHistoryDao.deleteJobHistoryData(startTime, endTime);
    logger
        .info("deleteJobHistoryData return deleteCount={} by startTime={} endTime={}", deleteCount,
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    return deleteCount;
  }

  @Override
  public Date getDateOfOldestJobHistory() {
    return this.jobHistoryDao.getDateOfOldestJobHistory();
  }

  @Override
  public void handleJobHistoryRuntimeData(ExecutorService executorService, Date startTime,
      Date endTime)
      throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("start handleJobHistoryRuntimeData with startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    Date dateOfOldestData = this.getDateOfOldestJobHistory();
    if (logger.isDebugEnabled()) {
      logger
          .debug("getDateOfOldestJobHistory={}", KiraCommonUtils.getDateAsString(dateOfOldestData));
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
            archiveForJobHistoryData(newTableNameSuffix, startTimeOfInterval, endTimeOfInterval);
            return null;
          }
        });
      }
      if (CollectionUtils.isEmpty(taskList)) {
        if (logger.isDebugEnabled()) {
          logger.debug("taskList is empty. So do nothing for handleJobHistoryRuntimeData");
        }
      } else {
        KiraManagerUtils.runTasksOneByOne(taskList,
            KiraManagerUtils.getSleepTimeBeforeRunNextTaskInMilliseconds(), true, executorService,
            KiraManagerUtils.getTimeOutPerTaskInMilliseconds());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("end handleJobHistoryRuntimeData with startTime={} and endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.info(
            "getDateOfOldestJobHistory return null. So handleJobHistoryRuntimeData do nothing.");
      }
    }
  }

}
