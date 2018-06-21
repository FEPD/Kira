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
package com.yihaodian.architecture.kira.schedule.time.internal.timer;

import com.yihaodian.architecture.kira.schedule.time.internal.service.ITimerTriggerToBeFiredHandleService;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.TimerTriggerComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used internally only.
 *
 * The timer can not be started again after it shutdown.
 */
public class KiraTimer implements ITimer {

  private static final int TIMER_STATE_INIT = 0;
  private static final int TIMER_STATE_STARTING = 1;
  private static final int TIMER_STATE_STARTED = 2;
  private static final int TIMER_STATE_SHUTTINGDOWN = 3;
  private static final int TIMER_STATE_SHUTDOWN = 4;
  private static long DEFAULT_WAITFOR_TIMER_STATRTED_TIMEOUT_INSECONDS = 120L;
  private final AtomicInteger kiraTimerSchedulerThreadCount = new AtomicInteger();
  private final String timerTriggerSchedulerId;
  private final int indexInTimerTriggerScheduler;
  private final String id;
  private final Set<ITimerTrigger>[] timeWheel;
  private final int indexMask;
  private final Thread schedulerThread;
  private final CountDownLatch kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch = new CountDownLatch(
      1);
  private final CountDownLatch kiraTimerSchedulerThreadStartedDownLatch = new CountDownLatch(1);
  private final ReadWriteLock lockForTicks = new ReentrantReadWriteLock(); //used for timeWheel to protect the ticks

  private final ReadWriteLock lockForTriggerInfoOnWheel = new ReentrantReadWriteLock(); //used to manage the realationship between the  trigger and timeWheel
  private final AtomicInteger timerState = new AtomicInteger(KiraTimer.TIMER_STATE_INIT);
  private final ReadWriteLock lockForTimerState = new ReentrantReadWriteLock();
  private final CountDownLatch kiraTimerSchedulerThreadShutdownDownLatch = new CountDownLatch(1);
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  private ITimerTriggerToBeFiredHandleService timerTriggerToBeFiredHandleService;
  private volatile long durationPerTickInMs;
  private volatile long kiraTimerSchedulerThreadStartTimeInMs;
  private volatile long ticks;
  //When requestShutdown=true, all works need to be aborted and do not retry anything. Add this to support quick response.
  private volatile boolean requestShutdown;

  public KiraTimer(String timerTriggerSchedulerId, int indexInTimerTriggerScheduler,
      ITimerTriggerToBeFiredHandleService timerTriggerToBeFiredHandleService) {
    this(timerTriggerSchedulerId, indexInTimerTriggerScheduler, timerTriggerToBeFiredHandleService,
        250L, TimeUnit.MILLISECONDS, 1024); //256 seconds per wheel= (4 minutes + 16 seconds)
  }

  /**
   * @param tickCountPerWheel should be power of two
   */
  public KiraTimer(String timerTriggerSchedulerId, int indexInTimerTriggerScheduler,
      ITimerTriggerToBeFiredHandleService timerTriggerToBeFiredHandleService, long durationPerTick,
      TimeUnit timeUnitForDurationPerTick, int tickCountPerWheel) {
    this.timerTriggerSchedulerId = timerTriggerSchedulerId;
    this.indexInTimerTriggerScheduler = indexInTimerTriggerScheduler;
    this.timerTriggerToBeFiredHandleService = timerTriggerToBeFiredHandleService;
    this.id = timerTriggerSchedulerId + "-KiraTimer-" + indexInTimerTriggerScheduler;
    //this.id = InetAddress.getLocalHost().getHostName() + System.currentTimeMillis();
    int normalizedTickCountPerWheel = normalizeTickCountPerWheel(tickCountPerWheel);
    //timeWheel = new ConcurrentSkipListSet[normalizedTickCountPerWheel];
    //timeWheel = new TreeSet[normalizedTickCountPerWheel];
    timeWheel = new HashSet[normalizedTickCountPerWheel];
    for (int i = 0; i < timeWheel.length; i++) {
      //Do not work with ConcurrentSkipListSet and TreeSet which strictly require to obey the rules described in Comparable. But i just want to use the id to identify the trigger. In this case no way to obey that rule perfectly.
      //timeWheel[i] = new ConcurrentSkipListSet<ITimerTrigger>(new TimerTriggerComparator());
      //timeWheel[i] = new ConcurrentSkipListSet<ITimerTrigger>();
      //timeWheel[i] = new TreeSet<ITimerTrigger>(new TimerTriggerComparator());
      //timeWheel[i] = new TreeSet<ITimerTrigger>();
      timeWheel[i] = new HashSet<ITimerTrigger>();
    }
    indexMask = timeWheel.length - 1;
    durationPerTickInMs = timeUnitForDurationPerTick.toMillis(durationPerTick);

    schedulerThread = new KiraTimerSchedulerThread();

    logger.info("KiraTimer is instantiated. KiraTimer's id=" + this.getId());
  }

