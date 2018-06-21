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
package com.yihaodian.architecture.kira.schedule.time;

import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import com.yihaodian.architecture.kira.schedule.time.callback.KiraTimeScheduleCallbackAdaptor;
import com.yihaodian.architecture.kira.schedule.time.scheduler.ITimerTriggerScheduler;
import com.yihaodian.architecture.kira.schedule.time.scheduler.KiraTimerTriggerScheduler;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraCronTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraSimpleTimerTrigger;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSchedulerIntegratedTest {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ITimerTriggerScheduler timerTriggerScheduler;

  public TimeSchedulerIntegratedTest() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new Thread() {
      public void run() {
        while (true) {
          System.out.println("begin test TimeSchedulerIntegratedTest");
          JUnitCore.runClasses(TimeSchedulerIntegratedTest.class);
          System.out.println("end test TimeSchedulerIntegratedTest");

          System.out
              .println("begin test TimeSchedulerIntegratedTest.testScheduleKiraSimpleTimerTrigger");
          new JUnitCore().run(Request
              .method(TimeSchedulerIntegratedTest.class, "testScheduleKiraSimpleTimerTrigger"));
          System.out
              .println("end test TimeSchedulerIntegratedTest.testScheduleKiraSimpleTimerTrigger");

          System.out.println(
              "begin test TimeSchedulerIntegratedTest.testAPIForKiraTimerTriggerScheduler");
          new JUnitCore().run(Request
              .method(TimeSchedulerIntegratedTest.class, "testAPIForKiraTimerTriggerScheduler"));
          System.out
              .println("end test TimeSchedulerIntegratedTest.testAPIForKiraTimerTriggerScheduler");

          try {
            System.out.println("Will sleep for 10 seconds before next loop");
            Thread.sleep(10000L);
          } catch (InterruptedException e) {
            //ignore it
          }
        }
      }
    }.start();
  }

  @Before
  public void beforeTest() throws Exception {
    try {
      logger.warn("begin beforeTest for TimeSchedulerIntegratedTest");

      int maxConcurrentTimerTriggerCount = 1000;
      timerTriggerScheduler = new KiraTimerTriggerScheduler(maxConcurrentTimerTriggerCount);
      if (!timerTriggerScheduler.isStarted()) {
        timerTriggerScheduler.start();
      }
    } catch (Exception e) {
      logger.error("Error occurs during call beforeTest for TimeSchedulerIntegratedTest.", e);
      throw e;
    } finally {
      logger.warn("end beforeTest for TimeSchedulerIntegratedTest");
    }
  }

  @After
  public void afterTest() throws Exception {
    try {
      logger.warn("begin afterTest for TimeSchedulerIntegratedTest");

      if (!timerTriggerScheduler.isShutdown()) {
        timerTriggerScheduler.shutdown();
      }
    } catch (Exception e) {
      logger.error("Error occurs during call afterTest for TimeSchedulerIntegratedTest.", e);
      throw e;
    } finally {
      logger.warn("end afterTest for TimeSchedulerIntegratedTest");
    }
  }

  @Test
  public void testScheduleKiraSimpleTimerTrigger() throws Exception {
    try {
      logger.warn("begin call testScheduleKiraSimpleTimerTrigger");

      ITimeScheduleCallback timeScheduleCallback = new TimeScheduleCallbackForTestScheduleKiraSimpleTimerTrigger();
      int repeatCount = 10;
      int everyNSeconds = 2;
      String name = "SimpleTriggerId";
      String group = "PoolId";

      KiraSimpleTimerTrigger timerTrigger = new KiraSimpleTimerTrigger(name, group,
          timeScheduleCallback, KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER, null, null,
          null, null, 0L, repeatCount, everyNSeconds * 1000L, null, 0, false);
      timerTriggerScheduler.rescheduleTimerTrigger(timerTrigger);
      String timerTriggerId = timerTrigger.getId();
      ITimerTrigger scheduledTimerTrigger = timerTriggerScheduler
          .getTimerTrigger(timerTriggerId, true);
      scheduledTimerTrigger = waitForTriggerToBeEndReturnLatest(scheduledTimerTrigger);
      long timesHasBeenTriggered = scheduledTimerTrigger.getTimesHasBeenTriggered();
      Assert.assertEquals(repeatCount + 1, timesHasBeenTriggered);
      Assert.assertNull(scheduledTimerTrigger.getNextFireTime());
      Assert.assertNotNull(scheduledTimerTrigger.getPreviousFireTime());
      Assert.assertFalse(scheduledTimerTrigger.isUnscheduled());
      int managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(1, managedTimerTriggerCount);

      //reschedule the trigger with the same id but with different configuration and reschedule again
      repeatCount = 2;
      timerTrigger = new KiraSimpleTimerTrigger(name, group, timeScheduleCallback,
          KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER, null, null, null, null, 0L,
          repeatCount, everyNSeconds * 1000L, null, 0, false);
      timerTriggerScheduler.rescheduleTimerTrigger(timerTrigger);
      scheduledTimerTrigger = timerTriggerScheduler.getTimerTrigger(timerTriggerId, true);
      scheduledTimerTrigger = waitForTriggerToBeEndReturnLatest(scheduledTimerTrigger);
      timesHasBeenTriggered = scheduledTimerTrigger.getTimesHasBeenTriggered();
      Assert.assertEquals(repeatCount + 1, timesHasBeenTriggered);
      Assert.assertNull(scheduledTimerTrigger.getNextFireTime());
      Assert.assertNotNull(scheduledTimerTrigger.getPreviousFireTime());
      managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(1, managedTimerTriggerCount);

      timerTriggerScheduler.unscheduleTimerTrigger(timerTriggerId);
      Assert.assertNull(timerTriggerScheduler.getTimerTrigger(timerTriggerId, true));
      managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(0, managedTimerTriggerCount);

    } catch (Exception e) {
      logger.error("Error occurs during call testScheduleKiraSimpleTimerTrigger.", e);
      throw e;
    } finally {
      logger.warn("end call testScheduleKiraSimpleTimerTrigger.");
    }
  }

  private ITimerTrigger waitForTriggerToBeEndReturnLatest(ITimerTrigger scheduledTimerTrigger)
      throws Exception {
    ITimerTrigger returnValue = scheduledTimerTrigger;

    String timerTriggerId = null;
    if (null != returnValue) {
      timerTriggerId = scheduledTimerTrigger.getId();
    }
    while (null != returnValue && null != returnValue.getNextFireTime()) {
      Thread.sleep(500L);
      returnValue = timerTriggerScheduler.getTimerTrigger(timerTriggerId, true);
    }

    return returnValue;
  }

  @Test
  public void testAPIForKiraTimerTriggerScheduler() throws Exception {
    ExecutorService workThreadToRunTaskExecutorService = null;
    try {
      logger.warn("begin call testAPIForKiraTimerTriggerScheduler");
      int timerTriggerCount = 1000;
      int clientThreadCount = 300;

      CountDownLatch allTimerTriggerScheduledCountDownLatch = new CountDownLatch(
          timerTriggerCount * clientThreadCount);
      CountDownLatch mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch = new CountDownLatch(
          1);
      CountDownLatch clientThreadWorkDoneCountDownLatch = new CountDownLatch(clientThreadCount);

      List<TaskForTestAPIForKiraTimerTriggerScheduler> taskList = new ArrayList<TaskForTestAPIForKiraTimerTriggerScheduler>();
      for (int i = 0; i < clientThreadCount; i++) {
        Set<ITimerTrigger> managedTimerTriggers = new LinkedHashSet<ITimerTrigger>();
        TaskForTestAPIForKiraTimerTriggerScheduler taskForTestAPIForKiraTimerTriggerScheduler = new TaskForTestAPIForKiraTimerTriggerScheduler(
            timerTriggerScheduler, managedTimerTriggers, allTimerTriggerScheduledCountDownLatch,
            clientThreadWorkDoneCountDownLatch,
            mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch);
        taskList.add(taskForTestAPIForKiraTimerTriggerScheduler);
      }

      ITimeScheduleCallback timeScheduleCallback = new KiraTimeScheduleCallbackAdaptor(); //ccjtodo: May use customized callback

      //create trigger and assign them to workThreadSimulatorTask
      List<ITimerTrigger> allTimerTriggerList = new ArrayList<ITimerTrigger>();
      for (int i = 0; i < timerTriggerCount; i++) {
        int nextIndexOfSimulatorTaskList = i % clientThreadCount;

        ITimerTrigger timerTrigger = null;
        int randomIndex = new Random().nextInt(2);
        int priority = new Random().nextInt(10);
        Date startDate = new Date(new Date().getTime() + new Random().nextInt(10000));
        Date endDate = new Date(new Date().getTime() + new Random().nextInt(Integer.MAX_VALUE));
        int everyNSeconds = new Random().nextInt(11);
        if (everyNSeconds == 0) {
          everyNSeconds = 10;
        }
        if (0 == randomIndex) {
          int repeatCount = new Random().nextInt(10000);
          timerTrigger = new KiraSimpleTimerTrigger("simpleTimerTriggerId" + i,
              "poolId" + nextIndexOfSimulatorTaskList, timeScheduleCallback, priority, startDate,
              endDate, null, null, 0, repeatCount, everyNSeconds * 1000L, null, 0, false);
        } else if (1 == randomIndex) {
          String cronExpressionString =
              "0/" + everyNSeconds + " * * ? * *"; //every n seconds where n<=10
          timerTrigger = new KiraCronTimerTrigger("cronTimerTriggerId" + i,
              "poolId" + nextIndexOfSimulatorTaskList, timeScheduleCallback, priority, startDate,
              endDate, null, null, 0, cronExpressionString, null, 0, false);
        }

        allTimerTriggerList.add(timerTrigger);

        //TaskForTestAPIForKiraTimerTriggerScheduler workThreadSimulatorTask = taskList.get(nextIndexOfSimulatorTaskList);
        //workThreadSimulatorTask.getManagedTimerTriggers().add(timerTrigger);

        for (TaskForTestAPIForKiraTimerTriggerScheduler taskForTestAPIForKiraTimerTriggerScheduler : taskList) {
          taskForTestAPIForKiraTimerTriggerScheduler.getManagedTimerTriggers()
              .add((ITimerTrigger) timerTrigger.clone());
        }
      }

      CustomizedThreadFactory customizedThreadFactory = new CustomizedThreadFactory(
          "testAPIForKiraTimerTriggerScheduler-workThreadSimulator-");
      workThreadToRunTaskExecutorService = Executors
          .newFixedThreadPool(clientThreadCount, customizedThreadFactory);
      for (TaskForTestAPIForKiraTimerTriggerScheduler taskForTestAPIForKiraTimerTriggerScheduler : taskList) {
        workThreadToRunTaskExecutorService.submit(taskForTestAPIForKiraTimerTriggerScheduler);
      }

      //wait for all timerTriggerScheduled
      allTimerTriggerScheduledCountDownLatch.await();

      //need to check the managed trigger count in scheduler
      int managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(timerTriggerCount, managedTimerTriggerCount);

      //wait for timer to work for 10 seconds
      Thread.sleep(10000L);

      mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch.countDown();

      //let it to run for about 25 seconds with clients with api
      long timeTotalToSleepInMilliSeconds = 25000L;
      long timeLeftInMilliSeconds = timeTotalToSleepInMilliSeconds;
      while (timeLeftInMilliSeconds > 0) {
        if (clientThreadWorkDoneCountDownLatch.getCount() <= 0) {
          break;
        }
        for (ITimerTrigger timerTrigger : allTimerTriggerList) {
          timerTriggerScheduler.getTimerTrigger(timerTrigger.getId(), true);
          timerTriggerScheduler.getManagedTimerTriggerCount();
        }

        long sleepTimeInMs = 200L;
        Thread.sleep(sleepTimeInMs);
        timeLeftInMilliSeconds = timeLeftInMilliSeconds - sleepTimeInMs;
      }

      clientThreadWorkDoneCountDownLatch.await();

      //need to check the managed trigger count in scheduler again before shutdown
      managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(timerTriggerCount, managedTimerTriggerCount);

      //unschedule all now
      for (ITimerTrigger timerTrigger : allTimerTriggerList) {
        timerTriggerScheduler.unscheduleTimerTrigger(timerTrigger.getId());
      }

      //need to check the managed trigger count in scheduler after unschedule all
      managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(0, managedTimerTriggerCount);

      //sleep for 10 seconds before shutdown
      //Thread.sleep(10000L);

      workThreadToRunTaskExecutorService.shutdown();
      timerTriggerScheduler.shutdown();

      //need to check the managed trigger count in scheduler after shutdown
      managedTimerTriggerCount = timerTriggerScheduler.getManagedTimerTriggerCount();
      Assert.assertEquals(0, managedTimerTriggerCount);

    } catch (Exception e) {
      logger.error("Error occurs during call testAPIForKiraTimerTriggerScheduler.", e);
      throw e;
    } finally {
      if (null != workThreadToRunTaskExecutorService && !workThreadToRunTaskExecutorService
          .isShutdown()) {
        workThreadToRunTaskExecutorService.shutdown();
      }

      logger.warn("end call testAPIForKiraTimerTriggerScheduler.");
    }
  }

  private static class TimeScheduleCallbackForTestScheduleKiraSimpleTimerTrigger extends
      KiraTimeScheduleCallbackAdaptor {

    @Override
    public void preSubmitTask(ITimerTrigger timerTrigger) throws Exception {
      //System.out.println(KiraCommonUtils.getDateAsString(new Date()) + " preSubmitTask called for timerTrigger="+timerTrigger);
    }

    @Override
    public void submitTask(ITimerTrigger timerTrigger) throws Exception {
      System.out.println(
          KiraCommonUtils.getDateAsString(new Date()) + " submitTask success for timerTrigger="
              + timerTrigger);
    }

    @Override
    public void doAfterSubmitTaskSuccess(ITimerTrigger timerTrigger) throws Exception {
      //System.out.println(KiraCommonUtils.getDateAsString(new Date()) + " doAfterSubmitTaskSuccess called for timerTrigger="+timerTrigger);
    }

    @Override
    public void doAfterTriggerBeReadyForNextTriggering(ITimerTrigger timerTrigger)
        throws Exception {
      //System.out.println(KiraCommonUtils.getDateAsString(new Date()) + " doAfterTriggerBeReadyForNextTriggering called for timerTrigger="+timerTrigger);
    }
  }

  private static class TaskForTestAPIForKiraTimerTriggerScheduler implements Callable<Object> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ITimerTriggerScheduler timerTriggerScheduler;
    private Set<ITimerTrigger> managedTimerTriggers = new LinkedHashSet<ITimerTrigger>();
    private CountDownLatch allTimerTriggerScheduledCountDownLatch;
    private CountDownLatch clientThreadWorkDoneCountDownLatch;
    private CountDownLatch mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch;

    public TaskForTestAPIForKiraTimerTriggerScheduler(ITimerTriggerScheduler timerTriggerScheduler,
        Set<ITimerTrigger> managedTimerTriggers,
        CountDownLatch allTimerTriggerScheduledCountDownLatch,
        CountDownLatch clientThreadWorkDoneCountDownLatch,
        CountDownLatch mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch) {
      this.timerTriggerScheduler = timerTriggerScheduler;
      this.managedTimerTriggers = managedTimerTriggers;
      this.allTimerTriggerScheduledCountDownLatch = allTimerTriggerScheduledCountDownLatch;
      this.clientThreadWorkDoneCountDownLatch = clientThreadWorkDoneCountDownLatch;
      this.mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch = mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch;
    }

    public Set<ITimerTrigger> getManagedTimerTriggers() {
      return managedTimerTriggers;
    }

    @Override
    public Object call() throws Exception {
      try {
        if (null != timerTriggerScheduler && timerTriggerScheduler.isStarted()) {
          for (ITimerTrigger timerTrigger : managedTimerTriggers) {
            timerTriggerScheduler.rescheduleTimerTrigger((ITimerTrigger) timerTrigger.clone());
            allTimerTriggerScheduledCountDownLatch.countDown();
            Assert.assertNotNull(timerTriggerScheduler.getTimerTrigger(timerTrigger.getId(), true));
          }
        }

        //wait for main thread to assert count
        mainThreadAssertCountAfterAllTimerTriggerScheduledCountDownLatch.await();

        int rescheduleTimes = 4;
        while (null != timerTriggerScheduler && timerTriggerScheduler.isStarted()) {
          for (ITimerTrigger timerTrigger : managedTimerTriggers) {
            timerTriggerScheduler.unscheduleTimerTrigger(timerTrigger.getId());
            Assert.assertNull(timerTriggerScheduler.getTimerTrigger(timerTrigger.getId(), true));

            timerTriggerScheduler.rescheduleTimerTrigger((ITimerTrigger) timerTrigger.clone());
            Assert.assertNotNull(timerTriggerScheduler.getTimerTrigger(timerTrigger.getId(), true));

            //test call getManagedTimerTriggerCount()
            timerTriggerScheduler.getManagedTimerTriggerCount();
          }

          //wait for 5 seconds then reschedule all
          try {
            Thread.sleep(5000L);
          } catch (InterruptedException e) {
            //ignore
          }
          rescheduleTimes--;
          if (rescheduleTimes < 0) {
            break;
          }
        }
      } finally {
        clientThreadWorkDoneCountDownLatch.countDown();
      }

      return null;
    }

  }
}
