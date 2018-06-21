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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

  public HedwigScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize);
  }

  public HedwigScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, threadFactory);
  }

  public HedwigScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
    super(corePoolSize, handler);
  }

  public HedwigScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, threadFactory, handler);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
    return super.schedule(buildHedwigRunnable(task), delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period,
      TimeUnit unit) {
    return super.scheduleAtFixedRate(buildHedwigRunnable(task), initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
      TimeUnit unit) {
    return super.scheduleWithFixedDelay(buildHedwigRunnable(task), initialDelay, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
    return super.schedule(buildHedwigCallable(task), delay, unit);
  }

  @Override
  public void execute(Runnable task) {
    super.execute(buildHedwigRunnable(task));
  }

  @Override
  public Future<?> submit(Runnable task) {
    return super.submit(buildHedwigRunnable(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return super.submit(buildHedwigRunnable(task), result);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return super.submit(buildHedwigCallable(task));
  }

  private HedwigRunnable buildHedwigRunnable(Runnable command) {
    return new HedwigRunnable(HedwigContextUtil.getInvocationContext(), command);
  }

  private <T> HedwigCallable<T> buildHedwigCallable(Callable<T> task) {
    return new HedwigCallable<T>(HedwigContextUtil.getInvocationContext(), task);
  }
}
