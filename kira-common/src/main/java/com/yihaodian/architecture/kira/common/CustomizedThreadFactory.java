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
package com.yihaodian.architecture.kira.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomizedThreadFactory implements ThreadFactory {

  private String threadNamePrefix;
  private ThreadGroup threadGroup;
  private int threadPriority = Thread.NORM_PRIORITY;
  private boolean daemon = false;
  private volatile AtomicInteger threadCount = new AtomicInteger(0);

  public CustomizedThreadFactory(String threadNamePrefix) {
    this(threadNamePrefix, false);
  }

  public CustomizedThreadFactory(String threadNamePrefix, boolean daemon) {
    this(threadNamePrefix, null, Thread.NORM_PRIORITY, daemon);
  }

  /**
   * @param threadNamePrefix e.g.:"myThread-"
   * @param threadGroup If the threadGroup is null the group will be set to be the same ThreadGroup
   * as the thread that is creating the new thread.
   * @param threadPriority Thread.NORM_PRIORITY will be used by default.
   * @param daemon The default value is false.
   */
  public CustomizedThreadFactory(String threadNamePrefix, ThreadGroup threadGroup,
      int threadPriority, boolean daemon) {
    this.threadNamePrefix = threadNamePrefix;
    this.threadGroup = threadGroup;
    this.threadPriority = threadPriority;
    this.daemon = daemon;
  }

  @Override
  public Thread newThread(Runnable r) {
    int threadNumber = threadCount.incrementAndGet();
    String threadName = threadNamePrefix + threadNumber;
    Thread thread = new Thread(threadGroup, r, threadName);
    thread.setPriority(threadPriority);
    thread.setDaemon(daemon);
    return thread;
  }
}