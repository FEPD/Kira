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
package com.yihaodian.architecture.hedwig.client.locator;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This locator is implemented base on zookeeper ephemeral node. It will synchronize with zk server
 * to make sure the profileContainer is up-to-date.
 *
 * @author Archer Jiang
 */
public class GroupServiceLocator extends ZkServiceLocator {

  private static Logger logger = LoggerFactory.getLogger(GroupServiceLocator.class);
  private Map<String, List<String>> campMap = new HashMap<String, List<String>>();
  private Set<String> processSet = new HashSet<String>();

  public GroupServiceLocator(ClientProfile clientProfile) throws HedwigException {
    super(clientProfile);
    observeCamps();
    loadAvailableProcess();
  }

  /**
   *
   */
  private void loadAvailableProcess() {
    Set<String> set = null;
    String baseCamp = ZkUtil.createBaseCampPath(clientProfile);
    Set<String> campSet = clientProfile.getGroupNames();
    List<String> camps = _zkClient.getChildren(baseCamp);
    campMap = new HashMap<String, List<String>>();
    if (camps != null && camps.size() > 1) {
      if (campSet == null || campSet.size() <= 0) {
        updateCampMap(InternalConstants.HEDWIG_PAHT_REFUGEE);
      } else {
        for (String campName : campSet) {
          updateCampMap(campName);
        }
      }
      processSet = totalProcess();
    } else {
      processSet = null;
    }
    balancer.setWhiteList(processSet);
    balancer.updateProfiles(profileContainer.values());
  }

  private Set<String> totalProcess() {
    Set<String> set = new HashSet<String>();
    for (List<String> pl : campMap.values()) {
      set.addAll(pl);
    }
    return set;
  }

  public void updateCampMap(String campName) {
    try {
      if (!InternalConstants.HEDWIG_PAHT_REFUGEE.equals(campName)) {
        fillRefugeeGroup();
      }
      String campPath = ZkUtil.createCampPath(clientProfile, campName);
      List<String> list = _zkClient.getChildren(campPath);
      if (list != null && list.size() > 0) {
        campMap.put(campPath, list);
      }
    } catch (Exception e) {
      logger.error(InternalConstants.HANDLE_LOG_PROFIX + e.getMessage(), e);
    }

  }

  private void fillRefugeeGroup() {
    try {
      String refugeePath = ZkUtil
          .createCampPath(clientProfile, InternalConstants.HEDWIG_PAHT_REFUGEE);
      List<String> list = _zkClient.getChildren(refugeePath);
      if (list == null || list.size() == 0) {
        for (String host : profileContainer.keySet()) {
          String fpath = refugeePath + "/" + host;
          if (!_zkClient.exists(fpath)) {
            _zkClient.createPersistent(fpath);
          }
        }
      }
    } catch (InvalidParamException e) {
      logger.error(InternalConstants.HANDLE_LOG_PROFIX + e.getMessage(), e);
    }

  }

  /**
   * Observe group change, include group increase/decrease and process increase/decrease.
   */
  private void observeCamps() {
    try {
      final String baseCamp = ZkUtil.createBaseCampPath(clientProfile);
      final Set<String> campSet = clientProfile.getGroupNames();
      String campPath = null;
      // observe group change add or delete
      _zkClient.subscribeChildChanges(baseCamp, new IZkChildListener() {
        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds)
            throws Exception {
          if (campSet != null && campSet.size() > 0) {
            for (String campName : campSet) {
              ZkUtil.createCampPath(clientProfile, campName);
            }
          }
          loadAvailableProcess();
        }
      });
      if (campSet != null && campSet.size() > 0) {
        for (String campName : campSet) {
          campPath = ZkUtil.createCampPath(clientProfile, campName);
          // observe available process change of interesting group
          _zkClient.subscribeChildChanges(campPath, new IZkChildListener() {

            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds)
                throws Exception {
              updateProcess(parentPath, _zkClient.getChildren(parentPath));

            }
          });
        }
      } else {
        campPath = ZkUtil.createRefugeePath(clientProfile);
        // observe refugee process
        _zkClient.subscribeChildChanges(campPath, new IZkChildListener() {

          @Override
          public void handleChildChange(String parentPath, List<String> currentChilds)
              throws Exception {
            List<String> list = _zkClient.getChildren(baseCamp);
            if (list != null && list.size() > 1) {
              updateProcess(parentPath, _zkClient.getChildren(parentPath));
            }
          }
        });
      }
    } catch (Exception e) {
      logger.error("Observe camps failed!!!");
    }
  }

  protected void updateProcess(String campPath, List<String> currentChilds) {
    campMap.put(campPath, currentChilds);
    processSet = totalProcess();
    balancer.setWhiteList(processSet);
    balancer.updateProfiles(profileContainer.values());
  }

}
