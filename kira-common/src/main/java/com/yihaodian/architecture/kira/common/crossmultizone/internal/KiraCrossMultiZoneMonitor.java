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
package com.yihaodian.architecture.kira.common.crossmultizone.internal;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.InternalConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.crossmultizone.dto.KiraCrossMultiZoneData;
import com.yihaodian.architecture.kira.common.crossmultizone.dto.KiraCrossMultiZoneDataTrackContext;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneDataChangeEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneDataCheckEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneEventType;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneConstants;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.common.event.AsyncEventDispatcher;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class can not be restarted, it can only be destroyed.
 */
public class KiraCrossMultiZoneMonitor extends ComponentAdaptor implements
    IKiraCrossMultiZoneMonitor {

  private static Logger logger = LoggerFactory.getLogger(KiraCrossMultiZoneMonitor.class);

  private static KiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor;
  private final ReadWriteLock lockForKiraCrossMultiZoneRuntimeDataManagement = new ReentrantReadWriteLock();
  private AsyncEventDispatcher kiraCrossMultiZoneEventDispatcher;
  private ScheduledExecutorService doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService;
  private KiraMasterZoneZkDataListener kiraMasterZoneZkDataListener;
  private String currentKiraZoneId;
  //runtime data
  private String kiraMasterZoneId;
  private KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole;
  private ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public KiraCrossMultiZoneMonitor() throws Exception {
    this.init();
  }

  public static synchronized IKiraCrossMultiZoneMonitor getKiraCrossMultiZoneMonitor()
      throws Exception {
    if (null == KiraCrossMultiZoneMonitor.kiraCrossMultiZoneMonitor) {
      KiraCrossMultiZoneMonitor.kiraCrossMultiZoneMonitor = new KiraCrossMultiZoneMonitor();
      KiraCrossMultiZoneMonitor.kiraCrossMultiZoneMonitor.start();
    }
    return KiraCrossMultiZoneMonitor.kiraCrossMultiZoneMonitor;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  private void init() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Initializing KiraCrossMultiZoneMonitor...");

      this.initKiraCrossMultiZoneData();
      this.prepareForZKListeners();
      this.prepareDoRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService();
      this.prepareAsyncEventDispatcher();
      logger.info("Successfully initialize KiraCrossMultiZoneMonitor.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraCrossMultiZoneMonitor.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish initialize KiraCrossMultiZoneMonitor. And it takes {} milliseconds. currentKiraZoneId={} and kiraMasterZoneId={} and kiraCrossMultiZoneRole={}",
          costTime, currentKiraZoneId, kiraMasterZoneId, kiraCrossMultiZoneRole);
    }
  }

  private void initKiraCrossMultiZoneData() throws Exception {
    try {
      this.currentKiraZoneId = this.getCurrentKiraZoneId();
      if (StringUtils.isBlank(this.currentKiraZoneId)) {
        String errorMessage = "currentKiraZoneId should not be blank.";
        logger.error(errorMessage);
        throw new RuntimeException(errorMessage);
      }

      //Need to get the masterZoneId from zk and cache it first when initializing. If this is failed then throw exception out.
      String masterZone = LoadProertesContainer.provider()
          .getProperty(InternalConstants.KIRA_MASTER_ZONE, null);
      if (!zkClient.exists(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE)) {
        zkClient.createPersistent(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
            true);

        if (!StringUtils.isBlank(masterZone)) {
          zkClient.writeData(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
              masterZone);
          logger.warn("write masterZone data in zookeeper !" + masterZone);
        }
        logger.warn("The znode of kiraMasterZone do not exist! has created this node!");
        //String errorMessage = "The znode of kiraMasterZone do not exist. Please set the value for it first. zkPath="+KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE;
        //logger.error(errorMessage);
        //System.out.println("znode  is not exist!" + errorMessage);
        //throw new RuntimeException(errorMessage);
      } else {
        String kiraMasterZoneValueOnZK = zkClient
            .readData(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE, true);
        if (StringUtils.isBlank(kiraMasterZoneValueOnZK)) {
          kiraMasterZoneValueOnZK = masterZone.trim();
        } else if (StringUtils.isNotBlank(kiraMasterZoneValueOnZK)) {
          this.kiraMasterZoneId = kiraMasterZoneValueOnZK.trim();
          this.kiraCrossMultiZoneRole = this
              .calculateAndGetKiraCrossMultiZoneRole(currentKiraZoneId, kiraMasterZoneId);
        } else {
          String errorMessage =
              "The znode value of kiraMasterZone is blank. Please check the value for it first. zkPath="
                  + KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE;
          logger.error(errorMessage);
          System.out.println("find master zone is fail!" + errorMessage);
          //throw new RuntimeException(errorMessage);
        }
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when initKiraCrossMultiZoneData, Will just exit kira server process to avoid weired problems. Please solve those problems before start kira server again.",
          t);
      //System.exit(1);
    }
  }

  private void prepareForZKListeners() {
    this.kiraMasterZoneZkDataListener = new KiraMasterZoneZkDataListener();
  }

  private void prepareDoRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService() {
    ThreadFactory threadFactory = new CustomizedThreadFactory(
        "KiraCrossMultiZoneMonitor-doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService-");
    this.doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(threadFactory);
    this.doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService
        .scheduleAtFixedRate(new KiraCrossMultiZoneMonitorDoRoutineWorkTask(),
            KiraCrossMultiZoneConstants.KIRA_CROSS_MULTI_ZONE_MONITOR_DOROUTINEWORK_INITIALDELAY_SECOND,
            KiraCrossMultiZoneConstants.KIRA_CROSS_MULTI_ZONE_MONITOR_DOROUTINEWORK_PERIOD_SECOND,
            TimeUnit.SECONDS);
  }

  private void prepareAsyncEventDispatcher() throws Exception {
    ThreadFactory threadFactory = new CustomizedThreadFactory(
        "KiraCrossMultiZoneMonitor-AsyncEventDispatcher-");
    RejectedExecutionHandler kiraCrossMultiZoneEventDispatcherRejectedExecutionHandler = new KiraCrossMultiZoneEventDispatcherRejectedExecutionHandler();
    this.kiraCrossMultiZoneEventDispatcher = new AsyncEventDispatcher(1, 1, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>(), threadFactory,
        kiraCrossMultiZoneEventDispatcherRejectedExecutionHandler);
    this.kiraCrossMultiZoneEventDispatcher.init();
  }

  @Override
  protected boolean doStart() {
    boolean returnValue = false;
    try {
      //unsubscribe first
      this.unsubscribeDataChangesForKiraMasterZone();
      this.subscribeDataChangesForKiraMasterZone();
      returnValue = true;
    } catch (Exception e) {
      logger.error("Error occurs when call doStart() for KiraCrossMultiZoneMonitor.", e);
    }

    return returnValue;
  }

  private void unsubscribeDataChangesForKiraMasterZone() throws Exception {
    zkClient
        .unsubscribeDataChanges(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
            this.kiraMasterZoneZkDataListener);
  }

  private void subscribeDataChangesForKiraMasterZone() throws Exception {
    zkClient.subscribeDataChanges(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
        this.kiraMasterZoneZkDataListener);
  }

  @Override
  public void registerKiraCrossMultiZoneEventHandler(EventHandler eventHandler)
      throws Exception {
    if (null != eventHandler) {
      this.lockForComponentState.readLock().lock();
      try {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          if (null != this.kiraCrossMultiZoneEventDispatcher) {
            this.kiraCrossMultiZoneEventDispatcher
                .register(KiraCrossMultiZoneEventType.class, eventHandler);
          } else {
            throw new KiraHandleException(
                "kiraCrossMultiZoneEventDispatcher is null. It should not happen. May have some bugs. So can not registerKiraCrossMultiZoneEventHandler.");
          }
        } else {
          throw new KiraHandleException(
              "The KiraCrossMultiZoneMonitor is destroying or already destroyed. So can not registerKiraCrossMultiZoneEventHandler again.");
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      throw new KiraHandleException(
          "eventHandler is null. So can not registerKiraCrossMultiZoneEventHandler.");
    }
  }

  private boolean dispatchKiraCrossMultiZoneEvent(KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent) {
    boolean returnValue = false;

    try {
      logger.info("Will dispatch KiraCrossMultiZoneEvent.");

      if (null != kiraCrossMultiZoneEventDispatcher) {
        kiraCrossMultiZoneEventDispatcher.dispatch(kiraCrossMultiZoneEvent);
        returnValue = true;
        logger.info("Successfully dispatch kiraCrossMultiZoneEvent. kiraCrossMultiZoneEvent={}",
            kiraCrossMultiZoneEvent);
      } else {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          logger.error(
              "kiraCrossMultiZoneEventDispatcher is null. So the event may be lost! componentState={} and kiraCrossMultiZoneEvent={}",
              this.componentState.get(), kiraCrossMultiZoneEvent);
        } else {
          //just return true when it is destroyed.
          returnValue = true;
          logger.warn(
              "KiraCrossMultiZoneEvent will be discarded for KiraCrossMultiZoneMonitor is destroying or destroyed. kiraCrossMultiZoneEvent={}",
              kiraCrossMultiZoneEvent);
        }
      }
    } catch (Exception e) {
      logger.error(
          "Exception occurs when dispatching KiraCrossMultiZoneEvent. componentState={} and kiraCrossMultiZoneEvent={}",
          this.componentState.get(), kiraCrossMultiZoneEvent);
    } finally {
      logger.info("Finish dispatch KiraCrossMultiZoneEvent.");
    }

    return returnValue;
  }

  @Override
  protected void doShutdown() {
    super.doShutdown();
    try {
      this.unsubscribeDataChangesForKiraMasterZone();
    } catch (Exception e) {
      logger.error("Error occurs when unsubscribeDataChangesForKiraMasterZone in " + this.getClass()
          .getSimpleName() + ".", e);
    }

  }

  @Override
  protected void doDestroy() {
    super.doDestroy();

    this.destroyDoRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService();
    this.destroyKiraCrossMultiZoneEventDispatcher();
  }

  private void destroyDoRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService() {
    if (null != doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService) {
      this.doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService.shutdown();
      this.doRoutineWorkOfKiraCrossMultiZoneMonitorScheduledExecutorService = null;
    }
  }

  private void destroyKiraCrossMultiZoneEventDispatcher() {
    if (null != this.kiraCrossMultiZoneEventDispatcher) {
      this.kiraCrossMultiZoneEventDispatcher.destroy();
      this.kiraCrossMultiZoneEventDispatcher = null;
    }
  }

  private void doRoutineWorkOfKiraCrossMultiZoneMonitor() {
    long startTime = System.currentTimeMillis();
    boolean logHighLevel = true;
    try {
      logger.info("Start doRoutineWorkOfKiraCrossMultiZoneMonitor...");
      if (!this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestDestroy) {
            if (this.isStarted()) {
              this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().lock();
              try {
                String kiraMasterZoneValueOnZK = zkClient
                    .readData(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
                        true);
                if (StringUtils.isNotBlank(kiraMasterZoneValueOnZK)) {
                  String kiraZoneId = this.currentKiraZoneId;
                  String newKiraMasterZoneData = kiraMasterZoneValueOnZK.trim();

                  KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole = this
                      .calculateAndGetKiraCrossMultiZoneRole(kiraZoneId, newKiraMasterZoneData);
                  if (null != newKiraCrossMultiZoneRole) {
                    KiraCrossMultiZoneDataTrackContext kiraCrossMultiZoneDataTrackContext = this
                        .getCurrentKiraCrossMultiZoneDataTrackContext();
                    this.kiraMasterZoneId = newKiraMasterZoneData.trim();
                    this.kiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;

                    kiraCrossMultiZoneDataTrackContext
                        .setNewDataAndCalculateDifference(this.kiraMasterZoneId,
                            this.kiraCrossMultiZoneRole);
                    boolean dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess = this
                        .dispatchKiraCrossMultiZoneDataChangeEventIfChanged(
                            kiraCrossMultiZoneDataTrackContext);
                    if (!dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess) {
                      logger.error(
                          "Failed to dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess when doRoutineWorkOfKiraCrossMultiZoneMonitor. currentKiraZoneId={} and kiraCrossMultiZoneDataTrackContext={}",
                          currentKiraZoneId, kiraCrossMultiZoneDataTrackContext);
                    } else {
                      if (!kiraCrossMultiZoneDataTrackContext.isChanged()) {
                        logHighLevel = false;
                        if (!this.requestDestroy) {
                          //only dispatch KiraCrossMultiZoneDataCheckEvent when all works and no destroy request now.
                          boolean dispatchKiraCrossMultiZoneDataCheckEventSuccess = this
                              .dispatchKiraCrossMultiZoneDataCheckEvent();
                          if (!dispatchKiraCrossMultiZoneDataCheckEventSuccess) {
                            logger.error(
                                "Failed to dispatchKiraCrossMultiZoneDataCheckEvent when doRoutineWorkOfKiraCrossMultiZoneMonitor. currentKiraZoneId={} and kiraMasterZoneId={} and kiraCrossMultiZoneRole={} and kiraCrossMultiZoneDataTrackContext={}",
                                this.currentKiraZoneId, this.kiraMasterZoneId,
                                this.kiraCrossMultiZoneRole, kiraCrossMultiZoneDataTrackContext);
                          }
                        } else {
                          logger.info(
                              "KiraServer may request destroy. so do not dispatchKiraCrossMultiZoneDataCheckEvent. componentState={} and requestShutdown={} and requestDestroy={} and currentKiraZoneId={} and kiraMasterZoneId={} and kiraCrossMultiZoneRole={} and kiraCrossMultiZoneDataTrackContext={}",
                              this.componentState, this.requestShutdown, this.requestDestroy,
                              this.currentKiraZoneId, this.kiraMasterZoneId,
                              this.kiraCrossMultiZoneRole, kiraCrossMultiZoneDataTrackContext);
                        }
                      }
                    }
                  } else {
                    logger.error(
                        "Can not decide the KiraCrossMultiZoneRole when doRoutineWorkOfKiraCrossMultiZoneMonitor. May have some bugs. kiraZoneId={} and newKiraMasterZoneData={}",
                        kiraZoneId, newKiraMasterZoneData);
                  }
                } else {
                  String errorMessage =
                      "The znode value of kiraMasterZone is blank. Please check the value for it first. zkPath="
                          + KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE;
                  logger.error(errorMessage);
                }
              } finally {
                this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().unlock();
              }
            } else {
              logger.info(
                  "KiraCrossMultiZoneMonitor is not in the started. So do not do anything for doRoutineWorkOfKiraCrossMultiZoneMonitor. componentState={} and requestShutdown={} and requestDestroy={}",
                  this.componentState, this.requestShutdown, this.requestDestroy);
            }
          } else {
            logger.info(
                "KiraCrossMultiZoneMonitor may request destroy just now. So do not doRoutineWorkOfKiraCrossMultiZoneMonitor. componentState={} and requestShutdown={} and requestDestroy={}",
                KiraCrossMultiZoneMonitor.this.componentState, this.requestShutdown,
                this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.info(
            "KiraCrossMultiZoneMonitor may request destroy just now. So do not doRoutineWorkOfKiraCrossMultiZoneMonitor. componentState={} and requestShutdown={} and requestDestroy={}",
            KiraCrossMultiZoneMonitor.this.componentState, this.requestShutdown,
            this.requestDestroy);
      }
    } catch (Exception e) {
      logger.error("Error occurs when doRoutineWorkOfKiraCrossMultiZoneMonitor.", e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      if (logHighLevel) {
        logger.info("Finish doRoutineWorkOfKiraCrossMultiZoneMonitor. It takes {} milliseconds.",
            costTime);
      } else {
        logger.debug("Finish doRoutineWorkOfKiraCrossMultiZoneMonitor. It takes {} milliseconds.",
            costTime);
      }

    }
  }

  private boolean dispatchKiraCrossMultiZoneDataCheckEvent() {
    boolean returnValue = false;

    KiraCrossMultiZoneData kiraCrossMultiZoneData = new KiraCrossMultiZoneData(
        this.kiraMasterZoneId, this.kiraCrossMultiZoneRole);
    KiraCrossMultiZoneDataCheckEvent kiraCrossMultiZoneDataCheckEvent = new KiraCrossMultiZoneDataCheckEvent(
        kiraCrossMultiZoneData, KiraCrossMultiZoneEventType.KIRA_CROSS_MULTI_ZONE_DATA_CHECK);
    returnValue = this.dispatchKiraCrossMultiZoneEvent(kiraCrossMultiZoneDataCheckEvent);
    return returnValue;
  }

  private KiraCrossMultiZoneDataTrackContext getCurrentKiraCrossMultiZoneDataTrackContext() {
    KiraCrossMultiZoneDataTrackContext returnValue = new KiraCrossMultiZoneDataTrackContext(
        this.kiraMasterZoneId, this.kiraCrossMultiZoneRole);
    return returnValue;
  }

  private void handleKiraMasterZoneDataChange(String KiraMasterZoneZKPath,
      String newKiraMasterZoneData) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start handleKiraMasterZoneDataChange...");
      if (!this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestDestroy) {
            if (this.isStarted()) {
              this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().lock();
              try {
                if (StringUtils.isBlank(newKiraMasterZoneData)) {
                  logger.error(
                      "newKiraMasterZoneData is blank. newKiraMasterZoneData={} and KiraMasterZoneZKPath={}",
                      newKiraMasterZoneData, KiraMasterZoneZKPath);
                } else {
                  if (!StringUtils
                      .equals(newKiraMasterZoneData.trim(), this.kiraMasterZoneId.trim())) {
                    String kiraZoneId = this.currentKiraZoneId;
                    KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole = this
                        .calculateAndGetKiraCrossMultiZoneRole(kiraZoneId,
                            newKiraMasterZoneData.trim());
                    if (null != newKiraCrossMultiZoneRole) {
                      KiraCrossMultiZoneDataTrackContext kiraCrossMultiZoneDataTrackContext = this
                          .getCurrentKiraCrossMultiZoneDataTrackContext();
                      this.kiraMasterZoneId = newKiraMasterZoneData.trim();
                      this.kiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;

                      kiraCrossMultiZoneDataTrackContext
                          .setNewDataAndCalculateDifference(this.kiraMasterZoneId,
                              this.kiraCrossMultiZoneRole);
                      boolean dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess = this
                          .dispatchKiraCrossMultiZoneDataChangeEventIfChanged(
                              kiraCrossMultiZoneDataTrackContext);
                      if (!dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess) {
                        logger.error(
                            "Failed to dispatchKiraCrossMultiZoneDataChangeEventIfChangedSuccess when handleKiraMasterZoneDataChange. currentKiraZoneId={} and kiraCrossMultiZoneDataTrackContext={}",
                            currentKiraZoneId, kiraCrossMultiZoneDataTrackContext);
                      }
                    } else {
                      logger.error(
                          "Can not decide the KiraCrossMultiZoneRole when handleKiraMasterZoneDataChange. May have some bugs. kiraZoneId={} and newKiraMasterZoneData={}",
                          kiraZoneId, newKiraMasterZoneData);
                    }
                  } else {
                    logger.warn(
                        "handleKiraMasterZoneDataChange detected. But kiraMasterZoneId seems do not change. This seldom happens. newKiraMasterZoneData={} and this.kiraMasterZoneId={} and KiraMasterZoneZKPath={}",
                        newKiraMasterZoneData, this.kiraMasterZoneId, KiraMasterZoneZKPath);
                  }
                }
              } finally {
                this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().unlock();
              }
            } else {
              logger.warn(
                  "KiraCrossMultiZoneMonitor is not in the started state. So do not handleKiraMasterZoneDataChange. componentState={} and KiraMasterZoneZKPath={} and newKiraMasterZoneData={}",
                  this.componentState, KiraMasterZoneZKPath,
                  KiraCommonUtils.toString(newKiraMasterZoneData));
            }
          } else {
            logger.warn(
                "KiraCrossMultiZoneMonitor may request destroy. So do not handleKiraMasterZoneDataChange. componentState={} and requestShutdown={} and requestDestroy={} and KiraMasterZoneZKPath={} and newKiraMasterZoneData={}",
                this.componentState, this.requestShutdown, this.requestDestroy,
                KiraMasterZoneZKPath, KiraCommonUtils.toString(newKiraMasterZoneData));
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.warn(
            "KiraCrossMultiZoneMonitor may request destroy. So do not handleKiraMasterZoneDataChange. componentState={} and requestShutdown={} and requestDestroy={} and KiraMasterZoneZKPath={} and newKiraMasterZoneData={}",
            this.componentState, this.requestShutdown, this.requestDestroy, KiraMasterZoneZKPath,
            KiraCommonUtils.toString(newKiraMasterZoneData));
      }
    } catch (Exception e) {
      logger.error("Error occurs when handleKiraMasterZoneDataChange. KiraMasterZoneZKPath="
          + KiraMasterZoneZKPath + " and newKiraMasterZoneData=" + KiraCommonUtils
          .toString(newKiraMasterZoneData), e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn(
          "Finish handleKiraMasterZoneDataChange. KiraMasterZoneZKPath={} and it takes {} milliseconds.",
          KiraMasterZoneZKPath, costTime);
    }
  }

  private boolean dispatchKiraCrossMultiZoneDataChangeEventIfChanged(
      KiraCrossMultiZoneDataTrackContext kiraCrossMultiZoneDataTrackContext) {
    boolean returnValue = false;
    if (kiraCrossMultiZoneDataTrackContext.isChanged()) {
      KiraCrossMultiZoneDataChangeEvent kiraCrossMultiZoneDataChangeEvent = new KiraCrossMultiZoneDataChangeEvent(
          kiraCrossMultiZoneDataTrackContext,
          KiraCrossMultiZoneEventType.KIRA_CROSS_MULTI_ZONE_DATA_CHANGED);
      returnValue = this.dispatchKiraCrossMultiZoneEvent(kiraCrossMultiZoneDataChangeEvent);
    } else {
      //If it is not changed also return true
      returnValue = true;
    }
    return returnValue;
  }

  private KiraCrossMultiZoneRoleEnum calculateAndGetKiraCrossMultiZoneRole(
      String kiraZoneId, String kiraMasterZoneId) throws Exception {
    KiraCrossMultiZoneRoleEnum returnValue = null;
    if (StringUtils.isNotBlank(kiraZoneId) && StringUtils.isNotBlank(kiraMasterZoneId)) {
      if (StringUtils.equals(kiraZoneId, kiraMasterZoneId)) {
        returnValue = KiraCrossMultiZoneRoleEnum.MASTER;
      } else {
        returnValue = KiraCrossMultiZoneRoleEnum.SLAVE;
      }
    } else {
      String errorMessage =
          "kiraZoneId and kiraMasterZoneId should both be not blank to calculateAndGetKiraCrossMultiZoneRole. kiraZoneId="
              + kiraZoneId + " and kiraMasterZoneId=" + kiraMasterZoneId;
      logger.error(errorMessage);
      throw new ValidationException(errorMessage);
    }
    return returnValue;
  }

  @Override
  public String getCurrentKiraZoneId() throws Exception {
    String returnValue = this.currentKiraZoneId;
    if (StringUtils.isBlank(returnValue)) {
      try {
        //returnValue = ZoneContainer.getInstance().getLocalZoneName();
        returnValue = LoadProertesContainer.provider().getProperty("kira.currentZone", null);
        if (StringUtils.isBlank(returnValue)) {
          throw new RuntimeException(
              "ZoneContainer.getInstance().getLocalZoneName() return blank.");
        } else {
          returnValue = returnValue.trim();
        }
      } catch (Exception e) {
        logger.error("Error occurs when getCurrentKiraZoneId.", e);
        throw e;
      }
    }

    return returnValue;
  }

  @Override
  public String getKiraMasterZoneId(boolean accurate) throws Exception {
    String returnValue = null;

    if (accurate) {
      this.lockForComponentState.readLock().lock();
      try {
        if (this.isStarted(accurate)) {
          this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().lock();
          try {
            returnValue = this.kiraMasterZoneId;
          } finally {
            this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().unlock();
          }
        } else {
          throw new KiraHandleException(this.getClass().getSimpleName()
              + " is not in started state. So can not determine the correct value for getKiraMasterZoneId.");
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      if (this.isStarted(accurate)) {
        returnValue = this.kiraMasterZoneId;
      } else {
        throw new KiraHandleException(this.getClass().getSimpleName()
            + " is not in started state. So can not determine the correct value for getKiraMasterZoneId.");
      }
    }

    return returnValue;
  }

  @Override
  public KiraCrossMultiZoneRoleEnum getKiraCrossMultiZoneRole(boolean accurate) {
    KiraCrossMultiZoneRoleEnum returnValue = null;

    if (accurate) {
      this.lockForComponentState.readLock().lock();
      try {
        this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().lock();
        try {
          returnValue = this.kiraCrossMultiZoneRole;
        } finally {
          this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().unlock();
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      returnValue = this.kiraCrossMultiZoneRole;
    }

    return returnValue;
  }

  private class KiraCrossMultiZoneMonitorDoRoutineWorkTask implements Runnable {

    private Logger logger = LoggerFactory
        .getLogger(KiraCrossMultiZoneMonitorDoRoutineWorkTask.class);

    @Override
    public void run() {
      try {
        logger.info("Calling run() in KiraCrossMultiZoneMonitorDoRoutineWorkTask...");
        KiraCrossMultiZoneMonitor.this.doRoutineWorkOfKiraCrossMultiZoneMonitor();
      } catch (Exception e) {
        logger.error("Error occurs when call run() in KiraServerDoRoutineWorkTask.", e);
      } finally {
        logger.info("Finish call run() in KiraServerDoRoutineWorkTask.");
      }
    }
  }

  private class KiraMasterZoneZkDataListener implements IZkDataListener {

    private final Logger logger = LoggerFactory.getLogger(KiraMasterZoneZkDataListener.class);

    private KiraMasterZoneZkDataListener() {
      super();
    }

    @Override
    public void handleDataChange(String dataPath, Object data)
        throws Exception {
      try {
        logger.info("The data Change event of KiraMasterZone detected...dataPath={}", dataPath);
        if (null != data) {
          String newKiraMasterZoneData = (String) data;
          KiraCrossMultiZoneMonitor.this
              .handleKiraMasterZoneDataChange(dataPath, newKiraMasterZoneData);
        } else {
          logger.warn("data is null for KiraMasterZone dataPath={}", dataPath);
        }
      } catch (Exception e) {
        logger.error("Error occurs when handleDataChange for KiraMasterZone. dataPath=" + dataPath
            + " and data=" + KiraCommonUtils.toString(data), e);
        throw e;
      }
    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
      if (logger.isDebugEnabled()) {
        logger
            .debug("The data deleted event of KiraMasterZone detected. But do nothing. dataPath={}",
                dataPath);
      }
    }

  }

}
