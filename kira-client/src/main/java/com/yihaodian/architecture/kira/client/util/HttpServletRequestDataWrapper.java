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
package com.yihaodian.architecture.kira.client.util;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class HttpServletRequestDataWrapper implements Serializable {

  private static final long serialVersionUID = 1L;

  private String remoteAddr;
  private String remoteHost;
  private int remotePort;

  private String localAddr;
  private String localName;
  private int localPort;

  private String protocol;
  private String scheme;
  private String serverName;
  private int serverPort;

  private String method;
  private String requestURI;
  private String requestURL;
  private String contextPath;
  private String queryString;

  private String requestedSessionId;

  private String authType;
  private String remoteUser;
  private Principal userPrincipal;

  private String characterEncoding;
  private String contentType;
  private int contentLength;

  private Locale locale;
  private List<Locale> locales = new ArrayList<Locale>();

  private List<Cookie> cookies = new ArrayList<Cookie>();

  private Map<String, List<String>> headerNameValuesMap = new LinkedHashMap<String, List<String>>();

  private Map<String, List<String>> parameterNameValuesMap = new LinkedHashMap<String, List<String>>();

  public HttpServletRequestDataWrapper(HttpServletRequest httpServletRequest) {
    this.remoteAddr = httpServletRequest.getRemoteAddr();
    this.remoteHost = httpServletRequest.getRemoteHost();
    this.remotePort = httpServletRequest.getRemotePort();

    this.localAddr = httpServletRequest.getLocalAddr();
    this.localName = httpServletRequest.getLocalName();
    this.localPort = httpServletRequest.getLocalPort();

    this.protocol = httpServletRequest.getProtocol();
    this.scheme = httpServletRequest.getScheme();
    this.serverName = httpServletRequest.getServerName();
    this.serverPort = httpServletRequest.getServerPort();

    this.method = httpServletRequest.getMethod();
    this.requestURI = httpServletRequest.getRequestURI();
    StringBuffer requestURLSB = httpServletRequest.getRequestURL();
    if (null != requestURLSB) {
      this.requestURL = requestURLSB.toString();
    }
    this.contextPath = httpServletRequest.getContextPath();
    this.queryString = httpServletRequest.getQueryString();

    this.requestedSessionId = httpServletRequest.getRequestedSessionId();

    this.authType = httpServletRequest.getAuthType();
    this.userPrincipal = httpServletRequest.getUserPrincipal();
    this.remoteUser = httpServletRequest.getRemoteUser();

    this.characterEncoding = httpServletRequest.getCharacterEncoding();
    this.contentType = httpServletRequest.getContentType();
    this.contentLength = httpServletRequest.getContentLength();

    locale = httpServletRequest.getLocale();

    Enumeration localesEnumeration = httpServletRequest.getLocales();
    if (null != localesEnumeration) {
      Object oneLocale = null;
      while (localesEnumeration.hasMoreElements()) {
        oneLocale = localesEnumeration.nextElement();
        if (oneLocale instanceof Locale) {
          this.locales.add((Locale) oneLocale);
        }
      }
    }

    Cookie[] cookiesAsArray = httpServletRequest.getCookies();
    if (null != cookiesAsArray) {
      for (Cookie cookie : cookiesAsArray) {
        if (null != cookie) {
          this.cookies.add((Cookie) cookie.clone());
        }
      }
    }

    Enumeration headerNamesEnumeration = httpServletRequest.getHeaderNames();
    if (null != headerNamesEnumeration) {
      Object headerName = null;
      ArrayList<String> headerValuesList = null;
      while (headerNamesEnumeration.hasMoreElements()) {
        headerName = headerNamesEnumeration.nextElement();
        headerValuesList = new ArrayList<String>();
        if (null != headerName) {
          Enumeration headerValuesEnumeration = httpServletRequest
              .getHeaders(headerName.toString());
          if (null != headerValuesEnumeration) {
            Object headerValue = null;
            while (headerValuesEnumeration.hasMoreElements()) {
              headerValue = headerValuesEnumeration.nextElement();
              if (null != headerValue) {
                headerValuesList.add(headerValue.toString());
              } else {
                headerValuesList.add(null);
              }
            }
          }
          headerNameValuesMap.put(headerName.toString(), headerValuesList);
        }
      }
    }

    Enumeration parameterNamesEnumeration = httpServletRequest.getParameterNames();
    if (null != parameterNamesEnumeration) {
      Object parameterName = null;
      List<String> parameterValuesAsList = null;
      while (parameterNamesEnumeration.hasMoreElements()) {
        parameterName = parameterNamesEnumeration.nextElement();
        parameterValuesAsList = new ArrayList<String>();
        if (null != parameterName) {
          String[] parameterValuesAsArray = httpServletRequest
              .getParameterValues(parameterName.toString());
          if (null != parameterValuesAsArray) {
            parameterValuesAsList = Arrays.asList(parameterValuesAsArray);
          }
          parameterNameValuesMap.put(parameterName.toString(), parameterValuesAsList);
        }
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public String getLocalAddr() {
    return localAddr;
  }

  public String getLocalName() {
    return localName;
  }

  public int getLocalPort() {
    return localPort;
  }

  public Map<String, List<String>> getHeaderNameValuesMap() {
    return headerNameValuesMap;
  }

  public String getHeaderValue(String headerName) {
    String returnValue = null;
    List<String> headerValueList = headerNameValuesMap.get(headerName);
    if (null != headerValueList && headerValueList.size() > 0) {
      returnValue = headerValueList.get(0);
    }
    return returnValue;
  }

  public List<String> getHeaderValues(String headerName) {
    return headerNameValuesMap.get(headerName);
  }

  public Map<String, List<String>> getParameterNameValuesMap() {
    return parameterNameValuesMap;
  }

  public String getParameterValue(String parameterName) {
    String returnValue = null;
    List<String> parameterValueList = parameterNameValuesMap.get(parameterName);
    if (null != parameterValueList && parameterValueList.size() > 0) {
      returnValue = parameterValueList.get(0);
    }
    return returnValue;
  }

  public List<String> getParameterValues(String parameterName) {
    return parameterNameValuesMap.get(parameterName);
  }

  public String getProtocol() {
    return protocol;
  }

  public String getScheme() {
    return scheme;
  }

  public String getServerName() {
    return serverName;
  }

  public int getServerPort() {
    return serverPort;
  }

  public String getMethod() {
    return method;
  }

  public String getRequestURI() {
    return requestURI;
  }

  public String getRequestURL() {
    return requestURL;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getQueryString() {
    return queryString;
  }

  public String getRequestedSessionId() {
    return requestedSessionId;
  }

  public String getAuthType() {
    return authType;
  }

  public String getRemoteUser() {
    return remoteUser;
  }

  public Principal getUserPrincipal() {
    return userPrincipal;
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public String getContentType() {
    return contentType;
  }

  public int getContentLength() {
    return contentLength;
  }

  public List<Cookie> getCookies() {
    return cookies;
  }

  public Locale getLocale() {
    return locale;
  }

  public List<Locale> getLocales() {
    return locales;
  }

  @Override
  public String toString() {
    return "HttpServletRequestDataWrapper [remoteAddr=" + remoteAddr
        + ", remoteHost=" + remoteHost + ", remotePort=" + remotePort
        + ", localAddr=" + localAddr + ", localName=" + localName
        + ", localPort=" + localPort + ", protocol=" + protocol
        + ", scheme=" + scheme + ", serverName=" + serverName
        + ", serverPort=" + serverPort + ", method=" + method
        + ", requestURI=" + requestURI + ", requestURL=" + requestURL
        + ", contextPath=" + contextPath + ", queryString="
        + queryString + ", requestedSessionId=" + requestedSessionId
        + ", authType=" + authType + ", remoteUser=" + remoteUser
        + ", userPrincipal=" + userPrincipal + ", characterEncoding="
        + characterEncoding + ", contentType=" + contentType
        + ", contentLength=" + contentLength + ", locale=" + locale
        + ", locales=" + locales + ", cookies=" + cookies
        + ", headerNameValuesMap=" + headerNameValuesMap
        + ", parameterNameValuesMap=" + parameterNameValuesMap + "]";
  }

}
