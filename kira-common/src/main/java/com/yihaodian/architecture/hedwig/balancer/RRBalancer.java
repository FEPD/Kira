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
import java.util.Collection;

/**
 * @author Archer
 */
public class RRBalancer extends AbstractBalancer {

  @Override
  public void updateProfiles(Collection<ServiceProfile> serviceSet) {
    lock.lock();
    try {
      Circle<Integer, ServiceProfile> circle = new Circle<Integer, ServiceProfile>();
      int size = 0;
      Collection<ServiceProfile> realServiceSet = BalancerUtil.filte(serviceSet, whiteList);
      for (ServiceProfile sp : realServiceSet) {
        circle.put(size++, sp);
      }
      profileCircle = circle;
    } finally {
      lock.unlock();
    }
  }

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


}
