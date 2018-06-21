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
package com.yihaodian.architecture.kira.manager.health.monitor.task;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.scheduler.IKiraTimerTriggerLocalScheduler;
import com.yihaodian.architecture.kira.manager.health.event.CreateAndRunJobForTimerTriggerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.event.NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class CreateAndRunJobForTimerTriggerMonitorTask extends
    KiraManagerMonitorTaskComponent implements Runnable {

  private static final long ALWAYS_CHECK_BENCHMARK_WHEN_NEXTFIRETIME_INTERVAL_TOO_LONG_THRESHOLD_IN_MILLISECONDS = 300000L; //5 minutes
  //private static final long ALWAYS_CHECK_BENCHMARK_WHEN_NEXTFIRETIME_INTERVAL_TOO_LONG_THRESHOLD_IN_MILLISECONDS = 1L; //1 milliseconds
  private static final long TOTAL_TIMEOUT_FOR_CREATEANDRUNJOB_IN_MILLISECONDS = 300000L; //5 minutes
  public static volatile Date lastSuccessfullyCreateAndRunJobTime = null;
  //private static final long TOTAL_TIMEOUT_FOR_CREATEANDRUNJOB_IN_MILLISECONDS = 20000L; //20 seconds
  private String idOfBenchmarkTimerTrigger = null;
  private String identityInTrackingSystemOfBenchmarkTimerTrigger = null;
  private Date nextFireTimeOfBenchmarkTimerTrigger = null;
  private Date lastChooseBenchmarkTime = null;

  public CreateAndRunJobForTimerTriggerMonitorTask() {
    this.monitorContext = new MonitorContext("Create and run job for timerTrigger", "");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  public synchronized void run() {
    try {
      boolean isNoNeedToCheckHealthy = this.isNoNeedToCheckHealthy();
      if (!isNoNeedToCheckHealthy) {
        boolean isNeedToChooseBenchmarkAgain = this.isNeedToChooseBenchmarkAgain();
        if (!isNeedToChooseBenchmarkAgain) {
          Date clonedLastSuccessfullyCreateAndRunJobTime = (Date) (
              (null == lastSuccessfullyCreateAndRunJobTime) ? null
                  : lastSuccessfullyCreateAndRunJobTime.clone());
          this.checkHealthy(clonedLastSuccessfullyCreateAndRunJobTime);
        } else {
          //choose the benchmark again and make next time to check healthy
          this.chooseNewBenchmark();
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when run for CreateAndRunJobForTimerTriggerMonitorTask", e);
    }
  }

  private void checkHealthy(Date clonedLastSuccessfullyCreateAndRunJobTime) throws Exception {
    //It is time to check health now
    boolean isHealthy = this.isHealthy(clonedLastSuccessfullyCreateAndRunJobTime);
    if (isHealthy) {
      String monitorDetails = "lastSuccessfullyCreateAndRunJobTime=" + KiraCommonUtils
          .getDateAsStringToMsPrecision(clonedLastSuccessfullyCreateAndRunJobTime);
      MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
          .updateAndGetMonitorNoticeInfoIfNeeded(!isHealthy, monitorDetails);
      if (null != monitorNoticeInfo) {
        CreateAndRunJobForTimerTriggerRecoveredEvent createAndRunJobForTimerTriggerRecoveredEvent = new CreateAndRunJobForTimerTriggerRecoveredEvent(
            KiraManagerHealthEventType.CREATEANDRUNJOB_FORTIMERTRIGGER_RECOVERED,
            SystemUtil.getLocalhostIp(), monitorNoticeInfo);
        KiraManagerHealthUtils
            .dispatchKiraManagerHealthEvent(createAndRunJobForTimerTriggerRecoveredEvent);
      }
      //choose the benchmark again and make next time to check healthy
      this.chooseNewBenchmark();
    } else {
      //If it is not healthy now, need to use the benchmark's nextFireTime and timeout to decide the healthy
      if (null != this.nextFireTimeOfBenchmarkTimerTrigger) {
        long futureThresholdTime = this.nextFireTimeOfBenchmarkTimerTrigger.getTime()
            + TOTAL_TIMEOUT_FOR_CREATEANDRUNJOB_IN_MILLISECONDS;
        long now = System.currentTimeMillis();
        if (futureThresholdTime < now) {
          //If arrive here, it is unhealthy.
          String monitorDetails =
              "KiraManager failed to create and run job for timerTrigger in recent "
                  + TOTAL_TIMEOUT_FOR_CREATEANDRUNJOB_IN_MILLISECONDS
                  + " milliseconds. And lastSuccessfullyCreateAndRunJobTime=" + KiraCommonUtils
                  .getDateAsStringToMsPrecision(clonedLastSuccessfullyCreateAndRunJobTime);
          MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
              .updateAndGetMonitorNoticeInfoIfNeeded(!isHealthy, monitorDetails);
          if (null != monitorNoticeInfo) {
            NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent = new NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent(
                KiraManagerHealthEventType.NO_JOBCREATEDANDRUN_FORSOMETIME_FORTIMERTRIGGER,
                SystemUtil.getLocalhostIp(), monitorNoticeInfo);
            KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(
                noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent);
          }
          //choose the benchmark again and make next time to check healthy
          this.chooseNewBenchmark();
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "It is not the time to decide if it is unhealthy of CreateAndRunJobForTimerTrigger now.");
          }
        }
      } else {
        logger.warn(
            "nextFireTimeOfBenchmarkTimerTrigger should not be null. It may have some bugs. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={}",
            this.idOfBenchmarkTimerTrigger, this.identityInTrackingSystemOfBenchmarkTimerTrigger);
      }
    }
  }

  private void chooseNewBenchmark() throws Exception {
    //clean old benchmark first
    this.idOfBenchmarkTimerTrigger = null;
    this.identityInTrackingSystemOfBenchmarkTimerTrigger = null;
    this.nextFireTimeOfBenchmarkTimerTrigger = null;

    this.lastChooseBenchmarkTime = new Date();
    TreeMap<Long, List<ITimerTrigger>> nextFireTimeTimerTriggerListMap = getNextFireTimeTimerTriggerListMap();
    if (null != nextFireTimeTimerTriggerListMap && nextFireTimeTimerTriggerListMap.size() > 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("nextFireTimeTimerTriggerListMap={}",
            this.getNextFireTimeTimerTriggerListMapAsString(nextFireTimeTimerTriggerListMap));
      }
      Map.Entry<Long, List<ITimerTrigger>> firstEntry = nextFireTimeTimerTriggerListMap
          .firstEntry();
      Long nextFireTimeAsLong = firstEntry.getKey();
      List<ITimerTrigger> timerTriggerList = firstEntry.getValue();
      if (CollectionUtils.isNotEmpty(timerTriggerList)) {
        ITimerTrigger oneTimerTrigger = timerTriggerList.get(0);
        this.idOfBenchmarkTimerTrigger = oneTimerTrigger.getId();
        this.identityInTrackingSystemOfBenchmarkTimerTrigger = oneTimerTrigger
            .getIdentityInTrackingSystem();
        this.nextFireTimeOfBenchmarkTimerTrigger = new Date(nextFireTimeAsLong.longValue());

        if (logger.isDebugEnabled()) {
          logger.debug(
              "New benchmark chosen. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and nextFireTimeOfBenchmarkTimerTrigger={}",
              this.idOfBenchmarkTimerTrigger, this.identityInTrackingSystemOfBenchmarkTimerTrigger,
              KiraCommonUtils
                  .getDateAsStringToMsPrecision(this.nextFireTimeOfBenchmarkTimerTrigger));
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("No benchmark selected for timerTriggerList is empty. nextFireTimeAsLong={}",
              nextFireTimeAsLong);
        }
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("No benchmark selected for nextFireTimeTimerTriggerListMap is empty.");
      }
    }
  }

  private String getNextFireTimeTimerTriggerListMapAsString(
      TreeMap<Long, List<ITimerTrigger>> nextFireTimeTimerTriggerListMap) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    if (null != nextFireTimeTimerTriggerListMap) {
      sb.append("(" + nextFireTimeTimerTriggerListMap.size() + ") ");
      for (Map.Entry<Long, List<ITimerTrigger>> entryOfNextFireTimeTimerTriggerListMap : nextFireTimeTimerTriggerListMap
          .entrySet()) {
        Long nextFireTimeAsLong = entryOfNextFireTimeTimerTriggerListMap.getKey();
        String nextFireTimeToDateString = null;
        if (null != nextFireTimeAsLong) {
          nextFireTimeToDateString = KiraCommonUtils
              .getDateAsStringToMsPrecision(new Date(nextFireTimeAsLong.longValue()));
        }
        sb.append(nextFireTimeToDateString);

        sb.append(":");

        List<ITimerTrigger> timerTriggerList = entryOfNextFireTimeTimerTriggerListMap.getValue();
        sb.append("[");
        if (null != timerTriggerList) {
          int length = timerTriggerList.size();
          sb.append("(" + length + ") ");
          for (int i = 0; i < length; i++) {
            String id = timerTriggerList.get(i).getId();
            sb.append(id);
            if (i < length - 1) {
              sb.append(",");
            }
          }
        }
        sb.append("]");
        sb.append(System.getProperty("line.separator"));
        sb.append(",");
      }
    }
    sb.append("}");

    String returnValue = sb.toString();
    return returnValue;
  }

  private boolean isNoNeedToCheckHealthy() throws Exception {
    boolean returnValue = false;
    boolean isKiraTimerTriggerLocalSchedulerStarted = this
        .isKiraTimerTriggerLocalSchedulerStarted();
    if (!isKiraTimerTriggerLocalSchedulerStarted) {
      this.resetAllData(true);
      returnValue = true;
      if (logger.isDebugEnabled()) {
        logger.debug(
            "isKiraTimerTriggerLocalSchedulerStarted is false. So no need to check healty now.");
      }
    } else {
      int managedTimerTriggerCount = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
          .getManagedTimerTriggerCount();
      if (managedTimerTriggerCount <= 0) {
        this.resetAllData(false);
        returnValue = true;
        if (logger.isDebugEnabled()) {
          logger.debug("managedTimerTriggerCount <=0. So no need to check healty now.");
        }
      }
    }
    return returnValue;
  }

  private boolean isHealthy(Date clonedLastSuccessfullyCreateAndRunJobTime) {
    boolean returnValue = false;
    if (null != clonedLastSuccessfullyCreateAndRunJobTime) {
      if (null != this.lastChooseBenchmarkTime) {
        if (clonedLastSuccessfullyCreateAndRunJobTime.getTime() >= this.lastChooseBenchmarkTime
            .getTime()) {
          returnValue = true;
        }
      } else {
        logger.warn(
            "lastChooseBenchmarkTime should not be blank. May have some bugs. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and nextFireTimeOfBenchmarkTimerTrigger={} and clonedLastSuccessfullyCreateAndRunJobTime={}",
            idOfBenchmarkTimerTrigger, identityInTrackingSystemOfBenchmarkTimerTrigger,
            KiraCommonUtils.getDateAsStringToMsPrecision(nextFireTimeOfBenchmarkTimerTrigger),
            KiraCommonUtils
                .getDateAsStringToMsPrecision(clonedLastSuccessfullyCreateAndRunJobTime));
      }
    }

    return returnValue;
  }

  private boolean isNeedToChooseBenchmarkAgain() throws Exception {
    boolean returnValue = false;

    if (null == this.idOfBenchmarkTimerTrigger) {
      returnValue = true;
    } else {
      ITimerTrigger timerTrigger = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler()
          .getTimerTrigger(this.idOfBenchmarkTimerTrigger, false);
      if (null == timerTrigger) {
        returnValue = true;
        logger.info(
            "BenchmarkTimerTrigger may be unscheduled or be moved to other kiraServer. So need to choose benchmark again. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={}",
            this.idOfBenchmarkTimerTrigger, this.identityInTrackingSystemOfBenchmarkTimerTrigger);
      } else {
        String currentIdentityInTrackingSystem = timerTrigger.getIdentityInTrackingSystem();
        if (!StringUtils.equals(currentIdentityInTrackingSystem,
            this.identityInTrackingSystemOfBenchmarkTimerTrigger)) {
          returnValue = true;
          logger.info(
              "BenchmarkTimerTrigger may be rescheduled. So need to choose benchmark again. idOfBenchmarkTimerTrigger={} and currentIdentityInTrackingSystem={} and identityInTrackingSystemOfBenchmarkTimerTrigger={}",
              this.idOfBenchmarkTimerTrigger, currentIdentityInTrackingSystem,
              this.identityInTrackingSystemOfBenchmarkTimerTrigger);
        } else {
          //If the next fire time is at the long future. Always need to check if the new benchmark exist.
          //ALWAYS_CHECK_BENCHMARK_WHEN_NEXTFIRETIME_INTERVAL_TOO_LONG_THRESHOLD_IN_MILLISECONDS
          if (null != this.nextFireTimeOfBenchmarkTimerTrigger) {
            if (this.nextFireTimeOfBenchmarkTimerTrigger.getTime() > (System.currentTimeMillis()
                + ALWAYS_CHECK_BENCHMARK_WHEN_NEXTFIRETIME_INTERVAL_TOO_LONG_THRESHOLD_IN_MILLISECONDS)) {
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "Will check to see if the new benchmark exist. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and nextFireTimeOfBenchmarkTimerTrigger={} and lastChooseBenchmarkTime={}",
                    this.idOfBenchmarkTimerTrigger,
                    this.identityInTrackingSystemOfBenchmarkTimerTrigger, KiraCommonUtils
                        .getDateAsStringToMsPrecision(this.nextFireTimeOfBenchmarkTimerTrigger),
                    KiraCommonUtils.getDateAsStringToMsPrecision(this.lastChooseBenchmarkTime));
              }
              //check to see if the old benchmark valid
              boolean isBenchmarkStillValid = this.isBenchmarkStillValid();
              if (!isBenchmarkStillValid) {
                returnValue = true;
                logger.info(
                    "Old benchmark is not valid now and need to choose benchmark again. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and nextFireTimeOfBenchmarkTimerTrigger={} and lastChooseBenchmarkTime={}",
                    this.idOfBenchmarkTimerTrigger, identityInTrackingSystemOfBenchmarkTimerTrigger,
                    KiraCommonUtils
                        .getDateAsStringToMsPrecision(this.nextFireTimeOfBenchmarkTimerTrigger),
                    KiraCommonUtils.getDateAsStringToMsPrecision(this.lastChooseBenchmarkTime));
              }
            }
          } else {
            logger.warn(
                "nextFireTimeOfBenchmarkTimerTrigger should not be null. May have some bugs. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and lastChooseBenchmarkTime={}",
                this.idOfBenchmarkTimerTrigger,
                this.identityInTrackingSystemOfBenchmarkTimerTrigger,
                KiraCommonUtils.getDateAsStringToMsPrecision(this.lastChooseBenchmarkTime));
          }
        }
      }

    }

    return returnValue;
  }

  private boolean isBenchmarkStillValid() throws Exception {
    boolean returnValue = false;

    TreeMap<Long, List<ITimerTrigger>> nextFireTimeTimerTriggerListMap = getNextFireTimeTimerTriggerListMap();
    if (null != nextFireTimeTimerTriggerListMap && nextFireTimeTimerTriggerListMap.size() > 0) {
      Map.Entry<Long, List<ITimerTrigger>> firstEntry = nextFireTimeTimerTriggerListMap
          .firstEntry();
      if (null != firstEntry) {
        List<ITimerTrigger> timerTriggerList = firstEntry.getValue();
        if (CollectionUtils.isNotEmpty(timerTriggerList)) {
          for (ITimerTrigger oneTimerTrigger : timerTriggerList) {
            if (StringUtils.equals(oneTimerTrigger.getId(), this.idOfBenchmarkTimerTrigger)
                && StringUtils.equals(oneTimerTrigger.getIdentityInTrackingSystem(),
                this.identityInTrackingSystemOfBenchmarkTimerTrigger)) {
              returnValue = true;
              if (logger.isDebugEnabled()) {
                logger.debug(
                    "Old benchmark is still valid. idOfBenchmarkTimerTrigger={} and identityInTrackingSystemOfBenchmarkTimerTrigger={} and nextFireTimeOfBenchmarkTimerTrigger={} and lastChooseBenchmarkTime={}",
                    this.idOfBenchmarkTimerTrigger,
                    this.identityInTrackingSystemOfBenchmarkTimerTrigger, KiraCommonUtils
                        .getDateAsStringToMsPrecision(this.nextFireTimeOfBenchmarkTimerTrigger),
                    KiraCommonUtils.getDateAsStringToMsPrecision(this.lastChooseBenchmarkTime));
              }
              break;
            }
          }
        }
      }
    }

    return returnValue;
  }

  public synchronized void resetAllData(boolean resetMonitorContext) {
    if (resetMonitorContext) {
      this.monitorContext.resetStatisticsData();
    }
    this.idOfBenchmarkTimerTrigger = null;
    this.identityInTrackingSystemOfBenchmarkTimerTrigger = null;
    this.nextFireTimeOfBenchmarkTimerTrigger = null;
    this.lastChooseBenchmarkTime = null;

    if (logger.isDebugEnabled()) {
      logger.debug("All data have been reset.");
    }
  }

  private boolean isKiraTimerTriggerLocalSchedulerStarted() {
    boolean returnValue = false;
    IKiraTimerTriggerLocalScheduler kiraTimerTriggerLocalScheduler = null;
    try {
      kiraTimerTriggerLocalScheduler = KiraManagerDataCenter.getKiraTimerTriggerLocalScheduler();
    } catch (Exception e) {
      logger.error("Can not get kiraTimerTriggerLocalScheduler. So regard it as not started.");
    }

    if (null != kiraTimerTriggerLocalScheduler) {
      if (kiraTimerTriggerLocalScheduler.isStarted()) {
        returnValue = true;
      }
    }

    return returnValue;
  }

  private TreeMap<Long, List<ITimerTrigger>> getNextFireTimeTimerTriggerListMap() throws Exception {
    TreeMap<Long, List<ITimerTrigger>> returnValue = new TreeMap<Long, List<ITimerTrigger>>();

    Collection<ITimerTrigger> managedTimerTriggers = KiraManagerDataCenter
        .getKiraTimerTriggerLocalScheduler().getManagedTimerTriggers(false);
    if (null != managedTimerTriggers) {
      Date nextFireTime = null;
      Long nextFireTimeAsLong = null;
      List<ITimerTrigger> timerTriggerList = null;
      for (ITimerTrigger oneTimerTrigger : managedTimerTriggers) {
        nextFireTime = oneTimerTrigger.getNextFireTime();
        if (null != nextFireTime) {
          nextFireTimeAsLong = Long.valueOf(nextFireTime.getTime());
          timerTriggerList = returnValue.get(nextFireTimeAsLong);
          if (null == timerTriggerList) {
            timerTriggerList = new ArrayList<ITimerTrigger>();
          }
          timerTriggerList.add(oneTimerTrigger);
          returnValue.put(nextFireTimeAsLong, timerTriggerList);
        }
      }
    }

    return returnValue;
  }

}
