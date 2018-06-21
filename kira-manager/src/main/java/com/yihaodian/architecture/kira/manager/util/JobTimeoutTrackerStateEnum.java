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
package com.yihaodian.architecture.kira.manager.util;

public enum JobTimeoutTrackerStateEnum {
  INITIAL(Integer.valueOf(0)),
  NOT_TIMEOUT(Integer.valueOf(1)),
  TIMEOUT_HANDLED_SUCCESS(Integer.valueOf(2)),
  TIMEOUT_HANDLED_FAILED(Integer.valueOf(3)),
  TIMEOUT_NO_NEED_TO_HANDLE_FOR_JOB_DO_NOT_EXIST(Integer.valueOf(4)),
  TIMEOUT_NO_NEED_TO_HANDLE_FOR_TRIGGER_DO_NOT_EXIST(Integer.valueOf(5)),
  TIMEOUT_NO_NEED_TO_HANDLE_FOR_RUNTIME_THRESHOLD_EMPTY(Integer.valueOf(6)),
  TIMEOUT_NO_NEED_TO_HANDLE_FOR_POOL_DO_NOT_ALARM_OR_ALARM_RECEIVER_NOT_SET(Integer.valueOf(7)),
  TIMEOUT_NO_NEED_TO_HANDLE_FOR_FAILED_TOO_MANY_TIMES(Integer.valueOf(8));

  private Integer state;

  JobTimeoutTrackerStateEnum(Integer state) {
    this.state = state;
  }

  public Integer getState() {
    return state;
  }
}
