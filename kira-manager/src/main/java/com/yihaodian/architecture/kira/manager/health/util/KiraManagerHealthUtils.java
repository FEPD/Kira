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
package com.yihaodian.architecture.kira.manager.health.util;

import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEvent;
import com.yihaodian.architecture.kira.manager.health.internal.KiraManagerHealthEventDispatcherWrapper;
import com.yihaodian.architecture.kira.manager.health.internal.KiraManagerHealthEventHandleComponent;
import com.yihaodian.architecture.kira.manager.health.monitor.KiraManagerHealthMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerHealthUtils {

  private static Logger logger = LoggerFactory.getLogger(KiraManagerHealthUtils.class);

  public KiraManagerHealthUtils() {
  }

  public static void prepareKiraManagerHealthUtils() throws Exception {
    KiraManagerHealthEventHandleComponent.getKiraManagerHealthEventHandleComponent();
    KiraManagerHealthMonitor.getKiraManagerHealthMonitor();
  }

  public static boolean dispatchKiraManagerHealthEvent(
      KiraManagerHealthEvent kiraManagerHealthEvent) {
    boolean returnValue = false;
    try {
      returnValue = KiraManagerHealthEventDispatcherWrapper
          .getKiraManagerHealthEventDispatcherWrapper()
          .dispatchKiraManagerHealthEvent(kiraManagerHealthEvent);
    } catch (Exception e) {
      logger.error(
          "Exception occurs when calling KiraManagerHealthUtils.KiraManagerHealthEvent. kiraManagerHealthEvent={}",
          kiraManagerHealthEvent);
    }
    return returnValue;
  }

  public static void destroyKiraManagerHealthUtils() {
    //Destroy monitors
    try {
      KiraManagerHealthMonitor.getKiraManagerHealthMonitor().destroy();
    } catch (Exception e) {
      logger.error(
          "Error occurs when calling KiraManagerHealthMonitor.getKiraManagerHealthMonitor().destroy().",
          e);
    }

    //Need to destroy health event dispatcher first then destroy health event handler
    try {
      KiraManagerHealthEventDispatcherWrapper.getKiraManagerHealthEventDispatcherWrapper()
          .destroyKiraManagerHealthEventDispatcherWrapper();
    } catch (Exception e) {
      logger.error(
          "Error occurs when calling KiraManagerHealthEventDispatcherWrapper.getKiraManagerHealthEventDispatcherWrapper().destroyKiraManagerHealthEventDispatcherWrapper().",
          e);
    }

    try {
      KiraManagerHealthEventHandleComponent.getKiraManagerHealthEventHandleComponent().destroy();
    } catch (Exception e) {
      logger.error(
          "Error occurs when calling KiraManagerHealthEventHandleComponent.getKiraManagerHealthEventHandleComponent().destroy().",
          e);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

}
