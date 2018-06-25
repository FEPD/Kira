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
package com.yihaodian.architecture.kira.manager.akka;

import com.yihaodian.architecture.kira.manager.alarm.AlarmCenter;
import com.yihaodian.architecture.kira.manager.service.JobItemService;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;

public class BaseActor {

  public static KiraClientMetadataService kiraClientMetadataService;
  public static JobItemService jobItemService;
  public static AlarmCenter alarmCenter;

  public KiraClientMetadataService getKiraClientMetadataService() {
    return kiraClientMetadataService;
  }

  public void setKiraClientMetadataService(
      KiraClientMetadataService kiraClientMetadataService) {
    this.kiraClientMetadataService = kiraClientMetadataService;
  }

  public void setJobItemService(JobItemService jobItemService) {
    this.jobItemService = jobItemService;
  }

  public void setAlarmCenter(AlarmCenter alarmCenter) {
    this.alarmCenter = alarmCenter;
  }

}
