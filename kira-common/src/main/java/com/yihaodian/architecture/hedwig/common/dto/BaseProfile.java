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
package com.yihaodian.architecture.hedwig.common.dto;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import com.yihaodian.architecture.hedwig.common.util.ZkUtil;
import java.io.Serializable;

/**
 * @author Archer Jiang
 */
public class BaseProfile implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -6856572567927129370L;
  protected String rootPath = InternalConstants.BASE_ROOT;
  protected String domainName = InternalConstants.UNKONW_DOMAIN;
  protected String parentPath;
  protected String serviceAppName = "defaultAppName";
  protected String serviceName = "defaultServiceName";
  protected String serviceVersion = "defaultVersion";

  public BaseProfile() {
    super();
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = HedwigUtil.filterString(domainName);
  }

  public String getServiceAppName() {
    return serviceAppName;
  }

  public void setServiceAppName(String serviceAppName) {
    this.serviceAppName = HedwigUtil.filterString(serviceAppName);
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = HedwigUtil.filterString(serviceName);
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = HedwigUtil.filterString(serviceVersion);
  }

  public String getParentPath() {
    if (HedwigUtil.isBlankString(parentPath)) {
      try {
        parentPath = ZkUtil.createParentPath(this);
      } catch (InvalidParamException e) {

      }
    }
    return parentPath;
  }

  @Override
  public String toString() {
    return "\nBaseProfile [rootPath=" + rootPath + ", parentPath=" + parentPath
        + ", \nserviceAppName="
        + serviceAppName + ", serviceName="
        + serviceName + ", serviceVersion=" + serviceVersion + "]";
  }

}
