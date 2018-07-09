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
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.Props;
import akka.dispatch.OnFailure;
import akka.remote.RemotingLifecycleEvent;
import akka.util.Timeout;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.akka.router.HostInfo;
import com.yihaodian.architecture.kira.common.akka.router.RoundRobinBalancer;
import com.yihaodian.architecture.kira.common.akka.util.ClientSystem;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class ProducerMessageActor {

  private static Logger logger = LoggerFactory.getLogger(ProducerMessageActor.class);
  private static ActorSystem clientSystem;
  private static RoundRobinBalancer rbBanlancer = new RoundRobinBalancer();
  private static String rootPath = "akka.tcp://YHDendpointSystem@";
  private static String endPath = ":2552/user/";
  private static int retryTime = 5;
  private static CountDownLatch waitServerRegister = new CountDownLatch(1);


  public ProducerMessageActor() {

  }

  public static void initActorSystem() {
    clientSystem = ClientSystem.getInstance().getActorSystem();
    ActorRef ref = clientSystem.actorOf(Props.create(ListenerActor.class));
    clientSystem.eventStream().subscribe(ref, RemotingLifecycleEvent.class);
    observeKiraServer();


  }

  public static void clientSendJobStatus(final String type, final Object object) {
    clientSendJobStatus(type, object, 0);
  }

  public static void clientSendJobStatus(final String type, final Object content, final int value) {

    final AtomicLong count = new AtomicLong(value);
    if (clientSystem == null) {
      initActorSystem();
    }

    HostInfo hi = rbBanlancer.select();
    if (hi == null) {
      try {
        waitServerRegister.await();
        hi = rbBanlancer.select();
      } catch (InterruptedException e) {
        logger.warn("Can not Find Kira Server IP, discard this message ! " + content);
      }finally {
        waitServerRegister = null;
      }
    }

    if (hi != null) {
      String path = rootPath + hi.getIp() + endPath + type;
      ActorSelection _actorSelection = clientSystem.actorSelection(path);
      _actorSelection.tell(new Identify(path), ActorRef.noSender());
      ActorRef _ack = clientSystem.actorOf(Props.create(SimpleAck.class));
      _actorSelection.tell(content, _ack);

      Future<ActorRef> future = _actorSelection.resolveOne(Timeout.intToTimeout(10000));
      ExecutionContext ec = clientSystem.dispatcher();
      future.onFailure(new OnFailure() {
        public void onFailure(Throwable failure) {
          if (count.get() < retryTime) {
            try {
              Thread.sleep(5000L);
              count.getAndIncrement();
              clientSendJobStatus(type, content, (int) count.get());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              e.printStackTrace();
            }
          } else {
            count.set(0);
            logger.error("client send data fail retry 5 time! " + content.toString());
          }
          logger
              .warn("client send content fail:  " + content + ", retry time : " + count.get());
        }
      }, ec);

           /* future.onSuccess(new OnSuccess<ActorRef>() {
                public void onSuccess(ActorRef result) {
                    System.out.println( "client send content successful === "+ result);
                }
            }, ec);*/
    }
  }


  public static void initBalancer() {

    List<String> kiraServerList = KiraUtil.kiraServer();
    if ( !kiraServerList.isEmpty()) {
       waitServerRegister.countDown();
      Map<String, HostInfo> map = new ConcurrentHashMap<String, HostInfo>();
      for (String connct : kiraServerList) {
        HostInfo h = new HostInfo(connct);
        map.put(connct, h);
      }
      rbBanlancer.updateProfiles(map.values());
    }
  }

  public static void observeKiraServer() {

    String path = KiraCommonConstants.ZK_PATH_APPCENTERS;
    ZkClient zkClient = KiraZkUtil.initDefaultZk();
    if (!zkClient.exists(path)) {
      zkClient.createPersistent(path);
    }

    zkClient.subscribeChildChanges(path, new IZkChildListener() {
      public void handleChildChange(String parentPath, List<String> currentChilds)
          throws Exception {
        if (!HedwigUtil.isBlankString(parentPath)) {
          initBalancer();
          logger.info(
              "Kira Server update: path===" + parentPath + " currentChilds===" + currentChilds);
        }
      }
    });
  }

}
