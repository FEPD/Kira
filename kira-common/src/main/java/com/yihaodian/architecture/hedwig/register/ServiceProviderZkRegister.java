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
package com.yihaodian.architecture.hedwig.register;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.Date;
import java.util.List;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class ServiceProviderZkRegister implements IServiceProviderRegister {

  private Logger logger = LoggerFactory.getLogger(ServiceProviderZkRegister.class);
  private ZkClient _zkClient = null;
  private String parentPath = "";
  private String childPath = "";
  private boolean isRegisted = false;

  public ServiceProviderZkRegister() throws HedwigException {
    _zkClient = KiraZkUtil.initDefaultZk();
  }

  @Override
  public void regist(final ServiceProfile profile) throws InvalidParamException {
    createPersistentZnodes(profile);
    createEphemeralZnodes(profile);
    _zkClient.subscribeStateChanges(new IZkStateListener() {

      @Override
      public void handleStateChanged(KeeperState state) throws Exception {
        logger.debug(
            InternalConstants.LOG_PROFIX + "zk connection state change to:" + state.toString());
      }

      @Override
      public void handleNewSession() throws Exception {
        logger.debug(InternalConstants.LOG_PROFIX + "Reconnect to zk!!!");
        createEphemeralZnodes(profile);
      }
    });
    String childPath = ZkUtil.createChildPath(profile);
    _zkClient.subscribeDataChanges(childPath, new IZkDataListener() {

      @Override
      public void handleDataChange(String dataPath, Object data) throws Exception {
        if (data != null) {
          ServiceProfile nsp = (ServiceProfile) data;
          profile.update(nsp);
        }
      }

      @Override
      public void handleDataDeleted(String dataPath) throws Exception {
        logger.debug(InternalConstants.LOG_PROFIX + dataPath + "has deleted!!!");
      }

    });
    _zkClient.subscribeChildChanges(parentPath, new IZkChildListener() {

      @Override
      public void handleChildChange(String parentPath, List<String> currentChilds)
          throws Exception {
        createEphemeralZnodes(profile);
      }
    });
    String baseCampPath = ZkUtil.createBaseCampPath(profile);
    _zkClient.subscribeChildChanges(baseCampPath, new IZkChildListener() {

      @Override
      public void handleChildChange(String parentPath, List<String> currentChilds)
          throws Exception {
        ZkUtil.createRefugeePath(profile);
      }
    });
    isRegisted = true;

  }

  private void createEphemeralZnodes(ServiceProfile profile) throws InvalidParamException {
    // create service node
    childPath = ZkUtil.createChildPath(profile);
    if (!_zkClient.exists(childPath)) {
      profile.setRegistTime(new Date());
      try {
        _zkClient.createEphemeral(childPath, profile);
      } catch (Exception e) {
        if (!(e instanceof NodeExistsException)) {
          throw new InvalidParamException(e.getCause());
        }
      }
    }
    // create ip node
    String ipNode = ZkUtil.createRollPath(profile) + "/" + ZkUtil.getProcessDesc(profile);
    if (!_zkClient.exists(ipNode)) {
      try {
        _zkClient.createEphemeral(ipNode);
      } catch (Exception e) {
        if (!(e instanceof NodeExistsException)) {
          throw new InvalidParamException(e.getCause());
        }
      }
    }
  }

  private void createPersistentZnodes(ServiceProfile profile) throws InvalidParamException {
    String rollPath = "";
    String refugeePath = "";
    // create base path
    parentPath = profile.getParentPath();
    if (!_zkClient.exists(parentPath)) {
      try {
        _zkClient.createPersistent(parentPath, true);
      } catch (Exception e) {
        if (!(e instanceof NodeExistsException)) {
          throw new InvalidParamException(e.getCause());
        }
      }
    }
    // create roll path
    rollPath = ZkUtil.createRollPath(profile);
    // create refugee path
    refugeePath = ZkUtil.createRefugeePath(profile);
    // create dictionary contextPath:appcode
    String appcode = KiraUtil.appId();
    ZkUtil.createAppcodeDict(profile, appcode);
  }

  @Override
  public void updateProfile(ServiceProfile newProfile) {
    if (isRegisted) {
      if (newProfile != null) {
        _zkClient.writeData(childPath, newProfile);
      }
    }
  }

  @Override
  public void unRegist(ServiceProfile profile) {
    String servicePath = profile.getServicePath();
    _zkClient.unsubscribeAll();
    if (_zkClient.exists(servicePath)) {
      _zkClient.delete(servicePath);
    }
    isRegisted = false;
  }

}
