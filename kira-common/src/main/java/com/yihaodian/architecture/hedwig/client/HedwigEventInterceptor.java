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
package com.yihaodian.architecture.hedwig.client;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.event.HedwigEventBuilder;
import com.yihaodian.architecture.hedwig.client.event.engine.HedwigEventEngine;
import com.yihaodian.architecture.hedwig.client.locator.GroupServiceLocator;
import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxyFactory;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemotingSupport;

//import com.yihaodian.monitor.util.MonitorJmsSendUtil;

/**
 * @author Archer
 */
public class HedwigEventInterceptor extends RemotingSupport implements MethodInterceptor,
    InitializingBean, DisposableBean {

  protected ClientProfile clientProfile = new ClientProfile();
  protected Class serviceInterface;
  protected String user;
  protected String password;
  protected boolean chunkedPost = true;
  protected boolean overloadedEnable = false;
  protected long readTimeout = ProperitesContainer.client()
      .getLongProperty(PropKeyConstants.HEDWIG_READ_TIMEOUT,
          InternalConstants.DEFAULT_READ_TIMEOUT);
  private Logger logger = LoggerFactory.getLogger(HedwigEventInterceptor.class);
  private HedwigHessianProxyFactory proxyFactory = new HedwigHessianProxyFactory();
  private IServiceLocator<ServiceProfile> locator;
  private Map<String, Object> hessianProxyMap = new ConcurrentHashMap<String, Object>();
  private HedwigContext eventContext;
  private HedwigEventBuilder eventBuilder;
  private HedwigEventEngine eventEngine;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Object result = null;
    BaseEvent event = eventBuilder.buildRequestEvent(invocation);
    result = eventEngine.exec(eventContext, event);
    if (!event.getState().equals(EventState.sucess)) {
      Throwable exception = event.getRemoteException();
      if (exception == null) {
        exception = new HedwigException(event.getErrorMessages());
      } else {
        logger.error("\nreqId:" + event.getReqestId() + ", providerHost:" + event.getTryHostList());
      }
      throw exception;
    }
    event = null;
    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    eventEngine = HedwigEventEngine.getEngine();
    proxyFactory.setHessian2Request(true);
    proxyFactory.setHessian2Reply(true);
    proxyFactory.setChunkedPost(clientProfile.isChunkedPost());
    proxyFactory.setOverloadEnabled(clientProfile.isOverloadedEnable());
    proxyFactory.setReadTimeout(clientProfile.getReadTimeout());
    if (!HedwigUtil.isBlankString(clientProfile.getUser()) && !HedwigUtil
        .isBlankString(clientProfile.getPassword())) {
      proxyFactory.setUser(clientProfile.getUser());
      proxyFactory.setPassword(clientProfile.getPassword());
    }
    eventContext = new HedwigContext(hessianProxyMap, clientProfile, proxyFactory,
        serviceInterface);
    try {
      //MonitorJmsSendUtil.getInstance();
      if (!HedwigUtil.isBlankString(clientProfile.getTarget())) {
        HedwigClientUtil.createProxy(eventContext, clientProfile.getTarget());
      } else {
        locator = new GroupServiceLocator(clientProfile);
        Collection<ServiceProfile> serviceProfiles = locator.getAllService();
        eventContext.setLocator(locator);
        try {
          for (ServiceProfile profile : serviceProfiles) {
            HedwigClientUtil.createProxy(eventContext, profile.getServiceUrl());
          }
        } catch (ConcurrentModificationException e) {
          logger.info("Lazy initial hessian proxy");
        }
      }
      eventBuilder = new HedwigEventBuilder(eventContext, clientProfile);
      logger.info("Initial " + clientProfile.getServiceName() + " client successful.");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new HedwigException(e.getCause());
    }

  }

  /**
   * Return the interface of the service to access.
   */
  public Class getServiceInterface() {
    return this.serviceInterface;
  }

  /**
   * Set the interface of the service to access. The interface must be suitable for the particular
   * service and remoting strategy. <p> Typically required to be able to create a suitable service
   * proxy, but can also be optional if the lookup returns a typed proxy.
   */
  public void setServiceInterface(Class serviceInterface) {
    if (serviceInterface != null && !serviceInterface.isInterface()) {
      throw new IllegalArgumentException("'serviceInterface' must be an interface");
    }
    this.serviceInterface = serviceInterface;
  }

  public void setClientProfile(ClientProfile clientProfile) {
    this.clientProfile = clientProfile;
  }

  @Override
  public void destroy() throws Exception {
    hessianProxyMap = null;
    if (eventEngine != null) {
      eventEngine.shutdown();
    }
    //ZkUtil.getZkClientInstance().unsubscribeAll();
    //MonitorJmsSendUtil.destroy();
    KiraZkUtil.initDefaultZk().unsubscribeAll();
  }

  public void setUser(String user) {
    this.clientProfile.setUser(user);
    this.user = user;
  }

  public void setPassword(String password) {
    this.clientProfile.setPassword(password);
    this.password = password;
  }

  public void setChunkedPost(boolean chunkedPost) {
    this.clientProfile.setChunkedPost(chunkedPost);
    this.chunkedPost = chunkedPost;
  }

  public void setOverloadedEnable(boolean overloadedEnable) {
    this.clientProfile.setOverloadedEnable(overloadedEnable);
    this.overloadedEnable = overloadedEnable;
  }

  public void setReadTimeout(long readTimeout) {
    this.clientProfile.setReadTimeout(readTimeout);
    readTimeout = clientProfile.getReadTimeout();
  }

}
