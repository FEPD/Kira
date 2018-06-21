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
package com.yihaodian.architecture.kira.common.lock.impl.zk;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;

public class LockZNodeData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String lockBasePath;
  private Date createTime;

  public LockZNodeData(String lockBasePath, Date createTime) {
    this.lockBasePath = lockBasePath;
    this.createTime = createTime;
  }

  public String getLockBasePath() {
    return lockBasePath;
  }

  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public String toString() {
    return "LockZNodeData{" +
        "lockBasePath='" + lockBasePath + '\'' +
        ", createTime=" + KiraCommonUtils.getDateAsStringToMsPrecision(createTime) +
        '}';
  }
}
