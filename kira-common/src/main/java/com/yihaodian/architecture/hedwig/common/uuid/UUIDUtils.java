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
package com.yihaodian.architecture.hedwig.common.uuid;

import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import java.util.concurrent.atomic.AtomicLong;

public class UUIDUtils {

  private static final long SEQUENCE_MIN_INCLUDE = 0L;

  private static final long SEQUENCE_MAX_EXCLUDE = 100000L;

  private static final AtomicLong sequence = new AtomicLong(SEQUENCE_MIN_INCLUDE);

  private static final String SEPARATOR = "_";

  public UUIDUtils() {
    // TODO Auto-generated constructor stub
  }

  public static String getUUID() {
    String returnValue = null;
    StringBuilder sb = new StringBuilder();

    String localHostIP = SystemUtil.getLocalhostIp();
    sb.append(localHostIP);
    sb.append(UUIDUtils.SEPARATOR);

    long currentTimeMillis = System.currentTimeMillis();
    sb.append(currentTimeMillis);
    sb.append(UUIDUtils.SEPARATOR);

    long nextSequence = getNextSequence(1L);
    sb.append(nextSequence);

    returnValue = sb.toString();

    return returnValue;
  }

  private static long getNextSequence(long currentTryCount) {
    if (sequence.get() >= UUIDUtils.SEQUENCE_MAX_EXCLUDE) {
      sequence.set(UUIDUtils.SEQUENCE_MIN_INCLUDE);
    }

    long returnValue = sequence.getAndIncrement();

    if (returnValue >= UUIDUtils.SEQUENCE_MAX_EXCLUDE) {
      // System.out.println("Need to calculate again. currentTryCount="+
      // currentTryCount + " and returnValue="+returnValue);
      returnValue = UUIDUtils.getNextSequence(++currentTryCount);
    }

    // if(currentTryCount>=2) {
    // System.out.println("currentTryCount="+currentTryCount);
    // }

    return returnValue;
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // for(int i=0;i<999999999;i++) {
    // String uuid = UUIDUtils.getUUID();
    // System.out.println("uuid="+uuid);
    // }
    //
    // int concurrentCount = 1000;
    // int testTimeInSeconds = 100000000;
    // ExecutorService executorService = null;
    // CustomizableThreadFactory threadFactory = new
    // CustomizableThreadFactory("UUIDUtils-getNextSequence-test-");
    // threadFactory.setDaemon(true);
    // executorService = Executors.newFixedThreadPool(concurrentCount,
    // threadFactory);
    // List<Callable<Object>> callableList = new
    // ArrayList<Callable<Object>>();
    // Callable<Object> callable = null;
    // for(int i=0;i<concurrentCount;i++) {
    // callable = new Callable<Object>() {
    // @Override
    // public Object call() throws Exception {
    // while(true) {
    // long nextSequence = UUIDUtils.getNextSequence(1L);
    // if(nextSequence>=UUIDUtils.SEQUENCE_MAX_EXCLUDE) {
    // throw new
    // RuntimeException("Test failed. nextSequence="+nextSequence);
    // }
    // Thread.sleep(10);
    // }
    // }
    // };
    // callableList.add(callable);
    // }
    // executorService.invokeAll(callableList,testTimeInSeconds,TimeUnit.SECONDS);
  }

}
