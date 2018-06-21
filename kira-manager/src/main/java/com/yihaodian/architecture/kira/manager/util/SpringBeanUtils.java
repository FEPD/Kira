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

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SpringBeanUtils implements BeanFactoryAware {

  private static Logger logger = LoggerFactory.getLogger(SpringBeanUtils.class);

  private static BeanFactory beanFactory;

  public static void waitForBeanFactorySet() {
    try {
      int waittime = 0;
      while ((null == SpringBeanUtils.beanFactory)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when waitForBeanFactorySet.", e);
    } finally {
      if (null == SpringBeanUtils.beanFactory) {
        String message = "SpringBeanUtils.beanFactory is still null.";
        logger.error(message);
        throw new RuntimeException(message);
      }
    }
  }

  public static Object getBean(String beanName) {
    SpringBeanUtils.waitForBeanFactorySet();
    Object returnValue = null;
    try {
      returnValue = beanFactory.getBean(beanName);
      int waittime = 0;
      while ((null == returnValue)
          && waittime < KiraCommonConstants.DEFAULT_WAITFORRESOURCETIMEOUTMILLISECOND) {
        Thread.sleep(100);
        waittime += 100;
      }
    } catch (InterruptedException e) {
      logger.error("InterruptedException caught when getBean() by beanName=" + beanName, e);
    } finally {
      returnValue = beanFactory.getBean(beanName);
      if (null == returnValue) {
        String message = "Still can not get bean by beanName=" + beanName;
        logger.error(message);
        throw new RuntimeException(message);
      }
    }

    return returnValue;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    SpringBeanUtils.beanFactory = beanFactory;
  }
}
