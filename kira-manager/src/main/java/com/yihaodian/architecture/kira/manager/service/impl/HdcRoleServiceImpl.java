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

import com.yihaodian.architecture.kira.manager.criteria.HdcRoleCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcRoleDao;
import com.yihaodian.architecture.kira.manager.domain.HdcRole;
import com.yihaodian.architecture.kira.manager.service.HdcRoleService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class HdcRoleServiceImpl extends Service implements HdcRoleService {

  private HdcRoleDao hdcRoleDao;

  public void setHdcRoleDao(HdcRoleDao hdcRoleDao) {
    this.hdcRoleDao = hdcRoleDao;
  }

  public void insert(HdcRole hdcRole) {
    hdcRoleDao.insert(hdcRole);
  }

  public int update(HdcRole hdcRole) {
    int actualRowsAffected = 0;

    int id = hdcRole.getId();

    HdcRole _oldHdcRole = hdcRoleDao.select(id);

    if (_oldHdcRole != null) {
      actualRowsAffected = hdcRoleDao.update(hdcRole);
    }

    return actualRowsAffected;
  }

  public int delete(int id) {
    int actualRowsAffected = 0;

    HdcRole _oldHdcRole = hdcRoleDao.select(id);

    if (_oldHdcRole != null) {
      actualRowsAffected = hdcRoleDao.delete(id);
    }

    return actualRowsAffected;
  }

  public HdcRole select(int id) {
    return hdcRoleDao.select(id);
  }

  public List<HdcRole> list(HdcRoleCriteria hdcRoleCriteria) {
    return hdcRoleDao.list(hdcRoleCriteria);
  }

  public List<HdcRole> listOnPage(HdcRoleCriteria hdcRoleCriteria) {
    return hdcRoleDao.listOnPage(hdcRoleCriteria);
  }

}
