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
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.domain.Job;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.PoolTriggerStatus;
import com.yihaodian.architecture.kira.manager.dto.TriggerEnvironmentDetailData;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.dto.TriggerPredictReportLineData;
import com.yihaodian.architecture.kira.manager.dto.UpdateTriggerResult;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TriggerMetadataService {

  void insert(TriggerMetadata triggerMetadata);

  int update(TriggerMetadata triggerMetadata);

  int delete(Long id);

  TriggerMetadata select(Long id);

  List<TriggerMetadata> list(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerMetadata> listOnPage(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerMetadata> listLatest(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerMetadata> listLatestOnPage(TriggerMetadataCriteria triggerMetadataCriteria);

  Map<String, Set<String>> getPoolIdTriggerIdSetMapOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria);

  PoolTriggerStatus getPoolTriggerStatus(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerEnvironmentDetailData> getTriggerEnvironmentDetailDataList(
      TriggerMetadataCriteria triggerMetadataCriteria);

  List<String> getPoolIdList(TriggerMetadataCriteria triggerMetadataCriteria);

  List<String> getTriggerIdList(TriggerMetadataCriteria triggerMetadataCriteria);

  int updateUnRegisteredStatusForTriggers(List<TriggerIdentity> triggerIdentityList,
      Boolean unRegistered);

  List<TriggerIdentity> getTriggerIdentityList(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerIdentity> getTriggerIdentityListWithoutVersion(
      TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerIdentity> getAllRegisteredAndUnDeletedTriggerIdentityInDB(boolean includeVersion)
      throws Exception;

  List<TriggerIdentity> getAllCanBeScheduledTriggerIdentityInDB(boolean includeVersion)
      throws Exception;

  List<TriggerIdentity> getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(
      Boolean masterZone) throws Exception;

  List<TriggerIdentity> getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(
      Boolean masterZone) throws Exception;

  TriggerMetadata registerTriggerMetadataZNode(TriggerMetadataZNodeData triggerMetadataZNodeData);

  List<TriggerMetadata> registerTriggerMetadataZNodeDataList(
      List<TriggerMetadataZNodeData> triggerMetadataZNodeDataList);

  TriggerMetadata getLatestAndAvailableTriggerMetadata(String poolId, String triggerId,
      String version);

  TriggerMetadata getLatestTriggerMetadata(TriggerMetadataCriteria triggerMetadataCriteria);

  TriggerMetadata getLatestAndAvailableTriggerMetadataByJobId(String jobId);

  List<PoolTriggerStatus> getPoolTriggerStatusListOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria);

  void unRegisterTrigger(TriggerIdentity triggerIdentity) throws Exception;

  void registerTrigger(TriggerIdentity triggerIdentity,
      IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager) throws Exception;

  void handleTriggerZKDataChange(TriggerMetadataZNodeData newTriggerMetadataZNodeData)
      throws Exception;

  boolean doRescheduleJob(TriggerIdentity triggerIdentity) throws Exception;

  HandleResult rescheduleJob(TriggerMetadataCriteria triggerMetadataCriteria);

  boolean unscheduleJobIfNeeded(String poolId, String triggerId) throws Exception;

  void doDeleteTrigger(String poolId, String triggerId, String version, String deletedBy)
      throws Exception;

  HandleResult deleteTrigger(TriggerMetadataCriteria triggerMetadataCriteria, String deletedBy);

  boolean scheduleTriggerIfNeeded(TriggerIdentity triggerIdentity) throws Exception;

  int updateDeletedStatusForTriggers(List<TriggerIdentity> triggerIdentityList, Boolean deleted,
      String comments);

  HandleResult manuallyRunJobByTriggerMetadata(TriggerMetadataCriteria triggerMetadataCriteria,
      String createdBy);

  Job createAndRunJobByTriggerMetadata(TriggerMetadata triggerMetadata, Boolean manuallyScheduled,
      String createdBy, Date triggerTime);

  UpdateTriggerResult updateTrigger(TriggerMetadataUpdateContent triggerMetadataUpdateContent,
      String updatedBy);

  TriggerMetadata doUpdateTrigger(TriggerMetadata oldTriggerMetadata,
      TriggerMetadataUpdateContent triggerMetadataUpdateContent, String updatedBy) throws Exception;

  HandleResult deleteTriggerEnvironment(TriggerMetadataCriteria triggerMetadataCriteria);

  TriggerMetadata getTriggerMetadataById(Long id);

  List<String> getTriggerTypeList();

  List<Integer> getMisfireInstructionList(TriggerMetadataCriteria triggerMetadataCriteria);

  List<TriggerIdentity> getAllRegisteredAndUndeletedTriggerIdentitysForPoolInDB(String poolId)
      throws Exception;

  int unRegisterAllRegisteredTriggersOfPool(String poolId) throws Exception;

  void handlePoolDeletedOnZK(String poolId) throws Exception;

  HandleResult createTrigger(TriggerMetadataCreateContent newTriggerMetadata, String userName);

  TriggerMetadata doCreateTrigger(TriggerMetadataCreateContent triggerMetadataCreateContent,
      String userName) throws Exception;

  void checkAndAddDefaultKiraClientMetadataForTriggers() throws Exception;

  List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(
      TriggerMetadataCriteria criteria);

  List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(
      TriggerMetadataCriteria criteria);

  void doDeleteTriggersOfPool(String poolId, String deletedBy) throws Exception;

  void cascadeCrossZoneDeleteTimerTrigger(TriggerIdentity triggerIdentity);

  int updateCrossMultiZoneData(String poolId, String triggerId, String version,
      Boolean copyFromMasterToSlaveZone, Boolean onlyScheduledInMasterZone, String comments);

  List<KiraTimerTriggerBusinessRunningInstance> getKiraTimerTriggerBusinessRunningInstanceList(
      TriggerMetadataCriteria criteria);

  List<TriggerPredictReportLineData> getTriggerPredictReportLineDataList(Date startTime,
      Date endTime, List<String> poolIdList, List<String> triggerIdList) throws Exception;

  List<TriggerIdentity> getTriggerIdentityListWhichWillBeTriggeredInScope(Date startTime,
      Date endTime, List<String> poolIdList, List<String> triggerIdList) throws Exception;

  Map<TriggerIdentity, List<Date>> getTriggerIdentityTriggeredTimeListMapInScope(Date startTime,
      Date endTime, List<String> poolIdList, List<String> triggerIdList, Integer maxCountPerTrigger)
      throws Exception;
}
