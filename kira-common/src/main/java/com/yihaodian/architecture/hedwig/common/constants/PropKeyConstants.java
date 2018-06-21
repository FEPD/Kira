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
package com.yihaodian.architecture.hedwig.common.constants;

public interface PropKeyConstants {

  public static final String ZK_SERVER_LIST = "cluster1.serverList";
  public static final String ZK_ROOT_PATH = "zk.root.path";
  public static final String JVM_PID = "jvm.pid";
  public static final String HOST_IP = "host.ip";
  public static final String HOST_PORT = "host.port";
  public static final String HOST_WEIGHTED = "host.weight";
  public static final String POOL_ID = "pool.id";
  public static final String CLIENT_VERSION = "clt.vsn";
  public static final String CAMPS_NAME = "clt.camp";

  public static final String HEDWIG_POOL_CORESIZE = "pool.size";
  public static final String HEDWIG_POOL_MAXSIZE = "pool.maxSize";
  public static final String HEDWIG_POOL_IDLETIME = "pool.idleTime";
  public static final String HEDWIG_POOL_QUEUESIZE = "pool.queueSize";

  public static final String HEDWIG_SCHEDULER_POOL_CORESIZE = "schedulerPool.size";
  public static final String HEDWIG_SCHEDULER_POOL_MAXSIZE = "schedulerPool.maxSize";
  public static final String HEDWIG_SCHEDULER_POOL_IDLETIME = "schedulerPool.idleTime";
  public static final String HEDWIG_SCHEDULER_POOL_DELY = "schedulerPool.dely";

  public static final String HEDWIG_PROFILE_SENSITIVE = "profile.sensitive";

  public static final String HEDWIG_READ_TIMEOUT = "read.timeout";
  public static final String HEDWIG_CLIENT_THROTTLE = "client.throttle";
  public static final String HEDWIG_TOKEN_GRAY = "token.gray";
}
