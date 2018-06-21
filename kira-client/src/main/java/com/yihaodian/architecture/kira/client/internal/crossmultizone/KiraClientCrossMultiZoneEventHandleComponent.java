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
package com.yihaodian.architecture.kira.client.internal.crossmultizone;

import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneEventHandleComponent;

/**
 * The instance of this class can not be restarted.
 */
public class KiraClientCrossMultiZoneEventHandleComponent extends
    KiraCrossMultiZoneEventHandleComponent {

  private static KiraClientCrossMultiZoneEventHandleComponent kiraClientCrossMultiZoneEventHandleComponent;

  public KiraClientCrossMultiZoneEventHandleComponent() throws Exception {
    this.init();
  }

  public static synchronized KiraClientCrossMultiZoneEventHandleComponent getKiraClientCrossMultiZoneEventHandleComponent()
      throws Exception {
    if (null == kiraClientCrossMultiZoneEventHandleComponent) {
      KiraClientCrossMultiZoneEventHandleComponent.kiraClientCrossMultiZoneEventHandleComponent = new KiraClientCrossMultiZoneEventHandleComponent();
      KiraClientCrossMultiZoneEventHandleComponent.kiraClientCrossMultiZoneEventHandleComponent
          .start();
    }

    return KiraClientCrossMultiZoneEventHandleComponent.kiraClientCrossMultiZoneEventHandleComponent;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void afterChangeToWorkAsMasterZone() throws Exception {
    super.afterChangeToWorkAsMasterZone();
  }

  @Override
  protected void afterChangeToWorkAsSlaveZone() throws Exception {
    super.afterChangeToWorkAsSlaveZone();
  }

}
