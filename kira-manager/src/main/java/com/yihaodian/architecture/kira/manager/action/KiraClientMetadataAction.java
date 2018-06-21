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
import com.yihaodian.architecture.kira.manager.criteria.KiraClientMetadataCriteria;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class KiraClientMetadataAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private KiraClientMetadataCriteria criteria = new KiraClientMetadataCriteria();

  private KiraClientMetadataUpdateContent kiraClientMetadata = new KiraClientMetadataUpdateContent();

  private KiraClientMetadataCreateContent newKiraClientMetadata = new KiraClientMetadataCreateContent();

  private KiraClientMetadataService kiraClientMetadataService;

  public KiraClientMetadataAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public KiraClientMetadataCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(KiraClientMetadataCriteria criteria) {
    this.criteria = criteria;
  }

  public KiraClientMetadataService getKiraClientMetadataService() {
    return kiraClientMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public KiraClientMetadataUpdateContent getKiraClientMetadata() {
    return kiraClientMetadata;
  }

  public void setKiraClientMetadata(
      KiraClientMetadataUpdateContent kiraClientMetadata) {
    this.kiraClientMetadata = kiraClientMetadata;
  }

  public KiraClientMetadataCreateContent getNewKiraClientMetadata() {
    return newKiraClientMetadata;
  }

  public void setNewKiraClientMetadata(
      KiraClientMetadataCreateContent newKiraClientMetadata) {
    this.newKiraClientMetadata = newKiraClientMetadata;
  }

  public String listOnPage() throws Exception {
    addInvisiblePoolListToCriteria();
    List<KiraClientMetadata> kiraClientMetadataList = kiraClientMetadataService
        .listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, kiraClientMetadataList);
    return null;
  }

  public String getAllPoolIdList() throws Exception {
    addInvisiblePoolListToCriteria();
    List<String> allPoolIdList = kiraClientMetadataService.getPoolIdList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, allPoolIdList);
    return null;
  }

  public String getKiraClientMetadataById() throws Exception {
    KiraClientMetadata kiraClientMetadata = kiraClientMetadataService
        .getKiraClientMetadataById(criteria.getId());
    Utils.sendHttpResponseForStruts2(criteria, kiraClientMetadata);
    return null;
  }

  public String updateKiraClientMetadata() throws Exception {
    HandleResult handleResult = kiraClientMetadataService
        .updateKiraClientMetadata(kiraClientMetadata);
    Utils.sendHttpResponseForExtFormViaStruts2(Boolean.TRUE, handleResult);
    return null;
  }

  private void addInvisiblePoolListToCriteria() {
    UserContextData userContextData = SecurityUtils.getUserContextDataViaStruts2();
    List<String> invisiblePoolListForUser = kiraClientMetadataService
        .getInvisiblePoolListForUser(userContextData);
    criteria.setPoolIdListToExclude(invisiblePoolListForUser);
  }

  public String createKiraClientMetadata() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    newKiraClientMetadata.setManuallyCreated(Boolean.TRUE);
    newKiraClientMetadata.setManuallyCreatedBy(userName);
    HandleResult handleResult = kiraClientMetadataService
        .createKiraClientMetadata(newKiraClientMetadata);
    Utils.sendHttpResponseForExtFormViaStruts2(Boolean.TRUE, handleResult);
    return null;
  }

  public String deleteKiraClientMetadata() throws Exception {
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    HandleResult handleResult = kiraClientMetadataService
        .deleteKiraClientMetadata(criteria, userName);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

}
