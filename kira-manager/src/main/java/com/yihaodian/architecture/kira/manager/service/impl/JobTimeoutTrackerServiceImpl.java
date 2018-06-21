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
import com.yihaodian.architecture.kira.manager.criteria.JobTimeoutTrackerCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobTimeoutTrackerDao;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.springframework.util.CollectionUtils;

public class JobTimeoutTrackerServiceImpl extends Service implements JobTimeoutTrackerService {

  private JobTimeoutTrackerDao jobTimeoutTrackerDao;

  public void setJobTimeoutTrackerDao(JobTimeoutTrackerDao jobTimeoutTrackerDao) {
    this.jobTimeoutTrackerDao = jobTimeoutTrackerDao;
  }

  public void insert(JobTimeoutTracker jobTimeoutTracker) {
    jobTimeoutTrackerDao.insert(jobTimeoutTracker);
  }

  public int update(JobTimeoutTracker jobTimeoutTracker) {
    int actualRowsAffected = 0;

    String id = jobTimeoutTracker.getId();

    JobTimeoutTracker _oldJobTimeoutTracker = jobTimeoutTrackerDao.select(id);

    if (_oldJobTimeoutTracker != null) {
      actualRowsAffected = jobTimeoutTrackerDao.update(jobTimeoutTracker);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    JobTimeoutTracker _oldJobTimeoutTracker = jobTimeoutTrackerDao.select(id);

    if (_oldJobTimeoutTracker != null) {
      actualRowsAffected = jobTimeoutTrackerDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobTimeoutTracker select(String id) {
    return jobTimeoutTrackerDao.select(id);
  }

  public List<JobTimeoutTracker> list(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria) {
    return jobTimeoutTrackerDao.list(jobTimeoutTrackerCriteria);
  }

  public List<JobTimeoutTracker> listOnPage(JobTimeoutTrackerCriteria jobTimeoutTrackerCriteria) {
    return jobTimeoutTrackerDao.listOnPageUsingLimit(jobTimeoutTrackerCriteria);
  }

  @Override
  public void archiveForJobTimeoutTrackerData(String newTableNameSuffix,
      Date startTime, Date endTime) {
    if (KiraManagerUtils.isNeedArchiveJobRuntimeData()) {
      int createTableCount = this.jobTimeoutTrackerDao
          .createArchiveTableForJobTimeoutTrackerTableIfNeeded(newTableNameSuffix);
      logger.info(
          "createArchiveTableForJobTimeoutTrackerTableIfNeeded return createTableCount={} by newTableNameSuffix={}",
          createTableCount, newTableNameSuffix);
      Object insertResult = this.jobTimeoutTrackerDao
          .insertDataToJobTimeoutTrackerArchiveTable(newTableNameSuffix, startTime, endTime);
      logger.info(
          "insertDataToJobTimeoutTrackerArchiveTable return insertResult={} by newTableNameSuffix={} startTime={} endTime={}",
          KiraCommonUtils.toString(insertResult), newTableNameSuffix,
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    deleteJobTimeoutTrackerData(startTime, endTime);
  }

  @Override
  public int deleteJobTimeoutTrackerData(Date startTime, Date endTime) {
    int deleteCount = this.jobTimeoutTrackerDao.deleteJobTimeoutTrackerData(startTime, endTime);
    logger.info("deleteJobTimeoutTrackerData return deleteCount={} by startTime={} endTime={}",
        deleteCount, KiraCommonUtils.getDateAsString(startTime),
        KiraCommonUtils.getDateAsString(endTime));
    return deleteCount;
  }

  @Override
  public Date getDateOfOldestJobTimeoutTracker() {
    return this.jobTimeoutTrackerDao.getDateOfOldestJobTimeoutTracker();
  }

  @Override
  public void handleJobTimeoutTrackerRuntimeData(
      ExecutorService executorService, Date startTime, Date endTime)
      throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("start handleJobTimeoutTrackerRuntimeData with startTime={} and endTime={}",
          KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
    }
    Date dateOfOldestData = this.getDateOfOldestJobTimeoutTracker();
    if (logger.isDebugEnabled()) {
      logger.debug("getDateOfOldestJobTimeoutTracker={}",
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
            archiveForJobTimeoutTrackerData(newTableNameSuffix, startTimeOfInterval,
                endTimeOfInterval);
            return null;
          }
        });
      }

      if (CollectionUtils.isEmpty(taskList)) {
        if (logger.isDebugEnabled()) {
          logger.debug("taskList is empty. So do nothing for handleJobTimeoutTrackerRuntimeData");
        }
      } else {
        KiraManagerUtils.runTasksOneByOne(taskList,
            KiraManagerUtils.getSleepTimeBeforeRunNextTaskInMilliseconds(), true, executorService,
            KiraManagerUtils.getTimeOutPerTaskInMilliseconds());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("end handleJobTimeoutTrackerRuntimeData with startTime={} and endTime={}",
            KiraCommonUtils.getDateAsString(startTime), KiraCommonUtils.getDateAsString(endTime));
      }
    } else {
      logger.info(
          "getDateOfOldestJobTimeoutTracker return null. So handleJobTimeoutTrackerRuntimeData do nothing.");
    }
  }

  @Override
  public int updateJobTimeoutTrackerState(String id, String jobId, Integer state,
      Date lastUpdateStateTime, String lastUpdateStateDetails, Integer handleTimeoutFailedCount,
      List<Integer> stateList, Integer dataVersion) throws Exception {
    return this.jobTimeoutTrackerDao
        .updateJobTimeoutTrackerState(id, jobId, state, lastUpdateStateTime, lastUpdateStateDetails,
            handleTimeoutFailedCount, stateList, dataVersion);
  }

}
