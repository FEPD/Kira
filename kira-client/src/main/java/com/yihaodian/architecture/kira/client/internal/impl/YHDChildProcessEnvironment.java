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

import com.yihaodian.architecture.kira.common.iface.IPoolAware;

public class YHDChildProcessEnvironment extends RemoteAccessableProcessEnvironment implements
    IPoolAware {

  private static final long serialVersionUID = 1L;

  private String applicationId;

  public YHDChildProcessEnvironment() {
    super();
  }

  public YHDChildProcessEnvironment(Integer pid) {
    super(pid);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  @Override
  public String toString() {
    return "YHDLocalProcessEnvironment [applicationId=" + applicationId
        + ", serviceUrl=" + serviceUrl + ", pid=" + pid
        + ", environmentStatus=" + environmentStatus + ", appId=" + getAppId() + "]";
  }

  @Override
  public String getServiceUrl() {
    //ccjtodo: phase2
    return null;
  }

  @Override
  public String getAppId() {
    //ccjtodo: phase2
    return null;
  }

}
