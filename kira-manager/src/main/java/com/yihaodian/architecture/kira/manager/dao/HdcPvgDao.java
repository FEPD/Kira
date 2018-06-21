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

import com.yihaodian.architecture.kira.manager.criteria.HdcPvgCriteria;
import com.yihaodian.architecture.kira.manager.domain.HdcPvg;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface HdcPvgDao {

  void insert(HdcPvg hdcPvg) throws DataAccessException;

  int update(HdcPvg hdcPvg) throws DataAccessException;

  int delete(Integer id) throws DataAccessException;

  HdcPvg select(Integer id) throws DataAccessException;

  List<HdcPvg> list(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException;

  List<HdcPvg> listOnPage(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException;

  int count(HdcPvgCriteria hdcPvgCriteria) throws DataAccessException;

}
