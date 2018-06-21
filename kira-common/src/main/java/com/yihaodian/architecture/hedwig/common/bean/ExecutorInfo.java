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
package com.yihaodian.architecture.hedwig.common.bean;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Archer
 */
public class ExecutorInfo {

  private int poolCoreSize;
  private int poolMaxSize;
  private int poolCurSize;
  /**
   * active thread in thread pool
   */
  private int activeThread;
  /**
   * The largest pool size the pool reached.
   */
  private int poolLargestSize;
  private int queueCurSize;
  private int queueRemainCapacity;

  public ExecutorInfo(ThreadPoolExecutor executor) {
    poolCurSize = executor.getPoolSize();
    poolCoreSize = executor.getCorePoolSize();
    poolMaxSize = executor.getMaximumPoolSize();
    poolLargestSize = executor.getLargestPoolSize();
    activeThread = executor.getActiveCount();
    queueCurSize = executor.getQueue().size();
    queueRemainCapacity = executor.getQueue().remainingCapacity();
  }

  public int getPoolCoreSize() {
    return poolCoreSize;
  }

  public void setPoolCoreSize(int poolCoreSize) {
    this.poolCoreSize = poolCoreSize;
  }

  public int getPoolMaxSize() {
    return poolMaxSize;
  }

  public void setPoolMaxSize(int poolMaxSize) {
    this.poolMaxSize = poolMaxSize;
  }

  public int getPoolCurSize() {
    return poolCurSize;
  }

  public void setPoolCurSize(int poolCurSize) {
    this.poolCurSize = poolCurSize;
  }

  public int getActiveThread() {
    return activeThread;
  }

  public void setActiveThread(int activeThread) {
    this.activeThread = activeThread;
  }

  public int getPoolLargestSize() {
    return poolLargestSize;
  }

  public void setPoolLargestSize(int poolLargestSize) {
    this.poolLargestSize = poolLargestSize;
  }

  public int getQueueCurSize() {
    return queueCurSize;
  }

  public void setQueueCurSize(int queueCurSize) {
    this.queueCurSize = queueCurSize;
  }

  public int getQueueRemainCapacity() {
    return queueRemainCapacity;
  }

  public void setQueueRemainCapacity(int queueRemainCapacity) {
    this.queueRemainCapacity = queueRemainCapacity;
  }

}
