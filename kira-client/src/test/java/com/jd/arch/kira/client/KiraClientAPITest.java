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

package com.jd.arch.kira.client;

import com.yihaodian.architecture.kira.client.api.KiraClientAPI;
import com.yihaodian.architecture.kira.client.util.TriggerMetadataClientSideView;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.util.LoadGlobalPropertyConfigurer;
import org.junit.Test;

public class KiraClientAPITest {

  @Test
  public void createKiraJob() throws KiraHandleException {
    {
      String appId = "kira";
      String triggerId = "doJobRunStatistics-Trigger";
      String newTriggerId = "ApiCreateTrigger";
      LoadGlobalPropertyConfigurer.AppId(appId);
      KiraClientAPI.deleteTrigger(appId, newTriggerId);
      //TriggerMetadataClientSideView triggerMetadataClientSideView = KiraClientAPI.getTriggerMetadataClientSideView(appId, triggerId);
      TriggerMetadataClientSideView triggerMetadataClientSideView = new TriggerMetadataClientSideView();
      String targetAppId = appId;
      String targetTriggerId = triggerId;
      //String targetTriggerId = newTriggerId; //invoke myself
      triggerMetadataClientSideView.setAppId(appId);
      triggerMetadataClientSideView.setTriggerId(newTriggerId);
      triggerMetadataClientSideView.setVersion("0.0.1");
      triggerMetadataClientSideView.setTargetAppId(targetAppId);
      triggerMetadataClientSideView.setTargetTriggerId(targetTriggerId);
      triggerMetadataClientSideView.setArgumentsAsJsonArrayString("[]");
//		triggerMetadataClientSideView.setTargetMethod("executeJobSuccess"); //invoke myself
//		triggerMetadataClientSideView.setTargetMethodArgTypes("[]"); //invoke myself
      //triggerMetadataClientSideView.setComments(null);

//		triggerMetadataClientSideView.setTriggerType(KiraClientAPI.TRIGGER_TYPE_SIMPLETRIGGER);
//		triggerMetadataClientSideView.setRepeatInterval(Long.valueOf(20000L));

      triggerMetadataClientSideView.setTriggerType(KiraClientAPI.TRIGGER_TYPE_CRONTRIGGER);
      triggerMetadataClientSideView.setCronExpression("0 30 1 * * ?");

      //triggerMetadataClientSideView.setTriggerType("aaa");

      //triggerMetadataClientSideView.setScheduledLocally(Boolean.TRUE); //The manually created trigger can not invoke itself.

      triggerMetadataClientSideView.setDescription("Kira API 创建定时任务");
      KiraClientAPI.createTrigger(triggerMetadataClientSideView);

      //ccjtodo: invoke myself
    }
  }
}
