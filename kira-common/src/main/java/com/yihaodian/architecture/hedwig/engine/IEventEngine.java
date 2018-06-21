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
package com.yihaodian.architecture.hedwig.engine;

import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.engine.event.IEvent;
import com.yihaodian.architecture.hedwig.engine.event.IEventContext;
import java.util.concurrent.Future;

/**
 * Event engine is use to invoke event handler in different style such as sync,async,one way, timely
 * scheduler
 *
 * @author Archer
 */
public interface IEventEngine<C extends IEventContext, E extends IEvent<T>, T> {

  /**
   * Invoke event handler in the caller's thread,can't retry.
   */
  public T syncInnerThreadExec(C context, final E event) throws HedwigException;

  /**
   * Invoke event handler in thread pool
   */
  public T syncPoolExec(C context, final E event) throws HedwigException;

  /**
   * Invoke event handler in thread pool
   */
  public Future<T> asyncExec(C context, final E event) throws HedwigException;

  /**
   * Reliable asynchronous request executor,base on message server
   */
  public void asyncReliableExec(C context, final E event) throws HedwigException;

  /**
   * Invoke event handler at most on time
   */
  public T oneWayExec(C context, final E event) throws HedwigException;

  /**
   * Invoke event handler after a specify interval
   */
  public void schedulerExec(C context, final E event) throws HedwigException;

  public Object exec(C context, E event) throws HedwigException;

  public void shutdown();
}
