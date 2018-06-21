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
package com.yihaodian.architecture.kira.manager.health.monitor.task;

import com.yihaodian.architecture.kira.common.SystemUtil;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.manager.health.event.DBForMenuRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.DBForMenuUnavailableEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEventType;
import com.yihaodian.architecture.kira.manager.health.util.KiraManagerHealthUtils;
import com.yihaodian.architecture.kira.manager.util.KiraManagerConstants;
import com.yihaodian.architecture.kira.manager.util.SpringBeanUtils;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.lang.exception.ExceptionUtils;

public class DBForMenuMonitorTask extends KiraManagerMonitorTaskComponent implements Runnable {

  public DBForMenuMonitorTask() {
    this.monitorContext = new MonitorContext("DB for Menu", "");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  public void run() {
    DataSource dataSource_hdc = (DataSource) SpringBeanUtils
        .getBean(KiraManagerConstants.SPRING_BEAN_NAME_DATASOURCE_KIRA);
    if (null != dataSource_hdc) {
      boolean result = true;
      String monitorDetails = null;
      try {
        Connection connection = null;
        try {
          connection = dataSource_hdc.getConnection();
//					if(null!=connection) {
//						boolean isConnectionValid = connection.isValid(KiraManagerConstants.DB_CONNECTION_SOCKETTIMEOUT_IN_SECOND);
//						if(!isConnectionValid) {
//							result = false;
//							monitorDetails = "Connection is not valid.";
//						}
//					}
        } finally {
          if (null == connection) {
            result = false;
            monitorDetails = "Can not get connection.";
          } else {
            try {
              connection.close();
            } catch (Throwable t) {
              logger.error(
                  "Error occurs when connection.close() in DBForMenuMonitorTask. Just ignore it here.",
                  t);
            }
          }
        }
      } catch (Throwable t) {
        result = false;
        monitorDetails = "exceptionDetails=" + ExceptionUtils.getFullStackTrace(t);
        //monitorDetails = t.getMessage();
        logger.error("Error occurs when runing DBForMenuMonitorTask.", t);
      } finally {
        MonitorNoticeInfo monitorNoticeInfo = this.monitorContext
            .updateAndGetMonitorNoticeInfoIfNeeded(!result, monitorDetails);
        if (null != monitorNoticeInfo) {
          if (monitorNoticeInfo.isBad()) {
            DBForMenuUnavailableEvent dbForMenuUnavailableEvent = new DBForMenuUnavailableEvent(
                KiraManagerHealthEventType.DB_FOR_MENU_UNAVAILABLE, SystemUtil.getLocalhostIp(),
                monitorNoticeInfo);
            KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(dbForMenuUnavailableEvent);
          } else {
            DBForMenuRecoveredEvent dbForMenuRecoveredEvent = new DBForMenuRecoveredEvent(
                KiraManagerHealthEventType.DB_FOR_MENU_RECOVERED, SystemUtil.getLocalhostIp(),
                monitorNoticeInfo);
            KiraManagerHealthUtils.dispatchKiraManagerHealthEvent(dbForMenuRecoveredEvent);
          }
        }
      }
    }
  }

}
