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
package com.yihaodian.architecture.kira.manager.service;

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.manager.criteria.KiraServerCriteria;
import com.yihaodian.architecture.kira.manager.dto.KiraServerDetailData;
import java.util.List;

public interface KiraServerService {

  List<KiraServerDetailData> getKiraServerDetailDataList(KiraServerCriteria kiraServerCriteria);

  //start admin usage
  public HandleResult startServer(KiraServerCriteria kiraServerCriteria);

  public HandleResult stopServer(KiraServerCriteria kiraServerCriteria);

  public HandleResult restartServer(KiraServerCriteria kiraServerCriteria);

  public HandleResult destroyServer(KiraServerCriteria kiraServerCriteria);

  public HandleResult recoverServer(KiraServerCriteria kiraServerCriteria);

  public HandleResult doLeaderRoutineWork();

  public HandleResult addServerIdToAssignedServerIdBlackList(KiraServerCriteria criteria);

  public void addServerIdToAssignedServerIdBlackList(String assignedServerIdInBlackList)
      throws Exception;

  public HandleResult removeServerIdFromAssignedServerIdBlackList(KiraServerCriteria criteria);

  public void removeServerIdFromAssignedServerIdBlackList(String assignedServerIdInBlackList)
      throws Exception;

  public HandleResult setAssignedServerIdBlackList(KiraServerCriteria criteria) throws Exception;

  public void setAssignedServerIdBlackList(String assignedServerIdBlackList) throws Exception;

  public HandleResult clearAssignedServerIdBlackList(KiraServerCriteria criteria) throws Exception;

  public void clearAssignedServerIdBlackList() throws Exception;

  public void loadBalanceForKiraTimerTriggerSchedule(Integer maxDoLoadBalanceRoundCount)
      throws Exception;
  //end admin usage
}
