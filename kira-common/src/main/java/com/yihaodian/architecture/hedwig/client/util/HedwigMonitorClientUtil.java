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
package com.yihaodian.architecture.hedwig.client.util;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.common.config.ProperitesContainer;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import java.util.Date;
import java.util.Set;

/**
 * @author Archer
 */
public class HedwigMonitorClientUtil {

  public static ClientBizLog createClientBizLog(BaseEvent event, HedwigContext context,
      String reqId, String globalId, Date reqTime) {
    BaseEvent be = event;
    ClientBizLog cbLog = new ClientBizLog();
    try {
      cbLog.setServicePath(ZkUtil.createAppPath(context.getClientProfile()));
    } catch (InvalidParamException e) {
      e.printStackTrace();
    }
    cbLog.setCallApp(context.getClientProfile().getClientAppName());
    cbLog.setCallHost(ProperitesContainer.client().getProperty(PropKeyConstants.HOST_IP));
    cbLog.setUniqReqId(globalId);
    cbLog.setServiceName(context.getClientProfile().getServiceName());
    cbLog.setProviderApp(context.getClientProfile().getServiceAppName());
    cbLog.setReqId(reqId);
    cbLog.setServiceVersion(context.getClientProfile().getServiceVersion());
    Set<String> groupSet = context.getClientProfile().getGroupNames();
    String groups = (groupSet == null || groupSet.size() == 0) ? InternalConstants.NON_GROUP
        : groupSet.toString();
    cbLog.setServiceGroup(groups);
    cbLog.setReqTime(reqTime);
    cbLog.setMethodName(be.getCallerMethod());
    cbLog.setServiceMethodName(be.getServiceMethod());
    return cbLog;
  }

  public static void setException(ClientBizLog cbLog, Throwable exception) {
    cbLog.setRespTime(new Date());
    cbLog.setSuccessed(MonitorConstants.FAIL);
    cbLog.setExceptionClassname(HedwigMonitorUtil.getExceptionClassName(exception));
    cbLog.setExceptionDesc(HedwigMonitorUtil.getExceptionMsg(exception));
  }

}
