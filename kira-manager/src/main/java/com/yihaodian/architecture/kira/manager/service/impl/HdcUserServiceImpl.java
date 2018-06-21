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

import com.yihaodian.architecture.kira.manager.criteria.HdcUserCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcUserDao;
import com.yihaodian.architecture.kira.manager.domain.HdcUser;
import com.yihaodian.architecture.kira.manager.service.HdcUserService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class HdcUserServiceImpl extends Service implements HdcUserService {

  private HdcUserDao hdcUserDao;

  public void setHdcUserDao(HdcUserDao hdcUserDao) {
    this.hdcUserDao = hdcUserDao;
  }

  public void insert(HdcUser hdcUser) {
    hdcUserDao.insert(hdcUser);
  }

  public int update(HdcUser hdcUser) {
    int actualRowsAffected = 0;

    int id = hdcUser.getId();

    HdcUser _oldHdcUser = hdcUserDao.select(id);

    if (_oldHdcUser != null) {
      actualRowsAffected = hdcUserDao.update(hdcUser);
    }

    return actualRowsAffected;
  }

  public int delete(int id) {
    int actualRowsAffected = 0;

    HdcUser _oldHdcUser = hdcUserDao.select(id);

    if (_oldHdcUser != null) {
      actualRowsAffected = hdcUserDao.delete(id);
    }

    return actualRowsAffected;
  }

  public HdcUser select(int id) {
    return hdcUserDao.select(id);
  }

  public List<HdcUser> list(HdcUserCriteria hdcUserCriteria) {
    return hdcUserDao.list(hdcUserCriteria);
  }

  public List<HdcUser> listOnPage(HdcUserCriteria hdcUserCriteria) {
    return hdcUserDao.listOnPage(hdcUserCriteria);
  }

}
