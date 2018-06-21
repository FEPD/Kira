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

public class KiraCrossMultiZoneData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String kiraMasterZoneId;
  private KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole;

  public KiraCrossMultiZoneData() {
  }

  public KiraCrossMultiZoneData(String kiraMasterZoneId,
      KiraCrossMultiZoneRoleEnum kiraCrossMultiZoneRole) {
    super();
    this.kiraMasterZoneId = kiraMasterZoneId;
    this.kiraCrossMultiZoneRole = kiraCrossMultiZoneRole;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getKiraMasterZoneId() {
    return kiraMasterZoneId;
  }

  public KiraCrossMultiZoneRoleEnum getKiraCrossMultiZoneRole() {
    return kiraCrossMultiZoneRole;
  }

  @Override
  public String toString() {
    return "KiraCrossMultiZoneData [kiraMasterZoneId=" + kiraMasterZoneId
        + ", kiraCrossMultiZoneRole=" + kiraCrossMultiZoneRole + "]";
  }

}
