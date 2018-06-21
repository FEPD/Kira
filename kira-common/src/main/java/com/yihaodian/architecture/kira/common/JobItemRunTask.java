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
package com.yihaodian.architecture.kira.common;

import com.yihaodian.architecture.kira.common.iface.IJobHandler;
import java.util.concurrent.Callable;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobItemRunTask implements Callable<HandleResult> {

  private static Logger logger = LoggerFactory.getLogger(JobItemRunTask.class);

  private JobItemRunContextData jobItemRunContextData;
  private IJobHandler jobHandler;

  public JobItemRunTask() {
  }

  public JobItemRunTask(JobItemRunContextData jobItemRunContextData, IJobHandler jobHandler) {
    super();
    this.jobItemRunContextData = jobItemRunContextData;
    this.jobHandler = jobHandler;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public JobItemRunContextData getJobItemRunContextData() {
    return jobItemRunContextData;
  }

  public void setJobItemRunContextData(JobItemRunContextData jobItemRunContextData) {
    this.jobItemRunContextData = jobItemRunContextData;
  }

  public IJobHandler getJobHandler() {
    return jobHandler;
  }

  public void setJobHandler(IJobHandler jobHandler) {
    this.jobHandler = jobHandler;
  }

  @Override
  public HandleResult call() throws Exception {
    String resultCode = null;
    String resultData = null;
    HandleResult handleResult = null;
    Throwable exceptionCaught = null;
    try {
      handleResult = jobHandler.handleRunJobItem(jobItemRunContextData);
      if (null != handleResult) {
        resultCode = handleResult.getResultCode();
        resultData = handleResult.getResultData();
      } else {
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
        resultData = "handleResult is null for jobHandler.handleRunJobItem. jobItemRunContextData="
            + KiraCommonUtils.toString(jobItemRunContextData);
      }
    } catch (Throwable t) {
      exceptionCaught = t;
      String errorMessage =
          "Exception occurs for running JobItemRunTask. jobItemRunContextData=" + KiraCommonUtils
              .toString(jobItemRunContextData);
      if (null != logger) {
        logger.error(errorMessage, exceptionCaught);
      } else {
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        //Just want to see the log on rare occasions.
        System.out.println(errorMessage + "and exceptionDesc=" + exceptionDesc);
      }
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraCommonConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        if (null == resultData) {
          resultData = "";
        }
        resultData += "  Error occurs for running JobItemRunTask. exceptionDesc=" + exceptionDesc;
      }

      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

}
