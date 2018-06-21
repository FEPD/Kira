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
package com.yihaodian.architecture.kira.client.internal.impl;

import com.yihaodian.architecture.kira.common.iface.IRemoteAccessable;

public abstract class RemoteAccessableProcessEnvironment extends
    ProcessEnvironment implements IRemoteAccessable {

  private static final long serialVersionUID = 1L;

  protected String serviceUrl;

  public RemoteAccessableProcessEnvironment() {
    // TODO Auto-generated constructor stub
  }

  public RemoteAccessableProcessEnvironment(Integer pid) {
    super(pid);
    // TODO Auto-generated constructor stub
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
  public String toString() {
    return "RemoteAccessableProcessEnvironment [serviceUrl=" + serviceUrl
        + ", pid=" + pid + ", environmentStatus=" + environmentStatus
        + "]";
  }

}
