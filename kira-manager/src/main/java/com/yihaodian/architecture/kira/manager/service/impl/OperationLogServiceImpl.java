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

import com.yihaodian.architecture.kira.manager.criteria.OperationLogCriteria;
import com.yihaodian.architecture.kira.manager.dao.OperationLogDao;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import com.yihaodian.architecture.kira.manager.service.OperationLogService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class OperationLogServiceImpl extends Service implements OperationLogService {

  private OperationLogDao operationLogDao;

  public void setOperationLogDao(OperationLogDao operationLogDao) {
    this.operationLogDao = operationLogDao;
  }

  public void insert(OperationLog operationLog) {
    operationLogDao.insert(operationLog);
  }

  public int update(OperationLog operationLog) {
    int actualRowsAffected = 0;

    String id = operationLog.getId();

    OperationLog _oldOperationLog = operationLogDao.select(id);

    if (_oldOperationLog != null) {
      actualRowsAffected = operationLogDao.update(operationLog);
    }

    return actualRowsAffected;
  }

  public int delete(String id) {
    int actualRowsAffected = 0;

    OperationLog _oldOperationLog = operationLogDao.select(id);

    if (_oldOperationLog != null) {
      actualRowsAffected = operationLogDao.delete(id);
    }

    return actualRowsAffected;
  }

  public OperationLog select(String id) {
    return operationLogDao.select(id);
  }

  public List<OperationLog> list(OperationLogCriteria operationLogCriteria) {
    return operationLogDao.list(operationLogCriteria);
  }

  public List<OperationLog> listOnPage(OperationLogCriteria operationLogCriteria) {
    return operationLogDao.listOnPage(operationLogCriteria);
  }

  @Override
  public List<OperationLog> getOperationLogDetailDataListOnPage(
      OperationLogCriteria operationLogCriteria) {
    return operationLogDao.getOperationLogDetailDataListOnPage(operationLogCriteria);
  }

}
