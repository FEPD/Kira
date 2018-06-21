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

public class ManuallyReRunJobResult extends HandleResult {

  private static final long serialVersionUID = 1L;

  private String oldJobId;
  private String newJobId;

  public ManuallyReRunJobResult() {
    // TODO Auto-generated constructor stub
  }

  public ManuallyReRunJobResult(String oldJobId, String resultCode, String resultData,
      String newJobId) {
    super(resultCode, resultData);
    this.oldJobId = oldJobId;
    this.newJobId = newJobId;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getOldJobId() {
    return oldJobId;
  }

  public void setOldJobId(String oldJobId) {
    this.oldJobId = oldJobId;
  }

  public String getNewJobId() {
    return newJobId;
  }

  public void setNewJobId(String newJobId) {
    this.newJobId = newJobId;
  }

  @Override
  public String toString() {
    return "ManuallyReRunJobResult [oldJobId=" + oldJobId + ", newJobId="
        + newJobId + ", resultCode=" + resultCode + ", resultData="
        + resultData + ", createTime=" + createTime + "]";
  }

}
