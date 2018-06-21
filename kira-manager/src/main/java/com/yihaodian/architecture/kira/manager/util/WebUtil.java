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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebUtil {

  private static final String QUERYSTRING_PREFIX = "?";
  private static final String HOSTNAME_UNKNOWN = "unknown";

  private static final String EQUAL_STRING = "=";
  private static final String LEFT_BRACKET = "[";
  private static final String RIGHT_BRACKET = "]";
  private static final String COMMA_STRING = ",";
  private static final String EMPTY_STRING = "";

  private static final String UTF8 = "UTF-8";
  private static final String CONTENTTYPE_EXCEL = "application/vnd.ms-excel";
  private static final String HTTP_HEADER_NAME_CONTENT_DISPOSITION = "Content-Disposition";

  private static Logger logger = LoggerFactory.getLogger(SpringBeanUtils.class);

  /**
   * Get the url which is "requestURL+?+queryString"
   *
   * @return the requestFullUrl
   */
  public static String getRequestFullUrl(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    StringBuilder sb = new StringBuilder();
    StringBuffer requestURL = httpServletRequest.getRequestURL();
    sb.append(requestURL);
    String queryString = httpServletRequest.getQueryString();
    if (StringUtils.isNotBlank(queryString)) {
      sb.append(QUERYSTRING_PREFIX).append(queryString);
    }
    returnValue = sb.toString();
    return returnValue;
  }

  /**
   * Get the client ip
   */
  public static String getClientIp(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    returnValue = httpServletRequest
        .getHeader(ProxyClientIPHttpHeaderNameEnum.XFORWARDEDFOR
            .getHeaderName());
    if (StringUtils.isBlank(returnValue)
        || HOSTNAME_UNKNOWN.equalsIgnoreCase(returnValue)) {
      returnValue = httpServletRequest
          .getHeader(ProxyClientIPHttpHeaderNameEnum.PROXYCLIENTIP
              .getHeaderName());
    }
    if (StringUtils.isBlank(returnValue)
        || HOSTNAME_UNKNOWN.equalsIgnoreCase(returnValue)) {
      returnValue = httpServletRequest
          .getHeader(ProxyClientIPHttpHeaderNameEnum.WLPROXYCLIENTIP
              .getHeaderName());
    }
    if (StringUtils.isBlank(returnValue)
        || HOSTNAME_UNKNOWN.equalsIgnoreCase(returnValue)) {
      returnValue = httpServletRequest.getRemoteAddr();
    }
    if (null != returnValue) {
      // 多个路由时，取第一个非unknown的ip
      String[] ips = returnValue.split(COMMA_STRING);
      for (String oneIp : ips) {
        if (!HOSTNAME_UNKNOWN.equals(oneIp)) {
          returnValue = oneIp;
          break;
        }
      }
    }
    return returnValue;
  }

  public static String getRequestHeadersInfo(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    Enumeration headerNames = httpServletRequest.getHeaderNames();
    StringBuilder sb = new StringBuilder();
    sb.append("Request Headers:");
    sb.append(LEFT_BRACKET);
    if (null != headerNames) {
      Object headerName = null;
      String headerValue = null;
      while (headerNames.hasMoreElements()) {
        headerName = headerNames.nextElement();
        headerValue = httpServletRequest.getHeader(headerName
            .toString());
        sb.append(headerName).append(EQUAL_STRING).append(headerValue)
            .append(headerNames.hasMoreElements() ? COMMA_STRING : EMPTY_STRING);
      }
    }
    sb.append(RIGHT_BRACKET);
    returnValue = sb.toString();
    return returnValue;
  }

  public static String getRequestParamtersInfo(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
    StringBuilder sb = new StringBuilder();
    sb.append("Request Parameters:");
    sb.append(LEFT_BRACKET);
    String parameterName = null;
    String parameterValue = null;
    while (parameterNames.hasMoreElements()) {
      parameterName = parameterNames.nextElement();
      parameterValue = httpServletRequest.getParameter(parameterName);
      sb.append(parameterName).append(EQUAL_STRING).append(parameterValue)
          .append(parameterNames.hasMoreElements() ? COMMA_STRING : EMPTY_STRING);
    }
    sb.append(RIGHT_BRACKET);
    returnValue = sb.toString();
    return returnValue;
  }

  public static String getRequestMethodInfo(HttpServletRequest httpServletRequest) {
    String returnValue = null;
    StringBuilder sb = new StringBuilder();
    sb.append("Request method:");
    sb.append(httpServletRequest.getMethod());
    returnValue = sb.toString();
    return returnValue;
  }

  public static void setHttpResponseHeaderForExcelExport(
      HttpServletRequest request, HttpServletResponse response, boolean noCache, String fileName) {
    if (noCache) {
      setNoCacheHeader(response);
    }
    String finalFileName = getFinalFileName(request, fileName);
    String contentDispositionHeaderValue = "attachment; filename=\"" + finalFileName + "\"";
    response.setContentType(CONTENTTYPE_EXCEL);
    response.setHeader(HTTP_HEADER_NAME_CONTENT_DISPOSITION, contentDispositionHeaderValue);
  }

  private static String getFinalFileName(HttpServletRequest request, String fileName) {
    String returnValue = null;
    String userAgent = request.getHeader("user-agent");
    returnValue = getFinalFileName(userAgent, fileName);
    return returnValue;
  }

  private static String getFinalFileName(String userAgent, String fileName) {
    String returnValue = fileName;
    try {
      boolean isInternetExplorer = userAgent.indexOf("MSIE") != -1;
      if (isInternetExplorer) {
        returnValue = URLEncoder.encode(fileName, UTF8);
        returnValue = returnValue.replace("+", "%20");
      } else {
        returnValue = MimeUtility.encodeWord(fileName, UTF8, "B");
      }
    } catch (UnsupportedEncodingException ex) {
      logger.error("Error occurs when encode fileName:" + fileName, ex);
    }
    return returnValue;
  }

  private static void setNoCacheHeader(HttpServletResponse response) {
    response.setHeader("Expires", "0");
    response.setHeader("Cache-control", "");
    response.setHeader("Pragma", "");
  }

  private enum ProxyClientIPHttpHeaderNameEnum {
    XFORWARDEDFOR("x-forwarded-for"),
    PROXYCLIENTIP("Proxy-Client-IP"),
    WLPROXYCLIENTIP("WL-Proxy-Client-IP");

    private final String headerName;

    private ProxyClientIPHttpHeaderNameEnum(String headerName) {
      this.headerName = headerName;
    }

    String getHeaderName() {
      return headerName;
    }

    @Override
    public String toString() {
      return headerName;
    }
  }
}
