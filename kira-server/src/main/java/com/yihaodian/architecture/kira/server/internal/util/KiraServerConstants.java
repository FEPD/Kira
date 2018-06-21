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
package com.yihaodian.architecture.kira.server.internal.util;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;

public interface KiraServerConstants extends KiraCommonConstants {

  public static final String ZK_PATH_DEFAULT_ROOT_OF_KIRASERVERCLUSTER =
      ZK_PATH_POOL_KIRA + ZNODE_NAME_PREFIX + "kiraServerCluster" + ZNODE_NAME_PREFIX;
  public static final String ZNODE_NAME_KIRASERVERS = "kiraServers";
  public static final String ZNODE_NAME_KIRASERVERLEADER = "kiraServerLeader";

  public static final long KIRA_SERVER_DOROUTINEWORK_INITIALDELAY_SECOND = 60L;
  public static final long KIRA_SERVER_DOROUTINEWORK_PERIOD_SECOND = 60L;
}
