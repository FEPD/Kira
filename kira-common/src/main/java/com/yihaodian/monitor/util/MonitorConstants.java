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
package com.yihaodian.monitor.util;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;

/**
 * Monitor常量类
 *
 * @author dongheng
 */
public class MonitorConstants {

  /**
   * 成功常量值
   */
  public static final int SUCCESS = 1;

  /**
   * 失败常量值
   */
  public static final int FAIL = -1;

  /**
   * 客户端日志ENGINE层常量值
   */
  public static final int LAYER_TYPE_ENGINE = 1;

  /**
   * 客户端日志HANDLER层常量值
   */
  public static final int LAYER_TYPE_HANDLER = 2;

  /**
   * AND常量
   */
  public static final String AND = "&";

  /**
   * MONITOR APP CODE 定义
   */
  public static final String MONITOR_APP_CODE = "monitor";
  /**
   * MONITOR APP CODE 定义
   */
  public static final String SEPARATOR = "/";
  public static final String BASE_ROOT = InternalConstants.BASE_ROOT;
  /**
   * 通讯平台domain name
   */
  public static final String HEDWIG_DOMAIN_NAME = "hedwig";

}
