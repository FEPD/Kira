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
package com.yihaodian.architecture.kira.schedule.time.trigger;

import java.util.Comparator;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do not work with ConcurrentSkipListSet and TreeSet which strictly require to obey the rules
 * described in Comparable. But i just want to use the id to identify the trigger. In this case no
 * way to obey that rule perfectly. This can now only work with Collections.sort
 */
public class TimerTriggerComparator implements Comparator<ITimerTrigger> {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  public TimerTriggerComparator() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public int compare(ITimerTrigger o1, ITimerTrigger o2) {
    if (null == o1 && null == o2) {
      return 0;
    }

    if (null == o1) {
      return -1;
    }
    if (null == o2) {
      return 1;
    }

    if (o1.equals(o2)) {
      return 0;
    }

    Date nextFireTimeOfo1 = o1.getNextFireTime();
    Date nextFireTimeOfo2 = o2.getNextFireTime();
    if (null == nextFireTimeOfo1 && null == nextFireTimeOfo2) {
      return 0;
    }
    if (null == nextFireTimeOfo1) {
      return 1;
    }
    if (null == nextFireTimeOfo2) {
      return -1;
    }

    int nextFireTimeCompareResult = nextFireTimeOfo1.compareTo(nextFireTimeOfo2);
    if (0 != nextFireTimeCompareResult) {
      return nextFireTimeCompareResult;
    } else {
      int priorityCompareResult = o1.getPriority() - o2.getPriority();
      return -priorityCompareResult;
    }
  }

}
