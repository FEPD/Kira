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
package com.yihaodian.architecture.hedwig.client.event;

import java.util.concurrent.TimeUnit;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Archer
 */
public class SchedulerEvent extends BaseEvent {


  /**
   *
   */
  private static final long serialVersionUID = -9165984817361999258L;
  private long delay;
  private TimeUnit delayUnit;

  public SchedulerEvent(MethodInvocation invocation) {
    super(invocation);
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public TimeUnit getDelayUnit() {
    return delayUnit;
  }

  public void setDelayUnit(TimeUnit delayUnit) {
    this.delayUnit = delayUnit;
  }

}
