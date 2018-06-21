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
package com.yihaodian.architecture.kira.manager.event.jobtimeouttracker;

import com.yihaodian.architecture.kira.common.event.EventDispatcher;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import com.yihaodian.architecture.kira.manager.alarm.AlarmCenter;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.JobTimeoutTracker;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.service.JobService;
import com.yihaodian.architecture.kira.manager.service.JobTimeoutTrackerService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.JobTimeoutTrackerStateEnum;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class JobTimeoutTrackerEventHandler implements EventHandler<JobTimeoutTrackerEvent>,
    ILifecycle {

  private static final int DEFAULT_COREPOOLSIZE = 5;
  private static final int DEFAULT_MAXIMUMPOOLSIZE = 10;
  private static final long DEFAULT_KEEPALIVETIME = 60L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  private static final int DEFAULT_QUEUE_CAPACITY = 300;

  private static Logger logger = LoggerFactory.getLogger(JobTimeoutTrackerEventHandler.class);

  private ExecutorService executor;

  private EventDispatcher eventDispatcher;

  private JobService jobService;

  private TriggerMetadataService triggerMetadataService;

  private KiraClientMetadataService kiraClientMetadataService;

  private JobTimeoutTrackerService jobTimeoutTrackerService;

  private AlarmCenter alarmCenter;

  public JobTimeoutTrackerEventHandler() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
  }

  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public void setJobTimeoutTrackerService(
      JobTimeoutTrackerService jobTimeoutTrackerService) {
    this.jobTimeoutTrackerService = jobTimeoutTrackerService;
  }

  public void setAlarmCenter(AlarmCenter alarmCenter) {
    this.alarmCenter = alarmCenter;
  }

  @Override
  public void init() {
    try {
      logger.info("init for JobTimeoutTrackerEventDispatcher");
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-manager-JobTimeoutTrackerEventDispatcher-");
      threadFactory.setDaemon(false);
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(DEFAULT_QUEUE_CAPACITY);
      executor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE, DEFAULT_MAXIMUMPOOLSIZE,
          DEFAULT_KEEPALIVETIME, DEFAULT_UNIT, queue, threadFactory);
      eventDispatcher.register(JobTimeoutTrackerEventType.class, this);
    } catch (Exception e) {
      logger.error("Error occurs when initialize JobTimeoutTrackerEventDispatcher.", e);
    }
  }

  @Override
  public void handle(JobTimeoutTrackerEvent event) {
    if (!this.executor.isShutdown()) {
      JobTimeoutTrackerEventHandleTask jobTimeoutTrackerEventHandleTask = new JobTimeoutTrackerEventHandleTask(
          event);
      executor.submit(jobTimeoutTrackerEventHandleTask);
    } else {
      logger.error(
          "Can not handle JobTimeoutTrackerEvent for executor is shutdown. JobTimeoutTrackerEvent={}",
          event);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != executor) {
        executor.shutdown();
      }
      logger.info("JobTimeoutTrackerEventDispatcher destroyed.");
    } catch (Exception e) {
      logger.error("Error occurs when destroy JobTimeoutTrackerEventDispatcher.", e);
    }
  }

  class JobTimeoutTrackerEventHandleTask implements Callable<Object> {

    private JobTimeoutTrackerEvent jobTimeoutTrackerEvent;

    JobTimeoutTrackerEventHandleTask(JobTimeoutTrackerEvent jobTimeoutTrackerEvent) {
      super();
      this.jobTimeoutTrackerEvent = jobTimeoutTrackerEvent;
    }

    @Override
    public Object call() throws Exception {
      switch (jobTimeoutTrackerEvent.getEventType()) {
        case JOB_TIMEOUT:
          handleJobTimeoutEvent((JobTimeoutEvent) jobTimeoutTrackerEvent);
          break;
        case JOB_TIMEOUT_HANDLE_FAILED:
          handleJobTimeoutHandleFailedEvent((JobTimeoutHandleFailedEvent) jobTimeoutTrackerEvent);
          break;
        default:
          logger.error("Unknown eventType for jobTimeoutTrackerEvent=" + jobTimeoutTrackerEvent);
          break;
      }
      return null;
    }

    private void handleJobTimeoutEvent(JobTimeoutEvent jobTimeoutEvent) {
      JobTimeoutTracker jobTimeoutTracker = jobTimeoutEvent.getJobTimeoutTracker();
      Integer state = jobTimeoutTracker.getState();
      if (JobTimeoutTrackerStateEnum.INITIAL.getState().equals(state)
          || JobTimeoutTrackerStateEnum.TIMEOUT_HANDLED_FAILED.getState().equals(state)) {
        try {
          String jobId = jobTimeoutTracker.getJobId();
          List<Integer> stateList = new ArrayList<Integer>();
          stateList.add(jobTimeoutTracker.getState());
          Integer handleTimeoutFailedCount = jobTimeoutTracker.getHandleTimeoutFailedCount();
          Integer dataVersion = jobTimeoutTracker.getDataVersion();
          Job job = jobService.select(jobId);
          if (null != job) {
            Long triggerMetadataId = job.getTriggerMetadataId();
            TriggerMetadata triggerMetadata = triggerMetadataService.select(triggerMetadataId);
            if (null != triggerMetadata) {
              Long runTimeThreshold = triggerMetadata.getRunTimeThreshold();
              if (null != runTimeThreshold) {
                String appId = triggerMetadata.getAppId();
                boolean isPoolNeedAlarmAndAlarmReceiverSet = kiraClientMetadataService
                    .isPoolNeedAlarmAndAlarmReceiverSet(appId);
                if (isPoolNeedAlarmAndAlarmReceiverSet) {
                  Integer jobStatusId = job.getJobStatusId();
                  boolean isJobCompleted = jobService.isJobCompleted(jobId, jobStatusId);
                  if (isJobCompleted) {
                    Date lastUpdateTime = job.getLastUpdateTime();
                    Date expectTimeoutTime = jobTimeoutTracker.getExpectTimeoutTime();
                    if (null != lastUpdateTime && null != expectTimeoutTime) {
                      if (lastUpdateTime.after(expectTimeoutTime)) {
                        alarmCenter.submitAlarmTaskForJobTimeout(jobTimeoutTracker);
                      } else {
                        jobTimeoutTrackerService
                            .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobId,
                                JobTimeoutTrackerStateEnum.NOT_TIMEOUT.getState(), new Date(),
                                jobTimeoutTracker.getLastUpdateStateDetails(),
                                jobTimeoutTracker.getHandleTimeoutFailedCount(), stateList,
                                dataVersion);
                      }
                    }
                  } else {
                    alarmCenter.submitAlarmTaskForJobTimeout(jobTimeoutTracker);
                  }
                } else {
                  logger.info(
                      "isPoolNeedAlarmAndAlarmReceiverSet is false now for jobTimeoutTracker={}",
                      jobTimeoutTracker);
                  jobTimeoutTrackerService
                      .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobId,
                          JobTimeoutTrackerStateEnum.TIMEOUT_NO_NEED_TO_HANDLE_FOR_POOL_DO_NOT_ALARM_OR_ALARM_RECEIVER_NOT_SET
                              .getState(), new Date(),
                          "The pool do not want to receive alarm or the receiver is empty. jobTimeoutTracker="
                              + jobTimeoutTracker, handleTimeoutFailedCount, stateList,
                          dataVersion);
                }
              } else {
                logger.info("runTimeThreshold is null now for jobTimeoutTracker={}",
                    jobTimeoutTracker);
                jobTimeoutTrackerService
                    .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobId,
                        JobTimeoutTrackerStateEnum.TIMEOUT_NO_NEED_TO_HANDLE_FOR_RUNTIME_THRESHOLD_EMPTY
                            .getState(), new Date(),
                        "The runTimeThreshold is null now. jobTimeoutTracker=" + jobTimeoutTracker,
                        handleTimeoutFailedCount, stateList, dataVersion);
              }
            } else {
              logger.warn(
                  "Can not find triggerMetadata by triggerMetadataId={} for jobTimeoutTracker={}",
                  triggerMetadataId, jobTimeoutTracker);
              jobTimeoutTrackerService
                  .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobId,
                      JobTimeoutTrackerStateEnum.TIMEOUT_NO_NEED_TO_HANDLE_FOR_TRIGGER_DO_NOT_EXIST
                          .getState(), new Date(),
                      "The trigger do not exist now. jobTimeoutTracker=" + jobTimeoutTracker,
                      handleTimeoutFailedCount, stateList, dataVersion);
            }
          } else {
            logger
                .warn("Can not find job data when handle jobTimeoutTracker={}", jobTimeoutTracker);
            jobTimeoutTrackerService.updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobId,
                JobTimeoutTrackerStateEnum.TIMEOUT_NO_NEED_TO_HANDLE_FOR_JOB_DO_NOT_EXIST
                    .getState(), new Date(),
                "The job do not exist now. jobTimeoutTracker=" + jobTimeoutTracker,
                handleTimeoutFailedCount, stateList, dataVersion);
          }
        } catch (Throwable t) {
          String exceptionDesc = ExceptionUtils.getFullStackTrace(t);
          JobTimeoutHandleFailedEvent jobTimeoutHandleFailedEvent = new JobTimeoutHandleFailedEvent(
              jobTimeoutTracker, JobTimeoutTrackerEventType.JOB_TIMEOUT_HANDLE_FAILED,
              exceptionDesc);
          eventDispatcher.dispatch(jobTimeoutHandleFailedEvent);
        }
      } else {
        logger.warn(
            "handleJobTimeoutTrackerEventWhenJobTimeout should not handle jobTimeoutTracker={} . It may have some bugs and ignore it.",
            jobTimeoutTracker);
      }
    }

    private void handleJobTimeoutHandleFailedEvent(
        JobTimeoutHandleFailedEvent jobTimeoutHandleFailedEvent) throws Exception {
      JobTimeoutTracker jobTimeoutTracker = jobTimeoutHandleFailedEvent.getJobTimeoutTracker();
      String jobTimeoutHandleFailedDetails = jobTimeoutHandleFailedEvent
          .getJobTimeoutHandleFailedDetails();
      List<Integer> stateList = new ArrayList<Integer>();
      stateList.add(jobTimeoutTracker.getState());
      Integer handleTimeoutFailedCount = jobTimeoutTracker.getHandleTimeoutFailedCount();
      if (null != handleTimeoutFailedCount) {
        handleTimeoutFailedCount = Integer.valueOf(handleTimeoutFailedCount.intValue() + 1);
      } else {
        handleTimeoutFailedCount = Integer.valueOf(1);
      }
      if (KiraManagerUtils.getJobTimeoutHandleFailedMaxCount() <= handleTimeoutFailedCount
          .intValue()) {
        jobTimeoutTrackerService
            .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobTimeoutTracker.getJobId(),
                JobTimeoutTrackerStateEnum.TIMEOUT_NO_NEED_TO_HANDLE_FOR_FAILED_TOO_MANY_TIMES
                    .getState(), new Date(),
                "Handle failed too many times after time out. jobTimeoutHandleFailedMaxCount="
                    + KiraManagerUtils.getJobTimeoutHandleFailedMaxCount()
                    + " and jobTimeoutHandleFailedDetails=" + jobTimeoutHandleFailedDetails,
                handleTimeoutFailedCount, stateList, jobTimeoutTracker.getDataVersion());
      } else {
        jobTimeoutTrackerService
            .updateJobTimeoutTrackerState(jobTimeoutTracker.getId(), jobTimeoutTracker.getJobId(),
                JobTimeoutTrackerStateEnum.TIMEOUT_HANDLED_FAILED.getState(), new Date(),
                jobTimeoutHandleFailedDetails, handleTimeoutFailedCount, stateList,
                jobTimeoutTracker.getDataVersion());
      }
    }

  }

}
