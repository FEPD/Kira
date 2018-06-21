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
import com.yihaodian.architecture.kira.manager.health.event.ExternalOverallMonitorForTimerTriggerFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.ExternalOverallMonitorForTimerTriggerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import java.util.Date;

public class ExternalOverallMonitorForTimerTriggerTask extends
    KiraManagerMonitorTaskComponent implements Runnable {

  private static final long TOTAL_TIMEOUT_IN_MILLISECONDS = 300000L; //5 minutes
  public static volatile Date lastSuccessfullyTriggeredTime = null;

  public ExternalOverallMonitorForTimerTriggerTask() {
    this.monitorContext = new MonitorContext("External overall monitor for timerTrigger", "");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  public void run() {
    if (null != lastSuccessfullyTriggeredTime) {
      String monitorDetails = null;

      boolean isHealthy = this.isHealthy();
      if (!isHealthy) {
        monitorDetails =
            "kiraManagerClusterTimerTriggerScheduleHealthCheck-Trigger was not triggered in "
                + TOTAL_TIMEOUT_IN_MILLISECONDS
                + " milliseconds. May have problem with kira manager cluster or the connection with kira manager cluster was lost. lastSuccessfullyTriggeredTime="
                + KiraCommonUtils.getDateAsStringToMsPrecision(lastSuccessfullyTriggeredTime);
      }

      MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
          .updateAndGetMonitorNoticeInfoIfNeeded(!isHealthy, monitorDetails);
      if (null != monitorNoticeInfo) {
        if (monitorNoticeInfo.isBad()) {
          ExternalOverallMonitorForTimerTriggerFailedEvent externalOverallMonitorForTimerTriggerFailedEvent = new ExternalOverallMonitorForTimerTriggerFailedEvent(
              KiraManagerHealthEventType.EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_FAILED,
              SystemUtil.getLocalhostIp(), monitorNoticeInfo);
          KiraManagerHealthUtils
              .dispatchKiraManagerHealthEvent(externalOverallMonitorForTimerTriggerFailedEvent);
        } else {
          ExternalOverallMonitorForTimerTriggerRecoveredEvent externalOverallMonitorForTimerTriggerRecoveredEvent = new ExternalOverallMonitorForTimerTriggerRecoveredEvent(
              KiraManagerHealthEventType.EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_RECOVERED,
              SystemUtil.getLocalhostIp(), monitorNoticeInfo);
          KiraManagerHealthUtils
              .dispatchKiraManagerHealthEvent(externalOverallMonitorForTimerTriggerRecoveredEvent);
        }
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "lastSuccessfullyTriggeredTime is null which means never successfully triggered after boot. So no need to check health.");
      }
    }
  }

  private boolean isHealthy() {
    boolean returnValue = false;

    boolean isExpired = KiraCommonUtils
        .isExpired(lastSuccessfullyTriggeredTime, TOTAL_TIMEOUT_IN_MILLISECONDS);
    if (!isExpired) {
      returnValue = true;
    }

    return returnValue;
  }

}
