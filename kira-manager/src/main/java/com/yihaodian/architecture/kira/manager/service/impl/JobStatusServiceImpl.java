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

import com.yihaodian.architecture.kira.manager.criteria.JobStatusCriteria;
import com.yihaodian.architecture.kira.manager.dao.JobStatusDao;
import com.yihaodian.architecture.kira.manager.domain.JobStatus;
import com.yihaodian.architecture.kira.manager.service.JobStatusService;
import com.yihaodian.architecture.kira.manager.service.Service;
import java.util.List;

public class JobStatusServiceImpl extends Service implements JobStatusService {

  private JobStatusDao jobStatusDao;

  public void setJobStatusDao(JobStatusDao jobStatusDao) {
    this.jobStatusDao = jobStatusDao;
  }

  public void insert(JobStatus jobStatus) {
    jobStatusDao.insert(jobStatus);
  }

  public int update(JobStatus jobStatus) {
    int actualRowsAffected = 0;

    Integer id = jobStatus.getId();

    JobStatus _oldJobStatus = jobStatusDao.select(id);

    if (_oldJobStatus != null) {
      actualRowsAffected = jobStatusDao.update(jobStatus);
    }

    return actualRowsAffected;
  }

  public int delete(Integer id) {
    int actualRowsAffected = 0;

    JobStatus _oldJobStatus = jobStatusDao.select(id);

    if (_oldJobStatus != null) {
      actualRowsAffected = jobStatusDao.delete(id);
    }

    return actualRowsAffected;
  }

  public JobStatus select(Integer id) {
    return jobStatusDao.select(id);
  }

  public List<JobStatus> list(JobStatusCriteria jobStatusCriteria) {
    return jobStatusDao.list(jobStatusCriteria);
  }

  public List<JobStatus> listOnPage(JobStatusCriteria jobStatusCriteria) {
    return jobStatusDao.listOnPage(jobStatusCriteria);
  }

}
