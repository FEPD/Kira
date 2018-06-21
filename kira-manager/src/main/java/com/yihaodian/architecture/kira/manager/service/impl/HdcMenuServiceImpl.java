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

import com.yihaodian.architecture.kira.manager.criteria.HdcMenuCriteria;
import com.yihaodian.architecture.kira.manager.dao.HdcMenuDao;
import com.yihaodian.architecture.kira.manager.domain.HdcMenu;
import com.yihaodian.architecture.kira.manager.dto.MenuTreeVO;
import com.yihaodian.architecture.kira.manager.service.HdcMenuService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HdcMenuServiceImpl extends Service implements HdcMenuService {

  private HdcMenuDao hdcMenuDao;

  public void setHdcMenuDao(HdcMenuDao hdcMenuDao) {
    this.hdcMenuDao = hdcMenuDao;
  }

  public void insert(HdcMenu hdcMenu) {
    hdcMenuDao.insert(hdcMenu);
  }

  public int update(HdcMenu hdcMenu) {
    int actualRowsAffected = 0;

    int id = hdcMenu.getId();

    HdcMenu _oldHdcMenu = hdcMenuDao.select(id);

    if (_oldHdcMenu != null) {
      actualRowsAffected = hdcMenuDao.update(hdcMenu);
    }

    return actualRowsAffected;
  }

  public int delete(int id) {
    int actualRowsAffected = 0;

    HdcMenu _oldHdcMenu = hdcMenuDao.select(id);

    if (_oldHdcMenu != null) {
      actualRowsAffected = hdcMenuDao.delete(id);
    }

    return actualRowsAffected;
  }

  public HdcMenu select(int id) {
    return hdcMenuDao.select(id);
  }

  public List<HdcMenu> list(HdcMenuCriteria hdcMenuCriteria) {
    return hdcMenuDao.list(hdcMenuCriteria);
  }

  public List<HdcMenu> listOnPage(HdcMenuCriteria hdcMenuCriteria) {
    return hdcMenuDao.listOnPage(hdcMenuCriteria);
  }

  @Override
  public List<MenuTreeVO> getMenuTreeVOList() {
    //查出所有的菜单
    HdcMenuCriteria hdcMenuCriteria = new HdcMenuCriteria();
    List<HdcMenu> allMenus = hdcMenuDao.list(hdcMenuCriteria);

    Map<Integer, MenuTreeVO> menuMap = new HashMap<Integer, MenuTreeVO>();
    for (HdcMenu menu : allMenus) {
      Integer pid = Integer.valueOf(menu.getPid());
      Integer id = Integer.valueOf(menu.getId());
      MenuTreeVO vo = new MenuTreeVO(id, pid, menu.getName());
      if (null == menuMap.get(pid)) {
        vo.setExpanded(Boolean.TRUE);
        menuMap.put(menu.getId(), vo);
      } else {
        vo.setLeaf(true);
        vo.setUrl(menu.getUrl());
        menuMap.get(pid).addChildMenu(vo);
      }
    }

    List<MenuTreeVO> resultList = new ArrayList<MenuTreeVO>();
    for (Entry<Integer, MenuTreeVO> vo : menuMap.entrySet()) {
      resultList.add(vo.getValue());
    }

    return resultList;
  }

}
