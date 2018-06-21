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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public abstract class AbstractBalancer implements LoadBalancer<ServiceProfile> {

  private static Logger logger = LoggerFactory.getLogger(AbstractBalancer.class);
  protected volatile Circle<Integer, ServiceProfile> profileCircle = new Circle<Integer, ServiceProfile>();
  protected Lock lock = new ReentrantLock();
  protected Random random = new Random();
  protected AtomicInteger position = new AtomicInteger(
      random.nextInt(InternalConstants.INTEGER_BARRIER));
  protected Collection<String> whiteList = null;

  @Override
  public ServiceProfile select() {
    ServiceProfile sp = null;
    if (logger.isDebugEnabled()) {
      logger.debug("####----AbstractBalancer.select().profileCircle=" + HedwigJsonUtil
          .toJSONString(profileCircle) + "----####----");
    }
    if (profileCircle == null || profileCircle.size() == 0) {
      return null;
    } else if (profileCircle.size() == 1) {
      sp = profileCircle.firstVlue();
      sp = sp.isAvailable() ? sp : null;
    } else {
      sp = doSelect();
    }
    if (sp == null) {
      logger.error("####Can't found ServiceProfile in profileCircle,sp=NULL!! profileCircle="
          + HedwigJsonUtil.toJSONString(profileCircle) + "----####----");
    }
    return sp;
  }

  protected abstract ServiceProfile doSelect();

  protected ServiceProfile getProfileFromCircle(int code) {
    int size = profileCircle.size();
    ServiceProfile sp = null;
    if (size > 0) {
      int tmp = code;
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
  public void setWhiteList(Collection<String> serviceSet) {
    this.whiteList = serviceSet;

  }

}
