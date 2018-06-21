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
package com.yihaodian.architecture.kira.client.quartz;

import com.yihaodian.architecture.kira.client.internal.util.KiraClientConstants;
import org.quartz.JobDetail;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

public class YHDMethodInvokingJobDetailFactoryBean extends
    MethodInvokingJobDetailFactoryBean {

  public YHDMethodInvokingJobDetailFactoryBean() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void postProcessJobDetail(JobDetail jobDetail) {
    super.postProcessJobDetail(jobDetail);

    jobDetail.getJobDataMap()
        .put(KiraClientConstants.JOBDATAMAP_KEY_TARGETMETHOD, this.getTargetMethod());
    Object[] arguments = getArguments();
    String[] argTypes = new String[arguments.length];
    for (int i = 0; i < arguments.length; ++i) {
      argTypes[i] = (arguments[i] != null ? arguments[i].getClass().getName()
          : Object.class.getName());
    }
    jobDetail.getJobDataMap()
        .put(KiraClientConstants.JOBDATAMAP_KEY_TARGETMETHODARGTYPES, argTypes);
    jobDetail.getJobDataMap().put(KiraClientConstants.JOBDATAMAP_KEY_ARGUMENTS, arguments);
  }

}
