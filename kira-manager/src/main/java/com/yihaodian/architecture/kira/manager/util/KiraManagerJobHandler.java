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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobCancelContextData;
import com.yihaodian.architecture.kira.common.JobItemRunContextData;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.iface.IJobHandler;
import com.yihaodian.architecture.kira.common.impl.RemoteKiraClientEnvironment;
import com.yihaodian.architecture.kira.common.spi.ICentralScheduleService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerJobHandler implements IJobHandler {

  private static Logger logger = LoggerFactory.getLogger(KiraManagerJobHandler.class);

  public KiraManagerJobHandler() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public HandleResult handleRunJobItem(
      JobItemRunContextData jobItemRunContextData) {
    HandleResult handleResult = null;
    IEnvironment environment = jobItemRunContextData.getEnvironment();
    if (environment instanceof RemoteKiraClientEnvironment) {
      RemoteKiraClientEnvironment remoteKiraClientEnvironment = (RemoteKiraClientEnvironment) environment;
      ICentralScheduleService centralScheduleService = remoteKiraClientEnvironment
          .getCentralScheduleService();
      JobItemRunRequest jobItemRunRequest = jobItemRunContextData.getJobItemRunRequest();
      handleResult = centralScheduleService.runJobItem(jobItemRunRequest);
    } else {
      //will not be here forever on serverside.
      logger.error("environment is not valid. jobItemRunContextData=" + KiraCommonUtils
          .toString(jobItemRunContextData));
      throw new RuntimeException("environment is not valid. environment=" + environment);
    }
    return handleResult;
  }

  @Override
  public HandleResult handleCancelJob(
      JobCancelContextData jobCancelContextData) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionCaught = null;
    try {
      IEnvironment environment = jobCancelContextData.getEnvironment();
      JobCancelRequest jobCancelRequest = jobCancelContextData.getJobCancelRequest();
      if (environment instanceof RemoteKiraClientEnvironment) {
        RemoteKiraClientEnvironment remoteKiraClientEnvironment = (RemoteKiraClientEnvironment) environment;
        ICentralScheduleService centralScheduleService = remoteKiraClientEnvironment
            .getCentralScheduleService();
        if (null != centralScheduleService) {
          HandleResult innerHandleResult = centralScheduleService.cancelJob(jobCancelRequest);
          if (null != innerHandleResult) {
            resultCode = innerHandleResult.getResultCode();
            resultData = innerHandleResult.getResultData();
          } else {
            resultCode = KiraServerConstants.RESULT_CODE_FAILED;
            resultData =
                "innerHandleResult is null for centralScheduleService.cancelJob. jobCancelContextData="
                    + KiraCommonUtils.toString(jobCancelContextData);
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          String serviceUrl = remoteKiraClientEnvironment.getServiceUrl();
          resultData =
              "centralScheduleService is null for centralScheduleService.cancelJob. serviceUrl="
                  + serviceUrl + " and jobCancelContextData=" + KiraCommonUtils
                  .toString(jobCancelContextData);
        }
      }
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs on KiraManagerJobHandler.handleCancelJob. jobCancelContextData="
          + KiraCommonUtils.toString(jobCancelContextData), e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData =
            "Error occurs on KiraManagerJobHandler.handleCancelJob. exceptionDesc=" + exceptionDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

}
