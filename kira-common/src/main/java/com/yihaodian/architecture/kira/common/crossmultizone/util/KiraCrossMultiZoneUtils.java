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
package com.yihaodian.architecture.kira.common.crossmultizone.util;

import com.yihaodian.architecture.kira.common.crossmultizone.internal.IKiraCrossMultiZoneMonitor;
import com.yihaodian.architecture.kira.common.crossmultizone.internal.KiraCrossMultiZoneMonitor;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraCrossMultiZoneUtils {

  private static Logger logger = LoggerFactory.getLogger(KiraCrossMultiZoneUtils.class);

  public static void registerKiraCrossMultiZoneEventHandler(EventHandler eventHandler)
      throws Exception {
    IKiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor = KiraCrossMultiZoneMonitor
        .getKiraCrossMultiZoneMonitor();
    kiraCrossMultiZoneMonitor.registerKiraCrossMultiZoneEventHandler(eventHandler);
  }

  public static String getCurrentKiraZoneId() throws Exception {
    IKiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor = KiraCrossMultiZoneMonitor
        .getKiraCrossMultiZoneMonitor();
    String currentKiraZoneId = kiraCrossMultiZoneMonitor.getCurrentKiraZoneId();

    return currentKiraZoneId;
  }

  public static String getKiraMasterZoneId(boolean accurate) throws Exception {
    IKiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor = KiraCrossMultiZoneMonitor
        .getKiraCrossMultiZoneMonitor();
    String kiraMasterZoneId = kiraCrossMultiZoneMonitor.getKiraMasterZoneId(accurate);

    return kiraMasterZoneId;
  }

  public static KiraCrossMultiZoneRoleEnum getKiraCrossMultiZoneRole(boolean accurate)
      throws Exception {
    IKiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor = KiraCrossMultiZoneMonitor
        .getKiraCrossMultiZoneMonitor();
    return kiraCrossMultiZoneMonitor.getKiraCrossMultiZoneRole(accurate);
  }

  public static void destroyKiraCrossMultiZoneUtils() {
    try {
      IKiraCrossMultiZoneMonitor kiraCrossMultiZoneMonitor = KiraCrossMultiZoneMonitor
          .getKiraCrossMultiZoneMonitor();
      kiraCrossMultiZoneMonitor.destroy();
    } catch (Exception e) {
      logger.error("Error occurs when destroyKiraCrossMultiZoneUtils.", e);
    }
  }

  public static boolean isMasterZone(KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole) {
    return KiraCrossMultiZoneRoleEnum.MASTER.equals(kiraCrossMultiZoneRole);
  }
}
