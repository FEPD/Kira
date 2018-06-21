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
package com.yihaodian.architecture.kira.client.internal.util;

import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.provider.AppProfile;
import com.yihaodian.architecture.kira.client.internal.bean.Job;
import com.yihaodian.architecture.kira.client.internal.impl.YHDLocalProcessEnvironment;
import com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean;
import com.yihaodian.architecture.kira.client.util.CentralScheduleServiceExporter;
import com.yihaodian.architecture.kira.client.util.KiraClientConfig;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.TriggerMetadataDetail;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class KiraClientDataCenter {

  private static final Date kiraClientStartTime = new Date();
  private static final String kiraClientVersion = KiraCommonUtils
      .getValueFromManifestInZipFileofClassByKey(KiraClientConfig.class,
          KiraCommonConstants.STRING_PROJECTVERSION);
  private static Logger logger = LoggerFactory.getLogger(KiraClientDataCenter.class);
  private static KiraClientConfig kiraClientConfig;

  private static AppProfile appProfileForCentralScheduleServiceExporter;
  private static CentralScheduleServiceExporter centralScheduleServiceExporter;
  private static String centralScheduleServiceZNodePath;


  //ccjtodo: phase2, need to be set as CHILD_PROCESS when started as childProcess.
  private static KiraClientRoleEnum kiraClientRole = KiraClientRoleEnum.MAIN_PROCESS;

  private static YHDLocalProcessEnvironment localProcessEnvironment;
  private static ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> triggerIdentityEnvironmentTriggerContextDataMap = new ConcurrentHashMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>>();
  private static Set<IEnvironment> environmentSet = new LinkedHashSet<IEnvironment>();
  private static ConcurrentMap<IEnvironment, Set<TriggerIdentity>> environmentTriggersToBeDeletedMap = new ConcurrentHashMap<IEnvironment, Set<TriggerIdentity>>();
  private static volatile boolean calculateTriggersToBeDeletedForLocalEnvironmentFinished = false; //only need to calculate once after boot or it will delete the added trigger when the same pool with new triggers deployed.

  private static ConcurrentMap<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>> poolIdEnvironmentKiraClientRegisterContextDataMap = new ConcurrentHashMap<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>>();

  private static volatile boolean allSchedulerInitialized;
  private static ConcurrentMap<String, YHDSchedulerFactoryBean> schedulerBeanNameYHDSchedulerFactoryBeanMap = new ConcurrentHashMap<String, YHDSchedulerFactoryBean>();

  //<jobid,Job>
  private static ConcurrentMap<String, Job> jobIdJobMap = new ConcurrentHashMap<String, Job>();

  private static ApplicationContext applicationContext;

  public KiraClientDataCenter() {
    // TODO Auto-generated constructor stub
  }

  public static Date getKiraclientStartTime() {
    return kiraClientStartTime;
  }

  public static String getKiraclientversion() {
    String returnValue = null;
    if (StringUtils.isBlank(kiraClientVersion)) {
      returnValue = "Unknown";
    } else {
      returnValue = kiraClientVersion;
    }
    return returnValue;
  }

  public static KiraClientConfig getKiraClientConfig() {
    return kiraClientConfig;
  }

  public static void setKiraClientConfig(KiraClientConfig kiraClientConfig) {
    KiraClientDataCenter.kiraClientConfig = kiraClientConfig;
  }

  public static boolean isAppCenter() {
    boolean returnValue = false;
    if (null != kiraClientConfig) {
      returnValue = kiraClientConfig.isAppCenter();
    }
    return returnValue;
  }

  public static boolean isWorkWithoutKira() {
    boolean returnValue = false;
    if (null != kiraClientConfig) {
      returnValue = kiraClientConfig.isWorkWithoutKira();
    }
    return returnValue;
  }

  public static long getWaitForResourceTimeoutMillisecond() {
    long returnValue = KiraClientConstants.WAITFOR_RESOURCE_TIMEOUT_MILLISECOND;
    if (null != kiraClientConfig) {
      returnValue = kiraClientConfig.getWaitForResourceTimeoutMillisecond();
    }
    return returnValue;
  }

  public static boolean isAutoDeleteTriggersOnZK() {
    boolean returnValue = true;
    if (null != kiraClientConfig) {
      returnValue = kiraClientConfig.isAutoDeleteTriggersOnZK();
    }
    return returnValue;
  }

  public static String getLocationsToRunJobForAllTriggers() {
    String returnValue = null;
    if (null != kiraClientConfig) {
      returnValue = kiraClientConfig.getLocationsToRunJobForAllTriggers();
    }
    return returnValue;
  }

  public static AppProfile getAppProfileForCentralScheduleServiceExporter() {
    boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!workWithoutKira) {
      KiraClientDataCenter.waitForAppProfileForCentralScheduleServiceExporterInitialized();
    }
    return KiraClientDataCenter.appProfileForCentralScheduleServiceExporter;
  }

  public static void setAppProfileForCentralScheduleServiceExporter(
      AppProfile appProfileForCentralScheduleServiceExporter) {
    KiraClientDataCenter.appProfileForCentralScheduleServiceExporter = appProfileForCentralScheduleServiceExporter;
  }

  public static CentralScheduleServiceExporter getCentralScheduleServiceExporter() {
    boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!workWithoutKira) {
      KiraClientDataCenter.waitForCentralScheduleServiceExporterInitialized();
    }
    return KiraClientDataCenter.centralScheduleServiceExporter;
  }

  public static void setCentralScheduleServiceExporter(
      CentralScheduleServiceExporter centralScheduleServiceExporter) {
    KiraClientDataCenter.centralScheduleServiceExporter = centralScheduleServiceExporter;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForCentralScheduleServiceExporterInitialized() {
    try {
      long waittime = 0;
      while ((null == KiraClientDataCenter.centralScheduleServiceExporter)
          && waittime < KiraClientDataCenter.getWaitForResourceTimeoutMillisecond()) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForCentralScheduleServiceExporterInitialized.");
    } finally {
      if (null == KiraClientDataCenter.centralScheduleServiceExporter) {
        throw new RuntimeException(
            "The centralScheduleServiceExporter must be initialized for using kira-client.");
      }
    }
  }

  public static String getCentralScheduleServiceZNodePath() {
    if (null == KiraClientDataCenter.centralScheduleServiceZNodePath) {
      AppProfile appProfile = getAppProfileForCentralScheduleServiceExporter();
      KiraClientDataCenter.centralScheduleServiceZNodePath =
          KiraClientConstants.ZK_PATH_THESTORE + KiraClientConstants.ZNODE_NAME_PREFIX + appProfile
              .getDomainName() + KiraClientConstants.ZNODE_NAME_PREFIX + appProfile
              .getServiceAppName() + KiraClientConstants.ZNODE_NAME_PREFIX
              + KiraClientConstants.SERVICE_NAME_CENTRAL_SCHEDULE_SERVICE
              + KiraClientConstants.ZNODE_NAME_PREFIX + HedwigUtil
              .filterString(KiraClientConstants.SERVICEVERSION_CENTRALSCHEDULESERVICE);
    }
    return KiraClientDataCenter.centralScheduleServiceZNodePath;
  }

  /**
   * 等待初始化完成
   */
  private static void waitForAppProfileForCentralScheduleServiceExporterInitialized() {
    try {
      long waittime = 0;
      while ((null == KiraClientDataCenter.appProfileForCentralScheduleServiceExporter)
          && waittime < KiraClientDataCenter.getWaitForResourceTimeoutMillisecond()) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForAppProfileForCentralScheduleServiceExporterInitialized.");
    } finally {
      if (null == KiraClientDataCenter.appProfileForCentralScheduleServiceExporter) {
        throw new RuntimeException(
            "The CentralScheduleServiceExporter must be initialized for using kira-client.");
      }
    }
  }

  public static KiraClientRoleEnum getKiraClientRole() {
    return KiraClientDataCenter.kiraClientRole;
  }

  public static void setKiraClientRole(KiraClientRoleEnum kiraClientRole) {
    KiraClientDataCenter.kiraClientRole = kiraClientRole;
  }

  public static synchronized YHDLocalProcessEnvironment getLocalProcessEnvironment() {
    if (null == KiraClientDataCenter.localProcessEnvironment) {
      Integer pid = SystemUtil.getPid();
      KiraClientDataCenter.localProcessEnvironment = new YHDLocalProcessEnvironment(pid);
    }
    return KiraClientDataCenter.localProcessEnvironment;
  }

  public static ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> getTriggerIdentityEnvironmentTriggerContextDataMap() {
    return KiraClientDataCenter.triggerIdentityEnvironmentTriggerContextDataMap;
  }

  public static void setTriggerIdentityEnvironmentTriggerContextDataMap(
      ConcurrentMap<TriggerIdentity, ConcurrentMap<IEnvironment, TriggerRegisterContextData>> triggerIdentityEnvironmentTriggerContextDataMap) {
    KiraClientDataCenter.triggerIdentityEnvironmentTriggerContextDataMap = triggerIdentityEnvironmentTriggerContextDataMap;
  }

  public static void addTriggerRegisterContextData(
      TriggerRegisterContextData triggerRegisterContextData) {
    TriggerMetadataDetail triggerMetaDataDetail = triggerRegisterContextData
        .getTriggerMetaDataDetail();
    String appId = triggerMetaDataDetail.getAppId();
    String triggerId = triggerMetaDataDetail.getTriggerId();
    TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
    IEnvironment environment = triggerRegisterContextData.getEnvironment();
    ConcurrentMap<IEnvironment, TriggerRegisterContextData> newValue = new ConcurrentHashMap<IEnvironment, TriggerRegisterContextData>();
    ConcurrentMap<IEnvironment, TriggerRegisterContextData> oldValue = KiraClientDataCenter.triggerIdentityEnvironmentTriggerContextDataMap
        .putIfAbsent(triggerIdentity, newValue);
    ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = oldValue;
    if (null == environmentTriggerContextDataMap) {
      environmentTriggerContextDataMap = newValue;
    }
    environmentTriggerContextDataMap.put(environment, triggerRegisterContextData);
    KiraClientDataCenter.triggerIdentityEnvironmentTriggerContextDataMap
        .put(triggerIdentity, environmentTriggerContextDataMap);
  }

  public static ConcurrentMap<IEnvironment, TriggerRegisterContextData> getEnvironmentTriggerContextDataMap(
      TriggerIdentity triggerIdentity) {
    ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = KiraClientDataCenter.triggerIdentityEnvironmentTriggerContextDataMap
        .get(triggerIdentity);
    return environmentTriggerContextDataMap;
  }

  public static void addEnvironment(IEnvironment environment) {
    KiraClientDataCenter.environmentSet.add(environment);
  }

  public static void removeEnvironment(IEnvironment environment) {
    KiraClientDataCenter.environmentSet.remove(environment);
  }

  public static ConcurrentMap<IEnvironment, Set<TriggerIdentity>> getEnvironmentTriggersToBeDeletedMap() {
    return environmentTriggersToBeDeletedMap;
  }

  public static void setEnvironmentTriggersToBeDeletedMap(
      ConcurrentMap<IEnvironment, Set<TriggerIdentity>> environmentTriggersToBeDeletedMap) {
    KiraClientDataCenter.environmentTriggersToBeDeletedMap = environmentTriggersToBeDeletedMap;
  }

  public static void addTriggersToBeDeleted(Set<TriggerIdentity> triggerIdentitys,
      IEnvironment environment) {
    if (null != triggerIdentitys) {
      Set<TriggerIdentity> triggerIdentitySet = KiraClientDataCenter.environmentTriggersToBeDeletedMap
          .get(environment);
      if (null == triggerIdentitySet) {
        triggerIdentitySet = new LinkedHashSet<TriggerIdentity>();
      }
      triggerIdentitySet.addAll(triggerIdentitys);
      KiraClientDataCenter.environmentTriggersToBeDeletedMap.put(environment, triggerIdentitySet);
    }
  }

  public static void removeTriggerFromEnvironmentTriggersToBeDeletedMap(
      TriggerIdentity triggerIdentity, IEnvironment environment) {
    Set<TriggerIdentity> triggersToBeDeleted = KiraClientDataCenter.environmentTriggersToBeDeletedMap
        .get(environment);
    if (null != triggersToBeDeleted) {
      triggersToBeDeleted.remove(triggerIdentity);
    }
  }

  public static boolean isCalculateTriggersToBeDeletedForLocalEnvironmentFinished() {
    return calculateTriggersToBeDeletedForLocalEnvironmentFinished;
  }

  public static void setCalculateTriggersToBeDeletedForLocalEnvironmentFinished(
      boolean calculateTriggersToBeDeletedForLocalEnvironmentFinished) {
    KiraClientDataCenter.calculateTriggersToBeDeletedForLocalEnvironmentFinished = calculateTriggersToBeDeletedForLocalEnvironmentFinished;
  }

  public static ConcurrentMap<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>> getPoolIdEnvironmentKiraClientRegisterContextDataMap() {
    return poolIdEnvironmentKiraClientRegisterContextDataMap;
  }

  public static void setPoolIdEnvironmentKiraClientRegisterContextDataMap(
      ConcurrentMap<String, ConcurrentMap<IEnvironment, KiraClientRegisterContextData>> poolIdEnvironmentKiraClientRegisterContextDataMap) {
    KiraClientDataCenter.poolIdEnvironmentKiraClientRegisterContextDataMap = poolIdEnvironmentKiraClientRegisterContextDataMap;
  }

  public static void addKiraClientRegisterContextData(
      KiraClientRegisterContextData kiraClientRegisterContextData) {
    KiraClientRegisterData kiraClientRegisterData = kiraClientRegisterContextData
        .getKiraClientRegisterData();
    String appId = kiraClientRegisterData.getAppId();
    IEnvironment environment = kiraClientRegisterContextData.getEnvironment();
    ConcurrentMap<IEnvironment, KiraClientRegisterContextData> newValue = new ConcurrentHashMap<IEnvironment, KiraClientRegisterContextData>();
    ConcurrentMap<IEnvironment, KiraClientRegisterContextData> oldValue = KiraClientDataCenter.poolIdEnvironmentKiraClientRegisterContextDataMap
        .putIfAbsent(appId, newValue);
    ConcurrentMap<IEnvironment, KiraClientRegisterContextData> environmentKiraClientRegisterContextDataMap = oldValue;
    if (null == environmentKiraClientRegisterContextDataMap) {
      environmentKiraClientRegisterContextDataMap = newValue;
    }
    environmentKiraClientRegisterContextDataMap.put(environment, kiraClientRegisterContextData);
    KiraClientDataCenter.poolIdEnvironmentKiraClientRegisterContextDataMap
        .put(appId, environmentKiraClientRegisterContextDataMap);
  }

  public static boolean isAllSchedulerInitialized() {
    return allSchedulerInitialized;
  }

  public static void setAllSchedulerInitialized(boolean allSchedulerInitialized) {
    KiraClientDataCenter.allSchedulerInitialized = allSchedulerInitialized;
  }

  public static ConcurrentMap<String, YHDSchedulerFactoryBean> getSchedulerBeanNameYHDSchedulerFactoryBeanMap() {
    return schedulerBeanNameYHDSchedulerFactoryBeanMap;
  }

  public static void setSchedulerBeanNameYHDSchedulerFactoryBeanMap(
      ConcurrentMap<String, YHDSchedulerFactoryBean> schedulerBeanNameYHDSchedulerFactoryBeanMap) {
    KiraClientDataCenter.schedulerBeanNameYHDSchedulerFactoryBeanMap = schedulerBeanNameYHDSchedulerFactoryBeanMap;
  }

  public static void addSchedulerBeanNameYHDSchedulerFactoryBeanRelationShip(
      String schedulerBeanName, YHDSchedulerFactoryBean yhdSchedulerFactoryBean) {
    KiraClientDataCenter.schedulerBeanNameYHDSchedulerFactoryBeanMap
        .put(schedulerBeanName, yhdSchedulerFactoryBean);
  }

  public static ConcurrentMap<String, Job> getJobIdJobMap() {
    return KiraClientDataCenter.jobIdJobMap;
  }

  public static void setJobIdJobMap(ConcurrentMap<String, Job> jobIdJobMap) {
    KiraClientDataCenter.jobIdJobMap = jobIdJobMap;
  }

  public static void addJob(Job job) {
    if (null != job) {
      String jobId = job.getId();
      KiraClientDataCenter.jobIdJobMap.put(jobId, job);
    }
  }

  public static void removeJob(Job job) {
    if (null != job) {
      String jobId = job.getId();
      KiraClientDataCenter.jobIdJobMap.remove(jobId);
    }
  }

  public static Job getJob(String jobId) {
    Job job = KiraClientDataCenter.jobIdJobMap.get(jobId);
    return job;
  }

  public static ApplicationContext getApplicationContext() {
    return KiraClientDataCenter.applicationContext;
  }

  public static void setApplicationContext(ApplicationContext applicationContext) {
    KiraClientDataCenter.applicationContext = applicationContext;
  }

  public static boolean isInMainProcess() {
    boolean returnValue = false;
    if (KiraClientRoleEnum.MAIN_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      returnValue = true;
    }
    return returnValue;
  }

  public static boolean isInChildProcess() {
    boolean returnValue = false;
    if (KiraClientRoleEnum.CHILD_PROCESS.equals(KiraClientDataCenter.getKiraClientRole())) {
      returnValue = true;
    }
    return returnValue;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
