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
import java.util.List;
import org.apache.commons.lang.StringUtils;


public class JobCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String id;

  private String jobItemId;

  private Integer jobStatusId;

  private String appId;

  private String triggerId;

  private String version;

  private String cancelJobJsonMapString;

  private List<Integer> jobStatusIdList;

  private String newTableNameSuffixForArchive;
  private String startTimeForArchive;
  private String endTimeForArchive;

  private List<String> poolIdListToExclude;

  private Date createTimeStart;

  private String createTimeStartAsString;

  private Date createTimeEnd;

  private String createTimeEndAsString;

  private String noPropertyPlaceHolder;

  public JobCriteria() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getJobItemId() {
    return jobItemId;
  }

  public void setJobItemId(String jobItemId) {
    this.jobItemId = jobItemId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Integer getJobStatusId() {
    return jobStatusId;
  }

  public void setJobStatusId(Integer jobStatusId) {
    this.jobStatusId = jobStatusId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCancelJobJsonMapString() {
    return cancelJobJsonMapString;
  }

  public void setCancelJobJsonMapString(String cancelJobJsonMapString) {
    this.cancelJobJsonMapString = cancelJobJsonMapString;
  }

  public List<Integer> getJobStatusIdList() {
    return jobStatusIdList;
  }

  public void setJobStatusIdList(List<Integer> jobStatusIdList) {
    this.jobStatusIdList = jobStatusIdList;
  }

  public String getNewTableNameSuffixForArchive() {
    return newTableNameSuffixForArchive;
  }

  public void setNewTableNameSuffixForArchive(String newTableNameSuffixForArchive) {
    this.newTableNameSuffixForArchive = newTableNameSuffixForArchive;
  }

  public String getStartTimeForArchive() {
    return startTimeForArchive;
  }

  public void setStartTimeForArchive(String startTimeForArchive) {
    this.startTimeForArchive = startTimeForArchive;
  }

  public String getEndTimeForArchive() {
    return endTimeForArchive;
  }

  public void setEndTimeForArchive(String endTimeForArchive) {
    this.endTimeForArchive = endTimeForArchive;
  }

  public List<String> getPoolIdListToExclude() {
    return poolIdListToExclude;
  }

  public void setPoolIdListToExclude(List<String> poolIdListToExclude) {
    this.poolIdListToExclude = poolIdListToExclude;
  }

  public Date getCreateTimeStart() {
    return createTimeStart;
  }

  public void setCreateTimeStart(Date createTimeStart) {
    this.createTimeStart = createTimeStart;
  }

  public String getCreateTimeStartAsString() {
    return createTimeStartAsString;
  }

  public void setCreateTimeStartAsString(String createTimeStartAsString) {
    this.createTimeStartAsString = createTimeStartAsString;
    if (StringUtils.isNotBlank(this.createTimeStartAsString)) {
      try {
        this.createTimeStart = KiraCommonUtils.getDateFromString(createTimeStartAsString, true);
      } catch (ParseException e) {
        logger.warn(
            "The value of createTimeStartAsString is not valid. Just ignore it now. createTimeStartAsString={} and valid dateFormat={}",
            createTimeStartAsString, KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }

  public Date getCreateTimeEnd() {
    return createTimeEnd;
  }

  public void setCreateTimeEnd(Date createTimeEnd) {
    this.createTimeEnd = createTimeEnd;
  }

  public String getCreateTimeEndAsString() {
    return createTimeEndAsString;
  }

  public void setCreateTimeEndAsString(String createTimeEndAsString) {
    this.createTimeEndAsString = createTimeEndAsString;
    if (StringUtils.isNotBlank(this.createTimeEndAsString)) {
      try {
        this.createTimeEnd = KiraCommonUtils.getDateFromString(createTimeEndAsString, true);
      } catch (ParseException e) {
        logger.warn(
            "The value of createTimeEndAsString is not valid. Just ignore it now. createTimeEndAsString={} and valid dateFormat={}",
            createTimeEndAsString, KiraCommonConstants.DATEFORMAT_DEFAULT);
      }
    }
  }

}
