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

public enum JobTypeEnum {

  JAVAJOB("JavaJob"),
  SHELLJOB("ShellJob");


  private String name;

  JobTypeEnum(String name) {
    this.name = name;
  }

  public static boolean isJavaJob(String triggerType) {
    return JobTypeEnum.JAVAJOB.getName().equals(triggerType);
  }

  public static boolean isShellJob(String triggerType) {
    return JobTypeEnum.SHELLJOB.getName().equals(triggerType);
  }

  public static JobTypeEnum getJobTypeEnumByJobType(String triggerType) {
    JobTypeEnum returnValue = null;
    for (JobTypeEnum jobTypeEnum : JobTypeEnum.values()) {
      if (jobTypeEnum.name.equals(triggerType)) {
        returnValue = jobTypeEnum;
        break;
      }
    }
    return returnValue;
  }

  public String getName() {
    return name;
  }
}
