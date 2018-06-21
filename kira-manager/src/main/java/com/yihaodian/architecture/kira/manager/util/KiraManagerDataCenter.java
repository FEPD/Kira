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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.manager.core.KiraManagerCoreBootstrap;
import com.yihaodian.architecture.kira.manager.core.metadata.kiraclient.IKiraClientMetadataManager;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.IKiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.scheduler.IKiraTimerTriggerLocalScheduler;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.jms.impl.JobItemStatusReportMessageHandler;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.QrtzFiredTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzSimpleTriggersService;
import com.yihaodian.architecture.kira.manager.service.QrtzTriggersService;
import com.yihaodian.architecture.kira.manager.service.TimerTriggerScheduleService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.service.UpgradeRoadmapService;
import com.yihaodian.architecture.kira.server.IKiraServer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerDataCenter {

  public volatile static byte[] excelFileByteDataForTriggersPredictReport = null;
  private static Logger logger = LoggerFactory.getLogger(KiraManagerDataCenter.class);
  private static List<Operation> allNotReadonlyOperations = new ArrayList<Operation>();
  private static Map<String, Operation> nameNotReadonlyOperationMap = new LinkedHashMap<String, Operation>();
  private static ConcurrentHashMap<String, List<String>> poolIdLastSelectedServiceUrlsToRunJobMap = new ConcurrentHashMap<String, List<String>>();

  private static Date serverBirthTime;

  private static String serverId;

  private static KiraManagerBootstrap kiraManagerBootstrap;

  private static KiraManagerCoreBootstrap kiraManagerCoreBootstrap;

  private static JobItemStatusReportMessageHandler jobItemStatusReportMessageHandler;

  private static KiraClientMetadataService kiraClientMetadataService;

  private static TriggerMetadataService triggerMetadataService;

  private static UpgradeRoadmapService upgradeRoadmapService;

  private static QrtzTriggersService qrtzTriggersService;

  private static QrtzSimpleTriggersService qrtzSimpleTriggersService;

  private static QrtzFiredTriggersService qrtzFiredTriggersService;

  private static TimerTriggerScheduleService timerTriggerScheduleService;

  private static IKiraServer kiraServer;

  private static IKiraClientMetadataManager kiraClientMetadataManager;

  private static IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager;

  private static IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter;

  private static IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler;

  private static VelocityUtils velocityUtils;

  private static volatile boolean recoverForTimerTriggerEnabled = true;

  public KiraManagerDataCenter() {
    // TODO Auto-generated constructor stub
  }

  public static List<Operation> getAllNotReadonlyOperations() {
    return KiraManagerDataCenter.allNotReadonlyOperations;
  }

  public static void setAllNotReadonlyOperations(
      List<Operation> allNotReadonlyOperations) {
    KiraManagerDataCenter.allNotReadonlyOperations = allNotReadonlyOperations;
  }

  public static Map<String, Operation> getNameNotReadonlyOperationMap() {
    return KiraManagerDataCenter.nameNotReadonlyOperationMap;
  }

  public static void setNameNotReadonlyOperationMap(
      Map<String, Operation> nameNotReadonlyOperationMap) {
    KiraManagerDataCenter.nameNotReadonlyOperationMap = nameNotReadonlyOperationMap;
  }

  public static List<String> getLastSelectedServiceUrlsToRunJobByPoolId(String appId) {
    return KiraManagerDataCenter.poolIdLastSelectedServiceUrlsToRunJobMap.get(appId);
  }

  public static void setLastSelectedServiceUrlsToRunJobForPool(String appId,
      List<String> lastSelectedServiceUrlsToRunJob) {
    KiraManagerDataCenter.poolIdLastSelectedServiceUrlsToRunJobMap
        .put(appId, lastSelectedServiceUrlsToRunJob);
  }

  public static ConcurrentHashMap<String, List<String>> getPoolIdLastSelectedServiceUrlsToRunJobMap() {
    return KiraManagerDataCenter.poolIdLastSelectedServiceUrlsToRunJobMap;
  }

  public static void setPoolIdLastSelectedServiceUrlsToRunJobMap(
      ConcurrentHashMap<String, List<String>> poolIdLastSelectedServiceUrlsToRunJobMap) {
    KiraManagerDataCenter.poolIdLastSelectedServiceUrlsToRunJobMap = poolIdLastSelectedServiceUrlsToRunJobMap;
  }

  public static Date getServerBirthTime() {
    return serverBirthTime;
  }

  public static void setServerBirthTime(Date serverBirthTime) {
    KiraManagerDataCenter.serverBirthTime = serverBirthTime;
  }

  public static String getServerId() {
    return serverId;
  }

  public static void setServerId(String serverId) {
    KiraManagerDataCenter.serverId = serverId;
  }

  public static KiraManagerBootstrap getKiraManagerBootstrap() {
    if (null == KiraManagerDataCenter.kiraManagerBootstrap) {
      KiraManagerDataCenter.kiraManagerBootstrap = (KiraManagerBootstrap) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_KIRAMANAGERBOOTSTRAP);
    }
    return KiraManagerDataCenter.kiraManagerBootstrap;
  }

  public static void setKiraManagerBootstrap(
      KiraManagerBootstrap kiraManagerBootstrap) {
    KiraManagerDataCenter.kiraManagerBootstrap = kiraManagerBootstrap;
  }

  public static KiraManagerCoreBootstrap getKiraManagerCoreBootstrap() throws Exception {
    KiraManagerDataCenter.waitForKiraManagerCoreBootstrapReady();
    return KiraManagerDataCenter.kiraManagerCoreBootstrap;
  }

  public static void setKiraManagerCoreBootstrap(
      KiraManagerCoreBootstrap kiraManagerCoreBootstrap) {
    KiraManagerDataCenter.kiraManagerCoreBootstrap = kiraManagerCoreBootstrap;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraManagerCoreBootstrapReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraManagerCoreBootstrap)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraManagerCoreBootstrapReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraManagerCoreBootstrap) {
        throw new RuntimeException(
            "The kiraManagerCoreBootstrap must be initialized for using kira-manager.");
      }
    }
  }

  public static JobItemStatusReportMessageHandler getJobItemStatusReportMessageHandler() {
    if (null == KiraManagerDataCenter.jobItemStatusReportMessageHandler) {
      KiraManagerDataCenter.jobItemStatusReportMessageHandler = (JobItemStatusReportMessageHandler) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_JOBITEMSTATUSREPORTMESSAGEHANDLER);
    }
    return KiraManagerDataCenter.jobItemStatusReportMessageHandler;
  }

  public static void setJobItemStatusReportMessageHandler(
      JobItemStatusReportMessageHandler jobItemStatusReportMessageHandler) {
    KiraManagerDataCenter.jobItemStatusReportMessageHandler = jobItemStatusReportMessageHandler;
  }

  public static KiraClientMetadataService getKiraClientMetadataService() {
    if (null == KiraManagerDataCenter.kiraClientMetadataService) {
      KiraManagerDataCenter.kiraClientMetadataService = (KiraClientMetadataService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_KIRACLIENTMETADATASERVICE);
    }
    return KiraManagerDataCenter.kiraClientMetadataService;
  }

  public static void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    KiraManagerDataCenter.kiraClientMetadataService = kiraClientMetadataService;
  }

  public static TriggerMetadataService getTriggerMetadataService() {
    if (null == KiraManagerDataCenter.triggerMetadataService) {
      KiraManagerDataCenter.triggerMetadataService = (TriggerMetadataService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_TRIGGERMETADATASERVICE);
    }
    return KiraManagerDataCenter.triggerMetadataService;
  }

  public static void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    KiraManagerDataCenter.triggerMetadataService = triggerMetadataService;
  }

  public static UpgradeRoadmapService getUpgradeRoadmapService() {
    if (null == KiraManagerDataCenter.upgradeRoadmapService) {
      KiraManagerDataCenter.upgradeRoadmapService = (UpgradeRoadmapService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_UPGRADEROADMAPSERVICE);
    }
    return KiraManagerDataCenter.upgradeRoadmapService;
  }

  public static void setUpgradeRoadmapService(
      UpgradeRoadmapService upgradeRoadmapService) {
    KiraManagerDataCenter.upgradeRoadmapService = upgradeRoadmapService;
  }

  public static QrtzTriggersService getQrtzTriggersService() {
    if (null == KiraManagerDataCenter.qrtzTriggersService) {
      KiraManagerDataCenter.qrtzTriggersService = (QrtzTriggersService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_QRTZTRIGGERSSERVICE);
    }
    return KiraManagerDataCenter.qrtzTriggersService;
  }

  public static void setQrtzTriggersService(
      QrtzTriggersService qrtzTriggersService) {
    KiraManagerDataCenter.qrtzTriggersService = qrtzTriggersService;
  }

  public static QrtzSimpleTriggersService getQrtzSimpleTriggersService() {
    if (null == KiraManagerDataCenter.qrtzSimpleTriggersService) {
      KiraManagerDataCenter.qrtzSimpleTriggersService = (QrtzSimpleTriggersService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_QRTZSIMPLETRIGGERSSERVICE);
    }
    return KiraManagerDataCenter.qrtzSimpleTriggersService;
  }

  public static void setQrtzSimpleTriggersService(
      QrtzSimpleTriggersService qrtzSimpleTriggersService) {
    KiraManagerDataCenter.qrtzSimpleTriggersService = qrtzSimpleTriggersService;
  }

  public static QrtzFiredTriggersService getQrtzFiredTriggersService() {
    if (null == KiraManagerDataCenter.qrtzFiredTriggersService) {
      KiraManagerDataCenter.qrtzFiredTriggersService = (QrtzFiredTriggersService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_QRTZFIREDTRIGGERSSERVICE);
    }
    return KiraManagerDataCenter.qrtzFiredTriggersService;
  }

  public static void setQrtzFiredTriggersService(
      QrtzFiredTriggersService qrtzFiredTriggersService) {
    KiraManagerDataCenter.qrtzFiredTriggersService = qrtzFiredTriggersService;
  }

  public static TimerTriggerScheduleService getTimerTriggerScheduleService() {
    if (null == KiraManagerDataCenter.timerTriggerScheduleService) {
      KiraManagerDataCenter.timerTriggerScheduleService = (TimerTriggerScheduleService) SpringBeanUtils
          .getBean(KiraManagerConstants.SPRING_BEAN_NAME_TIMERTRIGGERSCHEDULESERVICE);
    }
    return KiraManagerDataCenter.timerTriggerScheduleService;
  }

  public static void setTimerTriggerScheduleService(
      TimerTriggerScheduleService timerTriggerScheduleService) {
    KiraManagerDataCenter.timerTriggerScheduleService = timerTriggerScheduleService;
  }

  public static IKiraServer getKiraServer() throws Exception {
    KiraManagerDataCenter.waitForKiraServerReady();
    return KiraManagerDataCenter.kiraServer;
  }

  public static void setKiraServer(IKiraServer kiraServer) {
    KiraManagerDataCenter.kiraServer = kiraServer;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraServerReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraServer)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraServerReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraServer) {
        throw new RuntimeException("The KiraServer must be initialized for using kira-manager.");
      }
    }
  }

  public static IKiraClientMetadataManager getKiraClientMetadataManager() throws Exception {
    KiraManagerDataCenter.waitForKiraClientMetadataManagerReady();
    return KiraManagerDataCenter.kiraClientMetadataManager;
  }

  public static void setKiraClientMetadataManager(
      IKiraClientMetadataManager kiraClientMetadataManager) {
    KiraManagerDataCenter.kiraClientMetadataManager = kiraClientMetadataManager;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraClientMetadataManagerReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraClientMetadataManager)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraClientMetadataManagerReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraClientMetadataManager) {
        throw new RuntimeException(
            "The kiraClientMetadataManager must be initialized for using kira-manager.");
      }
    }
  }

  public static IKiraTimerTriggerMetadataManager getKiraTimerTriggerMetadataManager()
      throws Exception {
    KiraManagerDataCenter.waitForKiraTimerTriggerMetadataManagerReady();
    return KiraManagerDataCenter.kiraTimerTriggerMetadataManager;
  }

  public static void setKiraTimerTriggerMetadataManager(
      IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager) {
    KiraManagerDataCenter.kiraTimerTriggerMetadataManager = kiraTimerTriggerMetadataManager;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraTimerTriggerMetadataManagerReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraTimerTriggerMetadataManager) && waittime < (
          KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND * 2)) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraTimerTriggerMetadataManagerReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraTimerTriggerMetadataManager) {
        throw new RuntimeException(
            "The kiraTimerTriggerMetadataManager must be initialized for using kira-manager.");
      }
    }
  }

  public static IKiraTimerTriggerScheduleCenter getKiraTimerTriggerScheduleCenter()
      throws Exception {
    KiraManagerDataCenter.waitForKiraTimerTriggerScheduleCenterReady();
    return KiraManagerDataCenter.kiraTimerTriggerScheduleCenter;
  }

  public static void setKiraTimerTriggerScheduleCenter(
      IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter) {
    KiraManagerDataCenter.kiraTimerTriggerScheduleCenter = kiraTimerTriggerScheduleCenter;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraTimerTriggerScheduleCenterReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraTimerTriggerScheduleCenter) && waittime < (
          KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND * 2)) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraTimerTriggerScheduleCenterReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraTimerTriggerScheduleCenter) {
        throw new RuntimeException(
            "The kiraTimerTriggerScheduleCenter must be initialized for using kira-manager.");
      }
    }
  }

  public static IKiraTimerTriggerLocalScheduler getKiraTimerTriggerLocalScheduler()
      throws Exception {
    KiraManagerDataCenter.waitForKiraTimerTriggerLocalSchedulerReady();
    return KiraManagerDataCenter.kiraTimerTriggerLocalScheduler;
  }

  public static void setKiraTimerTriggerLocalScheduler(
      IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler) {
    KiraManagerDataCenter.kiraTimerTriggerLocalScheduler = kiraTimerTriggerLocalScheduler;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForKiraTimerTriggerLocalSchedulerReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.kiraTimerTriggerLocalScheduler)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForKiraTimerTriggerLocalSchedulerReady.");
    } finally {
      if (null == KiraManagerDataCenter.kiraTimerTriggerLocalScheduler) {
        throw new RuntimeException(
            "The kiraTimerTriggerLocalScheduler must be initialized for using kira-manager.");
      }
    }
  }

  public static VelocityUtils getVelocityUtils() throws Exception {
    KiraManagerDataCenter.waitForVelocityUtilsReady();
    return KiraManagerDataCenter.velocityUtils;
  }

  public static void setVelocityUtils(VelocityUtils velocityUtils) {
    KiraManagerDataCenter.velocityUtils = velocityUtils;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForVelocityUtilsReady() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraManagerDataCenter.velocityUtils)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForVelocityUtilsReady.");
    } finally {
      if (null == KiraManagerDataCenter.velocityUtils) {
        throw new RuntimeException("The velocityUtils must be initialized for using kira-manager.");
      }
    }
  }

  public static boolean isRecoverForTimerTriggerEnabled() {
    return recoverForTimerTriggerEnabled;
  }

  public static void setRecoverForTimerTriggerEnabled(
      boolean recoverForTimerTriggerEnabled) {
    KiraManagerDataCenter.recoverForTimerTriggerEnabled = recoverForTimerTriggerEnabled;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
