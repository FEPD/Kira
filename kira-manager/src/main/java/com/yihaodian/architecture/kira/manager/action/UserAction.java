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

import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import com.yihaodian.architecture.kira.manager.util.Utils;

public class UserAction extends BaseAction {

  private static final long serialVersionUID = 1L;

  public UserAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getLoginUserContextData() throws Exception {
    UserContextData userContextData = SecurityUtils.getUserContextDataViaStruts2();
    Utils.sendHttpResponseForStruts2(null, userContextData);
    return null;
  }

}
