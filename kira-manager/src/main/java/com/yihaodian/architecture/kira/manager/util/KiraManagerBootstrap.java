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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.manager.core.KiraManagerCoreBootstrap;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraManagerCrossMultiZoneUtils;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerBootstrap implements ILifecycle {

  private static Logger logger = LoggerFactory.getLogger(KiraManagerBootstrap.class);

  private KiraManagerCoreBootstrap kiraManagerCoreBootstrap;

  public KiraManagerBootstrap() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  @Override
  public void init() {
    logger.info("init for KiraManagerBootstrap.");
    KiraManagerDataCenter.setServerBirthTime(new Date());

    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    if (logger.isDebugEnabled()) {
      logger.debug("env={}", env);
    }
    if ("test".equalsIgnoreCase(env)) {
      //7 days and 1 hour = 60*24*7 + 60 minutes = 10140 minutes
      KiraManagerUtils.setMinutesToKeepJobRuntimeData(10140);
      //6 hours = 60*6 minutes =  360 minutes
      //3 hours = 60*3 minutes =  180 minutes
      KiraManagerUtils.setMinutesPerTimeToHandleJobRuntimeData(180);
    } else {
      //31 days and 1 hour = 60*24*31 + 60 minutes = 44700 minutes
      KiraManagerUtils.setMinutesToKeepJobRuntimeData(44700);
      //6 hours = 60*6 minutes =  360 minutes
      //3 hours = 60*3 minutes =  180 minutes
      KiraManagerUtils.setMinutesPerTimeToHandleJobRuntimeData(180);
    }

    try {
      KiraManagerCrossMultiZoneUtils.prepareKiraManagerCrossMultiZoneUtils();
    } catch (Exception e) {
      logger.error("Error occurs when prepareKiraManagerCrossMultiZoneUtils.", e);
      throw new RuntimeException(e);
    }

    try {
      KiraManagerHealthUtils.prepareKiraManagerHealthUtils();
    } catch (Exception e) {
      logger.error(
          "Error occurs when KiraManagerHealthUtils.prepareKiraManagerHealthEventHandleComponent.",
          e);
      throw new RuntimeException(e);
    }

    Thread kiraManagerCoreBootstrapStartThread = new Thread(new Runnable() {
      public void run() {
        try {
          KiraManagerBootstrap.this.kiraManagerCoreBootstrap = new KiraManagerCoreBootstrap();
          KiraManagerDataCenter
              .setKiraManagerCoreBootstrap(KiraManagerBootstrap.this.kiraManagerCoreBootstrap);
          KiraManagerBootstrap.this.kiraManagerCoreBootstrap.start();
        } catch (Exception e) {
          logger.error("Error occurs for start KiraManagerCoreBootstrap.", e);
        } finally {
          logger.info("Finish kiraManagerCoreBootstrapStartThread's work.");
        }
      }
    });
    kiraManagerCoreBootstrapStartThread.start();
  }

  @Override
  public void destroy() {
    if (null != logger) {
      logger.info("Destroying KiraManagerBootstrap...");
    }

    try {
      KiraManagerHealthUtils.destroyKiraManagerHealthUtils();
    } finally {
      if (null != logger) {
        logger.info(
            "Finish for KiraManagerHealthEventDispatchUtils.destroyKiraManagerHealthEventHandleComponent().");
      }
    }

    try {
      if (null != kiraManagerCoreBootstrap) {
        kiraManagerCoreBootstrap.destroy();
      }
    } finally {
      if (null != logger) {
        logger.info("Finish destroy for kiraManagerCoreBootstrap.");
      }
    }

    try {
      KiraManagerCrossMultiZoneUtils.destroyKiraManagerCrossMultiZoneUtils();
    } finally {
      if (null != logger) {
        logger.info("Finish destroy for destroyKiraManagerCrossMultiZoneUtils.");
      }
    }

    try {
      KiraManagerCrossMultiZoneUtils.destroyZoneSwitcherZKAssist();
    } finally {
      if (null != logger) {
        logger.info("Finish destroy for destroyZoneSwitcher.");
      }
    }

    try {
      ZkClient _zkClient = KiraZkUtil.initDefaultZk();
      _zkClient.unsubscribeAll();
    } finally {
      if (null != logger) {
        logger.info("Finish destroy for zookeeper watcher.");
      }
    }
  }
}
