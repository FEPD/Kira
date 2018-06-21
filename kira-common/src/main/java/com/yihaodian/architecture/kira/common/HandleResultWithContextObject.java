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

public class HandleResultWithContextObject<C> {

  private HandleResult handleResult;
  private C contextObject;

  public HandleResultWithContextObject() {
    // TODO Auto-generated constructor stub
  }

  public HandleResultWithContextObject(HandleResult handleResult,
      C contextObject) {
    super();
    this.handleResult = handleResult;
    this.contextObject = contextObject;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public HandleResult getHandleResult() {
    return handleResult;
  }

  public void setHandleResult(HandleResult handleResult) {
    this.handleResult = handleResult;
  }

  public C getContextObject() {
    return contextObject;
  }

  public void setContextObject(C contextObject) {
    this.contextObject = contextObject;
  }

  @Override
  public String toString() {
    return "HandleResultWithContextObject [handleResult=" + handleResult
        + ", contextObject=" + contextObject + "]";
  }

}
