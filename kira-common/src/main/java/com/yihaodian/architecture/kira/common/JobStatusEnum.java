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

public enum JobStatusEnum {
  CREATED(Integer.valueOf(1), "任务已创建", null),
  UPDATED(Integer.valueOf(2), "任务配置已更新", null),
  UNABLE_TO_DELIVER(Integer.valueOf(3), "任务无法派送", null),
  DELIVERING(Integer.valueOf(4), "任务正在被派送", null),
  DELIVERY_SUCCESS(Integer.valueOf(5), "任务已送达", null),
  DELIVERY_FAILED(Integer.valueOf(6), "任务派送失败", null),
  DELIVERY_PARTIAL_SUCCESS(Integer.valueOf(7), "任务派送部分成功", null),
  UNABLE_TO_RUN(Integer.valueOf(8), "任务无法执行", null),
  RUNNING(Integer.valueOf(9), "任务正在执行", null),
  RUN_SUCCESS(Integer.valueOf(10), "任务执行成功", null),
  RUN_FAILED(Integer.valueOf(11), "任务执行失败", null),
  RUN_PARTIAL_SUCCESS(Integer.valueOf(12), "任务执行部分成功", null),
  NO_NEED_TO_DELIVER(Integer.valueOf(13), "无需派送定时任务", null),
  NO_NEED_TO_RUN_BUSINESS_METHOD(Integer.valueOf(14), "无需执行业务方法", null),
  EXCEPTION_CAUGHT_DURING_SCHEDULE(Integer.valueOf(15), "调度过程中出现异常", null);

  private Integer id;
  private String name;
  private String description;

  JobStatusEnum(Integer id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public static JobStatusEnum getJobStatusEnumById(Integer id) {
    JobStatusEnum returnValue = null;
    JobStatusEnum[] JobStatusEnumArray = JobStatusEnum.values();
    if (null != JobStatusEnumArray) {
      for (JobStatusEnum oneJobStatusEnum : JobStatusEnumArray) {
        if (oneJobStatusEnum.getId().equals(id)) {
          returnValue = oneJobStatusEnum;
          break;
        }
      }
    }

    return returnValue;
  }

  public static String getJobStatusNameById(Integer id) {
    String returnValue = null;
    JobStatusEnum jobStatusEnum = JobStatusEnum.getJobStatusEnumById(id);
    if (null != jobStatusEnum) {
      returnValue = jobStatusEnum.getName();
    }

    return returnValue;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
