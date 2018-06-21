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
package com.yihaodian.architecture.kira.manager.event;

import com.yihaodian.architecture.kira.common.event.Event;
import com.yihaodian.architecture.kira.common.event.EventDispatcher;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class AsyncDispatcher implements EventDispatcher, ILifecycle {

  private static final int DEFAULT_COREPOOLSIZE = 2;
  private static final int DEFAULT_MAXIMUMPOOLSIZE = 2;
  private static final long DEFAULT_KEEPALIVETIME = 60L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  private static Logger logger = LoggerFactory.getLogger(AsyncDispatcher.class);
  private final Map<Class<? extends Enum>, EventHandler> eventDispatchers = new LinkedHashMap<Class<? extends Enum>, EventHandler>();
  private ExecutorService executor;
  ;

  public AsyncDispatcher() {
    // TODO Auto-generated constructor stub
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
      logger.info("init for AsyncDispatcher");
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          "kira-manager-AsyncDispatcher-");
      threadFactory.setDaemon(false);
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
      executor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE, DEFAULT_MAXIMUMPOOLSIZE,
          DEFAULT_KEEPALIVETIME, DEFAULT_UNIT, queue, threadFactory);
    } catch (Exception e) {
      logger.error("Error occurs when initialize AsyncDispatcher.", e);
    }
  }

  @Override
  public void destroy() {
    try {
      if (null != executor) {
        executor.shutdown();
      }
      logger.info("AsyncDispatcher destroyed.");
    } catch (Exception e) {
      logger.error("Error occurs when destroy AsyncDispatcher.", e);
    }
  }

  @Override
  public void register(Class<? extends Enum> eventType,
      EventHandler eventHandler) {
    eventDispatchers.put(eventType, eventHandler);
  }

  @Override
  public void dispatch(Event event) {
    GenericEventHandleTask genericEventHandleTask = new GenericEventHandleTask(event);
    executor.submit(genericEventHandleTask);
  }

  class GenericEventHandleTask implements Callable<Object> {

    private Event event;

    GenericEventHandleTask(Event event) {
      super();
      this.event = event;
    }

    @Override
    public Object call() throws Exception {
      EventHandler eventHandler = eventDispatchers.get(event.getEventType().getDeclaringClass());
      if (null != eventHandler) {
        eventHandler.handle(event);
      } else {
        logger.error("can not get eventHandler for event=" + event);
      }
      return null;
    }

  }


}
