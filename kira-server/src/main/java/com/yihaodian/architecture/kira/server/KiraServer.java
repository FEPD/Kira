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
package com.yihaodian.architecture.kira.server;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.ComponentStateEnum;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.event.AsyncEventDispatcher;
import com.yihaodian.architecture.kira.common.event.EventHandler;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.server.dto.KiraServerEntity;
import com.yihaodian.architecture.kira.server.dto.KiraServerInfo;
import com.yihaodian.architecture.kira.server.dto.KiraServerRuntimeData;
import com.yihaodian.architecture.kira.server.dto.KiraServerTrackContext;
import com.yihaodian.architecture.kira.server.event.KiraServerChangedEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerEventDispatcherRejectedExecutionHandler;
import com.yihaodian.architecture.kira.server.event.KiraServerEventType;
import com.yihaodian.architecture.kira.server.event.KiraServerRuntimeDataCheckEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerShutdownEvent;
import com.yihaodian.architecture.kira.server.event.KiraServerStartedEvent;
import com.yihaodian.architecture.kira.server.internal.util.KiraServerConstants;
import com.yihaodian.architecture.kira.server.internal.util.KiraServerZNodeData;
import com.yihaodian.architecture.kira.server.util.KiraServerRoleEnum;
import com.yihaodian.architecture.zkclient.IZkChildListener;
import com.yihaodian.architecture.zkclient.IZkStateListener;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KiraServer represents a server in the cluster which will has 1 leader and N follower at the
 * same time where N>=0. It can manage itself in the cluster and dispatch events when some cluster
 * or server related events occurs. It utilize the infrastructure of Zookeeper to maintain the
 * cluster. It can be started again after it was shutdown. But if it was destroyed it can not be
 * started any more.
 */
public class KiraServer extends ComponentAdaptor implements IKiraServer {

  private final ReadWriteLock lockForKiraServerRuntimeDataManagement = new ReentrantReadWriteLock();
  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  /**
   * serverId is used to identify a kiraServer in the cluster. Can not be changed after
   * instantiated. It should not be blank and should not contain the special character '/'.
   */
  private String serverId;
  //Fields below can be blank if they do not exist in the real world. Can not be changed after instantiated.
  //Fields host and port should both be blank or not blank.
  private String host; //host should not contain the special character '/' if it is not not blank and is used to generate serverId.
  private Integer port; //port should be >0 if not blank.
  private String accessUrlAsString;
  //This field will not be changed after initiated.
  private KiraServerEntity kiraServerEntity;
  /**
   * The zkPah root where the kiraServer cluster should work under. Can not be changed after
   * instantiated. The prefix and suffix of it should be "/" It should not be blank.
   */
  private String zkPathRootOfKiraServerCluster;
  //Fields below can not be changed after initialized.
  private String kiraServersZNodeFullPath;
  private String kiraServerLeaderZNodeFullPath;
  private KiraServerLeaderZKChildListener kiraServerLeaderZKChildListener;
  private KiraServersZkChildListener kiraServersZkChildListener;
  private ZKStateChangesListenerForKiraServer zkStateChangesListenerForKiraServer;
  private AsyncEventDispatcher kiraServerEventDispatcher;
  private ScheduledExecutorService doRoutineWorkOfKiraServerScheduledExecutorService;
  private Date lastShutdownServerTime;
  //Data below may be changed during runtime for some reason.
  private String kiraServerZNodePath;
  ;
  private Date lastUpdateKiraServerZNodePathTime;
  private KiraServerZNodeData kiraServerZNodeData;
  private Date lastUpdateKiraServerZNodeDataTime;
  private KiraServerRoleEnum kiraServerRole;
  private Date lastChangeKiraServerRoleTime;
  //Data below are only used by leader server
  private LinkedHashSet<KiraServerEntity> allOtherKiraServers = new LinkedHashSet<KiraServerEntity>();
  private Date lastUpdateAllOtherKiraServersTime;

  public KiraServer(String serverId) throws Exception {
    this(null, null, null, serverId, null);
  }

  public KiraServer(String host, Integer port, String accessUrlAsString) throws Exception {
    this(host, port, accessUrlAsString,
        KiraServerConstants.ZK_PATH_DEFAULT_ROOT_OF_KIRASERVERCLUSTER);
  }

  public KiraServer(String host, Integer port, String accessUrlAsString,
      String zkPathRootOfKiraServerCluster) throws Exception {
    this.checkAndSetHostAndPort(host, port, true);
    this.checkAndSetAccessUrlAsString(accessUrlAsString);
    //Generate serverId by the host and port
    String serverId = this.host + KiraServerConstants.COLON_DELIMITER + this.port.toString();
    this.checkAndSetServerId(serverId);
    this.checkAndSetZKPathRootOfKiraServerCluster(zkPathRootOfKiraServerCluster);

    init();
  }

  public KiraServer(String host, Integer port, String accessUrlAsString, String serverId,
      String zkPathRootOfKiraServerCluster) throws Exception {
    this.checkAndSetHostAndPort(host, port, false);
    this.checkAndSetAccessUrlAsString(accessUrlAsString);
    this.checkAndSetServerId(serverId);
    this.checkAndSetZKPathRootOfKiraServerCluster(zkPathRootOfKiraServerCluster);

    init();
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

  }

  @Override
  public boolean isLeaderServer(boolean accurate) {
    boolean returnValue = false;

    if (accurate) {
      this.lockForComponentState.readLock().lock();
      try {
        this.lockForKiraServerRuntimeDataManagement.readLock().lock();
        try {
          returnValue = this.isLeaderServer();
        } finally {
          this.lockForKiraServerRuntimeDataManagement.readLock().unlock();
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      returnValue = this.isLeaderServer();
    }

    return returnValue;
  }

  private boolean isLeaderServer() {
    return KiraServerRoleEnum.LEADER.equals(this.kiraServerRole);
  }

  private boolean isFollowerServer() {
    return KiraServerRoleEnum.FOLLOWER.equals(this.kiraServerRole);
  }

  private void init() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Initializing KiraServer...");

      this.kiraServersZNodeFullPath =
          this.zkPathRootOfKiraServerCluster + KiraServerConstants.ZNODE_NAME_KIRASERVERS;
      this.kiraServerLeaderZNodeFullPath =
          this.zkPathRootOfKiraServerCluster + KiraServerConstants.ZNODE_NAME_KIRASERVERLEADER;

      prepareForZKListeners();
      prepareAsyncEventDispatcher();
      prepareDoRoutineWorkOfKiraServerScheduledExecutorService();

      this.kiraServerEntity = new KiraServerEntity(this.serverId, this.host, this.port,
          this.accessUrlAsString);
      logger.info("Successfully initialize KiraServer.");
    } catch (Exception e) {
      logger.error("Error occurs when initializing KiraServer.", e);
      throw e;
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish initialize KiraServer. And it takes " + costTime + " milliseconds.");
    }
  }

  private void prepareDoRoutineWorkOfKiraServerScheduledExecutorService() throws Exception {
    ThreadFactory threadFactory = new CustomizedThreadFactory(
        "KiraServer-doRoutineWorkOfKiraServer-");
    this.doRoutineWorkOfKiraServerScheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(threadFactory);
    this.doRoutineWorkOfKiraServerScheduledExecutorService
        .scheduleAtFixedRate(new KiraServerDoRoutineWorkTask(),
            KiraServerConstants.KIRA_SERVER_DOROUTINEWORK_INITIALDELAY_SECOND,
            KiraServerConstants.KIRA_SERVER_DOROUTINEWORK_PERIOD_SECOND, TimeUnit.SECONDS);
  }

  private void prepareAsyncEventDispatcher() throws Exception {
    ThreadFactory threadFactory = new CustomizedThreadFactory("KiraServer-AsyncEventDispatcher-");
    RejectedExecutionHandler kiraServerEventDispatcherRejectedExecutionHandler = new KiraServerEventDispatcherRejectedExecutionHandler();
    this.kiraServerEventDispatcher = new AsyncEventDispatcher(1, 1, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>(), threadFactory,
        kiraServerEventDispatcherRejectedExecutionHandler);
    this.kiraServerEventDispatcher.init();
  }

  private void prepareForZKListeners() {
    this.kiraServersZkChildListener = new KiraServersZkChildListener();
    this.kiraServerLeaderZKChildListener = new KiraServerLeaderZKChildListener();
    this.zkStateChangesListenerForKiraServer = new ZKStateChangesListenerForKiraServer();
  }

  private void checkAndSetAccessUrlAsString(String accessUrlAsString) {
    if (StringUtils.isNotBlank(accessUrlAsString)) {
      this.accessUrlAsString = accessUrlAsString.trim();
    }
  }

  private void checkAndSetHostAndPort(String host, Integer port, boolean bothShouldBeNotBlank) {
    if (bothShouldBeNotBlank) {
      if (StringUtils.isBlank(host)) {
        throw new ValidationException("host should not be blank.");
      } else if (host.contains(KiraServerConstants.ZNODE_NAME_PREFIX)) {
        throw new ValidationException("host should not contain the special character "
            + KiraServerConstants.ZNODE_NAME_PREFIX);
      }
      this.host = host.trim();
      if (null == port) {
        throw new ValidationException("port should not be blank.");
      } else if (port.intValue() <= 0) {
        throw new ValidationException("port should not be >0 .");
      }
      this.port = port;
    } else {
      if (StringUtils.isNotBlank(host) && (null != port)) {
        this.host = host.trim();
        if (port.intValue() <= 0) {
          throw new ValidationException("port should not be >0 .");
        }
        this.port = port;
      } else if (StringUtils.isBlank(host) && (null == port)) {
        //allow both be blank
      } else {
        throw new ValidationException("host and port should both be blank or not blank.");
      }
    }
  }

  private void checkAndSetServerId(String serverId) {
    if (StringUtils.isBlank(serverId)) {
      throw new ValidationException("serverId should not be blank.");
    } else if (serverId.contains(KiraServerConstants.ZNODE_NAME_PREFIX)) {
      throw new ValidationException(
          "host should not contain the special character " + KiraServerConstants.ZNODE_NAME_PREFIX);
    }
    this.serverId = serverId.trim();
  }

