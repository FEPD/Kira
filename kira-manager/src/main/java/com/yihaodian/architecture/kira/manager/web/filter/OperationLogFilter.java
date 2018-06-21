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
package com.yihaodian.architecture.kira.manager.web.filter;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.domain.OperationLog;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.service.OperationLogService;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import com.yihaodian.architecture.kira.manager.util.SpringBeanUtils;
import com.yihaodian.architecture.kira.manager.util.WebUtil;
import com.yihaodian.architecture.kira.manager.web.HttpRequestContextHolder;
import com.yihaodian.architecture.kira.manager.web.WapperedHttpServletResponse;
import java.io.IOException;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

public class OperationLogFilter implements Filter {

  private static Logger logger = LoggerFactory.getLogger(OperationLogFilter.class);

  public OperationLogFilter() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    WapperedHttpServletResponse wapperedHttpServletResponse = new WapperedHttpServletResponse(
        (HttpServletResponse) response);
    String userName = SecurityUtils.getUserName(httpServletRequest);
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    Date operateTime = new Date();
    Exception caughtException = null;
    try {
      chain.doFilter(httpServletRequest, wapperedHttpServletResponse);
    } catch (IOException ioException) {
      logger.error("IOException occurs for OperationLogFilter.", ioException);
      caughtException = ioException;
      throw ioException;
    } catch (ServletException servletException) {
      logger.error("ServletException occurs for OperationLogFilter.", servletException);
      caughtException = servletException;
      throw servletException;
    } catch (RuntimeException runtimeException) {
      logger.error("RuntimeException occurs for OperationLogFilter.", runtimeException);
      caughtException = runtimeException;
      throw runtimeException;
    } finally {
      try {
        OperationLog operationLog = HttpRequestContextHolder.getOperationLog();
        if (null != operationLog) {
          String uuid = KiraCommonUtils.getUUID();
          operationLog.setId(uuid);
          operationLog.setOperateTime(operateTime);
          operationLog.setOperatedBy(userName);
          String operationDetails = getOperationDetails(httpServletRequest,
              wapperedHttpServletResponse, caughtException);
          operationLog.setOperationDetails(operationDetails);

          OperationLogService operationLogService = (OperationLogService) SpringBeanUtils
              .getBean(KiraServerConstants.SPRING_BEAN_NAME_OPERATIONLOGSERVICE);
          operationLogService.insert(operationLog);
        }
      } finally {
        HttpRequestContextHolder.removeOperationLog();
      }
      IOUtils.write(wapperedHttpServletResponse.getResponseData(), response.getOutputStream());
    }
  }

  private String getOperationDetails(HttpServletRequest httpServletRequest,
      WapperedHttpServletResponse wapperedHttpServletResponse, Exception caughtException)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    String requestUrl = WebUtil
        .getRequestFullUrl(httpServletRequest);
    String requestInfo = getRequestInfo(httpServletRequest);
    sb.append("===>RequestUrl: ").append(KiraCommonConstants.LINE_SEPARATOR).append(requestUrl);
    sb.append(KiraCommonConstants.LINE_SEPARATOR).append("===>RequestInfo: ")
        .append(KiraCommonConstants.LINE_SEPARATOR).append(requestInfo);
    String responseDataAsString = wapperedHttpServletResponse.getResponseDataAsString();
    String htmlEscapedResponseDataAsString = HtmlUtils.htmlEscape(responseDataAsString);
    sb.append(KiraCommonConstants.LINE_SEPARATOR).append("===>Response data: ")
        .append(KiraCommonConstants.LINE_SEPARATOR).append(htmlEscapedResponseDataAsString);
    if (null != caughtException) {
      String exceptionDesc = ExceptionUtils.getFullStackTrace(caughtException);
      sb.append(KiraCommonConstants.LINE_SEPARATOR).append("===>Caught exception: ")
          .append(KiraCommonConstants.LINE_SEPARATOR).append(exceptionDesc);
    }
    return sb.toString();
  }

  /**
   * Get the request info which include "Request method,request header info,request params info"
   */
  private String getRequestInfo(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    StringBuilder sbForRequestInfo = new StringBuilder();
    sbForRequestInfo.append(WebUtil
        .getRequestMethodInfo(httpServletRequest));
    sbForRequestInfo.append(KiraCommonConstants.LINE_SEPARATOR);
    sbForRequestInfo.append(WebUtil
        .getRequestHeadersInfo(httpServletRequest));
    sbForRequestInfo.append(KiraCommonConstants.LINE_SEPARATOR);
    sbForRequestInfo.append(WebUtil
        .getRequestParamtersInfo(httpServletRequest));
    returnValue = sbForRequestInfo.toString();
    return returnValue;
  }

  @Override
  public void destroy() {
  }

}
