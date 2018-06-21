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
package com.yihaodian.architecture.hedwig.common.constants;

public enum ExternalFlag {

  NONE(0), GRAY(1), ABTEST(2);

  private int code;

  ExternalFlag(int code) {
    this.code = code;
  }

  public static ExternalFlag getFlagByCode(int code) {
    ExternalFlag rf = ExternalFlag.NONE;
    for (ExternalFlag flg : ExternalFlag.values()) {
      if (flg.getCode() == code) {
        rf = flg;
      }
    }
    return rf;
  }

  public int getCode() {
    return code;
  }
}
