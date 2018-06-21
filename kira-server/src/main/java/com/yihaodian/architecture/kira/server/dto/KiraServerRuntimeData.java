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
package com.yihaodian.architecture.kira.server.dto;

import com.yihaodian.architecture.kira.server.util.KiraServerRoleEnum;
import java.util.LinkedHashSet;

public class KiraServerRuntimeData extends KiraServerEntity {

  private static final long serialVersionUID = 1L;

  private int componentState;
  private KiraServerRoleEnum kiraServerRole;
  private LinkedHashSet<KiraServerEntity> allOtherKiraServers;

  public KiraServerRuntimeData(String serverId, String host, Integer port,
      String accessUrlAsString, int componentState,
      KiraServerRoleEnum kiraServerRole,
      LinkedHashSet<KiraServerEntity> allOtherKiraServers) {
    super(serverId, host, port, accessUrlAsString);
    this.componentState = componentState;
    this.kiraServerRole = kiraServerRole;
    this.allOtherKiraServers = allOtherKiraServers;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  public int getComponentState() {
    return componentState;
  }

  public KiraServerRoleEnum getKiraServerRole() {
    return kiraServerRole;
  }

  public LinkedHashSet<KiraServerEntity> getAllOtherKiraServers() {
    return allOtherKiraServers;
  }

  @Override
  public String toString() {
    return "KiraServerRuntimeData [componentState=" + componentState
        + ", kiraServerRole=" + kiraServerRole
        + ", allOtherKiraServers=" + allOtherKiraServers
        + ", serverId=" + serverId + ", host=" + host + ", port="
        + port + ", accessUrlAsString=" + accessUrlAsString + "]";
  }

}
