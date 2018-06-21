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
import java.io.Serializable;
import java.util.Date;

public class AppCenterZNodeData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String appId;

  private String host;

  private Integer port;

  private String serviceUrl;

  private Date createTime = new Date();

  public AppCenterZNodeData() {
    // TODO Auto-generated constructor stub
  }

  public AppCenterZNodeData(String appId, String host, Integer port,
      String serviceUrl) {
    super();
    this.appId = appId;
    this.host = host;
    this.port = port;
    this.serviceUrl = serviceUrl;
    this.createTime = new Date();
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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
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

  @Override
  public String toString() {
    return "AppCenterZNodeData [poolId=" + appId + ", host=" + host
        + ", port=" + port + ", serviceUrl=" + serviceUrl
        + ", createTime=" + createTime + "]";
  }

}
