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

import com.yihaodian.architecture.kira.common.HandleResult;

public class UpdateTriggerResult extends HandleResult {

  private static final long serialVersionUID = 1L;

  private Long oldId;
  private String oldVersion;
  private Long newId;
  private String newVersion;

  public UpdateTriggerResult() {
    // TODO Auto-generated constructor stub
  }

  public UpdateTriggerResult(Long oldId, String oldVersion, String resultCode, String resultData,
      Long newId, String newVersion) {
    super(resultCode, resultData);
    this.oldId = oldId;
    this.oldVersion = oldVersion;
    this.newId = newId;
    this.newVersion = newVersion;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Long getOldId() {
    return oldId;
  }

  public void setOldId(Long oldId) {
    this.oldId = oldId;
  }

  public String getOldVersion() {
    return oldVersion;
  }

  public void setOldVersion(String oldVersion) {
    this.oldVersion = oldVersion;
  }

  public Long getNewId() {
    return newId;
  }

  public void setNewId(Long newId) {
    this.newId = newId;
  }

  public String getNewVersion() {
    return newVersion;
  }

  public void setNewVersion(String newVersion) {
    this.newVersion = newVersion;
  }

}
