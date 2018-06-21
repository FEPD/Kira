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

import com.yihaodian.architecture.kira.common.ChangedSetHolder;
import com.yihaodian.architecture.kira.server.util.KiraServerRoleEnum;
import java.util.LinkedHashSet;

public class KiraServerTrackContext extends KiraServerEntity {

  private static final long serialVersionUID = 1L;

  //old datas
  private int oldComponentState;
  private KiraServerRoleEnum oldKiraServerRole;
  private LinkedHashSet<KiraServerEntity> oldAllOtherKiraServers;

  //new datas
  private int newComponentState;
  private KiraServerRoleEnum newKiraServerRole; //Will always be null when kiraServer is shutdown.
  private LinkedHashSet<KiraServerEntity> newAllOtherKiraServers; //Will always be empty when kiraServer is shutdown, So this field is invalid when kiraServer is shutdown.

  //Calculated Difference.
  private volatile boolean componentStateChanged;
  private volatile boolean kiraServerRoleChanged;
  private ChangedSetHolder<KiraServerEntity> allOtherKiraServersChangedSetHolder;
  private volatile boolean changed;

  /**
   * @param oldAllOtherKiraServers ensure the cloned oldAllOtherKiraServers
   */
  public KiraServerTrackContext(String serverId, String host, Integer port,
      String accessUrlAsString, int oldComponentState, KiraServerRoleEnum oldKiraServerRole,
      LinkedHashSet<KiraServerEntity> oldAllOtherKiraServers) {
    super(serverId, host, port, accessUrlAsString);
    this.oldComponentState = oldComponentState;
    this.oldKiraServerRole = oldKiraServerRole;
    if (null == oldAllOtherKiraServers) {
      this.oldAllOtherKiraServers = new LinkedHashSet<KiraServerEntity>();
    } else {
      this.oldAllOtherKiraServers = oldAllOtherKiraServers;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public int getOldComponentState() {
    return oldComponentState;
  }

  public void setOldComponentState(int oldComponentState) {
    this.oldComponentState = oldComponentState;
  }

  public KiraServerRoleEnum getOldKiraServerRole() {
    return oldKiraServerRole;
  }

  public void setOldKiraServerRole(KiraServerRoleEnum oldKiraServerRole) {
    this.oldKiraServerRole = oldKiraServerRole;
  }

  public LinkedHashSet<KiraServerEntity> getOldAllOtherKiraServers() {
    return oldAllOtherKiraServers;
  }

  public void setOldAllOtherKiraServers(
      LinkedHashSet<KiraServerEntity> oldAllOtherKiraServers) {
    this.oldAllOtherKiraServers = oldAllOtherKiraServers;
  }

  public int getNewComponentState() {
    return newComponentState;
  }

  public void setNewComponentState(int newComponentState) {
    this.newComponentState = newComponentState;
  }

  public KiraServerRoleEnum getNewKiraServerRole() {
    return newKiraServerRole;
  }

  public void setNewKiraServerRole(KiraServerRoleEnum newKiraServerRole) {
    this.newKiraServerRole = newKiraServerRole;
  }

  public LinkedHashSet<KiraServerEntity> getNewAllOtherKiraServers() {
    return newAllOtherKiraServers;
  }

  public void setNewAllOtherKiraServers(
      LinkedHashSet<KiraServerEntity> newAllOtherKiraServers) {
    this.newAllOtherKiraServers = newAllOtherKiraServers;
  }

  public boolean isComponentStateChanged() {
    return componentStateChanged;
  }

  public void setComponentStateChanged(boolean componentStateChanged) {
    this.componentStateChanged = componentStateChanged;
  }

  public boolean isKiraServerRoleChanged() {
    return kiraServerRoleChanged;
  }

  public void setKiraServerRoleChanged(boolean kiraServerRoleChanged) {
    this.kiraServerRoleChanged = kiraServerRoleChanged;
  }

  public ChangedSetHolder<KiraServerEntity> getAllOtherKiraServersChangedSetHolder() {
    return allOtherKiraServersChangedSetHolder;
  }

  public void setAllOtherKiraServersChangedSetHolder(
      ChangedSetHolder<KiraServerEntity> allOtherKiraServersChangedSetHolder) {
    this.allOtherKiraServersChangedSetHolder = allOtherKiraServersChangedSetHolder;
  }

  public boolean isAllOtherKiraServersChanged() {
    return allOtherKiraServersChangedSetHolder.isTheSetChanged();
  }

  /**
   * @param newAllOtherKiraServers ensure the cloned newAllOtherKiraServers
   */
  public void setNewDataAndCalculateDifference(int newComponentState,
      KiraServerRoleEnum newKiraServerRole,
      LinkedHashSet<KiraServerEntity> newAllOtherKiraServers) {
    this.newComponentState = newComponentState;
    this.newKiraServerRole = newKiraServerRole;
    if (null == newAllOtherKiraServers) {
      this.newAllOtherKiraServers = new LinkedHashSet<KiraServerEntity>();
    } else {
      this.newAllOtherKiraServers = newAllOtherKiraServers;
    }

    //calculateDifference
    calculateDifference();
  }

  private void calculateDifference() {
    this.componentStateChanged = (this.oldComponentState != this.newComponentState);
    this.kiraServerRoleChanged = (this.oldKiraServerRole != this.newKiraServerRole);
    allOtherKiraServersChangedSetHolder = new ChangedSetHolder<KiraServerEntity>(
        this.oldAllOtherKiraServers, this.newAllOtherKiraServers);
    this.changed = (this.componentStateChanged || this.kiraServerRoleChanged || this
        .isAllOtherKiraServersChanged());
  }

  public boolean isChanged() {
    return this.changed;
  }

  @Override
  public String toString() {
    return "KiraServerTrackContext [oldComponentState="
        + oldComponentState + ", oldKiraServerRole="
        + oldKiraServerRole + ", oldAllOtherKiraServers="
        + oldAllOtherKiraServers + ", newComponentState="
        + newComponentState + ", newKiraServerRole="
        + newKiraServerRole + ", newAllOtherKiraServers="
        + newAllOtherKiraServers + ", componentStateChanged="
        + componentStateChanged + ", kiraServerRoleChanged="
        + kiraServerRoleChanged + ", allOtherKiraServersChangedSetHolder="
        + allOtherKiraServersChangedSetHolder + ", changed=" + changed
        + ", serverId=" + serverId + ", host=" + host + ", port="
        + port + ", accessUrlAsString=" + accessUrlAsString + "]";
  }

}
