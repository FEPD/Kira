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
package com.yihaodian.architecture.kira.client.internal.impl;

import com.alibaba.fastjson.JSONObject;
import com.yihaodian.architecture.kira.client.akka.ProducerMessageActor;
import com.yihaodian.architecture.kira.client.internal.iface.IKiraClientRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRegisterContextData;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRoleEnum;
import com.yihaodian.architecture.kira.client.internal.util.RegisterStatusEnum;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class KiraClientRegisterContextDataHandler implements
    IKiraClientRegisterContextDataHandler {

  private static final int DEFAULT_COREPOOLSIZE = 0;
  private static final int DEFAULT_MAXIMUMPOOLSIZE = 1;
  private static final long DEFAULT_KEEPALIVETIME = 60L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  private static Logger logger = LoggerFactory
      .getLogger(KiraClientRegisterContextDataHandler.class);
  private static IKiraClientRegisterContextDataHandler kiraClientRegisterContextDataHandler;

  private ExecutorService kiraClientRegisterContextDataHandleExecutor;

  private ScheduledExecutorService kiraClientRegisterContextDataMonitorScheduledExecutorService;

  private KiraClientRegisterContextDataHandler() {
  }

  public static synchronized IKiraClientRegisterContextDataHandler getKiraClientRegisterContextDataHandler() {
    if (null == kiraClientRegisterContextDataHandler) {
      kiraClientRegisterContextDataHandler = new KiraClientRegisterContextDataHandler();
      kiraClientRegisterContextDataHandler.init();
    }
    return kiraClientRegisterContextDataHandler;
  }

  public static synchronized IKiraClientRegisterContextDataHandler getKiraClientRegisterContextDataHandlerInstance() {
    return kiraClientRegisterContextDataHandler;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    try {
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX
              + "KiraClientRegisterContextDataHandler worker thread-");
      threadFactory.setDaemon(false);
      kiraClientRegisterContextDataHandleExecutor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE,
          DEFAULT_MAXIMUMPOOLSIZE, DEFAULT_KEEPALIVETIME, DEFAULT_UNIT, queue, threadFactory);

      CustomizableThreadFactory threadFactoryForMonitor = new CustomizableThreadFactory(
          KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX
              + "KiraClientRegisterContextDataMonitor worker thread-");
      threadFactoryForMonitor.setDaemon(false);
      kiraClientRegisterContextDataMonitorScheduledExecutorService = Executors
          .newSingleThreadScheduledExecutor(threadFactoryForMonitor);
      //scheduled to run every 2 minute after 1 minute's delay
      kiraClientRegisterContextDataMonitorScheduledExecutorService
          .scheduleAtFixedRate(new KiraClientRegisterContextDataMonitorTask(), 1L, 2L,
              TimeUnit.MINUTES);
    } catch (Exception e) {
      logger.error("Error occurs when initialize KiraClientRegisterContextDataHandler.", e);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != kiraClientRegisterContextDataHandleExecutor) {
        kiraClientRegisterContextDataHandleExecutor.shutdown();
        kiraClientRegisterContextDataHandleExecutor = null;
      }
      if (null != kiraClientRegisterContextDataMonitorScheduledExecutorService) {
        kiraClientRegisterContextDataMonitorScheduledExecutorService.shutdown();
        kiraClientRegisterContextDataMonitorScheduledExecutorService = null;
      }
    } catch (Exception e) {
      logger.error("Error occurs when destroy KiraClientRegisterContextDataHandler.", e);
    }
  }

  @Override
  public void handleKiraClientRegisterContextData(
      KiraClientRegisterContextData kiraClientRegisterContextData) {
    KiraClientRegisterContextDataHandleTask kiraClientRegisterContextDataHandleTask = new KiraClientRegisterContextDataHandleTask(
        kiraClientRegisterContextData);
    if (null != kiraClientRegisterContextDataHandleExecutor) {
      kiraClientRegisterContextDataHandleExecutor.submit(kiraClientRegisterContextDataHandleTask);
    }
  }

  private static class KiraClientRegisterContextDataHandleTask implements Runnable {

    private static Logger logger = LoggerFactory
        .getLogger(KiraClientRegisterContextDataHandleTask.class);
    private KiraClientRegisterContextData kiraClientRegisterContextData;

    public KiraClientRegisterContextDataHandleTask(
        KiraClientRegisterContextData kiraClientRegisterContextData) {
      super();
      this.kiraClientRegisterContextData = kiraClientRegisterContextData;
    }

    @Override
    public void run() {
      long startHandleTime = System.currentTimeMillis();
      RegisterStatusEnum registerStatus = RegisterStatusEnum.REGISTERING;
      try {
        if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
          KiraClientRegisterData kiraClientRegisterData = kiraClientRegisterContextData
              .getKiraClientRegisterData();
          //JumperMessageSender.sendKiraClientRegisterData(kiraClientRegisterData);
          ProducerMessageActor
              .clientSendJobStatus(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA,
                  kiraClientRegisterData);
          //ccjtodo: phase2, need to notify child process to change the register status asynchronously.
          registerStatus = RegisterStatusEnum.REGISTER_SUCCESS;
        } else if (KiraClientRoleEnum.CHILD_PROCESS
            .equals(KiraClientDataCenter.getKiraClientRole())) {
          //ccjtodo: phase2, need to send kiraClientRegisterData to manager to register
        }
      } catch (Exception e) {
        registerStatus = RegisterStatusEnum.REGISTER_FAILED;
        logger.error("Error occurs for KiraClientRegisterContextDataHandleTask.", e);
      } finally {
        kiraClientRegisterContextData.setRegisterStatus(registerStatus);
        KiraClientDataCenter.addKiraClientRegisterContextData(kiraClientRegisterContextData);
        if (logger.isDebugEnabled()) {
          long handleCostTime = System.currentTimeMillis() - startHandleTime;
          logger.debug("It takes " + handleCostTime
              + " milliseconds to for KiraClientRegisterContextDataHandleTask. kiraClientRegisterContextData="
              + JSONObject.toJSONString(kiraClientRegisterContextData));
        }
      }
    }

  }

  private static class KiraClientRegisterContextDataMonitorTask implements Runnable {

    private static Logger logger = LoggerFactory
        .getLogger(KiraClientRegisterContextDataMonitorTask.class);

    @Override
    public void run() {
      try {
        ConcurrentMap<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>> poolIdEnvironmentKiraClientRegisterContextDataMap = KiraClientDataCenter
            .getPoolIdEnvironmentKiraClientRegisterContextDataMap();
        for (Entry<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>> entry : poolIdEnvironmentKiraClientRegisterContextDataMap
            .entrySet()) {
          ConcurrentMap<IEnvironment, KiraClientRegisterContextData> environmentKiraClientRegisterContextDataMap = entry
              .getValue();
          for (Entry<IEnvironment, KiraClientRegisterContextData> theEntry : environmentKiraClientRegisterContextDataMap
              .entrySet()) {
            KiraClientRegisterContextData kiraClientRegisterContextData = theEntry.getValue();
            RegisterStatusEnum registerStatus = kiraClientRegisterContextData.getRegisterStatus();
            switch (registerStatus) {
              case UNREGISTERED:
              case REGISTER_FAILED:
                KiraClientRegisterContextDataHandler.getKiraClientRegisterContextDataHandler()
                    .handleKiraClientRegisterContextData(kiraClientRegisterContextData);
                break;

              default:
                break;
            }
          }
        }
      } catch (Exception e) {
        logger.error("Error occurs when running KiraClientRegisterContextDataMonitorTask.", e);
      }
    }
  }

}
