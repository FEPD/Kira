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
package com.yihaodian.architecture.kira.manager.core.metadata.kiraclient;

import com.yihaodian.architecture.kira.common.ComponentStateEnum;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.manager.akka.SimpleServer;
import com.yihaodian.architecture.kira.manager.jms.iface.IMessageHandler;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.server.IKiraServer;
import com.yihaodian.architecture.kira.server.event.KiraServerChangedEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerRuntimeDataCheckEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import com.yihaodian.architecture.kira.server.util.KiraServerEventHandleComponent;
import java.util.Date;

public class KiraClientMetadataManager extends KiraServerEventHandleComponent implements
    IKiraClientMetadataManager {

  private KiraClientMetadataService kiraClientMetadataService;

  private IMessageHandler kiraClientRegisterDataMessageHandler;

  private SimpleServer receiverActor;

  public KiraClientMetadataManager(IKiraServer kiraServer,
      KiraClientMetadataService kiraClientMetadataService) throws Exception {
    super(kiraServer);
    this.kiraServer = kiraServer;
    //this.kiraClientRegisterDataMessageHandler = new KiraClientRegisterDataMessageHandler(kiraClientMetadataService);
    this.kiraClientMetadataService = kiraClientMetadataService;
    this.init();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void init() throws Exception {
    logger.info("Initializing KiraClientMetadataManager...");
    long startTime = System.currentTimeMillis();
    try {
      super.init();
      //No more to init for me now.
      logger.info("Successfully initialize KiraClientMetadataManager.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraClientMetadataManager.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraClientMetadataManager. And it takes " + costTime
          + " milliseconds.");
    }
  }

  @Override
  protected void doHandleKiraServerRuntimeDataCheckEventWhenKiraServerStarted(
      KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent)
      throws Exception {
    //do nothing
  }

  @Override
  protected void doHandleKiraServerChangedEventWhenStarted(
      KiraServerChangedEvent kiraServerChangedEvent) throws Exception {
    //do nothing
  }

  @Override
  protected void doHandleKiraServerStartedEvent(
      KiraServerStartedEvent kiraServerStartedEvent) throws Exception {
    logger.info(
        "Begin doHandleKiraServerStartedEvent in " + this.getClass().getSimpleName() + " ...");
    long startTime = System.currentTimeMillis();
    try {

      //SimpleServer receiverActor = new SimpleServer();
      SimpleServer.initServerActor(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS);
      SimpleServer.initServerActor(KiraCommonConstants.QUEUE_KIRA_CLIENT_REGISTER_DATA);

      //MessageReceiverRegister.registerKiraClientRegisterDataReceiver(kiraClientRegisterDataMessageHandler, true);
      this.componentState.set(ComponentStateEnum.STARTED.getState());
      this.lastStartedTime = new Date();
      logger.info(
          "Successfully doHandleKiraServerStartedEvent in " + this.getClass().getSimpleName());
    } finally {
      KiraManagerDataCenter.setKiraClientMetadataManager(this);
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish doHandleKiraServerStartedEvent " + this.getClass().getSimpleName()
          + ". And it takes " + costTime + " milliseconds.");
    }
  }

  @Override
  protected void doShutdown() {
    logger.info("Begin doShutdown in " + this.getClass().getSimpleName() + " ...");
    long startTime = System.currentTimeMillis();
    try {
      //MessageReceiverRegister.unRegisterKiraClientRegisterDataReceiver();
      logger.info("Successfully doShutdown in " + this.getClass().getSimpleName());
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish doShutdown " + this.getClass().getSimpleName() + ". And it takes " + costTime
              + " milliseconds.");
    }
  }

}
