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
package com.yihaodian.architecture.kira.common.crossmultizone.util;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.crossmultizone.dto.KiraCrossMultiZoneData;
import com.yihaodian.architecture.kira.common.crossmultizone.dto.KiraCrossMultiZoneDataTrackContext;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneDataChangeEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneDataCheckEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneEvent;
import com.yihaodian.architecture.kira.common.crossmultizone.event.KiraCrossMultiZoneEventType;
import java.util.Date;
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

public abstract class KiraCrossMultiZoneEventHandleComponent extends ComponentAdaptor implements
    IKiraCrossMultiZoneEventHandleComponent {

  private final ReadWriteLock lockForKiraCrossMultiZoneRuntimeDataManagement = new ReentrantReadWriteLock();
  protected ThreadPoolExecutor kiraCrossMultiZoneEventExecutorService;
  //runtime data
  protected KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole;
  protected Date lastSetKiraCrossMultiZoneRoleTime;

  public KiraCrossMultiZoneEventHandleComponent() throws Exception {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  protected boolean isMasterZone() {
    return KiraCrossMultiZoneUtils.isMasterZone(this.kiraCrossMultiZoneRole);
  }

  @Override
  public boolean isMasterZone(boolean accurate) {
    boolean returnValue = false;

    if (accurate) {
      this.lockForComponentState.readLock().lock();
      try {
        this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().lock();
        try {
          returnValue = this.isMasterZone();
        } finally {
          this.lockForKiraCrossMultiZoneRuntimeDataManagement.readLock().unlock();
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      returnValue = this.isMasterZone();
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

  public Date getLastSetKiraCrossMultiZoneRoleTime() {
    return this.lastSetKiraCrossMultiZoneRoleTime;
  }

  protected void init() throws Exception {
    this.kiraCrossMultiZoneRole = KiraCrossMultiZoneUtils.getKiraCrossMultiZoneRole(true);
    this.lastSetKiraCrossMultiZoneRoleTime = new Date();

    this.prepareKiraCrossMultiZoneEventExecutorService();
    KiraCrossMultiZoneUtils.registerKiraCrossMultiZoneEventHandler(this);
  }

  protected void prepareKiraCrossMultiZoneEventExecutorService() throws Exception {
    ThreadFactory threadFactory = this.getThreadFactoryForKiraCrossMultiZoneEventExecutorService();
    RejectedExecutionHandler kiraCrossMultiZoneEventHandlerRejectedExecutionHandler = this
        .getRejectedExecutionHandlerForKiraCrossMultiZoneEventExecutorService();
    int corePoolSize = this.getCorePoolSizeForKiraCrossMultiZoneEventExecutorService();
    int maximumPoolSize = this.getMaximumPoolSizeForKiraCrossMultiZoneEventExecutorService();
    int capacity = this.getCapacityOfQueueForKiraCrossMultiZoneEventExecutorService();
    this.kiraCrossMultiZoneEventExecutorService = new ThreadPoolExecutor(corePoolSize,
        maximumPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(capacity),
        threadFactory, kiraCrossMultiZoneEventHandlerRejectedExecutionHandler);
  }

  protected ThreadFactory getThreadFactoryForKiraCrossMultiZoneEventExecutorService() {
    String classSimpleName = this.getClass().getSimpleName();
    return new CustomizedThreadFactory(classSimpleName + "-KiraCrossMultiZoneEventHandler-");
  }

  protected RejectedExecutionHandler getRejectedExecutionHandlerForKiraCrossMultiZoneEventExecutorService() {
    return new KiraCrossMultiZoneEventHandlerRejectedExecutionHandler();
  }

  protected int getCorePoolSizeForKiraCrossMultiZoneEventExecutorService() {
    return 1;
  }

  protected int getMaximumPoolSizeForKiraCrossMultiZoneEventExecutorService() {
    return 1;
  }

  protected int getCapacityOfQueueForKiraCrossMultiZoneEventExecutorService() {
    return 1000;
  }

  @Override
  public void handle(KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent) {
    try {
      if (!this.kiraCrossMultiZoneEventExecutorService.isShutdown()) {
        KiraCrossMultiZoneEventHandleTask kiraCrossMultiZoneEventHandleTask = new KiraCrossMultiZoneEventHandleTask(
            kiraCrossMultiZoneEvent);
        this.kiraCrossMultiZoneEventExecutorService.submit(kiraCrossMultiZoneEventHandleTask);
      } else {
        logger.warn(
            "Can not handle KiraCrossMultiZoneEvent for kiraCrossMultiZoneEventExecutorService is shutdown in "
                + this.getClass().getSimpleName() + ". kiraCrossMultiZoneEvent={}",
            kiraCrossMultiZoneEvent);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for handle kiraCrossMultiZoneEvent in " + this.getClass().getSimpleName()
              + ". kiraCrossMultiZoneEvent={}", kiraCrossMultiZoneEvent);
    }
  }

  protected void handleKiraCrossMultiZoneEvent(KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent) {
    switch (kiraCrossMultiZoneEvent.getEventType()) {
      case KIRA_CROSS_MULTI_ZONE_DATA_CHECK:
        KiraCrossMultiZoneDataCheckEvent kiraCrossMultiZoneDataCheckEvent = (KiraCrossMultiZoneDataCheckEvent) kiraCrossMultiZoneEvent;
        if (logger.isDebugEnabled()) {
          logger.debug(
              this.getClass().getSimpleName() + " caught kiraCrossMultiZoneDataCheckEvent={}",
              kiraCrossMultiZoneDataCheckEvent);
        }
        this.handleKiraCrossMultiZoneDataCheckEvent(kiraCrossMultiZoneDataCheckEvent);
        break;
      case KIRA_CROSS_MULTI_ZONE_DATA_CHANGED:
        KiraCrossMultiZoneDataChangeEvent KiraCrossMultiZoneDataChangeEvent = (KiraCrossMultiZoneDataChangeEvent) kiraCrossMultiZoneEvent;
        logger
            .warn(this.getClass().getSimpleName() + " caught KiraCrossMultiZoneDataChangeEvent={}",
                KiraCrossMultiZoneDataChangeEvent);
        this.handleKiraCrossMultiZoneDataChangeEvent(KiraCrossMultiZoneDataChangeEvent);
        break;
      default:
        logger.error(this.getClass().getSimpleName()
            + " caught unknown eventType for kiraCrossMultiZoneEvent=" + kiraCrossMultiZoneEvent);
    }
  }

  private void handleKiraCrossMultiZoneDataChangeEvent(
      KiraCrossMultiZoneDataChangeEvent kiraCrossMultiZoneDataChangeEvent) {
    try {
      if (!this.requestShutdown && !this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.isStarted()) {
              this.doHandleKiraCrossMultiZoneDataChangeEvent(kiraCrossMultiZoneDataChangeEvent);
            } else {
              logger.warn(this.getClass().getSimpleName()
                      + " is not in the started state. So do not handleKiraCrossMultiZoneDataChangeEvent. componentState={} and kiraCrossMultiZoneDataChangeEvent={}",
                  this.componentState, kiraCrossMultiZoneDataChangeEvent);
            }
          } else {
            logger.warn(this.getClass().getSimpleName()
                    + " may request shutdown or destroy. So do not handleKiraCrossMultiZoneDataChangeEvent. kiraCrossMultiZoneDataChangeEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                kiraCrossMultiZoneDataChangeEvent, this.componentState, this.requestShutdown,
                this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.warn(this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not handleKiraCrossMultiZoneDataChangeEvent for "
                + this.getClass().getSimpleName()
                + ". kiraCrossMultiZoneDataChangeEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
            kiraCrossMultiZoneDataChangeEvent, this.componentState, this.requestShutdown,
            this.requestDestroy);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs during call handleKiraCrossMultiZoneDataChangeEvent for " + this.getClass()
              .getSimpleName() + ". kiraCrossMultiZoneDataChangeEvent="
              + kiraCrossMultiZoneDataChangeEvent, e);
    }
  }

  private void doHandleKiraCrossMultiZoneDataChangeEvent(
      KiraCrossMultiZoneDataChangeEvent kiraCrossMultiZoneDataChangeEvent) throws Exception {
    logger.info(
        "Starting doHandleKiraCrossMultiZoneDataChangeEvent for " + this.getClass().getSimpleName()
            + "...");
    this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        KiraCrossMultiZoneDataTrackContext kiraCrossMultiZoneDataTrackContext = kiraCrossMultiZoneDataChangeEvent
            .getKiraCrossMultiZoneDataTrackContext();
        if (null != kiraCrossMultiZoneDataTrackContext) {
          KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRoleEnum = kiraCrossMultiZoneDataTrackContext
              .getNewKiraCrossMultiZoneRole();
          if (newKiraCrossMultiZoneRoleEnum == this.kiraCrossMultiZoneRole) {
            logger.warn(
                "Although kiraCrossMultiZoneDataChangeEvent caught but zoneRole do not change. So do nothing. kiraCrossMultiZoneDataChangeEvent={} and this.kiraCrossMultiZoneRole={}",
                kiraCrossMultiZoneDataChangeEvent, this.kiraCrossMultiZoneRole);
          } else {
            logger.warn(
                "Found zoneRole change in doHandleKiraCrossMultiZoneDataChangeEvent. kiraCrossMultiZoneDataChangeEvent={} and this.kiraCrossMultiZoneRole={}",
                kiraCrossMultiZoneDataChangeEvent, this.kiraCrossMultiZoneRole);
            this.handleZoneRoleChanged(newKiraCrossMultiZoneRoleEnum);
          }
        }
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.warn("Finish doHandleKiraCrossMultiZoneDataChangeEvent for " + this.getClass()
            .getSimpleName() + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().unlock();
    }
  }

  private void handleZoneRoleChanged(KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception {
    KiraCrossMultiZoneRoleEnum oldKiraCrossMultiZoneRoleEnum = this.kiraCrossMultiZoneRole;
    Date oldLastSetKiraCrossMultiZoneRoleTime = this.lastSetKiraCrossMultiZoneRoleTime;
    try {
      switch (newKiraCrossMultiZoneRole) {
        case MASTER:
          this.changeToWorkAsMasterZone(newKiraCrossMultiZoneRole);
          break;
        case SLAVE:
          this.changeToWorkAsSlaveZone(newKiraCrossMultiZoneRole);
          break;
        default:
          logger.error(
              "unKnown zoneRole detected when handleZoneRoleChanged. May have some bugs. newKiraCrossMultiZoneRoleEnum={}",
              newKiraCrossMultiZoneRole);
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when handleZoneRoleChanged. So will roll back to old role to let it redo it next time. oldKiraCrossMultiZoneRoleEnum="
              + oldKiraCrossMultiZoneRoleEnum + " and newKiraCrossMultiZoneRoleEnum="
              + newKiraCrossMultiZoneRole, t);
      this.kiraCrossMultiZoneRole = oldKiraCrossMultiZoneRoleEnum;
      this.lastSetKiraCrossMultiZoneRoleTime = oldLastSetKiraCrossMultiZoneRoleTime;
    }
  }

  private void changeToWorkAsMasterZone(KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start changeToWorkAsMasterZone....");
      this.kiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;
      this.lastSetKiraCrossMultiZoneRoleTime = new Date();
      this.afterChangeToWorkAsMasterZone();
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn("Finish changeToWorkAsMasterZone for " + this.getClass().getSimpleName()
          + ". It takes {} milliseconds.", costTime);
    }
  }

  protected void afterChangeToWorkAsMasterZone() throws Exception {
  }

  private void changeToWorkAsSlaveZone(KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole)
      throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start changeToWorkAsSlaveZone....");
      this.kiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;
      this.lastSetKiraCrossMultiZoneRoleTime = new Date();
      this.afterChangeToWorkAsSlaveZone();
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn("Finish changeToWorkAsSlaveZone for " + this.getClass().getSimpleName()
          + ". It takes {} milliseconds.", costTime);
    }
  }

  protected void afterChangeToWorkAsSlaveZone() throws Exception {
  }

  private void handleKiraCrossMultiZoneDataCheckEvent(
      KiraCrossMultiZoneDataCheckEvent kiraCrossMultiZoneDataCheckEvent) {
    try {
      if (!this.requestShutdown && !this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.isStarted()) {
              this.doHandleKiraCrossMultiZoneDataCheckEvent(kiraCrossMultiZoneDataCheckEvent);
            } else {
              logger.warn(this.getClass().getSimpleName()
                      + " is not in the started state. So do not handleKiraCrossMultiZoneDataCheckEvent. componentState={} and kiraCrossMultiZoneDataCheckEvent={}",
                  this.componentState, kiraCrossMultiZoneDataCheckEvent);
            }
          } else {
            logger.warn(this.getClass().getSimpleName()
                    + " may request shutdown or destroy. So do not handleKiraCrossMultiZoneDataCheckEvent. kiraCrossMultiZoneDataCheckEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
                kiraCrossMultiZoneDataCheckEvent, this.componentState, this.requestShutdown,
                this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.warn(this.getClass().getSimpleName()
                + " may request shutdown or destroy. So do not handleKiraCrossMultiZoneDataCheckEvent for "
                + this.getClass().getSimpleName()
                + ". kiraCrossMultiZoneDataCheckEvent={} and componentState={} and requestShutdown={} and requestDestroy={}",
            kiraCrossMultiZoneDataCheckEvent, this.componentState, this.requestShutdown,
            this.requestDestroy);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs during call handleKiraCrossMultiZoneDataCheckEvent in" + this.getClass()
              .getSimpleName() + ". kiraCrossMultiZoneDataCheckEvent="
              + kiraCrossMultiZoneDataCheckEvent, e);
    }
  }

  private void doHandleKiraCrossMultiZoneDataCheckEvent(
      KiraCrossMultiZoneDataCheckEvent kiraCrossMultiZoneDataCheckEvent) throws Exception {
    logger.info(
        "Starting doHandleKiraCrossMultiZoneDataCheckEvent for " + this.getClass().getSimpleName()
            + "...");
    this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().lock();
    try {
      long startTime = System.currentTimeMillis();
      try {
        KiraCrossMultiZoneData kiraCrossMultiZoneData = kiraCrossMultiZoneDataCheckEvent
            .getKiraCrossMultiZoneData();
        if (null != kiraCrossMultiZoneData) {
          KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRoleEnum = kiraCrossMultiZoneData
              .getKiraCrossMultiZoneRole();
          if (newKiraCrossMultiZoneRoleEnum != this.kiraCrossMultiZoneRole) {
            logger.warn(
                "Found zoneRole change in doHandleKiraCrossMultiZoneDataCheckEvent. kiraCrossMultiZoneDataCheckEvent={} and this.kiraCrossMultiZoneRole={}",
                kiraCrossMultiZoneDataCheckEvent, this.kiraCrossMultiZoneRole);
            this.handleZoneRoleChanged(newKiraCrossMultiZoneRoleEnum);
          }
        }
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish doHandleKiraCrossMultiZoneDataCheckEvent for " + this.getClass().getSimpleName()
                + ". And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForKiraCrossMultiZoneRuntimeDataManagement.writeLock().unlock();
    }
  }

  @Override
  public boolean isHasPendingEvent(Set<KiraCrossMultiZoneEventType> kiraCrossMultiZoneEventTypes) {
    boolean returnValue = false;

    try {
      if (CollectionUtils.isNotEmpty(kiraCrossMultiZoneEventTypes)) {
        ThreadPoolExecutor kiraCrossMultiZoneEventExecutorService = this.kiraCrossMultiZoneEventExecutorService;
        if (null != kiraCrossMultiZoneEventExecutorService) {
          if (!kiraCrossMultiZoneEventExecutorService.isShutdown()) {
            BlockingQueue<Runnable> kiraCrossMultiZoneEventHandleTaskQueue = kiraCrossMultiZoneEventExecutorService
                .getQueue();
            if (null != kiraCrossMultiZoneEventHandleTaskQueue) {
              int pendingCount = kiraCrossMultiZoneEventHandleTaskQueue.size();
              if (pendingCount > 0) {
                if (null != logger && logger.isInfoEnabled()) {
                  logger.info(
                      "Found total pendingCount={} in isHasPendingEvent() kiraCrossMultiZoneEventTypes={}",
                      pendingCount, kiraCrossMultiZoneEventTypes);
                }

                for (Runnable kiraCrossMultiZoneEventHandleTaskRunnable : kiraCrossMultiZoneEventHandleTaskQueue) {
                  if (kiraCrossMultiZoneEventHandleTaskRunnable instanceof FutureTask) {
                    FutureTask kiraCrossMultiZoneEventHandleTaskFutureTask = (FutureTask) kiraCrossMultiZoneEventHandleTaskRunnable;
                    Object callableObject = KiraCommonUtils.getCallableTaskObjectFromFutureTask(
                        kiraCrossMultiZoneEventHandleTaskFutureTask);
                    if (callableObject instanceof KiraCrossMultiZoneEventHandleTask) {
                      KiraCrossMultiZoneEventHandleTask kiraCrossMultiZoneEventHandleTask = (KiraCrossMultiZoneEventHandleTask) callableObject;
                      KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent = kiraCrossMultiZoneEventHandleTask
                          .getKiraCrossMultiZoneEvent();
                      if (null != kiraCrossMultiZoneEvent) {
                        KiraCrossMultiZoneEventType eventType = kiraCrossMultiZoneEvent
                            .getEventType();
                        if (null != eventType) {
                          if (kiraCrossMultiZoneEventTypes.contains(eventType)) {
                            returnValue = true;
                            if (null != logger) {
                              logger.info(
                                  "Found KiraCrossMultiZoneEventType={} in isHasPendingEvent() kiraCrossMultiZoneEventTypes={} and pendingCount={}",
                                  eventType, kiraCrossMultiZoneEventTypes, pendingCount);
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
        logger.error("Error occurs when call isHasPendingEvent(). kiraCrossMultiZoneEventTypes="
            + kiraCrossMultiZoneEventTypes, t);
      }
    }

    return returnValue;
  }

  protected void destroyKiraCrossMultiZoneEventExecutorService() {
    try {
      if (null != this.kiraCrossMultiZoneEventExecutorService) {
        this.kiraCrossMultiZoneEventExecutorService.shutdown();
      }
    } catch (Throwable t) {
      if (null != logger) {
        logger.error("Error occurs when destroyKiraCrossMultiZoneEventExecutorService.", t);
      }
    }
  }

  protected void destroyKiraCrossMultiZoneUtils() {
    KiraCrossMultiZoneUtils.destroyKiraCrossMultiZoneUtils();
  }

  @Override
  protected void doDestroy() {
    this.destroyKiraCrossMultiZoneEventExecutorService();
    this.destroyKiraCrossMultiZoneUtils();
  }

  protected class KiraCrossMultiZoneEventHandlerRejectedExecutionHandler implements
      RejectedExecutionHandler {

    private Logger logger = LoggerFactory
        .getLogger(KiraCrossMultiZoneEventHandlerRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      logger.error(
          "KiraCrossMultiZoneEventHandlerRejectedExecutionHandler is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
          runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
          executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
      throw new RejectedExecutionException();
    }
  }

  protected class KiraCrossMultiZoneEventHandleTask implements Callable<Object> {

    private KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent;

    public KiraCrossMultiZoneEventHandleTask(KiraCrossMultiZoneEvent kiraCrossMultiZoneEvent) {
      super();
      this.kiraCrossMultiZoneEvent = kiraCrossMultiZoneEvent;
    }

    public KiraCrossMultiZoneEvent getKiraCrossMultiZoneEvent() {
      return kiraCrossMultiZoneEvent;
    }

    @Override
    public Object call() throws Exception {
      KiraCrossMultiZoneEventHandleComponent.this
          .handleKiraCrossMultiZoneEvent(kiraCrossMultiZoneEvent);
      return null;
    }
  }

}
