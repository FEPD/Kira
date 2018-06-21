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
package com.yihaodian.architecture.hedwig.balancer;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.hash.HashFunction;
import com.yihaodian.architecture.hedwig.common.hash.HashFunctionFactory;
import java.util.Collection;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Archer Jiang
 */
public class ConsistentHashBalancer implements ConditionLoadBalancer<ServiceProfile, String> {

  private Circle<Long, ServiceProfile> profileCircle = new Circle<Long, ServiceProfile>();
  private Lock lock = new ReentrantLock();
  private HashFunction hf = HashFunctionFactory.getInstance().getMur2Function();
  private Random random = new Random();
  private Collection<String> whiteList = null;

  @Override
  public ServiceProfile select() {

    ServiceProfile sp = null;
    if (profileCircle != null && profileCircle.size() > 0) {
      if (profileCircle.size() == 1) {
        sp = profileCircle.firstVlue();
        if (!sp.isAvailable()) {
          sp = null;
        }
      } else {
        long code = hf.hash64(System.nanoTime() + "-" + random.nextInt(99));
        sp = getProfileFromCircle(code);
      }
    }
    return sp;
  }

  private ServiceProfile getProfileFromCircle(Long code) {
    int size = profileCircle.size();
    ServiceProfile sp = null;
    if (size > 0) {
      Long tmp = code;
      while (size > 0) {
        tmp = profileCircle.lowerKey(tmp);
        sp = profileCircle.get(tmp);
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
  public void updateProfiles(Collection<ServiceProfile> serviceSet) {
    lock.lock();
    try {
      Circle<Long, ServiceProfile> circle = new Circle<Long, ServiceProfile>();
      Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, whiteList);
      int totalWeight = getTotalWeight(realServiceSet);
      int size = realServiceSet.size();
      for (ServiceProfile sp : realServiceSet) {
        int mirror = getMirrorFactor(size, sp.getWeighted(), totalWeight,
            InternalConstants.MIRROR_SEED);
        for (int i = 0; i < mirror; i++) {
          String feed = sp.getServiceUrl() + i;
          long key = hf.hash64(feed);
          put2Circle(key, sp, circle);
        }
      }
      profileCircle = circle;
    } finally {
      lock.unlock();
    }

  }

  private void put2Circle(long key, ServiceProfile sp, TreeMap<Long, ServiceProfile> circle) {
    if (circle.containsKey(key)) {
      Long lower = circle.lowerKey(key);
      if (lower == null) {
        key = key / 2;
      } else {
        key = lower + (key - lower) / 2;
      }
      put2Circle(key, sp, circle);
    } else {
      circle.put(key, sp);
    }
  }

  private int getTotalWeight(Collection<ServiceProfile> serviceSet) {
    int value = 1;
    if (serviceSet != null && serviceSet.size() > 0) {
      for (ServiceProfile sp : serviceSet) {
        value += sp.getWeighted();
      }
    }

    return value;
  }

  private int getMirrorFactor(int size, int weighted, int totalWeight, int seed) {
    int value = totalWeight;
    value = seed * size * weighted / totalWeight;
    return value;
  }

  @Override
  public ServiceProfile select(String condition) {
    ServiceProfile sp = null;
    if (profileCircle != null && profileCircle.size() > 0) {
      if (profileCircle.size() == 1) {
        sp = profileCircle.firstVlue();
        if (!sp.isAvailable()) {
          sp = null;
        }
      } else {
        long code = hf.hash64(condition);
        sp = getProfileFromCircle(code);
      }
    }
    return sp;
  }

  @Override
  public void setWhiteList(Collection<String> serviceSet) {
    this.whiteList = serviceSet;

  }


}
