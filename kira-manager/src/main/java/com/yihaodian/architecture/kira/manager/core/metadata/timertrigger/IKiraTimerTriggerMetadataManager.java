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
package com.yihaodian.architecture.kira.manager.core.metadata.timertrigger;

import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.kira.server.util.IKiraServerEventHandleComponent;
import java.util.List;

public interface IKiraTimerTriggerMetadataManager extends IKiraServerEventHandleComponent {

  void handleTriggerZKDataChange(String dataPath,
      TriggerMetadataZNodeData newTriggerMetadataZNodeData) throws Exception;

  void handleChildChangeForTriggersZNode(String parentPath, List<String> currentChilds)
      throws Exception;

  void handleChildChangeForPoolOfTrigger(String parentPath, List<String> currentChilds)
      throws Exception;

  void unsubscribeDataChangesForTrigger(TriggerIdentity triggerIdentity) throws Exception;

  void subscribeDataChangesForTrigger(TriggerIdentity triggerIdentity) throws Exception;

  void unsubscribeDataChangesForTriggersOfPool(String poolId) throws Exception;

  void unsubscribeChildChangesForPoolByPoolId(String poolId) throws Exception;

  void manuallyCreateOrUpdateTriggerZNode(TriggerMetadataZNodeData triggerMetadataZNodeData)
      throws Exception;

  boolean isHandlingKiraTimerTriggerMetadata() throws Exception;
}
