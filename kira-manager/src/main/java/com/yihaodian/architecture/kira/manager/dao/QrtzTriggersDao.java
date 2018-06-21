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

import com.yihaodian.architecture.kira.manager.criteria.QrtzTriggersCriteria;
import com.yihaodian.architecture.kira.manager.domain.QrtzTriggers;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface QrtzTriggersDao {

  void insert(QrtzTriggers qrtzTriggers) throws DataAccessException;

  int update(QrtzTriggers qrtzTriggers) throws DataAccessException;

  int delete(String triggerGroup, String triggerName) throws DataAccessException;

  QrtzTriggers select(String triggerGroup, String triggerName) throws DataAccessException;

  List<QrtzTriggers> list(QrtzTriggersCriteria qrtzTriggersCriteria) throws DataAccessException;

  List<QrtzTriggers> listOnPage(QrtzTriggersCriteria qrtzTriggersCriteria)
      throws DataAccessException;

  int count(QrtzTriggersCriteria qrtzTriggersCriteria) throws DataAccessException;

  List<QrtzTriggers> listCanBeScheduled(QrtzTriggersCriteria qrtzTriggersCriteria)
      throws DataAccessException;

}
