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

import com.yihaodian.architecture.kira.manager.criteria.QrtzSimpleTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzSimpleTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzSimpleTriggers;
import com.yihaodian.architecture.kira.manager.service.QrtzSimpleTriggersService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class QrtzSimpleTriggersServiceImpl extends Service implements QrtzSimpleTriggersService {

  private QrtzSimpleTriggersDao qrtzSimpleTriggersDao;

  public void setQrtzSimpleTriggersDao(QrtzSimpleTriggersDao qrtzSimpleTriggersDao) {
    this.qrtzSimpleTriggersDao = qrtzSimpleTriggersDao;
  }

  public void insert(QrtzSimpleTriggers qrtzSimpleTriggers) {
    qrtzSimpleTriggersDao.insert(qrtzSimpleTriggers);
  }

  public int update(QrtzSimpleTriggers qrtzSimpleTriggers) {
    int actualRowsAffected = 0;

    String triggerGroup = qrtzSimpleTriggers.getTriggerGroup();
    String triggerName = qrtzSimpleTriggers.getTriggerName();

    QrtzSimpleTriggers _oldQrtzSimpleTriggers = qrtzSimpleTriggersDao
        .select(triggerGroup, triggerName);

    if (_oldQrtzSimpleTriggers != null) {
      actualRowsAffected = qrtzSimpleTriggersDao.update(qrtzSimpleTriggers);
    }

    return actualRowsAffected;
  }

  public int delete(String triggerGroup, String triggerName) {
    int actualRowsAffected = 0;

    QrtzSimpleTriggers _oldQrtzSimpleTriggers = qrtzSimpleTriggersDao
        .select(triggerGroup, triggerName);

    if (_oldQrtzSimpleTriggers != null) {
      actualRowsAffected = qrtzSimpleTriggersDao.delete(triggerGroup, triggerName);
    }

    return actualRowsAffected;
  }

  public QrtzSimpleTriggers select(String triggerGroup, String triggerName) {
    return qrtzSimpleTriggersDao.select(triggerGroup, triggerName);
  }

  public List<QrtzSimpleTriggers> list(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria) {
    return qrtzSimpleTriggersDao.list(qrtzSimpleTriggersCriteria);
  }

  public List<QrtzSimpleTriggers> listOnPage(
      QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria) {
    return qrtzSimpleTriggersDao.listOnPage(qrtzSimpleTriggersCriteria);
  }

}
