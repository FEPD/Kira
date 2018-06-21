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
package com.yihaodian.architecture.kira.manager.action;

import com.yihaodian.architecture.kira.manager.criteria.HdcMenuCriteria;
import com.yihaodian.architecture.kira.manager.dto.MenuTreeVO;
import com.yihaodian.architecture.kira.manager.service.HdcMenuService;
import com.yihaodian.architecture.kira.manager.util.Utils;
import java.util.List;

public class MenuAction extends BaseAction {

  private static final long serialVersionUID = -5306165704792042514L;

  private transient HdcMenuService hdcMenuService;

  private HdcMenuCriteria criteria = new HdcMenuCriteria();

  public MenuAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void setHdcMenuService(HdcMenuService hdcMenuService) {
    this.hdcMenuService = hdcMenuService;
  }

  public HdcMenuCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(HdcMenuCriteria criteria) {
    this.criteria = criteria;
  }

  public String getMenuTree() throws Exception {
    List<MenuTreeVO> list = hdcMenuService.getMenuTreeVOList();
    Utils.sendHttpResponseForStruts2(null, list);
    return null;
  }

}
