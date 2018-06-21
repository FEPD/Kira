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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.kira.manager.criteria.QrtzTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzTriggers;
import com.yihaodian.architecture.kira.manager.service.QrtzTriggersService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class QrtzTriggersServiceImpl extends Service implements QrtzTriggersService {

  private QrtzTriggersDao qrtzTriggersDao;

  public void setQrtzTriggersDao(QrtzTriggersDao qrtzTriggersDao) {
    this.qrtzTriggersDao = qrtzTriggersDao;
  }

  public void insert(QrtzTriggers qrtzTriggers) {
    qrtzTriggersDao.insert(qrtzTriggers);
  }

  public int update(QrtzTriggers qrtzTriggers) {
    int actualRowsAffected = 0;

    String triggerGroup = qrtzTriggers.getTriggerGroup();
    String triggerName = qrtzTriggers.getTriggerName();

    QrtzTriggers _oldQrtzTriggers = qrtzTriggersDao.select(triggerGroup, triggerName);

    if (_oldQrtzTriggers != null) {
      actualRowsAffected = qrtzTriggersDao.update(qrtzTriggers);
    }

    return actualRowsAffected;
  }

  public int delete(String triggerGroup, String triggerName) {
    int actualRowsAffected = 0;

    QrtzTriggers _oldQrtzTriggers = qrtzTriggersDao.select(triggerGroup, triggerName);

    if (_oldQrtzTriggers != null) {
      actualRowsAffected = qrtzTriggersDao.delete(triggerGroup, triggerName);
    }

    return actualRowsAffected;
  }

  public QrtzTriggers select(String triggerGroup, String triggerName) {
    return qrtzTriggersDao.select(triggerGroup, triggerName);
  }

  public List<QrtzTriggers> list(QrtzTriggersCriteria qrtzTriggersCriteria) {
    return qrtzTriggersDao.list(qrtzTriggersCriteria);
  }

  public List<QrtzTriggers> listOnPage(QrtzTriggersCriteria qrtzTriggersCriteria) {
    return qrtzTriggersDao.listOnPage(qrtzTriggersCriteria);
  }

  @Override
  public int count(QrtzTriggersCriteria qrtzTriggersCriteria) {
    return qrtzTriggersDao.count(qrtzTriggersCriteria);
  }

  @Override
  public List<QrtzTriggers> listCanBeScheduled(
      QrtzTriggersCriteria qrtzTriggersCriteria) {
    return qrtzTriggersDao.listCanBeScheduled(qrtzTriggersCriteria);
  }
}
