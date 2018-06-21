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

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class BaseServlet extends HttpServlet {

  protected static final String SUCCESS = "success";
  protected static final String MSG = "msg";
  private static final long serialVersionUID = 1L;
  protected Map data = new HashMap();

  public void writeJsonData(Object data, HttpServletResponse resp)
      throws IOException {
    writeData(JSONObject.toJSONString(data, true), resp);
  }

  public void writeData(Object data, HttpServletResponse resp)
      throws IOException {
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().print(data);
    resp.getWriter().flush();
    resp.getWriter().close();
  }
}