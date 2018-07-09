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

import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.event.ZKForKiraRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.ZKForKiraUnavailableEvent;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ZKForKiraMonitorTask extends KiraManagerMonitorTaskComponent implements Runnable {

  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public ZKForKiraMonitorTask() {
    this.monitorContext = new MonitorContext("Zookeeper Cluster for Kira", "");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  public void run() {
    boolean result = true;
    String monitorDetails = null;
    try {
      long timeOutInSeconds = 20L;
      result = zkClient.waitUntilConnected(timeOutInSeconds, TimeUnit.SECONDS);
      if (!result) {
        monitorDetails = "ZK can not be connected in " + timeOutInSeconds + " seconds.";
      }
    } catch (Throwable t) {
      result = false;
      monitorDetails = "exceptionDetails=" + ExceptionUtils.getFullStackTrace(t);
      //monitorDetails = t.getMessage();
      logger.error("Error occurs when runing ZKForKiraMonitorTask.", t);
    } finally {
      MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
          .updateAndGetMonitorNoticeInfoIfNeeded(!result, monitorDetails);
      if (null != monitorNoticeInfo) {
        if (monitorNoticeInfo.isBad()) {
          ZKForKiraUnavailableEvent zkForKiraUnavailableEvent = new ZKForKiraUnavailableEvent(
              KiraManagerHealthEventType.ZK_FOR_KIRA_UNAVAILABLE, SystemUtil.getLocalhostIp(),
              monitorNoticeInfo);
          KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(zkForKiraUnavailableEvent);
        } else {
          ZKForKiraRecoveredEvent zkForKiraRecoveredEvent = new ZKForKiraRecoveredEvent(
              KiraManagerHealthEventType.ZK_FOR_KIRA_RECOVERED, SystemUtil.getLocalhostIp(),
              monitorNoticeInfo);
          KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(zkForKiraRecoveredEvent);
        }
      }
    }
  }

}
