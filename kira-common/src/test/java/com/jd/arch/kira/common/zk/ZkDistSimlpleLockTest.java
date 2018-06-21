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

package com.jd.arch.kira.common.zk;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.lock.iface.IDistributedSimpleLock;
import com.yihaodian.architecture.kira.common.lock.impl.zk.ZKImplementedDistributedSimpleLock;
import org.junit.Test;

public class ZkDistSimlpleLockTest {

  @Test
  public void testDistributedSimpleLockLock() {
    for (int i = 0; i < 10; i++) {
      Thread t = new Thread("TestLockThread-" + i) {
        public void run() {
          while (true) {
            String poolId = "kira";
            String triggerId = "testSucess-Trigger";
            String triggerZNodeZKPath = KiraCommonUtils.getTriggerZNodeZKPath(poolId, triggerId);
            boolean autoCreateParentZNode = false;
            String parentZNodeNameForLocks = KiraCommonConstants.PARENT_ZNODE_NAME_FOR_LOCKS_TO_CONTROL_CONCURRENT_RUN_BUSINESS_METHOD;
            IDistributedSimpleLock distributedSimpleLock = new ZKImplementedDistributedSimpleLock(
                triggerZNodeZKPath, autoCreateParentZNode, parentZNodeNameForLocks);
            if (distributedSimpleLock.tryLock()) {
              try {
                System.out.println(Thread.currentThread().getName() + " got lock.");
                try {
                  Thread.sleep(3000L);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              } finally {
                System.out.println(Thread.currentThread().getName() + " will unlock.");
                distributedSimpleLock.unlock();
              }
            } else {
              System.out.println(Thread.currentThread().getName() + " can not get lock.");
            }
            try {
              Thread.sleep(10L);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      };
      t.start();
    }
  }
}
