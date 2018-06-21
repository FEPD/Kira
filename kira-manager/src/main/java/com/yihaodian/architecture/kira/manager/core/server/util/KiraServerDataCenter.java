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
package com.yihaodian.architecture.kira.manager.core.server.util;

import com.yihaodian.architecture.hedwig.provider.AppProfile;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraServerDataCenter {

  private static Logger logger = LoggerFactory.getLogger(KiraServerDataCenter.class);

  private static volatile AppProfile appProfileForClusterInternalServiceExporter;
  private static volatile String hostIpForClusterInternalService;
  private static volatile Integer portForClusterInternalService;
  private static volatile String clusterInternalServiceUrl;
  private static volatile String parentPathForClusterInternalServiceOnZK;

  public KiraServerDataCenter() {
    // TODO Auto-generated constructor stub
  }

  public static AppProfile getAppProfileForClusterInternalServiceExporter() throws Exception {
    KiraServerDataCenter.waitForAppProfileForClusterInternalServiceExporterInitialized();
    return KiraServerDataCenter.appProfileForClusterInternalServiceExporter;
  }

  public static void setAppProfileForClusterInternalServiceExporter(
      AppProfile appProfileForClusterInternalServiceExporter) {
    KiraServerDataCenter.appProfileForClusterInternalServiceExporter = appProfileForClusterInternalServiceExporter;
  }

  /**
   * 等待初始化完成
   */
  public static void waitForAppProfileForClusterInternalServiceExporterInitialized()
      throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraServerDataCenter.appProfileForClusterInternalServiceExporter)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForAppProfileForClusterInternalServiceExporterInitialized.");
    } finally {
      if (null == KiraServerDataCenter.appProfileForClusterInternalServiceExporter) {
        throw new RuntimeException(
            "The ClusterInternalServiceExporter must be initialized for using kira-server.");
      }
    }
  }

  public static String getHostIpForClusterInternalService() throws Exception {
    KiraServerDataCenter.waitForHostIpForClusterInternalServiceInitialized();
    return KiraServerDataCenter.hostIpForClusterInternalService;
  }

  public static void setHostIpForClusterInternalService(
      String hostIpForClusterInternalService) {
    KiraServerDataCenter.hostIpForClusterInternalService = hostIpForClusterInternalService;
  }

  /**
   * 等待初始化完成
   */
  public static void waitForHostIpForClusterInternalServiceInitialized() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraServerDataCenter.hostIpForClusterInternalService)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForHostIpForClusterInternalServiceInitialized.");
    } finally {
      if (null == KiraServerDataCenter.hostIpForClusterInternalService) {
        throw new RuntimeException(
            "The hostIpForClusterInternalService must be initialized for using kira-server.");
      }
    }
  }

  public static Integer getPortForClusterInternalService() throws Exception {
    KiraServerDataCenter.waitForPortForClusterInternalServiceInitialized();
    return KiraServerDataCenter.portForClusterInternalService;
  }

  public static void setPortForClusterInternalService(
      Integer portForClusterInternalService) {
    KiraServerDataCenter.portForClusterInternalService = portForClusterInternalService;
  }

  /**
   * 等待初始化完成
   */
  public static void waitForPortForClusterInternalServiceInitialized() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraServerDataCenter.portForClusterInternalService)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForPortForClusterInternalServiceInitialized.");
    } finally {
      if (null == KiraServerDataCenter.portForClusterInternalService) {
        throw new RuntimeException(
            "The portForClusterInternalService must be initialized for using kira-server.");
      }
    }
  }

  public static String getClusterInternalServiceUrl() throws Exception {
    KiraServerDataCenter.waitForClusterInternalServiceUrlInitialized();
    return KiraServerDataCenter.clusterInternalServiceUrl;
  }

  public static void setClusterInternalServiceUrl(String clusterInternalServiceUrl) {
    KiraServerDataCenter.clusterInternalServiceUrl = clusterInternalServiceUrl;
  }

  /**
   * 等待初始化完成
   */
  public static void waitForClusterInternalServiceUrlInitialized() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraServerDataCenter.clusterInternalServiceUrl)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForClusterInternalServiceUrlInitialized.");
    } finally {
      if (null == KiraServerDataCenter.clusterInternalServiceUrl) {
        throw new RuntimeException(
            "The clusterInternalServiceUrl must be initialized for using kira-server.");
      }
    }
  }

  public static String getParentPathForClusterInternalServiceOnZK() throws Exception {
    KiraServerDataCenter.waitForParentPathForClusterInternalServiceOnZKInitialized();
    return KiraServerDataCenter.parentPathForClusterInternalServiceOnZK;
  }

  public static void setParentPathForClusterInternalServiceOnZK(
      String parentPathForClusterInternalServiceOnZK) {
    KiraServerDataCenter.parentPathForClusterInternalServiceOnZK = parentPathForClusterInternalServiceOnZK;
  }

  /**
   * 等待初始化完成
   */
  public static void waitForParentPathForClusterInternalServiceOnZKInitialized() throws Exception {
    try {
      int waittime = 0;
      while ((null == KiraServerDataCenter.parentPathForClusterInternalServiceOnZK)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error(
          "InterruptedException caught when waitForParentPathForClusterInternalServiceOnZKInitialized.");
    } finally {
      if (null == KiraServerDataCenter.parentPathForClusterInternalServiceOnZK) {
        throw new RuntimeException(
            "The parentPathForClusterInternalServiceOnZK must be initialized for using kira-server.");
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
