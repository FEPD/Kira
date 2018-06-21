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


public class JobItemHistoryCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String id;
  private String jobItemId;

  private String newTableNameSuffixForArchive;
  private String startTimeForArchive;
  private String endTimeForArchive;

  public JobItemHistoryCriteria() {
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
