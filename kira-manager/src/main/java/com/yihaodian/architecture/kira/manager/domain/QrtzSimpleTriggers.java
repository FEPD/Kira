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

public class QrtzSimpleTriggers implements Serializable {

  private static final long serialVersionUID = 1L;

  private String triggerName;

  private String triggerGroup;

  private Long repeatCount;

  private Long repeatInterval;

  private Integer timesTriggered;

  public QrtzSimpleTriggers() {
  }

  public String getTriggerName() {
    return triggerName;
  }

  public void setTriggerName(String triggerName) {
    this.triggerName = triggerName;
  }

  public String getTriggerGroup() {
    return triggerGroup;
  }

  public void setTriggerGroup(String triggerGroup) {
    this.triggerGroup = triggerGroup;
  }

  public Long getRepeatCount() {
    return repeatCount;
  }

  public void setRepeatCount(Long repeatCount) {
    this.repeatCount = repeatCount;
  }

  public Long getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatInterval(Long repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  public Integer getTimesTriggered() {
    return timesTriggered;
  }

  public void setTimesTriggered(Integer timesTriggered) {
    this.timesTriggered = timesTriggered;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((triggerGroup == null) ? 0 : triggerGroup.hashCode());
    result = prime * result + ((triggerName == null) ? 0 : triggerName.hashCode());

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
    final QrtzSimpleTriggers other = (QrtzSimpleTriggers) obj;
    if (triggerGroup == null) {
      if (other.triggerGroup != null) {
        return false;
      }
    } else if (!triggerGroup.equals(other.triggerGroup)) {
      return false;
    }
    if (triggerName == null) {
      if (other.triggerName != null) {
        return false;
      }
    } else if (!triggerName.equals(other.triggerName)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "triggerGroup=" + "'" + triggerGroup + "'" + ", " +
        "triggerName=" + "'" + triggerName + "'" +
        ")";
  }

}
