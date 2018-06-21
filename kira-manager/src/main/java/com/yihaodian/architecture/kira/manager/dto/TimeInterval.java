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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.util.Date;

public class TimeInterval implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private Date startTime;
  private Date endTime;

  public TimeInterval() {
    // TODO Auto-generated constructor stub
  }

  public TimeInterval(Date startTime, Date endTime) {
    super();
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
    result = prime * result
        + ((startTime == null) ? 0 : startTime.hashCode());
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
    TimeInterval other = (TimeInterval) obj;
    if (endTime == null) {
      if (other.endTime != null) {
        return false;
      }
    } else if (!endTime.equals(other.endTime)) {
      return false;
    }
    if (startTime == null) {
      if (other.startTime != null) {
        return false;
      }
    } else if (!startTime.equals(other.startTime)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TimeInterval [startTime=" + KiraCommonUtils.getDateAsString(startTime) + ", endTime="
        + KiraCommonUtils.getDateAsString(endTime)
        + "]";
  }

}
