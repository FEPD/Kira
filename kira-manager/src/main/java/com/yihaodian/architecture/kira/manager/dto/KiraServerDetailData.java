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
package com.yihaodian.architecture.kira.manager.dto;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.server.dto.KiraServerInfo;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

public class KiraServerDetailData extends KiraServerInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date serverBirthTime;

  private String currentKiraZoneId;

  private String kiraMasterZoneId;

  private KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole;

  private Date lastSetKiraCrossMultiZoneRoleTime;

  private int managedTimerTriggerCount;

  private List<TriggerIdentity> managedTriggerIdentityList;

  private LinkedHashSet<String> assignedServerIdBlackList = new LinkedHashSet<String>();

  private Date lastUpdateAssignedServerIdBlackListTime;

  public KiraServerDetailData() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getClusterInternalServiceUrl() {
    return super.accessUrlAsString;
  }

  public String getServerRoleName() {
    return (null != super.kiraServerRole) ? super.kiraServerRole.getName() : null;
  }

  public String getServerBirthTimeAsString() {
    return KiraCommonUtils.getDateAsString(serverBirthTime);
  }

  public Date getServerBirthTime() {
    return serverBirthTime;
  }

  public void setServerBirthTime(Date serverBirthTime) {
    this.serverBirthTime = serverBirthTime;
  }

  public String getCurrentKiraZoneId() {
    return currentKiraZoneId;
  }

  public void setCurrentKiraZoneId(String currentKiraZoneId) {
    this.currentKiraZoneId = currentKiraZoneId;
  }

  public String getKiraMasterZoneId() {
    return kiraMasterZoneId;
  }

  public void setKiraMasterZoneId(String kiraMasterZoneId) {
    this.kiraMasterZoneId = kiraMasterZoneId;
  }

  public KiraCrossMultiZoneRoleEnum getKiraCrossMultiZoneRole() {
    return kiraCrossMultiZoneRole;
  }

  public void setKiraCrossMultiZoneRole(
      KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole) {
    this.kiraCrossMultiZoneRole = kiraCrossMultiZoneRole;
  }

  public Date getLastSetKiraCrossMultiZoneRoleTime() {
    return lastSetKiraCrossMultiZoneRoleTime;
  }

  public void setLastSetKiraCrossMultiZoneRoleTime(
      Date lastSetKiraCrossMultiZoneRoleTime) {
    this.lastSetKiraCrossMultiZoneRoleTime = lastSetKiraCrossMultiZoneRoleTime;
  }

  public String getLastSetKiraCrossMultiZoneRoleTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastSetKiraCrossMultiZoneRoleTime);
  }

  public int getManagedTimerTriggerCount() {
    return managedTimerTriggerCount;
  }

  public void setManagedTimerTriggerCount(int managedTimerTriggerCount) {
    this.managedTimerTriggerCount = managedTimerTriggerCount;
  }

  public List<TriggerIdentity> getManagedTriggerIdentityList() {
    return managedTriggerIdentityList;
  }

  public void setManagedTriggerIdentityList(
      List<TriggerIdentity> managedTriggerIdentityList) {
    this.managedTriggerIdentityList = managedTriggerIdentityList;
  }

  public LinkedHashSet<String> getAssignedServerIdBlackList() {
    return assignedServerIdBlackList;
  }

  public void setAssignedServerIdBlackList(
      LinkedHashSet<String> assignedServerIdBlackList) {
    this.assignedServerIdBlackList = assignedServerIdBlackList;
  }

  public Date getLastUpdateAssignedServerIdBlackListTime() {
    return lastUpdateAssignedServerIdBlackListTime;
  }

  public void setLastUpdateAssignedServerIdBlackListTime(
      Date lastUpdateAssignedServerIdBlackListTime) {
    this.lastUpdateAssignedServerIdBlackListTime = lastUpdateAssignedServerIdBlackListTime;
  }

  public String getLastUpdateAssignedServerIdBlackListTimeAsString() {
    return KiraCommonUtils.getDateAsString(lastUpdateAssignedServerIdBlackListTime);
  }

  @Override
  public String toString() {
    return "KiraServerDetailData [serverBirthTime=" + serverBirthTime
        + ", currentKiraZoneId=" + currentKiraZoneId
        + ", kiraMasterZoneId=" + kiraMasterZoneId
        + ", kiraCrossMultiZoneRole=" + kiraCrossMultiZoneRole
        + ", lastSetKiraCrossMultiZoneRoleTime=" + KiraCommonUtils
        .getDateAsString(lastSetKiraCrossMultiZoneRoleTime)
        + ", managedTimerTriggerCount=" + managedTimerTriggerCount
        + ", managedTriggerIdentityList=" + managedTriggerIdentityList
        + ", assignedServerIdBlackList=" + assignedServerIdBlackList
        + ", lastUpdateAssignedServerIdBlackListTime=" + KiraCommonUtils
        .getDateAsString(lastUpdateAssignedServerIdBlackListTime)
        + ", componentState=" + componentState + ", kiraServerRole="
        + kiraServerRole + ", lastChangeKiraServerRoleTime="
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
