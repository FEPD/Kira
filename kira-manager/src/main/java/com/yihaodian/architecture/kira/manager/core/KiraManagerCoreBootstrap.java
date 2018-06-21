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
package com.yihaodian.architecture.kira.manager.core;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.manager.core.metadata.kiraclient.IKiraClientMetadataManager;
import com.yihaodian.architecture.kira.manager.core.metadata.kiraclient.KiraClientMetadataManager;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.KiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.IKiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.KiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.scheduler.IKiraTimerTriggerLocalScheduler;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.scheduler.KiraTimerTriggerLocalScheduler;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerDataCenter;
import com.yihaodian.architecture.kira.manager.jms.impl.JobItemStatusReportMessageHandler;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.QrtzFiredTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzSimpleTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzTriggersService;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.service.UpgradeRoadmapService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.KiraServer;

public class KiraManagerCoreBootstrap extends ComponentAdaptor {

  private TriggerMetadataService triggerMetadataService;
  private KiraClientMetadataService kiraClientMetadataService;

  private UpgradeRoadmapService upgradeRoadmapService;
  private QrtzTriggersService qrtzTriggersService;
  private QrtzSimpleTriggersService qrtzSimpleTriggersService;
  private QrtzFiredTriggersService qrtzFiredTriggersService;
  private TimerTriggerScheduleService timerTriggerScheduleService;

  private JobItemStatusReportMessageHandler jobItemStatusReportMessageHandler;
  private IKiraServer kiraServer;
  private IKiraClientMetadataManager kiraClientMetadataManager;
  private IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager;
  private IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter;
  private IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler;

  public KiraManagerCoreBootstrap() throws Exception {
    init();
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

  }

  private void init() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Initializing KiraManagerCoreBootstrap...");

      this.jobItemStatusReportMessageHandler = KiraManagerDataCenter
          .getJobItemStatusReportMessageHandler();

      String hostIpForClusterInternalService = KiraServerDataCenter
          .getHostIpForClusterInternalService();
      Integer portForClusterInternalService = KiraServerDataCenter
          .getPortForClusterInternalService();
      String clusterInternalServiceUrl = KiraServerDataCenter.getClusterInternalServiceUrl();
      this.kiraServer = new KiraServer(hostIpForClusterInternalService,
          portForClusterInternalService, clusterInternalServiceUrl);

      KiraManagerDataCenter.setServerId(this.kiraServer.getServerId());

      this.kiraClientMetadataService = KiraManagerDataCenter.getKiraClientMetadataService();
      this.kiraClientMetadataManager = new KiraClientMetadataManager(this.kiraServer,
          this.kiraClientMetadataService);

      this.triggerMetadataService = KiraManagerDataCenter.getTriggerMetadataService();
      this.kiraTimerTriggerMetadataManager = new KiraTimerTriggerMetadataManager(this.kiraServer,
          this.triggerMetadataService);

      this.upgradeRoadmapService = KiraManagerDataCenter.getUpgradeRoadmapService();
      this.qrtzTriggersService = KiraManagerDataCenter.getQrtzTriggersService();
      this.qrtzSimpleTriggersService = KiraManagerDataCenter.getQrtzSimpleTriggersService();
      this.qrtzFiredTriggersService = KiraManagerDataCenter.getQrtzFiredTriggersService();
      this.timerTriggerScheduleService = KiraManagerDataCenter.getTimerTriggerScheduleService();
      this.kiraTimerTriggerScheduleCenter = new KiraTimerTriggerScheduleCenter(kiraServer,
          upgradeRoadmapService, qrtzTriggersService, qrtzSimpleTriggersService,
          qrtzFiredTriggersService, kiraClientMetadataService, triggerMetadataService,
          timerTriggerScheduleService);

      this.kiraTimerTriggerLocalScheduler = new KiraTimerTriggerLocalScheduler(kiraServer,
          kiraClientMetadataService, triggerMetadataService, timerTriggerScheduleService);

