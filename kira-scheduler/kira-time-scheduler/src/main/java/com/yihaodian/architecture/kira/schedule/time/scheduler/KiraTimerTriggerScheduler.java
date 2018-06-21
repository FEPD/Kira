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
package com.yihaodian.architecture.kira.schedule.time.scheduler;

import com.yihaodian.architecture.hedwig.common.uuid.UUIDUtils;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.schedule.time.internal.service.ITimerTriggerToBeFiredHandleService;
import com.yihaodian.architecture.kira.schedule.time.internal.service.KiraTimerTriggerToBeFiredHandleService;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.ITimer;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.KiraTimer;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scheduler can be started again after it was shutdown.
 */
public class KiraTimerTriggerScheduler implements ITimerTriggerScheduler {

  private static final int DEFAULT_TIMER_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int SCHEDULER_STATE_INIT = 0;
  private static final int SCHEDULER_STATE_STARTING = 1;
  private static final int SCHEDULER_STATE_STARTED = 2;
  private static final int SCHEDULER_STATE_SHUTTINGDOWN = 3;
  private static final int SCHEDULER_STATE_SHUTDOWN = 4;
  private static long DEFAULT_WAITFOR_TIMERTRIGGERSCHEDULER_STATRTED_TIMEOUT_INMIlLISECONDS = 120000L;
  private final String id;
  private final ReadWriteLock lockForTimerManagement = new ReentrantReadWriteLock(); //used to add/remove/get ITimer
  private final ReadWriteLock lockForTimerTriggerManagement = new ReentrantReadWriteLock(); //used to add/remove/get TimerTrigger
  private final AtomicInteger schedulerState = new AtomicInteger(
      KiraTimerTriggerScheduler.SCHEDULER_STATE_INIT);
  private final ReadWriteLock lockForSchedulerState = new ReentrantReadWriteLock();
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  private int maxConcurrentTimerTriggerCount;
  private ITimerTriggerToBeFiredHandleService timerTriggerToBeFiredHandleService;
  private ITimer[] timers;
  private int timerCount;
  private Map<String, ITimer> timerIdTimerMap = new ConcurrentHashMap<String, ITimer>();
  private Map<String, ITimer> timerTriggerIdTimerMap = new ConcurrentHashMap<String, ITimer>();
  private ConcurrentHashMap<ITimer, Set<String>> timerTimerTriggerIdSetMap = new ConcurrentHashMap<ITimer, Set<String>>();
  private Map<String, ITimerTrigger> managedTimerTriggerIdTimerTriggerMap = new ConcurrentHashMap<String, ITimerTrigger>();

  public KiraTimerTriggerScheduler(int maxConcurrentTimerTriggerCount) {
    this(maxConcurrentTimerTriggerCount, (0 >= DEFAULT_TIMER_COUNT) ? 1 : DEFAULT_TIMER_COUNT);
  }

  /**
   * @param maxConcurrentTimerTriggerCount The timerTrigger count which will be triggered in the
   * meantime. Should be not more than {@link KiraTimerTriggerToBeFiredHandleService#DEFAULT_MAX_POOL_SIZE
   * KiraTimerTriggerToBeFiredHandleService.DEFAULT_MAX_POOL_SIZE}
   * @param timerCount the timerCount which should be less than or equals with the cpu's processor
   * count.
   */
  public KiraTimerTriggerScheduler(int maxConcurrentTimerTriggerCount, int timerCount) {
    this.id = "KiraTimerTriggerScheduler-" + UUIDUtils.getUUID();
    if (maxConcurrentTimerTriggerCount <= 0) {
      throw new ValidationException(
          "maxConcurrentTimerTriggerCount should be >0 . maxConcurrentTimerTriggerCount="
              + maxConcurrentTimerTriggerCount);
    }
    this.maxConcurrentTimerTriggerCount = maxConcurrentTimerTriggerCount;
    timerTriggerToBeFiredHandleService = new KiraTimerTriggerToBeFiredHandleService(this.id,
        this.maxConcurrentTimerTriggerCount);

    if (timerCount <= 0) {
      throw new ValidationException(
          "timerCount should be >0 . timerCount=" + timerCount);
    }
    this.timerCount = timerCount;
    lockForTimerManagement.writeLock().lock();
    try {
      timers = new ITimer[this.timerCount];
    } finally {
      lockForTimerManagement.writeLock().unlock();
    }
    logger.info("KiraTimerTriggerScheduler is instantiated. KiraTimerTriggerScheduler's id=" + this
        .getId());
  }

