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

/**
 * @author Archer Jiang
 */
public enum ExceptionTypeConstants {

  NORMAL_MSG(1, "Hathaway Exception Normal:"),
  NORMAL_MSG_CAUSE(2, "Hathaway Exception Normal:");

  private int type;
  private String profix;

  ExceptionTypeConstants(int type, String profix) {
    this.type = type;
    this.profix = profix;
  }

  public int getType() {
    return type;
  }

  public String getProfix() {
    return profix;
  }

}
