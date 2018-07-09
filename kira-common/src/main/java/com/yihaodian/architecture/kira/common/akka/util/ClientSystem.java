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
import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.kira.common.akka.constants.Constants;
import com.yihaodian.architecture.kira.common.akka.constants.PhotonPropKeys;
import com.yihaodian.architecture.kira.common.akka.entrypoint.AckActor;
import com.yihaodian.architecture.kira.common.akka.entrypoint.DeadLetterActor;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author archer
 */
public class ClientSystem {

  private static Logger logger = LoggerFactory.getLogger(ClientSystem.class);
  private static ClientSystem client = new ClientSystem();
  private static ActorSystem _clientSystem;
  private static long DEFAULT_INVOKE_LAG = 1000l;
  private Class ackActorCLass = AckActor.class;
  private Scheduler scheduler;
  private Config config;

  private ClientSystem() {
    try {
      initClientSystem();
      ActorRef actor = _clientSystem.actorOf(Props.create(DeadLetterActor.class));
      _clientSystem.eventStream().subscribe(actor, DeadLetter.class);
      scheduler = _clientSystem.scheduler();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static ClientSystem getInstance() {
    return client;
  }

  private void initClientSystem() {
    Properties properties = new Properties();
    String host = SystemUtil.getLocalhostIp();
    properties.put(PhotonPropKeys.KEY_HOST, host);
    properties.put(PhotonPropKeys.KEY_AKKAPORT, "0");
    config = ConfigFactory.parseProperties(properties)
        .withFallback(ConfigFactory.load().getConfig("client"));
    _clientSystem = ActorSystem.create(Constants.ClientAkkaSystemName, config);
  }

  public ActorSystem getActorSystem() {
    return _clientSystem;
  }

  public long getInvokeLag() {
    return DEFAULT_INVOKE_LAG;
  }

  public Class getAckActorCLass() {
    return ackActorCLass;
  }

  public void setAckActorCLass(Class ackActorCLass) {
    if (ackActorCLass != null && !ackActorCLass.equals(this.ackActorCLass)) {
      this.ackActorCLass = ackActorCLass;
    }
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  public Config getConfig() {
    return config;
  }

}
