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
package com.yihaodian.architecture.kira.manager.web;

import com.yihaodian.architecture.kira.manager.domain.OperationLog;

public class HttpRequestContextHolder {

  private static final ThreadLocal<OperationLog> operationLogThreadLocal = new ThreadLocal<OperationLog>();

  public HttpRequestContextHolder() {
    // TODO Auto-generated constructor stub
  }

  public static OperationLog getOperationLog() {
    return operationLogThreadLocal.get();
  }

  public static void setOperationLog(OperationLog operationLog) {
    operationLogThreadLocal.set(operationLog);
  }

  public static void removeOperationLog() {
    operationLogThreadLocal.remove();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
