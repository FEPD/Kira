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
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.ServiceStatus;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.engine.exception.HessianProxyException;
import com.yihaodian.architecture.hedwig.engine.exception.ProviderNotFindException;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class SyncRequestHandler extends BaseHandler {

  Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

  @Override
  public Object doHandle(HedwigContext context, BaseEvent event) throws Throwable {

    Object result = null;
    ServiceProfile sp = context.getLocator().getService();
    String reqId = event.getReqestId();
    Object hessianProxy = null;
    if (sp == null) {
      throw new ProviderNotFindException(reqId,
          " Can't find service provider for :" + context.getClientProfile().toString());
    }
    String sUrl = sp.getServiceUrl();
    event.setTryHost(sp.getHostString());
    try {
      hessianProxy = HedwigClientUtil.getHessianProxy(context, sUrl);
    } catch (Exception e) {
      throw new HessianProxyException(reqId, e.getCause());
    }

    if (hessianProxy == null) {
      sp.setCurStatus(ServiceStatus.DISENABLE);
      throw new HessianProxyException(reqId,
          "Service provider is not avaliable!!! " + sp.toString());
    }
    MethodInvocation invocation = event.getInvocation();
    Object[] params = invocation.getArguments();
    try {
      result = invocation.getMethod().invoke(hessianProxy, params);
      if (sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
        sp.setCurStatus(ServiceStatus.ENABLE);
      }
    } catch (Throwable e) {
      event.setRemoteException(e.getCause());
      if (HandlerUtil.isNetworkException(e) && context.getClientProfile().isClientThrottle()
          && !sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
        sp.setCurStatus(ServiceStatus.TEMPORARY_DISENABLE);
        logger.warn(
            InternalConstants.LOG_PROFIX + sp.getHostString() + " has kickout of candidate!!!");
      }
      throw e;
    }

    return result;
  }

}
