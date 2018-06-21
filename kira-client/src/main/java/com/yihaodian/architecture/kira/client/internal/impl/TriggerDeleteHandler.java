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

import com.yihaodian.architecture.kira.client.internal.iface.ITriggerDeleteHandler;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientRoleEnum;
import com.yihaodian.architecture.kira.client.internal.util.RegisterStatusEnum;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;

public class TriggerDeleteHandler implements
    ITriggerDeleteHandler {

  private static Logger logger = LoggerFactory.getLogger(TriggerDeleteHandler.class);

  private static TriggerDeleteHandler triggerDeleteHandler;
  private ScheduledExecutorService triggerDeleteMonitorScheduledExecutorService;

  private TriggerDeleteHandler() {
    // TODO Auto-generated constructor stub
  }

  public static synchronized ITriggerDeleteHandler getTriggerDeleteHandler() {
    if (null == triggerDeleteHandler) {
      triggerDeleteHandler = new TriggerDeleteHandler();
      triggerDeleteHandler.init();
    }
    return triggerDeleteHandler;
  }

  public static synchronized ITriggerDeleteHandler getTriggerDeleteHandlerInstance() {
    return triggerDeleteHandler;
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
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX + "TriggerDeleteContextDataMonitor-");
      threadFactory.setDaemon(false);
      triggerDeleteMonitorScheduledExecutorService = Executors
          .newSingleThreadScheduledExecutor(threadFactory);
      //scheduled to run every 2 minute after 2 minute's delay to calculate and handle triggers which need to be deleted.
      triggerDeleteMonitorScheduledExecutorService
          .scheduleAtFixedRate(new TriggerDeleteMonitorTask(), 2L, 2L, TimeUnit.MINUTES);
    } catch (Exception e) {
      logger.error("Error occurs when initialize TriggerDeleteHandler.", e);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != triggerDeleteMonitorScheduledExecutorService) {
        triggerDeleteMonitorScheduledExecutorService.shutdown();
        triggerDeleteMonitorScheduledExecutorService = null;
      }
    } catch (Exception e) {
      logger.error("Error occurs when destroy TriggerDeleteHandler.", e);
    }
  }

  private void deleteTriggersByMainProcess(Set<TriggerIdentity> triggerIdentitys,
      IEnvironment environment) {
    if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      long startHandleTime = System.currentTimeMillis();
      try {
        if (null != triggerIdentitys) {
          for (TriggerIdentity triggerIdentity : triggerIdentitys) {
            try {
              KiraCommonUtils.deleteTriggerZKNode(triggerIdentity);

              if (environment instanceof YHDLocalProcessEnvironment) {
                KiraClientDataCenter
                    .removeTriggerFromEnvironmentTriggersToBeDeletedMap(triggerIdentity,
                        environment);
              } else {
                //ccjtodo: phase2 need to notify the child process if deleted successfully.
              }
            } catch (Exception e) {
              logger.error("Error occurs for deleteTrigger. triggerIdentity=" + KiraCommonUtils
                  .toString(triggerIdentity) + " and environment=" + environment, e);
            }
          }
        }
      } catch (Exception e) {
        logger.error("Error occurs for deleteTriggers. triggerIdentitys=" + KiraCommonUtils
            .toString(triggerIdentitys) + " and environment=" + environment, e);
      } finally {
        if (logger.isDebugEnabled()) {
          long handleCostTime = System.currentTimeMillis() - startHandleTime;
          logger.debug("It takes " + handleCostTime
                  + " milliseconds to for deleteTriggers. triggerIdentitys={} and environment={}",
              KiraCommonUtils.toString(triggerIdentitys), environment);
        }
      }
    }
  }

  private boolean isValidToDeleteTriggersForLocalEnvironment() {
    boolean returnValue = true;
    if (KiraClientDataCenter
        .isAllSchedulerInitialized()) { //check to see if all local triggers added.
      //It is valid to delete triggers only all local registeredTriggers in the registered success state.
      ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> triggerIdentityEnvironmentTriggerContextDataMap = KiraClientDataCenter
          .getTriggerIdentityEnvironmentTriggerContextDataMap();
      outerLoop:
      for (Entry<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> entry : triggerIdentityEnvironmentTriggerContextDataMap
          .entrySet()) {
        ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = entry
            .getValue();
        IEnvironment environment = null;
        for (Entry<IEnvironment, TriggerRegisterContextData> theEntry : environmentTriggerContextDataMap
            .entrySet()) {
          environment = theEntry.getKey();
          if (KiraClientDataCenter.getLocalProcessEnvironment().equals(environment)) {
            TriggerRegisterContextData triggerRegisterContextData = theEntry.getValue();
            String schedulerBeanName = triggerRegisterContextData.getSchedulerBeanName();
            YHDSchedulerFactoryBean yhdSchedulerFactoryBean = KiraClientDataCenter
                .getSchedulerBeanNameYHDSchedulerFactoryBeanMap().get(schedulerBeanName);
            if (null == yhdSchedulerFactoryBean) {
              //check to see if schedulerBeanNames of triggers for localEnvironment can be found locally. The scheduler may not be initialized successful which may cause that scheduler not added to cache.
              returnValue = false;
              break outerLoop;
            } else {
              RegisterStatusEnum registerStatus = triggerRegisterContextData.getRegisterStatus();
              if (!RegisterStatusEnum.REGISTER_SUCCESS.equals(registerStatus)) {
                returnValue = false;
                break outerLoop;
              }
            }
          }
        }
      }
    } else {
      returnValue = false;
    }

    return returnValue;
  }

  private void calculateTriggersToBeDeletedForLocalEnvironment() throws Exception {
    long startHandleTime = System.currentTimeMillis();
    try {
      Set<TriggerIdentity> allTriggersOfPoolToBeDeleted = new LinkedHashSet<TriggerIdentity>();
      if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
        String appId = KiraUtil.appId();
        List<TriggerIdentity> allTriggerIdentityOfPoolOnZK = KiraCommonUtils
            .getAllTriggerIdentityOfPoolOnZK(appId, false, true);
        allTriggersOfPoolToBeDeleted = new LinkedHashSet<TriggerIdentity>(
            allTriggerIdentityOfPoolOnZK);
      } else if (KiraClientRoleEnum.CHILD_PROCESS
          .equals(KiraClientDataCenter.getKiraClientRole())) {
        //ccjtodo: phase2, need to query all trggers(allTriggersOfPool) of that pool on ZK from manger. Then figure out the triggers to be deleted.
      }

      //filter out the triggers used.
      ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> triggerIdentityEnvironmentTriggerContextDataMap = KiraClientDataCenter
          .getTriggerIdentityEnvironmentTriggerContextDataMap();
      TriggerIdentity triggerIdentity = null;
      for (Entry<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> entry : triggerIdentityEnvironmentTriggerContextDataMap
          .entrySet()) {
        triggerIdentity = entry.getKey();
        ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = entry
            .getValue();
        TriggerRegisterContextData triggerRegisterContextData = environmentTriggerContextDataMap
            .get(KiraClientDataCenter.getLocalProcessEnvironment());
        if (null != triggerRegisterContextData) {
          allTriggersOfPoolToBeDeleted.remove(triggerIdentity);
        }
      }

      if (!CollectionUtils.isEmpty(allTriggersOfPoolToBeDeleted)) {
        KiraClientDataCenter.addTriggersToBeDeleted(allTriggersOfPoolToBeDeleted,
            KiraClientDataCenter.getLocalProcessEnvironment());
      }

      //no error occurs, so regard it as finished.
      KiraClientDataCenter.setCalculateTriggersToBeDeletedForLocalEnvironmentFinished(true);
      logger.debug(
          "calculateTriggersToBeDeletedForLocalEnvironment finished. allTriggersOfPoolToBeDeleted={}",
          allTriggersOfPoolToBeDeleted);
    } catch (Exception e) {
      logger.error("Error occurs for calculateTriggersToBeDeleted.", e);
      throw e;
    } finally {
      if (logger.isDebugEnabled()) {
        long handleCostTime = System.currentTimeMillis() - startHandleTime;
        logger.debug(
            "It takes " + handleCostTime + " milliseconds to for calculateTriggersToBeDeleted.");
      }
    }
  }

  private synchronized boolean tryToCalculateTriggersToBeDeletedForLocalEnvironment() {
    boolean returnValue = false;
    try {
      boolean isValidToDeleteTriggersForLocalEnvironment = isValidToDeleteTriggersForLocalEnvironment();
      if (isValidToDeleteTriggersForLocalEnvironment) {
        calculateTriggersToBeDeletedForLocalEnvironment();
        returnValue = true;
      }
    } catch (Exception e) {
      logger.error("Error occurs when tryToCalculateTriggersToBeDeletedForLocalEnvironment.", e);
    }
    return returnValue;
  }

  @Override
  public synchronized void handleTriggersToBeDeleted() {
    try {
      logger.debug("handleTriggersToBeDeleted called.");
      ConcurrentMap<IEnvironment, Set<TriggerIdentity>> environmentTriggersToBeDeletedMap = KiraClientDataCenter
          .getEnvironmentTriggersToBeDeletedMap();
      ConcurrentMap<IEnvironment, Set<TriggerIdentity>> triggersToBeDeletedMap = new ConcurrentHashMap<IEnvironment, Set<TriggerIdentity>>();
      for (Map.Entry<IEnvironment, Set<TriggerIdentity>> entry : environmentTriggersToBeDeletedMap
          .entrySet()) {
        IEnvironment environment = entry.getKey();
        Set<TriggerIdentity> triggersToBeDeleted = entry.getValue();
        if (!CollectionUtils.isEmpty(triggersToBeDeleted)) {
          triggersToBeDeletedMap
              .put(environment, new LinkedHashSet<TriggerIdentity>(triggersToBeDeleted));
        }
      }

      if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
        for (Map.Entry<IEnvironment, Set<TriggerIdentity>> entry : triggersToBeDeletedMap
            .entrySet()) {
          IEnvironment environment = entry.getKey();
          Set<TriggerIdentity> triggersToBeDeleted = entry.getValue();
          if (!CollectionUtils.isEmpty(triggersToBeDeleted)) {
            deleteTriggersByMainProcess(triggersToBeDeleted, environment);
          }
        }
      } else if (KiraClientRoleEnum.CHILD_PROCESS
          .equals(KiraClientDataCenter.getKiraClientRole())) {
        //ccjtodo: phase2, need to send triggers to manager to delete the triggers
      }
    } catch (Exception e) {
      logger.error("Error occurs when handleTriggersToBeDeleted.", e);
    }
  }

  @Override
  public synchronized void tryToCalculateTriggersToBeDeletedForLocalEnvironmentAndHandle() {
    try {
      if (!KiraClientDataCenter.isCalculateTriggersToBeDeletedForLocalEnvironmentFinished()) {
        boolean calculateOK = this.tryToCalculateTriggersToBeDeletedForLocalEnvironment();
        if (calculateOK) {
          logger.debug(
              "tryToCalculateTriggersToBeDeletedForLocalEnvironment return true, will call handleTriggersToBeDeleted.");
          handleTriggersToBeDeleted();
        } else {
          logger.debug(
              "tryToCalculateTriggersToBeDeletedForLocalEnvironment return false, it is not the time to call handleTriggersToBeDeleted.");
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "CalculateTriggersToBeDeletedForLocalEnvironmentFinished=true, so do not calculate and delete again.");
        }
      }
    } catch (Exception e) {
      logger
          .error("Error occurs when tryToCalculateTriggersToBeDeletedForLocalEnvironmentAndHandle.",
              e);
    }
  }

  private static class TriggerDeleteMonitorTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(TriggerDeleteMonitorTask.class);

    @Override
    public void run() {
      try {
        //handle local first
        TriggerDeleteHandler.getTriggerDeleteHandler()
            .tryToCalculateTriggersToBeDeletedForLocalEnvironmentAndHandle();

        //Then handle the whole environment.
        TriggerDeleteHandler.getTriggerDeleteHandler().handleTriggersToBeDeleted();
      } catch (Exception e) {
        logger.error("Error occurs when running TriggerDeleteMonitorTask.", e);
      }
    }
  }
}
