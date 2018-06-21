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
package com.yihaodian.architecture.kira.manager.alarm;

import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import com.yihaodian.architecture.kira.manager.criteria.JobCriteria;
import com.yihaodian.architecture.kira.manager.criteria.JobItemCriteria;
import com.yihaodian.architecture.kira.manager.domain.JobDetailData;
import com.yihaodian.architecture.kira.manager.domain.JobItemDetailData;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class AlarmCenter implements ILifecycle {

  private static final int DEFAULT_COREPOOLSIZE = 10;
  private static final int DEFAULT_MAXIMUMPOOLSIZE = 20;
  private static final long DEFAULT_KEEPALIVETIME = 60L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  private static final int DEFAULT_QUEUE_CAPACITY = 500;

  private static Logger logger = LoggerFactory.getLogger(AlarmCenter.class);

  private ExecutorService alarmExecutor;

  private AlarmHelper alarmHelper;

  private JobService jobService;

  private JobItemService jobItemService;

  private KiraClientMetadataService kiraClientMetadataService;

  public AlarmCenter() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setAlarmHelper(AlarmHelper alarmHelper) {
    this.alarmHelper = alarmHelper;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public void setJobItemService(JobItemService jobItemService) {
    this.jobItemService = jobItemService;
  }

  public void setKiraClientMetadataService(KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public void submitAlarmTaskForJobTimeout(JobTimeoutTracker jobTimeoutTracker) {
    if (null != alarmExecutor) {
      JobTimeoutAlarmTask jobTimeoutAlarmTask = new JobTimeoutAlarmTask(alarmHelper,
          jobTimeoutTracker);
      alarmExecutor.submit(jobTimeoutAlarmTask);
    }
  }

  public void alarmForJobIfNeeded(String jobId, Integer jobStatusId) {
    if (alarmHelper.isNeedToAlarm(jobStatusId)) {
      //Because isPoolNeedToAlarmByJobId will take a lot of time to query, so do not use it and work as the old way..
      //boolean isPoolNeedToAlarmByJobId = isPoolNeedToAlarmByJobId(jobId);
      submitAlarmTaskForJob(jobId);
    }
  }

  public void alarmForJobIfNeeded(String appId, String jobId, Integer jobStatusId) {
    if (alarmHelper.isNeedToAlarm(jobStatusId)) {
      boolean isPoolNeedToAlarm = this.isPoolNeedToAlarm(appId);
      if (isPoolNeedToAlarm) {
        submitAlarmTaskForJob(jobId);
      }
    }
  }

  private boolean isPoolNeedToAlarmByJobId(String jobId) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(jobId)) {
      JobCriteria jobCriteria = new JobCriteria();
      jobCriteria.setId(jobId);
      List<JobDetailData> jobDetailDataListOnPage = this.jobService
          .getJobDetailDataListOnPage(jobCriteria);
      if (!CollectionUtils.isEmpty(jobDetailDataListOnPage)) {
        JobDetailData jobDetailData = jobDetailDataListOnPage.get(0);
        String appId = jobDetailData.getAppId();
        returnValue = this.isPoolNeedToAlarm(appId);
      }
    }
    return returnValue;
  }

  public boolean isPoolNeedToAlarm(String appId) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(appId)) {
      KiraClientMetadata kiraClientMetadata = this.kiraClientMetadataService
          .getKiraClientMetadataByPoolId(appId);
      if (null != kiraClientMetadata) {
        Boolean sendAlarmEmail = kiraClientMetadata.getSendAlarmEmail();
        Boolean sendAlarmSMS = kiraClientMetadata.getSendAlarmSMS();
        if (Boolean.TRUE.equals(sendAlarmEmail)
            || Boolean.TRUE.equals(sendAlarmSMS)) {
          returnValue = true;
        }
      }
    }
    return returnValue;
  }

  public void submitAlarmTaskForJob(String jobId) {
    if (null != alarmExecutor) {
      JobAlarmTask jobAlarmTask = new JobAlarmTask(alarmHelper, jobId);
      alarmExecutor.submit(jobAlarmTask);
    }
  }

  public void alarmForJobItemIfNeeded(String jobItemId, Integer jobStatusId) {
    if (alarmHelper.isNeedToAlarm(jobStatusId)) {
      //Because isPoolNeedToAlarmByJobItemId will take a lot of time to query, so do not use it and work as the old way..
      //boolean isPoolNeedToAlarmByJobItemId = isPoolNeedToAlarmByJobItemId(jobItemId);
      submitAlarmTaskForJobItem(jobItemId);
    }
  }

  public void alarmForJobItemIfNeeded(String appId, String jobItemId, Integer jobStatusId) {
    if (alarmHelper.isNeedToAlarm(jobStatusId)) {
      boolean isPoolNeedToAlarm = this.isPoolNeedToAlarm(appId);
      if (isPoolNeedToAlarm) {
        submitAlarmTaskForJobItem(jobItemId);
      }
    }
  }

  public void alarmForJobItemIfPoolNeeded(String appId, String jobItemId) {
    boolean isPoolNeedToAlarm = this.isPoolNeedToAlarm(appId);
    if (isPoolNeedToAlarm) {
      submitAlarmTaskForJobItem(jobItemId);
    }
  }

  private boolean isPoolNeedToAlarmByJobItemId(String jobItemId) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(jobItemId)) {
      JobItemCriteria jobItemCriteria = new JobItemCriteria();
      jobItemCriteria.setId(jobItemId);
      List<JobItemDetailData> jobItemDetailDataListOnPage = this.jobItemService
          .getJobItemDetailDataListOnPage(jobItemCriteria);
      if (!CollectionUtils.isEmpty(jobItemDetailDataListOnPage)) {
        JobItemDetailData jobItemDetailData = jobItemDetailDataListOnPage.get(0);
        String appId = jobItemDetailData.getAppId();
        returnValue = this.isPoolNeedToAlarm(appId);
      }
    }
    return returnValue;
  }

  public void submitAlarmTaskForJobItem(String jobItemId) {
    if (null != alarmExecutor) {
      JobItemAlarmTask jobItemAlarmTask = new JobItemAlarmTask(alarmHelper, jobItemId);
      alarmExecutor.submit(jobItemAlarmTask);
    }
  }

  @Override
  public void init() {
    try {
      logger.info("init for AlarmCenter");
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-manager-alarmExecutor-");
      threadFactory.setDaemon(false);
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(DEFAULT_QUEUE_CAPACITY);
      alarmExecutor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE, DEFAULT_MAXIMUMPOOLSIZE,
          DEFAULT_KEEPALIVETIME, DEFAULT_UNIT, queue, threadFactory);
    } catch (Exception e) {
      logger.error("Error occurs when initialize AlarmCenter.", e);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != alarmExecutor) {
        alarmExecutor.shutdown();
      }
      logger.info("AlarmCenter destroyed.");
    } catch (Exception e) {
      logger.error("Error occurs when destroy AlarmCenter.", e);
    } finally {
      alarmExecutor = null;
    }
  }
}
