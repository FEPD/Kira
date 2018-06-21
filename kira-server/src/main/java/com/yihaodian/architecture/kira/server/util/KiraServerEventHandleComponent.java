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
package com.yihaodian.architecture.kira.server.util;

import com.yihaodian.architecture.kira.common.ChangedSetHolder;
import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.ComponentStateEnum;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.KiraWorkCanceledException;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.dto.KiraServerRuntimeData;
import com.yihaodian.architecture.kira.server.dto.KiraServerTrackContext;
import com.yihaodian.architecture.kira.server.event.KiraServerChangedEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerEventType;
import com.yihaodian.architecture.kira.server.event.KiraServerRuntimeDataCheckEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerShutdownEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KiraServerEventHandleComponent extends ComponentAdaptor implements
    IKiraServerEventHandleComponent {

  protected final ReadWriteLock lockForKiraServerClusterRuntimeDataManagement = new ReentrantReadWriteLock();
  protected IKiraServer kiraServer;
  protected ThreadPoolExecutor kiraServerEventExecutorService;
  //kiraServer cluster runtimeData
  protected KiraServerRoleEnum kiraServerRole;
  protected LinkedHashSet<KiraServerEntity> allOtherKiraServers = new LinkedHashSet<KiraServerEntity>();

  protected KiraServerEventHandleComponent(IKiraServer kiraServer) throws Exception {
    this.kiraServer = kiraServer;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  protected boolean isLeaderServer() {
    return KiraServerRoleEnum.LEADER.equals(this.kiraServerRole);
  }

  protected boolean isFollowerServer() {
    return KiraServerRoleEnum.FOLLOWER.equals(this.kiraServerRole);
  }

  @Override
  public boolean isLeaderServer(boolean accurate) {
    boolean returnValue = false;

    if (accurate) {
      this.lockForComponentState.readLock().lock();
      try {
        this.lockForKiraServerClusterRuntimeDataManagement.readLock().lock();
        try {
          returnValue = this.isLeaderServer();
        } finally {
          this.lockForKiraServerClusterRuntimeDataManagement.readLock().unlock();
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      returnValue = this.isLeaderServer();
    }

    return returnValue;
  }

  protected void init() throws Exception {
    this.prepareKiraServerEventExecutorService();
    this.kiraServer.registerKiraServerEventHandler(this);
  }

  protected void prepareKiraServerEventExecutorService() throws Exception {
    ThreadFactory threadFactory = this.getThreadFactoryForKiraServerEventExecutorService();
    RejectedExecutionHandler kiraServerEventHandlerRejectedExecutionHandler = this
        .getRejectedExecutionHandlerForKiraServerEventExecutorService();
    int corePoolSize = this.getCorePoolSizeForKiraServerEventExecutorService();
    int maximumPoolSize = this.getMaximumPoolSizeForKiraServerEventExecutorService();
    int capacity = this.getCapacityOfQueueForKiraServerEventExecutorService();
    this.kiraServerEventExecutorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L,
        TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(capacity), threadFactory,
        kiraServerEventHandlerRejectedExecutionHandler);
  }

  protected ThreadFactory getThreadFactoryForKiraServerEventExecutorService() {
    String classSimpleName = this.getClass().getSimpleName();
    return new CustomizedThreadFactory(classSimpleName + "-KiraServerEventHandler-");
  }

  protected RejectedExecutionHandler getRejectedExecutionHandlerForKiraServerEventExecutorService() {
    return new KiraServerEventHandlerRejectedExecutionHandler();
  }

  protected int getCorePoolSizeForKiraServerEventExecutorService() {
    return 1;
  }

  protected int getMaximumPoolSizeForKiraServerEventExecutorService() {
    return 1;
  }

  protected int getCapacityOfQueueForKiraServerEventExecutorService() {
    return 1000;
  }

  @Override
  public void start() {
    //handleKiraServerStartedEvent will do the start stuff.
    throw new RuntimeException(this.getClass().getSimpleName()
        + ": Do not call start() directly. When kiraServer is started, KiraServerStartedEvent will be handled to start me.");
  }

  @Override
  public void handle(KiraServerEvent kiraServerEvent) {
    try {
      if (!kiraServerEventExecutorService.isShutdown()) {
        KiraServerEventHandleTask kiraServerEventHandleTask = new KiraServerEventHandleTask(
            kiraServerEvent);
        kiraServerEventExecutorService.submit(kiraServerEventHandleTask);
      } else {
        logger.warn(
            "Can not handle KiraServerEvent for kiraServerEventExecutorService is shutdown in "
                + this.getClass().getSimpleName() + ". kiraServerEvent={}", kiraServerEvent);
      }
    } catch (Exception e) {
      logger.error("Error occurs for handle kiraServerEvent in " + this.getClass().getSimpleName()
          + ". kiraServerEvent={}", kiraServerEvent);
    }
  }

  protected void handleKiraServerEvent(KiraServerEvent kiraServerEvent) {
    switch (kiraServerEvent.getEventType()) {
      case KIRA_SERVER_DETAIL_DATA_CHECK:
        KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent = (KiraServerRuntimeDataCheckEvent) kiraServerEvent;
        if (logger.isDebugEnabled()) {
          logger
              .debug(this.getClass().getSimpleName() + " caught kiraServerRuntimeDataCheckEvent={}",
                  kiraServerRuntimeDataCheckEvent);
        }
        this.handleKiraServerRuntimeDataCheckEvent(kiraServerRuntimeDataCheckEvent);
        break;
      case KIRA_SERVER_CHANGED:
        KiraServerChangedEvent kiraServerChangedEvent = (KiraServerChangedEvent) kiraServerEvent;
        logger.info(this.getClass().getSimpleName() + " caught kiraServerChangedEvent={}",
            kiraServerChangedEvent);
        this.handleKiraServerChangedEvent(kiraServerChangedEvent);
        break;
      case KIRA_SERVER_STARTED:
        KiraServerStartedEvent kiraServerStartedEvent = (KiraServerStartedEvent) kiraServerEvent;
        logger.info(this.getClass().getSimpleName() + " caught kiraServerStartedEvent={}",
            kiraServerStartedEvent);
        this.handleKiraServerStartedEvent(kiraServerStartedEvent);
        break;
      case KIRA_SERVER_SHUTDOWN:
        KiraServerShutdownEvent kiraServerShutdownEvent = (KiraServerShutdownEvent) kiraServerEvent;
        logger.info(this.getClass().getSimpleName() + " caught kiraServerShutdownEvent={}",
            kiraServerShutdownEvent);
        this.handleKiraServerShutdownEvent(kiraServerShutdownEvent);
        break;
      default:
        logger.error(
            this.getClass().getSimpleName() + " caught unknown eventType for kiraServerEvent="
                + kiraServerEvent);
    }
  }

  protected void handleKiraServerRuntimeDataCheckEvent(
      KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent) {
    try {
      if (!this.requestShutdown && !this.requestDestroy) {
        int componentStateOfKiraServer = kiraServerRuntimeDataCheckEvent.getKiraServerRuntimeData()
            .getComponentState();
        if (ComponentStateEnum.SHUTDOWN.getState() == componentStateOfKiraServer) {
          this.lockForComponentState.readLock().lock();
          try {
            if (!this.requestShutdown && !this.requestDestroy && !this.isShutdown()) {
              logger.warn(
                  "kiraServer may already be shutdown. But " + this.getClass().getSimpleName()
                      + " is not in shutdown state. So shutdown now. kiraServerRuntimeDataCheckEvent={}",
                  kiraServerRuntimeDataCheckEvent);
              this.shutdown();
            }
          } finally {
            this.lockForComponentState.readLock().unlock();
          }
        } else if (ComponentStateEnum.STARTED.getState() == componentStateOfKiraServer) {
          this.lockForComponentState.readLock().lock();
          try {
            if (!this.requestShutdown && !this.requestDestroy) {
              if (this.isStarted()) {
                this.doHandleKiraServerRuntimeDataCheckEventWhenKiraServerStarted(
                    kiraServerRuntimeDataCheckEvent);
              } else {
                logger.warn(this.getClass().getSimpleName()
                        + " is not in the started state. So do not handleKiraServerRuntimeDataCheckEvent. componentState={} and kiraServerRuntimeDataCheckEvent={}",
                    this.componentState, kiraServerRuntimeDataCheckEvent);
              }
            } else {
              logger.warn(this.getClass().getSimpleName()
                      + " may request shutdown or destroy. So do not doHandleKiraServerRuntimeDataCheckEventWhenStarted. kiraServerRuntimeDataCheckEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                  kiraServerRuntimeDataCheckEvent, this.componentState, this.requestShutdown,
                  this.requestDestroy);
            }
          } finally {
            this.lockForComponentState.readLock().unlock();
          }
        } else {
          logger.error(this.getClass().getSimpleName()
                  + ": kiraServerRuntimeDataCheckEvent should only be sent when kiraServer is started or shutdown. May have some bugs. kiraServerRuntimeDataCheckEvent={}",
              kiraServerRuntimeDataCheckEvent);
        }
      } else {
        logger.warn(this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not handleKiraServerRuntimeDataCheckEvent. kiraServerRuntimeDataCheckEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
            kiraServerRuntimeDataCheckEvent, this.componentState, this.requestShutdown,
            this.requestDestroy);
      }
    } catch (KiraWorkCanceledException k) {
      logger.warn(
          "KiraWorkCanceledException occurs during call handleKiraServerRuntimeDataCheckEvent in"
              + this.getClass().getSimpleName() + ". kiraServerRuntimeDataCheckEvent="
              + kiraServerRuntimeDataCheckEvent, k);
    } catch (Exception e) {
      logger.error(
          "Error occurs during call handleKiraServerRuntimeDataCheckEvent in" + this.getClass()
              .getSimpleName() + ". kiraServerRuntimeDataCheckEvent="
              + kiraServerRuntimeDataCheckEvent, e);
    }
  }

  protected void doHandleKiraServerRuntimeDataCheckEventWhenKiraServerStarted(
      KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent) throws Exception {
    logger.info("Starting doHandleKiraServerRuntimeDataCheckEventWhenStarted for " + this.getClass()
        .getSimpleName() + "...");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        KiraServerRoleEnum newKiraServerRole = kiraServerRuntimeDataCheckEvent
            .getKiraServerRuntimeData().getKiraServerRole();
        if (null != newKiraServerRole) {
          LinkedHashSet<KiraServerEntity> newAllOtherKiraServers = kiraServerRuntimeDataCheckEvent
              .getKiraServerRuntimeData().getAllOtherKiraServers();
          this.handleNewRunTimeDataOfKiraServer(newKiraServerRole, newAllOtherKiraServers);
        } else {
          logger.error(
              "newKiraServerRole should not be null when doHandleKiraServerRuntimeDataCheckEventWhenStarted for "
                  + this.getClass().getSimpleName()
                  + ". May have some bugs. kiraServerRuntimeDataCheckEvent={}",
              kiraServerRuntimeDataCheckEvent);
        }
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish doHandleKiraServerRuntimeDataCheckEventWhenStarted for " + this.getClass()
                .getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  private void handleKiraServerChangedEvent(KiraServerChangedEvent kiraServerChangedEvent) {
    try {
      if (!this.requestShutdown && !this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.isStarted()) {
              this.doHandleKiraServerChangedEventWhenStarted(kiraServerChangedEvent);
            } else {
              logger.warn(this.getClass().getSimpleName()
                      + " is not in the started state. So do not handleKiraServerChangedEvent. componentState={} and kiraServerChangedEvent={}",
                  this.componentState, kiraServerChangedEvent);
            }
          } else {
            logger.warn(this.getClass().getSimpleName()
                    + " may request shutdown or destroy. So do not doHandleKiraServerChangedEvent. kiraServerChangedEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                kiraServerChangedEvent, this.componentState, this.requestShutdown,
                this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.warn(this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not handleKiraServerChangedEvent for " + this
                .getClass().getSimpleName()
                + ". kiraServerChangedEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
            kiraServerChangedEvent, this.componentState, this.requestShutdown, this.requestDestroy);
      }
    } catch (KiraWorkCanceledException k) {
      logger.warn(
          "KiraWorkCanceledException occurs during call handleKiraServerChangedEvent for " + this
              .getClass().getSimpleName() + ". kiraServerChangedEvent=" + kiraServerChangedEvent,
          k);
    } catch (Exception e) {
      logger.error("Error occurs during call handleKiraServerChangedEvent for " + this.getClass()
          .getSimpleName() + ". kiraServerChangedEvent=" + kiraServerChangedEvent, e);
    }
  }

  protected void doHandleKiraServerChangedEventWhenStarted(
      KiraServerChangedEvent kiraServerChangedEvent) throws Exception {
    logger.info(
        "Starting doHandleKiraServerChangedEventWhenStarted for " + this.getClass().getSimpleName()
            + "...");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        KiraServerTrackContext kiraServerTrackContext = kiraServerChangedEvent
            .getKiraServerTrackContext();
        KiraServerRoleEnum newKiraServerRole = kiraServerTrackContext.getNewKiraServerRole();
        if (null != newKiraServerRole) {
          LinkedHashSet<KiraServerEntity> newAllOtherKiraServers = kiraServerTrackContext
              .getNewAllOtherKiraServers();
          this.handleNewRunTimeDataOfKiraServer(newKiraServerRole, newAllOtherKiraServers);
        } else {
          logger.error(
              "newKiraServerRole should not be null when handleKiraServerChangedEvent for " + this
                  .getClass().getSimpleName() + ". May have some bugs. kiraServerChangedEvent={}",
              kiraServerChangedEvent);
        }
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish doHandleKiraServerChangedEventWhenStarted for " + this.getClass()
            .getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  protected void handleKiraServerStartedEvent(KiraServerStartedEvent kiraServerStartedEvent) {
    //always set requestShutdown to false for it can be restarted.
    this.requestShutdown = false;
    boolean needRetry = true;
    do {
      try {
        this.lockForComponentState.writeLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.componentState.compareAndSet(ComponentStateEnum.INIT.getState(),
                ComponentStateEnum.STARTING.getState())
                || this.componentState.compareAndSet(ComponentStateEnum.SHUTDOWN.getState(),
                ComponentStateEnum.STARTING.getState())
                || ComponentStateEnum.STARTING.getState() == this.componentState.get()) {
              this.doHandleKiraServerStartedEvent(kiraServerStartedEvent);
              needRetry = false;
            } else {
              if (this.requestShutdown || this.requestDestroy) {
                needRetry = false;
                logger.warn(this.getClass().getSimpleName()
                        + " may request shutdown or destroy. So do not retry handleKiraServerStartedEvent. kiraServerStartedEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                    kiraServerStartedEvent, this.componentState, this.requestShutdown,
                    this.requestDestroy);
              } else {
                needRetry = false;
                String errorMessage = this.getClass().getSimpleName()
                    + " failed to handleKiraServerStartedEvent. It may not be in correct state. May have bugs. kiraServerStartedEvent="
                    + kiraServerStartedEvent + " and componentState=" + this.componentState;
                logger.error(errorMessage);
              }
            }
          } else {
            needRetry = false;
            logger.warn(this.getClass().getSimpleName()
                    + ": KiraServer may request shutdown or destroy. So do not continue to handleKiraServerStartedEvent. kiraServerStartedEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                kiraServerStartedEvent, this.componentState, this.requestShutdown,
                this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.writeLock().unlock();
        }
      } catch (KiraWorkCanceledException k) {
        logger.warn(
            "KiraWorkCanceledException occurs during call handleKiraServerStartedEvent() in " + this
                .getClass().getSimpleName() + " . kiraServerStartedEvent=" + kiraServerStartedEvent,
            k);
      } catch (Exception e) {
        logger.error(
            "Exception occurs during call handleKiraServerStartedEvent() in " + this.getClass()
                .getSimpleName() + " . kiraServerStartedEvent=" + kiraServerStartedEvent, e);
      } catch (Error e) {
        logger.error("Error occurs during call handleKiraServerStartedEvent() in " + this.getClass()
            .getSimpleName() + " . So do not retry. kiraServerStartedEvent="
            + kiraServerStartedEvent, e);
        //If error occurs do not retry for it may has serious error occurs.
        needRetry = false;
      } finally {
        if (needRetry) {
          try {
            logger.info(
                "Will retry handleKiraServerStartedEvent for " + this.getClass().getSimpleName());
            try {
              Thread.sleep(KiraCommonConstants.RETRY_INTERVAL_MILLISECOND);
            } catch (InterruptedException e1) {
              if (this.requestShutdown || this.requestDestroy) {
                needRetry = false;
                logger.warn(this.getClass().getSimpleName()
                        + " may request destroy. So do not retry start. kiraServerStartedEvent={}",
                    kiraServerStartedEvent);
              }
              Thread.currentThread().interrupt();
            }
          } catch (Exception e) {
            logger.error(
                "Error occurs during wait for retry to handleKiraServerStartedEvent() in " + this
                    .getClass().getSimpleName() + " . kiraServerStartedEvent="
                    + kiraServerStartedEvent + " and componentState=" + this.componentState, e);
          }
        }
      }
    } while (needRetry && !this.requestShutdown && !this.requestDestroy);
  }

  protected void doHandleKiraServerStartedEvent(KiraServerStartedEvent kiraServerStartedEvent)
      throws Exception {
    logger.info(
        "Begin doHandleKiraServerStartedEvent in " + this.getClass().getSimpleName() + " ...");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        this.startByKiraServerStartedEvent(kiraServerStartedEvent);
        this.componentState.set(ComponentStateEnum.STARTED.getState());
        this.lastStartedTime = new Date();

        try {
          afterStartByKiraServerStartedEventSuccess();
        } catch (KiraWorkCanceledException k) {
          logger.warn(
              "KiraWorkCanceledException occurs when call afterStartByKiraServerStartedEventSuccess() in "
                  + this.getClass().getSimpleName(), k);
          //Do not throw out exception for it is already be marked as started.
        } catch (Throwable t) {
          logger.error(
              "Error occurs when call afterStartByKiraServerStartedEventSuccess() in " + this
                  .getClass().getSimpleName(), t);
          //Do not throw out exception for it is already be marked as started.
        }

        logger.info(
            "Successfully doHandleKiraServerStartedEvent in " + this.getClass().getSimpleName());
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish doHandleKiraServerStartedEvent in " + this.getClass().getSimpleName()
            + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  protected void afterStartByKiraServerStartedEventSuccess() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info(
          "Start afterStartByKiraServerStartedEventSuccess... in " + this.getClass().getSimpleName()
              + " ...");
      if (!this.requestShutdown && !this.requestDestroy) {
        if (this.isLeaderServer()) {
          this.doLeaderRoutineWork();
        } else if (this.isFollowerServer()) {
          this.doFollowerRoutineWork();
        } else {
          logger.warn(
              "afterStartByKiraServerStartedEventSuccess is called in server which is neither leader or follower. May have some bugs. kiraServerRole={}",
              this.kiraServerRole);
        }
      } else {
        logger.warn(this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not tryToDoLeaderRoutineWork. componentState={} and requestShutdown={} and requestDestroy={}",
            this.componentState, this.requestShutdown, this.requestDestroy);
      }
    } catch (KiraWorkCanceledException k) {
      logger.warn(
          "KiraWorkCanceledException occurs when afterStartByKiraServerStartedEventSuccess for "
              + this.getClass().getSimpleName() + " and componentState=" + this.componentState
              + " and requestShutdown=" + this.requestShutdown + " and requestDestroy="
              + this.requestDestroy, k);
    } catch (Exception e) {
      logger.error(
          "Error occurs when afterStartByKiraServerStartedEventSuccess for " + this.getClass()
              .getSimpleName() + " and componentState=" + this.componentState
              + " and requestShutdown=" + this.requestShutdown + " and requestDestroy="
              + this.requestDestroy, e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish afterStartByKiraServerStartedEventSuccess for " + this.getClass().getSimpleName()
              + ". It takes {} milliseconds.", costTime);
    }
  }

  protected void startByKiraServerStartedEvent(KiraServerStartedEvent kiraServerStartedEvent)
      throws Exception {
    KiraServerRuntimeData kiraServerRuntimeData = kiraServerStartedEvent.getKiraServerRuntimeData();
    KiraServerRoleEnum newKiraServerRole = kiraServerRuntimeData.getKiraServerRole();
    if (null != newKiraServerRole) {
      LinkedHashSet<KiraServerEntity> newAllOtherKiraServers = kiraServerRuntimeData
          .getAllOtherKiraServers();
      this.handleNewRunTimeDataOfKiraServer(newKiraServerRole, newAllOtherKiraServers);
    } else {
      logger.error(
          "newKiraServerRole should not be null for startByKiraServerStartedEvent. May have some bugs. kiraServerStartedEvent={}",
          kiraServerStartedEvent);
    }
  }

  protected void handleNewRunTimeDataOfKiraServer(KiraServerRoleEnum newKiraServerRole,
      LinkedHashSet<KiraServerEntity> newAllOtherKiraServers) throws Exception {
    if (!newKiraServerRole.equals(this.kiraServerRole)) {
      if (KiraServerRoleEnum.LEADER.equals(newKiraServerRole)) {
        this.changeToWorkAsLeader(newAllOtherKiraServers);
      } else if (KiraServerRoleEnum.FOLLOWER.equals(newKiraServerRole)) {
        this.changeToWorkAsFollower();
      } else {
        String errorMessage =
            "Unknown newKiraServerRole found when handleNewRunTimeData. newKiraServerRole="
                + newKiraServerRole + " and newAllOtherKiraServers=" + newAllOtherKiraServers;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } else {
      if (KiraServerRoleEnum.LEADER.equals(newKiraServerRole)) {
        this.continueToWorkAsLeader(newAllOtherKiraServers);
      } else if (KiraServerRoleEnum.FOLLOWER.equals(newKiraServerRole)) {
        this.continueToWorkAsFollower();
      } else {
        String errorMessage =
            "Unknown newKiraServerRole found when handleNewRunTimeData. newKiraServerRole="
                + newKiraServerRole + " and newAllOtherKiraServers=" + newAllOtherKiraServers;
        throw new KiraHandleException(errorMessage);
      }
    }
  }

  protected void continueToWorkAsLeader(LinkedHashSet<KiraServerEntity> newAllOtherKiraServers)
      throws Exception {
    //If the role do not changed, need to check if others changed.
    ChangedSetHolder<KiraServerEntity> allOtherKiraServersChangedSetHolder = new ChangedSetHolder<KiraServerEntity>(
        (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers.clone(),
        (LinkedHashSet<KiraServerEntity>) newAllOtherKiraServers.clone());
    if (allOtherKiraServersChangedSetHolder.isTheSetChanged()) {
      logger.info(
          "kiraServerRole do not changed and i am leader and allOtherKiraServersChanged. Will call handleAllOtherKiraServersChangedWhenAsLeader.");
      long startTime = System.currentTimeMillis();
      try {
        this.handleAllOtherKiraServersChangedWhenAsLeader(allOtherKiraServersChangedSetHolder);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish handleAllOtherKiraServersChangedWhenAsLeader for " + this.getClass()
                .getSimpleName()
                + ". It takes {} milliseconds. and allOtherKiraServersChangedSetHolder={}", costTime,
            allOtherKiraServersChangedSetHolder);
      }
    } else {
      logger.info(
          "kiraServerRole do not changed and i am leader and no allOtherKiraServersChanged. So do not do anything for handleNewRunTimeDataOfKiraServer.");
    }
  }

  protected void continueToWorkAsFollower() throws Exception {
    logger.info(
        "kiraServerRole do not changed and i am follower. So do not do anything for handleNewRunTimeDataOfKiraServer.");
  }

  protected void handleAllOtherKiraServersChangedWhenAsLeader(
      ChangedSetHolder<KiraServerEntity> allOtherKiraServersChangedSetHolder) throws Exception {
    this.allOtherKiraServers = allOtherKiraServersChangedSetHolder.getNewSet();
  }

  protected void changeToWorkAsLeader(LinkedHashSet<KiraServerEntity> newAllOtherKiraServers)
      throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start changeToWorkAsLeader....");
      this.beforeChangeToWorkAsLeader();

      this.kiraServerRole = KiraServerRoleEnum.LEADER;
      this.allOtherKiraServers = newAllOtherKiraServers;
      //No need to do more to deal with newAllOtherKiraServers

      this.afterChangeToWorkAsLeader();
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish changeToWorkAsLeader for " + this.getClass().getSimpleName()
              + ". It takes {} milliseconds. and newAllOtherKiraServers={}", costTime,
          newAllOtherKiraServers);
    }
  }

  protected void beforeChangeToWorkAsLeader() throws Exception {

  }

  protected void afterChangeToWorkAsLeader() throws Exception {
    if (this.isLeaderServer()) {
      if (this.isStarted()) {
        this.isNormalOperationNeedToBeAborted(
            "afterChangeToWorkAsLeader for " + this.getClass().getSimpleName(), true);

        //Need to doLeaderRoutineWork when it is started so doLeaderRoutineWork will not be called when is starting. And it will be called in afterStartByKiraServerStartedEventSuccess.
        this.doLeaderRoutineWork();
      }
    } else {
      logger.warn(
          "afterChangeToWorkAsLeader is called in server which is not leader. May have some bugs. kiraServerRole={}",
          this.kiraServerRole);
    }
  }

  protected void changeToWorkAsFollower() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start changeToWorkAsFollower....");
      beforeChangeToWorkAsFollower();

      this.kiraServerRole = KiraServerRoleEnum.FOLLOWER;
      this.allOtherKiraServers.clear();

      afterChangeToWorkAsFollower();
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish changeToWorkAsFollower for " + this.getClass().getSimpleName()
          + ". It takes {} milliseconds.", costTime);
    }
  }

  protected void beforeChangeToWorkAsFollower() throws Exception {

  }

  protected void afterChangeToWorkAsFollower() throws Exception {
    if (this.isFollowerServer()) {
      if (this.isStarted()) {
        //Need to doFollowerRoutineWork when it is started so doFollowerRoutineWork will not be called when started. And it will be called in afterStartByKiraServerStartedEventSuccess.
        this.doFollowerRoutineWork();
      }
    } else {
      logger.warn(
          "afterChangeToWorkAsFollower is called in server which is not follower. May have some bugs. kiraServerRole={}",
          this.kiraServerRole);
    }
  }

  @Override
  public void tryToDoLeaderRoutineWork() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger
          .info("Start tryToDoLeaderRoutineWork... in " + this.getClass().getSimpleName() + " ...");
      if (!this.requestShutdown && !this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.isStarted()) {
              this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
              try {
                if (this.isLeaderServer()) {
                  this.doLeaderRoutineWork();
                } else {
                  String errorMessage =
                      "tryToDoLeaderRoutineWork is called in server which is not leader. May have some bugs. kiraServerRole="
                          + this.kiraServerRole;
                  throw new KiraHandleException(errorMessage);
                }
              } finally {
                this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
              }
            } else {
              String errorMessage = this.getClass().getSimpleName()
                  + " is not in the started state. So do not tryToDoLeaderRoutineWork. componentState="
                  + this.componentState;
              throw new KiraHandleException(errorMessage);
            }
          } else {
            String errorMessage = this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not tryToDoLeaderRoutineWork. componentState="
                + this.componentState + " and requestShutdown=" + this.requestShutdown
                + " and requestDestroy=" + this.requestDestroy;
            throw new KiraHandleException(errorMessage);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        String errorMessage = this.getClass().getSimpleName()
            + " may request shutdown or destroy. So do not tryToDoLeaderRoutineWork. componentState="
            + this.componentState + " and requestShutdown=" + this.requestShutdown
            + " and requestDestroy=" + this.requestDestroy;
        throw new KiraHandleException(errorMessage);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when tryToDoLeaderRoutineWork for " + this.getClass().getSimpleName()
              + " and componentState=" + this.componentState + " and requestShutdown="
              + this.requestShutdown + " and requestDestroy=" + this.requestDestroy, e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish tryToDoLeaderRoutineWork for " + this.getClass().getSimpleName()
          + ". It takes {} milliseconds.", costTime);
    }
  }

  @Override
  public void tryToDoFollowerRoutineWork() throws Exception {
  }

  protected void doLeaderRoutineWork() throws Exception {
    this.isNormalOperationNeedToBeAborted(
        "doLeaderRoutineWork for " + this.getClass().getSimpleName(), true);
  }

  protected void doFollowerRoutineWork() throws Exception {
    this.isNormalOperationNeedToBeAborted(
        "doFollowerRoutineWork for " + this.getClass().getSimpleName(), true);
  }

  @Override
  public boolean isHasPendingEvent(Set<KiraServerEventType> kiraServerEventTypes) {
    boolean returnValue = false;
    try {
      if (CollectionUtils.isNotEmpty(kiraServerEventTypes)) {
        ThreadPoolExecutor kiraServerEventExecutorService = this.kiraServerEventExecutorService;
        if (null != kiraServerEventExecutorService) {
          if (!kiraServerEventExecutorService.isShutdown()) {
            BlockingQueue<Runnable> kiraServerEventHandleTaskQueue = kiraServerEventExecutorService
                .getQueue();
            if (null != kiraServerEventHandleTaskQueue) {
              int pendingCount = kiraServerEventHandleTaskQueue.size();
              if (pendingCount > 0) {
                if (null != logger && logger.isInfoEnabled()) {
                  logger.info(
                      "Found total pendingCount={} in isHasPendingEvent() kiraServerEventTypes={}",
                      pendingCount, kiraServerEventTypes);
                }

                for (Runnable kiraServerEventHandleTaskRunnable : kiraServerEventHandleTaskQueue) {
                  if (kiraServerEventHandleTaskRunnable instanceof FutureTask) {
                    FutureTask kiraServerEventHandleTaskFutureTask = (FutureTask) kiraServerEventHandleTaskRunnable;
                    Object callableObject = KiraCommonUtils
                        .getCallableTaskObjectFromFutureTask(kiraServerEventHandleTaskFutureTask);
                    if (callableObject instanceof KiraServerEventHandleTask) {
                      KiraServerEventHandleTask kiraServerEventHandleTask = (KiraServerEventHandleTask) callableObject;
                      KiraServerEvent kiraServerEvent = kiraServerEventHandleTask
                          .getKiraServerEvent();
                      if (null != kiraServerEvent) {
                        KiraServerEventType eventType = kiraServerEvent.getEventType();
                        if (null != eventType) {
                          if (kiraServerEventTypes.contains(eventType)) {
                            returnValue = true;
                            if (null != logger) {
                              logger.info(
                                  "Found KiraServerEventType={} in isHasPendingEvent() kiraServerEventTypes={} and pendingCount={}",
                                  eventType, kiraServerEventTypes, pendingCount);
                            }
                            break;
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      if (null != logger) {
        logger.error("Error occurs when call isHasPendingEvent(). kiraServerEventTypes="
            + kiraServerEventTypes, t);
      }
    }
    return returnValue;
  }

  protected void clearAllRunTimeData() {
    this.kiraServerRole = null;
    this.allOtherKiraServers.clear();
  }

  protected void handleKiraServerShutdownEvent(KiraServerShutdownEvent kiraServerShutdownEvent) {
    //Do not handle kiraServerShutdownEvent now. Just call shutdown() by default.
    this.shutdown();
  }

  @Override
  public void shutdown() {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start to call shutdown() for " + this.getClass().getSimpleName()
          + " in KiraServerEventHandleComponent.");

      this.requestShutdown = true;
      this.lockForComponentState.writeLock().lock();
      try {
        if (this.componentState.compareAndSet(ComponentStateEnum.STARTED.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())
            || this.componentState.compareAndSet(ComponentStateEnum.STARTING.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())) {
          try {
            this.doShutdown();
          } finally {
            this.componentState.set(ComponentStateEnum.SHUTDOWN.getState());
          }
        } else {
          if (this.requestDestroy) {
            logger.info(this.getClass().getSimpleName()
                + " may request destroy. But it is already not in the started state. So do nothing for shutdown "
                + this.getClass().getSimpleName() + ". componentState={}", this.componentState);
          } else {
            if (this.isInInitState()) {
              logger.info(
                  this.getClass().getSimpleName() + " is in init state. So no need to shutdown.");
            } else if (this.isShutdown()) {
              logger.info(this.getClass().getSimpleName()
                  + " is already in shutdown state. So do not shutdown again.");
            } else {
              String errorMessage = "Failed to shutdown " + this.getClass().getSimpleName()
                  + ". It may not in started state. componentState=" + this.componentState;
              logger.error(errorMessage);
            }
          }
        }
      } finally {
        this.lockForComponentState.writeLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when call shutdown() for " + this.getClass().getSimpleName() + ".",
          e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish to call shutdown() for " + this.getClass().getSimpleName()
          + " in KiraServerEventHandleComponent. It takes {} milliseconds.", costTime);
    }
  }

  @Override
  protected void doShutdown() {
    super.doShutdown();
    logger.info(
        "Will doShutdown in KiraServerEventHandleComponent for " + this.getClass().getSimpleName()
            + ".");
    this.lockForKiraServerClusterRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        logger.info(
            "Shutting down in KiraServerEventHandleComponent for " + this.getClass().getSimpleName()
                + "...");
        this.clearAllRunTimeData();

        logger.info("Successfully shutdown in KiraServerEventHandleComponent for " + this.getClass()
            .getSimpleName() + ".");
      } catch (Exception e) {
        logger.error("Error occurs when shutting down in KiraServerEventHandleComponent for " + this
            .getClass().getSimpleName() + ".", e);
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish shutdown in KiraServerEventHandleComponent for " + this.getClass()
            .getSimpleName() + ". It takes {} milliseconds.", costTime);
      }
    } finally {
      this.lockForKiraServerClusterRuntimeDataManagement.writeLock().unlock();
    }
  }

  protected void destroyKiraServerEventExecutorService() {
    if (null != this.kiraServerEventExecutorService) {
      this.kiraServerEventExecutorService.shutdown();
    }
  }

  @Override
  protected void doDestroy() {
    this.destroyKiraServerEventExecutorService();
  }

  protected <T> T doWork(String workDescription, String workContextInfo, Callable<T> work,
      ReadWriteLock readWriteLockForBusiness, boolean needWriteLock) throws Exception {
    this.isNormalOperationNeedToBeAborted(
        workDescription + " for " + this.getClass().getSimpleName(), true);

    T returnValue = null;
    this.lockForComponentState.readLock().lock();
    try {
      if (this.isStarted()) {
        this.lockForKiraServerClusterRuntimeDataManagement.readLock().lock();
        try {
          if (this.isLeaderServer() || this.isFollowerServer()) {
            if (this.isLeaderServer()) {
              if (null != readWriteLockForBusiness) {
                if (needWriteLock) {
                  readWriteLockForBusiness.writeLock().lock();
                } else {
                  readWriteLockForBusiness.readLock().lock();
                }
                try {
                  returnValue = this
                      .doWorkWhenStartedAndAsLeader(workDescription, workContextInfo, work);
                } finally {
                  if (needWriteLock) {
                    readWriteLockForBusiness.writeLock().unlock();
                  } else {
                    readWriteLockForBusiness.readLock().unlock();
                  }
                }
              } else {
                returnValue = this
                    .doWorkWhenStartedAndAsLeader(workDescription, workContextInfo, work);
              }
            } else if (this.isFollowerServer()) {
              if (null != readWriteLockForBusiness) {
                if (needWriteLock) {
                  readWriteLockForBusiness.writeLock().lock();
                } else {
                  readWriteLockForBusiness.readLock().lock();
                }
                try {
                  returnValue = this
                      .doWorkWhenStartedAndAsFollower(workDescription, workContextInfo, work);
                } finally {
                  if (needWriteLock) {
                    readWriteLockForBusiness.writeLock().unlock();
                  } else {
                    readWriteLockForBusiness.readLock().unlock();
                  }
                }
              } else {
                returnValue = this
                    .doWorkWhenStartedAndAsFollower(workDescription, workContextInfo, work);
              }
            }
          } else {
            String errorMessage =
                this.getClass().getSimpleName() + " is not leader or follower. so can not "
                    + workDescription + ". May have some bug. " + workContextInfo;
            logger.error(errorMessage);
            throw new KiraHandleException(errorMessage);
          }
        } finally {
          this.lockForKiraServerClusterRuntimeDataManagement.readLock().unlock();
        }
      } else {
        String errorMessage =
            this.getClass().getSimpleName() + " is not in started state. so do not "
                + workDescription + ". " + workContextInfo;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } finally {
      this.lockForComponentState.readLock().unlock();
    }

    return returnValue;
  }

  protected <T> T doWorkWhenStartedAndAsLeader(String workDescription, String workContextInfo,
      Callable<T> work) throws Exception {
    return work.call();
  }

  protected <T> T doWorkWhenStartedAndAsFollower(String workDescription, String workContextInfo,
      Callable<T> work) throws Exception {
    return work.call();
  }

  protected boolean isNormalOperationNeedToBeAborted(String operationSummary,
      boolean throwExceptionIfNeedAbort) throws Exception {
    boolean returnValue = false;

    if (this.requestShutdown || this.requestDestroy) {
      returnValue = true;
      String message =
          this.getClass().getSimpleName() + " may request shutdown or destroy. So need to abort "
              + operationSummary + ". and componentState=" + this.componentState
              + " and requestShutdown=" + this.requestShutdown + " and requestDestroy="
              + this.requestDestroy;
      logger.warn(message);
      if (throwExceptionIfNeedAbort) {
        throw new KiraWorkCanceledException(message);
      }
    }

    return returnValue;
  }

  protected boolean ifSomeHeavyWorkNeedToBeAborted(boolean throwExceptionIfNeedAbort)
      throws Exception {
    return false;
  }

  protected boolean isKiraCrossMultiZoneDataChangeEventOccur(boolean throwExceptionIfNeedAbort)
      throws Exception {
    return false;
  }

  protected boolean isHasPendingKiraServerChangedEvent() {
    boolean returnValue = false;
    try {
      Set<KiraServerEventType> kiraServerEventTypes = new LinkedHashSet<KiraServerEventType>();
      kiraServerEventTypes.add(KiraServerEventType.KIRA_SERVER_CHANGED);
      returnValue = this.isHasPendingEvent(kiraServerEventTypes);
    } catch (Throwable t) {
      if (null != logger) {
        logger.error("Error occurs when calling isHasPendingKiraServerChangedEvent.", t);
      }
    }
    return returnValue;
  }

  protected class KiraServerEventHandlerRejectedExecutionHandler implements
      RejectedExecutionHandler {

    private Logger logger = LoggerFactory
        .getLogger(KiraServerEventHandlerRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      logger.error(
          "KiraServerEventHandlerRejectedExecutionHandler is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
          runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
          executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
      throw new RejectedExecutionException();
    }
  }

  protected class KiraServerEventHandleTask implements Callable<Object> {

    private KiraServerEvent kiraServerEvent;

    public KiraServerEventHandleTask(KiraServerEvent kiraServerEvent) {
      super();
      this.kiraServerEvent = kiraServerEvent;
    }

    public KiraServerEvent getKiraServerEvent() {
      return kiraServerEvent;
    }

    @Override
    public Object call() throws Exception {
      KiraServerEventHandleComponent.this.handleKiraServerEvent(kiraServerEvent);
      return null;
    }
  }
}
