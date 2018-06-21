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
package com.yihaodian.architecture.kira.manager.health.internal;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.event.AsyncEventDispatcher;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class can not be restarted.
 */
public class KiraManagerHealthEventDispatcherWrapper extends ComponentAdaptor implements
    IKiraManagerHealthEventDispatcherWrapper {

  private static Logger logger = LoggerFactory
      .getLogger(KiraManagerHealthEventDispatcherWrapper.class);

  private static IKiraManagerHealthEventDispatcherWrapper kiraManagerHealthEventDispatcherWrapper;

  private AsyncEventDispatcher kiraManagerHealthEventDispatcher;

  private KiraManagerHealthEventDispatcherWrapper() throws Exception {
    this.init();
    this.start();
  }

  public static synchronized IKiraManagerHealthEventDispatcherWrapper getKiraManagerHealthEventDispatcherWrapper()
      throws Exception {
    if (null == KiraManagerHealthEventDispatcherWrapper.kiraManagerHealthEventDispatcherWrapper) {
      KiraManagerHealthEventDispatcherWrapper.kiraManagerHealthEventDispatcherWrapper = new KiraManagerHealthEventDispatcherWrapper();
    }
    return KiraManagerHealthEventDispatcherWrapper.kiraManagerHealthEventDispatcherWrapper;
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
      logger.info("Initializing KiraManagerHealthEventDispatcherWrapper...");

      this.prepareAsyncEventDispatcher();
      logger.info("Successfully initialize KiraManagerHealthEventDispatcherWrapper.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraManagerHealthEventDispatcherWrapper.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish initialize KiraManagerHealthEventDispatcherWrapper. And it takes " + costTime
              + " milliseconds.");
    }
  }

  private void prepareAsyncEventDispatcher() throws Exception {
    ThreadFactory threadFactory = new CustomizedThreadFactory(
        "KiraManagerHealthEventDispatcherWrapper-AsyncEventDispatcher-");
    RejectedExecutionHandler kiraCrossMultiZoneEventDispatcherRejectedExecutionHandler = new kiraManagerHealthEventDispatcherRejectedExecutionHandler();
    this.kiraManagerHealthEventDispatcher = new AsyncEventDispatcher(1, 1, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>(), threadFactory,
        kiraCrossMultiZoneEventDispatcherRejectedExecutionHandler);
    this.kiraManagerHealthEventDispatcher.init();
  }

  @Override
  public boolean dispatchKiraManagerHealthEvent(KiraManagerHealthEvent kiraManagerHealthEvent) {
    boolean returnValue = false;

    try {
      logger.info("Will dispatch KiraManagerHealthEvent.");

      if (null != this.kiraManagerHealthEventDispatcher) {
        this.kiraManagerHealthEventDispatcher.dispatch(kiraManagerHealthEvent);
        returnValue = true;
        logger.info("Successfully dispatch kiraManagerHealthEvent. kiraManagerHealthEvent={}",
            kiraManagerHealthEvent);
      } else {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          logger.error(
              "this.kiraManagerHealthEventDispatcher is null. So the event may be lost! componentState={} and kiraManagerHealthEvent={}",
              this.componentState.get(), kiraManagerHealthEvent);
        } else {
          //just return true when it is destroyed.
          returnValue = true;
          logger.warn(
              "KiraManagerHealthEvent will be discarded for KiraManagerHealthEventDispatcherWrapper is destroying or destroyed. kiraManagerHealthEvent={}",
              kiraManagerHealthEvent);
        }
      }
    } catch (Exception e) {
      logger.error(
          "Exception occurs when dispatching KiraManagerHealthEvent. componentState={} and kiraManagerHealthEvent={}",
          this.componentState.get(), kiraManagerHealthEvent);
    } finally {
      logger.info("Finish dispatch KiraManagerHealthEvent.");
    }

    return returnValue;
  }

  @Override
  public void registerKiraManagerHealthEventHandler(EventHandler eventHandler) throws Exception {
    if (null != eventHandler) {
      this.lockForComponentState.readLock().lock();
      try {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          if (null != this.kiraManagerHealthEventDispatcher) {
            this.kiraManagerHealthEventDispatcher
                .register(KiraManagerHealthEventType.class, eventHandler);
          } else {
            throw new KiraHandleException(
                "kiraManagerHealthEventDispatcher is null. It should not happen. May have some bugs. So can not registerKiraManagerHealthEventHandler.");
          }
        } else {
          throw new KiraHandleException(
              "The KiraManagerHealthEventDispatcherWrapper is destroying or already destroyed. So can not registerKiraManagerHealthEventHandler again.");
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      throw new KiraHandleException(
          "eventHandler is null. So can not registerKiraManagerHealthEventHandler.");
    }
  }

  @Override
  public void destroyKiraManagerHealthEventDispatcherWrapper() {
    this.destroy();
  }

  @Override
  protected void doDestroy() {
    super.doDestroy();

    this.destroyKiraManagerHealthEventDispatcher();
  }

  private void destroyKiraManagerHealthEventDispatcher() {
    if (null != this.kiraManagerHealthEventDispatcher) {
      this.kiraManagerHealthEventDispatcher.destroy();
      this.kiraManagerHealthEventDispatcher = null;
    }
  }

  static class kiraManagerHealthEventDispatcherRejectedExecutionHandler implements
      RejectedExecutionHandler {

    private Logger logger = LoggerFactory
        .getLogger(kiraManagerHealthEventDispatcherRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      logger.error(
          "kiraManagerHealthEventDispatcherRejectedExecutionHandler is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
          runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
          executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
      throw new RejectedExecutionException();
    }

  }

}