  @Override
  public String getId() {
    return id;
  }

  private boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded() {
    boolean returnValue = false;
    try {
      lockForSchedulerState.readLock().lock();
      try {
        if (KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED != schedulerState.get()) {
          try {
            if (KiraTimerTriggerScheduler.SCHEDULER_STATE_INIT == schedulerState.get()
                || KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTING == schedulerState.get()) {
              logger.warn(
                  "TimerTriggerScheduler may not be started yet. So will wait for it for at most {} milliseconds now.",
                  DEFAULT_WAITFOR_TIMERTRIGGERSCHEDULER_STATRTED_TIMEOUT_INMIlLISECONDS);

              long waitTimeInMilliSeconds = 0;
              while (KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED != schedulerState.get()
                  && waitTimeInMilliSeconds
                  < DEFAULT_WAITFOR_TIMERTRIGGERSCHEDULER_STATRTED_TIMEOUT_INMIlLISECONDS) {
                Thread.sleep(100);
                waitTimeInMilliSeconds += 100;
                logger.warn(
                    "TimerTriggerScheduler may not be started yet. Have already wait for {} milliseconds now.",
                    waitTimeInMilliSeconds);
              }
            } else {
              logger.warn(
                  "No need to wait for timerTriggerScheduler to be started for the timerTriggerScheduler is not be in init or starting state. timerTriggerScheduler's id={} and schedulerState={}",
                  this.getId(), this.schedulerState.get());
            }
          } finally {
            if (KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED == schedulerState.get()) {
              returnValue = true;
            }
          }
        } else {
          returnValue = true;
        }
      } finally {
        lockForSchedulerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for isTimerTriggerSchedulerStartedAndWaitIfNeeded. timerTriggerScheduler's id="
              + this.getId() + " and schedulerState=" + this.schedulerState.get(), e);
    }
    return returnValue;
  }

