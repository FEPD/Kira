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
package com.yihaodian.architecture.hedwig.engine.event;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import org.aopalliance.intercept.MethodInvocation;

public interface IEvent<T> extends Serializable {

  public boolean isRetryable();

  public int getExecCount();

  public long getExpireTime();

  public TimeUnit getExpireTimeUnit();

  public void increaseExecCount();

  public T getResult();

  public void setResult(T result);

  public MethodInvocation getInvocation();

  public EventState getState();

  public void setState(EventState state);

  public void setErrorMessage(String errorMessage);

  public String getReqestId();

  public long getStart();
}
