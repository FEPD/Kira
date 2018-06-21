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
package com.yihaodian.architecture.kira.server.event;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.server.dto.KiraServerRuntimeData;

public class KiraServerStartedEvent extends KiraServerEvent {

  private KiraServerRuntimeData kiraServerRuntimeData;

  public KiraServerStartedEvent(KiraServerEventType eventType,
      KiraServerRuntimeData kiraServerRuntimeData) {
    super(eventType);
    this.kiraServerRuntimeData = kiraServerRuntimeData;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public KiraServerRuntimeData getKiraServerRuntimeData() {
    return kiraServerRuntimeData;
  }

  @Override
  public String toString() {
    return "KiraServerStartedEvent [kiraServerRuntimeData="
        + kiraServerRuntimeData + ", eventType=" + eventType
        + ", eventTime=" + KiraCommonUtils.getDateAsString(eventTime) + "]";
  }

}
