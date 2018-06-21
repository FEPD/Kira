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

package com.yihaodian.architecture.kira.client.util;

import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import com.yihaodian.architecture.kira.common.HandleResult;

public class JobCancelResult extends HandleResult {

  public static final String RESULT_CODE_SUCCESS = KiraClientConstants.RESULT_CODE_SUCCESS;
  public static final String RESULT_CODE_PARTIAL_SUCCESS = KiraClientConstants.RESULT_CODE_PARTIAL_SUCCESS;
  public static final String RESULT_CODE_FAILED = KiraClientConstants.RESULT_CODE_FAILED;
  private static final long serialVersionUID = 1L;

  public JobCancelResult() {
  }

  /**
   * @param resultCode Can be value of RESULT_CODE_SUCCESS or RESULT_CODE_PARTIAL_SUCCESS or
   * RESULT_CODE_FAILED
   */
  public JobCancelResult(String resultCode, String resultData) {
    super(resultCode, resultData);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
