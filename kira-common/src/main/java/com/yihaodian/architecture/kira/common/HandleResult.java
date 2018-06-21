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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HandleResult implements Serializable {

  private static final long serialVersionUID = 1L;
  protected String resultCode;
  protected String resultData;
  protected Date createTime;
  private Map<Serializable, Serializable> otherDataMap = new LinkedHashMap<Serializable, Serializable>();

  public HandleResult() {
    // TODO Auto-generated constructor stub
  }

  public HandleResult(String resultCode, String resultData) {
    super();
    this.resultCode = resultCode;
    this.resultData = resultData;
    this.createTime = new Date();
    ;
  }

  public static HandleResult getSuccessHandleResult() {
    return new HandleResult(KiraCommonConstants.RESULT_CODE_SUCCESS, null);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getResultCode() {
    return resultCode;
  }

  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  public String getResultData() {
    return resultData;
  }

  public void setResultData(String resultData) {
    this.resultData = resultData;
  }

  public Map<Serializable, Serializable> getOtherDataMap() {
    return otherDataMap;
  }

  public void setOtherDataMap(
      Map<Serializable, Serializable> otherDataMap) {
    this.otherDataMap = otherDataMap;
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
    return "HandleResult [resultCode=" + resultCode + ", resultData="
        + resultData + ", otherDataMap=" + otherDataMap
        + ", createTime=" + createTime + "]";
  }

}
