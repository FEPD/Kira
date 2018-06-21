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

import com.yihaodian.architecture.kira.common.event.AbstractEvent;
import java.util.Date;

public abstract class KiraServerEvent extends AbstractEvent<KiraServerEventType> {

  public KiraServerEvent(KiraServerEventType eventType) {
    super(eventType);
  }

  public KiraServerEvent(KiraServerEventType eventType, Date eventTime) {
    super(eventType, eventTime);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
