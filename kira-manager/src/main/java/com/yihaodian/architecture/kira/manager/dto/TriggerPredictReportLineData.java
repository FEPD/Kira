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
package com.yihaodian.architecture.kira.manager.dto;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;

public class TriggerPredictReportLineData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String poolId;
  private String triggerId;
  private String description;
  private Date fromFutureTime;
  private Date toFutureTime;
  private Date firstTriggeredTimeInTheFuture;
  private Integer maxHistoryRuntimeInSeconds;
  private Integer minHistoryRuntimeInSeconds;
  private Integer avgHistoryRuntimeInSeconds;

  public TriggerPredictReportLineData(String poolId, String triggerId, String description,
      Date fromFutureTime, Date toFutureTime, Date firstTriggeredTimeInTheFuture,
      Integer maxHistoryRuntimeInSeconds, Integer minHistoryRuntimeInSeconds,
      Integer avgHistoryRuntimeInSeconds) {
    this.poolId = poolId;
    this.triggerId = triggerId;
    this.description = description;
    this.fromFutureTime = fromFutureTime;
    this.toFutureTime = toFutureTime;
    this.firstTriggeredTimeInTheFuture = firstTriggeredTimeInTheFuture;
    this.maxHistoryRuntimeInSeconds = maxHistoryRuntimeInSeconds;
    this.minHistoryRuntimeInSeconds = minHistoryRuntimeInSeconds;
    this.avgHistoryRuntimeInSeconds = avgHistoryRuntimeInSeconds;
  }

  public String getPoolId() {
    return poolId;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public String getDescription() {
    return description;
  }

  public Date getFromFutureTime() {
    return fromFutureTime;
  }

  public Date getToFutureTime() {
    return toFutureTime;
  }

  public Date getFirstTriggeredTimeInTheFuture() {
    return firstTriggeredTimeInTheFuture;
  }

  public Integer getMaxHistoryRuntimeInSeconds() {
    return maxHistoryRuntimeInSeconds;
  }

  public Integer getMinHistoryRuntimeInSeconds() {
    return minHistoryRuntimeInSeconds;
  }

  public Integer getAvgHistoryRuntimeInSeconds() {
    return avgHistoryRuntimeInSeconds;
  }

  @Override
  public String toString() {
    return "TriggerPredictReportLineData{" +
        "poolId='" + poolId + '\'' +
        ", triggerId='" + triggerId + '\'' +
        ", description='" + description + '\'' +
        ", fromFutureTime=" + KiraCommonUtils.getDateAsString(fromFutureTime) +
        ", toFutureTime=" + KiraCommonUtils.getDateAsString(toFutureTime) +
        ", firstTriggeredTimeInTheFuture=" + KiraCommonUtils
        .getDateAsString(firstTriggeredTimeInTheFuture) +
        ", maxHistoryRuntimeInSeconds=" + maxHistoryRuntimeInSeconds +
        ", minHistoryRuntimeInSeconds=" + minHistoryRuntimeInSeconds +
        ", avgHistoryRuntimeInSeconds=" + avgHistoryRuntimeInSeconds +
        '}';
  }
}