  private void checkAndSetZKPathRootOfKiraServerCluster(String zkPathRootOfKiraServerCluster) {
    if (StringUtils.isNotBlank(zkPathRootOfKiraServerCluster)) {
      if (!zkPathRootOfKiraServerCluster.trim().startsWith(KiraServerConstants.ZNODE_NAME_PREFIX)) {
        throw new ValidationException(
            "The prefix should be " + KiraServerConstants.ZNODE_NAME_PREFIX
                + " for zkPathRootOfKiraServerCluster.");
      }
      if (!zkPathRootOfKiraServerCluster.trim().endsWith(KiraServerConstants.ZNODE_NAME_PREFIX)) {
        throw new ValidationException(
            "The suffix should be " + KiraServerConstants.ZNODE_NAME_PREFIX
                + " for zkPathRootOfKiraServerCluster.");
      }
      this.zkPathRootOfKiraServerCluster = zkPathRootOfKiraServerCluster.trim();
    } else {
      this.zkPathRootOfKiraServerCluster = KiraServerConstants.ZK_PATH_DEFAULT_ROOT_OF_KIRASERVERCLUSTER;
    }
  }

  @Override
  public void registerKiraServerEventHandler(EventHandler eventHandler) throws Exception {
    if (null != eventHandler) {
      this.lockForComponentState.readLock().lock();
      try {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          if (null != this.kiraServerEventDispatcher) {
            this.kiraServerEventDispatcher.register(KiraServerEventType.class, eventHandler);
          } else {
            throw new KiraHandleException(
                "kiraServerEventDispatcher is null. It should not happen. May have some bugs. So can not registerKiraServerEventHandler.");
          }
        } else {
          throw new KiraHandleException(
              "The kiraServer is destroying or already destroyed. So can not registerKiraServerEventHandler again.");
        }
      } finally {
        this.lockForComponentState.readLock().unlock();
      }
    } else {
      throw new KiraHandleException(
          "eventHandler is null. So can not registerKiraServerEventHandler.");
    }
  }

  @Override
  public String getServerId() {
    return this.serverId;
  }

  @Override
  public KiraServerEntity getKiraServerEntity() {
    return this.kiraServerEntity;
  }

  @Override
  public KiraServerInfo getKiraServerInfo() {
    KiraServerInfo returnValue = null;
    returnValue = new KiraServerInfo(this.serverId, this.host, this.port,
        this.accessUrlAsString, this.componentState.get(),
        this.kiraServerRole,
        this.lastChangeKiraServerRoleTime, this.lastStartedTime,
        this.lastShutdownServerTime, this.kiraServerZNodePath,
        this.lastUpdateKiraServerZNodePathTime,
        this.lastUpdateKiraServerZNodeDataTime,
        this.lastUpdateAllOtherKiraServersTime);
    return returnValue;
  }

  @Override
  public LinkedHashSet<KiraServerEntity> getAllKiraServerEntitysInCluster() throws Exception {
    LinkedHashSet<KiraServerEntity> returnValue = this.getAllKiraServerEntitysInClusterByZK(false);
    return returnValue;
  }

  private LinkedHashSet<KiraServerEntity> getAllKiraServerEntitysInClusterByZK(
      boolean excludeMyself) throws Exception {
    LinkedHashSet<KiraServerEntity> allKiraServersOnZKNow = new LinkedHashSet<KiraServerEntity>();
    if (zkClient.exists(this.kiraServersZNodeFullPath)) {
      List<String> kiraServerZNodeNameList = zkClient.getChildren(this.kiraServersZNodeFullPath);
      if (!CollectionUtils.isEmpty(kiraServerZNodeNameList)) {
        String kiraServerZNodeFullPath = null;
        KiraServerZNodeData kiraServerZNodeData = null;
        KiraServerEntity kiraServerEntity = null;
        String otherServerId = null;
        for (String kiraServerZNodeName : kiraServerZNodeNameList) {
          kiraServerZNodeFullPath = KiraUtil
              .getChildFullPath(this.kiraServersZNodeFullPath, kiraServerZNodeName);
          kiraServerZNodeData = zkClient.readData(kiraServerZNodeFullPath, true);
          if (null != kiraServerZNodeData) {
            otherServerId = kiraServerZNodeData.getServerId();
            boolean canAdd = true;
            if (excludeMyself && this.serverId.equals(otherServerId)) {
              canAdd = false;
            }
            if (canAdd) {
              kiraServerEntity = new KiraServerEntity(otherServerId, kiraServerZNodeData.getHost(),
                  kiraServerZNodeData.getPort(), kiraServerZNodeData.getAccessUrlAsString());
              allKiraServersOnZKNow.add(kiraServerEntity);
            }
          }
        }
      }
    }

    return allKiraServersOnZKNow;
  }

  private boolean clearAllRuntimeDataForKiraServer() {
    boolean returnValue = false;
    try {
      this.kiraServerZNodePath = null;
      this.lastUpdateKiraServerZNodePathTime = null;
      this.kiraServerZNodeData = null;
      this.lastUpdateKiraServerZNodeDataTime = null;
      this.kiraServerRole = null;
      this.lastChangeKiraServerRoleTime = null;
      this.allOtherKiraServers.clear();
      this.lastUpdateAllOtherKiraServersTime = null;

      returnValue = true;
    } catch (Exception e) {
      logger.error(
          "Error occurs for clearAllRuntimeDataForKiraServer(). kiraServerId=" + this.serverId
              + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  @Override
  public String getComponentId() {
    return "KiraServer-" + this.serverId;
  }

  @Override
  public boolean doStart() {
    boolean returnValue = false;
    try {
      this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
      try {
        logger.info("Starting KiraServer...");
        long startTime = System.currentTimeMillis();
        try {
          boolean checkAndSelfRepairIfNeededSuccess = checkAndSelfRepairIfNeeded();
          if (checkAndSelfRepairIfNeededSuccess) {
            boolean dispatchKiraServerEventSuccess = this.dispatchKiraServerStartedEvent();
            if (dispatchKiraServerEventSuccess) {
              this.lastStartedTime = new Date();
              logger.info("Successfully start KiraServer.");
              returnValue = true;
            } else {
              logger.error("dispatchKiraServerChangedEventIfNeeded failed. serverId={}",
                  this.serverId);
            }
          } else {
            logger.error(
                "Failed to checkAndSelfRepairIfNeeded when starting KiraServer. kiraServerId={}",
                this.serverId);
          }
        } finally {
          long costTime = System.currentTimeMillis() - startTime;
          logger.info("Finish start KiraServer. And it takes " + costTime + " milliseconds.");
        }
      } finally {
        this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs when call doStart() for KiraServer. kiraServerId=" + this.serverId,
          e);
    }

    return returnValue;
  }

  private KiraServerTrackContext getCurrentKiraServerTrackContext() {
    KiraServerTrackContext returnValue = new KiraServerTrackContext(this.serverId, this.host,
        this.port, this.accessUrlAsString, this.componentState.get(), this.kiraServerRole,
        (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers.clone());
    return returnValue;
  }

  private void setNewDataAndCalculateDifferenceForKiraServerTrackContext(
      KiraServerTrackContext kiraServerTrackContext) {
    kiraServerTrackContext
        .setNewDataAndCalculateDifference(this.componentState.get(), this.kiraServerRole,
            (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers.clone());
  }

  private boolean dispatchKiraServerStartedEvent() {
    boolean returnValue = false;
    KiraServerRuntimeData kiraServerRuntimeData = new KiraServerRuntimeData(this.serverId,
        this.host, this.port, this.accessUrlAsString, this.componentState.get(),
        this.kiraServerRole, (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers.clone());
    KiraServerStartedEvent kiraServerStartedEvent = new KiraServerStartedEvent(
        KiraServerEventType.KIRA_SERVER_STARTED, kiraServerRuntimeData);
    returnValue = this.dispatchKiraServerEvent(kiraServerStartedEvent);
    return returnValue;
  }

  private boolean dispatchKiraServerChangedEventIfChanged(
      KiraServerTrackContext kiraServerTrackContext) {
    boolean returnValue = false;
    if (kiraServerTrackContext.isChanged()) {
      KiraServerChangedEvent kiraServerChangedEvent = new KiraServerChangedEvent(
          kiraServerTrackContext, KiraServerEventType.KIRA_SERVER_CHANGED);
      returnValue = this.dispatchKiraServerEvent(kiraServerChangedEvent);
    } else {
      //If it is not changed also return true
      returnValue = true;
    }
    return returnValue;
  }

  private boolean dispatchKiraServerRuntimeDataCheckEvent() {
    boolean returnValue = false;
    KiraServerRuntimeData kiraServerRuntimeData = new KiraServerRuntimeData(this.serverId,
        this.host, this.port, this.accessUrlAsString, this.componentState.get(),
        this.kiraServerRole, (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers.clone());
    KiraServerRuntimeDataCheckEvent kiraServerRuntimeDataCheckEvent = new KiraServerRuntimeDataCheckEvent(
        KiraServerEventType.KIRA_SERVER_DETAIL_DATA_CHECK, kiraServerRuntimeData);
    returnValue = this.dispatchKiraServerEvent(kiraServerRuntimeDataCheckEvent);
    return returnValue;
  }

  private boolean dispatchKiraServerShutdownEvent() {
    boolean returnValue = false;
    KiraServerShutdownEvent kiraServerShutdownEvent = new KiraServerShutdownEvent(
        KiraServerEventType.KIRA_SERVER_SHUTDOWN, this.kiraServerEntity);
    returnValue = this.dispatchKiraServerEvent(kiraServerShutdownEvent);
    return returnValue;
  }

  private boolean dispatchKiraServerEvent(KiraServerEvent kiraServerEvent) {
    boolean returnValue = false;

    try {
      logger.info("Will dispatch KiraServerEvent.");

      if (null != kiraServerEventDispatcher) {
        kiraServerEventDispatcher.dispatch(kiraServerEvent);
        returnValue = true;
        logger.info("Successfully dispatch kiraServerEvent. kiraServerId={} and kiraServerEvent={}",
            this.serverId, kiraServerEvent);
      } else {
        if ((!this.isDestroying()) && (!this.isDestroyed())) {
          logger.error(
              "kiraServerEventDispatcher is null. So the event may be lost! kiraServerId={} and componentState={} and kiraServerEvent={}",
              this.serverId, this.componentState.get(), kiraServerEvent);
        } else {
          //just return true when it is destroyed.
          returnValue = true;
          logger.warn(
              "KiraServerEvent will be discarded for kiraServer is destroying or destroyed. kiraServerId={} and kiraServerEvent={}",
              this.serverId, kiraServerEvent);
        }
      }
    } catch (Exception e) {
      logger.error(
          "Exception occurs when dispatching KiraServerEvent. kiraServerId={} and componentState={} and kiraServerEvent={}",
          this.serverId, this.componentState.get(), kiraServerEvent);
    } finally {
      logger.info("Finish dispatch KiraServerEvent.");
    }

    return returnValue;
  }

  private boolean checkAndSelfRepairIfNeeded() {
    boolean returnValue = false;

    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start checkAndSelfRepairIfNeeded.");
      //1.prepare environment for kiraServer
      boolean prepareForKiraServerEnvironmentIfNeededSuccess = prepareForKiraServerEnvironmentIfNeeded();
      if (prepareForKiraServerEnvironmentIfNeededSuccess) {
        //2. register server on zk if needed
        boolean registerServerIfNeededSuccess = registerServerIfNeeded();
        if (registerServerIfNeededSuccess) {
          //3. elect server
          boolean electLeaderServerIfNeededSuccess = electLeaderServerIfNeeded();
          if (electLeaderServerIfNeededSuccess) {
            //4. Identify the role and do cluster work by role such as listen for zk events related to cluster etc.
            boolean changeServerRoleAndWorkIfNeededSuccess = changeServerRoleAndWorkIfNeeded();
            if (changeServerRoleAndWorkIfNeededSuccess) {
              //5. subscribeZKStateChanges
              this.subscribeZKStateChangesListenerForKiraServer();
              returnValue = true;
              logger.info("Successfully checkAndSelfRepairIfNeeded.");
            } else {
              logger.error(
                  "Failed to changeServerRoleAndWorkIfNeeded when checkAndSelfRepairIfNeeded. kiraServerId={}",
                  this.serverId);
            }
          } else {
            logger.error(
                "Failed to electLeaderServerIfNeeded when checkAndSelfRepairIfNeeded. kiraServerId={}",
                this.serverId);
          }
        } else {
          logger.error(
              "Failed to registerServerIfNeeded when checkAndSelfRepairIfNeeded. kiraServerId={}",
              this.serverId);
        }
      } else {
        logger.error(
            "Failed to prepareForKiraServerEnvironmentIfNeeded when checkAndSelfRepairIfNeeded. kiraServerId={}",
            this.serverId);
      }
    } catch (Exception e) {
      logger
          .error("Error occurs when checkAndSelfRepairIfNeeded. kiraServerId=" + this.serverId, e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish checkAndSelfRepairIfNeeded. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private boolean changeServerRoleAndWorkIfNeeded() {
    boolean returnValue = false;
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start changeServerRoleAndWorkIfNeeded.");
      KiraServerZNodeData kiraServerLeaderZNodeData = this.getKiraServerLeaderZNodeData();
      if (null != kiraServerLeaderZNodeData) {
        KiraServerZNodeData myKiraServerZNodeData = kiraServerZNodeData;
        KiraServerRoleEnum oldkiraServerRole = this.kiraServerRole;
        KiraServerRoleEnum newKiraServerRole = null;
        if (kiraServerLeaderZNodeData.equals(myKiraServerZNodeData)) {
          newKiraServerRole = KiraServerRoleEnum.LEADER;
        } else {
          newKiraServerRole = KiraServerRoleEnum.FOLLOWER;
        }

        if (!newKiraServerRole.equals(oldkiraServerRole)) {
          //role changed.
          if (KiraServerRoleEnum.LEADER.equals(oldkiraServerRole)
              || ((null == oldkiraServerRole) && KiraServerRoleEnum.FOLLOWER
              .equals(newKiraServerRole))) {
            //from leader to follower
            boolean changeToWorkAsFollowerServerSuccess = changeToWorkAsFollowerServer();
            if (changeToWorkAsFollowerServerSuccess) {
              returnValue = true;
              logger.info("Successfully changeServerRoleAndWorkIfNeeded as follower server.");
            }
          } else if (KiraServerRoleEnum.FOLLOWER.equals(oldkiraServerRole)
              || ((null == oldkiraServerRole) && KiraServerRoleEnum.LEADER
              .equals(newKiraServerRole))) {
            //from follower to leader
            //if(null!=oldkiraServerRole) {
            //This new leader should sleep for some time to let that old leader do something.
            //Thread.sleep(KiraServerConstants.WAIT_FOR_KIRA_SERVER_ROLE_CHANGE_MILLISECOND);
            //} else {
            //first boot, then do not sleep.
            //}
            boolean changeToWorkAsLeaderServerSuccess = changeToWorkAsLeaderServer();
            if (changeToWorkAsLeaderServerSuccess) {
              returnValue = true;
              logger.info("Successfully changeServerRoleAndWorkIfNeeded  as leader server.");
            }
          }
        } else {
          //no role changed.
          //do nothing.
          returnValue = true;
          logger.info(
              "Successfully changeServerRoleAndWorkIfNeeded. No role changed. do nothing. oldkiraServerRole="
                  + oldkiraServerRole);
        }
      } else {
        logger.info("kiraServerLeaderZNodeData is null when changeServerRoleAndWorkIfNeeded.");
      }
    } catch (Exception e) {
      logger
          .error("Error occurs when changeServerRoleAndWorkIfNeeded. kiraServerId=" + this.serverId,
              e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish changeServerRoleAndWorkIfNeeded. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private boolean changeToWorkAsLeaderServer() {
    boolean returnValue = false;
    long startTime = System.currentTimeMillis();
    KiraServerRoleEnum oldkiraServerRole = this.kiraServerRole;
    boolean roleValueChanged = false;
    try {
      logger.info("Start changeToWorkAsLeaderServer.");
      unsubscribeChildChangesForKiraServerLeaderZNode();
      this.kiraServerRole = KiraServerRoleEnum.LEADER;
      roleValueChanged = true;

      boolean calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess = this
          .calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded(true);
      if (calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess) {
        subscribeChildChangesForKiraServersZNode();
        returnValue = true;
        logger.info("Successfully changeToWorkAsLeaderServer.");
      } else {
        logger.error(
            "Failed to calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded for changeToWorkAsLeaderServer. kiraServerId={}",
            this.serverId);
      }
    } catch (Exception e) {
      logger
          .error("Error occurs when changeToWorkAsLeaderServer. kiraServerId=" + this.serverId, e);
    } finally {
      if (!returnValue) {
        //need to set the role back if failed.
        if (roleValueChanged) {
          this.kiraServerRole = oldkiraServerRole;
        }
      } else {
        this.lastChangeKiraServerRoleTime = new Date();
      }
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish changeToWorkAsLeaderServer. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private void doRoutineWorkOfKiraServer() {
    long startTime = System.currentTimeMillis();
    boolean logHighLevel = true;
    try {
      logger.info("Start doRoutineWorkOfKiraServer...");
      if (!this.requestDestroy) {
        this.lockForComponentState.readLock().lock();
        try {
          if (!this.requestDestroy) {
            if (this.isStarted()) {
              this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
              try {
                KiraServerTrackContext kiraServerTrackContext = getCurrentKiraServerTrackContext();
                boolean checkAndSelfRepairIfNeededSuccess = KiraServer.this
                    .checkAndSelfRepairIfNeeded();
                if (checkAndSelfRepairIfNeededSuccess) {
                  boolean continueWithKiraServerTrackContext = true;
                  if (this.isLeaderServer()) {
                    boolean calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess = this
                        .calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded(false);
                    if (calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess) {
                      logger.info("Successfully doRoutineWorkOfKiraServer as Leader. serverid={}",
                          this.serverId);
                    } else {
                      continueWithKiraServerTrackContext = false;
                      logger.error(
                          "Failed to calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded for doRoutineWorkOfKiraServer. serverid={}",
                          this.serverId);
                    }
                  } else if (KiraServer.this.isFollowerServer()) {
                    //Follower do not calculate allOtherKiraServers.
                    logger.info("Successfully doRoutineWorkOfKiraServer as Follower. serverid={}",
                        this.serverId);
                  }

                  if (continueWithKiraServerTrackContext) {
                    this.setNewDataAndCalculateDifferenceForKiraServerTrackContext(
                        kiraServerTrackContext);
                    boolean dispatchKiraServerChangedEventIfChangedSuccess = this
                        .dispatchKiraServerChangedEventIfChanged(kiraServerTrackContext);
                    if (dispatchKiraServerChangedEventIfChangedSuccess) {
                      if (!kiraServerTrackContext.isChanged()) {
                        logHighLevel = false;
                        if (!this.requestShutdown && !this.requestDestroy) {
                          //only dispatch KiraServerRuntimeDataCheckEvent when all works and no shutdown or destroy request now.
                          boolean dispatchKiraServerRuntimeDataCheckEventSuccess = this
                              .dispatchKiraServerRuntimeDataCheckEvent();
                          if (!dispatchKiraServerRuntimeDataCheckEventSuccess) {
                            logger.error(
                                "Failed to dispatchKiraServerRuntimeDataCheckEvent for doRoutineWorkOfKiraServer. serverid={}",
                                this.serverId);
                          }
                        } else {
                          logger.warn(
                              "KiraServer may request shutdown or destroy. so do not send kiraServerRuntimeDataCheckEvent.kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
                              KiraServer.this.serverId, KiraServer.this.componentState,
                              this.requestShutdown, this.requestDestroy);
                        }
                      }
                    } else {
                      logger.error(
                          "Failed to dispatchKiraServerChangedEventIfChanged for doRoutineWorkOfKiraServer. serverid={}",
                          this.serverId);
                    }
                  }
                } else {
                  logger.error(
                      "Failed to checkAndSelfRepairIfNeeded for doRoutineWorkOfKiraServer. serverid={}",
                      this.serverId);
                }
              } finally {
                this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
              }
            } else if (!this.requestDestroy && this.isShutdown()) {
              //Always dispatch KiraServerRuntimeDataCheckEvent when in the shutdown state.
              boolean dispatchKiraServerRuntimeDataCheckEventSuccess = this
                  .dispatchKiraServerRuntimeDataCheckEvent();
              if (!dispatchKiraServerRuntimeDataCheckEventSuccess) {
                logger.error(
                    "Failed to dispatchKiraServerRuntimeDataCheckEvent for doRoutineWorkOfKiraServer when kiraServer is in shutdown state. serverid={}",
                    this.serverId);
              }
            } else {
              logger.info(
                  "KiraServer is not in the started and shutdown state or may request destroy. So do not do anything for doRoutineWorkOfKiraServer. serverId={} and componentState={} aand requestDestroy={}",
                  this.serverId, this.componentState, this.requestDestroy);
            }
          } else {
            logger.info(
                "KiraServer may request destroy. So do not doRoutineWorkOfKiraServer. kiraServerId={} and componentState={} and requestShutdown={}",
                KiraServer.this.serverId, KiraServer.this.componentState, this.requestShutdown);
          }
        } finally {
          this.lockForComponentState.readLock().unlock();
        }
      } else {
        logger.info(
            "KiraServer may request destroy. So do not doRoutineWorkOfKiraServer. kiraServerId={} and componentState={} and requestShutdown={}",
            KiraServer.this.serverId, KiraServer.this.componentState, this.requestShutdown);
      }
    } catch (Exception e) {
      logger.error("Error occurs when doRoutineWorkOfKiraServer. kiraServerId=" + this.serverId, e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      if (logHighLevel) {
        logger.info("Finish doRoutineWorkOfKiraServer. It takes {} milliseconds. serverId={}",
            costTime, this.serverId);
      } else {
        logger.debug("Finish doRoutineWorkOfKiraServer. It takes {} milliseconds. serverId={}",
            costTime, this.serverId);
      }

    }
  }

  private boolean calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded(
      boolean isFirstCalculateAfterWorkAsLeader) throws Exception {
    boolean returnValue = false;
    LinkedHashSet<KiraServerEntity> allOtherKiraServersCloned = (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers
        .clone();
    boolean allOtherKiraServersChanged = false;
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded.");
      if (isFirstCalculateAfterWorkAsLeader) {
        //Should clear first before the first time calculate.
        this.allOtherKiraServers.clear();
        allOtherKiraServersChanged = true;
      }
      LinkedHashSet<KiraServerEntity> allOtherKiraServersOnZKNow = this
          .getAllKiraServerEntitysInClusterByZK(true);
      if (!this.allOtherKiraServers.equals(allOtherKiraServersOnZKNow)) {
        this.allOtherKiraServers = allOtherKiraServersOnZKNow;
        allOtherKiraServersChanged = true;
      }
      returnValue = true;
      logger.info("Successfully calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded.");
    } catch (Exception e) {
      logger.error(
          "Error occurs for calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded. kiraServerId="
              + this.serverId, e);
    } finally {
      if (!returnValue) {
        //set it back
        if (allOtherKiraServersChanged) {
          this.allOtherKiraServers = allOtherKiraServersCloned;
        }
      } else {
        if (allOtherKiraServersChanged) {
          lastUpdateAllOtherKiraServersTime = new Date();
        }
      }
      long costTime = System.currentTimeMillis() - startTime;
      logger.info(
          "Finish calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded. And it takes " + costTime
              + " milliseconds. returnValue=" + returnValue);
    }

    return returnValue;
  }

  private boolean changeToWorkAsFollowerServer() {
    boolean returnValue = false;
    long startTime = System.currentTimeMillis();
    KiraServerRoleEnum oldkiraServerRole = this.kiraServerRole;
    boolean roleValueChanged = false;
    LinkedHashSet<KiraServerEntity> allOtherKiraServersCloned = (LinkedHashSet<KiraServerEntity>) this.allOtherKiraServers
        .clone();
    boolean allOtherKiraServersChanged = false;
    try {
      logger.info("Start changeToWorkAsFollowerServer.");
      unsubscribeChildChangesForKiraServersZNode();
      this.kiraServerRole = KiraServerRoleEnum.FOLLOWER;
      roleValueChanged = true;
      this.allOtherKiraServers.clear();
      allOtherKiraServersChanged = true;
      subscribeChildChangesForKiraServerLeaderZNode();
      returnValue = true;
      logger.info("Successfully changeToWorkAsFollowerServer.");
    } catch (Exception e) {
      logger.error("Error occurs when changeToWorkAsFollowerServer. kiraServerId=" + this.serverId,
          e);
    } finally {
      if (!returnValue) {
        //need to set the role back if failed.
        if (roleValueChanged) {
          this.kiraServerRole = oldkiraServerRole;
        }
        if (allOtherKiraServersChanged) {
          this.allOtherKiraServers = allOtherKiraServersCloned;
        }
      } else {
        Date now = new Date();
        this.lastChangeKiraServerRoleTime = now;
        this.lastUpdateAllOtherKiraServersTime = now;
      }
      long costTime = System.currentTimeMillis() - startTime;
      logger
          .info("Finish changeToWorkAsFollowerServer. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private void unsubscribeChildChangesForKiraServersZNode() throws Exception {
    try {
      logger.info("Start unsubscribeChildChangesForKiraServersZNode.");
      zkClient.unsubscribeChildChanges(this.kiraServersZNodeFullPath, kiraServersZkChildListener);
      logger.info("Successfully unsubscribeChildChangesForKiraServersZNode.");
    } catch (Exception e) {
      logger.error("Error occurs when unSubscribeChildChangesForScheduleServersZNode. kiraServerId="
          + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish unsubscribeChildChangesForKiraServersZNode.");
    }
  }

  private void subscribeChildChangesForKiraServersZNode() throws Exception {
    try {
      logger.info("Start subscribeChildChangesForKiraServersZNode.");
      zkClient
          .subscribeChildChanges(this.kiraServersZNodeFullPath, this.kiraServersZkChildListener);
      logger.info("Successfully subscribeChildChangesForKiraServersZNode.");
    } catch (Exception e) {
      logger.error("Error occurs when subscribeChildChangesForKiraServersZNode. kiraServerId="
          + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish subscribeChildChangesForKiraServersZNode.");
    }
  }

  private void unsubscribeChildChangesForKiraServerLeaderZNode() throws Exception {
    try {
      logger.info("Start unsubscribeChildChangesForKiraServerLeaderZNode.");
      zkClient.unsubscribeChildChanges(this.kiraServerLeaderZNodeFullPath,
          this.kiraServerLeaderZKChildListener);
      logger.info("Successfully unsubscribeChildChangesForKiraServerLeaderZNode.");
    } catch (Exception e) {
      logger.error(
          "Error occurs when unsubscribeChildChangesForKiraServerLeaderZNode. kiraServerId="
              + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish unsubscribeChildChangesForKiraServerLeaderZNode.");
    }
  }

  private void subscribeChildChangesForKiraServerLeaderZNode() throws Exception {
    try {
      logger.info("Start subscribeChildChangesForKiraServerLeaderZNode.");
      zkClient.subscribeChildChanges(this.kiraServerLeaderZNodeFullPath,
          this.kiraServerLeaderZKChildListener);
      logger.info("Successfully subscribeChildChangesForKiraServerLeaderZNode.");
    } catch (Exception e) {
      logger.error("Error occurs when subscribeChildChangesForKiraServerLeaderZNode. kiraServerId="
          + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish subscribeChildChangesForKiraServerLeaderZNode.");
    }
  }

  private void unsubscribeZKStateChangesListenerForKiraServer() throws Exception {
    try {
      logger.info("Start unsubscribeZKStateChangesListenerForKiraServer...");
      zkClient.unsubscribeStateChanges(this.zkStateChangesListenerForKiraServer);
      logger.info("Successfully unsubscribeZKStateChangesListenerForKiraServer.");
    } catch (Exception e) {
      logger.error("Error occurs when unsubscribeZKStateChangesListenerForKiraServer. kiraServerId="
          + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish unsubscribeZKStateChangesListenerForKiraServer.");
    }
  }

  private void subscribeZKStateChangesListenerForKiraServer() throws Exception {
    try {
      logger.info("Start subscribeZKStateChangesListenerForKiraServer...");
      zkClient.subscribeStateChanges(this.zkStateChangesListenerForKiraServer);
      logger.info("Successfully subscribeZKStateChangesListenerForKiraServer.");
    } catch (Exception e) {
      logger.error("Error occurs when subscribeZKStateChangesListenerForKiraServer. kiraServerId="
          + this.serverId, e);
      throw e;
    } finally {
      logger.info("Finish subscribeZKStateChangesListenerForKiraServer.");
    }
  }

  private KiraServerZNodeData getKiraServerLeaderZNodeData() {
    KiraServerZNodeData kiraServerLeaderZNodeData = null;
    try {
      if (zkClient.exists(this.kiraServerLeaderZNodeFullPath)) {
        List<String> kiraServerLeaderZNodeNameList = zkClient
            .getChildren(this.kiraServerLeaderZNodeFullPath);
        if (!CollectionUtils.isEmpty(kiraServerLeaderZNodeNameList)) {
          String kiraServerLeaderZNodeName = kiraServerLeaderZNodeNameList.get(0);
          String kiraServerLeaderZNodePath = KiraUtil
              .getChildFullPath(this.kiraServerLeaderZNodeFullPath, kiraServerLeaderZNodeName);
          kiraServerLeaderZNodeData = zkClient.readData(kiraServerLeaderZNodePath, true);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when getKiraServerLeaderZNodeData. kiraServerId=" + this.serverId,
          e);
    }

    return kiraServerLeaderZNodeData;
  }

  private boolean electLeaderServerIfNeeded() {
    boolean returnValue = false;

    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start electLeaderServerIfNeeded...");
      String kiraServerLeaderZNodePath = this.getKiraServerLeaderZNodePath();
      if (StringUtils.isNotBlank(kiraServerLeaderZNodePath)) {
        boolean isKiraServerLeaderZNodeDataValid = this
            .isKiraServerLeaderZNodePathValid(kiraServerLeaderZNodePath);
        if (!isKiraServerLeaderZNodeDataValid) {
          logger.warn("Kira server leader data is not valid. Will delete the leader node...");
          if (zkClient.exists(kiraServerLeaderZNodePath)) {
            zkClient.delete(kiraServerLeaderZNodePath);
          }
        } else {
          returnValue = true;
          logger.info(
              "Exist KiraServerLeaderZNodeData is valid. So no need to elect leader server now.");
        }
      }

      if (!returnValue) {
        boolean createKiraServerLeaderZNodeSuccess = createKiraServerLeaderZNodeIfNeeded();
        if (!createKiraServerLeaderZNodeSuccess) {
          logger.warn("createKiraServerLeaderZNode failed. ");
        } else {
          returnValue = true;
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when electLeaderServerIfNeeded. kiraServerId=" + this.serverId, e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish electLeaderServerIfNeeded. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private boolean createKiraServerLeaderZNodeIfNeeded() {
    boolean returnValue = false;
    try {
      logger.info("Start createKiraServerLeaderZNodeIfNeeded...");
      if (!zkClient.exists(this.kiraServersZNodeFullPath)) {
        try {
          zkClient.createPersistent(this.kiraServersZNodeFullPath, true);
          logger.info("ZNode created. kiraServersZNodeFullPath={}", this.kiraServersZNodeFullPath);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }
      if (zkClient.exists(this.kiraServersZNodeFullPath)) {
        List<String> kiraServerList = zkClient.getChildren(this.kiraServersZNodeFullPath);
        if (!CollectionUtils.isEmpty(kiraServerList)) {
          Integer min = null;
          String minKiraServerShortPath = null;
          for (String oneKiraServerShortPath : kiraServerList) {
            if (null == min) {
              min = Integer.valueOf(oneKiraServerShortPath.substring(oneKiraServerShortPath
                  .lastIndexOf(KiraServerConstants.ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX)
                  + 1));
              minKiraServerShortPath = oneKiraServerShortPath;
            } else {
              Integer tempValue = Integer.valueOf(oneKiraServerShortPath.substring(
                  oneKiraServerShortPath.lastIndexOf(
                      KiraServerConstants.ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX) + 1));
              if (min.compareTo(tempValue) > 0) {
                min = tempValue;
                minKiraServerShortPath = oneKiraServerShortPath;
              }
            }
          }
          String minChildPath = KiraUtil
              .getChildFullPath(this.kiraServersZNodeFullPath, minKiraServerShortPath);
          KiraServerZNodeData minKiraServerZNodeData = zkClient.readData(minChildPath, true);
          if (null != minKiraServerZNodeData) {
            String minKiraServerZNodeNameBase = minKiraServerShortPath.substring(0,
                minKiraServerShortPath.lastIndexOf(
                    KiraServerConstants.ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX));
            String minKiraServerZnodePath =
                this.kiraServerLeaderZNodeFullPath + KiraServerConstants.ZNODE_NAME_PREFIX
                    + minKiraServerZNodeNameBase;

            //check again before create. The leader znode may already be created by other server.
            if (zkClient.exists(this.kiraServerLeaderZNodeFullPath)) {
              List<String> kiraServerLeaderZNodeNameList = zkClient
                  .getChildren(this.kiraServerLeaderZNodeFullPath);
              if (CollectionUtils.isEmpty(kiraServerLeaderZNodeNameList)) {
                if (!zkClient.exists(minKiraServerZnodePath)) {
                  try {
                    zkClient.createEphemeral(minKiraServerZnodePath, minKiraServerZNodeData);
                    logger.info(
                        "Kira Server Leader ZNode created. minKiraServerZnodePath={} and minKiraServerZNodeData={}",
                        minKiraServerZnodePath, KiraCommonUtils.toString(minKiraServerZNodeData));
                  } catch (ZkNodeExistsException nodeExistsException) {
                    logger.info(
                        "ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
                            + ". Just ignore this exception. The leader node may be created by other server.");
                  }
                }
                returnValue = true;
              } else {
                returnValue = true;
                logger.info(
                    "Kira server leader found. It may be created by other server. So do not create the leader node. minKiraServerZnodePath="
                        + minKiraServerZnodePath + " and kiraServerLeaderZNodeNameList="
                        + KiraCommonUtils.toString(kiraServerLeaderZNodeNameList));
              }
            }
          }
        } else {
          logger.warn("No exist kira server found.");
          //this.registerServerIfNeeded(); //do not try here now.
        }
      } else {
        logger.warn(
            "The node still do not exist for createKiraServerLeaderZNodeIfNeeded. znodePath={}",
            this.kiraServersZNodeFullPath);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when createKiraServerLeaderZNodeIfNeeded. kiraServerId=" + this.serverId,
          e);
    } finally {
      logger.info("End createKiraServerLeaderZNodeIfNeeded. returnValue=" + returnValue);
    }

    return returnValue;
  }

  private boolean isKiraServerLeaderZNodePathValid(String kiraServerLeaderZNodePath)
      throws Exception {
    boolean returnValue = false;
    try {
      if (StringUtils.isNotBlank(kiraServerLeaderZNodePath)) {
        if (zkClient.exists(kiraServerLeaderZNodePath)) {
          KiraServerZNodeData kiraServerLeaderZNodeData = zkClient
              .readData(kiraServerLeaderZNodePath, true);
          returnValue = this.isKiraServerLeaderZNodeDataValid(kiraServerLeaderZNodeData);
        }
      }
    } catch (Exception e) {
      logger
          .error("Error occurs for isKiraServerLeaderZNodePathValid. kiraServerId=" + this.serverId,
              e);
      throw e;
    }
    return returnValue;
  }

  private boolean isKiraServerLeaderZNodeDataValid(KiraServerZNodeData kiraServerLeaderZNodeData)
      throws Exception {
    boolean returnValue = false;
    try {
      if (null != kiraServerLeaderZNodeData) {
        if (zkClient.exists(this.kiraServersZNodeFullPath)) {
          List<String> kiraServerZNodeNameList = zkClient
              .getChildren(this.kiraServersZNodeFullPath);
          if (!CollectionUtils.isEmpty(kiraServerZNodeNameList)) {
            String kiraServerZNodeFullPath = null;
            KiraServerZNodeData kiraServerZNodeData = null;
            for (String kiraServerZNodeName : kiraServerZNodeNameList) {
              kiraServerZNodeFullPath = KiraUtil
                  .getChildFullPath(this.kiraServersZNodeFullPath, kiraServerZNodeName);
              kiraServerZNodeData = zkClient.readData(kiraServerZNodeFullPath, true);
              if (kiraServerLeaderZNodeData.equals(kiraServerZNodeData)) {
                returnValue = true;
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      logger
          .error("Error occurs for isKiraServerLeaderZNodeDataValid. kiraServerId=" + this.serverId,
              e);
      throw e;
    }
    return returnValue;
  }

  private String getKiraServerLeaderZNodePath() throws Exception {
    String kiraServerLeaderZNodePath = null;
    try {
      if (zkClient.exists(this.kiraServerLeaderZNodeFullPath)) {
        List<String> kiraServerLeaderZNodeNameList = zkClient
            .getChildren(this.kiraServerLeaderZNodeFullPath);
        if (!CollectionUtils.isEmpty(kiraServerLeaderZNodeNameList)) {
          String kiraServerLeaderZNodeName = kiraServerLeaderZNodeNameList.get(0);
          kiraServerLeaderZNodePath = KiraUtil
              .getChildFullPath(this.kiraServerLeaderZNodeFullPath, kiraServerLeaderZNodeName);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs when getKiraServerLeaderZNodePath. kiraServerId=" + this.serverId,
          e);
      throw e;
    }

    return kiraServerLeaderZNodePath;
  }

  private boolean registerServerIfNeeded() {
    boolean returnValue = false;
    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start registerServerIfNeeded...");
      boolean cachedKiraServerDataValid = isCachedKiraServerDataValid();
      if (!cachedKiraServerDataValid) {
        reCreateKiraServerZNode();
      } else {
        logger.info("No need to recreate the kira server znode. cachedKiraServerDataValid="
            + cachedKiraServerDataValid);
      }
      returnValue = true;
      logger.info("Successfully registerServerIfNeeded.");
    } catch (Exception e) {
      logger.error("Error occurs when registerServerIfNeeded. kiraServerId=" + this.serverId, e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish registerServerIfNeeded. And it takes " + costTime + " milliseconds.");
    }

    return returnValue;
  }

  private void reCreateKiraServerZNode() throws Exception {
    String cachedKiraServerZNodePath = this.kiraServerZNodePath;
    boolean kiraServerZNodePathUpdated = false;
    KiraServerZNodeData originalKiraServerZNodeData = null;
    if (null != this.kiraServerZNodeData) {
      originalKiraServerZNodeData = (KiraServerZNodeData) this.kiraServerZNodeData.clone();
    }
    boolean kiraServerZNodeDataUpdated = false;
    try {
      logger.info("start reCreateKiraServerZNode");
      if (!zkClient.exists(this.kiraServersZNodeFullPath)) {
        try {
          zkClient.createPersistent(this.kiraServersZNodeFullPath, true);
          logger.info("ZNode created. kiraServersZNodeFullPath={}", this.kiraServersZNodeFullPath);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }

      if (StringUtils.isNotBlank(cachedKiraServerZNodePath)) {
        if (zkClient.exists(cachedKiraServerZNodePath)) {
          logger.warn("Will delete the old cachedKiraServerZNodePath=" + cachedKiraServerZNodePath);
          zkClient.delete(cachedKiraServerZNodePath);
        }
        this.kiraServerZNodePath = null;
      }
      this.kiraServerZNodeData = null;

      this.kiraServerZNodeData = new KiraServerZNodeData(this.serverId, this.host, this.port,
          this.accessUrlAsString);
      this.kiraServerZNodePath = zkClient.createEphemeralSequential(
          this.kiraServersZNodeFullPath + KiraServerConstants.ZNODE_NAME_PREFIX + this.serverId
              + KiraServerConstants.ZNODE_NAME_EPHEMERAL_SEQUENTIAL_BASE_NAME_SUFFIX,
          kiraServerZNodeData);
      logger.info("ZNode created. kiraServerZNodePath={}", this.kiraServerZNodePath);

      logger.info(
          "Successfully reCreateKiraServerZNode. new kiraServerZNodePath={} and new kiraServerZNodeData={}",
          this.kiraServerZNodePath, this.kiraServerZNodeData);
      Date now = new Date();
      kiraServerZNodePathUpdated = true;
      kiraServerZNodeDataUpdated = true;
      this.lastUpdateKiraServerZNodePathTime = now;
      this.lastUpdateKiraServerZNodeDataTime = now;
    } catch (Exception e) {
      if (kiraServerZNodePathUpdated) {
        //set it back
        this.kiraServerZNodePath = cachedKiraServerZNodePath;
      }
      if (kiraServerZNodeDataUpdated) {
        //set it back
        this.kiraServerZNodeData = originalKiraServerZNodeData;
      }
      logger.error("Error occurs when createKiraServerZNodeIfNeeded. kiraServerId=" + this.serverId,
          e);
      throw e;
    } finally {
      logger.info("Finish reCreateKiraServerZNode.");
    }
  }

  private boolean isCachedKiraServerDataValid() throws Exception {
    boolean returnValue = false;
    String cachedKiraServerZNodePath = this.kiraServerZNodePath;
    KiraServerZNodeData cachedKiraServerZNodeData = this.kiraServerZNodeData;
    try {
      if (StringUtils.isNotBlank(cachedKiraServerZNodePath)) {
        if (zkClient.exists(cachedKiraServerZNodePath)) {
          if (null != cachedKiraServerZNodeData) {
            KiraServerZNodeData kiraServerZNodeDataOnZK = zkClient
                .readData(cachedKiraServerZNodePath, true);
            if (cachedKiraServerZNodeData.equals(kiraServerZNodeDataOnZK)) {
              returnValue = true;
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error occurs for isCachedKiraServerDataValid. kiraServerId=" + this.serverId
              + " and cachedKiraServerZNodePath=" + cachedKiraServerZNodePath
              + " and cachedKiraServerZNodeData=" + KiraCommonUtils.toString(cachedKiraServerZNodeData),
          e);
      throw e;
    }

    return returnValue;
  }

  private boolean prepareForKiraServerEnvironmentIfNeeded() {
    boolean returnValue = false;

    long startTime = System.currentTimeMillis();
    try {
      logger.info("Start prepareForKiraServerEnvironmentIfNeeded...");
      if (!zkClient.exists(this.kiraServersZNodeFullPath)) {
        try {
          zkClient.createPersistent(this.kiraServersZNodeFullPath, true);
          logger.info("ZNode created. kiraServersZNodeFullPath={}", this.kiraServersZNodeFullPath);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }
      if (!zkClient.exists(this.kiraServerLeaderZNodeFullPath)) {
        try {
          zkClient.createPersistent(this.kiraServerLeaderZNodeFullPath, true);
          logger.info("ZNode created. kiraServerLeaderZNodeFullPath={}",
              this.kiraServerLeaderZNodeFullPath);
        } catch (ZkNodeExistsException nodeExistsException) {
          logger.info("ZkNodeExistsException occurs. message=" + nodeExistsException.getMessage()
              + ". Just ignore this exception. This node may be created by someone.");
        }
      }
      returnValue = true;
      logger.info("Succesfully prepareForKiraServerEnvironmentIfNeeded.");
    } catch (Exception e) {
      logger.error("Error occurs when prepareForKiraServerEnvironmentIfNeeded. kiraServerId="
          + KiraServer.this.serverId, e);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.info("Finish prepareForKiraServerEnvironmentIfNeeded. And it takes " + costTime
          + " milliseconds.");
    }

    return returnValue;
  }

  private boolean handleChildChangeForKiraServersZNode() {
    boolean returnValue = false;

    this.lockForComponentState.readLock().lock();
    try {
      if (!this.requestShutdown && !this.requestDestroy) {
        if (this.isStarted()) {
          this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
          try {
            long startTime = System.currentTimeMillis();
            try {
              logger.info("Start handleChildChangeForKiraServersZNode...");
              KiraServerTrackContext kiraServerTrackContext = getCurrentKiraServerTrackContext();
              boolean checkAndSelfRepairIfNeededSuccess = KiraServer.this
                  .checkAndSelfRepairIfNeeded();
              if (checkAndSelfRepairIfNeededSuccess) {
                boolean continueWithKiraServerTrackContext = true;
                if (KiraServer.this.isLeaderServer()) {
                  boolean calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess = this
                      .calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded(false);
                  if (calculateAllOtherKiraServersAndUpdateLocalCacheIfNeededSuccess) {
                    returnValue = true;
                    logger.info("Successfully handleChildChangeForKiraServersZNode.");
                  } else {
                    continueWithKiraServerTrackContext = false;
                    logger.error(
                        "Failed to calculateAllOtherKiraServersAndUpdateLocalCacheIfNeeded for handleChildChangeForKiraServersZNode. serverId={}",
                        this.serverId);
                  }
                } else if (KiraServer.this.isFollowerServer()) {
                  returnValue = true;
                  logger.warn(
                      "The follower server should not listen for events under kiraServers. kiraServerZNodePath={} and KiraServerZNodeData={}",
                      KiraServer.this.kiraServerZNodePath, KiraServer.this.kiraServerZNodeData);
                }

                if (continueWithKiraServerTrackContext) {
                  this.setNewDataAndCalculateDifferenceForKiraServerTrackContext(
                      kiraServerTrackContext);
                  boolean dispatchKiraServerChangedEventIfNeededSuccess = this
                      .dispatchKiraServerChangedEventIfChanged(kiraServerTrackContext);
                  if (!dispatchKiraServerChangedEventIfNeededSuccess) {
                    returnValue = false;
                    logger.error(
                        "Failed to dispatchKiraServerChangedEventIfChanged for handleChildChangeForKiraServersZNode. serverId={}",
                        this.serverId);
                  }
                }
              } else {
                logger.error(
                    "Failed to checkAndSelfRepairIfNeeded for handleChildChangeForKiraServersZNode. serverId={}",
                    this.serverId);
              }
            } catch (Exception e) {
              logger.error("Error occurs when handleChildChangeForKiraServersZNode. kiraServerId="
                  + this.serverId, e);
            } finally {
              long costTime = System.currentTimeMillis() - startTime;
              logger.info("Finish handleChildChangeForKiraServersZNode. And it takes " + costTime
                  + " milliseconds.");
            }
          } finally {
            this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
          }
        } else {
          logger.warn(
              "KiraServer is not in the started state. So do not handleChildChangeForKiraServersZNode. serverId={} and componentState={}",
              this.serverId, this.componentState);
          returnValue = true;
        }
      } else {
        logger.warn(
            "KiraServer may request shutdown or destroy just now. So do not continue to handleChildChangeForKiraServersZNode. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
            KiraServer.this.serverId, KiraServer.this.componentState, this.requestShutdown,
            this.requestDestroy);
      }
    } finally {
      this.lockForComponentState.readLock().unlock();
    }

    return returnValue;
  }

  private void deleteZNodeIfExist(String znodePath) {
    try {
      if (zkClient.exists(znodePath)) {
        zkClient.delete(znodePath);
      }
    } catch (Exception e) {
      logger.error("Error occurs for deleteZNodeIfExist. znodePath=" + znodePath, e);
    }
  }

  @Override
  public void shutdown() {
    try {
      this.requestShutdown = true;
      this.lockForComponentState.writeLock().lock();
      try {
        if (this.componentState.compareAndSet(ComponentStateEnum.STARTED.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())
            || this.componentState.compareAndSet(ComponentStateEnum.STARTING.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())) {
          this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
          try {
            long startTime = System.currentTimeMillis();
            try {
              logger.info("Shutting down KiraServer. serverId={}", this.serverId);
              try {
                this.unsubscribeChildChangesForKiraServersZNode();
              } catch (Exception e) {
                logger.error(
                    "Error occurs when unsubscribeChildChangesForKiraServersZNode during shutdown process. kiraServerId="
                        + this.serverId, e);
              }

              try {
                this.unsubscribeChildChangesForKiraServerLeaderZNode();
              } catch (Exception e) {
                logger.error(
                    "Error occurs when unsubscribeChildChangesForKiraServerLeaderZNode during shutdown process. kiraServerId="
                        + this.serverId, e);
              }

              if (StringUtils.isNotEmpty(this.kiraServerZNodePath)) {
                deleteZNodeIfExist(this.kiraServerZNodePath);
              }

              if (null != this.kiraServerZNodeData) {
                String kiraServerLeaderPath =
                    this.kiraServerLeaderZNodeFullPath + KiraServerConstants.ZNODE_NAME_PREFIX
                        + this.serverId;
                deleteZNodeIfExist(kiraServerLeaderPath);
              }

              try {
                this.unsubscribeZKStateChangesListenerForKiraServer();
              } catch (Exception e) {
                logger.error(
                    "Error occurs when unsubscribeZKStateChangesListenerForKiraServer during shutdown process. kiraServerId="
                        + this.serverId, e);
              }

              this.clearAllRuntimeDataForKiraServer();

              logger.info("Successfully shutdown KiraServer. serverId={}", this.serverId);
            } catch (Exception e) {
              logger.error(
                  "Error occurs when shutting down KiraServer. kiraServerId=" + this.serverId, e);
            } finally {
              this.componentState.set(ComponentStateEnum.SHUTDOWN.getState());
              this.lastShutdownServerTime = new Date();

              boolean dispatchKiraServerShutdownEventSucess = this
                  .dispatchKiraServerShutdownEvent();
              if (!dispatchKiraServerShutdownEventSucess) {
                logger
                    .error("Failed to dispatchKiraServerShutdownEvent. serverId={}", this.serverId);
              }

              long costTime = System.currentTimeMillis() - startTime;
              logger.info("Finish shutdown KiraServer. serverId={} and it takes {} milliseconds.",
                  this.serverId, costTime);
            }
          } finally {
            this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
          }
        } else {
          if (this.requestDestroy) {
            logger.warn(
                "KiraServer may request destroy. But it is already not in the started state. So do nothing for shutdown KiraServer. kiraServerId={} and componentState={}",
                this.serverId, this.componentState);
          } else {
            if (this.isInInitState()) {
              logger.warn("KiraServer is in init state. So no need to shutdown.");
            } else if (this.isShutdown()) {
              logger.warn("KiraServer is already in shutdown state. So do not shutdown again.");
            } else {
              String errorMessage =
                  "Failed to shutdown KiraServer. It may not in started state. serverid="
                      + this.serverId + " and componentState=" + this.componentState;
              logger.error(errorMessage);
            }
          }
        }
      } finally {
        this.lockForComponentState.writeLock().unlock();
      }
    } catch (Throwable t) {
      logger
          .error("Error occurs when call shutdown() for KiraServer. serverId=" + this.serverId, t);
    }
  }

  @Override
  public void destroy() {
    try {
      logger.info("Calling destroy() for KiraServer... serverId={}", this.serverId);
      this.requestDestroy = true;

      long startTime = System.currentTimeMillis();
      this.lockForComponentState.writeLock().lock();
      try {
        if (!this.isShutdown()) {
          this.shutdown();
        }

        this.componentState.set(ComponentStateEnum.DESTROYING.getState());
        logger.info("Destroying kiraServer... serverId={}", this.serverId);

        if (null != this.doRoutineWorkOfKiraServerScheduledExecutorService) {
          this.doRoutineWorkOfKiraServerScheduledExecutorService.shutdown();
          this.doRoutineWorkOfKiraServerScheduledExecutorService = null;
        }
        if (null != this.kiraServerEventDispatcher) {
          this.kiraServerEventDispatcher.destroy();
          this.kiraServerEventDispatcher = null;
        }

        this.componentState.set(ComponentStateEnum.DESTROYED.getState());
        logger.info("Successfully destroy kiraServer. serverId={}", this.serverId);
      } catch (Exception e) {
        //always set the state to destroyed even if error occurs.
        this.componentState.set(ComponentStateEnum.DESTROYED.getState());
        logger.error("Error occurs when destroying KiraServer. serverId=" + this.serverId, e);
      } finally {
        this.lockForComponentState.writeLock().unlock();
        long costTime = System.currentTimeMillis() - startTime;
        logger.info("Finish destroy KiraServer. serverId={} and it takes {} milliseconds.",
            this.serverId, costTime);
      }
    } catch (Throwable t) {
      logger.error("Error occurs when call destroy() for KiraServer. serverId=" + this.serverId, t);
    } finally {
      logger.info("Finish call destroy() for kiraServer. serverId={}", this.serverId);
    }
  }

  @Override
  public KiraServerEntity getKiraServerEntityOfLeader(boolean needCheckKiraServerLeaderZNodeData)
      throws Exception {
    KiraServerEntity returnValue = null;

    KiraServerZNodeData kiraServerLeaderZNodeData = null;
    String kiraServerLeaderZNodePath = this.getKiraServerLeaderZNodePath();
    if (StringUtils.isNotBlank(kiraServerLeaderZNodePath)) {
      if (zkClient.exists(kiraServerLeaderZNodePath)) {
        kiraServerLeaderZNodeData = zkClient.readData(kiraServerLeaderZNodePath, true);
        if (null != kiraServerLeaderZNodeData) {
          if (needCheckKiraServerLeaderZNodeData) {
            boolean isKiraServerLeaderZNodeDataValid = this
                .isKiraServerLeaderZNodeDataValid(kiraServerLeaderZNodeData);
            if (!isKiraServerLeaderZNodeDataValid) {
              String errorMessage =
                  "kiraServerLeaderZNodeData is not valid now for getKiraServerEntityOfLeader. kiraServerLeaderZNodePath="
                      + kiraServerLeaderZNodePath + " and kiraServerLeaderZNodeData="
                      + kiraServerLeaderZNodeData;
              logger.warn(errorMessage);
              throw new KiraHandleException(errorMessage);
            }
          }
        }
      } else {
        String errorMessage =
            "znode do not exist on kiraServerLeaderZNodePath for getKiraServerEntityOfLeader. kiraServerLeaderZNodePath="
                + kiraServerLeaderZNodePath;
        logger.warn(errorMessage);
        throw new KiraHandleException(errorMessage);
      }
    } else {
      String errorMessage = "kiraServerLeaderZNodePath is null for getKiraServerEntityOfLeader.";
      logger.warn(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    if (null != kiraServerLeaderZNodeData) {
      returnValue = new KiraServerEntity(kiraServerLeaderZNodeData.getServerId(),
          kiraServerLeaderZNodeData.getHost(), kiraServerLeaderZNodeData.getPort(),
          kiraServerLeaderZNodeData.getAccessUrlAsString());
    } else {
      String errorMessage =
          "kiraServerLeaderZNodeData is null for getKiraServerEntityOfLeader. kiraServerLeaderZNodePath="
              + kiraServerLeaderZNodePath;
      logger.warn(errorMessage);
      throw new KiraHandleException(errorMessage);
    }

    return returnValue;
  }

  @Override
  public KiraServerEntity getKiraServerEntityByServerId(String serverId) throws Exception {
    KiraServerEntity returnValue = null;
    if (StringUtils.isNotBlank(serverId)) {
      if (zkClient.exists(this.kiraServersZNodeFullPath)) {
        List<String> kiraServerZNodeNameList = zkClient.getChildren(this.kiraServersZNodeFullPath);
        if (!CollectionUtils.isEmpty(kiraServerZNodeNameList)) {
          String kiraServerZNodeFullPath = null;
          KiraServerZNodeData kiraServerZNodeData = null;
          KiraServerEntity kiraServerEntity = null;
          String otherServerId = null;
          for (String kiraServerZNodeName : kiraServerZNodeNameList) {
            kiraServerZNodeFullPath = KiraUtil
                .getChildFullPath(this.kiraServersZNodeFullPath, kiraServerZNodeName);
            kiraServerZNodeData = zkClient.readData(kiraServerZNodeFullPath, true);
            if (null != kiraServerZNodeData) {
              otherServerId = kiraServerZNodeData.getServerId();
              if (serverId.equals(otherServerId)) {
                String host = kiraServerZNodeData.getHost();
                Integer port = kiraServerZNodeData.getPort();
                String accessUrlAsString = kiraServerZNodeData.getAccessUrlAsString();
                returnValue = new KiraServerEntity(serverId, host, port, accessUrlAsString);
                break;
              }
            }
          }
        }
      }
    }

    return returnValue;
  }

  @Override
  public String toString() {
    return "KiraServer [serverId=" + serverId + ", host=" + host
        + ", port=" + port + ", accessUrlAsString=" + accessUrlAsString
        + ", zkPathRootOfKiraServerCluster="
        + zkPathRootOfKiraServerCluster + ", kiraServersZNodeFullPath="
        + kiraServersZNodeFullPath + ", kiraServerLeaderZNodeFullPath="
        + kiraServerLeaderZNodeFullPath + ", componentState="
        + componentState + ", lastStartedTime="
        + KiraCommonUtils.getDateAsString(lastStartedTime) + ", lastShutdownServerTime="
        + KiraCommonUtils.getDateAsString(lastShutdownServerTime)
        + ", requestShutdown=" + requestShutdown
        + ", requestDestroy=" + requestDestroy
        + ", kiraServerZNodePath=" + kiraServerZNodePath
        + ", lastUpdateKiraServerZNodePathTime="
        + KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodePathTime)
        + ", kiraServerZNodeData="
        + kiraServerZNodeData + ", lastUpdateKiraServerZNodeDataTime="
        + KiraCommonUtils.getDateAsString(lastUpdateKiraServerZNodeDataTime) + ", kiraServerRole="
        + kiraServerRole + ", lastChangeKiraServerRoleTime="
        + KiraCommonUtils.getDateAsString(lastChangeKiraServerRoleTime) + ", allOtherKiraServers="
        + allOtherKiraServers + ", lastUpdateAllOtherKiraServersTime="
        + KiraCommonUtils.getDateAsString(lastUpdateAllOtherKiraServersTime) + "]";
  }

  private class KiraServersZkChildListener implements IZkChildListener {

    private Logger logger = LoggerFactory.getLogger(KiraServersZkChildListener.class);

    public KiraServersZkChildListener() {
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds)
        throws Exception {
      logger.info("The child Change event of kiraServers detected...");
      try {
        if (!KiraServer.this.requestShutdown && !KiraServer.this.requestDestroy) {
          long startTime = System.currentTimeMillis();
          try {
            logger.info("Starting handleChildChange for KiraServersZkChildListener...");
            boolean handleChildChangeForKiraServersZNodeSuccess = KiraServer.this
                .handleChildChangeForKiraServersZNode();
            if (handleChildChangeForKiraServersZNodeSuccess) {
              logger.info("Successfully handleChildChange for KiraServersZkChildListener.");
            } else {
              logger.error(
                  "Failed to handleChildChangeForKiraServersZNode for KiraServersZkChildListener.");
            }
          } catch (Exception e) {
            logger.error(
                "Error occurs when handleChildChange for KiraServersZkChildListener. kiraServerId="
                    + KiraServer.this.serverId, e);
          } finally {
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(
                "Finish handleChildChange for KiraServersZkChildListener. And it takes " + costTime
                    + " milliseconds.");
          }
        } else {
          logger.warn(
              "KiraServer may request shutdown or destroy. So do not handleChildChange for KiraServersZkChildListener. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
              KiraServer.this.serverId, KiraServer.this.componentState,
              KiraServer.this.requestShutdown, KiraServer.this.requestDestroy);
        }
      } catch (Exception e) {
        logger.error(
            "Error occurs during call handleChildChange() for KiraServersZkChildListener. kiraServerId="
                + KiraServer.this.serverId, e);
      }
    }
  }

  private class KiraServerLeaderZKChildListener implements IZkChildListener {

    private Logger logger = LoggerFactory.getLogger(KiraServerLeaderZKChildListener.class);

    public KiraServerLeaderZKChildListener() {
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds)
        throws Exception {
      logger.info(
          "The child Change event of kiraServerLeader detected...So will call checkAndSelfRepairIfNeeded for kira server now.");
      try {
        if (!KiraServer.this.requestShutdown && !KiraServer.this.requestDestroy) {
          KiraServer.this.lockForComponentState.readLock().lock();
          try {
            if (!KiraServer.this.requestShutdown && !KiraServer.this.requestDestroy) {
              if (KiraServer.this.isStarted()) {
                KiraServer.this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
                try {
                  long startTime = System.currentTimeMillis();
                  try {
                    logger
                        .info("Starting handleChildChange for KiraServerLeaderZKChildListener...");
                    KiraServerTrackContext kiraServerTrackContext = getCurrentKiraServerTrackContext();
                    boolean checkAndSelfRepairIfNeededSuccess = KiraServer.this
                        .checkAndSelfRepairIfNeeded();
                    if (checkAndSelfRepairIfNeededSuccess) {
                      KiraServer.this.setNewDataAndCalculateDifferenceForKiraServerTrackContext(
                          kiraServerTrackContext);
                      boolean dispatchKiraServerChangedEventIfNeededSuccess = KiraServer.this
                          .dispatchKiraServerChangedEventIfChanged(kiraServerTrackContext);
                      if (dispatchKiraServerChangedEventIfNeededSuccess) {
                        logger.info(
                            "Successfully handleChildChange for KiraServerLeaderZKChildListener.");
                      } else {
                        logger.error(
                            "Failed to dispatchKiraServerChangedEventIfChanged for KiraServerLeaderZKChildListener.");
                      }
                    } else {
                      logger.error(
                          "Failed to checkAndSelfRepairIfNeeded for KiraServerLeaderZKChildListener。");
                    }
                  } catch (Exception e) {
                    logger.error(
                        "Error occurs when handleChildChange for KiraServerLeaderZKChildListener(). kiraServerId="
                            + KiraServer.this.serverId, e);
                  } finally {
                    long costTime = System.currentTimeMillis() - startTime;
                    logger.info(
                        "Finish handleChildChange for KiraServerLeaderZKChildListener. And it takes "
                            + costTime + " milliseconds.");
                  }
                } finally {
                  KiraServer.this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
                }
              } else {
                logger.warn(
                    "KiraServer is not in the started state. So do not handleChildChange for KiraServerLeaderZKChildListener. serverId={} and componentState={}",
                    KiraServer.this.serverId, KiraServer.this.componentState);
              }
            } else {
              logger.warn(
                  "KiraServer may request shutdown or destroy. So do not continue to handleChildChange for KiraServerLeaderZKChildListener. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
                  KiraServer.this.serverId, KiraServer.this.componentState,
                  KiraServer.this.requestShutdown, KiraServer.this.requestDestroy);
            }
          } finally {
            KiraServer.this.lockForComponentState.readLock().unlock();
          }
        } else {
          logger.warn(
              "KiraServer may request shutdown or destroy. So do not handleChildChange in KiraServerLeaderZKChildListener. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
              KiraServer.this.serverId, KiraServer.this.componentState,
              KiraServer.this.requestShutdown, KiraServer.this.requestDestroy);
        }
      } catch (Exception e) {
        logger.error(
            "Error occurs during call handleChildChange() for KiraServerLeaderZKChildListener. kiraServerId="
                + KiraServer.this.serverId, e);
      }
    }
  }

  private class ZKStateChangesListenerForKiraServer implements IZkStateListener {

    private Logger logger = LoggerFactory.getLogger(ZKStateChangesListenerForKiraServer.class);

    public ZKStateChangesListenerForKiraServer() {
    }

    @Override
    public void handleStateChanged(KeeperState state) throws Exception {
      logger.info("Kira server: zk connection state change to:{} and kiraServerId={}",
          state.toString(), KiraServer.this.serverId);
    }

    @Override
    public void handleNewSession() throws Exception {
      String kiraServerid = KiraServer.this.serverId;
      String kiraServerZNodePath = KiraServer.this.kiraServerZNodePath;
      KiraServerZNodeData kiraServerZNodeData = KiraServer.this.kiraServerZNodeData;
      logger.info(
          "zookeeper session has expired and a new session has been created. kiraServerid={} and old kiraServerZNodePath={} and old kiraServerZNodeData={} and any ephemeral nodes may disappear. So will call checkAndSelfRepairIfNeeded for kira server now. ",
          kiraServerid, kiraServerZNodePath, kiraServerZNodeData);
      try {
        if (!KiraServer.this.requestShutdown && !KiraServer.this.requestDestroy) {
          KiraServer.this.lockForComponentState.readLock().lock();
          try {
            if (!KiraServer.this.requestShutdown && !KiraServer.this.requestDestroy) {
              if (KiraServer.this.isStarted()) {
                KiraServer.this.lockForKiraServerRuntimeDataManagement.writeLock().lock();
                try {
                  long startTime = System.currentTimeMillis();
                  try {
                    logger.info(
                        "Starting handleNewSession for ZKStateChangesListenerForKiraServer...");
                    KiraServerTrackContext kiraServerTrackContext = getCurrentKiraServerTrackContext();
                    boolean checkAndSelfRepairIfNeededSuccess = KiraServer.this
                        .checkAndSelfRepairIfNeeded();
                    if (checkAndSelfRepairIfNeededSuccess) {
                      KiraServer.this.setNewDataAndCalculateDifferenceForKiraServerTrackContext(
                          kiraServerTrackContext);
                      boolean dispatchKiraServerChangedEventIfNeededSuccess = KiraServer.this
                          .dispatchKiraServerChangedEventIfChanged(kiraServerTrackContext);
                      if (dispatchKiraServerChangedEventIfNeededSuccess) {
                        logger.info(
                            "Successfully handleNewSession for ZKStateChangesListenerForKiraServer.");
                      } else {
                        logger.error(
                            "Failed to dispatchKiraServerChangedEventIfChanged for ZKStateChangesListenerForKiraServer. serverId={} and componentState={}",
                            KiraServer.this.serverId, KiraServer.this.componentState);
                      }
                    } else {
                      logger.error(
                          "Failed to checkAndSelfRepairIfNeeded for ZKStateChangesListenerForKiraServer. serverId={} and componentState={}",
                          KiraServer.this.serverId, KiraServer.this.componentState);
                    }
                  } catch (Exception e) {
                    logger.error(
                        "Error occurs when handleNewSession() for ZKStateChangesListenerForKiraServer. kiraServerId="
                            + KiraServer.this.serverId, e);
                  } finally {
                    long costTime = System.currentTimeMillis() - startTime;
                    logger.info(
                        "Finish handleNewSession for ZKStateChangesListenerForKiraServer. And it takes "
                            + costTime + " milliseconds.");
                  }
                } finally {
                  KiraServer.this.lockForKiraServerRuntimeDataManagement.writeLock().unlock();
                }
              } else {
                logger.warn(
                    "KiraServer is not in the started state. So do not handleNewSession for ZKStateChangesListenerForKiraServer. serverId={} and componentState={}",
                    KiraServer.this.serverId, KiraServer.this.componentState);
              }
            } else {
              logger.warn(
                  "KiraServer may request shutdown or destroy. So do not continue to handleNewSession in ZKStateChangesListenerForKiraServer. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
                  KiraServer.this.serverId, KiraServer.this.componentState,
                  KiraServer.this.requestShutdown, KiraServer.this.requestDestroy);
            }
          } finally {
            KiraServer.this.lockForComponentState.readLock().unlock();
          }
        } else {
          logger.warn(
              "KiraServer may request shutdown or destroy. So do not handleNewSession for ZKStateChangesListenerForKiraServer. kiraServerId={} and componentState={} and requestShutdown={} and requestDestroy={}",
              KiraServer.this.serverId, KiraServer.this.componentState,
              KiraServer.this.requestShutdown, KiraServer.this.requestDestroy);
        }
      } catch (Exception e) {
        logger.error(
            "Error occurs during call handleNewSession() for ZKStateChangesListenerForKiraServer. kiraServerId="
                + KiraServer.this.serverId, e);
      }
    }
  }

  private class KiraServerDoRoutineWorkTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(KiraServerDoRoutineWorkTask.class);

    @Override
    public void run() {
      try {
        logger.info("Calling run() in KiraServerDoRoutineWorkTask...kiraServerId={}",
            KiraServer.this.serverId);
        KiraServer.this.doRoutineWorkOfKiraServer();
      } catch (Exception e) {
        logger.error("Error occurs when call run() in KiraServerDoRoutineWorkTask. kiraServerId="
            + KiraServer.this.serverId, e);
      } finally {
        logger.info("Finish call run() in KiraServerDoRoutineWorkTask. kiraServerId={}",
            KiraServer.this.serverId);
      }
    }
  }
}
