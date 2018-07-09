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
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerUtils;
import com.yihaodian.architecture.kira.manager.health.event.ClusterInternalConnectionFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.ClusterInternalConnectionRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ClusterInternalConnectionMonitorTask extends KiraManagerMonitorTaskComponent implements
    Runnable {

  public ClusterInternalConnectionMonitorTask() {
    this.monitorContext = new MonitorContext("Cluster internal connection", "");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void run() {
    IKiraServer kiraServer = null;
    try {
      kiraServer = KiraManagerDataCenter.getKiraServer();
    } catch (Exception e) {
      logger.error("Error occurs when KiraManagerDataCenter.getKiraServer()", e);
    }
    if (null != kiraServer) {
      KiraServerEntity myKiraServerEntity = kiraServer.getKiraServerEntity();
      LinkedHashSet<KiraServerEntity> kiraServerEntitysInCluster = null;
      try {
        kiraServerEntitysInCluster = kiraServer.getAllKiraServerEntitysInCluster();
      } catch (Exception e) {
        logger.error("Error occurs when kiraServer.getAllKiraServerEntitysInCluster()", e);
      }
      if (null != myKiraServerEntity && null != kiraServerEntitysInCluster) {
        kiraServerEntitysInCluster.remove(myKiraServerEntity);
        if (kiraServerEntitysInCluster.size() > 0) {
          LinkedHashMap<String, String> failedServerIdErrorDetailsMap = new LinkedHashMap<String, String>();

          for (KiraServerEntity oneKiraServerEntity : kiraServerEntitysInCluster) {
            String serverId = oneKiraServerEntity.getServerId();
            String clusterInternalServiceUrl = oneKiraServerEntity.getAccessUrlAsString();
            if (StringUtils.isNotBlank(clusterInternalServiceUrl)) {
              try {
                IClusterInternalService clusterInternalService = KiraServerUtils
                    .getClusterInternalService(clusterInternalServiceUrl);
                if (null == clusterInternalService) {
                  failedServerIdErrorDetailsMap.put(serverId,
                      "clusterInternalService is null by clusterInternalServiceUrl="
                          + clusterInternalServiceUrl);
                } else {
                  clusterInternalService.getKiraServerEntity();
                }
              } catch (Throwable t) {
                String errorDetails = "exceptionDetails=" + ExceptionUtils.getFullStackTrace(t);
                failedServerIdErrorDetailsMap.put(serverId, errorDetails);
                logger.error(
                    "Error occurs when check clusterInternalServiceUrl in ClusterInternalConnectionMonitorTask. clusterInternalServiceUrl="
                        + clusterInternalServiceUrl, t);
              }
            }
          }

          boolean result = true;
          String monitorDetails = null;
          if (failedServerIdErrorDetailsMap.size() > 0) {
            result = false;
            monitorDetails = KiraCommonUtils.toString(failedServerIdErrorDetailsMap);
          }

          MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
              .updateAndGetMonitorNoticeInfoIfNeeded(!result, monitorDetails);
          if (null != monitorNoticeInfo) {
            if (monitorNoticeInfo.isBad()) {
              ClusterInternalConnectionFailedEvent clusterInternalConnectionFailedEvent = new ClusterInternalConnectionFailedEvent(
                  KiraManagerHealthEventType.CLUSTER_INTERNAL_CONNECTION_FAILED,
                  SystemUtil.getLocalhostIp(), monitorNoticeInfo);
              KiraManagerHealthUtils
                  .dispatchKiraManagerHealthEvent(clusterInternalConnectionFailedEvent);
            } else {
              ClusterInternalConnectionRecoveredEvent clusterInternalConnectionRecoveredEvent = new ClusterInternalConnectionRecoveredEvent(
                  KiraManagerHealthEventType.CLUSTER_INTERNAL_CONNECTION_RECOVERED,
                  SystemUtil.getLocalhostIp(), monitorNoticeInfo);
              KiraManagerHealthUtils
                  .dispatchKiraManagerHealthEvent(clusterInternalConnectionRecoveredEvent);
            }
          }
        }
      }
    }
  }

}
