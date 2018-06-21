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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Archer
 */
public class HedwigDiscardOldestPolicy<T> implements RejectedExecutionHandler {

  /*
   * Remove the head of the Queue then put Runnable into the tail.
   */
  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    if (!executor.isShutdown()) {
      BlockingQueue<Runnable> queue = executor.getQueue();
      try {
        if (queue.size() > 0) {
          Runnable head = queue.poll();
          if (!queue.offer(r)) {
            cancelFuture(r);
          }
          cancelFuture(head);
        }
      } catch (Exception e) {
      }
    }

  }

  public void cancelFuture(Runnable r) {
    if (r instanceof RunnableFuture) {
      RunnableFuture<T> fhead = (RunnableFuture<T>) r;
      fhead.cancel(false);
    }
  }

}
