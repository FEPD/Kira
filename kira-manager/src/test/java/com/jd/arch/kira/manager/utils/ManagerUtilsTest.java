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
package com.jd.arch.kira.manager.utils;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.manager.dto.TimeInterval;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;

public class ManagerUtilsTest {

  @Test
  public void splitJobRunTimeData(){
    KiraManagerUtils.setMinutesToKeepJobRuntimeData(60);
    KiraManagerUtils.setMinutesPerTimeToHandleJobRuntimeData(60);

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR_OF_DAY, -10);
    Date dateOfOldestData = new Date(cal.getTimeInMillis());

    cal = Calendar.getInstance();
    cal.add(Calendar.HOUR_OF_DAY, -1);
    Date endTime = new Date(cal.getTimeInMillis());

    Date startTime = null;
    System.out.println("startTime="+ KiraCommonUtils.getDateAsString(startTime));

    System.out.println("endTime="+KiraCommonUtils.getDateAsString(endTime));

    System.out.println("dateOfOldestData="+KiraCommonUtils.getDateAsString(dateOfOldestData));

    List<TimeInterval> splittedTimeIntervalListToHandleJobRuntimeData = KiraManagerUtils.getSplittedTimeIntervalListToHandleJobRuntimeData(startTime, endTime, dateOfOldestData);

    System.out.println("splittedTimeIntervalListToHandleJobRuntimeData="+splittedTimeIntervalListToHandleJobRuntimeData);

    String poolId = "kira";
    ConcurrentHashMap<String,List<String>> poolIdLastSelectedServiceUrlsToRunJobMap = new ConcurrentHashMap<String,List<String>>();
    List<String> secondaryServiceUrlList = new LinkedList<String>();
    secondaryServiceUrlList.add("1");
    poolIdLastSelectedServiceUrlsToRunJobMap.put(poolId, secondaryServiceUrlList);
    KiraManagerDataCenter.setPoolIdLastSelectedServiceUrlsToRunJobMap(poolIdLastSelectedServiceUrlsToRunJobMap);

    List<String> availableServiceUrlList = new LinkedList<String>();
    availableServiceUrlList.add("1");
    availableServiceUrlList.add("2");
    availableServiceUrlList.add("3");
    availableServiceUrlList.add("4");
    availableServiceUrlList.add("5");

    int count=10;
    while(count>0) {
      Set<String> selectedServiceUrlSet = KiraManagerUtils.getRandomSelectedServiceUrlSet(availableServiceUrlList, secondaryServiceUrlList, 2);
      System.out.println(selectedServiceUrlSet);
      KiraManagerDataCenter.setLastSelectedServiceUrlsToRunJobForPool(poolId, new LinkedList<String>(selectedServiceUrlSet));
      secondaryServiceUrlList = KiraManagerDataCenter.getLastSelectedServiceUrlsToRunJobByPoolId(poolId);
      count--;
    }
  }


}
