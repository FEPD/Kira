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
package com.yihaodian.architecture.kira.manager.health.monitor.task;

import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KiraManagerMonitorTaskComponent {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  protected MonitorContext monitorContext;

  public KiraManagerMonitorTaskComponent() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public MonitorContext getMonitorContext() {
    return monitorContext;
  }

}
