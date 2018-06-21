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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import java.util.Date;

public class TriggerMetadata extends TriggerMetadataZNodeData {

  private static final long serialVersionUID = 1L;

  private Long id;

  private Boolean unregistered = Boolean.FALSE;

  private Date unregisteredUpdateTime;

  private Boolean deleted = Boolean.FALSE;

  private Date deletedUpdateTime;

  private Date finalizedTime;

  public TriggerMetadata() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Boolean getUnregistered() {
    return unregistered;
  }

  public void setUnregistered(Boolean unregistered) {
    this.unregistered = unregistered;
  }

  public Date getUnregisteredUpdateTime() {
    return unregisteredUpdateTime;
  }

  public void setUnregisteredUpdateTime(Date unregisteredUpdateTime) {
    this.unregisteredUpdateTime = unregisteredUpdateTime;
  }

  public String getUnregisteredUpdateTimeAsString() {
    return KiraCommonUtils.getDateAsString(unregisteredUpdateTime);
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public Date getDeletedUpdateTime() {
    return deletedUpdateTime;
  }

  public void setDeletedUpdateTime(Date deletedUpdateTime) {
    this.deletedUpdateTime = deletedUpdateTime;
  }

  public String getDeletedUpdateTimeAsString() {
    return KiraCommonUtils.getDateAsString(deletedUpdateTime);
  }

  public Date getFinalizedTime() {
    return finalizedTime;
  }

  public void setFinalizedTime(Date finalizedTime) {
    this.finalizedTime = finalizedTime;
  }

  public String getFinalizedTimeAsString() {
    return KiraCommonUtils.getDateAsString(finalizedTime);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());

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
    final TriggerMetadata other = (TriggerMetadata) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "id=" + "'" + id + "'" +
        ")";
  }

}