      logger.info("Successfully initialize KiraManagerCoreBootstrap.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraManagerCoreBootstrap.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraManagerCoreBootstrap. And it takes " + costTime
          + " milliseconds.");
    }
  }

  @Override
  public boolean doStart() {
    boolean returnValue = false;

    logger.info("Begin doStart in KiraManagerCoreBootstrap...");
    long startTime = System.currentTimeMillis();
    try {
			/*try {
				MessageReceiverRegister.registerJobItemStatusReportReceiver(this.jobItemStatusReportMessageHandler, true);
			} catch (Throwable t) {
				logger.error("Error occurs when registerJobItemStatusReportReceiver",t);
			}*/

      //Need to start kiraServer first . Then it will send start event to other components which will cause those components to start.
      this.kiraServer.start();
      KiraManagerDataCenter.setKiraServer(this.kiraServer);

      logger.info("Successfully doStart in KiraManagerCoreBootstrap.");

      returnValue = true;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish doStart in KiraManagerCoreBootstrap. And it takes " + costTime
          + " milliseconds.");
    }

    return returnValue;
  }

  @Override
  public void doShutdown() {
    logger.info("Begin doShutdown in KiraManagerCoreBootstrap...");
    long startTime = System.currentTimeMillis();
    try {
      //MessageReceiverRegister.unRegisterJobItemStatusReportReceiver();

      this.kiraClientMetadataManager.shutdown();

      this.kiraTimerTriggerMetadataManager.shutdown();

      this.kiraTimerTriggerScheduleCenter.shutdown();

      this.kiraTimerTriggerLocalScheduler.shutdown();

      //Need to shutdown kiraServer at last. So all other components can not do something wrong.
      this.kiraServer.shutdown();

      logger.info("Successfully doShutdown in KiraManagerCoreBootstrap.");
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish doShutdown in KiraManagerCoreBootstrap. And it takes " + costTime
          + " milliseconds.");
    }
  }

  public void waitForAllComponentsNotInShuttingdownState() {
    boolean isAllComponentsNotInShuttingdownState = false;

    Boolean kiraTimerTriggerMetadataManagerShuttingdown = null;
    Boolean kiraTimerTriggerScheduleCenterShuttingdown = null;
    Boolean kiraTimerTriggerLocalSchedulerShuttingdown = null;
    Boolean kiraClientMetadataManagerShuttingdown = null;
    Boolean kiraServerShuttingdown = null;
    try {
      int waittime = 0;
      while (!isAllComponentsNotInShuttingdownState
          && waittime < (KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND)) {
        Thread.sleep(100);
        waittime += 100;

        kiraServerShuttingdown = KiraManagerDataCenter.getKiraServer().isShuttingdown();
        kiraClientMetadataManagerShuttingdown = KiraManagerDataCenter.getKiraClientMetadataManager()
            .isShuttingdown();
        kiraTimerTriggerLocalSchedulerShuttingdown = KiraManagerDataCenter
            .getKiraTimerTriggerLocalScheduler().isShuttingdown();
        kiraTimerTriggerMetadataManagerShuttingdown = KiraManagerDataCenter
            .getKiraTimerTriggerMetadataManager().isShuttingdown();
        kiraTimerTriggerScheduleCenterShuttingdown = KiraManagerDataCenter
            .getKiraTimerTriggerScheduleCenter().isShuttingdown();

        if (!kiraTimerTriggerMetadataManagerShuttingdown
            && !kiraTimerTriggerScheduleCenterShuttingdown
            && !kiraTimerTriggerLocalSchedulerShuttingdown
            && !kiraClientMetadataManagerShuttingdown
            && !kiraServerShuttingdown) {
          isAllComponentsNotInShuttingdownState = true;
        }
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForAllComponentsNotInShuttingdownState.");
    } catch (Throwable t) {
      logger.error("Throwable occurs when waitForAllComponentsNotInShuttingdownState.", t);
    } finally {
      if (!isAllComponentsNotInShuttingdownState) {
        logger.warn(
            "isAllComponentsNotInShuttingdownState is false. So may some component is still in the shutting down state. "
                +
                "kiraTimerTriggerMetadataManagerShuttingdown={} " +
                "and kiraTimerTriggerScheduleCenterShuttingdown={} " +
                "and kiraTimerTriggerLocalSchedulerShuttingdown={} " +
                "and kiraClientMetadataManagerShuttingdown={} " +
                "and kiraServerShuttingdown={}", kiraTimerTriggerMetadataManagerShuttingdown,
            kiraTimerTriggerScheduleCenterShuttingdown, kiraTimerTriggerLocalSchedulerShuttingdown,
            kiraClientMetadataManagerShuttingdown, kiraServerShuttingdown);
      }
    }
  }

  @Override
  public void restart() {
    logger.info("Begin restart in KiraManagerCoreBootstrap...");
    long startTime = System.currentTimeMillis();
    try {
      this.shutdown();
      this.waitForAllComponentsNotInShuttingdownState();
      //alwarys to startKiraServer no matter what are the state of the components.
      this.start();

      logger.info("Successfully restart in KiraManagerCoreBootstrap.");
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish restart in KiraManagerCoreBootstrap. And it takes " + costTime
          + " milliseconds.");
    }
  }

  @Override
  public void doDestroy() {
    this.kiraClientMetadataManager.destroy();

    this.kiraTimerTriggerMetadataManager.destroy();

    this.kiraTimerTriggerScheduleCenter.destroy();

    this.kiraTimerTriggerLocalScheduler.destroy();

    //Need to destroy kiraServer at last. So all other components can not do something wrong.
    this.kiraServer.destroy();
  }

}
