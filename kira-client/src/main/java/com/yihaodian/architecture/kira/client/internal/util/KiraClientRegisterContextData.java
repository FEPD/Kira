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
package com.yihaodian.architecture.kira.client.internal.util;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;

public class KiraClientRegisterContextData {

  private IEnvironment environment;
  private KiraClientRegisterData kiraClientRegisterData;
  private RegisterStatusEnum registerStatus = RegisterStatusEnum.UNREGISTERED;

  public KiraClientRegisterContextData() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public IEnvironment getEnvironment() {
    return environment;
  }

  public void setEnvironment(IEnvironment environment) {
    this.environment = environment;
  }

  public KiraClientRegisterData getKiraClientRegisterData() {
    return kiraClientRegisterData;
  }

  public void setKiraClientRegisterData(
      KiraClientRegisterData kiraClientRegisterData) {
    this.kiraClientRegisterData = kiraClientRegisterData;
  }

  public RegisterStatusEnum getRegisterStatus() {
    return registerStatus;
  }

  public void setRegisterStatus(RegisterStatusEnum registerStatus) {
    this.registerStatus = registerStatus;
  }

  @Override
  public String toString() {
    return "KiraClientRegisterContextData [environment=" + environment
        + ", kiraClientRegisterData=" + KiraCommonUtils.toString(kiraClientRegisterData)
        + ", registerStatus=" + registerStatus + "]";
  }

}
