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

import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.util.VerifyImg;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

public class LoginProcess extends BaseServlet {

  private static final long serialVersionUID = 1L;

  private static final String ldapConnUrlForNonProductionAndStaging = "";
  private static final String ldapConnUrlForProductionAndStaging = "";
  private static final String defaultPassWord =
      LoadProertesContainer.provider().getProperty("kira.server.test.loginPassword", "test123");
  private static final String defaultAccount =
      LoadProertesContainer.provider().getProperty("kira.server.test.account", "test");

  public void loginCheck(HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    String userName = req.getParameter("userName");
    String password = req.getParameter("password");

    try {
      if (StringUtils.trimToNull(userName) == null || StringUtils.trimToNull(password) == null) {
        data.put("success", false);
        data.put("msg", "用户名或者密码为空!");
      } else if (userName.equals(defaultAccount)  && password.equals(defaultPassWord)) {
        SecurityUtils.createUserContextDataInSession(req, userName);
        data.put("success", true);
        data.put("msg", "登录成功!");
      } else {
        //LDAP 登录验证
				/*
				LDAP ldap = new LDAP();
				String ldapConnUrl = null;
				boolean isProductionEnvironment = KiraManagerUtils.isProductionEnvironment();
				boolean isStagingEnvironment = KiraManagerUtils.isStagingEnvironment();
				String dns = "";
				if(isProductionEnvironment || isStagingEnvironment) {
					ldapConnUrl = ldapConnUrlForProductionAndStaging;
				} else {
					ldapConnUrl = ldapConnUrlForNonProductionAndStaging;
				}
				int pass = ldap.connect(userName, password, ldapConnUrl, dns);
				if(pass != 0){
					data.put("success", false);
					data.put("msg", "用户名或者密码错误!");
				}else{
					SecurityUtils.createUserContextDataInSession(req, userName);
					data.put("success", true);
					data.put("msg", "登录成功!");
				}*/
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    writeJsonData(data, resp);
  }

  public void logout(HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    SecurityUtils.deleteUserContextDataInSession(req);
    req.getRequestDispatcher("login.jsp").forward(req, resp);
  }

  public void verifyImg(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Map verifyMap = VerifyImg.createVerifyImg();
    String code = (String) verifyMap.get("code");
    BufferedImage buffImg = (BufferedImage) verifyMap.get("files");
    req.getSession().setAttribute("rand", code.toLowerCase());
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Cache-Control", "no-cache");
    resp.setDateHeader("Expires", 0);
    resp.setContentType("image/jpeg");
    ServletOutputStream sos = resp.getOutputStream();
    ImageIO.write(buffImg, "jpeg", sos);
    sos.flush();
    sos.close();
  }

}
