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
package com.yihaodian.architecture.kira.manager.crossmultizone;

import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneEventHandleComponent;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.IKiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;

/**
 * The instance of this class can not be restarted.
 */
public class KiraManagerCrossMultiZoneEventHandleComponent extends
    KiraCrossMultiZoneEventHandleComponent {

  private static KiraManagerCrossMultiZoneEventHandleComponent kiraManagerCrossMultiZoneEventHandleComponent;

  private KiraManagerCrossMultiZoneEventHandleComponent() throws Exception {
    this.init();
  }

  public static synchronized KiraManagerCrossMultiZoneEventHandleComponent getKiraManagerCrossMultiZoneEventHandleComponent()
      throws Exception {
    if (null == kiraManagerCrossMultiZoneEventHandleComponent) {
      KiraManagerCrossMultiZoneEventHandleComponent.kiraManagerCrossMultiZoneEventHandleComponent = new KiraManagerCrossMultiZoneEventHandleComponent();
      KiraManagerCrossMultiZoneEventHandleComponent.kiraManagerCrossMultiZoneEventHandleComponent
          .start();
    }

    return KiraManagerCrossMultiZoneEventHandleComponent.kiraManagerCrossMultiZoneEventHandleComponent;
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
    this.doForZoneChange();
  }

  private void doForZoneChange() throws Exception {
    logger.warn("Begin doForZoneChange in KiraManagerCrossMultiZoneEventHandleComponent...");
    long startTime = System.currentTimeMillis();
    try {
      //Now have 2 strategy:
      //1. restart
//			KiraManagerCoreBootstrap kiraManagerCoreBootstrap = KiraManagerDataCenter.getKiraManagerCoreBootstrap();
//			if(null!=kiraManagerCoreBootstrap) {
//				KiraManagerDataCenter.getKiraManagerCoreBootstrap().restart();
//			}

      //2. or just do parts
      IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
          .getKiraTimerTriggerScheduleCenter();
      kiraTimerTriggerScheduleCenter.handleZoneRoleChange(this.kiraCrossMultiZoneRole);

      logger.warn("Successfully doForZoneChange in KiraManagerCrossMultiZoneEventHandleComponent.");
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn(
          "Finish doForZoneChange in KiraManagerCrossMultiZoneEventHandleComponent. And it takes "
              + costTime + " milliseconds.");
    }
  }

  @Override
  protected void afterChangeToWorkAsSlaveZone() throws Exception {
    super.afterChangeToWorkAsSlaveZone();
    this.doForZoneChange();
  }

}
