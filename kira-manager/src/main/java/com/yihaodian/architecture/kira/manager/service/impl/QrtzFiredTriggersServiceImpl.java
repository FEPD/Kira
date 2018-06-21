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

import com.yihaodian.architecture.kira.manager.criteria.QrtzFiredTriggersCriteria;
import com.yihaodian.architecture.kira.manager.dao.QrtzFiredTriggersDao;
import com.yihaodian.architecture.kira.manager.domain.QrtzFiredTriggers;
import com.yihaodian.architecture.kira.manager.service.QrtzFiredTriggersService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class QrtzFiredTriggersServiceImpl extends Service implements QrtzFiredTriggersService {

  private QrtzFiredTriggersDao qrtzFiredTriggersDao;

  public void setQrtzFiredTriggersDao(QrtzFiredTriggersDao qrtzFiredTriggersDao) {
    this.qrtzFiredTriggersDao = qrtzFiredTriggersDao;
  }

  public void insert(QrtzFiredTriggers qrtzFiredTriggers) {
    qrtzFiredTriggersDao.insert(qrtzFiredTriggers);
  }

  public int update(QrtzFiredTriggers qrtzFiredTriggers) {
    int actualRowsAffected = 0;

    String entryId = qrtzFiredTriggers.getEntryId();

    QrtzFiredTriggers _oldQrtzFiredTriggers = qrtzFiredTriggersDao.select(entryId);

    if (_oldQrtzFiredTriggers != null) {
      actualRowsAffected = qrtzFiredTriggersDao.update(qrtzFiredTriggers);
    }

    return actualRowsAffected;
  }

  public int delete(String entryId) {
    int actualRowsAffected = 0;

    QrtzFiredTriggers _oldQrtzFiredTriggers = qrtzFiredTriggersDao.select(entryId);

    if (_oldQrtzFiredTriggers != null) {
      actualRowsAffected = qrtzFiredTriggersDao.delete(entryId);
    }

    return actualRowsAffected;
  }

  public QrtzFiredTriggers select(String entryId) {
    return qrtzFiredTriggersDao.select(entryId);
  }

  public List<QrtzFiredTriggers> list(QrtzFiredTriggersCriteria qrtzFiredTriggersCriteria) {
    return qrtzFiredTriggersDao.list(qrtzFiredTriggersCriteria);
  }

  public List<QrtzFiredTriggers> listOnPage(QrtzFiredTriggersCriteria qrtzFiredTriggersCriteria) {
    return qrtzFiredTriggersDao.listOnPage(qrtzFiredTriggersCriteria);
  }

}
