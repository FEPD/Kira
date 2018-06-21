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
package com.yihaodian.architecture.kira.manager.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.Utils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;
  private static Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);

  public SecurityInterceptor() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    logger.info("init for SecurityInterceptor.");
  }

  @Override
  public void destroy() {
    logger.info("destroy for SecurityInterceptor.");
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String returnValue = Action.LOGIN;
    ActionContext actionContext = invocation.getInvocationContext();
    HttpServletRequest httpServletRequest = (HttpServletRequest) actionContext
        .get(ServletActionContext.HTTP_REQUEST);
    boolean isUserAuthenticated = SecurityUtils.isUserAuthenticated(httpServletRequest);
    if (isUserAuthenticated) {
      returnValue = invocation.invoke();
    } else {
      boolean isHttpRequestAnAjaxCall = Utils.isHttpRequestAnAjaxCall(httpServletRequest);
      if (isHttpRequestAnAjaxCall) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) actionContext
            .get(ServletActionContext.HTTP_RESPONSE);
        httpServletResponse.sendError(KiraServerConstants.HTTP_STATUS_CODE_NOT_AUTHENTICATED,
            KiraServerConstants.HTTP_STATUS_TEXT_NOT_AUTHENTICATED);
        returnValue = Action.NONE;
      }
    }
    return returnValue;
  }

}
