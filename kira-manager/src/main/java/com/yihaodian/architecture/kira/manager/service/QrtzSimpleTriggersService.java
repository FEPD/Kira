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
package com.yihaodian.architecture.kira.manager.service;

import com.yihaodian.architecture.kira.manager.criteria.QrtzSimpleTriggersCriteria;
import com.yihaodian.architecture.kira.manager.domain.QrtzSimpleTriggers;
import java.util.List;

public interface QrtzSimpleTriggersService {

  void insert(QrtzSimpleTriggers qrtzSimpleTriggers);

  int update(QrtzSimpleTriggers qrtzSimpleTriggers);

  int delete(String triggerGroup, String triggerName);

  QrtzSimpleTriggers select(String triggerGroup, String triggerName);

  List<QrtzSimpleTriggers> list(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria);

  List<QrtzSimpleTriggers> listOnPage(QrtzSimpleTriggersCriteria qrtzSimpleTriggersCriteria);

}
