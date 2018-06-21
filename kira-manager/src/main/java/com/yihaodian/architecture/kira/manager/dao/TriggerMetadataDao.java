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
package com.yihaodian.architecture.kira.manager.dao;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface TriggerMetadataDao {

  void insert(TriggerMetadata triggerMetadata) throws DataAccessException;

  int update(TriggerMetadata triggerMetadata) throws DataAccessException;

  int delete(Long id) throws DataAccessException;

  TriggerMetadata select(Long id) throws DataAccessException;

  List<TriggerMetadata> list(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  List<TriggerMetadata> listOnPage(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  int count(TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  TriggerMetadata getLatestAndAvailableTriggerMetadata(String appId, String triggerId,
      String version) throws DataAccessException;

  List<TriggerMetadata> listLatest(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  List<TriggerMetadata> listLatestOnPage(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  List<TriggerMetadata> listLatestOnPageUsingLimit(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  int countLatest(TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityList(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityListOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  int countTriggerIdentityList(TriggerMetadataCriteria triggerMetadataCriteria)
      throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityListWithoutVersion(
      TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  List<String> getPoolIdList(TriggerMetadataCriteria triggerMetadataCriteria);

  List<String> getTriggerIdList(TriggerMetadataCriteria triggerMetadataCriteria);

  int updateUnRegisteredStatusForTriggers(List<TriggerIdentity> triggerIdentityList,
      Boolean unRegistered);

  TriggerMetadata getLatestAndAvailableTriggerMetadataByJobId(String jobId);

  int updateUnRegisteredStatus(TriggerMetadataCriteria triggerMetadataCriteria,
      Boolean unRegistered);

  int updateDeletedStatusForTriggers(List<TriggerIdentity> triggerIdentityList, Boolean deleted,
      String comments);

  List<String> getPoolIdListOfTriggersWhichHasNoKiraClientMetadata();

  List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria);

  int countLatestTriggerMetadatasWhichSetTriggerAsTarget(
      TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  List<TriggerMetadata> getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(
      TriggerMetadataCriteria triggerMetadataCriteria);

  int countLatestTriggerMetadatasWhichSetTriggersOfPoolAsTarget(
      TriggerMetadataCriteria triggerMetadataCriteria) throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityListWhichShouldBeUnScheduledWithoutVersion(
      Boolean masterZone) throws DataAccessException;

  List<TriggerIdentity> getTriggerIdentityListWhichShouldBeScheduledWithoutVersion(
      Boolean masterZone) throws DataAccessException;

  int updateCrossMultiZoneData(String appId, String triggerId, String version,
      Boolean copyFromMasterToSlaveZone, Boolean onlyScheduledInMasterZone, String comments)
      throws DataAccessException;

}
