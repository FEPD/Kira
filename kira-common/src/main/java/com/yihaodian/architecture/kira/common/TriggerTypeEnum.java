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

public enum TriggerTypeEnum {
  SIMPLETRIGGER("SimpleTrigger"),
  CRONTRIGGER("CronTrigger");

  private String name;

  TriggerTypeEnum(String name) {
    this.name = name;
  }

  public static boolean isSimpleTrigger(String triggerType) {
    return TriggerTypeEnum.SIMPLETRIGGER.getName().equals(triggerType);
  }

  public static boolean isCronTrigger(String triggerType) {
    return TriggerTypeEnum.CRONTRIGGER.getName().equals(triggerType);
  }

  public static TriggerTypeEnum getTriggerTypeEnumByTriggerType(String triggerType) {
    TriggerTypeEnum returnValue = null;
    for (TriggerTypeEnum triggerTypeEnum : TriggerTypeEnum.values()) {
      if (triggerTypeEnum.name.equals(triggerType)) {
        returnValue = triggerTypeEnum;
        break;
      }
    }
    return returnValue;
  }

  public String getName() {
    return name;
  }

}
