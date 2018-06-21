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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class LogManagerUtil {

  public static String Level_ALL = "ALL";
  public static String Level_DEBUG = "DEBUG";
  public static String Level_INFO = "INFO";
  public static String Level_WARN = "WARN";
  public static String Level_ERROR = "ERROR";
  public static String Level_FATAL = "FATAL";
  public static String Level_OFF = "OFF";
  public static String Level_TRACE = "TRACE";

  /**
   * 动态修改某个包的日志级别
   *
   * @param packageName 包路径
   * @param levelName 日志级别: ALL,DEBUG,INFO,WARN,ERROR,FATAL,OFF,TRACE
   */
  public static String updatePackageLogLevel(String packageName, String levelName) {
    StringBuilder resultSB = new StringBuilder();
    if (StringUtils.isNotBlank(packageName)) {
      Logger logger = null;
      if (packageName.equals("ROOT")) {
        logger = LogManager.getRootLogger();
      } else {
        logger = LogManager.getLogger(packageName);
      }
      if (logger != null) {
        if (StringUtils.isNotBlank(levelName)) {
          Level level = Level.toLevel(levelName, Level.ERROR);
          logger.setLevel(level);
        }
        if (logger.getLevel() != null) {
          resultSB.append(packageName).append("=").append(logger.getLevel().toString());
        } else {
          Category category = findParentCategory(logger);
          if (category != null && category.getLevel() != null) {
            resultSB.append(packageName).append("=").append(category.getName()).append("=")
                .append(category.getLevel().toString());
          } else {
            resultSB.append("NOT FOUND LEVEL!! Logger=" + packageName);
          }
        }
      } else {
        resultSB.append("NOT FOUND Logger!! Logger=" + packageName);
      }
    } else {
      resultSB.append("PackageName is Empty!! packageName=" + packageName);
    }
    return resultSB.toString();
  }

  private static Category findParentCategory(Logger logger) {
    Category result = null;
    if (logger != null) {
      Category category = logger.getParent();
      if (category != null && category.getLevel() == null) {
        while (category.getParent() != null) {
          if (category.getLevel() != null) {
            break;
          } else {
            category = category.getParent();
          }
        }
      }
      result = category;
    }
    return result;
  }
}
