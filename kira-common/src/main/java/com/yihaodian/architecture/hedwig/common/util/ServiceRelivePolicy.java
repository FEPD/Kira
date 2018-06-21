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
package com.yihaodian.architecture.hedwig.common.util;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class ServiceRelivePolicy implements RelivePolicy {

  private static final int DEFAULT_RELIVE_INTERVAL = 500;
  Logger logger = LoggerFactory.getLogger(ServiceRelivePolicy.class);
  private String providerHost = "";
  private volatile int tryCount = 0;
  private volatile int threshold = InternalConstants.DEFAULT_RELIVE_THRESHOLD;
  private int SCALE = 2;
  private int COUNT_LIMIT = 60000;
  private volatile long start = 0;
  private volatile long interval = DEFAULT_RELIVE_INTERVAL;
  private long TIME_LIMIT = 60000;

  public ServiceRelivePolicy(String providerHost) {
    super();
    this.providerHost = providerHost;
  }

  public ServiceRelivePolicy() {
    super();
  }

  @Override
  public boolean tryRelive() {
    boolean value = false;
    boolean vc = meetCountPolicy();
    boolean vt = meetTimePolicy();
    if (vc || vt) {
      interval = interval * SCALE;
      interval = interval < TIME_LIMIT ? interval : TIME_LIMIT;
      threshold = threshold * SCALE;
      threshold = threshold < COUNT_LIMIT ? threshold : COUNT_LIMIT;
      value = true;
    }
    return value;
  }

  private boolean meetTimePolicy() {
    boolean v = false;
    if (start == 0) {
      start = System.currentTimeMillis();
    } else {
      long tmp = System.currentTimeMillis() - start;
      if (tmp > interval) {
        start = System.currentTimeMillis();
        v = true;
        logger.warn(
            InternalConstants.LOG_PROFIX + providerHost + " node revive due to meetTimePolicy:"
                + interval + "ms");
      }
    }
    return v;
  }

  private boolean meetCountPolicy() {
    boolean v = false;
    tryCount++;
    if (tryCount >= threshold) {
      tryCount = 0;
      v = true;
      logger.warn(
          InternalConstants.LOG_PROFIX + providerHost + " node revive due to meetCountPolicy:"
              + threshold);
    }
    return v;
  }

  @Override
  public void reset() {
    tryCount = 0;
    threshold = InternalConstants.DEFAULT_RELIVE_THRESHOLD;
    start = 0;
    interval = DEFAULT_RELIVE_INTERVAL;
  }

  public String getProviderHost() {
    return providerHost;
  }

  public void setProviderHost(String providerHost) {
    this.providerHost = providerHost;
  }
}
