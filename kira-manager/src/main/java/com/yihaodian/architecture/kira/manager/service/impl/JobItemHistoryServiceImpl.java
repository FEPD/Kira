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
import com.yihaodian.architecture.kira.manager.criteria.JobItemHistoryCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobItemHistoryDao;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistory;
import com.yihaodian.architecture.kira.manager.domain.JobItemHistoryDetailData;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.service.JobItemHistoryService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.springframework.util.CollectionUtils;

public class JobItemHistoryServiceImpl extends Service implements JobItemHistoryService {

  private JobItemHistoryDao jobItemHistoryDao;

  public void setJobItemHistoryDao(JobItemHistoryDao jobItemHistoryDao) {
    this.jobItemHistoryDao = jobItemHistoryDao;
  }

  public void insert(JobItemHistory jobItemHistory) {
    jobItemHistoryDao.insert(jobItemHistory);
  }

  public int update(JobItemHistory jobItemHistory) {
    int actualRowsAffected = 0;

    String id = jobItemHistory.getId();

    JobItemHistory _oldJobItemHistory = jobItemHistoryDao.select(id);

    if (_oldJobItemHistory != null) {
      actualRowsAffected = jobItemHistoryDao.update(jobItemHistory);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    JobItemHistory _oldJobItemHistory = jobItemHistoryDao.select(id);

    if (_oldJobItemHistory != null) {
      actualRowsAffected = jobItemHistoryDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobItemHistory select(String id) {
    return jobItemHistoryDao.select(id);
  }

  public List<JobItemHistory> list(JobItemHistoryCriteria jobItemHistoryCriteria) {
    return jobItemHistoryDao.list(jobItemHistoryCriteria);
  }

  public List<JobItemHistory> listOnPage(JobItemHistoryCriteria jobItemHistoryCriteria) {
    return jobItemHistoryDao.listOnPage(jobItemHistoryCriteria);
  }

  @Override
  public void createJobItemHistory(String jobItemId, Integer jobStatusId, String resultData,
      Date createTime) {
    JobItemHistory jobItemHistory = new JobItemHistory();
    String uuid = KiraCommonUtils.getUUID();
    jobItemHistory.setId(uuid);
    jobItemHistory.setJobItemId(jobItemId);
    jobItemHistory.setJobStatusId(jobStatusId);
    jobItemHistory.setCreateTime(createTime);
    jobItemHistory.setResultData(resultData);
    this.insert(jobItemHistory);
  }

  @Override
  public List<JobItemHistoryDetailData> getJobItemHistoryDetailDataListOnPage(
      JobItemHistoryCriteria jobItemHistoryCriteria) {
    return jobItemHistoryDao.getJobItemHistoryDetailDataListOnPage(jobItemHistoryCriteria);
  }

  @Override
  public void archiveForJobItemHistoryData(String newTableNameSuffix,
      Date startTime, Date endTime) {
    if (KiraManagerUtils.isNeedArchiveJobRuntimeData()) {
      int createTableCount = this.jobItemHistoryDao
          .createArchiveTableForJobItemHistoryTableIfNeeded(newTableNameSuffix);
      logger.info(
          "createArchiveTableForJobItemHistoryTableIfNeeded return createTableCount={} by newTableNameSuffix={}",
          createTableCount, newTableNameSuffix);
      Object insertResult = this.jobItemHistoryDao
          .insertDataToJobItemHistoryArchiveTable(newTableNameSuffix, startTime, endTime);
      logger.info(
          "insertDataToJobItemHistoryArchiveTable return insertResult={} by newTableNameSuffix={} startTime={} endTime={}",
          KiraCommonUtils.toString(insertResult), newTableNameSuffix,
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    deleteJobItemHistoryData(startTime, endTime);
  }

  @Override
  public int deleteJobItemHistoryData(Date startTime, Date endTime) {
    int deleteCount = this.jobItemHistoryDao.deleteJobItemHistoryData(startTime, endTime);
    logger.info("deleteJobItemHistoryData return deleteCount={} by startTime={} endTime={}",
        deleteCount, KiraCommonUtils.getDateAsString(startTime),
        KiraCommonUtils.getDateAsString(endTime));
    return deleteCount;
  }

  @Override
  public Date getDateOfOldestJobItemHistory() {
    return this.jobItemHistoryDao.getDateOfOldestJobItemHistory();
  }

  @Override
  public void handleJobItemHistoryRuntimeData(ExecutorService executorService, Date startTime,
      Date endTime)
      throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("start handleJobItemHistoryRuntimeData with startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    Date dateOfOldestData = this.getDateOfOldestJobItemHistory();
    if (logger.isDebugEnabled()) {
      logger.debug("getDateOfOldestJobItemHistory={}",
          KiraCommonUtils.getDateAsString(dateOfOldestData));
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
            archiveForJobItemHistoryData(newTableNameSuffix, startTimeOfInterval,
                endTimeOfInterval);
            return null;
          }
        });
      }

      if (CollectionUtils.isEmpty(taskList)) {
        if (logger.isDebugEnabled()) {
          logger.debug("taskList is empty. So do nothing for handleJobItemHistoryRuntimeData");
        }
      } else {
        KiraManagerUtils.runTasksOneByOne(taskList,
            KiraManagerUtils.getSleepTimeBeforeRunNextTaskInMilliseconds(), true, executorService,
            KiraManagerUtils.getTimeOutPerTaskInMilliseconds());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("end handleJobItemHistoryRuntimeData with startTime={} and endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      }
    } else {
      logger.info(
          "getDateOfOldestJobItemHistory return null. So handleJobItemHistoryRuntimeData do nothing.");
    }
  }


}
