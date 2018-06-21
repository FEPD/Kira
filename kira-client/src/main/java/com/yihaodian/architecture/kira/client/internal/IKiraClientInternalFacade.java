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
package com.yihaodian.architecture.kira.client.internal;

import com.yihaodian.architecture.kira.client.quartz.YHDSchedulerFactoryBean;
import com.yihaodian.architecture.kira.client.util.KiraClientConfig;
import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import org.quartz.Trigger;

public interface IKiraClientInternalFacade extends ILifecycle {

  public Trigger[] handleAndGetLocallyRunTriggers(YHDSchedulerFactoryBean yhdSchedulerFactoryBean,
      Trigger[] triggers, boolean workWithoutKira);

  public void handleForKiraClientConfig(KiraClientConfig kiraClientConfig);

  public void handleOthers() throws Exception;
}
