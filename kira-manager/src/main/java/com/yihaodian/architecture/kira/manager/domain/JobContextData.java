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
package com.yihaodian.architecture.kira.manager.domain;

import java.io.Serializable;
import java.util.Date;

public class JobContextData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Boolean manuallyScheduled;
  private String createdBy;
  private Date runAtTime;

  public JobContextData() {
    // TODO Auto-generated constructor stub
  }

  public JobContextData(Boolean manuallyScheduled, String createdBy,
      Date runAtTime) {
    super();
    this.manuallyScheduled = manuallyScheduled;
    this.createdBy = createdBy;
    this.runAtTime = runAtTime;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Boolean getManuallyScheduled() {
    return manuallyScheduled;
  }

  public void setManuallyScheduled(Boolean manuallyScheduled) {
    this.manuallyScheduled = manuallyScheduled;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getRunAtTime() {
    return runAtTime;
  }

  public void setRunAtTime(Date runAtTime) {
    this.runAtTime = runAtTime;
  }

  @Override
  public String toString() {
    return "JobContextData [manuallyScheduled=" + manuallyScheduled
        + ", createdBy=" + createdBy + ", runAtTime=" + runAtTime + "]";
  }

}
