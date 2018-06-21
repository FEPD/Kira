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

package com.yihaodian.architecture.kira.client.util;

import com.yihaodian.architecture.hedwig.provider.AppProfile;
import com.yihaodian.architecture.hedwig.provider.BasicAuthWebserviceExporter;
import com.yihaodian.architecture.kira.client.internal.impl.CentralScheduleService;
import com.yihaodian.architecture.kira.client.internal.util.CentralScheduleServiceContextHolder;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralScheduleServiceExporter extends BasicAuthWebserviceExporter {

  private Logger logger = LoggerFactory.getLogger(CentralScheduleServiceExporter.class);

  public CentralScheduleServiceExporter() {
    super();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setAppProfile(AppProfile appProfile) {
    boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!workWithoutKira) {
      String serviceAppName = appProfile.getServiceAppName();
      if (StringUtils.isBlank(serviceAppName)) {
        throw new IllegalArgumentException("serviceAppName should not be blank.");
      }
      if ("/".equals(serviceAppName.trim())) {
        throw new IllegalArgumentException("serviceAppName should not be of /");
      }
      super.setUserName(KiraCommonConstants.CENTRAL_SCHEDULE_SERVICE_AUTH_USERNAME);
      super.setPassword(KiraCommonConstants.CENTRAL_SCHEDULE_SERVICE_AUTH_PASSWORD);
      super.setService(CentralScheduleService.getCentralScheduleService());
      super.setServiceInterface(ICentralScheduleService.class);
      super.setServiceVersion(KiraCommonConstants.SERVICEVERSION_CENTRALSCHEDULESERVICE);
      super.setAppProfile(appProfile);
      KiraClientDataCenter.setAppProfileForCentralScheduleServiceExporter(appProfile);
    }
  }

  @Override
  public void afterPropertiesSet() {
    boolean workWithoutKira = KiraClientDataCenter.isWorkWithoutKira();
    if (!workWithoutKira) {
      super.afterPropertiesSet();
      KiraClientDataCenter.setCentralScheduleServiceExporter(this);
    }
  }

  @Override
  public void handleRequest(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    try {
      HttpServletRequestDataWrapper httpServletRequestDataWrapper = new HttpServletRequestDataWrapper(
          request);
      CentralScheduleServiceContextHolder
          .setHttpServletRequestDataWrapper(httpServletRequestDataWrapper);
      super.handleRequest(request, response);
    } finally {
      CentralScheduleServiceContextHolder.removeHttpServletRequestDataWrapper();
    }
  }

  @Override
  public void destroy() throws Exception {
    try {
      super.destroy();
    } catch (Exception e) {
      if (null != logger) {
        logger.error("Error occurs for super.destroy(); for CentralScheduleServiceExporter.", e);
      }
    }
  }

}
