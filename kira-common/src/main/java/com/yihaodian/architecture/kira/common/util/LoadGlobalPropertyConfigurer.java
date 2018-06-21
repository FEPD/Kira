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
package com.yihaodian.architecture.kira.common.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Created by zhoufeiqiang on 04/09/2017.
 */
public class LoadGlobalPropertyConfigurer extends PropertyPlaceholderConfigurer implements
    InitializingBean, BeanNameAware {

  private static String appId;
  private static LoadGlobalPropertyConfigurer loadGlobalPropertyConfigurer = new LoadGlobalPropertyConfigurer();

  public static String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public static void AppId(String appId) {
    String _appId = LoadGlobalPropertyConfigurer.getAppId();
    if (StringUtils.isBlank(_appId) && StringUtils.isNotBlank(appId)) {
      _appId = appId;
      loadGlobalPropertyConfigurer.setAppId(_appId);
    } else if (StringUtils.isBlank(_appId) && StringUtils.isBlank(appId)) {
      throw new RuntimeException("appId should not be blank!");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (StringUtils.isBlank(appId)) {
      throw new RuntimeException("appId should not be blank！");
    }
  }
}
