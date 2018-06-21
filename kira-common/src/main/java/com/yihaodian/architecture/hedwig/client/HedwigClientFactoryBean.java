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

import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import java.util.Set;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Archer
 */
public class HedwigClientFactoryBean extends HedwigEventInterceptor implements FactoryBean {

  private Object serviceProxy;
  private String serviceAppName;
  private String domainName;
  private String serviceName;
  private String serviceVersion;
  private String target;
  private String clientAppName;
  private String clientVersion;
  private String groupName;
  private Long timeout;
  private boolean autoRedo = false;
  private Set<String> noRetryMethods;
  private boolean clientThrottle = ProperitesContainer.client()
      .getBoolean(PropKeyConstants.HEDWIG_CLIENT_THROTTLE, true);
  private boolean useBroadCast = false;

  public HedwigClientFactoryBean() {
    super();
  }

  public HedwigClientFactoryBean(Class<?> clazz, ClientProfile profile) throws Exception {
    super();
    this.serviceInterface = clazz;
    this.clientProfile = profile;
    afterPropertiesSet();
  }

  public HedwigClientFactoryBean(Class<?> clazz, String domainName, String serviceAppName,
      String serviceName, String serviceVersion,
      String clientAppName, Long timeout) throws Exception {
    super();
    this.serviceInterface = clazz;
    setServiceAppName(serviceAppName);
    setDomainName(domainName);
    setServiceName(serviceName);
    setServiceVersion(serviceVersion);
    setClientAppName(clientAppName);
    setTimeout(timeout);
    afterPropertiesSet();
  }

  public HedwigClientFactoryBean(Class<?> clazz, String domainName, String serviceAppName,
      String serviceName, String serviceVersion,
      String clientAppName, String user, String password, Long timeout) throws Exception {
    super();
    this.serviceInterface = clazz;
    setServiceAppName(serviceAppName);
    setDomainName(domainName);
    setServiceName(serviceName);
    setServiceVersion(serviceVersion);
    setClientAppName(clientAppName);
    setTimeout(timeout);
    setUser(user);
    setPassword(password);
    afterPropertiesSet();
  }

  public HedwigClientFactoryBean(Class<?> clazz, String target, String serviceAppName,
      String clientAppName, Long timeout, String user,
      String password) throws Exception {
    this.serviceInterface = clazz;
    setServiceAppName(serviceAppName);
    setTarget(target);
    setClientAppName(clientAppName);
    setTimeout(timeout);
    setUser(user);
    setPassword(password);
    afterPropertiesSet();
  }

  public HedwigClientFactoryBean(Class<?> clazz, String target, String serviceAppName,
      String clientAppName, Long timeout)
      throws Exception {
    super();
    this.serviceInterface = clazz;
    setServiceAppName(serviceAppName);
    setTarget(target);
    setClientAppName(clientAppName);
    setTimeout(timeout);
    afterPropertiesSet();
  }

  @Override
  public Object getObject() throws Exception {
    return serviceProxy;
  }

  @Override
  public Class getObjectType() {
    return getServiceInterface();
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws Exception {
    validate(clientProfile);
    super.afterPropertiesSet();
    this.serviceProxy = new ProxyFactory(getServiceInterface(), this)
        .getProxy(getBeanClassLoader());
  }

  private void validate(ClientProfile clientProfile) throws InvalidParamException {
    if (HedwigUtil.isBlankString(clientProfile.getClientAppName())) {
      throw new InvalidParamException("clientAppName must not blank!!!");
    }
    if (HedwigUtil.isBlankString(target)) {
      if (HedwigUtil.isBlankString(clientProfile.getDomainName())) {
        throw new InvalidParamException("domainName must not blank!!!");
      }
      if (HedwigUtil.isBlankString(clientProfile.getServiceName())) {
        throw new InvalidParamException("serviceName must not blank!!!");
      }
      if (HedwigUtil.isBlankString(clientProfile.getServiceVersion())) {
        throw new InvalidParamException("serviceVersion must not blank!!!");
      }
    }
  }

  public void setServiceAppName(String serviceAppName) {
    clientProfile.setServiceAppName(serviceAppName);
    this.serviceAppName = clientProfile.getServiceAppName();
  }

  public void setServiceName(String serviceName) {
    clientProfile.setServiceName(serviceName);
    this.serviceName = clientProfile.getServiceName();
  }

  public void setServiceVersion(String serviceVersion) {
    clientProfile.setServiceVersion(serviceVersion);
    this.serviceVersion = clientProfile.getServiceVersion();
  }

  public void setTarget(String target) {
    this.target = target;
    clientProfile.setTarget(target);
  }

  public void setTimeout(Long timeout) {
    clientProfile.setTimeout(timeout);
    this.timeout = clientProfile.getTimeout();
  }

  public void setDomainName(String domainName) {
    clientProfile.setDomainName(domainName);
    this.domainName = clientProfile.getDomainName();
  }

  public void setClientAppName(String clientAppName) {
    clientProfile.setClientAppName(clientAppName);
    this.clientAppName = clientProfile.getClientAppName();
  }

  public void setNoRetryMethods(Set<String> noRetryMethods) {
    clientProfile.setNoRetryMethods(noRetryMethods);
    this.noRetryMethods = noRetryMethods;

  }

  public void setGroupName(String groupName) {
    clientProfile.setStrGroupName(groupName);
    this.groupName = groupName;
  }

  public void setAutoRedo(boolean autoRedo) {
    clientProfile.setRedoAble(autoRedo);
    this.autoRedo = clientProfile.isRedoAble();
  }

  public void setClientVersion(String clientVersion) {
    clientProfile.setClientVersion(clientVersion);
    this.clientVersion = clientProfile.getClientVersion();
  }

  public void setClientThrottle(boolean clientThrottle) {
    clientProfile.setClientThrottle(clientThrottle);
    this.clientThrottle = clientProfile.isClientThrottle();
  }

  public void setUseBroadCast(boolean useBroadCast) {
    clientProfile.setUseBroadcast(useBroadCast);
    this.useBroadCast = useBroadCast;

  }

}
