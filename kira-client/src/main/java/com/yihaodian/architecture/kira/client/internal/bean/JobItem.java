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

package com.yihaodian.architecture.kira.client.internal.bean;

import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class JobItem implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private Integer jobStatusId;
  private String resultData;
  private String jobId;
  private IEnvironment environment;
  private Date createTime;
  private Date lastUpdateTime;

  private transient CountDownLatch deliveringPhaseEventHandledSignal = new CountDownLatch(1);
  private transient CountDownLatch deliverCompletePhaseEventHandledSignal = new CountDownLatch(1);
  private transient CountDownLatch noNeedToRunBusinessMethodPhaseEventHandledSignal = new CountDownLatch(
      1);
  private transient CountDownLatch runningPhaseEventHandledSignal = new CountDownLatch(1);
  private transient CountDownLatch runCompletePhaseEventHandledSignal = new CountDownLatch(1);

  public JobItem() {
  }

  public JobItem(String id, Integer jobStatusId, String resultData, String jobId,
      IEnvironment environment) {
    this(id, jobStatusId, resultData, jobId, environment, new Date());
  }

  public JobItem(String id, Integer jobStatusId, String resultData, String jobId,
      IEnvironment environment, Date createTime) {
    super();
    this.id = id;
    this.jobStatusId = jobStatusId;
    this.resultData = resultData;
    this.jobId = jobId;
    this.environment = environment;
    this.createTime = createTime;
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

  public Integer getJobStatusId() {
    return jobStatusId;
  }

  public void setJobStatusId(Integer jobStatusId) {
    this.jobStatusId = jobStatusId;
  }

  public String getResultData() {
    return resultData;
  }

  public void setResultData(String resultData) {
    this.resultData = resultData;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public IEnvironment getEnvironment() {
    return environment;
  }

  public void setEnvironment(IEnvironment environment) {
    this.environment = environment;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public CountDownLatch getDeliveringPhaseEventHandledSignal() {
    return deliveringPhaseEventHandledSignal;
  }

  public void setDeliveringPhaseEventHandledSignal(
      CountDownLatch deliveringPhaseEventHandledSignal) {
    this.deliveringPhaseEventHandledSignal = deliveringPhaseEventHandledSignal;
  }

  public CountDownLatch getDeliverCompletePhaseEventHandledSignal() {
    return deliverCompletePhaseEventHandledSignal;
  }

  public void setDeliverCompletePhaseEventHandledSignal(
      CountDownLatch deliverCompletePhaseEventHandledSignal) {
    this.deliverCompletePhaseEventHandledSignal = deliverCompletePhaseEventHandledSignal;
  }

  public CountDownLatch getNoNeedToRunBusinessMethodPhaseEventHandledSignal() {
    return noNeedToRunBusinessMethodPhaseEventHandledSignal;
  }

  public void setNoNeedToRunBusinessMethodPhaseEventHandledSignal(
      CountDownLatch noNeedToRunBusinessMethodPhaseEventHandledSignal) {
    this.noNeedToRunBusinessMethodPhaseEventHandledSignal = noNeedToRunBusinessMethodPhaseEventHandledSignal;
  }

  public CountDownLatch getRunningPhaseEventHandledSignal() {
    return runningPhaseEventHandledSignal;
  }

  public void setRunningPhaseEventHandledSignal(
      CountDownLatch runningPhaseEventHandledSignal) {
    this.runningPhaseEventHandledSignal = runningPhaseEventHandledSignal;
  }

  public CountDownLatch getRunCompletePhaseEventHandledSignal() {
    return runCompletePhaseEventHandledSignal;
  }

  public void setRunCompletePhaseEventHandledSignal(
      CountDownLatch runCompletePhaseEventHandledSignal) {
    this.runCompletePhaseEventHandledSignal = runCompletePhaseEventHandledSignal;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JobItem)) {
      return false;
    }
    JobItem other = (JobItem) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Date lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  public String toString() {
    return "JobItem [id=" + id + ", jobStatusId=" + jobStatusId
        + ", resultData=" + resultData + ", jobId=" + jobId
        + ", environment=" + environment + ", createTime=" + createTime
        + ", lastUpdateTime=" + lastUpdateTime
        + ", deliveringPhaseEventHandledSignal="
        + deliveringPhaseEventHandledSignal
        + ", deliverCompletePhaseEventHandledSignal="
        + deliverCompletePhaseEventHandledSignal
        + ", runningPhaseEventHandledSignal="
        + runningPhaseEventHandledSignal
        + ", runCompletePhaseEventHandledSignal="
        + runCompletePhaseEventHandledSignal + "]";
  }

}
