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

import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.TriggerIdentity;
import com.yihaodian.architecture.kira.manager.criteria.JobRunStatisticsCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobRunStatisticsDao;
import com.yihaodian.architecture.kira.manager.domain.JobRunStatistics;
import com.yihaodian.architecture.kira.manager.service.JobRunStatisticsService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class JobRunStatisticsServiceImpl extends Service implements JobRunStatisticsService {

  private JobRunStatisticsDao jobRunStatisticsDao;

  private TriggerMetadataService triggerMetadataService;

  public void setJobRunStatisticsDao(JobRunStatisticsDao jobRunStatisticsDao) {
    this.jobRunStatisticsDao = jobRunStatisticsDao;
  }

  public void setTriggerMetadataService(TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public void insert(JobRunStatistics jobRunStatistics) {
    jobRunStatisticsDao.insert(jobRunStatistics);
  }

  public int update(JobRunStatistics jobRunStatistics) {
    int actualRowsAffected = 0;

    Long id = jobRunStatistics.getId();

    JobRunStatistics _oldJobRunStatistics = jobRunStatisticsDao.select(id);

    if (_oldJobRunStatistics != null) {
      actualRowsAffected = jobRunStatisticsDao.update(jobRunStatistics);
    }

    return actualRowsAffected;
  }

  public int delete(Long id) {
    int actualRowsAffected = 0;

    JobRunStatistics _oldJobRunStatistics = jobRunStatisticsDao.select(id);

    if (_oldJobRunStatistics != null) {
      actualRowsAffected = jobRunStatisticsDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobRunStatistics select(Long id) {
    return jobRunStatisticsDao.select(id);
  }

  public List<JobRunStatistics> list(JobRunStatisticsCriteria jobRunStatisticsCriteria) {
    return jobRunStatisticsDao.list(jobRunStatisticsCriteria);
  }

  public List<JobRunStatistics> listOnPage(JobRunStatisticsCriteria jobRunStatisticsCriteria) {
    return jobRunStatisticsDao.listOnPage(jobRunStatisticsCriteria);
  }

  @Override
  public JobRunStatistics getJobRunStatistics(String appId, String triggerId) {
    JobRunStatistics returnValue = null;
    JobRunStatisticsCriteria jobRunStatisticsCriteria = new JobRunStatisticsCriteria();
    jobRunStatisticsCriteria.setAppId(appId);
    jobRunStatisticsCriteria.setTriggerId(triggerId);
    List<JobRunStatistics> list = this.listOnPage(jobRunStatisticsCriteria);
    if (CollectionUtils.isNotEmpty(list)) {
      returnValue = list.get(0);
    }
    return returnValue;
  }

  @Override
  public void doJobRunStatistics(Date beginTime, Date endTime, Integer maxSampleCount)
      throws Exception {
    if (null == beginTime || null == endTime || null == maxSampleCount) {
      throw new IllegalArgumentException(
          "beginTime and endTime and maxSampleCount should not be null. beginTime="
              + KiraCommonUtils.getDateAsString(beginTime) + " and endTime=" + KiraCommonUtils
              .getDateAsString(endTime) + " and maxSampleCount=" + maxSampleCount);
    }

    List<TriggerIdentity> allRegisteredAndUnDeletedTriggerIdentityInDB = this.triggerMetadataService
        .getAllRegisteredAndUnDeletedTriggerIdentityInDB(false);
    if (CollectionUtils.isNotEmpty(allRegisteredAndUnDeletedTriggerIdentityInDB)) {
      String oneAppId = null;
      String oneTriggerId = null;
      for (TriggerIdentity oneTriggerIdentity : allRegisteredAndUnDeletedTriggerIdentityInDB) {
        oneAppId = oneTriggerIdentity.getAppId();
        oneTriggerId = oneTriggerIdentity.getTriggerId();
        Date now = new Date();
        this.jobRunStatisticsDao
            .doJobRunStatistics(beginTime, endTime, oneAppId, oneTriggerId, maxSampleCount, now);
      }
    }
  }

}
