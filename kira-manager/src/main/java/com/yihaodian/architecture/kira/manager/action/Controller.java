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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller extends BaseServlet {

  protected static final Logger _log = LoggerFactory.getLogger(Controller.class);
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String method = req.getParameter("method");
      // 获取验证码
      if (method.equals("verifyImg")) {
        verifyImg(req, resp);
        return;
      }
      // 登录页面
      else if (method.equals("login")) {
        req.getRequestDispatcher("login.jsp").forward(req, resp);
        return;
      }
      // 登录检查
      else if (method.equals("loginCheck")) {
        loginCheck(req, resp);
        return;
      }
      // 登录权限控制
      if (!SecurityUtils.isUserAuthenticated(req)) {
        req.getRequestDispatcher("login.jsp").forward(req, resp);
        return;
      }
      // 主页
      else if (method.equals("index")) {
        req.getRequestDispatcher("index.jsp").forward(req, resp);
        return;
      }
      // 退出登录
      else if (method.equals("logout")) {
        logout(req, resp);
        return;
      }
    } catch (Exception e) {
      _log.error(e.getMessage(), e);
      data.put(SUCCESS, false);
      data.put(MSG, "系统发生了错误，请联系管理员！");
      writeJsonData(data, resp);
    }
  }

  public void logout(HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    LoginProcess login = new LoginProcess();
    login.logout(req, resp);
  }

  public void loginCheck(HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    LoginProcess login = new LoginProcess();
    login.loginCheck(req, resp);
  }

  public void verifyImg(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    LoginProcess login = new LoginProcess();
    login.verifyImg(req, resp);
  }

}
