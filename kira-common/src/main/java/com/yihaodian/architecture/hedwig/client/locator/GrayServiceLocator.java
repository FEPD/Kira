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

import com.yihaodian.architecture.hedwig.client.util.HedwigClientUtil;
import com.yihaodian.architecture.hedwig.common.dto.ClientProfile;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.zkclient.IZkDataListener;

/**
 * @author archer
 */
public class GrayServiceLocator extends GroupServiceLocator {

  private static String grayPath = "";

  public GrayServiceLocator(ClientProfile clientProfile) throws HedwigException {
    super(clientProfile);
    String poolName = HedwigClientUtil.getClientPoolName();
    poolName = HedwigUtil.isBlankString(poolName) ? clientProfile.getClientAppName() : poolName;
    grayPath = HedwigUtil.genPoolGrayPath(poolName);
    observeGray();
  }

  private void observeGray() {
    if (!_zkClient.exists(grayPath)) {
      _zkClient.createPersistent(grayPath, true);
    }
    _zkClient.subscribeDataChanges(grayPath, new IZkDataListener() {

      @Override
      public void handleDataDeleted(String dataPath) throws Exception {
        // TODO Auto-generated method stub

      }

      @Override
      public void handleDataChange(String dataPath, Object data) throws Exception {
        // TODO Auto-generated method stub

      }
    });
  }

}
