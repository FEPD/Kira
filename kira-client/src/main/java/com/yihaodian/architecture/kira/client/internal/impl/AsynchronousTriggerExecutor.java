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

import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.client.internal.util.TriggerMethodInvokeTask;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class AsynchronousTriggerExecutor extends AbstractTriggerExecutor {

  public AsynchronousTriggerExecutor() {

  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  @Override
  protected HandleResult executeViaLocalProcessEnvironment(
      JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData, Object targetObject,
      Method methodObject, Object[] arguments) throws Exception {
    ExecutorService executorService = null;
    try {
      String jobId = jobItemRunRequest.getJobId();
      CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(
          KiraClientConstants.KIRA_CLIENT_THREAD_NAME_PREFIX + "TriggerExecutor-" + jobId + "-");
      threadFactory.setDaemon(false);
      executorService = Executors.newSingleThreadExecutor(threadFactory);
      TriggerMethodInvokeTask triggerMethodInvokeTask = new TriggerMethodInvokeTask(
          jobItemRunRequest, triggerRegisterContextData, targetObject, methodObject, arguments);
      executorService.submit(triggerMethodInvokeTask);
    } finally {
      if (null != executorService) {
        executorService.shutdown();
      }
    }

    HandleResult handleResult = new HandleResult(KiraCommonConstants.RESULT_CODE_SUCCESS, null);
    return handleResult;
  }

  @Override
  protected HandleResult executeViaChildProcessEnvironment(
      JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData,
      YHDChildProcessEnvironment childProcessEnvironment) throws Exception {
    //ccjtodo: do it on phase 2
    return null;
  }

}
