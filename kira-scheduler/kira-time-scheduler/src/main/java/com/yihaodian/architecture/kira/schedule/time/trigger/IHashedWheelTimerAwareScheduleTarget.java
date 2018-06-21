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

interface IHashedWheelTimerAwareScheduleTarget {

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  long getLifeOnHashedWheelInMs();

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  void setLifeOnHashedWheelInMs(long lifeInMs);

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  int getIndexOnHashedWheel();

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  void setIndexOnHashedWheel(int indexOnHashedWheel);

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  long getRemainingRoundsOnHashedWheel();

  /**
   * Caution: Used internally only. The user should not call this method directly.
   */
  void setRemainingRoundsOnHashedWheel(long remainingRoundsOnHashedWheel);
}
