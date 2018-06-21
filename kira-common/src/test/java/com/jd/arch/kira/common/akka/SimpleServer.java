package com.jd.arch.kira.common.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.DefaultResizer;
import akka.routing.Resizer;
import akka.routing.RoundRobinPool;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.kira.common.akka.router.SimpleWorkerActor;
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

  public static void main(String[] args) {
    System.setProperty("global.config.path", "/Users/zhoufeiqiang/config/base");
    Map<String, String> map = new HashMap<String, String>();
    //map.put("akka.actor.default-dispatcher.fork-join-executor.parallelism-min", "6");
    //map.put("akka.actor.default-dispatcher.fork-join-executor.parallelism-max", "16");
    ProperitesContainer.provider().pullAll(map);
    ActorSystem serverSystem = ServerSystem.getInstance().getActorSystem();
    Resizer resizer = new DefaultResizer(8, 8);
    ActorRef ref = serverSystem
        .actorOf(new RoundRobinPool(20).props(Props.create(SimpleWorkerActor.class)), "print");
    logger.info("actorRef====" + ref.toString());
    System.out.println("actorRef====" + ref.toString());
  }
}
