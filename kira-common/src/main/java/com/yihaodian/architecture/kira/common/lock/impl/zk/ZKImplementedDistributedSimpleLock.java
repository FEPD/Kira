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
package com.yihaodian.architecture.kira.common.lock.impl.zk;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.lock.iface.IDistributedSimpleLock;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKImplementedDistributedSimpleLock implements IDistributedSimpleLock {

  private static final String LOCKNODE_PREFIX =
      "lock" + KiraCommonConstants.ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX;
  private static final String PROCESS_KEY_NAME_UNLOCKED = "unlocked";
  private static final String PROCESS_KEY_VALUE_YES = "YES";
  private static Logger logger = LoggerFactory.getLogger(ZKImplementedDistributedSimpleLock.class);
  private final Lock lockForState;
  private final Map<String, String> lockProcessMap = new LinkedHashMap<String, String>();
  private String zkFullPathOfParentZNode; // /**/*/appId/triggerId
  private boolean autoCreateParentZNode;
  private String parentZNodeNameForLocks; // locksToControlConcurrentRunBusinessMethod
  private volatile String lockZKFullPath;
  private volatile LockZNodeData lockZNodeData;
  private volatile boolean lockOwner;
  private volatile IZkStateListener zkStateListener;
  private ZkClient zkClient = KiraZkUtil.initDefaultZk();

  public ZKImplementedDistributedSimpleLock(String zkFullPathOfParentZNode,
      boolean autoCreateParentZNode, String parentZNodeNameForLocks) {
    if (StringUtils.isNotBlank(zkFullPathOfParentZNode) && StringUtils
        .isNotBlank(parentZNodeNameForLocks)) {
      this.zkFullPathOfParentZNode = KiraCommonUtils.getTrimedZKPath(zkFullPathOfParentZNode);
      this.autoCreateParentZNode = autoCreateParentZNode;
      this.parentZNodeNameForLocks = KiraCommonUtils.getTrimedZKPath(parentZNodeNameForLocks);
      this.lockForState = new ReentrantLock();
    } else {
      throw new IllegalArgumentException(
          "Both zkFullPathOfParentZNode and parentZNodeNameForLocks should not be blank.");
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {

  }

  @Override
  public synchronized boolean tryLock() {
    boolean returnValue = false;
    try {
      this.ensureParentZNodeExist();
      this.createParentZNodeOfLocksIfNeeded();
      try {
        this.createLockZNode();
        boolean isLockOwner = this.isLockOwner();
        if (isLockOwner) {
          this.lockOwner = true;
          this.subscribeZKStateChanges();
        }
      } finally {
        if (!this.lockOwner) {
          this.cleanUp();
        } else {
          returnValue = true;
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when tryLock(). detailInfo=" + this.getDetailInfoAsString(), e);
    }

    return returnValue;
  }

  private String getDetailInfoAsString() {
    String returnValue =
        "zkFullPathOfParentZNode=" + this.zkFullPathOfParentZNode + " and autoCreateParentZNode="
            + this.autoCreateParentZNode + " and parentZNodeNameForLocks="
            + this.parentZNodeNameForLocks + " and lockZKFullPath=" + this.lockZKFullPath
            + " and lockZNodeData=" + this.lockZNodeData + " and lockOwner=" + this.lockOwner;
    return returnValue;
  }

  private void deleteLockZNodeIfNeeded() throws Exception {
    if (StringUtils.isNotBlank(this.lockZKFullPath)) {
      if (zkClient.exists(this.lockZKFullPath)) {
        zkClient.deleteRecursive(this.lockZKFullPath);
      }
    }
  }

  private void cleanUp() throws Exception {
    try {
      this.deleteLockZNodeIfNeeded();
    } finally {
      this.unsubscribeZKStateChangesIfNeeded();
    }
  }

  private void subscribeZKStateChanges() throws Exception {
    final String _lockZKFullPath = this.lockZKFullPath;
    final LockZNodeData _lockZNodeData = this.lockZNodeData;
    final boolean _lockOwner = this.lockOwner;
    this.zkStateListener = new IZkStateListener() {
      @Override
      public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
      }

      @Override
      public void handleNewSession() throws Exception {
        synchronized (ZKImplementedDistributedSimpleLock.this) {
          ZKImplementedDistributedSimpleLock.this.lockForState.lock();
          try {
            boolean isUnlocked = PROCESS_KEY_VALUE_YES.equals(
                ZKImplementedDistributedSimpleLock.this.lockProcessMap
                    .get(PROCESS_KEY_NAME_UNLOCKED));
            if (_lockOwner && (!isUnlocked) && StringUtils.isNotBlank(_lockZKFullPath)
                && null != _lockZNodeData) {
              if (zkClient.exists(_lockZKFullPath)) {
                String fullPathOfParentZNodeForLocks = ZKImplementedDistributedSimpleLock.this
                    .getFullPathOfParentZNodeForLocks();
                if (zkClient.exists(fullPathOfParentZNodeForLocks)) {
                  zkClient.createEphemeral(_lockZKFullPath, _lockZNodeData);
                }
              }
            }
          } finally {
            ZKImplementedDistributedSimpleLock.this.lockForState.unlock();
          }
        }
      }
    };
    zkClient.subscribeStateChanges(this.zkStateListener);
  }

  private void unsubscribeZKStateChangesIfNeeded() throws Exception {
    if (null != this.zkStateListener) {
      zkClient.unsubscribeStateChanges(this.zkStateListener);
    }
  }

  private boolean isLockOwner() throws Exception {
    boolean returnValue = false;
    String fullPathOfParentZNodeForLocks = this.getFullPathOfParentZNodeForLocks();
    List<String> lockList = zkClient.getChildren(fullPathOfParentZNodeForLocks);
    if (CollectionUtils.isNotEmpty(lockList)) {
      SortedSet<String> lockZKFullPathSortedSet = new TreeSet<String>();
      for (String lockZKShortPath : lockList) {
        lockZKFullPathSortedSet.add(
            fullPathOfParentZNodeForLocks + KiraCommonConstants.ZNODE_NAME_PREFIX
                + lockZKShortPath);
      }
      String lockZKFullPathOfOwner = lockZKFullPathSortedSet.first();
      if (StringUtils.equals(this.lockZKFullPath, lockZKFullPathOfOwner)) {
        returnValue = true;
      }
    }
    return returnValue;
  }

  private String createLockZNode() throws Exception {
    String fullPathOfParentZNodeForLocks = this.getFullPathOfParentZNodeForLocks();
    String lockBasePath =
        fullPathOfParentZNodeForLocks + KiraCommonConstants.ZNODE_NAME_PREFIX + LOCKNODE_PREFIX;
    Date createTime = new Date();
    this.lockZNodeData = new LockZNodeData(lockBasePath, createTime);
    this.lockZKFullPath = zkClient.createEphemeralSequential(lockBasePath, lockZNodeData);
    return this.lockZKFullPath;
  }

  private String getFullPathOfParentZNodeForLocks() {
    String fullPathOfParentZNodeForLocks =
        this.zkFullPathOfParentZNode + KiraCommonConstants.ZNODE_NAME_PREFIX + KiraUtil
            .filterString(this.parentZNodeNameForLocks);
    return fullPathOfParentZNodeForLocks;
  }

  private void createParentZNodeOfLocksIfNeeded() throws Exception {
    String fullPathOfParentZNodeForLocks = this.getFullPathOfParentZNodeForLocks();
    if (!zkClient.exists(fullPathOfParentZNodeForLocks)) {
      try {
        zkClient.createPersistent(fullPathOfParentZNodeForLocks, true);
      } catch (ZkNodeExistsException nodeExistsException) {
        logger.debug("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
            + ". Just ignore this exception. This node may be created by someone.");
      }
    }
  }

  private void ensureParentZNodeExist() throws Exception {
    //Ensure parentZNode exist.
    if (!zkClient.exists(this.zkFullPathOfParentZNode)) {
      if (autoCreateParentZNode) {
        try {
          zkClient.createPersistent(this.zkFullPathOfParentZNode, true);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.debug("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      } else {
        throw new KiraHandleException(
            "zkFullPathOfParentZNode do not exist when tryLock(). zkFullPathOfParentZNode="
                + this.zkFullPathOfParentZNode);
      }
    }
  }

  @Override
  public synchronized void unlock() {
    try {
      this.lockForState.lock();
      try {
        this.cleanUp();
      } finally {
        this.lockProcessMap.put(PROCESS_KEY_NAME_UNLOCKED, PROCESS_KEY_VALUE_YES);
        this.lockForState.unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when unlock(). detailInfo=" + this.getDetailInfoAsString(), e);
    }
  }
}
