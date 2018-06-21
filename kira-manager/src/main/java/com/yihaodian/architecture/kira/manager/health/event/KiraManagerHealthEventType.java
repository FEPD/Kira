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
package com.yihaodian.architecture.kira.manager.health.event;

public enum KiraManagerHealthEventType {
  ZK_FOR_KIRA_UNAVAILABLE, ZK_FOR_KIRA_RECOVERED, DB_FOR_SCHEDULE_UNAVAILABLE, DB_FOR_SCHEDULE_RECOVERED, DB_FOR_MENU_UNAVAILABLE, DB_FOR_MENU_RECOVERED, KIRAJOBITEMSTATUSCONSUMER_SICK, KIRAJOBITEMSTATUSCONSUMER_RECOVERED, KIRACLIENTREGISTERDATACONSUMER_SICK, KIRACLIENTREGISTERDATACONSUMER_RECOVERED, CLUSTER_INTERNAL_CONNECTION_FAILED, CLUSTER_INTERNAL_CONNECTION_RECOVERED, TIMER_TRIGGER_SCHEDULE_FAILED, TIMER_TRIGGER_SCHEDULE_RECOVERED, RUN_TIMER_TRIGGER_TASK_FAILED, RUN_TIMER_TRIGGER_TASK_RECOVERED, NO_JOBCREATEDANDRUN_FORSOMETIME_FORTIMERTRIGGER, CREATEANDRUNJOB_FORTIMERTRIGGER_RECOVERED, EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_FAILED, EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_RECOVERED;
}
