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
package com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.util;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;

public class FiringTimerTriggerZNodeData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String fullPathOnZK;
  private String serverId;
  private Date lastServerStartedTime;
  private String appId;
  private String triggerId;
  private Date previousFireTime;
  private Date fireTimeFromNextFireTime;
  private boolean forMisfire;
  private boolean forRecovery;

  public FiringTimerTriggerZNodeData() {
    // TODO Auto-generated constructor stub
  }

  public FiringTimerTriggerZNodeData(String fullPathOnZK, String serverId,
      Date lastServerStartedTime,
      String appId, String triggerId, Date previousFireTime,
      Date fireTimeFromNextFireTime, boolean forMisfire, boolean forRecovery) {
    super();
    this.fullPathOnZK = fullPathOnZK;
    this.serverId = serverId;
    this.lastServerStartedTime = lastServerStartedTime;
    this.appId = appId;
    this.triggerId = triggerId;
    this.previousFireTime = previousFireTime;
    this.fireTimeFromNextFireTime = fireTimeFromNextFireTime;
    this.forMisfire = forMisfire;
    this.forRecovery = forRecovery;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getFullPathOnZK() {
    return fullPathOnZK;
  }

  public void setFullPathOnZK(String fullPathOnZK) {
    this.fullPathOnZK = fullPathOnZK;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public Date getLastServerStartedTime() {
    return lastServerStartedTime;
  }

  public void setLastServerStartedTime(Date lastServerStartedTime) {
    this.lastServerStartedTime = lastServerStartedTime;
  }

  public String getAppId() {
    return appId;
  }

  public void setAPpId(String appId) {
    this.appId = appId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public Date getPreviousFireTime() {
    return previousFireTime;
  }

  public void setPreviousFireTime(Date previousFireTime) {
    this.previousFireTime = previousFireTime;
  }

  public Date getFireTimeFromNextFireTime() {
    return fireTimeFromNextFireTime;
  }

  public void setFireTimeFromNextFireTime(Date fireTimeFromNextFireTime) {
    this.fireTimeFromNextFireTime = fireTimeFromNextFireTime;
  }

  public boolean isForMisfire() {
    return forMisfire;
  }

  public void setForMisfire(boolean forMisfire) {
    this.forMisfire = forMisfire;
  }

  public boolean isForRecovery() {
    return forRecovery;
  }

  public void setForRecovery(boolean forRecovery) {
    this.forRecovery = forRecovery;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((fireTimeFromNextFireTime == null) ? 0
        : fireTimeFromNextFireTime.hashCode());
    result = prime * result + (forMisfire ? 1231 : 1237);
    result = prime * result + (forRecovery ? 1231 : 1237);
    result = prime * result
        + ((fullPathOnZK == null) ? 0 : fullPathOnZK.hashCode());
    result = prime
        * result
        + ((lastServerStartedTime == null) ? 0 : lastServerStartedTime
        .hashCode());
    result = prime * result + ((appId == null) ? 0 : appId.hashCode());
    result = prime
        * result
        + ((previousFireTime == null) ? 0 : previousFireTime.hashCode());
    result = prime * result
        + ((serverId == null) ? 0 : serverId.hashCode());
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    FiringTimerTriggerZNodeData other = (FiringTimerTriggerZNodeData) obj;
    if (fireTimeFromNextFireTime == null) {
      if (other.fireTimeFromNextFireTime != null) {
        return false;
      }
    } else if (!fireTimeFromNextFireTime
        .equals(other.fireTimeFromNextFireTime)) {
      return false;
    }
    if (forMisfire != other.forMisfire) {
      return false;
    }
    if (forRecovery != other.forRecovery) {
      return false;
    }
    if (fullPathOnZK == null) {
      if (other.fullPathOnZK != null) {
        return false;
      }
    } else if (!fullPathOnZK.equals(other.fullPathOnZK)) {
      return false;
    }
    if (lastServerStartedTime == null) {
      if (other.lastServerStartedTime != null) {
        return false;
      }
    } else if (!lastServerStartedTime.equals(other.lastServerStartedTime)) {
      return false;
    }
    if (appId == null) {
      if (other.appId != null) {
        return false;
      }
    } else if (!appId.equals(other.appId)) {
      return false;
    }
    if (previousFireTime == null) {
      if (other.previousFireTime != null) {
        return false;
      }
    } else if (!previousFireTime.equals(other.previousFireTime)) {
      return false;
    }
    if (serverId == null) {
      if (other.serverId != null) {
        return false;
      }
    } else if (!serverId.equals(other.serverId)) {
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
    return "FiringTimerTriggerZNodeData [fullPathOnZK=" + fullPathOnZK
        + ", serverId=" + serverId + ", lastServerStartedTime="
        + KiraCommonUtils.getDateAsString(lastServerStartedTime,
        KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION) + ", appId=" + appId
        + ", triggerId="
        + triggerId + ", previousFireTime=" + KiraCommonUtils.getDateAsString(previousFireTime,
        KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)
        + ", fireTimeFromNextFireTime=" + KiraCommonUtils.getDateAsString(fireTimeFromNextFireTime,
        KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION)
        + ", forMisfire=" + forMisfire + ", forRecovery=" + forRecovery
        + "]";
  }

}
