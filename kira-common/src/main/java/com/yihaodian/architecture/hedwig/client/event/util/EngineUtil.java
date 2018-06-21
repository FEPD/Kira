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
package com.yihaodian.architecture.hedwig.client.event.util;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.engine.exception.EngineException;
import com.yihaodian.architecture.hedwig.engine.handler.IEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class EngineUtil {

  public static Logger logger = LoggerFactory.getLogger(EngineUtil.class);

  public static Object retry(IEventHandler<HedwigContext, BaseEvent, Object> handler,
      BaseEvent event, HedwigContext context)
      throws EngineException {
    while (event.isRetryable()) {
      try {
        return handler.handle(context, event);
      } catch (Throwable e) {
        logger.error("Event execute " + event.getExecCount() + " times failed!!");
      }
    }
    throw new EngineException("Event execute " + event.getExecCount() + " times failed!!");
  }
}
