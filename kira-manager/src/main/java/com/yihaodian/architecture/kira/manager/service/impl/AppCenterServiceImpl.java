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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.AppCenterZNodeData;
import com.yihaodian.architecture.kira.manager.criteria.AppCenterCriteria;
import com.yihaodian.architecture.kira.manager.dto.AppCenterDetailData;
import com.yihaodian.architecture.kira.manager.service.AppCenterService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class AppCenterServiceImpl extends Service implements AppCenterService {

  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public AppCenterServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<AppCenterDetailData> getAppCenterDetailDataList(
      AppCenterCriteria appCenterCriteria) {
    List<AppCenterDetailData> returnValue = new ArrayList<AppCenterDetailData>();
    try {
      if (zkClient.exists(KiraCommonConstants.ZK_PATH_APPCENTERS)) {
        List<String> appCentersChildZNodeNameList = zkClient
            .getChildren(KiraCommonConstants.ZK_PATH_APPCENTERS);
        if (!CollectionUtils.isEmpty(appCentersChildZNodeNameList)) {
          String appCenterZKFullPath = null;
          AppCenterZNodeData appCenterZNodeData = null;
          AppCenterDetailData appCenterDetailData = null;
          for (String appCentersChildZNodeName : appCentersChildZNodeNameList) {
            appCenterZKFullPath = KiraUtil
                .getChildFullPath(KiraCommonConstants.ZK_PATH_APPCENTERS, appCentersChildZNodeName);
            appCenterZNodeData = zkClient.readData(appCenterZKFullPath, true);
            if (null != appCenterZNodeData) {
              String appId = appCenterZNodeData.getAppId();
              String host = appCenterZNodeData.getHost();
              Integer port = appCenterZNodeData.getPort();
              String serviceUrl = appCenterZNodeData.getServiceUrl();
              Date createTime = appCenterZNodeData.getCreateTime();

              appCenterDetailData = new AppCenterDetailData(appId, host, port, serviceUrl,
                  createTime);
              returnValue.add(appCenterDetailData);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when getAppCenterDetailDataList. appCenterCriteria=" + KiraCommonUtils
              .toString(appCenterCriteria), e);
    }
    return returnValue;
  }

}
