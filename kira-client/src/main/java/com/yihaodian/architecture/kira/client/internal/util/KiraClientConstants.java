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
package com.yihaodian.architecture.kira.client.internal.util;

public interface KiraClientConstants extends
    com.yihaodian.architecture.kira.common.KiraCommonConstants {

  public static final String KIRA_CLIENT_THREAD_NAME_PREFIX = "KIRA-CLIENT-";

  public static final String JOBDATAMAP_KEY_TARGETMETHOD = "targetMethod";
  public static final String JOBDATAMAP_KEY_TARGETMETHODARGTYPES = "targetMethodArgTypes";
  public static final String JOBDATAMAP_KEY_ARGUMENTS = "arguments";
  public static final String JOBDATAMAP_KEY_METHODINVOKER = "methodInvoker";

  public static final String JOB_PHASE_DELIVERING = "jobDelivering";
  public static final String JOB_PHASE_DELIVER_COMPLETE = "jobDeliverComplete";
  public static final String JOB_PHASE_RUNNING = "jobRunning";
  public static final String JOB_PHASE_RUN_COMPLETE = "jobRunComplete";
  public static final String JOB_PHASE_NO_NEED_TO_RUN_BUSINESS_METHOD = "noNeedToRunBusinessMethod";

  public static long WAITFOR_STATECHANGE_TIMEOUT_SECONDS = 60L;
  public static long WAITFOR_RESOURCE_TIMEOUT_MILLISECOND = 120000L;

  public static final String ALLSCHEDULEDLOCALLY_HINT = "This pool is set to schedule all the jobs locally.";

  public static final String STRING_EMPTY = "empty";
}
