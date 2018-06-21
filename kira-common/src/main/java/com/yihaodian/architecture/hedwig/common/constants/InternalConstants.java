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

public interface InternalConstants {

  public static final String CONFIG_GROUP = "yihaodian_common";
  public static final String CONFIG_FILE_CLIENT = "hedwigClient.properties";
  public static final String CONFIG_FILE_PROVIDER = "hedwigProvider.properties";
  public static final String CONFIG_FILE_ZKCLUSTER = "zookeeper-cluster.properties";
  public static final String STRING_SEPARATOR = ",";
  public static final String BASE_ROOT = "/MASS";
  public static final String BASE_ROOT_FLAGS = "/FlagsCenter";
  public static final String FLAG_GRAY = "/gray";
  public static final String UNKONW_DOMAIN = "UnknowDomain";
  public static final String PROPERITIES_FILE_NAME = "hedwig.properties";
  public static final String PROPERITIES_PATH_KEY = "hedwig_config";
  public static final String NON_GROUP = "NoGroup";

  public static final String REMOTE_EXCEPTION = "RemoteException";

  public static final String HASH_FUNCTION_MUR2 = "murmur2";

  public static final String LOG_PROFIX = "Hedwig said: ";
  public static final String ENGINE_LOG_PROFIX = "Hedwig event engine said: ";
  public static final String HANDLE_LOG_PROFIX = "Hedwig event handler said: ";

  public static final String BALANCER_NAME_ROUNDROBIN = "RoundRobin";
  public static final String BALANCER_NAME_WEIGHTED_ROUNDROBIN = "WeightedRoundRobin";
  public static final String BALANCER_NAME_CONSISTENTHASH = "ConsistentHash";

  public static final String SERVICE_REGISTER_ZK = "zkRegister";

  public static final String PROTOCOL_PROFIX_HTTP = "http";
  public static final String HEDWIG_URL_PATTERN = "hedwigServices";

  public static final long DEFAULT_REQUEST_TIMEOUT = 50;
  public static final long DEFAULT_READ_TIMEOUT = 20;

  public static final int DEFAULT_POOL_CORESIZE = 20;
  public static final int DEFAULT_POOL_MAXSIZE = 30;
  public static final int DEFAULT_POOL_IDLETIME = 60;
  public static final int DEFAULT_POOL_QUEUESIZE = 20;

  public static final int DEFAULT_SCHEDULER_POOL_CORESIZE = 5;
  public static final int DEFAULT_SCHEDULER_POOL_MAXSIZE = 10;
  public static final int DEFAULT_SCHEDULER_POOL_IDLETIME = 60;
  public static final int DEFAULT_SCHEDULER_DELY = 5;

  public static final int DEFAULT_RELIVE_THRESHOLD = 50;
  public static final int MAX_REDO_THRESHOLD = 3;

  public static final int DEFAULT_MAX_COLLECT_ROUND = 12;
  public static final int DEFAULT_COLLECT_INTERVAL = 5;
  public static final int DEFAULT_COLLECT_INTERVAL_UNIT_SECOND = 1000;

  public static final String HEDWIG_REQUEST_ID = "reqId";
  public static final String HEDWIG_GLOBAL_ID = "glbId";
  public static final String HEDWIG_TXN_ID = "txnId";
  public static final String HEDWIG_INVOKE_TIME = "invokeTime";
  public static final String HEDWIG_SERVICE_IP = "providerIp";
  public static final String HEDWIG_MONITORLOG = "monitorLog";
  public static final String HEDWIG_REQUEST_HOP = "reqHop";

  public static final int HEDWIG_CLIENT = -1;
  public static final int HEDWIG_PROVIDER = 1;

  public static final int MIRROR_SEED = 30;

  public static final int INTEGER_BARRIER = Integer.MAX_VALUE / 2;

  public static final String HEDWIG_PAHT_CAMPS = "hedwig_camps";
  public static final String HEDWIG_PAHT_REFUGEE = "refugee";
  public static final String HEDWIG_PAHT_ROLL = "hedwig_roll";
  public static final String HEDWIG_PAHT_APPDICT = "/AppPathDict";

  public static final int ZK_SESSION_TIMEOUT = 15000;

  /**
   * 通讯平台domain name
   */
  public static final String HEDWIG_DOMAIN_NAME = "hedwig";

  public static final int VALUE_LENGTH_LIMIT = 150;

  public static final String HEDWIG_METHOD_IS_VOID_KEY = "HEDWIG_METHOD_IS_VOID_KEY";
  public static final String HEDWIG_METHOD_ARGUMENTS_KEY = "HEDWIG_METHOD_ARGUMENTS_KEY";

}
