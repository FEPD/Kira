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
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import java.io.Serializable;
import java.util.Date;

public class LocalSchedulerStartedZNodeData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date lastStartedTime;

  private KiraServerEntity kiraServerEntity;

  public LocalSchedulerStartedZNodeData() {
    // TODO Auto-generated constructor stub
  }

  public LocalSchedulerStartedZNodeData(Date lastStartedTime,
      KiraServerEntity kiraServerEntity) {
    super();
    this.lastStartedTime = lastStartedTime;
    this.kiraServerEntity = kiraServerEntity;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Date getLastStartedTime() {
    return lastStartedTime;
  }

  public void setLastStartedTime(Date lastStartedTime) {
    this.lastStartedTime = lastStartedTime;
  }

  public KiraServerEntity getKiraServerEntity() {
    return kiraServerEntity;
  }

  public void setKiraServerEntity(KiraServerEntity kiraServerEntity) {
    this.kiraServerEntity = kiraServerEntity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((kiraServerEntity == null) ? 0 : kiraServerEntity.hashCode());
    result = prime * result
        + ((lastStartedTime == null) ? 0 : lastStartedTime.hashCode());
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
    LocalSchedulerStartedZNodeData other = (LocalSchedulerStartedZNodeData) obj;
    if (kiraServerEntity == null) {
      if (other.kiraServerEntity != null) {
        return false;
      }
    } else if (!kiraServerEntity.equals(other.kiraServerEntity)) {
      return false;
    }
    if (lastStartedTime == null) {
      if (other.lastStartedTime != null) {
        return false;
      }
    } else if (!lastStartedTime.equals(other.lastStartedTime)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "LocalSchedulerStartedZNodeData [lastStartedTime="
        + KiraCommonUtils.getDateAsString(lastStartedTime,
        KiraCommonConstants.DATEFORMAT_YYYYMMDDHHMMSSSSS_FOR_VERSION) + ", kiraServerEntity="
        + kiraServerEntity
        + "]";
  }

}
