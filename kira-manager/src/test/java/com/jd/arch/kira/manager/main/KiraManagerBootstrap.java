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
package com.jd.arch.kira.manager.main;

import com.yihaodian.architecture.kira.manager.core.KiraManagerCoreBootstrap;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerDataCenter;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.service.impl.KiraClientMetadataServiceImpl;
import com.yihaodian.architecture.kira.manager.service.impl.TriggerMetadataServiceImpl;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import org.junit.Test;

public class KiraManagerBootstrap {

  @Test
  public void bootstrapStartTest() throws Exception {
    System.out.println("Test Start");

    String hostIpForClusterInternalService = "127.0.0.1";
    KiraServerDataCenter.setHostIpForClusterInternalService(hostIpForClusterInternalService);
    Integer portForClusterInternalService = Integer.valueOf(8080);
    KiraServerDataCenter.setPortForClusterInternalService(portForClusterInternalService);
    String clusterInternalServiceUrl = "http://" + hostIpForClusterInternalService + ":"+ portForClusterInternalService.toString() +"/kira/remote/centralScheduleService";
    KiraServerDataCenter.setClusterInternalServiceUrl(clusterInternalServiceUrl);

    KiraClientMetadataService kiraClientMetadataService = new KiraClientMetadataServiceImpl();
    KiraManagerDataCenter.setKiraClientMetadataService(kiraClientMetadataService);
    TriggerMetadataService triggerMetadataService = new TriggerMetadataServiceImpl();
    KiraManagerDataCenter.setTriggerMetadataService(triggerMetadataService);
    KiraManagerCoreBootstrap kiraManagerBootstrap = new KiraManagerCoreBootstrap();
    kiraManagerBootstrap.start();
    Thread.sleep(20000L);
    kiraManagerBootstrap.shutdown();
    Thread.sleep(10000L);
    kiraManagerBootstrap.destroy();
    System.out.println("Test done");
  }

}
