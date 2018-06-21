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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;
import java.io.Serializable;
import java.util.List;

public class TriggerEnvironment implements Serializable {

  private static final long serialVersionUID = 1L;
  private String poolId;
  private String triggerId;
  private String triggerEnvironmentZKFullPath;
  private String centralScheduleServiceZKParentPath;
  private List<String> centralScheduleServiceUrlList;
  private List<ServiceProfile> centralScheduleServiceProfileList;

  public TriggerEnvironment() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getPoolId() {
    return poolId;
  }

  public void setPoolId(String poolId) {
    this.poolId = poolId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public String getTriggerEnvironmentZKFullPath() {
    return triggerEnvironmentZKFullPath;
  }

  public void setTriggerEnvironmentZKFullPath(String triggerEnvironmentZKFullPath) {
    this.triggerEnvironmentZKFullPath = triggerEnvironmentZKFullPath;
  }

  public String getCentralScheduleServiceZKParentPath() {
    return centralScheduleServiceZKParentPath;
  }

  public void setCentralScheduleServiceZKParentPath(
      String centralScheduleServiceZKParentPath) {
    this.centralScheduleServiceZKParentPath = centralScheduleServiceZKParentPath;
  }

  public List<String> getCentralScheduleServiceUrlList() {
    return centralScheduleServiceUrlList;
  }

  public void setCentralScheduleServiceUrlList(
      List<String> centralScheduleServiceUrlList) {
    this.centralScheduleServiceUrlList = centralScheduleServiceUrlList;
  }

  public List<ServiceProfile> getCentralScheduleServiceProfileList() {
    return centralScheduleServiceProfileList;
  }

  public void setCentralScheduleServiceProfileList(
      List<ServiceProfile> centralScheduleServiceProfileList) {
    this.centralScheduleServiceProfileList = centralScheduleServiceProfileList;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((poolId == null) ? 0 : poolId.hashCode());
    result = prime
        * result
        + ((triggerEnvironmentZKFullPath == null) ? 0
        : triggerEnvironmentZKFullPath.hashCode());
    result = prime * result
        + ((triggerId == null) ? 0 : triggerId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TriggerEnvironment)) {
      return false;
    }
    TriggerEnvironment other = (TriggerEnvironment) obj;
    if (poolId == null) {
      if (other.poolId != null) {
        return false;
      }
    } else if (!poolId.equals(other.poolId)) {
      return false;
    }
    if (triggerEnvironmentZKFullPath == null) {
      if (other.triggerEnvironmentZKFullPath != null) {
        return false;
      }
    } else if (!triggerEnvironmentZKFullPath
        .equals(other.triggerEnvironmentZKFullPath)) {
      return false;
    }
    if (triggerId == null) {
      if (other.triggerId != null) {
        return false;
      }
    } else if (!triggerId.equals(other.triggerId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TriggerEnvironment [poolId=" + poolId + ", triggerId="
        + triggerId + ", triggerEnvironmentZKFullPath="
        + triggerEnvironmentZKFullPath
        + ", centralScheduleServiceZKParentPath="
        + centralScheduleServiceZKParentPath
        + ", centralScheduleServiceUrlList="
        + centralScheduleServiceUrlList
        + ", centralScheduleServiceProfileList="
        + centralScheduleServiceProfileList + "]";
  }

}
