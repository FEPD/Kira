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
package com.yihaodian.architecture.kira.common;

import static com.yihaodian.architecture.hedwig.common.constants.InternalConstants.BASE_ROOT;

public interface KiraCommonConstants {

  public static final String DOMAIN_NAME_SOA = "201-SOA";
  public static final String POOL_ID_KIRA = "kira";
  public static final String SERVICE_NAME_CENTRAL_SCHEDULE_SERVICE = "centralScheduleService";

  public static final String ZNODE_NAME_PREFIX = "/";
  public static final String ZNODE_NAME_CENTRAL_SCHEDULE_SERVICE =
      ZNODE_NAME_PREFIX + SERVICE_NAME_CENTRAL_SCHEDULE_SERVICE;
  public static final String ZNODE_NAME_TRIGGER_METADATA = "/triggerMetadata";
  public static final String ZNODE_NAME_ENVIRONMENTS = "/environments";
  public static final String ZNODE_NAME_RUNNING = "/running";

  public static final String PARENT_ZNODE_NAME_FOR_LOCKS_TO_CONTROL_CONCURRENT_RUN_BUSINESS_METHOD = "locksToControlConcurrentRunBusinessMethod";

  public static final String ZK_PATH_THESTORE = BASE_ROOT;
  public static final String ZK_PATH_DOMAIN_SOA =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA;
  public static final String ZK_PATH_POOL_KIRA =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA;
  public static final String ZK_PATH_TRIGGERS =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA
          + "/triggers";
  public static final String ZK_PATH_APPCENTERS =
      ZK_PATH_THESTORE + ZNODE_NAME_PREFIX + DOMAIN_NAME_SOA + ZNODE_NAME_PREFIX + POOL_ID_KIRA
          + "/appCenters";

  public static final String ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX = "_";

  public static final String SPECIAL_DELIMITER = ":::";
  public static final String COLON_DELIMITER = ":";
  public static final String COMMA_DELIMITER = ",";
  public static final String KIRA_TIMER_TRIGGER_VERSION_DELIMITER = ".";

  public static final long CENTRAL_SCHEDULE_SERVICE_HEDWIG_HESSIAN_READTIMEOUT_MILLISECOND = 50000L;
  public static final String CENTRAL_SCHEDULE_SERVICE_AUTH_USERNAME = "yhdCentralSchedualServiceUserName";
  public static final String CENTRAL_SCHEDULE_SERVICE_AUTH_PASSWORD = "yhdCentralSchedualServicePassword";
  public static final String SERVICEVERSION_CENTRALSCHEDULESERVICE = "0.0.1";

  public static final String QUEUE_KIRA_JOB_ITEM_STATUS = "kiraJobItemStatus";
  public static final String QUEUE_KIRA_CLIENT_REGISTER_DATA = "kiraClientRegisterData";

  public static final long JOB_ITEM_HANDLE_TIMEOUT_SECOND = 120L;

  // result code
  public static final String RESULT_CODE_SUCCESS = "0";
  public static final String RESULT_CODE_PARTIAL_SUCCESS = "1";
  public static final String RESULT_CODE_FAILED = "2";
  public static final String RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD = "3";

  public static final String DATEFORMAT_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
  public static final String DATEFORMAT_YYYYMMDDHHMMSSSSS = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION = "yyyyMMdd.HHmmss.SSS";
  public static final String DATEFORMAT_DEFAULT = DATEFORMAT_YYYYMMDDHHMMSS;

  public static final long RETRY_INTERVAL_MILLISECOND = 2000L;

  public static final String LINE_SEPARATOR = "\r\n";

  public static final String STRING_SNAPSHOT = "-SNAPSHOT-";

  public static final String STRING_PROJECTVERSION = "ProjectVersion";

  public static final String CHARSET_UTF8 = "UTF-8";

  public static final String DEFAULT_TRIGGER_VERSION = "0";

  public static final int DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND = 120000;

  public static final int DEFAULT_PRIORITY_OF_TIMER_TRIGGER = 5;

  // The minimum value of dispatch time out in milliseconds
  public static final long JOB_DISPATCH_TIMEOUT_LOW_LIMIT = 2 * 60 * 1000;

  public static final String KIRA_AGENT_WEBAPPS_DIR = "kira.agent.webapps.dir";

  public static final String JOB_DETAIL_KEY = "jobDetail";
}
