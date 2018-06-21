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
package com.yihaodian.architecture.kira.common.crossmultizone.dto;

import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

public class KiraCrossMultiZoneDataTrackContext implements Serializable {

  private static final long serialVersionUID = 1L;

  private String oldKiraMasterZoneId;

  private String newKiraMasterZoneId;

  private volatile boolean masterZoneIdChanged;

  private KiraCrossMultiZoneRoleEnum oldKiraCrossMultiZoneRole;

  private KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole;

  private volatile boolean kiraCrossMultiZoneRoleChanged;

  private volatile boolean changed;

  public KiraCrossMultiZoneDataTrackContext() {
  }

  public KiraCrossMultiZoneDataTrackContext(String oldKiraMasterZoneId,
      KiraCrossMultiZoneRoleEnum oldKiraCrossMultiZoneRole) {
    super();
    this.oldKiraMasterZoneId = oldKiraMasterZoneId;
    this.oldKiraCrossMultiZoneRole = oldKiraCrossMultiZoneRole;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getOldKiraMasterZoneId() {
    return oldKiraMasterZoneId;
  }

  public String getNewKiraMasterZoneId() {
    return newKiraMasterZoneId;
  }

  public boolean isMasterZoneIdChanged() {
    return masterZoneIdChanged;
  }

  public KiraCrossMultiZoneRoleEnum getOldKiraCrossMultiZoneRole() {
    return oldKiraCrossMultiZoneRole;
  }

  public KiraCrossMultiZoneRoleEnum getNewKiraCrossMultiZoneRole() {
    return newKiraCrossMultiZoneRole;
  }

  public boolean isKiraCrossMultiZoneRoleChanged() {
    return kiraCrossMultiZoneRoleChanged;
  }

  public boolean isChanged() {
    return changed;
  }

  public void setNewDataAndCalculateDifference(String newKiraMasterZoneId,
      KiraCrossMultiZoneRoleEnum newKiraCrossMultiZoneRole) {
    this.newKiraMasterZoneId = newKiraMasterZoneId;
    this.newKiraCrossMultiZoneRole = newKiraCrossMultiZoneRole;

    //calculateDifference
    calculateDifference();
  }

  private void calculateDifference() {
    this.masterZoneIdChanged = !StringUtils
        .equals(this.oldKiraMasterZoneId, this.newKiraMasterZoneId);
    this.kiraCrossMultiZoneRoleChanged = (this.oldKiraCrossMultiZoneRole
        != this.newKiraCrossMultiZoneRole);

    this.changed = (this.masterZoneIdChanged || this.kiraCrossMultiZoneRoleChanged);
  }

  @Override
  public String toString() {
    return "KiraCrossMultiZoneDataTrackContext [oldKiraMasterZoneId="
        + oldKiraMasterZoneId + ", newKiraMasterZoneId=" + newKiraMasterZoneId
        + ", masterZoneIdChanged=" + masterZoneIdChanged
        + ", oldKiraCrossMultiZoneRole=" + oldKiraCrossMultiZoneRole
        + ", newKiraCrossMultiZoneRole=" + newKiraCrossMultiZoneRole
        + ", kiraCrossMultiZoneRoleChanged="
        + kiraCrossMultiZoneRoleChanged + ", changed=" + changed + "]";
  }

}
