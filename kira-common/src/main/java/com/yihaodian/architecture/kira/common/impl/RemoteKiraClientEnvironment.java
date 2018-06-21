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

import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;

public class RemoteKiraClientEnvironment extends AbstractRemoteEnvironment {

  private static final long serialVersionUID = 1L;

  protected transient ICentralScheduleService centralScheduleService = null;

  public RemoteKiraClientEnvironment() {
    // TODO Auto-generated constructor stub
  }

  public RemoteKiraClientEnvironment(String serviceUrl,
      ICentralScheduleService centralScheduleService) {
    super(serviceUrl);
    this.centralScheduleService = centralScheduleService;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public ICentralScheduleService getCentralScheduleService() {
    return centralScheduleService;
  }

  public void setCentralScheduleService(
      ICentralScheduleService centralScheduleService) {
    this.centralScheduleService = centralScheduleService;
  }

  @Override
  public String toString() {
    return "RemoteKiraClientEnvironment [centralScheduleService="
        + centralScheduleService + ", serviceUrl=" + serviceUrl
        + ", environmentStatus=" + environmentStatus + "]";
  }

}
