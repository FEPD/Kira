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
package com.yihaodian.architecture.kira.manager.akka;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.akka.entrypoint.WorkerActor;
import com.yihaodian.architecture.kira.common.dto.JobItemStatusReport;
import java.util.Date;

/**
 * Created by zhoufeiqiang on 14/09/2017.
 */
public class JobStatusWorkerActor extends WorkerActor {

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof JobItemStatusReport) {
      JobItemStatusReport jobItemStatusReport = (JobItemStatusReport) message;

      if (null != jobItemStatusReport) {
        //change to use the server side time instead of client side time.
        jobItemStatusReport.setCreateTime(new Date());
        String jobItemId = jobItemStatusReport.getJobItemId();
        Integer jobStatusId = jobItemStatusReport.getJobStatusId();
        String resultData = jobItemStatusReport.getResultData();
        BaseActor.jobItemService.updateJobItemStatus(jobItemId, jobStatusId, resultData,
            KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusId), true, true);
        BaseActor.alarmCenter.alarmForJobItemIfNeeded(jobItemId, jobStatusId);
      }
      getSender()
          .tell("job status " + jobItemStatusReport.getJobItemId() + " receive ok!", getSelf());
    }
  }
}
