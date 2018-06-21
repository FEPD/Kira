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
package com.yihaodian.architecture.hedwig.provider;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Archer
 */
public class AppProfile implements ApplicationContextAware {

  public final static int WEIGHT_LIMIT = 20;
  private Logger logger = LoggerFactory.getLogger(AppProfile.class);
  private String domainName = InternalConstants.UNKONW_DOMAIN;
  private String serviceAppName = "defaultAppName";
  private String urlPattern = InternalConstants.HEDWIG_URL_PATTERN;
  private boolean assembleAppName = false;
  private int port = -1;
  private ApplicationContext springContext;
  private String user;
  private String password;

  public AppProfile() {
    super();
  }

  public AppProfile(ApplicationContext springContext) {
    super();
    this.springContext = springContext;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = HedwigUtil.filterString(domainName);
  }

  public String getServiceAppName() {
    return serviceAppName;
  }

  public void setServiceAppName(String serviceAppName) {
    this.serviceAppName = HedwigUtil.filterString(serviceAppName);
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    this.urlPattern = HedwigUtil.filterString(urlPattern);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isAssembleAppName() {
    return assembleAppName;
  }

  public void setAssembleAppName(boolean assembleAppName) {
    this.assembleAppName = assembleAppName;
  }

  public void changeAllServiceEnable(boolean enable) {
    String[] names = BeanFactoryUtils
        .beanNamesForTypeIncludingAncestors(springContext, HedwigWebserviceExporter.class);
    if (names != null && names.length >= 1) {
      for (String beanName : names) {
        HedwigWebserviceExporter hhe = (HedwigWebserviceExporter) springContext.getBean(beanName);
        hhe.changeServEnable(enable);
      }
    }
  }

  public List<String> changeAllServiceListEnable(boolean enable) {
    String[] names = BeanFactoryUtils
        .beanNamesForTypeIncludingAncestors(springContext, HedwigWebserviceExporter.class);
    List<String> resultList = new ArrayList<String>();
    if (names != null && names.length > 0) {
      List<String> inputBeanName = new ArrayList<String>();
      for (String beanName : names) {
        inputBeanName.add(beanName);
      }
      List<String> failureList = changeServiceListEnable(inputBeanName, enable);
      if (failureList != null && failureList.size() > 0) {
        try {
          Thread.sleep(5000L);
          logger.error("#### changeServiceListEnable failure!! Retry after 5000ms, failureList="
              + HedwigJsonUtil.toJSONString(failureList));
        } catch (Exception e) {
          e.printStackTrace();
        }
        resultList = changeServiceListEnable(failureList, enable);
      }
    }
    return resultList;
  }

  public List<String> changeServiceListEnable(List<String> beanNames, boolean enable) {
    List<String> failureList = new ArrayList<String>();
    if (beanNames != null && beanNames.size() > 0) {
      for (String beanName : beanNames) {
        try {
          try {
            Thread.sleep(500L);
          } catch (Exception e) {
            e.printStackTrace();
          }
          changeOneServiceEnable(beanName, enable);
        } catch (Exception e) {
          failureList.add(beanName);
          e.printStackTrace();
        }
      }
    }
    return failureList;
  }

  private void changeOneServiceEnable(String beanName, boolean enable) {
    HedwigWebserviceExporter hhe = (HedwigWebserviceExporter) springContext.getBean(beanName);
    hhe.changeServEnable(enable);
  }

  public void changeAllServiceWeight(int newWeight) {
    String[] names = BeanFactoryUtils
        .beanNamesForTypeIncludingAncestors(springContext, HedwigWebserviceExporter.class);
    if (names != null && names.length >= 1) {
      for (String beanName : names) {
        HedwigWebserviceExporter hhe = (HedwigWebserviceExporter) springContext.getBean(beanName);
        hhe.changeServWeight(newWeight);
      }
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    springContext = applicationContext;
  }

  public ApplicationContext getSpringContext() {
    return springContext;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
