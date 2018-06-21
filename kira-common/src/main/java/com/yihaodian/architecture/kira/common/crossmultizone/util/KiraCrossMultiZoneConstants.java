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

import com.yihaodian.architecture.kira.common.KiraCommonConstants;

public interface KiraCrossMultiZoneConstants extends KiraCommonConstants {

  public static final String ZK_PATH_CROSS_MULTI_ZONE_ROOT =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA
          + "/crossMultiZone";
  public static final String ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE =
      ZK_PATH_CROSS_MULTI_ZONE_ROOT + "/masterZone";

  public static final long KIRA_CROSS_MULTI_ZONE_MONITOR_DOROUTINEWORK_INITIALDELAY_SECOND = 60L;
  public static final long KIRA_CROSS_MULTI_ZONE_MONITOR_DOROUTINEWORK_PERIOD_SECOND = 60L;
}
