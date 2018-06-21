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

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.remote.AssociatedEvent;
import akka.remote.DisassociatedEvent;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by archer on 6/29/15.
 */
public class ListenerActor extends UntypedActor {

  static Logger logger = LoggerFactory.getLogger(ListenerActor.class);

  private ActorRef ref;

  public ListenerActor() {

  }

  public ListenerActor(ActorRef ref) {
    this.ref = ref;
    context().watch(ref);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof DisassociatedEvent) {
      DisassociatedEvent de = (DisassociatedEvent) message;
      logger.error(
          "Disassociated:" + de.getRemoteAddress().hostPort() + ",local" + de.localAddress()
              .hostPort());
    } else if (message instanceof AssociatedEvent) {
      AssociatedEvent ae = (AssociatedEvent) message;
      String a = KiraUtil.getHosturl(ae.getRemoteAddress());
      logger.info("associated remote:" + a + ",local:" + ae.localAddress().hostPort());
    } else if (message instanceof Terminated) {
      logger.error(message.toString());
    } else {
      logger.error(message.toString());
    }
  }
}
