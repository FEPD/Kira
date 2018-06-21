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
import com.yihaodian.architecture.kira.client.internal.iface.ITriggerRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRoleEnum;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientUtils;
import com.yihaodian.architecture.kira.client.internal.util.RegisterStatusEnum;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.TriggerMetadataDetail;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class TriggerRegisterContextDataHandler implements ITriggerRegisterContextDataHandler {

  private static final int DEFAULT_COREPOOLSIZE = 0;
  private static final int DEFAULT_MAXIMUMPOOLSIZE = 1;
  private static final long DEFAULT_KEEPALIVETIME = 60L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  private static Logger logger = LoggerFactory.getLogger(TriggerRegisterContextDataHandler.class);
  private static ITriggerRegisterContextDataHandler triggerRegisterContextDataHandler;

  private ExecutorService triggerRegisterContextDataHandleExecutor;

  private ScheduledExecutorService triggerRegisterContextDataMonitorScheduledExecutorService;

  private TriggerRegisterContextDataHandler() {
  }

  public static synchronized ITriggerRegisterContextDataHandler getTriggerRegisterContextDataHandler() {
    if (null == triggerRegisterContextDataHandler) {
      triggerRegisterContextDataHandler = new TriggerRegisterContextDataHandler();
      triggerRegisterContextDataHandler.init();
    }
    return triggerRegisterContextDataHandler;
  }

  public static synchronized ITriggerRegisterContextDataHandler getTriggerRegisterContextDataHandlerInstance() {
    return triggerRegisterContextDataHandler;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleTriggerRegisterContextData(
      TriggerRegisterContextData triggerRegisterContextData) {
    TriggerRegisterContextDataHandleTask triggerRegisterContextDataHandleTask = new TriggerRegisterContextDataHandleTask(
        triggerRegisterContextData);
    if (null != triggerRegisterContextDataHandleExecutor) {
      triggerRegisterContextDataHandleExecutor.submit(triggerRegisterContextDataHandleTask);
    }
  }

  @Override
  public void init() {
    try {
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
      triggerRegisterContextDataHandleExecutor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE,
          DEFAULT_MAXIMUMPOOLSIZE, DEFAULT_KEEPALIVETIME, DEFAULT_UNIT, queue,
          new TriggerRegisterContextDataHandlerThreadFactory());

      triggerRegisterContextDataMonitorScheduledExecutorService = Executors
          .newSingleThreadScheduledExecutor(new TriggerRegisterContextDataMonitorThreadFactory());
      //scheduled to run every 2 minute after 1 minute's delay
      triggerRegisterContextDataMonitorScheduledExecutorService
          .scheduleAtFixedRate(new TriggerRegisterContextDataMonitorTask(), 1L, 2L,
              TimeUnit.MINUTES);
    } catch (Exception e) {
      logger.error("Error occurs when initialize TriggerRegisterContextDataHandler.", e);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != triggerRegisterContextDataHandleExecutor) {
        triggerRegisterContextDataHandleExecutor.shutdown();
        triggerRegisterContextDataHandleExecutor = null;
      }
      if (null != triggerRegisterContextDataMonitorScheduledExecutorService) {
        triggerRegisterContextDataMonitorScheduledExecutorService.shutdown();
        triggerRegisterContextDataMonitorScheduledExecutorService = null;
      }
    } catch (Exception e) {
      logger.error("Error occurs when destroy TriggerRegisterContextDataHandler.", e);
    }
  }

  private static class TriggerRegisterContextDataHandlerThreadFactory implements ThreadFactory {

    final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX
          + "TriggerRegisterContextDataHandler worker thread-" + threadNumber.getAndIncrement());
      thread.setDaemon(false);
      return thread;
    }
  }

  private static class TriggerRegisterContextDataHandleTask implements Runnable {

    private static Logger logger = LoggerFactory
        .getLogger(TriggerRegisterContextDataHandleTask.class);
    private TriggerRegisterContextData triggerRegisterContextData;

    public TriggerRegisterContextDataHandleTask(
        TriggerRegisterContextData triggerRegisterContextData) {
      this.triggerRegisterContextData = triggerRegisterContextData;
    }

    @Override
    public void run() {
      long startHandleTime = System.currentTimeMillis();
      RegisterStatusEnum registerStatus = RegisterStatusEnum.REGISTERING;
      try {
        if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
          TriggerMetadataDetail triggerMetaDataDetail = triggerRegisterContextData
              .getTriggerMetaDataDetail();
          TriggerMetadataZNodeData triggerMetaDataZNodeData = new TriggerMetadataZNodeData();
          BeanUtils.copyProperties(triggerMetaDataDetail, triggerMetaDataZNodeData);
          triggerMetaDataZNodeData.getOtherDataMap().put("createPoolZNodeForTrigger", Boolean.TRUE);
          boolean isNeedToWriteTriggerZnodeData = KiraCommonUtils
              .isNeedToWriteTriggerZnodeData(triggerMetaDataZNodeData);
          if (isNeedToWriteTriggerZnodeData) {
            KiraCommonUtils.createOrUpdateTriggerZNode(triggerMetaDataZNodeData, true);
          }
          KiraClientUtils
              .createOrUpdateEnvironmentZNodeForTriggerIfNeeded(triggerMetaDataZNodeData);
          //ccjtodo: phase2, need to notify child process to change the register status asynchronously.
          registerStatus = RegisterStatusEnum.REGISTER_SUCCESS;
        } else if (KiraClientRoleEnum.CHILD_PROCESS
            .equals(KiraClientDataCenter.getKiraClientRole())) {
          //ccjtodo: phase2, need to send trigger meta data to manager to register
        }
      } catch (Exception e) {
        registerStatus = RegisterStatusEnum.REGISTER_FAILED;
        logger.error("Error occurs for TriggerRegisterContextDataHandleTask.", e);
      } finally {
        triggerRegisterContextData.setRegisterStatus(registerStatus);
        KiraClientDataCenter.addTriggerRegisterContextData(triggerRegisterContextData);
        TriggerDeleteHandler.getTriggerDeleteHandler()
            .tryToCalculateTriggersToBeDeletedForLocalEnvironmentAndHandle();
        if (logger.isDebugEnabled()) {
          long handleCostTime = System.currentTimeMillis() - startHandleTime;
          logger.debug("It takes " + handleCostTime
              + " milliseconds to for TriggerRegisterContextDataHandleTask. triggerRegisterContextData="
              + JSONObject.toJSONString(triggerRegisterContextData));
        }
      }
    }
  }

  private static class TriggerRegisterContextDataMonitorThreadFactory implements ThreadFactory {

    final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX
          + "TriggerRegisterContextDataMonitor worker thread-" + threadNumber.getAndIncrement());
      thread.setDaemon(false);
      return thread;
    }
  }

  private static class TriggerRegisterContextDataMonitorTask implements Runnable {

    private static Logger logger = LoggerFactory
        .getLogger(TriggerRegisterContextDataMonitorTask.class);

    @Override
    public void run() {
      try {
        ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> triggerIdentityEnvironmentTriggerContextDataMap = KiraClientDataCenter
            .getTriggerIdentityEnvironmentTriggerContextDataMap();
        for (Entry<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> entry : triggerIdentityEnvironmentTriggerContextDataMap
            .entrySet()) {
          ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = entry
              .getValue();
          for (Entry<IEnvironment, TriggerRegisterContextData> theEntry : environmentTriggerContextDataMap
              .entrySet()) {
            TriggerRegisterContextData triggerRegisterContextData = theEntry.getValue();
            RegisterStatusEnum registerStatus = triggerRegisterContextData.getRegisterStatus();
            switch (registerStatus) {
              case UNREGISTERED:
              case REGISTER_FAILED:
                TriggerRegisterContextDataHandler.getTriggerRegisterContextDataHandler()
                    .handleTriggerRegisterContextData(triggerRegisterContextData);
                break;

              default:
                break;
            }
          }
        }
      } catch (Exception e) {
        logger.error("Error occurs when running TriggerRegisterContextDataMonitorTask.", e);
      }
    }

  }
}
