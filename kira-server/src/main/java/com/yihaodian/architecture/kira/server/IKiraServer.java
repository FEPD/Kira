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
package com.yihaodian.architecture.kira.server;

import com.yihaodian.architecture.kira.common.IComponent;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.dto.KiraServerInfo;
import java.util.LinkedHashSet;

public interface IKiraServer extends IComponent {

  public void registerKiraServerEventHandler(EventHandler eventHandler) throws Exception;

  public String getServerId();

  public KiraServerEntity getKiraServerEntity();

  public KiraServerInfo getKiraServerInfo();

  public boolean isLeaderServer(boolean accurate);

  /**
   * @return those which only in the cluster of kiraServer
   */
  public LinkedHashSet<KiraServerEntity> getAllKiraServerEntitysInCluster() throws Exception;

  public KiraServerEntity getKiraServerEntityOfLeader(boolean needCheckKiraServerLeaderZNodeData)
      throws Exception;

  public KiraServerEntity getKiraServerEntityByServerId(String serverId) throws Exception;
}
