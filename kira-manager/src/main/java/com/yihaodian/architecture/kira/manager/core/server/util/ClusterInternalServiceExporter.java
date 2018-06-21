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
package com.yihaodian.architecture.kira.manager.core.server.util;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.provider.AppProfile;
import com.yihaodian.architecture.hedwig.provider.BasicAuthWebserviceExporter;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterInternalServiceExporter extends
    BasicAuthWebserviceExporter {

  private Logger logger = LoggerFactory.getLogger(ClusterInternalServiceExporter.class);

  public ClusterInternalServiceExporter() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setAppProfile(AppProfile appProfile) {
    super.setUserName(KiraServerConstants.CLUSTER_INTERNAL_SERVICE_AUTH_USERNAME);
    super.setPassword(KiraServerConstants.CLUSTER_INTERNAL_SERVICE_AUTH_PASSWORD);
    super.setServiceInterface(IClusterInternalService.class);
    super.setServiceVersion(KiraServerConstants.SERVICEVERSION_CLUSTERINTERNALSERVICE);
    super.setAppProfile(appProfile);
    KiraServerDataCenter.setAppProfileForClusterInternalServiceExporter(appProfile);
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    ServiceProfile serviceProfile = KiraCommonUtils.getServiceProfile(this);
    if (null != serviceProfile) {
      String hostIp = serviceProfile.getHostIp();
      KiraServerDataCenter.setHostIpForClusterInternalService(hostIp);
      int port = serviceProfile.getPort();
      KiraServerDataCenter.setPortForClusterInternalService(Integer.valueOf(port));
      String serviceUrl = serviceProfile.getServiceUrl();
      KiraServerDataCenter.setClusterInternalServiceUrl(serviceUrl);
      String parentPathForClusterInternalServiceOnZK = serviceProfile.getParentPath();
      KiraServerDataCenter
          .setParentPathForClusterInternalServiceOnZK(parentPathForClusterInternalServiceOnZK);
    } else {
      throw new RuntimeException(
          "Failed to get serviceProfile for ClusterInternalServiceExporter.");
    }
  }

  @Override
  public void destroy() throws Exception {
    try {
      super.destroy();
    } catch (Exception e) {
      if (null != logger) {
        logger.error("Error occurs for super.destroy(); for ClusterInternalServiceExporter.", e);
      }
    }
  }

}
