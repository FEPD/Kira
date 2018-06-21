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

import com.yihaodian.architecture.kira.manager.util.Paging;

public class KiraClientCriteria extends Criteria {

  private static final long serialVersionUID = 1L;

  private String centralScheduleServiceUrl;

  public KiraClientCriteria() {
    // TODO Auto-generated constructor stub
  }

  public KiraClientCriteria(int maxResults) {
    super(maxResults);
    // TODO Auto-generated constructor stub
  }

  public KiraClientCriteria(Paging paging) {
    super(paging);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getCentralScheduleServiceUrl() {
    return centralScheduleServiceUrl;
  }

  public void setCentralScheduleServiceUrl(String centralScheduleServiceUrl) {
    this.centralScheduleServiceUrl = centralScheduleServiceUrl;
  }

}
