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
package com.yihaodian.architecture.kira.manager.health.monitor;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.manager.health.monitor.task.ClusterInternalConnectionMonitorTask;
import com.yihaodian.architecture.kira.manager.health.monitor.task.CreateAndRunJobForTimerTriggerMonitorTask;
import com.yihaodian.architecture.kira.manager.health.monitor.task.DBForMenuMonitorTask;
import com.yihaodian.architecture.kira.manager.health.monitor.task.DBForScheduleMonitorTask;
import com.yihaodian.architecture.kira.manager.health.monitor.task.ExternalOverallMonitorForTimerTriggerTask;
import com.yihaodian.architecture.kira.manager.health.monitor.task.ZKForKiraMonitorTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class can not be restarted.
 */
public class KiraManagerHealthMonitor extends ComponentAdaptor {

  private static Logger logger = LoggerFactory.getLogger(KiraManagerHealthMonitor.class);

  private static KiraManagerHealthMonitor kiraManagerHealthMonitor;
  private ScheduledExecutorService doKiraManagerHealthMonitorScheduledExecutorService;

  private KiraManagerHealthMonitor() throws Exception {
    this.init();
    this.start();
  }

  public static synchronized KiraManagerHealthMonitor getKiraManagerHealthMonitor()
      throws Exception {
    if (null == KiraManagerHealthMonitor.kiraManagerHealthMonitor) {
      KiraManagerHealthMonitor.kiraManagerHealthMonitor = new KiraManagerHealthMonitor();
    }

    return KiraManagerHealthMonitor.kiraManagerHealthMonitor;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  private void init() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Initializing KiraManagerHealthMonitor...");

      this.prepareDoKiraManagerHealthMonitorScheduledExecutorService();
      logger.info("Successfully initialize KiraManagerHealthMonitor.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraManagerHealthMonitor.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraManagerHealthMonitor. And it takes " + costTime
          + " milliseconds.");
    }
  }

  private void prepareDoKiraManagerHealthMonitorScheduledExecutorService() throws Exception {
    ThreadFactory threadFactory = new CustomizedThreadFactory(
        "KiraManagerHealthMonitor-doKiraManagerHealthMonitorScheduledExecutorService-");

    int monitorTaskCount = 8;

    int corePoolSize = monitorTaskCount;
    this.doKiraManagerHealthMonitorScheduledExecutorService = Executors
        .newScheduledThreadPool(corePoolSize, threadFactory);

    long initialDelayInSeconds = 60L;
    long triggerPeriodInSeconds = 60L;
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new ZKForKiraMonitorTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new DBForScheduleMonitorTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new DBForMenuMonitorTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new ClusterInternalConnectionMonitorTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);

    initialDelayInSeconds = 600L; //It will take a long time for migration when boot up, So set it to 10 minutes. Need to be set back to 300 seconds in the future
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new CreateAndRunJobForTimerTriggerMonitorTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);
    this.doKiraManagerHealthMonitorScheduledExecutorService
        .scheduleAtFixedRate(new ExternalOverallMonitorForTimerTriggerTask(), initialDelayInSeconds,
            triggerPeriodInSeconds, TimeUnit.SECONDS);
  }

  @Override
  protected void doDestroy() {
    super.doDestroy();

    this.destroyDoKiraManagerHealthMonitorScheduledExecutorService();
  }

  private void destroyDoKiraManagerHealthMonitorScheduledExecutorService() {
    if (null != doKiraManagerHealthMonitorScheduledExecutorService) {
      this.doKiraManagerHealthMonitorScheduledExecutorService.shutdown();
      this.doKiraManagerHealthMonitorScheduledExecutorService = null;
    }
  }

}
