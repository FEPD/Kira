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
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.manager.criteria.KiraClientMetadataCriteria;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import java.util.List;

public interface KiraClientMetadataService {

  void insert(KiraClientMetadata kiraClientMetadata);

  int update(KiraClientMetadata kiraClientMetadata);

  int delete(Long id);

  KiraClientMetadata select(Long id);

  List<KiraClientMetadata> list(KiraClientMetadataCriteria kiraClientMetadataCriteria);

  List<KiraClientMetadata> listOnPage(KiraClientMetadataCriteria kiraClientMetadataCriteria);

  void handleKiraClientRegisterData(KiraClientRegisterData kiraClientRegisterData);

  List<KiraClientMetadata> getKiraClientMetadataListByPoolIdList(List<String> poolIdList);

  KiraClientMetadata getKiraClientMetadataByPoolId(String poolId);

  List<String> getInvisiblePoolListForUser(UserContextData userContextData);

  List<String> getPoolIdList(KiraClientMetadataCriteria kiraClientMetadataCriteria);

  KiraClientMetadata getKiraClientMetadataById(Long id);

  HandleResult updateKiraClientMetadata(
      KiraClientMetadataUpdateContent kiraClientMetadataUpdateContent);

  int doUpdateKiraClientMetadata(KiraClientMetadata oldKiraClientMetadata,
      KiraClientMetadataUpdateContent kiraClientMetadataUpdateContent) throws Exception;

  HandleResult createKiraClientMetadata(
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent);

  KiraClientMetadata doCreateKiraClientMetadata(
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent) throws Exception;

  boolean isKiraClientMetadataExistForPool(String poolId);

  HandleResult deletePool(KiraClientMetadataCriteria kiraClientMetadataCriteria, String deletedBy);

  HandleResult deleteKiraClientMetadata(KiraClientMetadataCriteria kiraClientMetadataCriteria,
      String deletedBy);

  int doDeleteKiraClientMetadata(KiraClientMetadataCriteria kiraClientMetadataCriteria,
      String deletedBy) throws Exception;

  boolean isPoolNeedAlarmAndAlarmReceiverSet(String poolId);
}
