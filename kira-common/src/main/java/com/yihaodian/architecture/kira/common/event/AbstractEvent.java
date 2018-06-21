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
package com.yihaodian.architecture.kira.common.event;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.util.Date;

public abstract class AbstractEvent<EventTYPE extends Enum<EventTYPE>> implements
    Event<EventTYPE> {

  protected final EventTYPE eventType;
  protected final Date eventTime;

  public AbstractEvent(EventTYPE eventType) {
    this.eventType = eventType;
    this.eventTime = new Date();
  }

  public AbstractEvent(EventTYPE eventType, Date eventTime) {
    this.eventType = eventType;
    this.eventTime = eventTime;
  }

  @Override
  public Date getEventTime() {
    return eventTime;
  }

  @Override
  public EventTYPE getEventType() {
    return eventType;
  }

  @Override
  public String toString() {
    return "EventType=" + getEventType() + " and eventTime=" + KiraCommonUtils
        .getDateAsString(eventTime);
  }
}
