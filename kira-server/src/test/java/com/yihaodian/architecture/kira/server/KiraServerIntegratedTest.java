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
package com.yihaodian.architecture.kira.server;

import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.server.event.KiraServerChangedEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerRuntimeDataCheckEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerShutdownEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraServerIntegratedTest {

  private static Logger logger = LoggerFactory.getLogger(KiraServerIntegratedTest.class);

  public KiraServerIntegratedTest() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    System.out.println("Test Start");
    int kiraServerNumber = 100;
    //int kiraServerNumber = 1;
    List<KiraServerSimulateTask> taskList = new ArrayList<KiraServerSimulateTask>();
    KiraServerSimulateTask kiraServerSimulateTask = null;
    KiraServerEventHandler kiraServerEventHandler = null;
    String host = null;
    Integer port = null;
    String accessUrlAsString = null;
    long lifeInMsAfterEveryTimeStarted;
    long sleepTimeInMsBeforeNextStart;
    int testTime;
    for (int i = 1; i <= kiraServerNumber; i++) {
      kiraServerEventHandler = new KiraServerEventHandler();
      host = "127.0.0." + i;
      port = Integer.valueOf(8080 + i);
      accessUrlAsString =
          "http://" + host + ":" + port.toString() + "/kira/remote/centralScheduleService";

      //lifeInMsAfterEveryTimeStarted = 120000L*i +  new Random().nextInt(10000);
      //lifeInMsAfterEveryTimeStarted = 120000L +  new Random().nextInt(10000);
      //lifeInMsAfterEveryTimeStarted = Long.MAX_VALUE;
      lifeInMsAfterEveryTimeStarted = 20000L + new Random().nextInt(10000);

      //sleepTimeInMsBeforeNextStart = 50000L;
      sleepTimeInMsBeforeNextStart = 5000L;
      testTime = Integer.MAX_VALUE;

      kiraServerSimulateTask = new KiraServerSimulateTask(host, port, accessUrlAsString,
          kiraServerEventHandler, lifeInMsAfterEveryTimeStarted, sleepTimeInMsBeforeNextStart,
          testTime);
      taskList.add(kiraServerSimulateTask);
    }

    CustomizedThreadFactory customizedThreadFactory = new CustomizedThreadFactory(
        "KiraServerIntegratedTest-KiraServerSimulateTask-");
    ExecutorService kiraServerSimulateTaskExecutorService = Executors
        .newFixedThreadPool(kiraServerNumber, customizedThreadFactory);
    for (KiraServerSimulateTask onekiraServerSimulateTask : taskList) {
      kiraServerSimulateTaskExecutorService.submit(onekiraServerSimulateTask);
    }
    Thread.sleep(Long.MAX_VALUE);
    //Thread.sleep(120000);
    for (KiraServerSimulateTask onekiraServerSimulateTask : taskList) {
      onekiraServerSimulateTask.destroy();
    }
    if (null != kiraServerSimulateTaskExecutorService) {
      kiraServerSimulateTaskExecutorService.shutdown();
    }
    System.out.println("Test done");
  }

  private static class KiraServerSimulateTask implements Callable<Object> {

    private static Logger logger = LoggerFactory.getLogger(KiraServerSimulateTask.class);
    private final CountDownLatch destroyDownLatch = new CountDownLatch(1);
    private volatile long lifeInMsAfterEveryTimeStarted;
    private volatile long sleepTimeInMsBeforeNextStart;
    private volatile int testTime;
    private KiraServerEventHandler kiraServerEventHandler;
    private KiraServer kiraServer;

    public KiraServerSimulateTask(String host, Integer port, String accessUrlAsString,
        KiraServerEventHandler kiraServerEventHandler, long lifeInMsAfterEveryTimeStarted,
        long sleepTimeInMsBeforeNextStart, int testTime) throws Exception {
      super();
      this.lifeInMsAfterEveryTimeStarted = lifeInMsAfterEveryTimeStarted;
      this.sleepTimeInMsBeforeNextStart = sleepTimeInMsBeforeNextStart;
      this.testTime = testTime;
      this.kiraServerEventHandler = kiraServerEventHandler;

      kiraServer = new KiraServer(host, port, accessUrlAsString);
      kiraServer.registerKiraServerEventHandler(this.kiraServerEventHandler);
    }

    @Override
    public Object call() throws Exception {
      this.test();
      return null;
    }

    public void test() {
      for (int i = 0; i < this.testTime; i++) {
        if (null == kiraServer) {
          return;
        }
        oneTimeTest();
      }
    }

    private void oneTimeTest() {
      try {
        if (null != kiraServer) {
          kiraServer.start();
          try {
            if (destroyDownLatch.await(lifeInMsAfterEveryTimeStarted, TimeUnit.MILLISECONDS)) {
              return;
            }
          } catch (InterruptedException interruptedException) {
            if (null == kiraServer) {
              return;
            }
          }

          if (null != kiraServer) {
            kiraServer.shutdown();
            try {
              if (destroyDownLatch.await(sleepTimeInMsBeforeNextStart, TimeUnit.MILLISECONDS)) {
                return;
              }
            } catch (InterruptedException interruptedException) {
              if (null == kiraServer) {
                return;
              }
            }
          }
        }
      } catch (Exception e) {
        logger.error("Error occurs when oneTimeTest.", e);
      }
    }

    private void destroy() {
      if (null != kiraServer) {
        kiraServer.destroy();
        kiraServer = null;
        destroyDownLatch.countDown();
        kiraServerEventHandler.destroy();
      }
    }

  }

  private static class KiraServerEventHandler implements EventHandler<KiraServerEvent> {

    private static Logger logger = LoggerFactory.getLogger(KiraServerEventHandler.class);

    private ExecutorService executor;

    public KiraServerEventHandler() throws Exception {
      init();
    }

    private void init() throws Exception {
      ThreadFactory threadFactory = new CustomizedThreadFactory("KiraServerEventHandler-");
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(300);
      executor = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, queue, threadFactory);
      logger.info("Successfully init KiraServerEventHandler.");
    }

    public void destroy() {
      try {
        if (null != executor) {
          executor.shutdown();
        }
        logger.info("Successfully destroy KiraServerEventHandler.");
      } catch (Exception e) {
        logger.error("Error occurs when destroy KiraServerEventHandler.", e);
      }
    }

    @Override
    public void handle(KiraServerEvent kiraServerEvent) {
      KiraServerEventHandleTask kiraServerEventHandleTask = new KiraServerEventHandleTask(
          kiraServerEvent);
      if (null != executor) {
        executor.submit(kiraServerEventHandleTask);
      } else {
        logger.error("executor is null when try to handle kiraServerEvent={}", kiraServerEvent);
      }
    }

    private static class KiraServerEventHandleTask implements Callable<Object> {

      private static Logger logger = LoggerFactory.getLogger(KiraServerEventHandleTask.class);

      private KiraServerEvent kiraServerEvent;

      KiraServerEventHandleTask(KiraServerEvent kiraServerEvent) {
        this.kiraServerEvent = kiraServerEvent;
      }

      @Override
      public Object call() throws Exception {
        switch (kiraServerEvent.getEventType()) {
          case KIRA_SERVER_DETAIL_DATA_CHECK:
            KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent = (KiraServerRuntimeDataCheckEvent) kiraServerEvent;
            //System.out.println("Will handle kiraServerRuntimeDataCheckEvent="+kiraServerRuntimeDataCheckEvent);
            logger.info("Will handle kiraServerRuntimeDataCheckEvent={}",
                kiraServerRuntimeDataCheckEvent);
            break;
          case KIRA_SERVER_CHANGED:
            KiraServerChangedEvent kiraServerChangedEvent = (KiraServerChangedEvent) kiraServerEvent;
            //System.out.println("Will handle kiraServerChangedEvent="+kiraServerChangedEvent);
            logger.info("Will handle kiraServerChangedEvent={}", kiraServerChangedEvent);
            break;
          case KIRA_SERVER_STARTED:
            KiraServerStartedEvent kiraServerStartedEvent = (KiraServerStartedEvent) kiraServerEvent;
            //System.out.println("Will handle kiraServerStartedEvent="+kiraServerStartedEvent);
            logger.info("Will handle kiraServerStartedEvent={}", kiraServerStartedEvent);
            break;
          case KIRA_SERVER_SHUTDOWN:
            KiraServerShutdownEvent kiraServerShutdownEvent = (KiraServerShutdownEvent) kiraServerEvent;
            //System.out.println("Will handle kiraServerShutdownEvent="+kiraServerShutdownEvent);
            logger.info("Will handle kiraServerShutdownEvent={}", kiraServerShutdownEvent);
            break;
          default:
            logger.error("Unknown eventType for kiraServerEvent=" + kiraServerEvent);
        }

        return null;
      }
    }
  }

}
