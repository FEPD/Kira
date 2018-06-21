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
package com.yihaodian.architecture.kira.schedule.time;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.schedule.time.callback.ITimeScheduleCallback;
import com.yihaodian.architecture.kira.schedule.time.callback.KiraTimeScheduleCallbackAdaptor;
import com.yihaodian.architecture.kira.schedule.time.internal.service.ITimerTriggerToBeFiredHandleService;
import com.yihaodian.architecture.kira.schedule.time.internal.service.KiraTimerTriggerToBeFiredHandleService;
import com.yihaodian.architecture.kira.schedule.time.internal.service.helper.KiraTimerTriggerToBeFiredHandleTask;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.ITimer;
import com.yihaodian.architecture.kira.schedule.time.internal.timer.KiraTimer;
import com.yihaodian.architecture.kira.schedule.time.trigger.ITimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraCronTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.KiraSimpleTimerTrigger;
import com.yihaodian.architecture.kira.schedule.time.trigger.TimerTriggerComparator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.junit.Test;

public class KiraTimerTest {

  @Test
  public void TimerTest() throws Exception {

    //Do not work with ConcurrentSkipListSet and TreeSet which strictly require to obey the rules described in Comparable. But i just want to use the id to identify the trigger. In this case no way to obey that rule perfectly.
    //test below verify it
    //begin verity
    Set<ITimerTrigger> mySet0 = new ConcurrentSkipListSet<ITimerTrigger>(
        new TimerTriggerComparator());
    Set<ITimerTrigger> mySet1 = new ConcurrentSkipListSet<ITimerTrigger>();
    Set<ITimerTrigger> mySet2 = new TreeSet<ITimerTrigger>();
    Set<ITimerTrigger> mySet3 = new HashSet<ITimerTrigger>();
    for (int i = 0; i < 10; i++) {
      KiraSimpleTimerTrigger kiraSimpleTimerTrigger = new KiraSimpleTimerTrigger(
          "mySimpleTimerTriggerId" + i, "myPoolId", null,
          KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER, null, null, null, null, 0L, 2,
          2 * 1000L, null, 0, false);
      KiraSimpleTimerTrigger kiraSimpleTimerTrigger2 = new KiraSimpleTimerTrigger(
          "mySimpleTimerTriggerId", "myPoolId", null,
          KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER + i + 1, null, null, null, null, 0L,
          2, 2 * 1000L, null, 0, false);
      System.out.println(kiraSimpleTimerTrigger.equals(kiraSimpleTimerTrigger2));
      mySet0.add(kiraSimpleTimerTrigger);
      mySet1.add(kiraSimpleTimerTrigger);
      mySet3.add(kiraSimpleTimerTrigger);
      mySet2.add(kiraSimpleTimerTrigger);
    }
    KiraSimpleTimerTrigger kiraSimpleTimerTrigger3 = new KiraSimpleTimerTrigger(
        "mySimpleTimerTriggerId", "myPoolId", null,
        KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER, null, null, null, null, 0, 2,
        2 * 1000L, null, 0, false);
    boolean removeResult0 = mySet0.remove(kiraSimpleTimerTrigger3);
    boolean removeResult1 = mySet1.remove(kiraSimpleTimerTrigger3);
    boolean removeResult2 = mySet2.remove(kiraSimpleTimerTrigger3);
    boolean removeResult3 = mySet3.remove(kiraSimpleTimerTrigger3);

    System.out.println("mySet0's removeResult=" + removeResult0);
    System.out.println("mySet1's removeResult=" + removeResult1);
    System.out.println("mySet2's removeResult=" + removeResult2);
    System.out.println("mySet3's removeResult=" + removeResult3);

    System.out.println("mySet0.size()=" + mySet0.size());
    System.out.println("mySet1.size()=" + mySet1.size());
    System.out.println("mySet2.size()=" + mySet2.size());
    System.out.println("mySet3.size()=" + mySet3.size());
    //end verity

    // 0 0/1 * ? * *
    int triggerCount = 10;
    String timerTriggerSchedulerId = "UnknownTimerTriggerSchedulerId";
    ITimerTriggerToBeFiredHandleService timerTriggerToBeFiredHandleService = new KiraTimerTriggerToBeFiredHandleService(
        timerTriggerSchedulerId, triggerCount) {

      @Override
      public void handleTimerTriggerToBeFired(ITimer timer, ITimerTrigger timerTrigger)
          throws Exception {
        System.out.println(KiraCommonUtils.getDateAsString(new Date())
            + " handleTimerTriggerToBeFired for timerTrigger=" + timerTrigger);
        KiraTimerTriggerToBeFiredHandleTask kiraTimerTriggerToBeFiredHandleTask = new KiraTimerTriggerToBeFiredHandleTask(
            timer, timerTrigger);
        threadPoolExecutor.submit(kiraTimerTriggerToBeFiredHandleTask);
      }

    };
    timerTriggerToBeFiredHandleService.start();
    KiraTimer kiraTimer = new KiraTimer(timerTriggerSchedulerId, 0,
        timerTriggerToBeFiredHandleService);
    kiraTimer.start();
    List<ITimerTrigger> timerTriggerList = new ArrayList<ITimerTrigger>();
    for (int i = 0; i < triggerCount; i++) {
      ITimeScheduleCallback timeScheduleCallback = new KiraTimeScheduleCallbackAdaptor() {
        @Override
        public void submitTask(ITimerTrigger timerTrigger) throws Exception {
          //System.out.println(KiraCommonUtils.getDateAsString(new Date()) + " submitTask success for timerTrigger="+timerTrigger);
        }
      };

      KiraCronTimerTrigger kiraCronTimerTrigger = new KiraCronTimerTrigger(
          "myCronTimerTriggerId" + i, "myPoolId", timeScheduleCallback,
          KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER + i, new Date(), null, null, null,
          0, "0/2 * * ? * *", null, 0, false);
      timerTriggerList.add(kiraCronTimerTrigger);
      KiraSimpleTimerTrigger kiraSimpleTimerTrigger = new KiraSimpleTimerTrigger(
          "mySimpleTimerTriggerId" + i, "myPoolId", timeScheduleCallback,
          KiraCommonConstants.DEFAULT_PRIORITY_OF_TIMER_TRIGGER + i, null, null, null, null, 0, 2,
          2 * 1000L, null, 0, false);
      timerTriggerList.add(kiraSimpleTimerTrigger);
    }

    for (ITimerTrigger timerTrigger : timerTriggerList) {
      kiraTimer.addTimerTrigger(timerTrigger);
    }
    Thread.sleep(10000L);
//		for(ITimerTrigger timerTrigger : timerTriggerList) {
//			kiraTimer.removeTimerTrigger(timerTrigger,true);
//		}
//		Thread.sleep(10000L);
    kiraTimer.shutdown();
    timerTriggerToBeFiredHandleService.shutdown();
  }
}