  @Override
  public void start() throws Exception {
    long startHandleTime = System.currentTimeMillis();
    try {
      lockForSchedulerState.writeLock().lock();
      try {
        if (schedulerState.compareAndSet(KiraTimerTriggerScheduler.SCHEDULER_STATE_INIT,
            KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTING)
            || schedulerState.compareAndSet(KiraTimerTriggerScheduler.SCHEDULER_STATE_SHUTDOWN,
            KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTING)) {
          try {
            logger.info(
                "KiraTimerTriggerScheduler is starting... KiraTimerTriggerScheduler's id=" + this
                    .getId());
            timerTriggerToBeFiredHandleService.start();

            lockForTimerManagement.writeLock().lock();
            try {
              ITimer timer = null;
              for (int i = 0; i < timerCount; i++) {
                timer = new KiraTimer(id, i, timerTriggerToBeFiredHandleService);
                timer.start();

                timers[i] = timer;
                timerIdTimerMap.put(timer.getId(), timer);
              }
            } finally {
              lockForTimerManagement.writeLock().unlock();
            }

            lockForTimerTriggerManagement.writeLock().lock();
            try {
              ITimer timer = null;
              for (int i = 0; i < timerCount; i++) {
                timer = timers[i];
                if (null != timer) {
                  Set<String> timerTriggerSet = new HashSet<String>();
                  timerTimerTriggerIdSetMap.putIfAbsent(timer, timerTriggerSet);
                }
              }
            } finally {
              lockForTimerTriggerManagement.writeLock().unlock();
            }
          } finally {
            schedulerState.set(KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED);
            logger.info("KiraTimerTriggerScheduler is started. KiraTimerTriggerScheduler=" + this
                .toDetailString());
          }
        } else {
          String errorMessage =
              "Failed to start KiraTimerTriggerScheduler. It may not be in correct state. KiraTimerTriggerScheduler="
                  + this.toDetailString();
          logger.error(errorMessage);
          throw new IllegalStateException(errorMessage);
        }
      } finally {
        lockForSchedulerState.writeLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when call start() for KiraTimerTriggerScheduler=" + this, e);
      throw e;
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info("It takes " + handleCostTime
          + " milliseconds to start KiraTimerTriggerScheduler. KiraTimerTriggerScheduler=" + this
          .toDetailString());
    }
  }

  @Override
  public void shutdown() {
    long startHandleTime = System.currentTimeMillis();
    try {
      lockForSchedulerState.writeLock().lock();
      try {
        if (schedulerState.compareAndSet(KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED,
            KiraTimerTriggerScheduler.SCHEDULER_STATE_SHUTTINGDOWN)) {
          try {
            logger.info(
                "KiraTimerTriggerScheduler is shutting down. KiraTimerTriggerScheduler's id=" + this
                    .getId());
            lockForTimerManagement.writeLock().lock();
            try {
              ITimer timer = null;
              for (int i = 0; i < timerCount; i++) {
                timer = timers[i];
                if (null != timer) {
                  timer.shutdown();
                  timerIdTimerMap.remove(timer.getId());
                  timers[i] = null;
                }
              }
            } finally {
              lockForTimerManagement.writeLock().unlock();
            }

            lockForTimerTriggerManagement.writeLock().lock();
            try {
              timerTriggerIdTimerMap.clear();
              timerTimerTriggerIdSetMap.clear();
              managedTimerTriggerIdTimerTriggerMap.clear();
            } finally {
              lockForTimerTriggerManagement.writeLock().unlock();
            }
          } finally {
            try {
              if (null != timerTriggerToBeFiredHandleService) {
                timerTriggerToBeFiredHandleService.shutdown();
              }
            } finally {
              schedulerState.set(KiraTimerTriggerScheduler.SCHEDULER_STATE_SHUTDOWN);
              logger.info(
                  "KiraTimerTriggerScheduler is shutdown down. KiraTimerTriggerScheduler=" + this
                      .toDetailString());
            }
          }
        } else {
          String errorMessage =
              "Failed to shutdown KiraTimerTriggerScheduler. It may not in started state. KiraTimerTriggerScheduler="
                  + this.toDetailString();
          logger.error(errorMessage);
        }
      } finally {
        lockForSchedulerState.writeLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when call shutdown() for KiraTimerTriggerScheduler=" + this, e);
    } finally {
      long handleCostTime = System.currentTimeMillis() - startHandleTime;
      logger.info("It takes " + handleCostTime
          + " milliseconds to shutdown KiraTimerTriggerScheduler. KiraTimerTriggerScheduler=" + this
          .toDetailString());
    }
  }

  @Override
  public boolean isStarted() {
    boolean returnValue = false;
    try {
      lockForSchedulerState.readLock().lock();
      try {
        returnValue = (KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED == this.schedulerState
            .get());
      } finally {
        lockForSchedulerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isStarted(). timerTriggerScheduler's id=" + this.getId()
          + " and schedulerState=" + this.schedulerState.get(), e);
    }

    return returnValue;
  }

  @Override
  public boolean isShutdown() {
    boolean returnValue = false;
    try {
      lockForSchedulerState.readLock().lock();
      try {
        returnValue = (KiraTimerTriggerScheduler.SCHEDULER_STATE_SHUTDOWN == this.schedulerState
            .get());
      } finally {
        lockForSchedulerState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isShutdown(). timerTriggerScheduler's id=" + this.getId()
          + " and schedulerState=" + this.schedulerState.get(), e);
    }

    return returnValue;
  }

  private ITimer selectTimerToAddTimerTrigger(ITimerTrigger timerTrigger) {
    ITimer returnValue = null;
    if (null != timerTrigger) {
      lockForTimerTriggerManagement.readLock().lock();
      try {
        ITimer timer = null;
        Set<String> timerTriggerIdSet = null;
        int minTimerTriggerCount = Integer.MAX_VALUE;
        Set<Entry<ITimer, Set<String>>> entrySet = timerTimerTriggerIdSetMap.entrySet();
        for (Entry<ITimer, Set<String>> entry : entrySet) {
          timer = entry.getKey();
          timerTriggerIdSet = entry.getValue();
          if (null != timerTriggerIdSet) {
            int timerTriggerCount = timerTriggerIdSet.size();
            if (timerTriggerCount < minTimerTriggerCount) {
              minTimerTriggerCount = timerTriggerCount;
              returnValue = timer;
            }
          }
        }
      } finally {
        lockForTimerTriggerManagement.readLock().unlock();
      }
    }
    return returnValue;
  }

  @Override
  public boolean rescheduleTimerTrigger(ITimerTrigger timerTrigger) throws Exception {
    boolean returnValue = false;
    try {
      if (null != timerTrigger) {
        boolean isUnscheduled = timerTrigger.isUnscheduled();
        if (isUnscheduled) {
          String errorMessage =
              "This trigger has already be unscheduled. So can not be rescheduled again. You should not call the setUnscheduled() and isUnscheduled() method which are used internally only. timerTrigger="
                  + KiraCommonUtils.toString(timerTrigger);
          logger.error(errorMessage);
          throw new KiraHandleException(errorMessage);
        } else {
          String timerTriggerId = timerTrigger.getId();
          if (StringUtils.isNotBlank(timerTriggerId)) {
            lockForSchedulerState.readLock().lock();
            try {
              boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded = isTimerTriggerSchedulerStartedAndWaitIfNeeded();
              if (isTimerTriggerSchedulerStartedAndWaitIfNeeded) {
                lockForTimerTriggerManagement.writeLock().lock();
                try {
                  //unschedule the exist one first
                  this.unscheduleTimerTrigger(timerTriggerId);

                  Date nextFireTime = timerTrigger.getNextFireTime();
                  if (null != nextFireTime) {
                    ITimer timer = this.selectTimerToAddTimerTrigger(timerTrigger);
                    if (null != timer) {
                      if (KiraTimerTriggerScheduler.SCHEDULER_STATE_STARTED == schedulerState
                          .get()) {
                        boolean addTimerTriggerToTimerSuccess = timer.addTimerTrigger(timerTrigger);
                        if (addTimerTriggerToTimerSuccess) {
                          timerTriggerIdTimerMap.put(timerTriggerId, timer);
                          Set<String> timerTriggerSet = timerTimerTriggerIdSetMap.get(timer);
                          timerTriggerSet.add(timerTriggerId);

                          managedTimerTriggerIdTimerTriggerMap.put(timerTriggerId, timerTrigger);
                          returnValue = true;
                          logger
                              .info("scheduleTimerTrigger success for timerTrigger={} in timer={}",
                                  timerTrigger.getId(), timer.getId());
                        } else {
                          String errorMessage =
                              "addTimerTrigger failed for timerTrigger=" + timerTrigger
                                  + " in timer=" + timer.getId();
                          logger.error(errorMessage);
                          throw new KiraHandleException(errorMessage);
                        }
                      } else {
                        String errorMessage =
                            "Can not scheduleTimerTrigger for KiraTimerTriggerScheduler is not started when check again. timerTrigger="
                                + timerTrigger + " in KiraTimerTriggerScheduler's id=" + this
                                .getId() + " and schedulerState=" + this.schedulerState.get();
                        logger.error(errorMessage);
                        throw new KiraHandleException(errorMessage);
                      }
                    } else {
                      String errorMessage =
                          "Failed to selectTimerToAddTimerTrigger for timerTrigger=" + timerTrigger
                              + " in KiraTimerTriggerScheduler's id=" + this.getId()
                              + " and schedulerState=" + this.schedulerState.get();
                      logger.error(errorMessage);
                      throw new KiraHandleException(errorMessage);
                    }
                  } else {
                    managedTimerTriggerIdTimerTriggerMap.put(timerTriggerId, timerTrigger);
                    returnValue = true;
                    logger.info(
                        "nextFireTime is null. So do not add it to any timer. But always need to manage it even if nextFireTime is null. timerTrigger={}",
                        timerTrigger);
                  }
                } finally {
                  lockForTimerTriggerManagement.writeLock().unlock();
                }
              } else {
                String errorMessage =
                    "isTimerTriggerSchedulerStartedAndWaitIfNeeded return false. So just return false for rescheduleTimerTrigger. KiraTimerTriggerScheduler="
                        + this + " and timerTrigger=" + timerTrigger;
                logger.error(errorMessage);
                throw new KiraHandleException(errorMessage);
              }
            } finally {
              lockForSchedulerState.readLock().unlock();
            }
          } else {
            String errorMessage =
                "timerTriggerId should not be null when rescheduleTimerTrigger. timerTrigger="
                    + timerTrigger;
            logger.error(errorMessage);
            throw new KiraHandleException(errorMessage);
          }
        }
      } else {
        String errorMessage =
            "timerTrigger should not be null when rescheduleTimerTrigger. timerTrigger="
                + timerTrigger;
        logger.error(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } catch (KiraHandleException e) {
      throw e;
    } catch (Exception e) {
      logger.error(
          "Error occurs for rescheduleTimerTrigger. kiraTimerScheduler's id=" + this.getId()
              + "and schedulerState=" + this.schedulerState.get() + " and timerTrigger="
              + timerTrigger, e);
      throw e;
    }

    return returnValue;
  }

  @Override
  public ITimerTrigger unscheduleTimerTrigger(String timerTriggerId) throws Exception {
    ITimerTrigger returnValue = null;
    if (StringUtils.isNotBlank(timerTriggerId)) {
      try {
        lockForSchedulerState.readLock().lock();
        try {
          boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded = isTimerTriggerSchedulerStartedAndWaitIfNeeded();
          if (isTimerTriggerSchedulerStartedAndWaitIfNeeded) {
            lockForTimerTriggerManagement.writeLock().lock();
            try {
              ITimerTrigger existTimerTrigger = managedTimerTriggerIdTimerTriggerMap
                  .get(timerTriggerId);
              if (null != existTimerTrigger) {
                ITimer timer = timerTriggerIdTimerMap.get(timerTriggerId);
                if (null != timer) {
                  boolean removeTimerTriggerResult = timer
                      .removeTimerTrigger(existTimerTrigger, true);
                  logger.info(
                      "removeTimerTrigger finished for timerTrigger={} in timer={} and removeTimerTriggerResult={}",
                      timerTriggerId, timer.getId(), removeTimerTriggerResult);

                  //always need to remove the relationship between timer and trigger even if removeSuccess is false
                  Set<String> timerTriggerIdSet = timerTimerTriggerIdSetMap.get(timer);
                  if (null != timerTriggerIdSet) {
                    timerTriggerIdSet.remove(timerTriggerId);
                  }
                }

                //always need to remove the relationship between timer and trigger
                timerTriggerIdTimerMap.remove(timerTriggerId);
              }

              //always need to remove the managed trigger
              returnValue = managedTimerTriggerIdTimerTriggerMap.remove(timerTriggerId);
            } finally {
              lockForTimerTriggerManagement.writeLock().unlock();
              logger.info("unscheduleTimerTrigger done for timerTrigger={} and returnValue={}",
                  timerTriggerId, returnValue);
            }
          } else {
            String errorMessage =
                "isTimerTriggerSchedulerStartedAndWaitIfNeeded return false. So just return null for unscheduleTimerTrigger. KiraTimerTriggerScheduler's id="
                    + this.getId() + " and schedulerState=" + this.schedulerState.get()
                    + " and timerTriggerId=" + timerTriggerId;
            logger.error(errorMessage);
            throw new KiraHandleException(errorMessage);
          }
        } finally {
          lockForSchedulerState.readLock().unlock();
        }
      } catch (KiraHandleException e) {
        throw e;
      } catch (Exception e) {
        logger.error(
            "Error occurs for unscheduleTimerTrigger. kiraTimerScheduler's id=" + this.getId()
                + "and schedulerState=" + this.schedulerState.get() + " and timerTriggerId="
                + timerTriggerId, e);
        throw e;
      }
    } else {
      String errorMessage = "timerTriggerId should not be null when unscheduleTimerTrigger.";
      logger.error(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    return returnValue;
  }

  @Override
  public ITimerTrigger getTimerTrigger(String timerTriggerId, boolean returnClonedObject)
      throws Exception {
    ITimerTrigger returnValue = null;
    if (StringUtils.isNotBlank(timerTriggerId)) {
      try {
        lockForSchedulerState.readLock().lock();
        try {
          boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded = isTimerTriggerSchedulerStartedAndWaitIfNeeded();
          if (isTimerTriggerSchedulerStartedAndWaitIfNeeded) {
            lockForTimerTriggerManagement.readLock().lock();
            try {
              ITimerTrigger existTimerTrigger = managedTimerTriggerIdTimerTriggerMap
                  .get(timerTriggerId);
              if (null != existTimerTrigger) {
                if (returnClonedObject) {
                  returnValue = (ITimerTrigger) existTimerTrigger.clone();
                } else {
                  returnValue = existTimerTrigger;
                }
              }
            } finally {
              lockForTimerTriggerManagement.readLock().unlock();
            }
          } else {
            String errorMessage =
                "isTimerTriggerSchedulerStartedAndWaitIfNeeded return false. So just return null for getTimerTrigger. KiraTimerTriggerScheduler="
                    + this + " and timerTriggerId=" + timerTriggerId;
            logger.error(errorMessage);
            throw new KiraHandleException(errorMessage);
          }
        } finally {
          lockForSchedulerState.readLock().unlock();
        }
      } catch (KiraHandleException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error occurs for getTimerTrigger. kiraTimerScheduler's id=" + this.getId()
            + "and schedulerState=" + this.schedulerState.get() + " and timerTriggerId="
            + timerTriggerId, e);
        throw e;
      }
    }

    return returnValue;
  }

  @Override
  public int getManagedTimerTriggerCount() throws Exception {
    int returnValue = 0;
    try {
      lockForSchedulerState.readLock().lock();
      try {
        if (this.isShutdown()) {
          logger.info(
              "KiraTimerTriggerScheduler is shutdown. So just return 0 for getManagedIimerTriggerCount. KiraTimerTriggerScheduler="
                  + this);
        } else {
          boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded = isTimerTriggerSchedulerStartedAndWaitIfNeeded();
          if (isTimerTriggerSchedulerStartedAndWaitIfNeeded) {
            lockForTimerTriggerManagement.readLock().lock();
            try {
              returnValue = managedTimerTriggerIdTimerTriggerMap.size();
            } finally {
              lockForTimerTriggerManagement.readLock().unlock();
            }
          } else {
            String errorMessage =
                "isTimerTriggerSchedulerStartedAndWaitIfNeeded return false for getManagedIimerTriggerCount. KiraTimerTriggerScheduler="
                    + this;
            logger.error(errorMessage);
            throw new KiraHandleException(errorMessage);
          }
        }
      } finally {
        lockForSchedulerState.readLock().unlock();
      }

    } catch (KiraHandleException e) {
      throw e;
    } catch (Exception e) {
      logger.error(
          "Error occurs for getManagedIimerTriggerCount. kiraTimerScheduler's id=" + this.getId()
              + "and schedulerState=" + this.schedulerState.get(), e);
      throw e;
    }
    return returnValue;
  }

  @Override
  public List<TriggerIdentity> getManagedTriggerIdentityList()
      throws Exception {
    List<TriggerIdentity> returnValue = new ArrayList<TriggerIdentity>();
    try {
      lockForSchedulerState.readLock().lock();
      try {
        boolean isTimerTriggerSchedulerStartedAndWaitIfNeeded = isTimerTriggerSchedulerStartedAndWaitIfNeeded();
        if (isTimerTriggerSchedulerStartedAndWaitIfNeeded) {
          lockForTimerTriggerManagement.readLock().lock();
          try {
            if (null != managedTimerTriggerIdTimerTriggerMap) {
              TriggerIdentity triggerIdentity = null;
              for (ITimerTrigger timerTrigger : managedTimerTriggerIdTimerTriggerMap.values()) {
                triggerIdentity = new TriggerIdentity(timerTrigger.getGroup(),
                    timerTrigger.getName());
                returnValue.add(triggerIdentity);
              }
            }
          } finally {
            lockForTimerTriggerManagement.readLock().unlock();
          }
        } else {
          String errorMessage =
              "isTimerTriggerSchedulerStartedAndWaitIfNeeded return false. So just throw exception for getManagedTriggerIdentityList. KiraTimerTriggerScheduler="
                  + this;
          logger.error(errorMessage);
          throw new KiraHandleException(errorMessage);
        }
      } finally {
        lockForSchedulerState.readLock().unlock();
      }
    } catch (KiraHandleException e) {
      throw e;
    } catch (Exception e) {
      logger.error(
          "Error occurs for getManagedTriggerIdentityList. kiraTimerScheduler's id=" + this.getId()
              + "and schedulerState=" + this.schedulerState.get(), e);
      throw e;
    }

    return returnValue;
  }

  @Override
  public Collection<ITimerTrigger> getManagedTimerTriggers(boolean accurate) throws Exception {
    Collection<ITimerTrigger> returnValue = null;
    if (accurate) {
      lockForSchedulerState.readLock().lock();
      try {
        lockForTimerTriggerManagement.readLock().lock();
        try {
          returnValue = managedTimerTriggerIdTimerTriggerMap.values();
        } finally {
          lockForTimerTriggerManagement.readLock().unlock();
        }
      } finally {
        lockForSchedulerState.readLock().unlock();
      }
    } else {
      returnValue = managedTimerTriggerIdTimerTriggerMap.values();
    }

    return returnValue;
  }

  public String toDetailString() {
    return "KiraTimerTriggerScheduler [maxConcurrentTimerTriggerCount="
        + maxConcurrentTimerTriggerCount
        + ", timerTriggerToBeFiredHandleService="
        + timerTriggerToBeFiredHandleService + ", id=" + id
        + ", timers=" + Arrays.toString(timers) + ", timerCount="
        + timerCount + ", timerIdTimerMap=" + timerIdTimerMap
        + ", lockForTimerManagement=" + lockForTimerManagement
        + ", timerTriggerIdTimerMap=" + timerTriggerIdTimerMap
        + ", timerTimerTriggerIdSetMap=" + timerTimerTriggerIdSetMap
        + ", managedTimerTriggerIdTimerTriggerMap="
        + managedTimerTriggerIdTimerTriggerMap
        + ", lockForTimerTriggerManagement="
        + lockForTimerTriggerManagement + ", schedulerState="
        + schedulerState + ", lockForSchedulerStateManagement="
        + lockForSchedulerState + "]";
  }

  @Override
  public String toString() {
    return "KiraTimerTriggerScheduler [ id=" + id
        + ", maxConcurrentTimerTriggerCount="
        + maxConcurrentTimerTriggerCount
        + ", timerTriggerToBeFiredHandleService="
        + timerTriggerToBeFiredHandleService
        + ", timers=" + Arrays.toString(timers) + ", timerCount="
        + timerCount + ", timerIdTimerMap=" + timerIdTimerMap
        + ", lockForTimerManagement=" + lockForTimerManagement
        + ", timerTriggerIdTimerMap=" + timerTriggerIdTimerMap
        + ", timerTimerTriggerIdSetMap=" + timerTimerTriggerIdSetMap
        + ", managedTimerTriggerIdTimerTriggerMap="
        + managedTimerTriggerIdTimerTriggerMap
        + ", lockForTimerTriggerManagement="
        + lockForTimerTriggerManagement + ", schedulerState="
        + schedulerState + ", lockForSchedulerStateManagement="
        + lockForSchedulerState + "]";
  }
}
