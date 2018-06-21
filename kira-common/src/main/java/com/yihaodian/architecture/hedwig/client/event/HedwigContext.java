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
package com.yihaodian.architecture.hedwig.client.event;

import com.caucho.hessian.client.HessianProxyFactory;
import com.yihaodian.architecture.hedwig.client.locator.IServiceLocator;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;
import java.util.Map;

/**
 * @author Archer
 */
public class HedwigContext implements IEventContext, Cloneable {

  private IServiceLocator<ServiceProfile> locator;
  private Map<String, Object> hessianProxyMap;
  private ClientProfile clientProfile;
  private HessianProxyFactory proxyFactory;
  private Class serviceInterface;

  public HedwigContext() {
    super();
    // TODO Auto-generated constructor stub
  }

  public HedwigContext(Map<String, Object> hessianProxyMap, ClientProfile clientProfile,
      HessianProxyFactory proxyFactory,
      Class serviceInterface) {
    this.hessianProxyMap = hessianProxyMap;
    this.clientProfile = clientProfile;
    this.proxyFactory = proxyFactory;
    this.serviceInterface = serviceInterface;
  }

  public IServiceLocator<ServiceProfile> getLocator() {
    return locator;
  }

  public void setLocator(IServiceLocator<ServiceProfile> locator) {
    this.locator = locator;
  }

  public Map<String, Object> getHessianProxyMap() {
    return hessianProxyMap;
  }

  public void setHessianProxyMap(Map<String, Object> hessianProxyMap) {
    this.hessianProxyMap = hessianProxyMap;
  }

  public ClientProfile getClientProfile() {
    return clientProfile;
  }

  public void setClientProfile(ClientProfile clientProfile) {
    this.clientProfile = clientProfile;
  }

  public Class getServiceInterface() {
    return serviceInterface;
  }

  public void setServiceInterface(Class serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  public HessianProxyFactory getProxyFactory() {
    return proxyFactory;
  }

  public void setProxyFactory(HessianProxyFactory proxyFactory) {
    this.proxyFactory = proxyFactory;
  }

  @Override
  public HedwigContext clone() throws CloneNotSupportedException {
    HedwigContext context = new HedwigContext(hessianProxyMap, clientProfile, proxyFactory,
        serviceInterface);
    if (locator != null) {
      context.setLocator(locator);
    }
    return context;
  }

  @Override
  public String toString() {
    return "HedwigContext [locator=" + locator + ", hessianProxyMap=" + hessianProxyMap
        + ", clientProfile=" + clientProfile
        + ", proxyFactory=" + proxyFactory + ", serviceInterface=" + serviceInterface + "]";
  }

}
