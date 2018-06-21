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
package com.yihaodian.architecture.hedwig.client.event.handle;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.client.util.HedwigMonitorClientUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.handler.IEventHandler;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.yihaodian.monitor.util.MonitorJmsSendUtil;

/**
 * @author Archer
 */
public abstract class BaseHandler implements IEventHandler<HedwigContext, BaseEvent, Object> {

  private Logger logger = LoggerFactory.getLogger(BaseHandler.class);

  @Override
  public Object handle(HedwigContext context, BaseEvent event) throws Throwable {
    event.increaseExecCount();
    ClientBizLog cbLog = null;
    String globalId = HedwigContextUtil.getGlobalId();
    String txnId = HedwigClientUtil.generateTransactionId();
    String reqId = event.getReqestId();
    HedwigContextUtil.setTransactionId(txnId);
    cbLog = HedwigMonitorClientUtil.createClientBizLog(event, context, reqId, globalId, new Date());
    cbLog.setCommId(txnId);
    Object r = null;
    Object[] params = event.getInvocation().getArguments();
    try {
      r = doHandle(context, event);
      event.setState(EventState.sucess);
      event.setResult(r);
      cbLog.setRespTime(new Date());
      cbLog.setSuccessed(MonitorConstants.SUCCESS);
    } catch (Throwable e) {
      event.setState(EventState.failed);
      event.setErrorMessage(e.getMessage());
      cbLog.setInParamObjects(params);
      HedwigMonitorClientUtil.setException(cbLog, e);
      logger.error(
          "\n Event execute failed!!,reqId:" + reqId + ", providerHost:" + event.getTryHostList(),
          e);
      throw e;
    } finally {
      cbLog.setProviderHost(event.getLastTryHost());
      cbLog.setCurtLayer(HedwigContextUtil.getRequestHop());
      cbLog.setLocalLayer(System.nanoTime());
      if (MonitorConstants.FAIL == cbLog.getSuccessed()) {
        cbLog.setLayerType(MonitorConstants.LAYER_TYPE_HANDLER);
        //MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
      }
    }

    return r;
  }

  abstract protected Object doHandle(HedwigContext context, BaseEvent event) throws Throwable;

}
