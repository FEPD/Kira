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
import com.yihaodian.architecture.kira.common.TriggerMetadataDetail;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;

public class TriggerRegisterContextData {

  private IEnvironment environment;
  private TriggerMetadataDetail triggerMetaDataDetail;
  private RegisterStatusEnum registerStatus = RegisterStatusEnum.UNREGISTERED;

  //schedulerBeanName will be null if it is not LocalProcessEnvironment
  private String schedulerBeanName;

  public TriggerRegisterContextData() {
    super();
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

  public String getSchedulerBeanName() {
    return schedulerBeanName;
  }

  public void setSchedulerBeanName(String schedulerBeanName) {
    this.schedulerBeanName = schedulerBeanName;
  }

  public TriggerMetadataDetail getTriggerMetaDataDetail() {
    return triggerMetaDataDetail;
  }

  public void setTriggerMetaDataDetail(TriggerMetadataDetail triggerMetaDataDetail) {
    this.triggerMetaDataDetail = triggerMetaDataDetail;
  }

  public RegisterStatusEnum getRegisterStatus() {
    return registerStatus;
  }

  public void setRegisterStatus(RegisterStatusEnum registerStatus) {
    this.registerStatus = registerStatus;
  }

  @Override
  public String toString() {
    return "TriggerRegisterContextData [environment=" + environment
        + ", triggerMetaDataDetail=" + KiraCommonUtils.toString(triggerMetaDataDetail)
        + ", registerStatus=" + registerStatus + ", schedulerBeanName="
        + schedulerBeanName + "]";
  }

}
