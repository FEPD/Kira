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

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class HedwigCallerRunPolicy implements RejectedExecutionHandler {

  private Logger logger = LoggerFactory.getLogger(HedwigAbortPolicy.class);

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    logger.debug(
        InternalConstants.ENGINE_LOG_PROFIX + "Hedwig Executor queue size:" + executor.getQueue()
            .size());
    if (!executor.isShutdown()) {
      r.run();
    }

  }

}
