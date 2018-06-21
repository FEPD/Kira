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
import com.yihaodian.architecture.hedwig.common.hessian.HedwigHessianProxyFactory;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraServerUtils {

  private static final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private static Logger logger = LoggerFactory.getLogger(KiraServerUtils.class);

  public static IClusterInternalService getClusterInternalServiceOfLeader() throws Exception {
    IClusterInternalService returnValue = null;

    KiraServerEntity kiraServerEntityOfLeader = KiraManagerDataCenter.getKiraServer()
        .getKiraServerEntityOfLeader(false);
    if (null != kiraServerEntityOfLeader) {
      String accessUrlAsStringForLeader = kiraServerEntityOfLeader.getAccessUrlAsString();
      returnValue = KiraServerUtils.getClusterInternalService(accessUrlAsStringForLeader);
      if (null == returnValue) {
        throw new KiraHandleException(
            "Can not get clusterInternalService by accessUrlAsStringForLeader="
                + accessUrlAsStringForLeader);
      }
    } else {
      throw new KiraHandleException("Can not get the information for leader server.");
    }

    return returnValue;
  }

  /**
   * @return those which in the cluster and out of cluster
   */
  public static LinkedHashSet<IClusterInternalService> getAllClusterInternalServices()
      throws Exception {
    LinkedHashSet<IClusterInternalService> returnValue = new LinkedHashSet<IClusterInternalService>();

    LinkedHashSet<String> allClusterInternalServiceUrls = KiraServerUtils
        .getAllClusterInternalServiceUrls();
    IClusterInternalService clusterInternalService = null;
    for (String clusterInternalServiceUrl : allClusterInternalServiceUrls) {
      clusterInternalService = KiraServerUtils.getClusterInternalService(clusterInternalServiceUrl);
      if (null != clusterInternalService) {
        returnValue.add(clusterInternalService);
      }
    }

    return returnValue;
  }

  /**
   * @return those which in the cluster and out of cluster
   */
  public static LinkedHashSet<String> getAllClusterInternalServiceUrls() throws Exception {
    LinkedHashSet<String> returnValue = new LinkedHashSet<String>();

    String parentPathForClusterInternalServiceOnZK = KiraServerDataCenter
        .getParentPathForClusterInternalServiceOnZK();
    if (zkClient.exists(parentPathForClusterInternalServiceOnZK)) {
      List<String> zkNodeNameList = zkClient.getChildren(parentPathForClusterInternalServiceOnZK);
      String zkNodeFullPath = null;
      ServiceProfile serviceProfile = null;
      String serviceUrl = null;
      for (String zkNodeName : zkNodeNameList) {
        zkNodeFullPath = KiraUtil
            .getChildFullPath(parentPathForClusterInternalServiceOnZK, zkNodeName);
        serviceProfile = zkClient.readData(zkNodeFullPath, true);
        serviceUrl = serviceProfile.getServiceUrl();
        returnValue.add(serviceUrl);
      }
    }
    return returnValue;
  }

  public static IClusterInternalService getClusterInternalService(String serviceUrl)
      throws Exception {
    if (StringUtils.isBlank(serviceUrl)) {
      throw new KiraHandleException("serviceUrl should not be null for getClusterInternalService.");
    }
    IClusterInternalService returnValue = null;
    try {
      HedwigHessianProxyFactory proxyFactory = new HedwigHessianProxyFactory();
      proxyFactory.setHessian2Request(true);
      proxyFactory.setHessian2Reply(true);
      proxyFactory.setChunkedPost(false);
      proxyFactory.setOverloadEnabled(false);
      proxyFactory.setReadTimeout(
          KiraServerConstants.CLUSTER_INTERNAL_SERVICE_HEDWIG_HESSIAN_READTIMEOUT_MILLISECOND);
      proxyFactory.setUser(KiraServerConstants.CLUSTER_INTERNAL_SERVICE_AUTH_USERNAME);
      proxyFactory.setPassword(KiraServerConstants.CLUSTER_INTERNAL_SERVICE_AUTH_PASSWORD);
      returnValue = (IClusterInternalService) proxyFactory
          .create(IClusterInternalService.class, serviceUrl);
    } catch (Exception e) {
      logger.error("Error occurs when getClusterInternalService for serviceUrl=" + serviceUrl, e);
      throw e;
    }

    return returnValue;
  }
}
