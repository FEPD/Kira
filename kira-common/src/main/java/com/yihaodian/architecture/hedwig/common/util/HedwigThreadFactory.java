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

import java.util.concurrent.ThreadFactory;

/**
 * @author Archer
 */
public class HedwigThreadFactory implements ThreadFactory {


  /* (non-Javadoc)
   * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
   */
  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r, "Hidwig worker thread");
    t.setDaemon(true);
    t.setPriority(Thread.NORM_PRIORITY);
    return t;
  }

}
