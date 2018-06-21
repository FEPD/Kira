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
package com.yihaodian.architecture.kira.common.util;

import akka.actor.Address;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Created by zhoufeiqiang on 04/09/2017.
 */
public class KiraUtil {

  public static String appId() {
    String appId = LoadGlobalPropertyConfigurer.getAppId();
    if (StringUtils.isBlank(appId)) {
      throw new RuntimeException("appId should not be blank!");
    }
    return appId;
  }

  public static String filterString(String value) {
    return value.replaceAll("/", "_");
  }

  public static String getChildFullPath(String parentPath, String shortChildPath) {
    return parentPath + "/" + shortChildPath;
  }

  public static List<String> kiraServer() {
    String path = KiraCommonConstants.ZK_PATH_APPCENTERS;
    ZkClient zkClient = KiraZkUtil.initDefaultZk();
    List<String> kiraServerList = null;
    if (zkClient.exists(path)) {
      kiraServerList = zkClient.getChildren(path);
    }
    return kiraServerList;
  }

  public static String getHosturl(Address address) {
    String v = null;
    if (address != null) {
      String tmp = address.hostPort();
      if (tmp != null) {
        String[] arr = tmp.split("@");
        if (arr != null && arr.length == 2) {
          v = arr[1];
        }
      }
    }
    return v;
  }

  public static void main(String args[]) {

  }
}