  public static void main(String[] args) throws Exception {

  }

  private int normalizeTickCountPerWheel(int tickCountPerWheel) {
    int normalizedTickCountPerWheel = 1;
    while (normalizedTickCountPerWheel < tickCountPerWheel) {
      normalizedTickCountPerWheel <<= 1;
    }
    return normalizedTickCountPerWheel;
  }

  @Override
  public void start() throws Exception {
    long startHandleTime = System.currentTimeMillis();
    try {
      lockForTimerState.writeLock().lock();
      try {
        if (timerState.compareAndSet(KiraTimer.TIMER_STATE_INIT, KiraTimer.TIMER_STATE_STARTING)) {
          try {
            logger.info("KiraTimer is starting... KiraTimer's id=" + this.getId());
            schedulerThread.start();
            waitForKiraTimerSchedulerThreadStartTimeInitialized();
          } finally {
            timerState.set(KiraTimer.TIMER_STATE_STARTED);
            kiraTimerSchedulerThreadStartedDownLatch.countDown();
            logger.info("KiraTimer is started. KiraTimer's id=" + this.getId());
          }
        } else {
          String errorMessage =
              "Failed to start KiraTimer. It may not be in correct state. KiraTimer=" + this
                  .toDetailString();
          logger.error(errorMessage);
          throw new IllegalStateException(errorMessage);
        }
      } finally {
        lockForTimerState.writeLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when call start() for KiraTimer=" + this, e);
      throw e;
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info(
          "It takes " + handleCostTime + " milliseconds to start KiraTimer. KiraTimer=" + this
              .toDetailString());
    }
  }

  private void waitForKiraTimerSchedulerThreadStartTimeInitialized() {
    try {
      kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch.await();
    } catch (InterruptedException interruptedException) {
    }
  }

  @Override
  public void shutdown() {
    long startHandleTime = System.currentTimeMillis();
    try {
      this.requestShutdown = true;

      boolean setToShuttingDownSuccess = false;
      lockForTimerState.writeLock().lock();
      try {
        if (timerState
            .compareAndSet(KiraTimer.TIMER_STATE_STARTED, KiraTimer.TIMER_STATE_SHUTTINGDOWN)) {
          setToShuttingDownSuccess = true;
        }
      } finally {
        lockForTimerState.writeLock().unlock();
      }

      if (setToShuttingDownSuccess) {
        kiraTimerSchedulerThreadShutdownDownLatch.await();
        lockForTimerState.writeLock().lock();
        try {
          timerState.set(KiraTimer.TIMER_STATE_SHUTDOWN);
        } finally {
          lockForTimerState.writeLock().unlock();
          logger.info("KiraTimer is shutdown. KiraTimer's id=" + this.getId());
        }
      } else {
        String errorMessage =
            "Failed to shutdown KiraTimer. It may not be in the started state. KiraTimer=" + this
                .toDetailString();
        logger.error(errorMessage);
        throw new IllegalStateException(errorMessage);
      }
    } catch (Exception e) {
      logger.error("Error occurs when call shutdown() for KiraTimer=" + this, e);
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info(
          "It takes " + handleCostTime + " milliseconds to shutdown KiraTimer. KiraTimer=" + this
              .toDetailString());
    }
  }

  /**
   * Wait for this timer to be started. Will wait for 2 minutes at most now.
   *
   * @return true if it was started.
   */
  private boolean isTimerStartedAndWaitIfNeeded() {
    boolean returnValue = false;
    try {
      lockForTimerState.readLock().lock();
      try {
        if (KiraTimer.TIMER_STATE_STARTED != KiraTimer.this.timerState.get()) {
          try {
            if (KiraTimer.TIMER_STATE_INIT == KiraTimer.this.timerState.get()
                || KiraTimer.TIMER_STATE_STARTING == KiraTimer.this.timerState.get()) {
              logger.warn(
                  "Timer may not be started yet. So will wait for it for at most {} seconds now.",
                  DEFAULT_WAITFOR_TIMER_STATRTED_TIMEOUT_INSECONDS);
              kiraTimerSchedulerThreadStartedDownLatch
                  .await(DEFAULT_WAITFOR_TIMER_STATRTED_TIMEOUT_INSECONDS, TimeUnit.SECONDS);
            } else {
              logger.warn(
                  "No need to wait for timer to be started for the timer is not be in init or starting state. timer's id={} and timerState={}",
                  this.getId(), this.timerState.get());
            }
          } finally {
            if (KiraTimer.TIMER_STATE_STARTED == KiraTimer.this.timerState.get()) {
              returnValue = true;
            }
          }
        } else {
          returnValue = true;
        }
      } finally {
        lockForTimerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isTimerStartedAndWaitIfNeeded. timer's id=" + this.getId()
          + " and timerState=" + this.timerState.get(), e);
    }

    return returnValue;
  }

  private boolean isTimerShuttingdown() {
    boolean isTimerShuttingdown = false;
    try {
      lockForTimerState.readLock().lock();
      try {
        isTimerShuttingdown = (KiraTimer.TIMER_STATE_SHUTTINGDOWN == KiraTimer.this.timerState
            .get());
      } finally {
        lockForTimerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Exception occurs for isTimerShuttingdown. kiraTimer's id=" + this.getId()
          + " and timerState=" + this.timerState.get(), e);
    }

    return isTimerShuttingdown;
  }

  private boolean isTimerShutdown() {
    boolean isTimerShutdown = false;
    try {
      lockForTimerState.readLock().lock();
      try {
        isTimerShutdown = (KiraTimer.TIMER_STATE_SHUTDOWN == KiraTimer.this.timerState.get());
      } finally {
        lockForTimerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Exception occurs for isTimerShutdown. kiraTimer's id=" + this.getId()
          + " and timerState=" + this.timerState.get(), e);
    }

    return isTimerShutdown;
  }

  @Override
  public boolean addTimerTrigger(ITimerTrigger timerTrigger) {
    boolean returnValue = false;
    if (null != timerTrigger) {
      try {
        lockForTimerState.readLock().lock();
        try {
          boolean isTimerStartedAndWaitIfNeeded = isTimerStartedAndWaitIfNeeded();
          if (isTimerStartedAndWaitIfNeeded) {
            Date nextFireTime = timerTrigger.getNextFireTime();
            if (null != nextFireTime) {
              lockForTicks.readLock().lock();
              try {
                lockForTriggerInfoOnWheel.writeLock().lock();
                try {
                  long nextFireTimeInMs = nextFireTime.getTime();
                  long lifeOnHashedWheelInMs =
                      nextFireTimeInMs - kiraTimerSchedulerThreadStartTimeInMs;
                  timerTrigger.setLifeOnHashedWheelInMs(lifeOnHashedWheelInMs);
                  long ticksCalculated = lifeOnHashedWheelInMs / durationPerTickInMs;
                  long finalTicks = Math.max(ticksCalculated, ticks); //It may be the past.
                  int indexOnHashedWheel = (int) (finalTicks & indexMask);
                  timerTrigger.setIndexOnHashedWheel(indexOnHashedWheel);

                  long remainingRoundsOnHashedWheel = (ticksCalculated - ticks) / timeWheel.length;
                  timerTrigger.setRemainingRoundsOnHashedWheel(remainingRoundsOnHashedWheel);

                  if (KiraTimer.TIMER_STATE_STARTED == timerState.get()) {
                    boolean isUnscheduled = timerTrigger.isUnscheduled();
                    if (isUnscheduled) {
                      logger.info(
                          "The trigger is unscheduled. So no need to add it to time wheel. timerTrigger={} in timer={}",
                          timerTrigger.getId(), this.getId());
                    } else {
                      //need to remove the exist one first, that may be the unscheduled one.
                      boolean removeExistResult = timeWheel[indexOnHashedWheel]
                          .remove(timerTrigger);
                      if (removeExistResult) {
                        logger.error(
                            "removeExistResult is true. May have bugs. timerTrigger={} and timer={}",
                            timerTrigger, this.toDetailString());
                      }
                      boolean addedSuccess = timeWheel[indexOnHashedWheel].add(timerTrigger);
                      if (!addedSuccess) {
                        logger.error(
                            "Failed to add timeTrigger to time wheel. It may already has this timer? You should always remove it first and then add it. timerTrigger={} and time={}",
                            timerTrigger, this.toDetailString());
                      } else {
                        logger.info("addTimerTrigger success for timerTrigger={} in timer={}",
                            timerTrigger.getId(), this.getId());
                        returnValue = true;
                      }
                    }
                  } else {
                    logger.error(
                        "No need to add to the time wheel now. For KiraTimer is not started. timerTrigger={} in KiraTimer={}",
                        timerTrigger, this.toDetailString());
                  }
                } finally {
                  lockForTriggerInfoOnWheel.writeLock().unlock();
                }
              } finally {
                lockForTicks.readLock().unlock();
              }
            } else {
              logger.info(
                  "nextFireTime is null. So need to add to the time wheel. for timerTrigger={}",
                  timerTrigger);
            }
          } else {
            if (this.isTimerShuttingdown() || this.isTimerShutdown()) {
              logger.info(
                  "The timer is shuttingdown or shutdown. So can not addTimerTrigger. timer's id={} and timerState={} and timerTrigger={}",
                  this.getId(), this.timerState.get(), timerTrigger);
            } else {
              logger.error(
                  "May have some bugs. Can not addTimerTrigger. timer's id={} and timerState={} and timerTrigger={}",
                  this.getId(), this.timerState.get(), timerTrigger);
            }
          }
        } finally {
          lockForTimerState.readLock().unlock();
        }
      } catch (Exception e) {
        logger.error("Error occurs for addTimerTrigger. timerTrigger=" + timerTrigger
                + " and kiraTimer's id=" + this.getId() + " and timerState=" + this.timerState.get(),
            e);
      }
    } else {
      logger.error("timerTrigger should not be null for addTimerTrigger.");
    }
    return returnValue;
  }

  @Override
  public boolean removeTimerTrigger(ITimerTrigger timerTrigger, boolean unscheduled) {
    boolean returnValue = false;
    if (null != timerTrigger) {
      try {
        lockForTimerState.readLock().lock();
        try {
          boolean isTimerStartedAndWaitIfNeeded = isTimerStartedAndWaitIfNeeded();
          if (isTimerStartedAndWaitIfNeeded) {
            lockForTicks.readLock().lock();
            try {
              lockForTriggerInfoOnWheel.writeLock().lock();
              try {
                if (unscheduled) {
                  //Need to mark the trigger as unscheduled first, so that trigger will never be re-added to timer again if possible
                  timerTrigger.setUnscheduled(unscheduled);
                }

                int indexOnHashedWheel = timerTrigger.getIndexOnHashedWheel();
                returnValue = timeWheel[indexOnHashedWheel].remove(timerTrigger);
              } finally {
                lockForTriggerInfoOnWheel.writeLock().unlock();
              }
            } finally {
              lockForTicks.readLock().unlock();
            }
          } else {
            logger.error(
                "isTimerStartedAndWaitIfNeeded return false. So can not removeTimerTrigger. timer's id={} and timerState={} and timerTrigger={}",
                this.getId(), this.timerState.get(), timerTrigger);
          }
        } finally {
          lockForTimerState.readLock().unlock();
        }
      } catch (Exception e) {
        logger.error("Error occurs for removeTimerTrigger. timerTrigger=" + timerTrigger
            + "and kiraTimer's id=" + this.getId() + " and timerState=" + this.timerState.get(), e);
      }
    } else {
      logger.error("timerTrigger should not be null for removeTimerTrigger.");
    }

    return returnValue;
  }

  @Override
  public boolean prepareForNextTriggering(ITimerTrigger timerTrigger) {
    boolean returnValue = false;
    if (null != timerTrigger) {
      try {
        lockForTimerState.readLock().lock();
        try {
          //Just continue no matter what the state of the timer. So no need to check the state of the timer.
          lockForTriggerInfoOnWheel.writeLock().lock();
          try {
            //need to do this when holding lock because the trigger's data need to be kept consistent in timer.
            timerTrigger.triggered();
            returnValue = true;
          } finally {
            lockForTriggerInfoOnWheel.writeLock().unlock();
          }
        } finally {
          lockForTimerState.readLock().unlock();
        }
      } catch (Exception e) {
        logger.error("Exception occurs for prepareForNextTriggering. timerTrigger=" + timerTrigger
                + " and kiraTimer's id=" + this.getId() + " and timerState=" + this.timerState.get(),
            e);
      }
    } else {
      logger.error(
          "timerTrigger is null for prepareForNextTriggering. timer's id={} and timerState={}",
          this.getId(), this.timerState.get());
    }

    return returnValue;
  }

  private boolean isTimerStarting() {
    boolean isTimerStarting = false;
    try {
      lockForTimerState.readLock().lock();
      try {
        isTimerStarting = (KiraTimer.TIMER_STATE_STARTING == KiraTimer.this.timerState.get());
      } finally {
        lockForTimerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Exception occurs for isTimerStarting. kiraTimer's id=" + this.getId()
          + " and timerState=" + this.timerState.get(), e);
    }

    return isTimerStarting;
  }

  private boolean isTimerStarted() {
    boolean isTimerStarted = false;
    try {
      lockForTimerState.readLock().lock();
      try {
        isTimerStarted = (KiraTimer.TIMER_STATE_STARTED == KiraTimer.this.timerState.get());
      } finally {
        lockForTimerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error(
          "Exception occurs for isTimerStarted. kiraTimer's id=" + this.getId() + " and timerState="
              + this.timerState.get(), e);
    }

    return isTimerStarted;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
    KiraTimer other = (KiraTimer) obj;
    if (id == null) {
			if (other.id != null) {
				return false;
			}
    } else if (!id.equals(other.id)) {
			return false;
		}
    return true;
  }

  private String toDetailString() {
    return "KiraTimer [ id=" + id
        + ", timerTriggerSchedulerId="
        + timerTriggerSchedulerId
        + "kiraTimerSchedulerThreadCount="
        + kiraTimerSchedulerThreadCount
        + ", indexInTimerTriggerScheduler="
        + indexInTimerTriggerScheduler
        + ", timerTriggerToBeFiredHandleService="
        + timerTriggerToBeFiredHandleService
        + ", timeWheel="
        + Arrays.toString(timeWheel)
        + ", indexMask="
        + indexMask
        + ", durationPerTickInMs="
        + durationPerTickInMs
        + ", schedulerThread="
        + schedulerThread
        + ", kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch="
        + kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch
        + ", kiraTimerSchedulerThreadStartedDownLatch="
        + kiraTimerSchedulerThreadStartedDownLatch
        + ", kiraTimerSchedulerThreadStartTimeInMs="
        + kiraTimerSchedulerThreadStartTimeInMs + ", ticks=" + ticks
        + ", lockForTicks=" + lockForTicks
        + ", lockForTriggerInfoOnWheel=" + lockForTriggerInfoOnWheel
        + ", timerState=" + timerState
        + ", lockForTimerStateManagement="
        + lockForTimerState
        + ", kiraTimerSchedulerThreadShutdownDownLatch="
        + kiraTimerSchedulerThreadShutdownDownLatch + "]";
  }

  @Override
  public String toString() {
    return "KiraTimer [ id=" + id
        + ", timerTriggerSchedulerId="
        + timerTriggerSchedulerId
        + ", kiraTimerSchedulerThreadCount="
        + kiraTimerSchedulerThreadCount
        + ", indexInTimerTriggerScheduler="
        + indexInTimerTriggerScheduler
        + ", timerTriggerToBeFiredHandleService="
        + timerTriggerToBeFiredHandleService
        + ", timeWheel="
        + Arrays.toString(timeWheel)
        + ", indexMask="
        + indexMask
        + ", durationPerTickInMs="
        + durationPerTickInMs
        + ", schedulerThread="
        + schedulerThread
        + ", kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch="
        + kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch
        + ", kiraTimerSchedulerThreadStartedDownLatch="
        + kiraTimerSchedulerThreadStartedDownLatch
        + ", kiraTimerSchedulerThreadStartTimeInMs="
        + kiraTimerSchedulerThreadStartTimeInMs + ", ticks=" + ticks
        + ", lockForTicks=" + lockForTicks
        + ", lockForTriggerInfoOnWheel=" + lockForTriggerInfoOnWheel
        + ", timerState=" + timerState
        + ", lockForTimerStateManagement="
        + lockForTimerState
        + ", kiraTimerSchedulerThreadShutdownDownLatch="
        + kiraTimerSchedulerThreadShutdownDownLatch + "]";
  }

  private final class KiraTimerSchedulerThread extends Thread {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public KiraTimerSchedulerThread() {
      super(KiraTimer.this.getId() + "-KiraTimerSchedulerThread-" + kiraTimerSchedulerThreadCount
          .incrementAndGet());
      this.setDaemon(false);
      this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
      logger.info("KiraTimerSchedulerThread is running. name=" + this.getName());
      KiraTimer.this.kiraTimerSchedulerThreadStartTimeInMs = System.currentTimeMillis();
      KiraTimer.this.kiraTimerSchedulerThreadStartTimeInitializedCountDownLatch.countDown();

      boolean isTimerStarting = KiraTimer.this.isTimerStarting();
      while (isTimerStarting) {
        logger.info("KiraTimerSchedulerThread will yield when KiraTimer is starting.");
        Thread.yield();

        isTimerStarting = KiraTimer.this.isTimerStarting();
      }

      boolean isTimerStarted = KiraTimer.this.isTimerStarted();
      List<ITimerTrigger> timerTriggerListToBeFired = new ArrayList<ITimerTrigger>();
      while (isTimerStarted) {
        try {
          if (this.isNormalOperationNeedToBeAborted(
              "infinite loop in run() of KiraTimerSchedulerThread")) {
            break;
          }

          long timePastAfterKiraTimerSchedulerThreadStartedInMs = waitForNextTick();

          lockForTimerState.readLock().lock();
          try {
            if (timePastAfterKiraTimerSchedulerThreadStartedInMs >= 0) {
              findTimerTriggerListToBeFired(timerTriggerListToBeFired,
                  timePastAfterKiraTimerSchedulerThreadStartedInMs);
              if (!timerTriggerListToBeFired.isEmpty()) {
                Collections.sort(timerTriggerListToBeFired, new TimerTriggerComparator());
                //Collections.sort(timerTriggerListToBeFired);
                handleTimerTriggerListToBeFired(timerTriggerListToBeFired);
              }
            } else {
              logger.error(
                  "timePastAfterKiraTimerSchedulerThreadStartedInMs < 0 which should not happen. timePastAfterKiraTimerSchedulerThreadStartedInMs={}",
                  timePastAfterKiraTimerSchedulerThreadStartedInMs);
            }
          } finally {
            lockForTimerState.readLock().unlock();
          }
        } catch (Exception e) {
          logger.error(
              "Exception occurs during one time loop for KiraTimerSchedulerThread. KiraTimer="
                  + KiraTimer.this.toDetailString(), e);
        } finally {
          timerTriggerListToBeFired.clear();
          isTimerStarted = KiraTimer.this.isTimerStarted();
        }
      }

      kiraTimerSchedulerThreadShutdownDownLatch.countDown();
    }

    private void handleTimerTriggerListToBeFired(List<ITimerTrigger> timerTriggerListToBeFired) {
      for (ITimerTrigger oneTimerTrigger : timerTriggerListToBeFired) {
        if (this.isNormalOperationNeedToBeAborted("handleTimerTriggerListToBeFired")) {
          break;
        }

        try {
          boolean isUnscheduled = oneTimerTrigger.isUnscheduled();
          if (isUnscheduled) {
            logger.info(
                "The trigger is unscheduled. So do not handleTimerTriggerListToBeFired. oneTimerTrigger={} in timer={}",
                oneTimerTrigger.getId(), this.getId());
          } else {
            KiraTimer.this.timerTriggerToBeFiredHandleService
                .handleTimerTriggerToBeFired(KiraTimer.this, oneTimerTrigger);
          }
        } catch (Exception e) {
          logger.error(
              "Error occurs for KiraTimer.this.timerTriggerToBeFiredHandleService.handleTimerTriggerToBeFired. timer="
                  + KiraTimer.this.getId() + " and oneTimerTrigger=" + oneTimerTrigger.getId(), e);
        }
      }
    }

    private boolean isNormalOperationNeedToBeAborted(String operationSummary) {
      boolean returnValue = false;

      if (KiraTimer.this.requestShutdown) {
        returnValue = true;
        if (null != logger) {
          logger.warn(
              "The KiraTimer may requestShutdown. So do not continue with " + operationSummary
                  + ". and timer={} and timerState={}", KiraTimer.this.getId(),
              KiraTimer.this.timerState.get());
        }
      }

      return returnValue;
    }

    private void findTimerTriggerListToBeFired(
        List<ITimerTrigger> timerTriggerListToBeFired,
        long timePastAfterKiraTimerSchedulerThreadStartedInMs) {
      lockForTicks.writeLock().lock();
      try {
        int timeWheelIndex = (int) (ticks & indexMask);
        lockForTriggerInfoOnWheel.writeLock().lock();
        try {
          Set<ITimerTrigger> timerTriggers = timeWheel[timeWheelIndex];
          Iterator<ITimerTrigger> it = timerTriggers.iterator();
          ITimerTrigger oneTimerTrigger = null;
          while (it.hasNext()) {
            if (this.isNormalOperationNeedToBeAborted("findTimerTriggerListToBeFired")) {
              break;
            }

            oneTimerTrigger = it.next();
            boolean isUnscheduled = oneTimerTrigger.isUnscheduled();
            if (isUnscheduled) {
              it.remove();
              logger.error(
                  "Unscheduled timerTrigger was removed on timeWheel[{}] of timer={} during findTimerTriggerListToBeFired. This should not happen for the unscheduled trigger should not be added to time wheel. May have some bugs. oneTimerTrigger={}",
                  timeWheelIndex, KiraTimer.this.getId(), oneTimerTrigger);
            } else {
              long remainingRoundsOnHashedWheel = oneTimerTrigger.getRemainingRoundsOnHashedWheel();
              if (0 >= remainingRoundsOnHashedWheel) {
                it.remove();
                long lifeOnHashedWheelInMs = oneTimerTrigger.getLifeOnHashedWheelInMs();
                if (lifeOnHashedWheelInMs <= timePastAfterKiraTimerSchedulerThreadStartedInMs) {
                  timerTriggerListToBeFired.add(oneTimerTrigger);
                } else {
                  logger.error(
                      "The timerTrigger was placed into a wrong slot. This should never happen. oneTimerTrigger="
                          + oneTimerTrigger.toString()
                          + " and timePastAfterKiraTimerSchedulerThreadStartedInMs="
                          + timePastAfterKiraTimerSchedulerThreadStartedInMs);
                }
              } else {
                oneTimerTrigger.setRemainingRoundsOnHashedWheel(remainingRoundsOnHashedWheel - 1);
              }
            }
          }
        } finally {
          lockForTriggerInfoOnWheel.writeLock().unlock();
        }
      } finally {
        ticks++;
        lockForTicks.writeLock().unlock();
      }
    }

    /**
     * @return The time past after kiraTimerSchedulerThread started in milliseconds
     */
    private long waitForNextTick() {
      long deadlineInMs = KiraTimer.this.durationPerTickInMs * (ticks + 1);
      while (true) {
        long timePastAfterKiraTimerSchedulerThreadStartedInMs =
            System.currentTimeMillis() - KiraTimer.this.kiraTimerSchedulerThreadStartTimeInMs;
        long remainTimeInMs = deadlineInMs - timePastAfterKiraTimerSchedulerThreadStartedInMs;
        if (remainTimeInMs <= 0) {
          return timePastAfterKiraTimerSchedulerThreadStartedInMs;
        } else {
          try {
            //logger.debug(".");
            //System.out.println(".");
            boolean isTimerStarted = KiraTimer.this.isTimerStarted();
            if (isTimerStarted) {
              //only need to sleep when timer is in started state.
              Thread.sleep(remainTimeInMs);
            }
          } catch (InterruptedException e) {
            boolean isTimerStarted = KiraTimer.this.isTimerStarted();
            if (!isTimerStarted) {
              return -1;
            }
          }
        }
      }
    }
  }

}
