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

import com.alibaba.fastjson.JSONArray;
import com.yihaodian.architecture.kira.client.internal.iface.ITriggerExecutor;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.KiraClientDataCenter;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.client.util.JobCanceledException;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import com.yihaodian.architecture.kira.common.iface.IEnvironment;
import com.yihaodian.architecture.kira.common.util.JSONUtil;
import java.lang.reflect.Method;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ReflectionUtils;

public abstract class AbstractTriggerExecutor implements ITriggerExecutor {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public AbstractTriggerExecutor() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  public HandleResult execute(JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionCaught = null;
    try {
      IEnvironment environment = triggerRegisterContextData.getEnvironment();
      HandleResult innerHandleResult = null;
      if (environment instanceof YHDLocalProcessEnvironment) {
        String triggerId = jobItemRunRequest.getTriggerId();
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
                    Class targetClass = methodInvoker.getTargetClass();
                    if (null != targetClass) {
                      String targetMethod = jobItemRunRequest.getTargetMethod();
                      String targetMethodArgTypes = jobItemRunRequest.getTargetMethodArgTypes();
                      JSONArray targetMethodArgTypesJsonArray = JSONArray
                          .parseArray(targetMethodArgTypes);
                      if (null != targetMethodArgTypesJsonArray) {
                        Object[] targetMethodArgObjectArray = targetMethodArgTypesJsonArray
                            .toArray();
                        if (null != targetMethodArgObjectArray) {
                          Class[] argTypes = new Class[targetMethodArgObjectArray.length];
                          for (int i = 0, size = targetMethodArgObjectArray.length; i < size; ++i) {
                            argTypes[i] = Class.forName(targetMethodArgObjectArray[i].toString());
                          }
                          Method methodObject = targetClass.getMethod(targetMethod, argTypes);
                          if (null != methodObject) {
                            Object[] arguments = null;
                            if (argTypes.length > 0) {
                              String argumentsAsJsonArrayString = jobItemRunRequest
                                  .getArgumentsAsJsonArrayString();
                              JSONArray argumentsJSONArray = JSONArray
                                  .parseArray(argumentsAsJsonArrayString);
                              if (null != argumentsJSONArray) {
                                Object[] argumentsJSONObjectArray = argumentsJSONArray.toArray();
                                if (null != argumentsJSONObjectArray) {
                                  if (argumentsJSONObjectArray.length == argTypes.length) {
                                    arguments = new Object[argumentsJSONObjectArray.length];
                                    Object argumentsJSONObject = null;
                                    for (int i = 0, size = argumentsJSONObjectArray.length;
                                        i < size; ++i) {
                                      argumentsJSONObject = argumentsJSONObjectArray[i];
                                      //arguments[i] = JSONObject.parseObject((argumentsJSONObject==null)?null:argumentsJSONObject.toString(), argTypes[i]);
                                      arguments[i] = JSONUtil.parseObject(
                                          (argumentsJSONObject == null) ? null
                                              : argumentsJSONObject.toString(), argTypes[i]);
                                    }
                                  } else {
                                    resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                                    resultData =
                                        "The length of argumentsJSONObjectArray and argTypes is not the same. jobItemRunRequest="
                                            + KiraCommonUtils.toString(jobItemRunRequest);
                                  }
                                } else {
                                  resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                                  resultData =
                                      "argumentsJSONObjectArray is null. jobItemRunRequest="
                                          + KiraCommonUtils.toString(jobItemRunRequest);
                                }
                              } else {
                                resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                                resultData = "argumentsJSONArray is null. jobItemRunRequest="
                                    + KiraCommonUtils.toString(jobItemRunRequest);
                              }
                            } else {
                              arguments = new Object[0];
                            }
                            // In the static case, targetObject will simply be null.
                            Object targetObject = methodInvoker.getTargetObject();
                            ReflectionUtils.makeAccessible(methodObject);
                            innerHandleResult = executeViaLocalProcessEnvironment(jobItemRunRequest,
                                triggerRegisterContextData, targetObject, methodObject, arguments);
                          } else {
                            resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                            resultData =
                                "methodObject is null. targetMethod=" + targetMethod + ",argTypes="
                                    + KiraCommonUtils.toString(argTypes);
                          }
                        } else {
                          resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                          resultData = "targetMethodArgObjectArray is null.targetMethodArgTypes="
                              + targetMethodArgTypes;
                        }
                      } else {
                        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                        resultData =
                            "Can not get targetMethodArgTypesJsonArray by targetMethodArgTypes="
                                + targetMethodArgTypes;
                      }
                    } else {
                      resultCode = KiraClientConstants.RESULT_CODE_FAILED;
                      resultData = "targetClass is null.";
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
                  "triggerBeanObject is not implement JobDetailAwareTrigger. triggerBeanObject.class="
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
        innerHandleResult = executeViaChildProcessEnvironment(jobItemRunRequest,
            triggerRegisterContextData, (YHDChildProcessEnvironment) environment);
      }
      if (null != innerHandleResult) {
        resultCode = innerHandleResult.getResultCode();
        resultData = innerHandleResult.getResultData();
      } else {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        resultData = "innerHandleResult is null for jobItemRunRequest=" + KiraCommonUtils
            .toString(jobItemRunRequest) + " and triggerContextData=" + KiraCommonUtils
            .toString(triggerRegisterContextData);
      }
    } catch (JobCanceledException jobCanceledException) {
      resultCode = KiraClientConstants.RESULT_CODE_FAILED;
      resultData = "The running job is canceled. message=" + jobCanceledException.getMessage();
      logger.error("The running job is canceled.jobItemRunRequest=" + KiraCommonUtils
          .toString(jobItemRunRequest) + " and triggerContextData=" + KiraCommonUtils
          .toString(triggerRegisterContextData), jobCanceledException);
    } catch (Exception e) {
      exceptionCaught = e;
      logger.error("Error occurs on ITriggerExecutor.execute. jobItemRunRequest=" + KiraCommonUtils
          .toString(jobItemRunRequest) + " and triggerContextData=" + KiraCommonUtils
          .toString(triggerRegisterContextData), e);
    } finally {
      if (null != exceptionCaught) {
        resultCode = KiraClientConstants.RESULT_CODE_FAILED;
        String exceptionDesc = ExceptionUtils.getFullStackTrace(exceptionCaught);
        resultData = "Error occurs on ITriggerExecutor.execute. exceptionDesc=" + exceptionDesc;
      }

      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  protected abstract HandleResult executeViaLocalProcessEnvironment(
      JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData, Object targetObject,
      Method methodObject, Object[] arguments) throws Exception;

  protected abstract HandleResult executeViaChildProcessEnvironment(
      JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData,
      YHDChildProcessEnvironment childProcessEnvironment) throws Exception;

}
