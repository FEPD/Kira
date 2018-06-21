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
package com.yihaodian.architecture.hedwig.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigTimeoutUtil {

  private static final String HEDWIG_INVOKE_TIMEOUT = "HEDWIG_INVOKE_TIMEOUT";
  private static final String HEDWIG_INVOKE_READ_TIMEOUT = "HEDWIG_INVOKE_READ_TIMEOUT";
  private static final int MIN_TIME_OUT = 50; //最小50ms
  private static final int MAX_TIME_OUT = 30000;//最大30s
  private static Logger logger = LoggerFactory.getLogger(HedwigTimeoutUtil.class);

  /**
   * 设置方法这一次请求调用超时时间 单位ms
   *
   * @param timeout timeout是一次服务调用的最长等待时间，对应代码为Future.get(timeout)；单位ms
   * @param readTimeout readTimeout是远程请求所使用的链接的最长等待时间，超过这个时间链接会自动断开。 readTimeout的值为服务端处理请求的时间+网络传输时间；timeout的值根据准许的重试次数可以设置为readtiemout的3-5倍
   */
  public static void setRequestTimeout(Long timeout, Long readTimeout) {
    if (timeout == null || timeout < MIN_TIME_OUT || timeout > MAX_TIME_OUT) {
      throw new IllegalArgumentException("### Hedwig Timeout Config ERROR!!  " +
          "MIN=" + MIN_TIME_OUT + ",MAX=" + MAX_TIME_OUT + " timeout=" + timeout);
    }
    if (readTimeout == null || readTimeout < MIN_TIME_OUT || readTimeout > MAX_TIME_OUT) {
      throw new IllegalArgumentException("### Hedwig ReadTimeout Config ERROR!!  " +
          "MIN=" + MIN_TIME_OUT + ",MAX=" + MAX_TIME_OUT + " readTimeout=" + readTimeout);
    }
    if (timeout < readTimeout) {
      throw new IllegalArgumentException(
          "### Hedwig Timeout Config ERROR!! timeout can't less than readTimeout!!" +
              " timeout=" + timeout + ",readTimeout=" + readTimeout);
    }
    HedwigContextUtil.setAttribute(HEDWIG_INVOKE_TIMEOUT, timeout);
    HedwigContextUtil.setAttribute(HEDWIG_INVOKE_READ_TIMEOUT, readTimeout);
  }


  public static Long getRequestTimeout() {
    Long result = null;
    Object obj = HedwigContextUtil.getAttribute(HEDWIG_INVOKE_TIMEOUT, null);
    if (obj != null) {
      result = (Long) obj;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("###-----getRequestTimeout()=" + result + "-----");
    }
    return result;
  }

  public static Long getRequestReadTimeout() {
    Long result = null;
    Object obj = HedwigContextUtil.getAttribute(HEDWIG_INVOKE_READ_TIMEOUT, null);
    if (obj != null) {
      result = (Long) obj;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("###-----getRequestReadTimeout()=" + result + "-----");
    }
    return result;
  }
}

