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
import java.io.Serializable;
import java.util.Date;

public class OperationLogDetailData implements Serializable {

  private static final long serialVersionUID = 1L;
  private String id;
  private Integer operationId;
  private String operatedBy;
  private Date operateTime;
  private String operationDetails;
  private String resultCode;
  private String resultDetails;

  private String operationName;
  private String operationDisplay;
  private Integer operationType;

  public OperationLogDetailData() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getOperationId() {
    return operationId;
  }

  public void setOperationId(Integer operationId) {
    this.operationId = operationId;
  }

  public String getOperatedBy() {
    return operatedBy;
  }

  public void setOperatedBy(String operatedBy) {
    this.operatedBy = operatedBy;
  }

  public Date getOperateTime() {
    return operateTime;
  }

  public void setOperateTime(Date operateTime) {
    this.operateTime = operateTime;
  }

  public String getOperateTimeAsString() {
    return KiraCommonUtils.getDateAsString(operateTime);
  }

  public String getOperationDetails() {
    return operationDetails;
  }

  public void setOperationDetails(String operationDetails) {
    this.operationDetails = operationDetails;
  }

  public String getResultCode() {
    return resultCode;
  }

  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  public String getResultDetails() {
    return resultDetails;
  }

  public void setResultDetails(String resultDetails) {
    this.resultDetails = resultDetails;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public String getOperationDisplay() {
    return operationDisplay;
  }

  public void setOperationDisplay(String operationDisplay) {
    this.operationDisplay = operationDisplay;
  }

  public Integer getOperationType() {
    return operationType;
  }

  public void setOperationType(Integer operationType) {
    this.operationType = operationType;
  }

}
