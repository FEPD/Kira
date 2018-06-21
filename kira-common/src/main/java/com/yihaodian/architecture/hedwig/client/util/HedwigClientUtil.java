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
package com.yihaodian.architecture.hedwig.client.util;

import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.uuid.UUID;
import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import java.net.MalformedURLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Archer
 */
public class HedwigClientUtil {

  private static String PoolName = "";
  private static volatile long mark = -1;
  private static int SEQUENCE = 0;
  private static Lock lock = new ReentrantLock();
  private static String shortIP = "";

  static {
    genShortIp();
  }

  public static Object getHessianProxy(HedwigContext context, String serviceUrl)
      throws MalformedURLException {
    Object proxy = null;
    if (context.getHessianProxyMap().containsKey(serviceUrl)) {
      proxy = context.getHessianProxyMap().get(serviceUrl);
    } else {
      proxy = createProxy(context, serviceUrl);
    }
    return proxy;
  }

  public static Object createProxy(HedwigContext context, String serviceUrl)
      throws MalformedURLException {
    Object proxy = null;
    proxy = context.getProxyFactory().create(context.getServiceInterface(), serviceUrl);
    if (proxy != null) {
      context.getHessianProxyMap().put(serviceUrl, proxy);
    }
    return proxy;
  }

  public static String generateReqId(IEvent<Object> event) {
    String reqId = "";
    long t = HedwigUtil.getCurrentTime();
    reqId = "req-" + t + "-" + shortIP + event.hashCode() + getSeq();
    return reqId;
  }

  public static String generateGlobalId(IEvent<Object> event) {
    String glbId = "";
    long t = HedwigUtil.getCurrentTime();
    glbId = "glb-" + t + "-" + shortIP + event.hashCode() + getSeq();
    return glbId;
  }

  private static int getSeq() {
    lock.lock();
    try {
      int v = 0;
      long l = System.currentTimeMillis();
      if (mark < l) {
        mark = l;
        SEQUENCE = 0;
        v = 0;
      } else {
        v = ++SEQUENCE;
      }
      return v;
    } finally {
      lock.unlock();
    }
  }

  public static String generateTransactionId() {
    String txnId = "";
    txnId = "txn-" + new UUID().toString();
    return txnId;
  }

  public static int getRedoCount(HedwigContext context) {
    int nodeCount = context.getLocator().getAllService().size();
    int redoCount = nodeCount >= 1 ? (nodeCount - 1) : 0;
    return redoCount;
  }

  public static void genShortIp() {
    StringBuilder sb = new StringBuilder();
    String hostIp = ProperitesContainer.client().getProperty(PropKeyConstants.HOST_IP, "");
    if (!HedwigUtil.isBlankString(hostIp)) {
      String[] nodes = hostIp.split("\\.");
      if (nodes != null && nodes.length == 4) {
        sb.append(nodes[2]).append(".").append(nodes[3]).append("-");
      }
    }
    shortIP = sb.toString();
  }

  public static String generateReqId(IEvent<Object> event, String clientAppName,
      String serviceAppName) {
    String reqId = "";
    long t = HedwigUtil.getCurrentTime();
    reqId = clientAppName + "-" + serviceAppName + "-" + t + "-" + shortIP + event.hashCode()
        + getSeq();
    return reqId;
  }

  public static String getClientPoolName() {
    if (HedwigUtil.isBlankString(PoolName)) {
      lock.lock();
      try {
        PoolName = KiraUtil.appId();
      } finally {
        lock.unlock();
      }
    }
    return PoolName;
  }

}
