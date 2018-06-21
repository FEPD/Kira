package com.jd.arch.kira.client.akka;

import com.yihaodian.architecture.kira.client.akka.ProducerMessageActor;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import org.junit.Test;

public class ProducerMessageTest {

  @Test
  public void akkaProducerMessage() {
    ProducerMessageActor.clientSendJobStatus(KiraCommonConstants.QUEUE_KIRA_JOB_ITEM_STATUS, "123");
  }

}
