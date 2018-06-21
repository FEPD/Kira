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
package com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.listener;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.zkclient.IZkDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerZkDataListener implements IZkDataListener {

  private static Logger logger = LoggerFactory.getLogger(TriggerZkDataListener.class);

  private IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager;

  public TriggerZkDataListener(
      IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager) {
    super();
    this.kiraTimerTriggerMetadataManager = kiraTimerTriggerMetadataManager;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleDataChange(String dataPath, Object data) throws Exception {
    try {
      logger.info("The data Change event of trigger detected...dataPath={}", dataPath);
      if (null != data) {
        TriggerMetadataZNodeData newTriggerMetadataZNodeData = (TriggerMetadataZNodeData) data;
        this.kiraTimerTriggerMetadataManager
            .handleTriggerZKDataChange(dataPath, newTriggerMetadataZNodeData);
      } else {
        logger.warn("data is null for trigger dataPath={}", dataPath);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when handleDataChange for trigger. dataPath=" + dataPath + " and data="
              + KiraCommonUtils.toString(data), e);
      throw e;
    }
  }

  @Override
  public void handleDataDeleted(String dataPath) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("The data deleted event of trigger detected. But do nothing. dataPath={}",
          dataPath);
    }
  }

}
