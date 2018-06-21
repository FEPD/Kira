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
import com.yihaodian.architecture.hedwig.common.util.HedwigJsonUtil;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class WRRBalancer extends AbstractBalancer {

  private static Logger logger = LoggerFactory.getLogger(WRRBalancer.class);

  @Override
  protected ServiceProfile doSelect() {
    int key = position.getAndIncrement();
    int totalSize = profileCircle.size();
    int realPos = key % totalSize;
    if (key > InternalConstants.INTEGER_BARRIER) {
      position.set(0);
    }
    return getProfileFromCircle(realPos);
  }

  @Override
  public void updateProfiles(Collection<ServiceProfile> serviceSet) {
    lock.lock();
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("####----updateProfiles.serviceSet=" + HedwigJsonUtil.toJSONString(serviceSet)
            + "----####----");
      }
      Circle<Integer, ServiceProfile> circle = new Circle<Integer, ServiceProfile>();
      int size = 0;
      Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, whiteList);
      for (ServiceProfile sp : realServiceSet) {
        int weight = sp.getWeighted();
        for (int i = 0; i < weight; i++) {
          circle.put(size++, sp);
        }
      }
      profileCircle = circle;
    } finally {
      lock.unlock();
    }
  }

}
