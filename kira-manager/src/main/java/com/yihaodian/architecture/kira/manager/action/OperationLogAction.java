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

import com.yihaodian.architecture.kira.manager.criteria.OperationLogCriteria;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import com.yihaodian.architecture.kira.manager.service.OperationLogService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class OperationLogAction extends BaseAction {

  private static final long serialVersionUID = 1L;
  private OperationLogCriteria criteria = new OperationLogCriteria();

  private transient OperationLogService operationLogService;

  public OperationLogAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public OperationLogCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(OperationLogCriteria criteria) {
    this.criteria = criteria;
  }

  public void setOperationLogService(OperationLogService operationLogService) {
    this.operationLogService = operationLogService;
  }

  public String list() throws Exception {
    List<OperationLog> operationLogList = operationLogService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, operationLogList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<OperationLog> operationLogList = operationLogService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, operationLogList);
    return null;
  }

  public String getOperationLogDetailDataListOnPage() throws Exception {
    List<OperationLog> operationLogDetailDataListOnPage = operationLogService
        .getOperationLogDetailDataListOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, operationLogDetailDataListOnPage);
    return null;
  }

}
