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
package com.yihaodian.architecture.kira.manager.dao;

import com.yihaodian.architecture.kira.manager.criteria.UpgradeRoadmapCriteria;
import com.yihaodian.architecture.kira.manager.domain.UpgradeRoadmap;
import java.util.List;
import org.springframework.dao.DataAccessException;

public interface UpgradeRoadmapDao {

  void insert(UpgradeRoadmap upgradeRoadmap) throws DataAccessException;

  int update(UpgradeRoadmap upgradeRoadmap) throws DataAccessException;

  int delete(Long id) throws DataAccessException;

  UpgradeRoadmap select(Long id) throws DataAccessException;

  List<UpgradeRoadmap> list(UpgradeRoadmapCriteria upgradeRoadmapCriteria)
      throws DataAccessException;

  List<UpgradeRoadmap> listOnPage(UpgradeRoadmapCriteria upgradeRoadmapCriteria)
      throws DataAccessException;

  int count(UpgradeRoadmapCriteria upgradeRoadmapCriteria) throws DataAccessException;

}
