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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import com.yihaodian.architecture.kira.manager.service.KiraClientService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class KiraClientServiceImpl extends Service implements KiraClientService {

  public KiraClientServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<String, String> queryKiraClientInfoAsMap(
      String centralScheduleServiceUrl) {
    Map<String, String> returnValue = new LinkedHashMap<String, String>();
    try {
      if (StringUtils.isNotBlank(centralScheduleServiceUrl)) {
        ICentralScheduleService centralScheduleService = KiraManagerUtils
            .getCentralScheduleService(centralScheduleServiceUrl);
        if (null != centralScheduleService) {
          returnValue = centralScheduleService.queryKiraClientInfoAsMap();
        } else {
          logger.warn("centralScheduleService is null for centralScheduleServiceUrl="
              + centralScheduleServiceUrl);
        }
      } else {
        logger.warn("centralScheduleServiceUrl should not be blank.");
      }
    } catch (Exception e) {
      logger.error("Error occurs for queryKiraClientInfoAsMap by centralScheduleServiceUrl="
          + centralScheduleServiceUrl, e);
    }
    return returnValue;
  }
}
