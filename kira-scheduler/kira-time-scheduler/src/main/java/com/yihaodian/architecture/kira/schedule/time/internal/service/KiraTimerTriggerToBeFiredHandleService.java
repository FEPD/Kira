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
package com.yihaodian.architecture.kira.schedule.time.internal.service;

import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.schedule.time.internal.service.helper.KiraTimerTriggerToBeFiredHandleServiceCallerRunsPolicy;
import com.yihaodian.architecture.kira.schedule.time.internal.service.helper.KiraTimerTriggerToBeFiredHandleTask;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.ITimer;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraTimerTriggerToBeFiredHandleService implements
    ITimerTriggerToBeFiredHandleService {

  public static final int DEFAULT_MAX_POOL_SIZE = 1000;
  private static final long DEFAULT_KEEPALIVETIME_INSECONDS = 60L;
  private static final int DEFAULT_QUEUECAPACITY = 0;
  private static Logger logger = LoggerFactory
      .getLogger(KiraTimerTriggerToBeFiredHandleService.class);
  private final String timerTriggerSchedulerId;
  protected ThreadPoolExecutor threadPoolExecutor;
  private int corePoolSize = 1;
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
  private long KeepAliveTimeInSeconds = 60L;
  private int queueCapacity = DEFAULT_QUEUECAPACITY;

  public KiraTimerTriggerToBeFiredHandleService(String timerTriggerSchedulerId, int corePoolSize) {
    this(timerTriggerSchedulerId, corePoolSize, DEFAULT_MAX_POOL_SIZE,
        DEFAULT_KEEPALIVETIME_INSECONDS, DEFAULT_QUEUECAPACITY);
  }

  public KiraTimerTriggerToBeFiredHandleService(String timerTriggerSchedulerId, int corePoolSize,
      int maxPoolSize, long KeepAliveTimeInSeconds, int queueCapacity) {
    this.timerTriggerSchedulerId = timerTriggerSchedulerId;
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
    this.KeepAliveTimeInSeconds = KeepAliveTimeInSeconds;
    this.queueCapacity = queueCapacity;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void start() throws Exception {
    long startHandleTime = System.currentTimeMillis();
    try {
      logger.info("KiraTimerTriggerToBeFiredHandleService is starting... timerTriggerSchedulerId="
          + timerTriggerSchedulerId);
      if (null != threadPoolExecutor) {
        threadPoolExecutor.shutdown();
      }
      String threadNamePrefix =
          timerTriggerSchedulerId + "-KiraTimerTriggerToBeFiredHandleService-";
      CustomizedThreadFactory customizedThreadFactory = new CustomizedThreadFactory(
          threadNamePrefix);
      BlockingQueue<Runnable> queue = null;
      if (queueCapacity > 0) {
        queue = new LinkedBlockingQueue<Runnable>(queueCapacity);
      } else {
        queue = new SynchronousQueue<Runnable>(true);
      }
      KiraTimerTriggerToBeFiredHandleServiceCallerRunsPolicy kiraTimerTriggerToBeFiredHandleServiceCallerRunsPolicy = new KiraTimerTriggerToBeFiredHandleServiceCallerRunsPolicy();
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, KeepAliveTimeInSeconds,
          TimeUnit.SECONDS, queue, customizedThreadFactory,
          kiraTimerTriggerToBeFiredHandleServiceCallerRunsPolicy);
    } catch (Exception e) {
      logger.error(
          "Error occurs when call start() for KiraTimerTriggerToBeFiredHandleService=" + this, e);
      throw e;
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info("It takes " + handleCostTime
          + " milliseconds to start KiraTimerTriggerToBeFiredHandleService=" + this
          .toDetailString());
    }
  }

  @Override
  public void shutdown() throws Exception {
    long startHandleTime = System.currentTimeMillis();
    try {
      logger.info(
          "KiraTimerTriggerToBeFiredHandleService is shutting down... timerTriggerSchedulerId="
              + timerTriggerSchedulerId);
      if (null != threadPoolExecutor) {
        threadPoolExecutor.shutdown();
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when call shutdown() for KiraTimerTriggerToBeFiredHandleService=" + this,
          e);
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info("It takes " + handleCostTime
          + " milliseconds to shutdown KiraTimerTriggerToBeFiredHandleService=" + this
          .toDetailString());
    }
  }

  @Override
  public void handleTimerTriggerToBeFired(ITimer timer, ITimerTrigger timerTrigger)
      throws Exception {
    boolean isUnscheduled = timerTrigger.isUnscheduled();
    if (isUnscheduled) {
      logger.info(
          "The trigger is unscheduled. So do not submit KiraTimerTriggerToBeFiredHandleTask. timerTrigger={} in timer={}",
          timerTrigger.getId(), timer.getId());
    } else {
      KiraTimerTriggerToBeFiredHandleTask kiraTimerTriggerToBeFiredHandleTask = new KiraTimerTriggerToBeFiredHandleTask(
          timer, timerTrigger);
      threadPoolExecutor.submit(kiraTimerTriggerToBeFiredHandleTask);
    }

  }

  public String toDetailString() {
    return "KiraTimerTriggerToBeFiredHandleService [timerTriggerSchedulerId="
        + timerTriggerSchedulerId
        + ", corePoolSize="
        + corePoolSize
        + ", maxPoolSize="
        + maxPoolSize
        + ", KeepAliveTimeInSeconds="
        + KeepAliveTimeInSeconds
        + ", queueCapacity="
        + queueCapacity
        + ", threadPoolExecutor=" + threadPoolExecutor + "]";
  }

  @Override
  public String toString() {
    return "KiraTimerTriggerToBeFiredHandleService [timerTriggerSchedulerId="
        + timerTriggerSchedulerId
        + ", corePoolSize="
        + corePoolSize
        + ", maxPoolSize="
        + maxPoolSize
        + ", KeepAliveTimeInSeconds="
        + KeepAliveTimeInSeconds
        + ", queueCapacity="
        + queueCapacity
        + ", threadPoolExecutor=" + threadPoolExecutor + "]";
  }
}
