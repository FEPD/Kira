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

import com.yihaodian.architecture.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;

/**
 * Created by zhoufeiqiang on 05/09/2017.
 */
public class KiraZkUtil {

  private static ZkClient defaultZk;
  private static ZkClient defineZk;

  public static ZkClient initDefaultZk() {
    ZkClient _zkClient = null;
    if (defaultZk != null) {
      return defaultZk;
    } else {
      try {
        String zkServerIpList = LoadProertesContainer.provider()
            .getProperty("cluster1.serverList", null);
        if (StringUtils.isNotBlank(zkServerIpList)) {
          _zkClient = new ZkClient(zkServerIpList);
          defaultZk = _zkClient;
        } else {
          throw new RuntimeException(
              "zkServerIp should not be blank! please config zookeeper server ip !");
        }
      } catch (Exception e) {
        throw new RuntimeException("Init Default Zookeeper fail! ", e);
      }
    }
    return defaultZk;
  }


  public static ZkClient initDefineZk(String serverString) {
    ZkClient _zkClient = null;
    try {
      if (defineZk != null) {
        return defineZk;
      } else {
        _zkClient = new ZkClient(serverString);
        defineZk = _zkClient;
      }
    } catch (Exception e) {
      throw new RuntimeException("Init Define  Zookeeper fail! " + serverString, e);
    }
    return defineZk;
  }

}
