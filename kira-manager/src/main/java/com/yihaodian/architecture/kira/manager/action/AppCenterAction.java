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

import com.yihaodian.architecture.kira.manager.criteria.AppCenterCriteria;
import com.yihaodian.architecture.kira.manager.dto.AppCenterDetailData;
import com.yihaodian.architecture.kira.manager.service.AppCenterService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class AppCenterAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private AppCenterCriteria criteria = new AppCenterCriteria();

  private transient AppCenterService appCenterService;

  public AppCenterAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public AppCenterCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(AppCenterCriteria criteria) {
    this.criteria = criteria;
  }

  public AppCenterService getAppCenterService() {
    return appCenterService;
  }

  public void setAppCenterService(AppCenterService appCenterService) {
    this.appCenterService = appCenterService;
  }

  public String getAppCenterDetailDataList() throws Exception {
    List<AppCenterDetailData> appCenterDetailDataList = appCenterService
        .getAppCenterDetailDataList(criteria);
    Utils.sendHttpResponseForStruts2(criteria, appCenterDetailDataList);
    return null;
  }

}
