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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorContext implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  private static long DEFAULT_REPEATED_NOTICE_INTERVAL_IN_SECONDS = 1800L;
  protected final ReadWriteLock lockForData = new ReentrantReadWriteLock();
  protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
  private String monitorTarget;
  private String monitorTargetDetail;
  private volatile Date firstRecentBadTime;
  private volatile Date firstRecentGoodTime;
  private volatile long repeatedNoticeIntervalInSeconds = DEFAULT_REPEATED_NOTICE_INTERVAL_IN_SECONDS;
  private volatile Date lastNoticeTimeForBad;

  public MonitorContext(String monitorTarget, String monitorTargetDetail) {
    this(monitorTarget, monitorTargetDetail, DEFAULT_REPEATED_NOTICE_INTERVAL_IN_SECONDS);
  }

  public MonitorContext(String monitorTarget, String monitorTargetDetail,
      long repeatedNoticeIntervalInSeconds) {
    this.monitorTarget = monitorTarget;
    this.monitorTargetDetail = monitorTargetDetail;
    if (repeatedNoticeIntervalInSeconds > 0) {
      this.repeatedNoticeIntervalInSeconds = repeatedNoticeIntervalInSeconds;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  public String getMonitorTarget() {
    return monitorTarget;
  }

  public String getMonitorTargetDetail() {
    return monitorTargetDetail;
  }

  public Date getFirstRecentBadTime() {
    lockForData.readLock().lock();
    try {
      return firstRecentBadTime;
    } finally {
      lockForData.readLock().unlock();
    }
  }

  public Date getFirstRecentGoodTime() {
    lockForData.readLock().lock();
    try {
      return firstRecentGoodTime;
    } finally {
      lockForData.readLock().unlock();
    }
  }

  public long getRepeatedNoticeIntervalInSeconds() {
    lockForData.readLock().lock();
    try {
      return repeatedNoticeIntervalInSeconds;
    } finally {
      lockForData.readLock().unlock();
    }
  }

  public void setRepeatedNoticeIntervalInSeconds(
      long repeatedNoticeIntervalInSeconds) {
    lockForData.writeLock().lock();
    try {
      this.repeatedNoticeIntervalInSeconds = repeatedNoticeIntervalInSeconds;
    } finally {
      lockForData.writeLock().unlock();
    }
  }

  public void resetStatisticsData() {
    lockForData.writeLock().lock();
    try {
      this.firstRecentBadTime = null;
      this.firstRecentGoodTime = null;
      this.lastNoticeTimeForBad = null;
    } finally {
      lockForData.writeLock().unlock();
    }
  }

  /**
   * @return null if no need to notice something.
   */
  public MonitorNoticeInfo updateAndGetMonitorNoticeInfoIfNeeded(boolean isBad,
      String monitorDetails) {
    MonitorNoticeInfo returnValue = null;

    lockForData.writeLock().lock();
    try {
      Date now = new Date();
      if (isBad) {
        boolean needNoticeForBad = false;
        boolean repeatNoticeForBad = false;

        if (null == this.firstRecentBadTime) {
          needNoticeForBad = true;
        } else {
          if (null == this.firstRecentGoodTime) {
            //Never good after boot
            if (null == this.lastNoticeTimeForBad) {
              //Never notice after bad
              needNoticeForBad = true;
            } else {
              boolean isNeedToRepeatNoticeBad = this.isNeedToRepeatNoticeBad(now);
              if (isNeedToRepeatNoticeBad) {
                needNoticeForBad = true;
                repeatNoticeForBad = true;
              }
            }
          } else {
            if (this.firstRecentBadTime.after(this.firstRecentGoodTime)) {
              //bad again
              if (null != this.lastNoticeTimeForBad) {
                boolean isNeedToRepeatNoticeBad = this.isNeedToRepeatNoticeBad(now);
                if (isNeedToRepeatNoticeBad) {
                  needNoticeForBad = true;
                  repeatNoticeForBad = true;
                }
              } else {
                logger.error(
                    "lastNoticeTimeForBad should not be null if bad again. May have some bugs. MonitorContext={}",
                    this);
              }
            } else {
              //First bad after last good
              needNoticeForBad = true;
            }
          }
        }

        if (needNoticeForBad) {
          if (!repeatNoticeForBad) {
            this.firstRecentBadTime = now;
          }
          try {
            MonitorContext clonedMonitorContext = (MonitorContext) this.clone();
            returnValue = new MonitorNoticeInfo(clonedMonitorContext, isBad, false,
                repeatNoticeForBad, monitorDetails);
          } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Not Cloneable.");
          }

          this.lastNoticeTimeForBad = now;
        }
      } else {
        boolean isRecovered = false;

        if (null != this.firstRecentBadTime) {
          if (null == this.firstRecentGoodTime
              || this.firstRecentBadTime.after(this.firstRecentGoodTime)) {
            isRecovered = true;
          }
        }

        if (isRecovered
            || null == firstRecentGoodTime) {
          this.firstRecentGoodTime = now;
        }

        if (isRecovered) {
          try {
            MonitorContext clonedMonitorContext = (MonitorContext) this.clone();
            returnValue = new MonitorNoticeInfo(clonedMonitorContext, isBad, isRecovered, false,
                monitorDetails);

          } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Not Cloneable.");
          }
        }
      }
    } finally {
      lockForData.writeLock().unlock();
    }

    return returnValue;
  }

  private boolean isNeedToRepeatNoticeBad(Date now) {
    //check to see if it is time to need notice bad again
    boolean returnValue = false;
    Date futureTime = new Date(
        this.lastNoticeTimeForBad.getTime() + this.repeatedNoticeIntervalInSeconds * 1000);
    if (now.after(futureTime)) {
      returnValue = true;
    }
    return returnValue;
  }

  @Override
  public String toString() {
    return "MonitorContext [monitorTarget=" + monitorTarget
        + ", monitorTargetDetail=" + monitorTargetDetail
        + ", firstRecentBadTime=" + KiraCommonUtils.getDateAsString(firstRecentBadTime)
        + ", firstRecentGoodTime=" + KiraCommonUtils.getDateAsString(firstRecentGoodTime)
        + ", repeatedNoticeIntervalInSeconds="
        + repeatedNoticeIntervalInSeconds + ", lastNoticeTimeForBad="
        + KiraCommonUtils.getDateAsString(lastNoticeTimeForBad) + "]";
  }

}
