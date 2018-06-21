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
package com.yihaodian.architecture.kira.server.internal.util;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;

public class KiraServerZNodeData implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private String serverId;
  private String host;
  private Integer port;
  private String accessUrlAsString;
  private Date createTime;

  public KiraServerZNodeData() {
  }

  public KiraServerZNodeData(String serverId, String host, Integer port,
      String accessUrlAsString) {
    super();
    this.serverId = serverId;
    this.host = host;
    this.port = port;
    this.accessUrlAsString = accessUrlAsString;
    this.createTime = new Date();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getServerId() {
    return serverId;
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getAccessUrlAsString() {
    return accessUrlAsString;
  }

  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((accessUrlAsString == null) ? 0 : accessUrlAsString
        .hashCode());
    result = prime * result
        + ((createTime == null) ? 0 : createTime.hashCode());
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((port == null) ? 0 : port.hashCode());
    result = prime * result
        + ((serverId == null) ? 0 : serverId.hashCode());
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
    KiraServerZNodeData other = (KiraServerZNodeData) obj;
    if (accessUrlAsString == null) {
      if (other.accessUrlAsString != null) {
        return false;
      }
    } else if (!accessUrlAsString.equals(other.accessUrlAsString)) {
      return false;
    }
    if (createTime == null) {
      if (other.createTime != null) {
        return false;
      }
    } else if (!createTime.equals(other.createTime)) {
      return false;
    }
    if (host == null) {
      if (other.host != null) {
        return false;
      }
    } else if (!host.equals(other.host)) {
      return false;
    }
    if (port == null) {
      if (other.port != null) {
        return false;
      }
    } else if (!port.equals(other.port)) {
      return false;
    }
    if (serverId == null) {
      if (other.serverId != null) {
        return false;
      }
    } else if (!serverId.equals(other.serverId)) {
      return false;
    }
    return true;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public String toDetailString() {
    return "KiraServerZNodeData [serverId=" + serverId + ", host=" + host
        + ", port=" + port + ", accessUrlAsString=" + accessUrlAsString
        + ", createTime=" + KiraCommonUtils.getDateAsString(createTime) + "]";
  }

  @Override
  public String toString() {
    return "KiraServerZNodeData [serverId=" + serverId + ", host=" + host
        + ", port=" + port + ", accessUrlAsString=" + accessUrlAsString
        + ", createTime=" + KiraCommonUtils.getDateAsString(createTime) + "]";
  }

}
