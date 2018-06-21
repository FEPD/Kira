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
package com.yihaodian.architecture.kira.manager.service;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraZoneContextData;
import com.yihaodian.architecture.kira.manager.dto.RunningEnvironmentForTimerTrigger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OtherService {

  void testSucess();

  void testFailed();

  void doLeaderRoutineWork();

  List<String> getTriggersWhichWillBeTriggeredInScope(LinkedHashMap<String, String> paramsAsMap)
      throws Exception;

  Map<String, List<String>> getTriggerTriggeredTimeListMapInScope(
      LinkedHashMap<String, String> paramsAsMap) throws Exception;

  void handleJobRuntimeData(LinkedHashMap<String, String> paramsAsMap) throws Throwable;

  HandleResult rectifyValueOfPoolNodesUnderTriggers();

  List<String> getPoolValueNeedRectifiedPoolsUnderTriggers() throws Exception;

  void scanJobTimeoutTrackerData(LinkedHashMap<String, String> paramsAsMap) throws Exception;

  void setPrivateEmails(String privateEmails) throws Exception;

  void setPrivatePhoneNumbers(String privatePhoneNumbers) throws Exception;

  void setAdminEmails(String adminEmails) throws Exception;

  void setAdminPhoneNumbers(String adminPhoneNumbers) throws Exception;

  void setHealthEventReceiverEmails(String healthEventReceiverEmails) throws Exception;

  void setHealthEventReceiverPhoneNumbers(String healthEventReceiverPhoneNumbers) throws Exception;

  void healthCheckForTimerTriggerScheduleForKiraManagerCluster() throws Exception;

  List<RunningEnvironmentForTimerTrigger> getInValidRunningEnvironmentListForTimerTrigger()
      throws Exception;

  List<String> cleanInValidEnvironmentsForTimerTrigger() throws Exception;

  void setKiraMasterZone(String newKiraMasterZone) throws Exception;

  void setAdminUserNames(String adminUserNames) throws Exception;

  Set<String> getPoolIdsWhichHaveNoRunningEnvironments() throws Exception;

  KiraZoneContextData getKiraZoneContextData() throws Exception;

  List<String> getTriggersWhosePoolIdOrTriggerIdStartWithBlankOrEndWithBlank() throws Exception;

  List<String> getTriggersWhichCanBeScheduledWhenInOldNoZoneSystem() throws Exception;

  List<String> migrateTheTriggersWhenUpgradeFromNoZoneSystemToZoneSystem() throws Exception;

  List<String> getTriggersWhoseVersionAreNotValid() throws Exception;

  void doJobRunStatistics(LinkedHashMap<String, String> paramsAsMap) throws Exception;

  void doTriggersPredictReport(LinkedHashMap<String, String> paramsAsMap) throws Exception;

  void testSendEmail();

  void testSendSMS();

}
