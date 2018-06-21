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
package com.yihaodian.architecture.kira.manager.service.impl;

import com.yihaodian.architecture.kira.manager.criteria.UpgradeRoadmapCriteria;
import com.yihaodian.architecture.kira.manager.dao.UpgradeRoadmapDao;
import com.yihaodian.architecture.kira.manager.domain.UpgradeRoadmap;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.UpgradeRoadmapService;
import com.yihaodian.architecture.kira.manager.util.UpgradeRoadmapConstants;
import java.util.Date;
import java.util.List;

public class UpgradeRoadmapServiceImpl extends Service implements UpgradeRoadmapService {

  private UpgradeRoadmapDao upgradeRoadmapDao;

  public void setUpgradeRoadmapDao(UpgradeRoadmapDao upgradeRoadmapDao) {
    this.upgradeRoadmapDao = upgradeRoadmapDao;
  }

  public void insert(UpgradeRoadmap upgradeRoadmap) {
    upgradeRoadmapDao.insert(upgradeRoadmap);
  }

  public int update(UpgradeRoadmap upgradeRoadmap) {
    int actualRowsAffected = 0;

    Long id = upgradeRoadmap.getId();

    UpgradeRoadmap _oldUpgradeRoadmap = upgradeRoadmapDao.select(id);

    if (_oldUpgradeRoadmap != null) {
      actualRowsAffected = upgradeRoadmapDao.update(upgradeRoadmap);
    }

    return actualRowsAffected;
  }

  public int delete(Long id) {
    int actualRowsAffected = 0;

    UpgradeRoadmap _oldUpgradeRoadmap = upgradeRoadmapDao.select(id);

    if (_oldUpgradeRoadmap != null) {
      actualRowsAffected = upgradeRoadmapDao.delete(id);
    }

    return actualRowsAffected;
  }

  public UpgradeRoadmap select(Long id) {
    return upgradeRoadmapDao.select(id);
  }

  public List<UpgradeRoadmap> list(UpgradeRoadmapCriteria upgradeRoadmapCriteria) {
    return upgradeRoadmapDao.list(upgradeRoadmapCriteria);
  }

  public List<UpgradeRoadmap> listOnPage(UpgradeRoadmapCriteria upgradeRoadmapCriteria) {
    return upgradeRoadmapDao.listOnPage(upgradeRoadmapCriteria);
  }

  @Override
  public int count(UpgradeRoadmapCriteria upgradeRoadmapCriteria) {
    return upgradeRoadmapDao.count(upgradeRoadmapCriteria);
  }

  @Override
  public boolean isHasMigrateQuartzScheduleDataSuccessRecord() {
    boolean returnValue = false;

    UpgradeRoadmapCriteria upgradeRoadmapCriteria = new UpgradeRoadmapCriteria();
    upgradeRoadmapCriteria
        .setName(UpgradeRoadmapConstants.UPGRADE_ROADMAP_NAME_MIGRATE_QUARTZ_SCHEDULE_DATA_SUCCESS);
    int count = this.count(upgradeRoadmapCriteria);
    if (count > 0) {
      returnValue = true;
    }

    return returnValue;
  }

  @Override
  public void insertMigratingQuartzScheduleDataRecord() {
    UpgradeRoadmap upgradeRoadmap = new UpgradeRoadmap();
    upgradeRoadmap
        .setName(UpgradeRoadmapConstants.UPGRADE_ROADMAP_NAME_MIGRATING_QUARTZ_SCHEDULE_DATA);
    upgradeRoadmap.setCreateTime(new Date());
    upgradeRoadmapDao.insert(upgradeRoadmap);
  }

  @Override
  public void insertMigrateQuartzScheduleDataSuccessRecord() {
    UpgradeRoadmap upgradeRoadmap = new UpgradeRoadmap();
    upgradeRoadmap
        .setName(UpgradeRoadmapConstants.UPGRADE_ROADMAP_NAME_MIGRATE_QUARTZ_SCHEDULE_DATA_SUCCESS);
    upgradeRoadmap.setCreateTime(new Date());
    upgradeRoadmapDao.insert(upgradeRoadmap);
  }

  @Override
  public void insertMigrateQuartzScheduleDataFailedRecord(String exceptionDesc) {
    UpgradeRoadmap upgradeRoadmap = new UpgradeRoadmap();
    upgradeRoadmap
        .setName(UpgradeRoadmapConstants.UPGRADE_ROADMAP_NAME_MIGRATE_QUARTZ_SCHEDULE_DATA_FAILED);
    upgradeRoadmap.setCreateTime(new Date());
    upgradeRoadmap.setUpgradeDetails(exceptionDesc);
    upgradeRoadmapDao.insert(upgradeRoadmap);
  }

}
