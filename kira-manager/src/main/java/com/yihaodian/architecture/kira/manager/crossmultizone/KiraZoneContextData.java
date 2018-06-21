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
package com.yihaodian.architecture.kira.manager.crossmultizone;

import java.io.Serializable;

public class KiraZoneContextData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String currentKiraZoneId;

  private String kiraCrossMultiZoneRoleName;

  private String kiraMasterZoneId;

  public KiraZoneContextData() {
    // TODO Auto-generated constructor stub
  }

  public KiraZoneContextData(String currentKiraZoneId,
      String kiraCrossMultiZoneRoleName, String kiraMasterZoneId) {
    super();
    this.currentKiraZoneId = currentKiraZoneId;
    this.kiraCrossMultiZoneRoleName = kiraCrossMultiZoneRoleName;
    this.kiraMasterZoneId = kiraMasterZoneId;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getCurrentKiraZoneId() {
    return currentKiraZoneId;
  }

  public void setCurrentKiraZoneId(String currentKiraZoneId) {
    this.currentKiraZoneId = currentKiraZoneId;
  }

  public String getKiraCrossMultiZoneRoleName() {
    return kiraCrossMultiZoneRoleName;
  }

  public void setKiraCrossMultiZoneRoleName(String kiraCrossMultiZoneRoleName) {
    this.kiraCrossMultiZoneRoleName = kiraCrossMultiZoneRoleName;
  }

  public String getKiraMasterZoneId() {
    return kiraMasterZoneId;
  }

  public void setKiraMasterZoneId(String kiraMasterZoneId) {
    this.kiraMasterZoneId = kiraMasterZoneId;
  }

}
