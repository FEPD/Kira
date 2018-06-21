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

import com.yihaodian.architecture.kira.common.akka.entrypoint.WorkerActor;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import java.util.Date;

/**
 * Created by zhoufeiqiang on 14/09/2017.
 */
public class JobRegisterMetaWorkerActor extends WorkerActor {

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof KiraClientRegisterData) {
      KiraClientRegisterData kiraClientRegisterData = (KiraClientRegisterData) message;

      if (null != kiraClientRegisterData) {
        //change to use the server side time instead of client side time.
        kiraClientRegisterData.setCreateTime(new Date());
        BaseActor.kiraClientMetadataService.handleKiraClientRegisterData(kiraClientRegisterData);
      }
      getSender()
          .tell("register data " + kiraClientRegisterData.getApplicationId() + " receive ok !",
              getSelf());
    }
  }
}
