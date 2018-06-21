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
package com.yihaodian.architecture.kira.common.zk;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerMetadataDetail;
import java.util.Date;

public class TriggerMetadataZNodeData extends TriggerMetadataDetail {

  private static final long serialVersionUID = 1L;

  private Boolean manuallyCreated = Boolean.FALSE;

  private String manuallyCreatedBy;

  private Date createTime = new Date();

  public TriggerMetadataZNodeData() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Boolean getManuallyCreated() {
    if (null == manuallyCreated) {
      setManuallyCreated(Boolean.FALSE);
    }
    return manuallyCreated;
  }

  public void setManuallyCreated(Boolean manuallyCreated) {
    this.manuallyCreated = manuallyCreated;
  }

  public String getManuallyCreatedBy() {
    return manuallyCreatedBy;
  }

  public void setManuallyCreatedBy(String manuallyCreatedBy) {
    this.manuallyCreatedBy = manuallyCreatedBy;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateTimeAsString() {
    return KiraCommonUtils.getDateAsString(createTime);
  }

}
