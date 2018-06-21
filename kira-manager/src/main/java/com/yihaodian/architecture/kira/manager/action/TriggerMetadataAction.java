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
package com.yihaodian.architecture.kira.manager.action;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.dto.KiraTimerTriggerBusinessRunningInstance;
import com.yihaodian.architecture.kira.manager.criteria.TriggerMetadataCriteria;
import com.yihaodian.architecture.kira.manager.domain.TriggerMetadata;
import com.yihaodian.architecture.kira.manager.dto.PoolTriggerStatus;
import com.yihaodian.architecture.kira.manager.dto.TriggerEnvironmentDetailData;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.TriggerMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class TriggerMetadataAction extends BaseAction {

  private static final long serialVersionUID = 7726591665598206347L;

  private transient TriggerMetadataService triggerMetadataService;

  private transient KiraClientMetadataService kiraClientMetadataService;

  private TriggerMetadataCriteria criteria = new TriggerMetadataCriteria();

  private TriggerMetadataUpdateContent triggerMetadata = new TriggerMetadataUpdateContent();

  private TriggerMetadataCreateContent newTriggerMetadata = new TriggerMetadataCreateContent();

  public TriggerMetadataAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public TriggerMetadataCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(TriggerMetadataCriteria criteria) {
    this.criteria = criteria;
  }

  public TriggerMetadataUpdateContent getTriggerMetadata() {
    return triggerMetadata;
  }

  public void setTriggerMetadata(TriggerMetadataUpdateContent triggerMetadata) {
    this.triggerMetadata = triggerMetadata;
  }

  public TriggerMetadataCreateContent getNewTriggerMetadata() {
    return newTriggerMetadata;
  }

  public void setNewTriggerMetadata(
      TriggerMetadataCreateContent newTriggerMetadata) {
    this.newTriggerMetadata = newTriggerMetadata;
  }

  public String listOnPage() throws Exception {
    criteria.setUnregistered(null);
    criteria.setDeleted(null);
    addInvisiblePoolListToCriteria();
    List<TriggerMetadata> triggerMetadataList = triggerMetadataService
        .listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadataList);
    return null;
  }

  public String listLatest() throws Exception {
    criteria.getPaging().setMaxResults(Integer.MAX_VALUE);
    List<TriggerMetadata> triggerMetadataList = triggerMetadataService.listLatest(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadataList);
    return null;
  }

  public String listLatestOnPage() throws Exception {
    addInvisiblePoolListToCriteria();

    List<TriggerMetadata> triggerMetadataList = triggerMetadataService.listLatestOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadataList);
    return null;
  }

  private void addInvisiblePoolListToCriteria() {
    UserContextData userContextData = SecurityUtils.getUserContextDataViaStruts2();
    List<String> invisiblePoolListForUser = kiraClientMetadataService
        .getInvisiblePoolListForUser(userContextData);
    criteria.setPoolIdListToExclude(invisiblePoolListForUser);
  }

  public String getAllPoolIdTriggerIdsMap() throws Exception {
    criteria.getPaging().setMaxResults(Integer.MAX_VALUE);
    Map<String, Set<String>> poolIdTriggerIdSetMap = triggerMetadataService
        .getPoolIdTriggerIdSetMapOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, poolIdTriggerIdSetMap);
    return null;
  }

  public String getAllPoolIdList() throws Exception {
    criteria.setUnregistered(null);
    criteria.setDeleted(null);
    addInvisiblePoolListToCriteria();
    List<String> allPoolIdList = triggerMetadataService.getPoolIdList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, allPoolIdList);
    return null;
  }

  public String getAllAvailablePoolIdList() throws Exception {
    addInvisiblePoolListToCriteria();
    List<String> allPoolIdList = triggerMetadataService.getPoolIdList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, allPoolIdList);
    return null;
  }

  public String getAllTriggerIdList() throws Exception {
    criteria.setUnregistered(null);
    criteria.setDeleted(null);
    addInvisiblePoolListToCriteria();
    List<String> allTriggerIdList = triggerMetadataService.getTriggerIdList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, allTriggerIdList);
    return null;
  }

  public String getAllAvailableTriggerIdList() throws Exception {
    addInvisiblePoolListToCriteria();
    List<String> allTriggerIdList = triggerMetadataService.getTriggerIdList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, allTriggerIdList);
    return null;
  }

  public String getPoolTriggerStatus() throws Exception {
    PoolTriggerStatus poolTriggerStatus = triggerMetadataService.getPoolTriggerStatus(criteria);
    Utils.sendHttpResponseForStruts2(criteria, poolTriggerStatus);
    return null;
  }

  public String getPoolTriggerStatusListOnPage() throws Exception {
    addInvisiblePoolListToCriteria();
    List<PoolTriggerStatus> poolTriggerStatusListOnPage = triggerMetadataService
        .getPoolTriggerStatusListOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, poolTriggerStatusListOnPage);
    return null;
  }

  public String getTriggerEnvironmentDetailDataListOnPage() throws Exception {
    List<TriggerEnvironmentDetailData> triggerEnvironmentDetailDataList = triggerMetadataService
        .getTriggerEnvironmentDetailDataList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerEnvironmentDetailDataList);
    return null;
  }

  public String updateTriggersAsUnRegistered() throws Exception {
    String appId = criteria.getAppId();
    String triggerId = criteria.getTriggerId();
    String version = criteria.getVersion();
    List<TriggerIdentity> triggerIdentityList = new ArrayList<TriggerIdentity>();
    TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId, version);
    triggerIdentityList.add(triggerIdentity);
    int updatedCount = triggerMetadataService
        .updateUnRegisteredStatusForTriggers(triggerIdentityList, Boolean.TRUE);
    Utils.sendHttpResponseForStruts2(criteria, updatedCount);
    return null;
  }

  public String rescheduleJob() throws Exception {
    HandleResult handleResult = triggerMetadataService.rescheduleJob(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String deleteTrigger() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    HandleResult handleResult = triggerMetadataService.deleteTrigger(criteria, userName);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String getTriggerMetadataById() throws Exception {
    TriggerMetadata triggerMetadata = triggerMetadataService
        .getTriggerMetadataById(criteria.getId());
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadata);
    return null;
  }

  public String getMisfireInstructionList() throws Exception {
    List<Integer> misfireInstructionList = triggerMetadataService
        .getMisfireInstructionList(criteria);
    Utils.sendHttpResponseForStruts2(null, misfireInstructionList);
    return null;
  }

  public String getTriggerTypeList() throws Exception {
    List<String> triggerTypeList = triggerMetadataService.getTriggerTypeList();
    Utils.sendHttpResponseForStruts2(null, triggerTypeList);
    return null;
  }

  public String updateTrigger() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    HandleResult handleResult = triggerMetadataService.updateTrigger(triggerMetadata, userName);
    Utils.sendHttpResponseForExtFormViaStruts2(Boolean.TRUE, handleResult);
    return null;
  }

  public String deleteTriggerEnvironment() throws Exception {
    HandleResult handleResult = triggerMetadataService.deleteTriggerEnvironment(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String manuallyRunJobByTriggerMetadata() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    HandleResult handleResult = triggerMetadataService
        .manuallyRunJobByTriggerMetadata(criteria, userName);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String createTrigger() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    HandleResult handleResult = triggerMetadataService.createTrigger(newTriggerMetadata, userName);
    Utils.sendHttpResponseForExtFormViaStruts2(Boolean.TRUE, handleResult);
    return null;
  }

  public String getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage() throws Exception {
    List<TriggerMetadata> triggerMetadataList = triggerMetadataService
        .getLatestTriggerMetadatasWhichSetTriggerAsTargetOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadataList);
    return null;
  }

  public String getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage() throws Exception {
    List<TriggerMetadata> triggerMetadataList = triggerMetadataService
        .getLatestTriggerMetadatasWhichSetTriggersOfPoolAsTargetOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, triggerMetadataList);
    return null;
  }

  public String getKiraTimerTriggerBusinessRunningInstanceListOnPage() throws Exception {
    List<KiraTimerTriggerBusinessRunningInstance> kiraTimerTriggerBusinessRunningInstanceList = triggerMetadataService
        .getKiraTimerTriggerBusinessRunningInstanceList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, kiraTimerTriggerBusinessRunningInstanceList);
    return null;
  }

}
