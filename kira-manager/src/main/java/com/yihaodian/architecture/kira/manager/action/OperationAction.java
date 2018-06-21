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

import com.yihaodian.architecture.kira.manager.criteria.OperationCriteria;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.service.OperationService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class OperationAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private transient OperationService operationService;

  private OperationCriteria criteria = new OperationCriteria();

  public OperationAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setOperationService(OperationService operationService) {
    this.operationService = operationService;
  }

  public OperationCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(OperationCriteria criteria) {
    this.criteria = criteria;
  }

  public String list() throws Exception {
    List<Operation> operationList = operationService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, operationList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<Operation> operationList = operationService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, operationList);
    return null;
  }

  public String getAllNotReadonlyOperations() throws Exception {
    List<Operation> allNotReadonlyOperations = operationService.getAllNotReadonlyOperations();
    Utils.sendHttpResponseForStruts2(criteria, allNotReadonlyOperations);
    return null;
  }

}
