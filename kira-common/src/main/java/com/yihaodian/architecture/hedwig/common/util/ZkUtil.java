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
package com.yihaodian.architecture.hedwig.common.util;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.BaseProfile;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Archer Jiang
 */
public class ZkUtil {

  static ZkClient _zkClient = KiraZkUtil.initDefaultZk();
  static Lock lock = new ReentrantLock();


  public static ZkClient getZkClientInstance() throws HedwigException {

    if (_zkClient == null) {
      lock.lock();
      try {
        if (_zkClient == null) {
          _zkClient = KiraZkUtil.initDefaultZk();
				/*	String serverList = ProperitesContainer.provider().getProperty(PropKeyConstants.ZK_SERVER_LIST);
					if (!HedwigUtil.isBlankString(serverList)) {
						_zkClient = new ZkClient(serverList, InternalConstants.ZK_SESSION_TIMEOUT, Integer.MAX_VALUE);
					} else {
						throw new HedwigException("ZK client initial error, serverList:" + serverList);
					}*/

        }
      } finally {
        lock.unlock();
      }
    }
    return _zkClient;
  }


  public static String createChildPath(ServiceProfile profile) throws InvalidParamException {
    if (profile == null) {
      throw new InvalidParamException(" Service profile must not null!!!");
    }
    StringBuilder path = new StringBuilder(profile.getParentPath()).append("/")
        .append(getProcessDesc(profile));
    return path.toString();
  }

  public static String getProcessDesc(ServiceProfile profile) throws InvalidParamException {
    StringBuilder path = new StringBuilder().append(profile.getHostIp()).append(":")
        .append(profile.getPort());
    return path.toString();
  }

  public static String createAppPath(BaseProfile profile) throws InvalidParamException {
    if (profile == null) {
      throw new InvalidParamException(" Service profile must not null!!!");
    }
    StringBuilder appPath = new StringBuilder(profile.getRootPath());
    appPath.append("/").append(profile.getDomainName()).append("/")
        .append(profile.getServiceAppName());
    return appPath.toString();
  }

  public static String createParentPath(BaseProfile profile) throws InvalidParamException {
    if (profile == null) {
      throw new InvalidParamException(" Service profile must not null!!!");
    }
    StringBuilder path = new StringBuilder(createAppPath(profile));
    path.append("/").append(profile.getServiceName()).append("/")
        .append(profile.getServiceVersion());
    return path.toString();
  }

  public static void createAppcodeDict(BaseProfile profile, String appcode)
      throws InvalidParamException {
    if (profile == null) {
      throw new InvalidParamException(" Service profile must not null!!!");
    }
    if (HedwigUtil.isBlankString(appcode)) {
      appcode = "defaultAppName";
    }
    String filterCode = appcode.replace("/", "#");
    StringBuilder pathBuilder = new StringBuilder(InternalConstants.HEDWIG_PAHT_APPDICT);
    String appPath = createAppPath(profile);
    String fAppPath = appPath.replace("/", "#");
    pathBuilder.append("/").append(fAppPath).append(":").append(filterCode);
    String path = pathBuilder.toString();
    if (!_zkClient.exists(path)) {
      _zkClient.createPersistent(path, true);
    }
  }

  public static String generatePath(BaseProfile profile, String subPath)
      throws InvalidParamException {
    String value = "";
    if (profile == null && subPath != null) {
      throw new InvalidParamException(" Service profile must not null!!!");
    }
    StringBuilder path = new StringBuilder(
        profile.getRootPath() == null ? "" : profile.getRootPath());
    path.append("/").append(profile.getDomainName()).append("/").append(profile.getServiceAppName())
        .append("/").append(subPath);
    value = path.toString();
    if (!value.endsWith("/") && !_zkClient.exists(value)) {
      _zkClient.createPersistent(value, true);
    }
    return value;
  }

  public static String createRollPath(BaseProfile profile) throws InvalidParamException {
    return ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_ROLL);
  }

  public static String createRefugeePath(BaseProfile profile) throws InvalidParamException {
    return ZkUtil.generatePath(profile,
        InternalConstants.HEDWIG_PAHT_CAMPS + "/" + InternalConstants.HEDWIG_PAHT_REFUGEE);
  }

  public static String createCampPath(BaseProfile profile, String campName)
      throws InvalidParamException {
    return ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS + "/" + campName);
  }

  public static String createBaseCampPath(BaseProfile profile) {
    String value = "";
    try {
      value = ZkUtil.generatePath(profile, InternalConstants.HEDWIG_PAHT_CAMPS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return value;
  }

  public static String assembleProfilePath(String domain, String appName, String servName,
      String servVersion, String providerHost)
      throws Exception {

    StringBuilder sb = new StringBuilder(InternalConstants.BASE_ROOT);
    if (!HedwigUtil.isBlankString(domain)) {
      sb.append("/").append(domain);
    } else {
      throw new Exception("Domain must not null!!!");
    }
    if (!HedwigUtil.isBlankString(appName)) {
      sb.append("/").append(appName);
    } else {
      throw new Exception("appName must not null!!!");
    }
    if (!HedwigUtil.isBlankString(servName)) {
      sb.append("/").append(servName);
    } else {
      throw new Exception("servName must not null!!!");
    }
    if (!HedwigUtil.isBlankString(servVersion)) {
      sb.append("/").append(servVersion);
    } else {
      throw new Exception("servVersion must not null!!!");
    }
    if (!HedwigUtil.isBlankString(providerHost)) {
      sb.append("/").append(providerHost);
    } else {
      throw new Exception("providerHost must not null!!!");
    }
    return sb.toString();
  }
}
