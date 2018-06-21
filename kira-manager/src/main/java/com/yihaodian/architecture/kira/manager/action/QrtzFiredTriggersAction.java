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

import com.yihaodian.architecture.kira.manager.criteria.QrtzFiredTriggersCriteria;
import com.yihaodian.architecture.kira.manager.domain.QrtzFiredTriggers;
import com.yihaodian.architecture.kira.manager.service.QrtzFiredTriggersService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class QrtzFiredTriggersAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  private QrtzFiredTriggersCriteria criteria = new QrtzFiredTriggersCriteria();

  private transient QrtzFiredTriggersService qrtzFiredTriggersService;

  public QrtzFiredTriggersAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public QrtzFiredTriggersCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(QrtzFiredTriggersCriteria criteria) {
    this.criteria = criteria;
  }

  public void setQrtzFiredTriggersService(
      QrtzFiredTriggersService qrtzFiredTriggersService) {
    this.qrtzFiredTriggersService = qrtzFiredTriggersService;
  }

  public String list() throws Exception {
    List<QrtzFiredTriggers> qrtzFiredTriggersList = qrtzFiredTriggersService.list(criteria);
    Utils.sendHttpResponseForStruts2(criteria, qrtzFiredTriggersList);
    return null;
  }

  public String listOnPage() throws Exception {
    List<QrtzFiredTriggers> qrtzFiredTriggersList = qrtzFiredTriggersService.listOnPage(criteria);
    Utils.sendHttpResponseForStruts2(criteria, qrtzFiredTriggersList);
    return null;
  }

}
