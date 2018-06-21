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
package com.yihaodian.architecture.kira.manager.core.metadata.timertrigger;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.listener.PoolOfTriggerZkChildListener;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.listener.TriggerZkDataListener;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.listener.TriggersZkChildListener;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.util.KiraServerEventHandleComponent;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

public class KiraTimerTriggerMetadataManager extends KiraServerEventHandleComponent implements
    IKiraTimerTriggerMetadataManager {

  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private TriggerMetadataService triggerMetadataService;
  private TriggersZkChildListener triggersZkChildListener;
  //runtimeData
  private Map<TriggerIdentity, TriggerZkDataListener> triggerIdentityTriggerZkDataListenerMap = new ConcurrentHashMap<TriggerIdentity, TriggerZkDataListener>();
  private Map<String, PoolOfTriggerZkChildListener> poolIdPoolOfTriggerZkChildListenerMap = new ConcurrentHashMap<String, PoolOfTriggerZkChildListener>();
  private AtomicLong handleTriggerZKDataChangeCount = new AtomicLong(0L);
  private AtomicLong handleChildChangeForTriggersZNodeCount = new AtomicLong(0L);
  private AtomicLong handleChildChangeForPoolOfTriggerCount = new AtomicLong(0L);
  private AtomicLong manuallyCreateOrUpdateTriggerZNodeCount = new AtomicLong(0L);

  public KiraTimerTriggerMetadataManager(IKiraServer kiraServer,
      TriggerMetadataService triggerMetadataService) throws Exception {
    super(kiraServer);
    this.triggerMetadataService = triggerMetadataService;
    this.init();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void init() throws Exception {
    logger.info("Initializing KiraTimerTriggerMetadataManager...");
    long startTime = System.currentTimeMillis();
    try {
      super.init();
      this.triggersZkChildListener = new TriggersZkChildListener(this);
      logger.info("Successfully initialize KiraTimerTriggerMetadataManager.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraTimerTriggerMetadataManager.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraTimerTriggerMetadataManager. And it takes " + costTime
          + " milliseconds.");
    }
  }

  @Override
  protected void beforeChangeToWorkAsFollower() throws Exception {
    this.unsubscribeDataChangesForAllTriggers();
    this.unsubscribeChildChangesForTriggersZNode();
    this.unsubscribeChildChangesForAllPoolOfTriggerOnZK();
  }

  @Override
  protected void doLeaderRoutineWork() throws Exception {
    super.doLeaderRoutineWork();

    //need to make sure the quartz schedule data are migrated first.
    //Now quartz stuff had already been migrated and discarded in kira server side. Keep the code just for reference.
    //KiraManagerDataCenter.isQuartzScheduleDataMigrated();

    this.subscribeChildChangesForTriggersZNode();
    this.subscribeChildChangesForAllPoolOfTriggerOnZK();
    this.subscribeDataChangesForAllTriggersOnZK();
    this.checkAndHandleAllTriggersAddedOrDeletedOnZK();
    this.checkAndAddDefaultKiraClientMetadataForTriggers();
  }

  @Override
  protected void afterStartByKiraServerStartedEventSuccess() throws Exception {
    //Now it is ready and need to set it before do those big jobs.
    KiraManagerDataCenter.setKiraTimerTriggerMetadataManager(this);
    super.afterStartByKiraServerStartedEventSuccess();
  }

  private void checkAndAddDefaultKiraClientMetadataForTriggers() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start checkAndAddDefaultKiraClientMetadataForTriggers...");
      this.triggerMetadataService.checkAndAddDefaultKiraClientMetadataForTriggers();
    } catch (Exception e) {
      logger.error("Error occurs when checkAndAddDefaultKiraClientMetadataForTriggers.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("It takes " + costTime
          + " milliseconds to checkAndAddDefaultKiraClientMetadataForTriggers.");
    }
  }

  @Override
  protected void clearAllRunTimeData() {
    super.clearAllRunTimeData();
    this.poolIdPoolOfTriggerZkChildListenerMap.clear();
    this.triggerIdentityTriggerZkDataListenerMap.clear();
  }

  @Override
  protected void doShutdown() {
    logger.info("Will doShutdown for " + this.getClass().getSimpleName() + ".");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        logger.info("Shutting down " + this.getClass().getSimpleName() + "...");
        if (this.isLeaderServer()) {
          this.unsubscribeChildChangesForTriggersZNode();
          this.unsubscribeChildChangesForAllPoolOfTriggerOnZK();
          this.unsubscribeDataChangesForAllTriggers();
        }
        this.clearAllRunTimeData();

        logger.info("Successfully shutdown " + this.getClass().getSimpleName() + ".");
      } catch (Exception e) {
        logger.error("Error occurs when shutting down " + this.getClass().getSimpleName() + ".", e);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish shutdown " + this.getClass().getSimpleName() + ". It takes {} milliseconds.",
            costTime);
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  @Override
  public void unsubscribeChildChangesForPoolByPoolId(String appId) throws Exception {
    try {
      String onePoolPath = KiraUtil
          .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, KiraUtil.filterString(appId));
      PoolOfTriggerZkChildListener poolOfTriggerZkChildListener = this.poolIdPoolOfTriggerZkChildListenerMap
          .get(appId);
      if (null != poolOfTriggerZkChildListener) {
        zkClient.unsubscribeChildChanges(onePoolPath, poolOfTriggerZkChildListener);
      }

      //update cache
      this.poolIdPoolOfTriggerZkChildListenerMap.remove(appId);
    } catch (Exception e) {
      logger.error("Error occurs for unsubscribeChildChangesForPoolByPoolId. appId=" + appId, e);
      throw e;
    }
  }

  @Override
  public void unsubscribeDataChangesForTriggersOfPool(String poolId) throws Exception {
    for (Map.Entry<TriggerIdentity, TriggerZkDataListener> entry : this.triggerIdentityTriggerZkDataListenerMap
        .entrySet()) {
      TriggerIdentity triggerIdentity = entry.getKey();
      this.unsubscribeDataChangesForTrigger(triggerIdentity);
    }
  }

  @Override
  public void unsubscribeDataChangesForTrigger(TriggerIdentity triggerIdentity) throws Exception {
    try {
      String appId = triggerIdentity.getAppId();
      String triggerId = triggerIdentity.getTriggerId();
      TriggerIdentity key = new TriggerIdentity(appId, triggerId);
      TriggerZkDataListener triggerZkDataListener = this.triggerIdentityTriggerZkDataListenerMap
          .get(key);

      if (null != triggerZkDataListener) {
        String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
        zkClient.unsubscribeDataChanges(triggerZNodeZKPath, triggerZkDataListener);
      }

      //update cache
      this.triggerIdentityTriggerZkDataListenerMap.remove(key);
    } catch (Exception e) {
      logger.error(
          "Error occurs for unsubscribeDataChangesForTrigger. triggerIdentity=" + KiraCommonUtils
              .toString(triggerIdentity), e);
      throw e;
    }
  }

  @Override
  public void subscribeDataChangesForTrigger(TriggerIdentity triggerIdentity) throws Exception {
    try {
      String appId = triggerIdentity.getAppId();
      String triggerId = triggerIdentity.getTriggerId();

      TriggerIdentity key = new TriggerIdentity(appId, triggerId);
      if (null != this.triggerIdentityTriggerZkDataListenerMap.get(key)) {
        logger.info(
            "No need to subscribe the triggerZkDataListener for it is already subscribed for triggerIdentity="
                + triggerIdentity);
      } else {
        //unsubscribe first
        try {
          unsubscribeDataChangesForTrigger(key);
        } catch (Exception e) {
          logger.error("Error occurs for unsubscribeDataChangesForTriggerOnZK. triggerIdentity="
              + KiraCommonUtils.toString(triggerIdentity), e);
          throw e;
        }

        String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
        TriggerZkDataListener triggerZkDataListener = new TriggerZkDataListener(this);
        zkClient.subscribeDataChanges(triggerZNodeZKPath, triggerZkDataListener);

        //update cache
        this.triggerIdentityTriggerZkDataListenerMap.put(key, triggerZkDataListener);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for subscribeDataChangesForTrigger. triggerIdentity=" + KiraCommonUtils
              .toString(triggerIdentity), e);
      throw e;
    }
  }

  @Override
  public void handleTriggerZKDataChange(String triggerZKPath,
      TriggerMetadataZNodeData newTriggerMetadataZNodeData) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start handleTriggerZKDataChange...");
      if (!this.requestShutdown && !this.requestDestroy) {
        try {
          this.handleTriggerZKDataChangeCount.incrementAndGet();

          this.lockForComponentState.readLock().lock();
          try {
            if (!this.requestShutdown && !this.requestDestroy) {
              if (this.isStarted()) {
                this.lockForKiraServerClusterRuntimeDataManagement.readLock().lock();
                try {
                  if (this.isLeaderServer()) {
                    triggerMetadataService.handleTriggerZKDataChange(newTriggerMetadataZNodeData);
                  } else {
                    logger.warn(
                        "The follower server should not listen for triggerZKDataChange event. kiraServerRole={} and triggerZKPath={} and newTriggerMetadataZNodeData={}",
                        this.kiraServerRole, triggerZKPath,
                        KiraCommonUtils.toString(newTriggerMetadataZNodeData));
                  }
                } finally {
                  this.lockForKiraServerClusterRuntimeDataManagement.readLock().unlock();
                }
              } else {
                logger.warn(
                    "KiraTimerTriggerMetadataManager is not in the started state. So do not handleTriggerZKDataChange. componentState={} and triggerZKPath={} and newTriggerMetadataZNodeData={}",
                    this.componentState, triggerZKPath,
                    KiraCommonUtils.toString(newTriggerMetadataZNodeData));
              }
            } else {
              logger.warn(
                  "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleTriggerZKDataChange. componentState={} and requestShutdown={} and requestDestroy={} and triggerZKPath={} and newTriggerMetadataZNodeData={}",
                  this.componentState, this.requestShutdown, this.requestDestroy, triggerZKPath,
                  KiraCommonUtils.toString(newTriggerMetadataZNodeData));
            }
          } finally {
            this.lockForComponentState.readLock().unlock();
          }
        } finally {
          this.handleTriggerZKDataChangeCount.decrementAndGet();
        }
      } else {
        logger.warn(
            "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleTriggerZKDataChange. componentState={} and requestShutdown={} and requestDestroy={} and triggerZKPath={} and newTriggerMetadataZNodeData={}",
            this.componentState, this.requestShutdown, this.requestDestroy, triggerZKPath,
            KiraCommonUtils.toString(newTriggerMetadataZNodeData));
      }
    } catch (Exception e) {
      logger.error("Error occurs when handleTriggerZKDataChange. triggerZKPath=" + triggerZKPath
          + " and newTriggerMetadataZNodeData=" + KiraCommonUtils
          .toString(newTriggerMetadataZNodeData), e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger
          .info("Finish handleTriggerZKDataChange. triggerZKPath={} and it takes {} milliseconds.",
              triggerZKPath, costTime);
    }
  }

  @Override
  public void handleChildChangeForTriggersZNode(String parentPath,
      List<String> currentChilds) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start handleChildChangeForTriggersZNode...");
      if (!this.requestShutdown && !this.requestDestroy) {
        try {
          this.handleChildChangeForTriggersZNodeCount.incrementAndGet();

          this.lockForComponentState.readLock().lock();
          try {
            if (!this.requestShutdown && !this.requestDestroy) {
              if (this.isStarted()) {
                this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
                try {
                  if (this.isLeaderServer()) {
                    this.checkAndHandlePoolsDeletedOnZK();
                    this.subscribeChildChangesForAllPoolOfTriggerOnZKIfNeeded();
                  } else {
                    logger.warn(
                        "The follower server should not listen for events under triggers. kiraServerRole={}",
                        this.kiraServerRole);
                  }
                } finally {
                  this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
                }
              } else {
                logger.warn(
                    "KiraTimerTriggerMetadataManager is not in the started state. So do not handleChildChangeForTriggersZNode. componentState={}",
                    this.componentState);
              }
            } else {
              logger.warn(
                  "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleChildChangeForTriggersZNode. componentState={} and requestShutdown={} and requestDestroy={}",
                  this.componentState, this.requestShutdown, this.requestDestroy);
            }
          } finally {
            this.lockForComponentState.readLock().unlock();
          }
        } finally {
          this.handleChildChangeForTriggersZNodeCount.decrementAndGet();
        }
      } else {
        logger.warn(
            "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleChildChangeForTriggersZNode. componentState={} and requestShutdown={} and requestDestroy={}",
            this.componentState, this.requestShutdown, this.requestDestroy);
      }
    } catch (Exception e) {
      logger.error("Error occurs when handleChildChangeForTriggersZNode.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish handleChildChangeForTriggersZNode. It takes {} milliseconds.", costTime);
    }
  }

  @Override
  public void handleChildChangeForPoolOfTrigger(String poolZNodePath,
      List<String> currentChilds) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start handleChildChangeForPoolOfTrigger...");
      if (!this.requestShutdown && !this.requestDestroy) {
        try {
          this.handleChildChangeForPoolOfTriggerCount.incrementAndGet();

          this.lockForComponentState.readLock().lock();
          try {
            if (!this.requestShutdown && !this.requestDestroy) {
              if (this.isStarted()) {
                this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
                try {
                  if (this.isLeaderServer()) {
                    this.doHandleChildChangeForPoolOfTrigger(poolZNodePath, currentChilds);
                  } else {
                    logger.warn(
                        "The follower server should not listen for events under trigger's pool. poolZNodePath={}",
                        poolZNodePath);
                  }
                } finally {
                  this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
                }
              } else {
                logger.warn(
                    "KiraTimerTriggerMetadataManager is not in the started state. So do not handleChildChangeForPoolOfTrigger. componentState={} and poolZNodePath={}",
                    this.componentState, poolZNodePath);
              }
            } else {
              logger.warn(
                  "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleChildChangeForPoolOfTrigger. componentState={} and requestShutdown={} and requestDestroy={} and poolZNodePath={}",
                  this.componentState, this.requestShutdown, this.requestDestroy, poolZNodePath);
            }
          } finally {
            this.lockForComponentState.readLock().unlock();
          }
        } finally {
          this.handleChildChangeForPoolOfTriggerCount.decrementAndGet();
        }
      } else {
        logger.warn(
            "KiraTimerTriggerMetadataManager may request shutdown or destroy. So do not handleChildChangeForPoolOfTrigger. componentState={} and requestShutdown={} and requestDestroy={} and poolZNodePath={}",
            this.componentState, this.requestShutdown, this.requestDestroy, poolZNodePath);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when handleChildChangeForPoolOfTrigger. poolZNodePat" + poolZNodePath, e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish handleChildChangeForPoolOfTrigger. poolZNodePath={} and it takes {} milliseconds.",
          poolZNodePath, costTime);
    }
  }

  @Override
  public void manuallyCreateOrUpdateTriggerZNode(TriggerMetadataZNodeData triggerMetadataZNodeData)
      throws Exception {
    if (null != triggerMetadataZNodeData) {
      try {
        this.manuallyCreateOrUpdateTriggerZNodeCount.incrementAndGet();

        this.lockForComponentState.readLock().lock();
        try {
          if (this.isStarted()) {
            this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
            try {
              if (this.isLeaderServer()) {
                try {
                  KiraCommonUtils.createOrUpdateTriggerZNode(triggerMetadataZNodeData, false);
                } catch (Exception e) {
                  logger.error(
                      "Error occurs on createOrUpdateTriggerZNode for manuallyCreateOrUpdateTriggerZNode. triggerMetadataZNodeData="
                          + KiraCommonUtils.toString(triggerMetadataZNodeData), e);
                  throw e;
                }

                String poolId = triggerMetadataZNodeData.getAppId();
                String filteredPoolId = KiraUtil.filterString(poolId);
                //need to subscribe data changes if needed.
                this.subscribeChildChangesForPoolOfTriggerOnZKIfNeeded(filteredPoolId);

                String triggerId = triggerMetadataZNodeData.getTriggerId();
                TriggerIdentity triggerIdentity = new TriggerIdentity(poolId, triggerId);
                //need to subscribe data changes if needed.
                this.subscribeDataChangesForTrigger(triggerIdentity);
              } else {
                String poolId = triggerMetadataZNodeData.getAppId();
                String triggerId = triggerMetadataZNodeData.getTriggerId();
                TriggerIdentity triggerIdentity = new TriggerIdentity(poolId, triggerId);
                String reason = "This KiraServer is not leader. So can not manuallyCreateOrUpdateTriggerZNode.";
                logger.warn(reason + " triggerMetadataZNodeData=" + triggerMetadataZNodeData);
                throw new KiraHandleException(reason + " triggerIdentity=" + triggerIdentity);
              }
            } finally {
              this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
            }

          } else {
            String poolId = triggerMetadataZNodeData.getAppId();
            String triggerId = triggerMetadataZNodeData.getTriggerId();
            TriggerIdentity triggerIdentity = new TriggerIdentity(poolId, triggerId);
            String reason = "The KiraTimerTriggerMetadataManager is not in started state. So can not manuallyCreateOrUpdateTriggerZNode.";
            logger.warn(reason + " triggerMetadataZNodeData=" + triggerMetadataZNodeData);
            throw new KiraHandleException(reason + " triggerIdentity=" + triggerIdentity);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } finally {
        this.manuallyCreateOrUpdateTriggerZNodeCount.decrementAndGet();
      }
    } else {
      throw new KiraHandleException(
          "triggerMetadataZNodeData should not be null for manuallyCreateOrUpdateTriggerZNode");
    }
  }

  @Override
  public boolean isHandlingKiraTimerTriggerMetadata() throws Exception {
    boolean returnValue = false;
    try {
      //handleTriggerZKDataChangeCount handleChildChangeForPoolOfTriggerCount
      if (this.handleTriggerZKDataChangeCount.longValue() > 0
          || this.handleChildChangeForPoolOfTriggerCount.longValue() > 0) {
        returnValue = true;
        logger.info(
            "KiraTimerTriggerMetadataManager is handing kiraTimerTriggerMetadata now. handleTriggerZKDataChangeCount={} and handleChildChangeForPoolOfTriggerCount={}",
            this.handleTriggerZKDataChangeCount, this.handleChildChangeForPoolOfTriggerCount);
      }
    } catch (Throwable t) {
      logger.error("Error occus when call isHandlingKiraTimerTriggerMetadata()", t);
    }
    return returnValue;
  }

  private void checkAndHandleAllTriggersAddedOrDeletedOnZK() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start checkAndHandleAllTriggersAddedOrDeletedOnZK...");
      Set<TriggerIdentity> registeredTriggerIdentitySet = new LinkedHashSet<TriggerIdentity>();
      Set<TriggerIdentity> unRegisteredTriggerIdentitySet = new LinkedHashSet<TriggerIdentity>();
      //need to take version info for calculate the registered and unregistered triggers.
      List<TriggerIdentity> allTriggerIdentityOnZK = this.getAllTriggerIdentityOnZK(true);
      List<TriggerIdentity> allRegisteredAndUnDeletedTriggerIdentityInDB = this.triggerMetadataService
          .getAllRegisteredAndUnDeletedTriggerIdentityInDB(true);
      this.calculateRegisteredAndUnRegisteredTriggers(allTriggerIdentityOnZK,
          allRegisteredAndUnDeletedTriggerIdentityInDB, registeredTriggerIdentitySet,
          unRegisteredTriggerIdentitySet);
      this.unRegisterTriggers(unRegisteredTriggerIdentitySet);
      this.registerTriggers(registeredTriggerIdentitySet);
    } catch (Exception e) {
      logger.error("Error occurs when checkAndHandleAllTriggersAddedOrDeletedOnZK.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "It takes " + costTime + " milliseconds to checkAndHandleAllTriggersAddedOrDeletedOnZK.");
    }
  }

  private void calculateRegisteredAndUnRegisteredTriggers(
      List<TriggerIdentity> triggerIdentitysOnZK,
      List<TriggerIdentity> triggerIdentitysInDB,
      Set<TriggerIdentity> registeredTriggerIdentitySet,
      Set<TriggerIdentity> unRegisteredTriggerIdentitySet) throws Exception {
    try {
      if (null != triggerIdentitysOnZK && null != triggerIdentitysInDB) {
        for (TriggerIdentity triggerIdentity : triggerIdentitysOnZK) {
          this.isNormalOperationNeedToBeAborted(
              "calculateRegisteredAndUnRegisteredTriggers for " + this.getClass().getSimpleName(),
              true);

          if (!triggerIdentitysInDB.contains(triggerIdentity)) {
            registeredTriggerIdentitySet.add(triggerIdentity);
          }
        }
        for (TriggerIdentity triggerIdentity : triggerIdentitysInDB) {
          this.isNormalOperationNeedToBeAborted(
              "calculateRegisteredAndUnRegisteredTriggers for " + this.getClass().getSimpleName(),
              true);

          if (!triggerIdentitysOnZK.contains(triggerIdentity)) {
            unRegisteredTriggerIdentitySet.add(triggerIdentity);
          }
        }
      }
    } catch (Exception e) {
      int sizeOfTriggerIdentitysOnZK = 0;
      if (null != triggerIdentitysOnZK) {
        sizeOfTriggerIdentitysOnZK = triggerIdentitysOnZK.size();
      }
      int sizeOfTriggerIdentitysInDB = 0;
      if (null != triggerIdentitysInDB) {
        sizeOfTriggerIdentitysInDB = triggerIdentitysInDB.size();
      }
      int sizeOfRegisteredTriggerIdentitySet = 0;
      if (null != registeredTriggerIdentitySet) {
        sizeOfRegisteredTriggerIdentitySet = registeredTriggerIdentitySet.size();
      }
      int sizeOfUnRegisteredTriggerIdentitySet = 0;
      if (null != unRegisteredTriggerIdentitySet) {
        sizeOfUnRegisteredTriggerIdentitySet = unRegisteredTriggerIdentitySet.size();
      }

      logger.error(
          "Error occurs when calculateRegisteredAndUnRegisteredTriggers. sizeOfTriggerIdentitysOnZK="
              + sizeOfTriggerIdentitysOnZK + " and sizeOfTriggerIdentitysInDB="
              + sizeOfTriggerIdentitysInDB + " and sizeOfRegisteredTriggerIdentitySet="
              + sizeOfRegisteredTriggerIdentitySet + " and sizeOfUnRegisteredTriggerIdentitySet="
              + sizeOfUnRegisteredTriggerIdentitySet, e);
      throw e;
    }
  }

  private void unRegisterTriggers(Set<TriggerIdentity> unRegisteredTriggerIdentitySet)
      throws Exception {
    if (null != unRegisteredTriggerIdentitySet) {
      for (TriggerIdentity triggerIdentity : unRegisteredTriggerIdentitySet) {
        this.isNormalOperationNeedToBeAborted(
            "unRegisterTriggers for " + this.getClass().getSimpleName(), true);

        try {
          //Need to cascadeCrossZoneDeleteTimerTrigger first because we need to get the trigger metadata which is now in registered state from db.
          triggerMetadataService.cascadeCrossZoneDeleteTimerTrigger(triggerIdentity);

          triggerMetadataService.unRegisterTrigger(triggerIdentity);
        } catch (Exception e) {
          logger.error(
              "Error occurs when try to unRegisterTrigger. triggerIdentity=" + KiraCommonUtils
                  .toString(triggerIdentity), e);
          throw e;
        }
      }
    }
  }

  private void registerTriggers(Set<TriggerIdentity> registeredTriggerIdentitySet)
      throws Exception {
    if (null != registeredTriggerIdentitySet) {
      for (TriggerIdentity triggerIdentity : registeredTriggerIdentitySet) {
        this.isNormalOperationNeedToBeAborted(
            "registerTriggers for " + this.getClass().getSimpleName(), true);

        try {
          triggerMetadataService.registerTrigger(triggerIdentity, this);
        } catch (Exception e) {
          logger.error("Error occurs when registerTrigger. triggerIdentity=" + KiraCommonUtils
              .toString(triggerIdentity), e);
          throw e;
        }
      }
    }
  }

  private void unsubscribeChildChangesForAllPoolOfTriggerOnZK() {
    try {
      //Set canBreakIfNormalOperationNeedToBeAborted to false to fully unsubscribeChildChangesForAllPoolOfTriggerOnZK.
      List<String> poolOfTriggerShortPathList = this.getAllPoolOfTriggerShortPathListOnZK(false);
      unsubscribeChildChangesForPoolOfTriggerOnZK(poolOfTriggerShortPathList);
    } catch (Exception e) {
      logger.error("Error occurs for subscribeChildChangesForAllPoolOfTriggerOnZK.", e);
    }
  }

  private void unsubscribeChildChangesForPoolOfTriggerOnZK(
      List<String> poolOfTriggerShortPathList) {
    if (!CollectionUtils.isEmpty(poolOfTriggerShortPathList)) {
      for (String poolOfTriggerShortPath : poolOfTriggerShortPathList) {
        try {
          this.unsubscribeChildChangesForPoolOfTriggerOnZK(poolOfTriggerShortPath);
        } catch (Exception e) {
          logger.error(
              "Error occurs for unsubscribeChildChangesForPoolOfTriggerOnZK. poolOfTriggerShortPathList="
                  + KiraCommonUtils.toString(poolOfTriggerShortPathList), e);
        }
      }
    }
  }

  private void unsubscribeChildChangesForPoolOfTriggerOnZK(String poolOfTriggerShortPath)
      throws Exception {
    try {
      String onePoolPath = KiraUtil
          .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, poolOfTriggerShortPath);
      String poolId = zkClient.readData(onePoolPath, true);
      if (StringUtils.isNotBlank(poolId)) {
        PoolOfTriggerZkChildListener poolOfTriggerZkChildListener = this.poolIdPoolOfTriggerZkChildListenerMap
            .get(poolId);
        if (null != poolOfTriggerZkChildListener) {
          zkClient.unsubscribeChildChanges(onePoolPath, poolOfTriggerZkChildListener);
        }

        //update cache
        this.poolIdPoolOfTriggerZkChildListenerMap.remove(poolId);
      } else {
        logger.warn("poolId is blank on onePoolPath={}" + onePoolPath);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for unsubscribeChildChangesForPoolOfTriggerOnZK. poolOfTriggerShortPath="
              + KiraCommonUtils.toString(poolOfTriggerShortPath), e);
      throw e;
    }
  }

  private void subscribeChildChangesForAllPoolOfTriggerOnZK() throws Exception {
    try {
      List<String> poolOfTriggerShortPathList = this.getAllPoolOfTriggerShortPathListOnZK(true);
      this.subscribeChildChangesForPoolOfTriggerOnZK(poolOfTriggerShortPathList);
    } catch (Exception e) {
      logger.error("Error occurs for subscribeChildChangesForAllPoolOfTriggerOnZK.", e);
      throw e;
    }
  }

  private List<String> getAllPoolOfTriggerShortPathListOnZK(
      boolean canBreakIfNormalOperationNeedToBeAborted) throws Exception {
    List<String> returnValue = new ArrayList<String>();
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolList)) {
        String onePoolPath = null;
        for (String onePoolShortPath : poolList) {
          if (canBreakIfNormalOperationNeedToBeAborted) {
            this.isNormalOperationNeedToBeAborted(
                "getAllPoolOfTriggerShortPathListOnZK for " + this.getClass().getSimpleName(),
                true);
          }

          onePoolPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolPath, true);
          if (poolZNodeData instanceof String) {
            //it is of type String. regard it as poolZNode
            returnValue.add(onePoolShortPath);
          }
        }
      }
    }
    return returnValue;
  }

  private void subscribeChildChangesForPoolOfTriggerOnZK(List<String> poolOfTriggerShortPathList)
      throws Exception {
    if (!CollectionUtils.isEmpty(poolOfTriggerShortPathList)) {
      for (String poolOfTriggerShortPath : poolOfTriggerShortPathList) {
        this.isNormalOperationNeedToBeAborted(
            "subscribeChildChangesForPoolOfTriggerOnZK for " + this.getClass().getSimpleName(),
            true);
        try {
          this.subscribeChildChangesForPoolOfTriggerOnZKIfNeeded(poolOfTriggerShortPath);
        } catch (Exception e) {
          logger.error(
              "Error occurs for subscribeChildChangesForPoolOfTriggerOnZK. poolOfTriggerShortPathList="
                  + KiraCommonUtils.toString(poolOfTriggerShortPathList), e);
          throw e;
        }
      }
    }
  }

  private void subscribeChildChangesForPoolOfTriggerOnZKIfNeeded(String poolOfTriggerShortPath)
      throws Exception {
    try {
      String onePoolPath = KiraUtil
          .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, poolOfTriggerShortPath);
      String poolId = zkClient.readData(onePoolPath, true);
      if (StringUtils.isNotBlank(poolId)) {
        if (null != this.poolIdPoolOfTriggerZkChildListenerMap.get(poolId)) {
          logger.info(
              "No need to subscribe the poolOfTriggerZkChildListener for it is already subscribed for poolId={} and onePoolPath={}",
              poolId, onePoolPath);
        } else {
          try {
            //need to call doHandleChildChangeForPoolOfTrigger to handle the triggers which is added before subscribe the childchanges of pool.
            if (zkClient.exists(onePoolPath)) {
              List<String> triggerShortPathList = zkClient.getChildren(onePoolPath);
              this.doHandleChildChangeForPoolOfTrigger(onePoolPath, triggerShortPathList);
            } else {
              logger.warn(
                  "The node do not exist for subscribeChildChangesForPoolOfTriggerOnZKIfNeeded. poolOfTriggerShortPath={} and onePoolPath={}",
                  poolOfTriggerShortPath, onePoolPath);
            }
          } catch (Exception e) {
            logger.error("Error occurs for call handleChildChangeForPoolOfTrigger. onePoolPath="
                + onePoolPath, e);
            throw e;
          }

          //unsubscribe first
          try {
            this.unsubscribeChildChangesForPoolOfTriggerOnZK(poolOfTriggerShortPath);
          } catch (Exception e) {
            logger.error(
                "Error occurs for unsubscribeChildChangesForPoolOfTriggerOnZK. poolOfTriggerShortPath="
                    + KiraCommonUtils.toString(poolOfTriggerShortPath), e);
            throw e;
          }

          PoolOfTriggerZkChildListener poolOfTriggerZkChildListener = new PoolOfTriggerZkChildListener(
              this);
          zkClient.subscribeChildChanges(onePoolPath, poolOfTriggerZkChildListener);

          //update cache
          this.poolIdPoolOfTriggerZkChildListenerMap.put(poolId, poolOfTriggerZkChildListener);
        }
      } else {
        logger.warn("poolId is blank on onePoolPath={}" + onePoolPath);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for subscribeChildChangesForPoolOfTriggerOnZKIfNeeded. poolOfTriggerShortPath="
              + KiraCommonUtils.toString(poolOfTriggerShortPath), e);
      throw e;
    }
  }

  private void unsubscribeChildChangesForTriggersZNode() {
    try {
      zkClient.unsubscribeChildChanges(KiraCommonConstants.ZK_PATH_TRIGGERS,
          this.triggersZkChildListener);
    } catch (Exception e) {
      logger.error("Error occurs when unsubscribeChildChangesForTriggersZNode.", e);
    }
  }

  private void subscribeChildChangesForTriggersZNode() throws Exception {
    try {
      zkClient.subscribeChildChanges(KiraCommonConstants.ZK_PATH_TRIGGERS, triggersZkChildListener);
    } catch (Exception e) {
      logger.error("Error occurs when subscribeChildChangesForTriggersZNode.subscribeChildChanges.",
          e);
      throw e;
    }
  }

  private void unsubscribeDataChangesForAllTriggers() {
    Set<TriggerIdentity> triggerIdentitySet = this.triggerIdentityTriggerZkDataListenerMap.keySet();
    if (null != triggerIdentitySet) {
      for (TriggerIdentity triggerIdentity : triggerIdentitySet) {
        try {
          unsubscribeDataChangesForTrigger(triggerIdentity);
        } catch (Exception e) {
          logger.error("Error occurs for unsubscribeDataChangesForAllTriggers. triggerIdentity="
              + KiraCommonUtils.toString(triggerIdentity), e);
        }
      }
    }
  }

  private void subscribeDataChangesForAllTriggersOnZK() throws Exception {
    try {
      List<TriggerIdentity> allTriggerIdentityOfPoolOnZK = this.getAllTriggerIdentityOnZK(false);
      this.subscribeDataChangesForTriggers(allTriggerIdentityOfPoolOnZK);
    } catch (Exception e) {
      logger.error("Error occurs when subscribeDataChangesForAllTriggersOnZK.", e);
      throw e;
    }
  }

  private List<TriggerIdentity> getAllTriggerIdentityOnZK(boolean includeVersion) throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
        List<String> poolList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
        if (!CollectionUtils.isEmpty(poolList)) {
          String onePoolPath = null;
          for (String onePoolShortPath : poolList) {
            this.isNormalOperationNeedToBeAborted(
                "getAllTriggerIdentityOnZK for " + this.getClass().getSimpleName(), true);

            onePoolPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
            Object poolZNodeData = zkClient.readData(onePoolPath, true);
            if (poolZNodeData instanceof TriggerMetadataZNodeData) {
              logger.error(
                  "Found TriggerMetadataZNodeData as the child node of triggers. onePoolPath={} and poolZNodeData={}",
                  onePoolPath, KiraCommonUtils.toString(poolZNodeData));
            } else {
              List<TriggerIdentity> triggerIdentitysUnderPoolZNode = KiraCommonUtils
                  .getAllTriggerIdentityUnderPoolZNode(onePoolPath, includeVersion);
              returnValue.addAll(triggerIdentitysUnderPoolZNode);
            }
          }
        }
      }
    } catch (Exception e) {
      logger
          .error("Error occurs for getAllTriggerIdentityOnZK. includeVersion=" + includeVersion, e);
      throw e;
    }

    return returnValue;
  }

  private void subscribeDataChangesForTriggers(List<TriggerIdentity> triggerIdentitysOnZK)
      throws Exception {
    if (!CollectionUtils.isEmpty(triggerIdentitysOnZK)) {
      for (TriggerIdentity triggerIdentity : triggerIdentitysOnZK) {
        try {
          this.isNormalOperationNeedToBeAborted(
              "subscribeDataChangesForTriggers for " + this.getClass().getSimpleName(), true);

          this.subscribeDataChangesForTrigger(triggerIdentity);
        } catch (Exception e) {
          logger.error("Error occurs for subscribeDataChangesForTriggers for triggerIdentity="
              + KiraCommonUtils.toString(triggerIdentity), e);
          throw e;
        }
      }
    }
  }

  private void subscribeChildChangesForAllPoolOfTriggerOnZKIfNeeded() {
    try {
      List<String> allPoolIdOfTriggerOnZK = KiraCommonUtils.getAllPoolIdOfTriggerOnZK();
      if (!CollectionUtils.isEmpty(allPoolIdOfTriggerOnZK)) {
        for (String onePoolIdOfTriggerOnZK : allPoolIdOfTriggerOnZK) {
          String filteredPoolId = KiraUtil.filterString(onePoolIdOfTriggerOnZK);
          this.subscribeChildChangesForPoolOfTriggerOnZKIfNeeded(filteredPoolId);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs on subscribeChildChangesForPoolOfTriggerOnZKIfNeeded.", e);
    }
  }

  private void checkAndHandlePoolsDeletedOnZK() {
    try {
      Set<String> deletedPoolSet = new LinkedHashSet<String>();
      deletedPoolSet.addAll(this.poolIdPoolOfTriggerZkChildListenerMap.keySet());
      List<String> allPoolIdOfTriggerOnZK = KiraCommonUtils.getAllPoolIdOfTriggerOnZK();
      if (!CollectionUtils.isEmpty(allPoolIdOfTriggerOnZK)) {
        for (String onePoolId : allPoolIdOfTriggerOnZK) {
          deletedPoolSet.remove(onePoolId);
        }
      }

      if (!CollectionUtils.isEmpty(deletedPoolSet)) {
        logger.info("deletedPoolSet detected. deletedPoolSet=" + deletedPoolSet);
        for (String onePoolId : deletedPoolSet) {
          try {
            logger.info("Will handlePoolDeletedOnZK for onePoolId=" + onePoolId);
            this.triggerMetadataService.handlePoolDeletedOnZK(onePoolId);
          } catch (Exception e) {
            logger.error("Error occurs for call handlePoolDeletedOnZK. onePoolId=" + onePoolId);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs on checkAndHandlePoolsDeletedOnZK.", e);
    }
  }

  private void doHandleChildChangeForPoolOfTrigger(String poolZNodePath,
      List<String> currentChilds) throws Exception {
    List<TriggerIdentity> allTriggerIdentityUnderPoolZNode = KiraCommonUtils
        .getAllTriggerIdentityUnderPoolZNode(poolZNodePath, true);
    Object poolZNodeData = zkClient.readData(poolZNodePath, true);
    String poolId = (String) poolZNodeData;
    if (StringUtils.isNotBlank(poolId)) {
      List<TriggerIdentity> allRegisteredAndUndeletedTriggerIdentitysForPoolInDB = triggerMetadataService
          .getAllRegisteredAndUndeletedTriggerIdentitysForPoolInDB(poolId);
      Set<TriggerIdentity> registeredTriggerIdentitySet = new LinkedHashSet<TriggerIdentity>();
      Set<TriggerIdentity> unRegisteredTriggerIdentitySet = new LinkedHashSet<TriggerIdentity>();
      this.calculateRegisteredAndUnRegisteredTriggers(allTriggerIdentityUnderPoolZNode,
          allRegisteredAndUndeletedTriggerIdentitysForPoolInDB, registeredTriggerIdentitySet,
          unRegisteredTriggerIdentitySet);
      this.unRegisterTriggers(unRegisteredTriggerIdentitySet);
      this.registerTriggers(registeredTriggerIdentitySet);
    } else {
      logger.info(
          "The found poolId is blank when doHandleChildChangeForPoolOfTrigger. That node may be deleted from zk now. poolZNodePath={} and currentChilds={}",
          poolZNodePath, currentChilds);
    }

  }

}
