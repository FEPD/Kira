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
package com.yihaodian.architecture.kira.common.akka.util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.actor.Scheduler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.akka.constants.Constants;
import com.yihaodian.architecture.kira.common.akka.constants.PhotonPropKeys;
import com.yihaodian.architecture.kira.common.akka.core.DeadLetterActor;
import com.yihaodian.architecture.kira.common.akka.entrypoint.WorkerActor;
import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author archer
 */
// TODO multi-ActorySystem in JVM
public class ServerSystem {

  private static Logger logger = LoggerFactory.getLogger(ServerSystem.class);

  /*
   * Disalbe jvm bytecode reorder.
   */
  private static volatile ServerSystem server = new ServerSystem();
  private static ActorSystem _serverSystem;
  private Class workActorClass = WorkerActor.class;
  private Scheduler scheduler;

  private ServerSystem() {
    initServerSystem();
  }

  public static ServerSystem getInstance() {
    return server;
  }

  public ActorSystem getActorSystem() {
    return _serverSystem;
  }

  private void initServerSystem() {
    try {
      Config config = null;
      Properties properties = new Properties();
      String host = SystemUtil.getLocalhostIp();
      properties.put(PhotonPropKeys.KEY_HOST, host);
      if (LoadProertesContainer.provider().getIntProperty(PhotonPropKeys.KEY_AKKAPORT, 0) <= 1) {
        properties.put(PhotonPropKeys.KEY_AKKAPORT, Constants.DEFAULT_VALUE_PORT);
      }
      config = ConfigFactory.parseProperties(properties)
          .withFallback(ConfigFactory.load().getConfig("server"));
      _serverSystem = ActorSystem.create(Constants.AkkaSystemName, config);
      ActorRef actor = _serverSystem.actorOf(Props.create(DeadLetterActor.class));
      _serverSystem.eventStream().subscribe(actor, DeadLetter.class);
      scheduler = _serverSystem.scheduler();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

  }

  public Class getWorkActorClass() {
    return workActorClass;
  }

  public void setWorkActorClass(Class workActorClass) {
    if (workActorClass != null && !this.workActorClass.equals(workActorClass)) {
      this.workActorClass = workActorClass;
    }
  }

  public Scheduler getScheduler() {
    return scheduler;
  }
}
