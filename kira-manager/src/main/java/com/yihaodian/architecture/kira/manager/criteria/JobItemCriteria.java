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

/**
 * This package provide the common code for other packages in the parent package.
 */
package com.yihaodian.architecture.kira.manager.criteria;


public class JobItemCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String id;

  private String jobId;

  private String appId;

  private String triggerId;

  private String version;

  private String newTableNameSuffixForArchive;
  private String startTimeForArchive;
  private String endTimeForArchive;

  public JobItemCriteria() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
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

}
