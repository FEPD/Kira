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
package com.yihaodian.architecture.kira.manager.jms.impl;

public class JobItemStatusReportMessageHandler {// implements IMessageHandler {
/*	private static Logger logger = LoggerFactory.getLogger(JobItemStatusReportMessageHandler.class);
	private JobItemService jobItemService;
	private AlarmCenter alarmCenter;
	
	public void setJobItemService(JobItemService jobItemService) {
		this.jobItemService = jobItemService;
	}

	public void setAlarmCenter(AlarmCenter alarmCenter) {
		this.alarmCenter = alarmCenter;
	}

	public JobItemStatusReportMessageHandler() {
	}

	@Override
	public void doHandle(Message message) {
		JobItemStatusReport jobItemStatusReport = message.transferContentToBean(JobItemStatusReport.class);
		if(logger.isDebugEnabled()) {
			logger.debug("jobItemStatusReport={}",jobItemStatusReport);
		}
		*//*if(null!=jobItemStatusReport) {
			//change to use the server side time instead of client side time.
			jobItemStatusReport.setCreateTime(new Date());
			String jobItemId = jobItemStatusReport.getJobItemId();
			Integer jobStatusId = jobItemStatusReport.getJobStatusId();
			String resultData = jobItemStatusReport.getResultData();
			jobItemService.updateJobItemStatus(jobItemId, jobStatusId, resultData, KiraCommonUtils.getCanBeUpdatedJobStatusIdList(jobStatusId), true, true);
			alarmCenter.alarmForJobItemIfNeeded(jobItemId, jobStatusId);
		}*//*
	}*/

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
