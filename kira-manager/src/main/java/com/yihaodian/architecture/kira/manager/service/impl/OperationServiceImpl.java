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

import com.yihaodian.architecture.kira.manager.criteria.OperationCriteria;
import com.yihaodian.architecture.kira.manager.dao.OperationDao;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.service.OperationService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.OperationTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class OperationServiceImpl extends Service implements OperationService {

  private OperationDao operationDao;

  public void setOperationDao(OperationDao operationDao) {
    this.operationDao = operationDao;
  }

  public void insert(Operation operation) {
    operationDao.insert(operation);
  }

  public int update(Operation operation) {
    int actualRowsAffected = 0;

    Integer id = operation.getId();

    Operation _oldOperation = operationDao.select(id);

    if (_oldOperation != null) {
      actualRowsAffected = operationDao.update(operation);
    }

    return actualRowsAffected;
  }

  public int delete(Integer id) {
    int actualRowsAffected = 0;

    Operation _oldOperation = operationDao.select(id);

    if (_oldOperation != null) {
      actualRowsAffected = operationDao.delete(id);
    }

    return actualRowsAffected;
  }

  public Operation select(Integer id) {
    return operationDao.select(id);
  }

  public List<Operation> list(OperationCriteria operationCriteria) {
    return operationDao.list(operationCriteria);
  }

  public List<Operation> listOnPage(OperationCriteria operationCriteria) {
    return operationDao.listOnPage(operationCriteria);
  }

  @Override
  public List<Operation> getAllNotReadonlyOperations() {
    List<Operation> returnValue = new ArrayList<Operation>();
    try {
      OperationCriteria operationCriteria = new OperationCriteria();
      operationCriteria.setType(OperationTypeEnum.NOTREADONLY.getType());
      List<Operation> allNotReadonlyOperations = operationDao.list(operationCriteria);
      if (!CollectionUtils.isEmpty(allNotReadonlyOperations)) {
        returnValue.addAll(allNotReadonlyOperations);
      }
    } catch (Exception e) {
      logger.error("Error occurs for getAllNotReadonlyOperations.", e);
    }

    return returnValue;
  }

  @Override
  public List<Operation> getAllOperations() {
    List<Operation> returnValue = new ArrayList<Operation>();
    try {
      OperationCriteria operationCriteria = new OperationCriteria();
      List<Operation> allOperations = operationDao.list(operationCriteria);
      if (!CollectionUtils.isEmpty(allOperations)) {
        returnValue.addAll(allOperations);
      }
    } catch (Exception e) {
      logger.error("Error occurs for getAllOperations.", e);
    }
    return returnValue;
  }
}
