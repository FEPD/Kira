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

import com.yihaodian.architecture.kira.manager.criteria.QrtzTriggersCriteria;
import com.yihaodian.architecture.kira.manager.domain.QrtzTriggers;
import com.yihaodian.architecture.kira.manager.service.QrtzTriggersService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class QrtzTriggersAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private QrtzTriggersCriteria criteria = new QrtzTriggersCriteria();

  private transient QrtzTriggersService qrtzTriggersService;

  public QrtzTriggersAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public QrtzTriggersCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(QrtzTriggersCriteria criteria) {
    this.criteria = criteria;
  }

  public void setQrtzTriggersService(QrtzTriggersService qrtzTriggersService) {
    this.qrtzTriggersService = qrtzTriggersService;
  }

  public String list() throws Exception {
    List<QrtzTriggers> qrtzTriggersList = qrtzTriggersService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, qrtzTriggersList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<QrtzTriggers> qrtzTriggersList = qrtzTriggersService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, qrtzTriggersList);
    return null;
  }

}
