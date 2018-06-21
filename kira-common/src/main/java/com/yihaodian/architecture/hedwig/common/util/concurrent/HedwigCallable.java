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

package com.yihaodian.architecture.hedwig.common.util.concurrent;

import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.InvocationContext;
import java.util.concurrent.Callable;

/**
 * @author Hikin Yao
 * @version 1.0
 */
class HedwigCallable<V> implements Callable<V> {

  private InvocationContext invocationContext;
  private Callable<V> task;

  public HedwigCallable(InvocationContext invocationContext, Callable<V> task) {
    if (invocationContext != null) {
      this.invocationContext = invocationContext.clone();//注意对象复制
    }
    this.task = task;
  }

  @Override
  public V call() throws Exception {
    try {
      HedwigContextUtil.setInvocationContext(this.invocationContext);
      return this.task.call();
    } finally {
      HedwigContextUtil.cleanGlobal();
    }
  }
}
