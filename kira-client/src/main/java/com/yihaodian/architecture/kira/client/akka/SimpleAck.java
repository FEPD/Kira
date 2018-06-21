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

package com.yihaodian.architecture.kira.client.akka;

import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.util.Timeout;
import java.util.concurrent.TimeUnit;

/**
 * Created by archer on 9/29/15.
 */
public class SimpleAck extends UntypedActor {

  Timeout timeout;

  public SimpleAck() {
    timeout = new Timeout(5000, TimeUnit.MILLISECONDS);
    this.getContext().setReceiveTimeout(timeout.duration());
  }

  @Override
  public void onReceive(Object message) throws Exception {

    if (!message.equals(ReceiveTimeout.getInstance())) {
      //System.out.println("client receiver message === " + message);
    } else {
//			System.out.println("requst timeout" + timeout.toString());
      getSender().tell(timeout, getSelf());
    }
    getContext().stop(getSelf());
  }
}
