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
package com.yihaodian.architecture.hedwig.provider;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.yihaodian.architecture.hedwig.common.exception.AuthenticationException;
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianOutput;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Archer
 */
public class BasicAuthWebserviceExporter extends HedwigWebserviceExporter {

  private String user;
  private String password;
  private String basicAuth;

  @Override
  public void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (!postCheck(request, response)) {
      return;
    }
    if (priviledgeCheck(request, response)) {
      super.handleRequest(request, response);
    } else {
      AbstractHessianOutput out = new HedwigHessianOutput(response.getOutputStream());
      try {
        if (this.getSerializerFactory() != null) {
          out.setSerializerFactory(this.getSerializerFactory());
          Exception e = new AuthenticationException(
              "Authentication failed can't access service!!!");
          out.startReply();
          out.writeFault(HttpServletResponse.SC_FORBIDDEN + "",
              "There was an error with your User/Password combination.", e);
          out.completeReply();
        }
      } finally {
        out.close();
      }
    }
  }

  private boolean priviledgeCheck(HttpServletRequest request, HttpServletResponse response) {
    boolean value = true;
    if (!HedwigUtil.isBlankString(this.user) || !HedwigUtil.isBlankString(this.password)) {
      String authKey = request.getHeader("Authorization");
      if (authKey == null || !basicAuth.equals(authKey)) {
        value = false;
      }
    }
    return value;
  }

  @Override
  public void afterPropertiesSet() {
    this.user = this.user == null ? getAppProfile().getUser() : this.user;
    this.password = this.password == null ? getAppProfile().getPassword() : this.password;
    if (!HedwigUtil.isBlankString(this.user) || !HedwigUtil.isBlankString(this.password)) {
      String enStr = this.user + ":" + this.password;
      basicAuth = "Basic " + new String(Base64.encodeBase64(enStr.getBytes()));
    }
    super.afterPropertiesSet();
  }

  public void setUserName(String user) {
    if (!HedwigUtil.isBlankString(user)) {
      this.user = user;
    }
  }

  public void setPassword(String password) {
    if (!HedwigUtil.isBlankString(password)) {
      this.password = password;
    }

  }

}
