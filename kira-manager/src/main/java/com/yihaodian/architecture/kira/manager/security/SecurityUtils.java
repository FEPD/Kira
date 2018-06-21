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
package com.yihaodian.architecture.kira.manager.security;

import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.ServletActionContext;

public class SecurityUtils {

  public SecurityUtils() {
    // TODO Auto-generated constructor stub
  }

  public static boolean isUserAuthenticated(HttpServletRequest httpServletRequest) {
    boolean returnValue = false;
    UserContextData userContextData = (UserContextData) httpServletRequest.getSession()
        .getAttribute(KiraServerConstants.SESSION_ATTRIBUTE_KEY_USERCONTEXTDATA);
    if (null != userContextData) {
      returnValue = true;
    }
    return returnValue;
  }

  public static void createUserContextDataInSession(HttpServletRequest httpServletRequest,
      String userName) {
    UserContextData userContextData = new UserContextData();
    userContextData.setUserName(userName);
    httpServletRequest.getSession()
        .setAttribute(KiraServerConstants.SESSION_ATTRIBUTE_KEY_USERCONTEXTDATA, userContextData);
  }

  public static void deleteUserContextDataInSession(HttpServletRequest httpServletRequest) {
    httpServletRequest.getSession()
        .removeAttribute(KiraServerConstants.SESSION_ATTRIBUTE_KEY_USERCONTEXTDATA);
  }

  public static UserContextData getUserContextDataViaStruts2() {
    HttpServletRequest httpServletRequest = ServletActionContext.getRequest();
    UserContextData userContextData = getUserContextData(httpServletRequest);
    return userContextData;
  }

  public static UserContextData getUserContextData(HttpServletRequest httpServletRequest) {
    UserContextData userContextData = (UserContextData) httpServletRequest.getSession()
        .getAttribute(KiraServerConstants.SESSION_ATTRIBUTE_KEY_USERCONTEXTDATA);
    return userContextData;
  }

  public static String getUserNameViaStruts2() {
    String returnValue = null;
    HttpServletRequest httpServletRequest = ServletActionContext.getRequest();
    returnValue = getUserName(httpServletRequest);
    return returnValue;
  }

  public static String getUserName(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    UserContextData userContextData = getUserContextData(httpServletRequest);
    if (null != userContextData) {
      returnValue = userContextData.getUserName();
    }
    return returnValue;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
