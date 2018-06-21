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

public interface KiraServerConstants extends
    com.yihaodian.architecture.kira.common.KiraCommonConstants {

  public static final String APP_KIRA = "kira";
  public static final String POOL_ID_FULLNAME_KIRA = "kira";

  public static final String SERVICE_NAME_CLUSTER_INTERNAL_SERVICE = "clusterInternalService";
  public static final String ZNODE_NAME_CLUSTER_INTERNAL_SERVICE =
      ZNODE_NAME_PREFIX + SERVICE_NAME_CLUSTER_INTERNAL_SERVICE;
  public static final String ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX = "_";

  public static final String ZK_PATH_SCHEDULE_SERVERS =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA
          + "/scheduleServers";
  public static final String ZK_PATH_SCHEDULE_SERVER_LEADER =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA
          + "/scheduleServerLeader";

  public static final String HTTP_CONTENT_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8";

  public static final String DEFAULT_CHARSET = "UTF-8";

  public static final String CONFIG_FILENAME_KIRASERVERPROPERTIES = "kira-server.properties";
  public static final String PROPERTY_KIRA_SERVER_LISTEN_PORT = "kira.server.listen.port";

  public static final String JOBDATAMAP_KEY_TRIGGERMETADATA = "triggerMetadata";

  public static final String SPRING_BEAN_NAME_JOBSERVICE = "jobService";
  public static final String SPRING_BEAN_NAME_TRIGGERMETADATASERVICE = "triggerMetadataService";
  public static final String SPRING_BEAN_NAME_JOBITEMSERVICE = "jobItemService";
  public static final String SPRING_BEAN_NAME_OPERATIONLOGSERVICE = "operationLogService";

  public static final long CLUSTER_INTERNAL_SERVICE_HEDWIG_HESSIAN_READTIMEOUT_MILLISECOND = 50000L;
  public static final String CLUSTER_INTERNAL_SERVICE_AUTH_USERNAME = "yhdClusterInternalServiceUserName";
  public static final String CLUSTER_INTERNAL_SERVICE_AUTH_PASSWORD = "yhdClusterInternalServicePassword";
  public static final String SERVICEVERSION_CLUSTERINTERNALSERVICE = "0.0.1";

  public static final long WAIT_FOR_KIRA_SERVER_ROLE_CHANGE_MILLISECOND = 5000L;

  public static final long WAIT_FOR_LEADER_DO_ROUTINE_WORK_TIMEOUT_SECOND = 120L;

  //scheduled to run health check every 30 seconds after 1 minute's delay
  public static final long KIRA_SERVER_HEALTH_CHECK_INITIALDELAY_SECOND = 60L;
  public static final long KIRA_SERVER_HEALTH_CHECK_PERIOD_SECOND = 30L;
  public static final long KIRA_SERVER_HEALTH_CHECK_THRESHOLD_MILLISECOND = 120000L; //2 minutes

  //scheduled to load balance the kira server cluster every 10 minute after 5 minute's delay
  public static final long KIRA_SERVER_CLUSTER_LOAD_BALANCE_INITIALDELAY_SECOND = 300L;
  public static final long KIRA_SERVER_CLUSTER_LOAD_BALANCE_PERIOD_SECOND = 600L;

  public static final long TRIGGER_CAN_BE_REASSIGNED_FUTURE_INTERVAL_MILLISECOND = 300000L; //5 minutes

  public static final String SESSION_ATTRIBUTE_KEY_USERCONTEXTDATA = "userContextData";
  public static final int HTTP_STATUS_CODE_NOT_AUTHENTICATED = 999;
  public static final String HTTP_STATUS_TEXT_NOT_AUTHENTICATED = "You are not authenticated.";

  public static final String UNKNOWN_USER = "Unknown User";

  public static final String STRUTS2_ACTION_METHOD_DEFAULT = "execute";

  public static final int MAX_TRY_COUNT_DB = 10;

  public static final String KIRA_CLIENT_VERSION_NA = "N/A";
}
