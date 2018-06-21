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
package com.yihaodian.architecture.kira.manager.crossmultizone;

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneConstants;
import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneRoleEnum;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.common.zk.TriggerMetadataZNodeData;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerCrossMultiZoneUtils {

  private static final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private static Logger logger = LoggerFactory.getLogger(KiraManagerCrossMultiZoneUtils.class);

  public KiraManagerCrossMultiZoneUtils() {
    // TODO Auto-generated constructor stub
  }

  public static void prepareKiraManagerCrossMultiZoneUtils() throws Exception {
    KiraManagerCrossMultiZoneEventHandleComponent
        .getKiraManagerCrossMultiZoneEventHandleComponent();
  }

  public static boolean isMasterZone(boolean accurate) throws Exception {
    boolean returnValue = KiraManagerCrossMultiZoneEventHandleComponent
        .getKiraManagerCrossMultiZoneEventHandleComponent().isMasterZone(accurate);

    return returnValue;
  }

  public static KiraCrossMultiZoneRoleEnum getKiraCrossMultiZoneRole(boolean accurate)
      throws Exception {
    return KiraManagerCrossMultiZoneEventHandleComponent
        .getKiraManagerCrossMultiZoneEventHandleComponent().getKiraCrossMultiZoneRole(accurate);
  }

  public static Date getLastSetKiraCrossMultiZoneRoleTime() throws Exception {
    return KiraManagerCrossMultiZoneEventHandleComponent
        .getKiraManagerCrossMultiZoneEventHandleComponent().getLastSetKiraCrossMultiZoneRoleTime();
  }

  public static void destroyKiraManagerCrossMultiZoneUtils() {
    try {
      KiraManagerCrossMultiZoneEventHandleComponent
          .getKiraManagerCrossMultiZoneEventHandleComponent().destroy();
    } catch (Exception e) {
      logger.error("Error occurs when destroyKiraManagerCrossMultiZoneUtils.", e);
    }
  }

  public static void cascadeCrossZoneSaveTimerTriggerIfNeeded(String appId, String triggerId,
      TriggerMetadataZNodeData triggerMetadataZNodeData) {
    try {
      if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId) && (null
          != triggerMetadataZNodeData)) {
        Boolean copyFromMasterToSlaveZone = triggerMetadataZNodeData.getCopyFromMasterToSlaveZone();

        boolean isNeedToCascadeCrossZoneChanges = KiraManagerCrossMultiZoneUtils
            .isNeedToCascadeCrossZoneChanges(copyFromMasterToSlaveZone, true);
        if (isNeedToCascadeCrossZoneChanges) {
          try {
            logger.info(
                "Will cascadeCrossZoneSaveTimerTrigger to other zones. appId={} and triggerId={} and triggerMetadataZNodeData={}",
                appId, triggerId, KiraCommonUtils.toString(triggerMetadataZNodeData));

            //Always need to save poolId first although may no pool added because createParents is true by default which may cause empty poolId.
            String zkFullPathOfPoolId = KiraCommonUtils.getPoolZNodeZKPathForTrigger(appId);
            KiraManagerCrossMultiZoneUtils
                .saveCrossZonePersistent(zkFullPathOfPoolId, appId, true, true);

            String zkFullPathOfTimerTrigger = KiraCommonUtils
                .getTriggerZNodeZKPath(appId, triggerId);
            //TriggerMetadataZNodeData existTriggerMetadataZNodeData = ZkUtil.getZkClientInstance().readData(zkFullPath,true);
            KiraManagerCrossMultiZoneUtils
                .saveCrossZonePersistent(zkFullPathOfTimerTrigger, triggerMetadataZNodeData, true,
                    true);
          } finally {
            logger.info("Finish cascadeCrossZoneSaveTimerTrigger to other zones.");
          }
        }
      } else {
        logger.error(
            "poolId and triggerId and triggerMetadataZNodeData should be both not blank or null for cascadeCrossZoneSaveTimerTriggerIfNeeded. poolId={} and triggerId={} and triggerMetadataZNodeData={}",
            appId, triggerId, KiraCommonUtils.toString(triggerMetadataZNodeData));
      }
    } catch (Throwable t) {
      logger.error("Error occurs when cascadeCrossZoneSaveTimerTriggerIfNeeded. appId=" + appId
          + " and triggerId=" + triggerId + " and triggerMetadataZNodeData=" + KiraCommonUtils
          .toString(triggerMetadataZNodeData), t);
    }
  }

  public static void cascadeCrossZoneDeleteTimerTriggerIfNeeded(String appId, String triggerId,
      Boolean copyFromMasterToSlaveZone) {
    try {
      if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(triggerId)) {
        boolean isNeedToCascadeCrossZoneChanges = KiraManagerCrossMultiZoneUtils
            .isNeedToCascadeCrossZoneChanges(copyFromMasterToSlaveZone, true);
        if (isNeedToCascadeCrossZoneChanges) {
          try {
            logger.info(
                "Will cascadeCrossZoneDeleteTimerTrigger to other zones. appId={} and triggerId={} and copyFromMasterToSlaveZone={}",
                appId, triggerId, copyFromMasterToSlaveZone);

            String zkFullPath = KiraCommonUtils.getTriggerZNodeZKPath(appId, triggerId);
            KiraManagerCrossMultiZoneUtils.deleteCrossZonePathRecursive(zkFullPath, true);
          } finally {
            logger.info("Finish cascadeCrossZoneDeleteTimerTrigger to other zones.");
          }
        }
      } else {
        logger.error(
            "poolId and triggerId should be both not blank for cascadeCrossZoneDeleteTimerTriggerIfNeeded. appId={} and triggerId={}",
            appId, triggerId);
      }
    } catch (Throwable t) {
      logger.error("Error occurs when cascadeCrossZoneDeleteTimerTriggerIfNeeded. appId=" + appId
          + " and triggerId=" + triggerId, t);
    }
  }

  public static void cascadeCrossZoneSavePoolOfTimerTriggerIfNeeded(String poolId,
      Boolean copyFromMasterToSlaveZone) {
    try {
      if (StringUtils.isNotBlank(poolId)) {
        boolean isNeedToCascadeCrossZoneChanges = KiraManagerCrossMultiZoneUtils
            .isNeedToCascadeCrossZoneChanges(copyFromMasterToSlaveZone, true);
        if (isNeedToCascadeCrossZoneChanges) {
          try {
            logger.info(
                "Will cascadeCrossZoneSavePoolOfTimerTrigger to other zones. poolId={} and copyFromMasterToSlaveZone={}",
                poolId, copyFromMasterToSlaveZone);

            String zkFullPath = KiraCommonUtils.getPoolZNodeZKPathForTrigger(poolId);
            KiraManagerCrossMultiZoneUtils.saveCrossZonePersistent(zkFullPath, poolId, true, true);
          } finally {
            logger.info("Finish cascadeCrossZoneSavePoolOfTimerTrigger to other zones.");
          }
        }
      } else {
        logger.error("poolId is blank for cascadeCrossZoneSavePoolOfTimerTriggerIfNeeded.");
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when cascadeCrossZoneSavePoolOfTimerTriggerIfNeeded. poolId=" + poolId, t);
    }
  }

  public static void cascadeCrossZoneDeletePoolOfTimerTriggerIfNeeded(String poolId,
      Boolean copyFromMasterToSlaveZone, boolean onlyCascadeCrossZoneChangesOnMasterZone) {
    try {
      if (StringUtils.isNotBlank(poolId)) {
        boolean isNeedToCascadeCrossZoneChanges = KiraManagerCrossMultiZoneUtils
            .isNeedToCascadeCrossZoneChanges(copyFromMasterToSlaveZone,
                onlyCascadeCrossZoneChangesOnMasterZone);
        if (isNeedToCascadeCrossZoneChanges) {
          try {
            logger.info(
                "Will cascadeCrossZoneDeletePoolOfTimerTrigger to other zones. poolId={} and copyFromMasterToSlaveZone={} and onlyCascadeCrossZoneChangesOnMasterZone={}",
                poolId, copyFromMasterToSlaveZone, onlyCascadeCrossZoneChangesOnMasterZone);

            String zkFullPath = KiraCommonUtils.getPoolZNodeZKPathForTrigger(poolId);
            KiraManagerCrossMultiZoneUtils.deleteCrossZonePathRecursive(zkFullPath, true);
          } finally {
            logger.info("Finish cascadeCrossZoneDeletePoolOfTimerTrigger to other zones.");
          }
        }
      } else {
        logger.error("poolId is blank for cascadeCrossZoneDeletePoolOfTimerTriggerIfNeeded.");
      }
    } catch (Throwable t) {
      logger.error(
          "Error occurs when cascadeCrossZoneDeletePoolOfTimerTriggerIfNeeded. poolId=" + poolId,
          t);
    }
  }

  public static void setKiraMasterZone(String newKiraMasterZone) {
    long startTime = System.currentTimeMillis();
    try {
      KiraManagerCrossMultiZoneUtils
          .saveCrossZonePersistent(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE,
              newKiraMasterZone, true, false);
      logger.warn("Successfully call setKiraMasterZone. newKiraMasterZone={}", newKiraMasterZone);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger
          .warn("Finish call setKiraMasterZone. newKiraMasterZone={} and it takes {} milliseconds.",
              newKiraMasterZone, costTime);
    }
  }

  private static void saveCrossZonePersistent(String path, Object data, boolean createParents,
      boolean ignoreLocalZone) {
    long startTime = System.currentTimeMillis();
    try {
      //ZoneSwitcherUtil.getZKAssist().createCrossZonePersistent(path, data, true, ignoreLocalZone);
      //ZoneSwitcherUtil.getZKAssist().writeCrossZoneData(path,data);
      logger.warn("Successfully call saveCrossZonePersistent. path={}", path);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn(
          "Finish call saveCrossZonePersistent. path={} and data={} and createParents={} and ignoreLocalZone={} and it takes {} milliseconds.",
          path, KiraCommonUtils.toString(data), createParents, ignoreLocalZone, costTime);
    }
  }

  private static void deleteCrossZonePathRecursive(String path, boolean ignoreLocalZone) {
    long startTime = System.currentTimeMillis();
    try {
      //ZoneSwitcherUtil.getZKAssist().deleteCrossZonePathRecursive(path, ignoreLocalZone);
      logger.warn("Successfully call deleteCrossZonePathRecursive. path={}", path);
    } finally {
      long costTime = System.currentTimeMillis() - startTime;
      logger.warn(
          "Finish call deleteCrossZonePathRecursive. path={} and ignoreLocalZone={} and it takes {} milliseconds.",
          path, ignoreLocalZone, costTime);
    }
  }

  private static boolean isNeedToCascadeCrossZoneChanges(Boolean copyFromMasterToSlaveZone,
      boolean onlyCascadeCrossZoneChangesOnMasterZone) throws Exception {
    boolean returnValue = true;
    if (copyFromMasterToSlaveZone) {
      if (onlyCascadeCrossZoneChangesOnMasterZone) {
        boolean isMasterZone = KiraManagerCrossMultiZoneUtils.isMasterZone(false);
        returnValue = isMasterZone;
      }
    } else {
      returnValue = false;
    }

    return returnValue;
  }

  public static void destroyZoneSwitcherZKAssist() {
    try {
      //ZoneSwitcherUtil.getZKAssist().destroy();
    } catch (Exception e) {
      logger.error("Error occurs when destroyZoneSwitcherZKAssist.", e);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

}
