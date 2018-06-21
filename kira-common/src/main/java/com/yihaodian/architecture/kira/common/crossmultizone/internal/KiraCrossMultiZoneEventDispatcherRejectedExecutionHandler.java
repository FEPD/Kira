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
package com.yihaodian.architecture.kira.common.crossmultizone.internal;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraCrossMultiZoneEventDispatcherRejectedExecutionHandler implements
    RejectedExecutionHandler {

  private Logger logger = LoggerFactory
      .getLogger(KiraCrossMultiZoneEventDispatcherRejectedExecutionHandler.class);

  @Override
  public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
    logger.error(
        "KiraCrossMultiZoneEventDispatcherRejectedExecutionHandler is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
        runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
        executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
    throw new RejectedExecutionException();
  }

}
