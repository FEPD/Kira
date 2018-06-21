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
package com.yihaodian.architecture.kira.client.internal.impl;

import com.yihaodian.architecture.kira.common.impl.AbstractEnvironment;

public abstract class ProcessEnvironment extends AbstractEnvironment {

  private static final long serialVersionUID = 1L;

  protected Integer pid;

  public ProcessEnvironment() {
    // TODO Auto-generated constructor stub
  }

  public ProcessEnvironment(Integer pid) {
    super();
    this.pid = pid;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  public Integer getPid() {
    return pid;
  }

  public void setPid(Integer pid) {
    this.pid = pid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pid == null) ? 0 : pid.hashCode());
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
    if (!(obj instanceof ProcessEnvironment)) {
      return false;
    }
    ProcessEnvironment other = (ProcessEnvironment) obj;
    if (pid == null) {
      if (other.pid != null) {
        return false;
      }
    } else if (!pid.equals(other.pid)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ProcessEnvironment [pid=" + pid + ", environmentStatus="
        + getEnvironmentStatus() + "]";
  }

}
