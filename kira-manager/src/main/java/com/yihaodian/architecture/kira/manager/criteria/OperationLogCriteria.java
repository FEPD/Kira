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
package com.yihaodian.architecture.kira.manager.criteria;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

public class OperationLogCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String id;

  private String operatedBy;

  private String resultCode;

  private String operationName;

  private Integer operationType;

  private Date operateTimeStart;

  private String operateTimeStartAsString;

  private Date operateTimeEnd;

  private String operateTimeEndAsString;

  public OperationLogCriteria() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOperatedBy() {
    return operatedBy;
  }

  public void setOperatedBy(String operatedBy) {
    this.operatedBy = operatedBy;
  }

  public String getResultCode() {
    return resultCode;
  }

  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public Integer getOperationType() {
    return operationType;
  }

  public void setOperationType(Integer operationType) {
    this.operationType = operationType;
  }

  public Date getOperateTimeStart() {
    return operateTimeStart;
  }

  public void setOperateTimeStart(Date operateTimeStart) {
    this.operateTimeStart = operateTimeStart;
  }

  public String getOperateTimeStartAsString() {
    return operateTimeStartAsString;
  }

  public void setOperateTimeStartAsString(String operateTimeStartAsString) {
    this.operateTimeStartAsString = operateTimeStartAsString;
    if (StringUtils.isNotBlank(this.operateTimeStartAsString)) {
      try {
        this.operateTimeStart = KiraCommonUtils.getDateFromString(operateTimeStartAsString, true);
      } catch (ParseException e) {
        logger.warn(
            "The value of operateTimeStartAsString is not valid. Just ignore it now. operateTimeStartAsString={} and valid dateFormat={}",
            operateTimeStartAsString, KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }

  public Date getOperateTimeEnd() {
    return operateTimeEnd;
  }

  public void setOperateTimeEnd(Date operateTimeEnd) {
    this.operateTimeEnd = operateTimeEnd;
  }

  public String getOperateTimeEndAsString() {
    return operateTimeEndAsString;
  }

  public void setOperateTimeEndAsString(String operateTimeEndAsString) {
    this.operateTimeEndAsString = operateTimeEndAsString;
    if (StringUtils.isNotBlank(this.operateTimeEndAsString)) {
      try {
        this.operateTimeEnd = KiraCommonUtils.getDateFromString(operateTimeEndAsString, true);
      } catch (ParseException e) {
        logger.warn(
            "The value of operateTimeEndAsString is not valid. Just ignore it now. operateTimeEndAsString={} and valid dateFormat={}",
            operateTimeEndAsString, KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }
}
