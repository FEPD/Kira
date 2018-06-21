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
package com.yihaodian.architecture.kira.client.internal.util;

import com.yihaodian.architecture.kira.client.internal.bean.Job;
import com.yihaodian.architecture.kira.client.util.JobCanceledException;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerMethodInvokeTask implements Callable<HandleResult> {

  private static Logger logger = LoggerFactory.getLogger(TriggerMethodInvokeTask.class);

  private JobItemRunRequest jobItemRunRequest;
  private TriggerRegisterContextData triggerRegisterContextData;
  private Object targetObject;
  private Method methodObject;
  private Object[] arguments;

  public TriggerMethodInvokeTask() {
    // TODO Auto-generated constructor stub
  }

  public TriggerMethodInvokeTask(JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData, Object targetObject,
      Method methodObject, Object[] arguments) {
    super();
    this.jobItemRunRequest = jobItemRunRequest;
    this.triggerRegisterContextData = triggerRegisterContextData;
    this.targetObject = targetObject;
    this.methodObject = methodObject;
    this.arguments = arguments;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public JobItemRunRequest getJobItemRunRequest() {
    return jobItemRunRequest;
  }

  public void setJobItemRunRequest(JobItemRunRequest jobItemRunRequest) {
    this.jobItemRunRequest = jobItemRunRequest;
  }

  public TriggerRegisterContextData getTriggerRegisterContextData() {
    return triggerRegisterContextData;
  }

  public void setTriggerRegisterContextData(TriggerRegisterContextData triggerRegisterContextData) {
    this.triggerRegisterContextData = triggerRegisterContextData;
  }

  public Object getTargetObject() {
    return targetObject;
  }

  public void setTargetObject(Object targetObject) {
    this.targetObject = targetObject;
  }

  public Method getMethodObject() {
    return methodObject;
  }

  public void setMethodObject(Method methodObject) {
    this.methodObject = methodObject;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public void setArguments(Object[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public HandleResult call() throws Exception {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionCaught = null;
    Boolean asynchronous = jobItemRunRequest.getAsynchronous();
    String jobId = jobItemRunRequest.getJobId();
    String jobItemId = jobItemRunRequest.getJobItemId();
    Job job = KiraClientDataCenter.getJobIdJobMap().get(jobId);
    try {
      HandleResult methodInvokeResult = KiraClientUtils
          .invokeMethod(jobItemRunRequest, targetObject, methodObject, arguments);
      if (null != methodInvokeResult) {
        resultCode = methodInvokeResult.getResultCode();
        resultData = methodInvokeResult.getResultData();
      } else {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        resultData = "methodInvokeResult is null.";
      }
    } catch (JobCanceledException jobCanceledException) {
      resultCode = KiraClientConstants.RESULT_CODE_FAILED;
      resultData = "The running job is canceled. message=" + jobCanceledException.getMessage();
      logger.error("The running job is canceled.jobItemRunRequest=" + KiraCommonUtils
          .toString(jobItemRunRequest) + " and triggerContextData=" + KiraCommonUtils
          .toString(triggerRegisterContextData), jobCanceledException);
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs on MethodInvokeTask.", e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData = "Error occurs on MethodInvokeTask. exceptionDesc=" + exceptionDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
      if (asynchronous && !KiraClientConstants.RESULT_CODE_NO_NEED_TO_RUN_BUSINESS_METHOD
          .equals(resultCode)) {
        KiraClientUtils.updateJobItem(KiraClientConstants.JOB_PHASE_RUN_COMPLETE, job, jobItemId,
            handleResult);
      }
    }

    return handleResult;
  }
}
