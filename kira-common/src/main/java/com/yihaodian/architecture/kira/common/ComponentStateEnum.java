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
package com.yihaodian.architecture.kira.common;

public enum ComponentStateEnum {
  INIT(0),
  STARTING(1),
  STARTED(2),
  SHUTTINGDOWN(3),
  SHUTDOWN(4),
  DESTROYING(5),
  DESTROYED(6);

  private int state;

  ComponentStateEnum(int state) {
    this.state = state;
  }

  public int getState() {
    return this.state;
  }
}
