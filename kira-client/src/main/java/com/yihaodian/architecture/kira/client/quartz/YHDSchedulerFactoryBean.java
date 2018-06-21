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
package com.yihaodian.architecture.kira.client.quartz;

import com.yihaodian.architecture.kira.client.internal.IKiraClientInternalFacade;
import com.yihaodian.architecture.kira.client.internal.KiraClientInternalFacade;
import com.yihaodian.architecture.kira.client.internal.iface.IKiraClientRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.iface.ITriggerDeleteHandler;
import com.yihaodian.architecture.kira.client.internal.iface.ITriggerRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.iface.IYHDBean;
import com.yihaodian.architecture.kira.client.internal.impl.KiraClientRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.impl.TriggerDeleteHandler;
import com.yihaodian.architecture.kira.client.internal.impl.TriggerRegisterContextDataHandler;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.util.KiraClientConfig;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class YHDSchedulerFactoryBean extends SchedulerFactoryBean implements IYHDBean,
    BeanPostProcessor {

  private static Logger logger = LoggerFactory.getLogger(YHDSchedulerFactoryBean.class);

  private Trigger[] triggers = new Trigger[0];

  private Trigger[] locallyRunTriggers = new Trigger[0];

  private String beanName;

  private String locationsToRunJobForAllTriggersOfThisScheduler;

  public YHDSchedulerFactoryBean() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Trigger[] getTriggers() {
    return triggers;
  }

  @Override
  public void setTriggers(Trigger[] triggers) {
    this.triggers = triggers;
    super.setTriggers(triggers);
  }

  public Trigger[] getLocallyRunTriggers() {
    return locallyRunTriggers;
  }

  @Override
  public String getBeanName() {
    return beanName;
  }

  @Override
  public void setBeanName(String beanName) {
    this.beanName = beanName;
    super.setBeanName(beanName);
  }

  public String getLocationsToRunJobForAllTriggersOfThisScheduler() {
    return locationsToRunJobForAllTriggersOfThisScheduler;
  }

  /**
   * locations which are seperated by , e.g. ip1:port1,ip2:port2,ip3:port3
   */
  public void setLocationsToRunJobForAllTriggersOfThisScheduler(
      String locationsToRunJobForAllTriggersOfThisScheduler) {
    this.locationsToRunJobForAllTriggersOfThisScheduler = locationsToRunJobForAllTriggersOfThisScheduler;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    KiraClientDataCenter.setApplicationContext(applicationContext);
    super.setApplicationContext(applicationContext);
  }

  public void afterPropertiesSet() throws Exception {
    boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!workWithoutKira) {
      //Do not use crossMultiZone stuff for kira-client now
      //KiraClientCrossMultiZoneUtils.prepareKiraClientCrossMultiZoneUtils();

      KiraClientConfig kiraClientConfig = KiraClientDataCenter.getKiraClientConfig();
      if (null == kiraClientConfig) {
        //Create a default one , store and handle it
        kiraClientConfig = new KiraClientConfig();
        KiraClientInternalFacade.getKiraClientInternalFacade()
            .handleForKiraClientConfig(kiraClientConfig);
      }
    }

    locallyRunTriggers = KiraClientInternalFacade.getKiraClientInternalFacade()
        .handleAndGetLocallyRunTriggers(this, triggers, KiraClientDataCenter.isWorkWithoutKira());
    KiraClientInternalFacade.getKiraClientInternalFacade().handleOthers();
    super.setTriggers(locallyRunTriggers);
    super.afterPropertiesSet();
    KiraClientDataCenter.addSchedulerBeanNameYHDSchedulerFactoryBeanRelationShip(beanName, this);
  }

  @Override
  public void destroy() {
    try {
      super.destroy();
    } catch (Exception e) {
      if (null != logger) {
        logger.error("Error on super.destroy();", e);
      }
    }
    try {
      ITriggerRegisterContextDataHandler triggerRegisterContextDataHandler = TriggerRegisterContextDataHandler
          .getTriggerRegisterContextDataHandlerInstance();
      if (null != triggerRegisterContextDataHandler) {
        triggerRegisterContextDataHandler.destroy();
      }
      ITriggerDeleteHandler triggerDeleteHandler = TriggerDeleteHandler
          .getTriggerDeleteHandlerInstance();
      if (null != triggerDeleteHandler) {
        triggerDeleteHandler.destroy();
      }
      IKiraClientRegisterContextDataHandler kiraClientRegisterContextDataHandler = KiraClientRegisterContextDataHandler
          .getKiraClientRegisterContextDataHandlerInstance();
      if (null != kiraClientRegisterContextDataHandler) {
        kiraClientRegisterContextDataHandler.destroy();
      }
      IKiraClientInternalFacade kiraClientInternalFacade = KiraClientInternalFacade
          .getKiraClientInternalFacade();
      if (null != kiraClientInternalFacade) {
        kiraClientInternalFacade.destroy();
      }
    } finally {
      //JumperMessageSender.destroy();

      //Do not use crossMultiZone stuff for kira-client now
      //KiraClientCrossMultiZoneUtils.destroyKiraClientCrossMultiZoneUtils();
    }
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    KiraClientDataCenter.setAllSchedulerInitialized(true);
    return bean;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

}
