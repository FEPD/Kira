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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.DefaultResizer;
import akka.routing.Resizer;
import akka.routing.RoundRobinPool;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.akka.util.ServerSystem;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by archer on 4/1/15.
 */
public class SimpleServer {

  static Logger logger = LoggerFactory.getLogger(SimpleServer.class);

  public static void initServerActor(String path) {

    Map<String, String> map = new HashMap<String, String>();
    //map.put("akka.actor.default-dispatcher.fork-join-executor.parallelism-min", "6");
    //map.put("akka.actor.default-dispatcher.fork-join-executor.parallelism-max", "16");
    ActorSystem serverSystem = ServerSystem.getInstance().getActorSystem();
    Resizer resizer = new DefaultResizer(8, 8);
    if (path.contains(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA)) {
      ActorRef ref = serverSystem
          .actorOf(new RoundRobinPool(20).props(Props.create(JobRegisterMetaWorkerActor.class)),
              path);
      logger.info("init actorRef====" + path + "===ref == " + ref.toString());

    } else if (path.contains(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS)) {
      ActorRef ref = serverSystem
          .actorOf(new RoundRobinPool(20).props(Props.create(JobStatusWorkerActor.class)), path);
      logger.info("init actorRef====" + path + "===ref == " + ref.toString());
    }
  }
}
