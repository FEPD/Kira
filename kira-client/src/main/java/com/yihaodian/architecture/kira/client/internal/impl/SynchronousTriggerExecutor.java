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

import com.yihaodian.architecture.kira.client.internal.util.KiraClientUtils;
import com.yihaodian.architecture.kira.client.internal.util.TriggerRegisterContextData;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import java.lang.reflect.Method;

public class SynchronousTriggerExecutor extends AbstractTriggerExecutor {

  public SynchronousTriggerExecutor() {
    // TODO Auto-generated constructor stub
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
    HandleResult handleResult = KiraClientUtils
        .invokeMethod(jobItemRunRequest, targetObject, methodObject, arguments);
    return handleResult;
  }

  @Override
  protected HandleResult executeViaChildProcessEnvironment(
      JobItemRunRequest jobItemRunRequest,
      TriggerRegisterContextData triggerRegisterContextData,
      YHDChildProcessEnvironment childProcessEnvironment) throws Exception {
    // ccjtodo: do it on phase 2
    return null;
  }

}
