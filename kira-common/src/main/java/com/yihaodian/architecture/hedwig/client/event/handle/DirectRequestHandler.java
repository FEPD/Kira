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
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.exception.HandlerException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Archer
 */
public class DirectRequestHandler extends BaseHandler {

  @Override
  protected Object doHandle(HedwigContext context, BaseEvent event) throws HandlerException {
    Object hessianProxy = null;
    Object result = null;
    String sUrl = context.getClientProfile().getTarget();
    String reqId = event.getReqestId();
    if (HedwigUtil.isBlankString(sUrl)) {
      throw new HandlerException(reqId, "Target url must not null!!!");
    }
    String host = HedwigUtil.getHostFromUrl(sUrl);
    event.setTryHost(host);
    try {
      hessianProxy = HedwigClientUtil.getHessianProxy(context, sUrl);
    } catch (Exception e) {
      throw new HandlerException(reqId, e.getCause());
    }

    if (hessianProxy == null) {
      context.getHessianProxyMap().remove(sUrl);
      throw new HandlerException(reqId, "HedwigHessianInterceptor is not properly initialized");
    }
    try {
      MethodInvocation invocation = event.getInvocation();
      result = invocation.getMethod().invoke(hessianProxy, invocation.getArguments());
      event.setState(EventState.sucess);
    } catch (Throwable e) {
      event.setState(EventState.failed);
      event.setRemoteException(e.getCause());
      throw new HandlerException(reqId, e.getCause());
    }
    return result;
  }

}
