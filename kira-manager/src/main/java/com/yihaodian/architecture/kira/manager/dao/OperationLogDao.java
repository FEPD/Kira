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
package com.yihaodian.architecture.kira.manager.dao;

import com.yihaodian.architecture.kira.manager.criteria.OperationLogCriteria;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface OperationLogDao {

  void insert(OperationLog operationLog) throws DataAccessException;

  int update(OperationLog operationLog) throws DataAccessException;

  int delete(String id) throws DataAccessException;

  OperationLog select(String id) throws DataAccessException;

  List<OperationLog> list(OperationLogCriteria operationLogCriteria) throws DataAccessException;

  List<OperationLog> listOnPage(OperationLogCriteria operationLogCriteria)
      throws DataAccessException;

  int count(OperationLogCriteria operationLogCriteria) throws DataAccessException;

  List<OperationLog> getOperationLogDetailDataListOnPage(OperationLogCriteria operationLogCriteria)
      throws DataAccessException;

  int countJobDetailDataList(OperationLogCriteria operationLogCriteria) throws DataAccessException;

}
