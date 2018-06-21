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
package com.yihaodian.architecture.kira.common.spi;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.dto.JobCancelRequest;
import com.yihaodian.architecture.kira.common.dto.JobItemRunRequest;
import java.util.Map;

public interface ICentralScheduleService {

  public HandleResult runJobItem(JobItemRunRequest jobItemRunRequest);

  public HandleResult cancelJob(JobCancelRequest jobCancelRequest);

  public Map<String, String> queryKiraClientInfoAsMap();
}
