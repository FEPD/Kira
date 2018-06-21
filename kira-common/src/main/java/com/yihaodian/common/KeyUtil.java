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
package com.yihaodian.common;

import com.yihaodian.monitor.dto.ClientBizLog;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.StringUtils;

public class KeyUtil {

  private static final String unknown = "unknown";
  /**
   * 分隔符
   */
  private static final String separator = "@";
  private static final AtomicLong curtValue = new AtomicLong(initValue());
  private static final AtomicLong commIdSeq = new AtomicLong(0);
  private static final int partions = 10;
  private static final String dateFmt = "ddHHmm";
  private static final Map<String, String> appCodes = new HashMap<String, String>();
  private static final int maxLength = 18;
  private static final int threshold = 10000000;
  static Integer jvmPidHashcode = null;
  private static String curtAppCode = null;

  /**
   * 生产 规则：partionId + ProviderAppCode +TS
   */
  public static String getReqId(ClientBizLog clientBizLog) {
    if (clientBizLog == null || clientBizLog.getReqTime() == null) {
      return getReqId(new Date());
    }
    return getReqId(clientBizLog.getReqTime());
  }

  /**
   * @param
   */
  public static String getReqId(Date reqTime) {
    if (reqTime == null) {
      reqTime = new Date();
    }
    return getReqId(curtAppCode, reqTime);
  }

  /**
   * 基于 callApp callHost reqTime生成reqId
   */
  public static String getReqId(String providerApp, Date reqTime) {
    StringBuilder sBuilder = new StringBuilder();
    long curt = curtValue.incrementAndGet();
    if (curt >= threshold) {
      curtValue.set(0);
    }
    sBuilder.append(curt % partions).append(separator);
    /** 其他所有情况均为参数异常，统一按下列方式默认处理 **/
    if (reqTime == null) {
      reqTime = new Date();
    }
    String innerAppCode = appCodes.get(providerApp);
    if (StringUtils.isEmpty(innerAppCode) && StringUtils.isNotEmpty(providerApp)) {
      innerAppCode = unknown;
      if (innerAppCode.length() > maxLength) {
        innerAppCode = innerAppCode.substring(0, maxLength);
      }
      appCodes.put(providerApp, innerAppCode);
    }
    if (StringUtils.isEmpty(innerAppCode)) {
      innerAppCode = unknown;
    }
    return sBuilder.append(getSegment(providerApp, reqTime)).append(curt)
        .append(separator).append(getJvmPidHashCode()).toString();
  }

  public static String getSegment(String appCode, Date reqTime) {
    StringBuilder sBuilder = new StringBuilder();
    String innerAppCode = appCodes.get(appCode);
    if (StringUtils.isEmpty(innerAppCode) && StringUtils.isNotEmpty(appCode)) {
      innerAppCode = unknown;
      if (innerAppCode.length() > maxLength) {
        innerAppCode = innerAppCode.substring(0, maxLength);
      }
      appCodes.put(appCode, innerAppCode);
    }
    if (StringUtils.isEmpty(innerAppCode)) {
      innerAppCode = unknown;
    }
    return sBuilder.append(formatDateTime(reqTime)).append(separator).append(innerAppCode)
        .append(separator).toString();
  }

  /**
   * 获取全局请求ID
   */
  public static String getGlobalId(String callApp, Date reqTime) {
    return getReqId(callApp, reqTime);
  }

  public static int getPartions() {
    return partions;
  }

  private static String formatDateTime(Date date) {
    if (date == null) {
      date = new Date();
    }
    return DateUtil.getFormatDateTime(new Date(), dateFmt);
  }

  public static void main(String[] args) {

  }

  private static long initValue() {
    long v1 = System.currentTimeMillis();
    long v2 = (v1 / (1000 * 60)) * (1000 * 60);
    return v1 - v2;
  }

  public static Integer getJvmPidHashCode() {
    if (jvmPidHashcode == null) {
      try {
        jvmPidHashcode = ManagementFactory.getRuntimeMXBean().getName().hashCode();
      } catch (Exception e) {
        jvmPidHashcode = (int) (Math.random() * (100000 + 1) - 1);
      }
    }
    return jvmPidHashcode;
  }

  public static String getSeparator() {
    return separator;
  }

  /**
   * 生成CommId字符串
   */
  public String getCommId() {
    return String.valueOf(commIdSeq.incrementAndGet());
  }
}
