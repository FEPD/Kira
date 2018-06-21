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
import java.util.HashSet;
import java.util.Set;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class BroadcastHandler extends BaseHandler {

  Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);

  @Override
  protected Object doHandle(HedwigContext context, BaseEvent event) throws Throwable {
    Object result = null;
    Object obj = null;
    Set<String> sendHosts = new HashSet<String>();
    for (int i = 0; i < 100; i++) {
      ServiceProfile sp = context.getLocator().getService();
      if (sp != null) {
        String sUrl = sp.getServiceUrl();
        String hostString = sp.getHostString();
        if (sendHosts.contains(hostString)) {
          break;
        }
        sendHosts.add(hostString);
        Object hessianProxy = null;
        try {
          hessianProxy = HedwigClientUtil.getHessianProxy(context, sUrl);
        } catch (Exception e) {
          event.setErrorMessage(hostString + "::" + e.getMessage());
        }
        if (hessianProxy != null) {
          MethodInvocation invocation = event.getInvocation();
          Object[] params = invocation.getArguments();
          try {
            obj = invocation.getMethod().invoke(hessianProxy, params);
            if (obj != null) {
              result = obj;
            }
            if (sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
              sp.setCurStatus(ServiceStatus.ENABLE);
            }
          } catch (Throwable e) {
            event.setRemoteException(e.getCause());
            if (HandlerUtil.isNetworkException(e) && context.getClientProfile().isClientThrottle()
                && !sp.getCurStatus().equals(ServiceStatus.TEMPORARY_DISENABLE)) {
              sp.setCurStatus(ServiceStatus.TEMPORARY_DISENABLE);
              logger.info(InternalConstants.LOG_PROFIX + sp.getHostString()
                  + " has kickout of candidate!!!");
            }
          }
        }
      }
    }
    return result;
  }
}
