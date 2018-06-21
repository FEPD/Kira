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
package com.yihaodian.architecture.kira.common.monitor;

import java.io.Serializable;

public class MonitorNoticeInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private MonitorContext monitorContext;
  private boolean bad;
  private boolean recovered;
  private boolean repeatNoticeForBad;
  private String monitorDetails;

  public MonitorNoticeInfo(MonitorContext monitorContext, boolean bad, boolean recovered,
      boolean repeatNoticeForBad, String monitorDetails) {
    this.monitorContext = monitorContext;
    this.bad = bad;
    this.recovered = recovered;
    this.repeatNoticeForBad = repeatNoticeForBad;
    this.monitorDetails = monitorDetails;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public MonitorContext getMonitorContext() {
    return monitorContext;
  }

  public boolean isBad() {
    return bad;
  }

  public boolean isRecovered() {
    return recovered;
  }

  public boolean isRepeatNoticeForBad() {
    return repeatNoticeForBad;
  }

  public String getMonitorDetails() {
    return monitorDetails;
  }

  @Override
  public String toString() {
    return "MonitorNoticeInfo [monitorContext=" + monitorContext + ", bad="
        + bad + ", recovered=" + recovered + ", repeatNoticeForBad="
        + repeatNoticeForBad + ", monitorDetails=" + monitorDetails
        + "]";
  }

}
