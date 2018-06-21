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
import com.yihaodian.architecture.kira.manager.criteria.KiraServerCriteria;
import com.yihaodian.architecture.kira.manager.dto.KiraServerDetailData;
import com.yihaodian.architecture.kira.manager.service.KiraServerService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class KiraServerAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private KiraServerCriteria criteria = new KiraServerCriteria();

  private transient KiraServerService kiraServerService;

  public KiraServerAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public KiraServerCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(KiraServerCriteria criteria) {
    this.criteria = criteria;
  }

  public KiraServerService getKiraServerService() {
    return kiraServerService;
  }

  public void setKiraServerService(KiraServerService kiraServerService) {
    this.kiraServerService = kiraServerService;
  }

  //start admin usage

  public String getKiraServerDetailDataList() throws Exception {
    List<KiraServerDetailData> kiraServerDetailDataList = kiraServerService
        .getKiraServerDetailDataList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, kiraServerDetailDataList);
    return null;
  }

  public String startServer() throws Exception {
    HandleResult handleResult = kiraServerService.startServer(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String stopServer() throws Exception {
    HandleResult handleResult = kiraServerService.stopServer(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String restartServer() throws Exception {
    HandleResult handleResult = kiraServerService.restartServer(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String destroyServer() throws Exception {
    HandleResult handleResult = kiraServerService.destroyServer(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String recoverServer() throws Exception {
    HandleResult handleResult = kiraServerService.recoverServer(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String addServerIdToAssignedServerIdBlackList() throws Exception {
    HandleResult handleResult = kiraServerService.addServerIdToAssignedServerIdBlackList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String removeServerIdFromAssignedServerIdBlackList() throws Exception {
    HandleResult handleResult = kiraServerService
        .removeServerIdFromAssignedServerIdBlackList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  public String setAssignedServerIdBlackList() throws Exception {
    HandleResult handleResult = kiraServerService.setAssignedServerIdBlackList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

  //end admin usage

  public String clearAssignedServerIdBlackList() throws Exception {
    HandleResult handleResult = kiraServerService.clearAssignedServerIdBlackList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, handleResult);
    return null;
  }

}
