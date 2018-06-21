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

import com.yihaodian.architecture.kira.manager.criteria.HdcPvgCriteria;
import com.yihaodian.architecture.kira.manager.domain.HdcPvg;
import java.util.List;

public interface HdcPvgService {

  void insert(HdcPvg hdcPvg);

  int update(HdcPvg hdcPvg);

  int delete(int id);

  HdcPvg select(int id);

  List<HdcPvg> list(HdcPvgCriteria hdcPvgCriteria);

  List<HdcPvg> listOnPage(HdcPvgCriteria hdcPvgCriteria);

}
