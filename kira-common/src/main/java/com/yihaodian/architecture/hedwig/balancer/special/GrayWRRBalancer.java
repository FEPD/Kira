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

package com.yihaodian.architecture.hedwig.balancer.special;

import com.yihaodian.architecture.hedwig.balancer.BalancerUtil;
import com.yihaodian.architecture.hedwig.balancer.Circle;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import com.yihaodian.architecture.hedwig.common.util.StringUtils;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayWRRBalancer implements DynamicLoadBalancer<ServiceProfile, String> {

  private static Logger logger = LoggerFactory.getLogger(GrayWRRBalancer.class);
  protected volatile Circle<Integer, ServiceProfile> normalCircle = new Circle<Integer, ServiceProfile>();
  protected volatile Circle<Integer, ServiceProfile> grayCircle = new Circle<Integer, ServiceProfile>();
  protected Collection<String> groupWhiteList = null;
  protected Collection<String> grayList = null;
  protected volatile int offset = 0;
  protected int MAXTOKEN = 1000000;
  protected volatile Circle<Integer, Boolean> grayWindow = new Circle<Integer, Boolean>();
  protected Random random = new Random();
  protected AtomicInteger npos = new AtomicInteger(
      random.nextInt(InternalConstants.INTEGER_BARRIER));
  protected AtomicInteger gpos = new AtomicInteger(
      random.nextInt(InternalConstants.INTEGER_BARRIER));
  protected Lock lock = new ReentrantLock();

  public GrayWRRBalancer() {
    super();
    grayWindow.put(0, false);
  }

  @Override
  public ServiceProfile select() {
    String token = HedwigContextUtil.getString(PropKeyConstants.HEDWIG_TOKEN_GRAY, "0");
    if (logger.isDebugEnabled()) {
      logger.debug("###[hedwig-client:GrayWRRBalancer.select()] token.gray=" + token);
      logger.debug("####----normalCircle=" + HedwigJsonUtil.toJSONString(normalCircle)
          + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
          + ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow) + "----####----");

    }
    return select(token);
  }

  @Override
  public void updateProfiles(Collection<ServiceProfile> serviceSet) {
    lock.lock();
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("####----updateProfiles.serviceSet=" + HedwigJsonUtil.toJSONString(serviceSet)
            + "----####----");
      }
      Circle<Integer, ServiceProfile> nncircle = new Circle<Integer, ServiceProfile>();
      Circle<Integer, ServiceProfile> ngcircle = new Circle<Integer, ServiceProfile>();
      int nsize = 0;
      int gsize = 0;
      Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, groupWhiteList);
      for (ServiceProfile sp : realServiceSet) {
        int weight = sp.getWeighted();
        if (this.grayList.size() > 0) {
          if (this.grayList.contains(sp.getHostIp())) {
            for (int i = 0; i < weight; i++) {
              ngcircle.put(gsize++, sp);
            }
          } else {
            for (int i = 0; i < weight; i++) {
              nncircle.put(nsize++, sp);
            }
          }
        } else {
          for (int i = 0; i < weight; i++) {
            nncircle.put(nsize++, sp);
          }
        }

      }
      normalCircle = nncircle;
      grayCircle = ngcircle;
      grayWindow = getGrayWindow(nncircle, ngcircle);
      if (logger.isDebugEnabled()) {
        logger.debug("####----normalCircle=" + HedwigJsonUtil.toJSONString(normalCircle)
            + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
            + ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow)
            + "----####----");
      }
    } finally {
      lock.unlock();
    }

  }

  private Circle<Integer, Boolean> getGrayWindow(Circle<Integer, ServiceProfile> nncircle,
      Circle<Integer, ServiceProfile> ngcircle) {
    Circle<Integer, Boolean> circle = new Circle<Integer, Boolean>();
    int ns = nncircle.size();
    int gs = ngcircle.size();
    if (ns > 0 && gs > 0) {
      int pivot = MAXTOKEN * gs / ns;
      circle.put(offset, true);
      circle.put(pivot, false);
    } else {
      circle.put(Integer.valueOf(0), new Boolean(false));
    }
    return circle;
  }

  @Override
  public ServiceProfile select(String userToken) {
    ServiceProfile sp = null;
    int itoken = StringUtils.isNumeric(userToken) ? Integer.valueOf(userToken) : 0;
    if (grayWindow.lowerValue(itoken)) {
      int pos = gpos.incrementAndGet();
      int totalSize = grayCircle.size();
      int realPos = pos % totalSize;
      System.out.print("gray:");
      sp = getProfileFromCircle(grayCircle, realPos);
      if (pos > InternalConstants.INTEGER_BARRIER) {
        gpos.set(0);
      }
    } else {
      int pos = npos.incrementAndGet();
      int totalSize = normalCircle.size();
      int realPos = pos % totalSize;
      System.out.print("norm:");
      sp = getProfileFromCircle(normalCircle, realPos);
      if (pos > InternalConstants.INTEGER_BARRIER) {
        npos.set(0);
      }
    }
    if (sp == null) {
      logger.error("#### ServiceProfile is NULL!! ----normalCircle=" + HedwigJsonUtil
          .toJSONString(normalCircle)
          + ",grayCircle=" + HedwigJsonUtil.toJSONString(grayCircle)
          + ",grayWindow=" + HedwigJsonUtil.toJSONString(grayWindow)
          + "----####----");
    }
    return sp;
  }

  protected ServiceProfile getProfileFromCircle(Circle<Integer, ServiceProfile> circle, int code) {
    int size = circle.size();
    ServiceProfile sp = null;
    if (size > 0) {
      int tmp = code;
      while (size > 0) {
        tmp = circle.higherKey(tmp);
        sp = circle.get(tmp);
        if (sp != null && sp.isAvailable()) {
          break;
        } else {
          sp = null;
        }
        size--;
      }
    }
    return sp;
  }

  @Override
  public void setSpecialList(Collection<String> list) {
    this.grayList = list;
  }

  @Override
  public void setWhiteList(Collection<String> serviceSet) {
    this.groupWhiteList = serviceSet;

  }

}
