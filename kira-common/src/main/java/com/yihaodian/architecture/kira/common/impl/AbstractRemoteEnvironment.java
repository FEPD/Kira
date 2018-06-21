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
package com.yihaodian.architecture.kira.common.impl;

import com.yihaodian.architecture.kira.common.iface.IRemoteAccessable;

public abstract class AbstractRemoteEnvironment extends AbstractEnvironment implements
    IRemoteAccessable {

  private static final long serialVersionUID = 1L;

  protected String serviceUrl;

  public AbstractRemoteEnvironment() {
    // TODO Auto-generated constructor stub
  }

  public AbstractRemoteEnvironment(String serviceUrl) {
    super();
    this.serviceUrl = serviceUrl;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AbstractRemoteEnvironment)) {
      return false;
    }
    AbstractRemoteEnvironment other = (AbstractRemoteEnvironment) obj;
    if (serviceUrl == null) {
      if (other.serviceUrl != null) {
        return false;
      }
    } else if (!serviceUrl.equals(other.serviceUrl)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RemoteEnvironment [serviceUrl=" + serviceUrl
        + ", environmentStatus=" + this.getEnvironmentStatus() + "]";
  }

}
