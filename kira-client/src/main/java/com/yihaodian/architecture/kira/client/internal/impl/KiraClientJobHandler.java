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
package com.yihaodian.architecture.kira.client.internal.impl;

import com.yihaodian.architecture.kira.client.iface.IJobCancelable;
import com.yihaodian.architecture.kira.client.internal.iface.ITriggerExecutor;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.RegisterStatusEnum;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.client.util.JobCancelResult;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.JobCancelContextData;
import com.yihaodian.architecture.kira.common.JobItemRunContextData;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.iface.IJobHandler;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;
import org.springframework.util.MethodInvoker;

public class KiraClientJobHandler implements IJobHandler {

  private static Logger logger = LoggerFactory.getLogger(KiraClientJobHandler.class);

  public KiraClientJobHandler() {
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
    HandleResult outerHandleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionCaught = null;
    try {
      JobItemRunRequest jobItemRunRequest = jobItemRunContextData.getJobItemRunRequest();
      Boolean asynchronous = jobItemRunRequest.getAsynchronous();
      String appId = jobItemRunRequest.getAppId();
      String triggerId = jobItemRunRequest.getTriggerId();
      TriggerIdentity triggerIdentity = new TriggerIdentity(appId, triggerId);
      ConcurrentMap<IEnvironment, TriggerRegisterContextData> environmentTriggerContextDataMap = KiraClientDataCenter
          .getEnvironmentTriggerContextDataMap(triggerIdentity);
      if (null != environmentTriggerContextDataMap) {
        IEnvironment environment = jobItemRunContextData.getEnvironment();
        TriggerRegisterContextData triggerRegisterContextData = environmentTriggerContextDataMap
            .get(environment);
        if (null != triggerRegisterContextData) {
          if (RegisterStatusEnum.REGISTER_SUCCESS
              .equals(triggerRegisterContextData.getRegisterStatus())) {
            ITriggerExecutor triggerExecutor = null;
            if (asynchronous) {
              triggerExecutor = new AsynchronousTriggerExecutor();
            } else {
              triggerExecutor = new SynchronousTriggerExecutor();
            }
            HandleResult handleResult = triggerExecutor
                .execute(jobItemRunRequest, triggerRegisterContextData);
            if (null != handleResult) {
              resultCode = handleResult.getResultCode();
              resultData = handleResult.getResultData();
            } else {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
              resultData =
                  "handleResult is null for triggerExecutor.execute. jobItemRunContextData="
                      + KiraCommonUtils.toString(jobItemRunContextData);
            }
          } else {
            resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            resultData =
                "Register status for trigger is not success.triggerContextData=" + KiraCommonUtils
                    .toString(triggerRegisterContextData) + " and jobItemRunContextData="
                    + KiraCommonUtils.toString(jobItemRunContextData);
          }
        } else {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          resultData =
              "triggerContextData is null on KiraClientJobHandler.handleRunJobItem. jobItemRunContextData={}"
                  + KiraCommonUtils.toString(jobItemRunContextData);
        }
      } else {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        resultData =
            "environmentTriggerContextDataMap is null on KiraClientJobHandler.handleRunJobItem. jobItemRunContextData="
                + KiraCommonUtils.toString(jobItemRunContextData);
      }
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs on KiraClientJobHandler.handleRunJobItem. jobItemRunContextData="
          + KiraCommonUtils.toString(jobItemRunContextData), e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData =
            "Error occurs on KiraClientJobHandler.handleRunJobItem. exceptionDesc=" + exceptionDesc;
      }

      outerHandleResult = new HandleResult(resultCode, resultData);
    }

    return outerHandleResult;
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
      if (environment instanceof YHDLocalProcessEnvironment) {
        String triggerId = jobCancelRequest.getTriggerId();
        ApplicationContext applicationContext = KiraClientDataCenter.getApplicationContext();
        if (null != applicationContext) {
          Object triggerBeanObject = applicationContext.getBean(triggerId);
          if (null != triggerBeanObject) {
            if (triggerBeanObject instanceof JobDetailAwareTrigger) {
              JobDetail jobDetail = ((JobDetailAwareTrigger) triggerBeanObject).getJobDetail();
              if (null != jobDetail) {
                Object methodInvokerObj = jobDetail.getJobDataMap()
                    .get(KiraClientConstants.JOBDATAMAP_KEY_METHODINVOKER);
                if (null != methodInvokerObj) {
                  if (methodInvokerObj instanceof MethodInvoker) {
                    MethodInvoker methodInvoker = (MethodInvoker) methodInvokerObj;
                    Class<?> targetClass = methodInvoker.getTargetClass();
                    Object targetObject = methodInvoker.getTargetObject();
                    if (null != targetObject) {
                      if (targetObject instanceof IJobCancelable) {
                        IJobCancelable jobCancelableTargetObject = (IJobCancelable) targetObject;
                        JobCancelResult jobCancelResult = jobCancelableTargetObject
                            .cancelJob(jobCancelRequest.getMethodParamDataMap());
                        if (null != jobCancelResult) {
                          resultCode = jobCancelResult.getResultCode();
                          resultData = jobCancelResult.getResultData();
                        } else {
                          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                          resultData =
                              "jobCancelResult is null. jobCancelContextData=" + KiraCommonUtils
                                  .toString(jobCancelContextData);
                        }
                      } else {
                        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                        resultData =
                            "The triggerBeanObject must implements IJobCancelable. targetObject's class="
                                + targetObject.getClass() + " and jobCancelContextData="
                                + KiraCommonUtils.toString(jobCancelContextData);
                      }
                    } else {
                      resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                      resultData = "targetObject is null. targetClass=" + targetClass;
                    }
                  } else {
                    resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                    resultData =
                        "methodInvokerObj should be of type MethodInvoker. methodInvokerObj's class="
                            + methodInvokerObj.getClass();
                  }
                } else {
                  resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                  resultData = "methodInvokerObj is null.";
                }
              } else {
                resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                resultData = "jobDetail is null.";
              }
            } else {
              resultCode = KiraClientConstants.RESULT_CODE_FAILED;
              resultData =
                  "triggerBeanObject is not implement JobDetailAwareTrigger. triggerBeanObject's class="
                      + triggerBeanObject.getClass();
            }
          } else {
            resultCode = KiraClientConstants.RESULT_CODE_FAILED;
            resultData = "triggerBeanObject is null. triggerId=" + triggerId;
          }
        } else {
          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
          resultData = "applicationContext is null.";
        }

      } else if (environment instanceof YHDChildProcessEnvironment) {
        //ccjtodo: in phase2
      }
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs on KiraClientJobHandler.handleCancelJob. jobCancelContextData="
          + KiraCommonUtils.toString(jobCancelContextData), e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData =
            "Error occurs on KiraClientJobHandler.handleCancelJob. exceptionDesc=" + exceptionDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

}
