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

import com.yihaodian.architecture.kira.manager.criteria.HdcPvgCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcPvgDao;
import com.yihaodian.architecture.kira.manager.domain.HdcPvg;
import com.yihaodian.architecture.kira.manager.service.HdcPvgService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class HdcPvgServiceImpl extends Service implements HdcPvgService {

  private HdcPvgDao hdcPvgDao;

  public void setHdcPvgDao(HdcPvgDao hdcPvgDao) {
    this.hdcPvgDao = hdcPvgDao;
  }

  public void insert(HdcPvg hdcPvg) {
    hdcPvgDao.insert(hdcPvg);
  }

  public int update(HdcPvg hdcPvg) {
    int actualRowsAffected = 0;

    int id = hdcPvg.getId();

    HdcPvg _oldHdcPvg = hdcPvgDao.select(id);

    if (_oldHdcPvg != null) {
      actualRowsAffected = hdcPvgDao.update(hdcPvg);
    }

    return actualRowsAffected;
  }

  public int delete(int id) {
    int actualRowsAffected = 0;

    HdcPvg _oldHdcPvg = hdcPvgDao.select(id);

    if (_oldHdcPvg != null) {
      actualRowsAffected = hdcPvgDao.delete(id);
    }

    return actualRowsAffected;
  }

  public HdcPvg select(int id) {
    return hdcPvgDao.select(id);
  }

  public List<HdcPvg> list(HdcPvgCriteria hdcPvgCriteria) {
    return hdcPvgDao.list(hdcPvgCriteria);
  }

  public List<HdcPvg> listOnPage(HdcPvgCriteria hdcPvgCriteria) {
    return hdcPvgDao.listOnPage(hdcPvgCriteria);
  }

}
