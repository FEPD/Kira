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

import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggersZkChildListener implements IZkChildListener {

  private static Logger logger = LoggerFactory.getLogger(TriggersZkChildListener.class);

  private IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager;

  public TriggersZkChildListener(
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
  public void handleChildChange(String parentPath, List<String> currentChilds)
      throws Exception {
    logger.info("The child Change event of triggers detected...");
    this.kiraTimerTriggerMetadataManager
        .handleChildChangeForTriggersZNode(parentPath, currentChilds);
  }

}
