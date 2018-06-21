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
package com.yihaodian.architecture.kira.common.akka.router;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractBalancer implements LoadBalancer<HostInfo> {

  protected Circle<Integer, HostInfo> profileCircle = new Circle<Integer, HostInfo>();
  protected Lock lock = new ReentrantLock();
  protected Random random = new Random();
  protected AtomicInteger position = new AtomicInteger(
      random.nextInt(Integer.MAX_VALUE / 2));
  protected Collection<String> whiteList = null;

  protected abstract HostInfo doSelect();

  @Override
  public void setWhiteList(Collection<String> serviceSet) {
    this.whiteList = serviceSet;

  }

  @Override
  public HostInfo select() {
    if (profileCircle == null || profileCircle.size() == 0) {
      return null;
    } else if (profileCircle.size() == 1) {
      HostInfo sp = profileCircle.firstVlue();
      return sp;
    } else {
      return doSelect();
    }
  }

  protected HostInfo getProfileFromCircle(int code) {
    int size = profileCircle.size();
    HostInfo sp = null;
    if (size > 0) {
      int tmp = code;
      while (size > 0) {
        tmp = profileCircle.lowerKey(tmp);
        sp = profileCircle.get(tmp);
        if (sp != null) {
          break;
        }
        size--;
      }
    }
    return sp;
  }

}
