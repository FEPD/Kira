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
package com.yihaodian.architecture.kira.manager.util;

import com.alibaba.fastjson.JSON;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import com.yihaodian.architecture.kira.manager.dto.ManuallyReRunJobResult;
import com.yihaodian.architecture.kira.manager.dto.UpdateTriggerResult;
import com.yihaodian.architecture.kira.manager.web.HttpRequestContextHolder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static Logger logger = LoggerFactory.getLogger(Utils.class);

  public Utils() {
    // TODO Auto-generated constructor stub
  }

  public static void sendHttpResponseForStruts2(Object contextData, Object resultData)
      throws IOException {
    Utils.logOperationIfNeeded(contextData, resultData);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("contextData", contextData);
    map.put("resultData", resultData);
    String jsonString = Utils.toJsonString(map);
    Utils.sendHttpResponseForStruts2(jsonString);
  }

  public static void sendHttpResponseForExtFormViaStruts2(Boolean success, Object resultData)
      throws IOException {
    Utils.logOperationIfNeeded(success, resultData);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("success", success);
    map.put("data", resultData);
    String jsonString = Utils.toJsonString(map);
    Utils.sendHttpResponseForStruts2(jsonString);
  }

  private static void logOperationIfNeeded(Object contextData, Object resultObject) {
    try {
      HttpServletRequest httpServletRequest = ServletActionContext.getRequest();
      HttpServletResponse httpServletResponse = ServletActionContext.getResponse();
      String servletPath = httpServletRequest.getServletPath();
      String requestUrl = WebUtil.getRequestFullUrl(httpServletRequest);
      if (!StringUtils.isBlank(servletPath)) {
        Map<String, Operation> nameNotReadonlyOperationMap = KiraManagerDataCenter
            .getNameNotReadonlyOperationMap();
        Operation operation = nameNotReadonlyOperationMap.get(servletPath);
        if (null == operation) {
          logger.info("Will not log operation for requestUrl={} and servletPath={}", requestUrl,
              servletPath);
        } else {
          OperationLog operationLog = new OperationLog();
          operationLog.setOperationId(operation.getId());
          if (null != resultObject) {
            StringBuilder sb = new StringBuilder();
            if (resultObject instanceof HandleResult) {
              String resultCode = ((HandleResult) resultObject).getResultCode();
              String resultData = ((HandleResult) resultObject).getResultData();
              operationLog.setResultCode(resultCode);
              if (StringUtils.isNotBlank(resultData)) {
                sb.append("resultData: ").append(resultData);
              }
            }

            if (resultObject instanceof ManuallyReRunJobResult) {
              String oldJobId = ((ManuallyReRunJobResult) resultObject).getOldJobId();
              String newJobId = ((ManuallyReRunJobResult) resultObject).getNewJobId();
              if (StringUtils.isNotBlank(oldJobId)) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("oldJobId: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(oldJobId);
              }
              if (StringUtils.isNotBlank(newJobId)) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("newJobId: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(newJobId);
              }
            }

            if (resultObject instanceof UpdateTriggerResult) {
              Long oldId = ((UpdateTriggerResult) resultObject).getOldId();
              if (null != oldId) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("oldId: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(oldId);
              }
              String oldVersion = ((UpdateTriggerResult) resultObject).getOldVersion();
              if (StringUtils.isNotBlank(oldVersion)) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("oldVersion: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(oldVersion);
              }

              Long newId = ((UpdateTriggerResult) resultObject).getNewId();
              if (null != newId) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("newId: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(newId);
              }

              String newVersion = ((UpdateTriggerResult) resultObject).getNewVersion();
              if (StringUtils.isNotBlank(newVersion)) {
                sb.append(KiraCommonConstants.LINE_SEPARATOR).append("newVersion: ")
                    .append(KiraCommonConstants.LINE_SEPARATOR).append(newVersion);
              }
            }
            operationLog.setResultDetails(KiraCommonUtils.toString(sb.toString()));
          }

          HttpRequestContextHolder.setOperationLog(operationLog);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs for logOperationIfNeeded. contextData=" + KiraCommonUtils
          .toString(contextData) + " and resultData=" + KiraCommonUtils.toString(resultObject), e);
    }
  }

  public static void sendHttpResponseForStruts2(Map<String, Object> map) throws IOException {
    String jsonString = Utils.toJsonString(map);
    Utils.sendHttpResponseForStruts2(jsonString);
  }

  public static void sendHttpResponseForStruts2(String responseString) throws IOException {
    HttpServletResponse response = ServletActionContext.getResponse();
    response.setContentType(KiraServerConstants.HTTP_CONTENT_TYPE_APPLICATION_JSON);
    response.getWriter().write(responseString);
    response.getWriter().flush();
  }

  private static String toJsonString(Map<String, Object> map) {
    String jsonString = JSON.toJSONString(map, true);
    return jsonString;
  }

  public static boolean isHttpRequestAnAjaxCall(HttpServletRequest request) {
    return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
  }

}
