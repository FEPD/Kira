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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.server.util.KiraServerRoleEnum;
import java.io.Serializable;
import java.util.Date;

public class KiraServerInfo extends KiraServerEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  protected int componentState;
  protected KiraServerRoleEnum kiraServerRole;
  protected Date lastChangeKiraServerRoleTime;
  protected Date lastStartedTime;
  protected Date lastShutdownServerTime;
  protected String kiraServerZNodePath;
  protected Date lastUpdateKiraServerZNodePathTime;
  protected Date lastUpdateKiraServerZNodeDataTime;
  protected Date lastUpdateAllOtherKiraServersTime;

  public KiraServerInfo() {

  }

  public KiraServerInfo(String serverId, String host, Integer port,
      String accessUrlAsString, int componentState,
      KiraServerRoleEnum kiraServerRole,
      Date lastChangeKiraServerRoleTime, Date lastStartedTime,
      Date lastShutdownServerTime, String kiraServerZNodePath,
      Date lastUpdateKiraServerZNodePathTime,
      Date lastUpdateKiraServerZNodeDataTime,
      Date lastUpdateAllOtherKiraServersTime) {
    super(serverId, host, port, accessUrlAsString);
    this.componentState = componentState;
    this.kiraServerRole = kiraServerRole;
    this.lastChangeKiraServerRoleTime = lastChangeKiraServerRoleTime;
    this.lastStartedTime = lastStartedTime;
    this.lastShutdownServerTime = lastShutdownServerTime;
    this.kiraServerZNodePath = kiraServerZNodePath;
    this.lastUpdateKiraServerZNodePathTime = lastUpdateKiraServerZNodePathTime;
    this.lastUpdateKiraServerZNodeDataTime = lastUpdateKiraServerZNodeDataTime;
    this.lastUpdateAllOtherKiraServersTime = lastUpdateAllOtherKiraServersTime;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public int getComponentState() {
    return componentState;
  }

  public void setComponentState(int componentState) {
    this.componentState = componentState;
  }

  public KiraServerRoleEnum getKiraServerRole() {
    return kiraServerRole;
  }

  public void setKiraServerRole(KiraServerRoleEnum kiraServerRole) {
    this.kiraServerRole = kiraServerRole;
  }

  public Date getLastChangeKiraServerRoleTime() {
    return lastChangeKiraServerRoleTime;
  }

  public void setLastChangeKiraServerRoleTime(Date lastChangeKiraServerRoleTime) {
    this.lastChangeKiraServerRoleTime = lastChangeKiraServerRoleTime;
  }

  public String getLastChangeKiraServerRoleTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastChangeKiraServerRoleTime);
  }

  public Date getLastStartedTime() {
    return lastStartedTime;
  }

  public void setLastStartedTime(Date lastStartedTime) {
    this.lastStartedTime = lastStartedTime;
  }

  public String getLastStartedTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastStartedTime);
  }

  public Date getLastShutdownServerTime() {
    return lastShutdownServerTime;
  }

  public void setLastShutdownServerTime(Date lastShutdownServerTime) {
    this.lastShutdownServerTime = lastShutdownServerTime;
  }

  public String getLastShutdownServerTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastShutdownServerTime);
  }

  public String getKiraServerZNodePath() {
    return kiraServerZNodePath;
  }

  public void setKiraServerZNodePath(String kiraServerZNodePath) {
    this.kiraServerZNodePath = kiraServerZNodePath;
  }

  public Date getLastUpdateKiraServerZNodePathTime() {
    return lastUpdateKiraServerZNodePathTime;
  }

  public void setLastUpdateKiraServerZNodePathTime(
      Date lastUpdateKiraServerZNodePathTime) {
    this.lastUpdateKiraServerZNodePathTime = lastUpdateKiraServerZNodePathTime;
  }

  public String getLastUpdateKiraServerZNodePathTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodePathTime);
  }

  public Date getLastUpdateKiraServerZNodeDataTime() {
    return lastUpdateKiraServerZNodeDataTime;
  }

  public void setLastUpdateKiraServerZNodeDataTime(
      Date lastUpdateKiraServerZNodeDataTime) {
    this.lastUpdateKiraServerZNodeDataTime = lastUpdateKiraServerZNodeDataTime;
  }

  public String getLastUpdateKiraServerZNodeDataTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodeDataTime);
  }

  public Date getLastUpdateAllOtherKiraServersTime() {
    return lastUpdateAllOtherKiraServersTime;
  }

  public void setLastUpdateAllOtherKiraServersTime(
      Date lastUpdateAllOtherKiraServersTime) {
    this.lastUpdateAllOtherKiraServersTime = lastUpdateAllOtherKiraServersTime;
  }

  public String getLastUpdateAllOtherKiraServersTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastUpdateAllOtherKiraServersTime);
  }

  @Override
  public String toString() {
    return "KiraServerInfo [componentState=" + componentState
        + ", kiraServerRole=" + kiraServerRole
        + ", lastChangeKiraServerRoleTime="
        + KiraCommonUtils.getDateAsString(lastChangeKiraServerRoleTime) + ", lastStartedTime="
        + KiraCommonUtils.getDateAsString(lastStartedTime) + ", lastShutdownServerTime="
        + KiraCommonUtils.getDateAsString(lastShutdownServerTime) + ", kiraServerZNodePath="
        + kiraServerZNodePath + ", lastUpdateKiraServerZNodePathTime="
        + KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodePathTime)
        + ", lastUpdateKiraServerZNodeDataTime="
        + KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodeDataTime)
        + ", lastUpdateAllOtherKiraServersTime="
        + KiraCommonUtils.getDateAsString(lastUpdateAllOtherKiraServersTime) + ", serverId="
        + serverId
        + ", host=" + host + ", port=" + port + ", accessUrlAsString="
        + accessUrlAsString + "]";
  }

}
