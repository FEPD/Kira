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
package com.yihaodian.architecture.kira.common.event;

import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncEventDispatcher implements EventDispatcher {

  public static final int DEFAULT_COREPOOLSIZE = 2;
  public static final int DEFAULT_MAXIMUMPOOLSIZE = 2;
  public static final long DEFAULT_KEEPALIVETIME = 60L;
  public static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;
  public static final BlockingQueue<Runnable> DEFAULT_WORKQueue = new LinkedBlockingQueue<Runnable>();
  public static final ThreadFactory DEFAULT_THREADFACTORY = new CustomizedThreadFactory(
      "AsyncEventDispatcher-");
  public static final RejectedExecutionHandler DEFAULT_REJECTEDEXECUTIONHANDLER = new AbortPolicy();
  private static final int EVENTDISPATCHER_STATE_INSTANTIATED = 0;
  private static final int EVENTDISPATCHER_STATE_INIT = 1;
  private static final int EVENTDISPATCHER_STATE_DESTROY = 2;
  private static Logger logger = LoggerFactory.getLogger(AsyncEventDispatcher.class);
  private final Map<Class<? extends Enum>, EventHandler> eventTypeEventHandlerMap = new LinkedHashMap<Class<? extends Enum>, EventHandler>();
  private final AtomicInteger eventDispatcherState = new AtomicInteger(
      AsyncEventDispatcher.EVENTDISPATCHER_STATE_INSTANTIATED);
  private final ReadWriteLock lockForEventDispatcherState = new ReentrantReadWriteLock();
  private int corePoolSize = DEFAULT_COREPOOLSIZE;
  private int maximumPoolSize = DEFAULT_MAXIMUMPOOLSIZE;
  private long keepAliveTime = DEFAULT_KEEPALIVETIME;
  ;
  private TimeUnit timeUnit = DEFAULT_UNIT;
  private BlockingQueue<Runnable> workQueue = DEFAULT_WORKQueue;
  private ThreadFactory threadFactory = DEFAULT_THREADFACTORY;
  private RejectedExecutionHandler rejectedExecutionHandler = DEFAULT_REJECTEDEXECUTIONHANDLER;
  private ExecutorService executor;

  public AsyncEventDispatcher() {
  }

  public AsyncEventDispatcher(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit timeUnit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler rejectedExecutionHandler) {
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.keepAliveTime = keepAliveTime;
    this.timeUnit = timeUnit;
    this.workQueue = workQueue;
    this.threadFactory = threadFactory;
    this.rejectedExecutionHandler = rejectedExecutionHandler;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  private boolean isInInitState() {
    boolean returnValue = false;

    this.lockForEventDispatcherState.readLock().lock();
    try {
      if (AsyncEventDispatcher.EVENTDISPATCHER_STATE_INIT == this.eventDispatcherState.get()) {
        returnValue = true;
      }
    } finally {
      this.lockForEventDispatcherState.readLock().unlock();
    }

    return returnValue;
  }

  public void init() throws Exception {
    long startTime = System.currentTimeMillis();
    this.lockForEventDispatcherState.writeLock().lock();
    try {
      try {
        logger.info("Initializing for AsyncEventDispatcher...");

        //CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("kira-manager-AsyncDispatcher-");
        //threadFactory.setDaemon(false);
        //BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize,
            this.keepAliveTime, this.timeUnit, this.workQueue, this.threadFactory,
            this.rejectedExecutionHandler);
        eventDispatcherState.set(AsyncEventDispatcher.EVENTDISPATCHER_STATE_INIT);
        logger.info("Successfully initialize AsyncEventDispatcher.");
      } catch (Exception e) {
        logger.error("Error occurs when initialize AsyncEventDispatcher.", e);
        throw e;
      } finally {
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish initialize AsyncEventDispatcher. And it takes " + costTime
            + " milliseconds. this=" + this.toDetailString());
      }
    } finally {
      this.lockForEventDispatcherState.writeLock().unlock();
    }
  }

  public void destroy() {
    long startTime = System.currentTimeMillis();
    this.lockForEventDispatcherState.writeLock().lock();
    try {
      try {
        logger.info("Destroying AsyncEventDispatcher...");
        if (null != executor) {
          executor.shutdown();
        }
        this.eventDispatcherState.set(AsyncEventDispatcher.EVENTDISPATCHER_STATE_DESTROY);
        logger.info("Sucessfully destroy AsyncEventDispatcher.");
      } catch (Exception e) {
        logger.error("Error occurs when destroy AsyncEventDispatcher.", e);
      } finally {
        eventTypeEventHandlerMap.clear();
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(
            "Finish destroy AsyncEventDispatcher. And it takes " + costTime + " milliseconds.");
      }
    } finally {
      this.lockForEventDispatcherState.writeLock().unlock();
    }
  }

  @Override
  public void register(Class<? extends Enum> eventType,
      EventHandler eventHandler) throws Exception {
    this.lockForEventDispatcherState.readLock().lock();
    try {
      if (isInInitState()) {
        try {
          logger.info("Registering eventType with eventHandler...");

          EventHandler registeredEventHandler = eventTypeEventHandlerMap.get(eventType);
          if (null == registeredEventHandler) {
            eventTypeEventHandlerMap.put(eventType, eventHandler);
          } else if (!(registeredEventHandler instanceof EventMultiHandler)) {
            EventMultiHandler eventMultiHandler = new EventMultiHandler();
            eventMultiHandler.addEventHandler(registeredEventHandler);
            eventMultiHandler.addEventHandler(eventHandler);
            eventTypeEventHandlerMap.put(eventType, eventMultiHandler);
          } else {
            EventMultiHandler eventMultiHandler = (EventMultiHandler) registeredEventHandler;
            eventMultiHandler.addEventHandler(eventHandler);
          }

          logger.info("Sucessfully register eventType with eventHandler.");
        } catch (Exception e) {
          logger.error(
              "Error occurs when register eventType with eventHandler. eventType=" + eventType
                  + " and eventHandler=" + eventHandler, e);
          throw e;
        } finally {
          logger.info(
              "Finish register eventType with eventHandler. eventType={} and eventHandler={} and this={}",
              eventType, eventHandler, this.toDetailString());
        }
      } else {
        String reason = "AsyncEventDispatcher is not in init state. so can not register it now. ";
        logger.warn(reason + " eventDispatcherState={} and eventType={} and eventHandler={}",
            this.eventDispatcherState.get(), eventType, eventHandler);
        throw new RuntimeException(reason);
      }
    } finally {
      this.lockForEventDispatcherState.readLock().unlock();
    }
  }

  @Override
  public void dispatch(Event event) {
    this.lockForEventDispatcherState.readLock().lock();
    try {
      if (isInInitState()) {
        GenericEventHandleTask genericEventHandleTask = new GenericEventHandleTask(event);
        executor.submit(genericEventHandleTask);
      } else {
        String reason = "AsyncEventDispatcher is not in init state. so can not dispatch event now. ";
        logger
            .warn(reason + " eventDispatcherState={} and event={}", this.eventDispatcherState.get(),
                event);
      }
    } finally {
      this.lockForEventDispatcherState.readLock().unlock();
    }
  }

  public String toDetailString() {
    return this.toString();
  }

  @Override
  public String toString() {
    return "AsyncEventDispatcher [corePoolSize=" + corePoolSize
        + ", maximumPoolSize=" + maximumPoolSize + ", keepAliveTime="
        + keepAliveTime + ", timeUnit=" + timeUnit + ", workQueue="
        + workQueue + ", threadFactory=" + threadFactory
        + ", rejectedExecutionHandler=" + rejectedExecutionHandler
        + ", executor=" + executor + ", eventTypeEventHandlerMap="
        + eventTypeEventHandlerMap + ", eventDispatcherState="
        + eventDispatcherState + ", lockForEventDispatcherState="
        + lockForEventDispatcherState + "]";
  }

  class GenericEventHandleTask implements Callable<Object> {

    private Event event;

    GenericEventHandleTask(Event event) {
      super();
      this.event = event;
    }

    @Override
    public Object call() throws Exception {
      EventHandler eventHandler = eventTypeEventHandlerMap
          .get(event.getEventType().getDeclaringClass());
      if (null != eventHandler) {
        eventHandler.handle(event);
      } else {
        AsyncEventDispatcher.this.lockForEventDispatcherState.readLock().lock();
        try {
          if (isInInitState()) {
            logger.error("can not get eventHandler for event=" + event);
          } else {
            String reason = "AsyncEventDispatcher is not in init state. so can not find eventHandler to handle event now. ";
            logger.warn(reason + " eventDispatcherState={} and event={}",
                AsyncEventDispatcher.this.eventDispatcherState.get(), event);
          }
        } finally {
          AsyncEventDispatcher.this.lockForEventDispatcherState.readLock().unlock();
        }
      }
      return null;
    }

  }


}
