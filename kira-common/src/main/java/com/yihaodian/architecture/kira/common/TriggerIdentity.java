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
package com.yihaodian.architecture.kira.common;

import java.io.Serializable;

public class TriggerIdentity implements Serializable {

  private static final long serialVersionUID = 1L;
  private String appId;
  private String triggerId;
  private String version;

  public TriggerIdentity(String appId, String triggerId) {
    super();
    this.appId = appId;
    this.triggerId = triggerId;
  }

  public TriggerIdentity(String appId, String triggerId, String version) {
    super();
    this.appId = appId;
    this.triggerId = triggerId;
    this.version = version;
  }

  public TriggerIdentity() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((appId == null) ? 0 : appId.hashCode());
    result = prime * result
        + ((triggerId == null) ? 0 : triggerId.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    if (!(obj instanceof TriggerIdentity)) {
      return false;
    }
    TriggerIdentity other = (TriggerIdentity) obj;
    if (appId == null) {
      if (other.appId != null) {
        return false;
      }
    } else if (!appId.equals(other.appId)) {
      return false;
    }
    if (triggerId == null) {
      if (other.triggerId != null) {
        return false;
      }
    } else if (!triggerId.equals(other.triggerId)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    } else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TriggerIdentity [appId=" + appId + ", triggerId=" + triggerId
        + ", version=" + version + "]";
  }

}
