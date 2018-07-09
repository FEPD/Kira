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
package com.yihaodian.architecture.kira.manager.action;

import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.util.KiraUtil;
import com.yihaodian.architecture.kira.common.util.KiraZkUtil;
import com.yihaodian.architecture.kira.manager.criteria.OtherCriteria;
import com.yihaodian.architecture.kira.manager.crossmultizone.KiraZoneContextData;
import com.yihaodian.architecture.kira.manager.dto.RunningEnvironmentForTimerTrigger;
import com.yihaodian.architecture.kira.manager.service.OtherService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.Utils;
import com.yihaodian.architecture.kira.manager.util.WebUtil;
import com.yihaodian.architecture.zkclient.ZkClient;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.util.CollectionUtils;

public class OtherAction extends BaseAction {

  private static final long serialVersionUID = 1L;
  private final ZkClient zkClient = KiraZkUtil.initDefaultZk();
  private OtherCriteria criteria = new OtherCriteria();
  private transient OtherService otherService;

  public OtherAction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public OtherCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(OtherCriteria criteria) {
    this.criteria = criteria;
  }

  public OtherService getOtherService() {
    return otherService;
  }

  public void setOtherService(OtherService otherService) {
    this.otherService = otherService;
  }

  public String getCurrentServerIp() throws Exception {
    String localhostIp = SystemUtil.getLocalhostIp();
    Utils.sendHttpResponseForStruts2(null, localhostIp);
    return null;
  }

  public String getKiraZoneContextData() throws Exception {
    KiraZoneContextData KiraZoneContextData = otherService.getKiraZoneContextData();
    Utils.sendHttpResponseForStruts2(null, KiraZoneContextData);
    return null;
  }

  public String getChildsUnderTriggersZNode() throws Exception {
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> list = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      Utils.sendHttpResponseForStruts2(null, list);
    }
    return null;
  }

  public String getPoolsZNodeValueUnderTriggersZNode() throws Exception {
    List<String> poolsZNodeValueUnderTriggersZNodeList = new ArrayList<String>();
    if (zkClient.exists(KiraCommonConstants.ZK_PATH_TRIGGERS)) {
      List<String> poolShortPathList = zkClient.getChildren(KiraCommonConstants.ZK_PATH_TRIGGERS);
      if (!CollectionUtils.isEmpty(poolShortPathList)) {
        String onePoolPath = null;
        for (String onePoolShortPath : poolShortPathList) {
          onePoolPath = KiraUtil
              .getChildFullPath(KiraCommonConstants.ZK_PATH_TRIGGERS, onePoolShortPath);
          Object poolZNodeData = zkClient.readData(onePoolPath, true);
          if (poolZNodeData instanceof String) {
            //it is of type String. regard it as poolZNode
            String poolIdValueOfPoolZNode = (String) poolZNodeData;
            poolsZNodeValueUnderTriggersZNodeList.add(poolIdValueOfPoolZNode);
          } else {
            logger.warn("poolZNodeData={} under onePoolPath={}", poolZNodeData, onePoolPath);
          }
        }
      }
    }
    Utils.sendHttpResponseForStruts2(null, poolsZNodeValueUnderTriggersZNodeList);
    return null;
  }

  public String getAdminUserNames() throws Exception {
    String adminUserNames = KiraManagerUtils.getAdminUserNames();
    Utils.sendHttpResponseForStruts2(null, adminUserNames);
    return null;
  }

  public String setAdminUserNames() throws Exception {
    String adminUserNames = criteria.getAdminUserNames();
    KiraManagerUtils.setAdminUserNames(adminUserNames);
    String newAdminUserNames = KiraManagerUtils.getAdminUserNames();
    Utils.sendHttpResponseForStruts2(null, newAdminUserNames);
    return null;
  }

  public String getPoolValueNeedRectifiedPoolsUnderTriggers() throws Exception {
    List<String> poolValueNeedRectifiedPoolsUnderTriggers = otherService
        .getPoolValueNeedRectifiedPoolsUnderTriggers();
    Utils.sendHttpResponseForStruts2(null, poolValueNeedRectifiedPoolsUnderTriggers);
    return null;
  }

  public String rectifyValueOfPoolNodesUnderTriggers() throws Exception {
    HandleResult handleResult = otherService.rectifyValueOfPoolNodesUnderTriggers();
    Utils.sendHttpResponseForStruts2(null, handleResult);
    return null;
  }

  public String isNeedArchiveJobRuntimeData() throws Exception {
    boolean isNeedArchiveJobRuntimeData = KiraManagerUtils.isNeedArchiveJobRuntimeData();
    Utils.sendHttpResponseForStruts2(null, isNeedArchiveJobRuntimeData);
    return null;
  }

  public String setNeedArchiveJobRuntimeData() throws Exception {
    boolean paramValue = criteria.isNeedArchiveJobRuntimeData();
    KiraManagerUtils.setNeedArchiveJobRuntimeData(paramValue);
    boolean isNeedArchiveJobRuntimeData = KiraManagerUtils.isNeedArchiveJobRuntimeData();
    Utils.sendHttpResponseForStruts2(null, isNeedArchiveJobRuntimeData);
    return null;
  }

  public String getMinutesToKeepJobRuntimeData() throws Exception {
    int minutesToKeepJobRuntimeData = KiraManagerUtils.getMinutesToKeepJobRuntimeData();
    Utils.sendHttpResponseForStruts2(null, minutesToKeepJobRuntimeData);
    return null;
  }

  public String setMinutesToKeepJobRuntimeData() throws Exception {
    int paramValue = criteria.getMinutesToKeepJobRuntimeData();
    KiraManagerUtils.setMinutesToKeepJobRuntimeData(paramValue);
    int minutesToKeepJobRuntimeData = KiraManagerUtils.getMinutesToKeepJobRuntimeData();
    Utils.sendHttpResponseForStruts2(criteria, minutesToKeepJobRuntimeData);
    return null;
  }

  public String getMinutesPerTimeToHandleJobRuntimeData() throws Exception {
    int minutesPerTimeToHandleJobRuntimeData = KiraManagerUtils
        .getMinutesPerTimeToHandleJobRuntimeData();
    Utils.sendHttpResponseForStruts2(null, minutesPerTimeToHandleJobRuntimeData);
    return null;
  }

  public String setMinutesPerTimeToHandleJobRuntimeData() throws Exception {
    int paramValue = criteria.getMinutesPerTimeToHandleJobRuntimeData();
    KiraManagerUtils.setMinutesPerTimeToHandleJobRuntimeData(paramValue);
    int minutesPerTimeToHandleJobRuntimeData = KiraManagerUtils
        .getMinutesPerTimeToHandleJobRuntimeData();
    Utils.sendHttpResponseForStruts2(criteria, minutesPerTimeToHandleJobRuntimeData);
    return null;
  }

  public String getSleepTimeBeforeRunNextTaskInMilliseconds() throws Exception {
    long sleepTimeBeforeRunNextTaskInMilliseconds = KiraManagerUtils
        .getSleepTimeBeforeRunNextTaskInMilliseconds();
    Utils.sendHttpResponseForStruts2(null, sleepTimeBeforeRunNextTaskInMilliseconds);
    return null;
  }

  public String setSleepTimeBeforeRunNextTaskInMilliseconds() throws Exception {
    long paramValue = criteria.getSleepTimeBeforeRunNextTaskInMilliseconds();
    KiraManagerUtils.setSleepTimeBeforeRunNextTaskInMilliseconds(paramValue);
    long sleepTimeBeforeRunNextTaskInMilliseconds = KiraManagerUtils
        .getSleepTimeBeforeRunNextTaskInMilliseconds();
    Utils.sendHttpResponseForStruts2(criteria, sleepTimeBeforeRunNextTaskInMilliseconds);
    return null;
  }

  public String getTimeOutPerTaskInMilliseconds() throws Exception {
    long timeOutPerTaskInMilliseconds = KiraManagerUtils.getTimeOutPerTaskInMilliseconds();
    Utils.sendHttpResponseForStruts2(null, timeOutPerTaskInMilliseconds);
    return null;
  }

  public String setTimeOutPerTaskInMilliseconds() throws Exception {
    long paramValue = criteria.getTimeOutPerTaskInMilliseconds();
    KiraManagerUtils.setTimeOutPerTaskInMilliseconds(paramValue);
    long timeOutPerTaskInMilliseconds = KiraManagerUtils.getTimeOutPerTaskInMilliseconds();
    Utils.sendHttpResponseForStruts2(criteria, timeOutPerTaskInMilliseconds);
    return null;
  }

  public String getPrivateEmails() throws Exception {
    String privateEmails = KiraManagerUtils.getPrivateEmails();
    Utils.sendHttpResponseForStruts2(null, privateEmails);
    return null;
  }

  public String setPrivateEmails() throws Exception {
    String privateEmails = criteria.getPrivateEmails();
    KiraManagerUtils.setPrivateEmails(privateEmails);
    String newPrivateEmails = KiraManagerUtils.getPrivateEmails();
    Utils.sendHttpResponseForStruts2(null, newPrivateEmails);
    return null;
  }

  public String getPrivatePhoneNumbers() throws Exception {
    String privatePhoneNumbers = KiraManagerUtils.getPrivatePhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, privatePhoneNumbers);
    return null;
  }

  public String setPrivatePhoneNumbers() throws Exception {
    String privatePhoneNumbers = criteria.getPrivatePhoneNumbers();
    KiraManagerUtils.setPrivatePhoneNumbers(privatePhoneNumbers);
    String newPrivatePhoneNumbers = KiraManagerUtils.getPrivatePhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, newPrivatePhoneNumbers);
    return null;
  }

  public String getAdminEmails() throws Exception {
    String adminEmails = KiraManagerUtils.getAdminEmails();
    Utils.sendHttpResponseForStruts2(null, adminEmails);
    return null;
  }

  public String setAdminEmails() throws Exception {
    String adminEmails = criteria.getAdminEmails();
    KiraManagerUtils.setAdminEmails(adminEmails);
    String newAdminEmails = KiraManagerUtils.getAdminEmails();
    Utils.sendHttpResponseForStruts2(null, newAdminEmails);
    return null;
  }

  public String getAdminPhoneNumbers() throws Exception {
    String adminPhoneNumbers = KiraManagerUtils.getAdminPhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, adminPhoneNumbers);
    return null;
  }

  public String setAdminPhoneNumbers() throws Exception {
    String adminPhoneNumbers = criteria.getAdminPhoneNumbers();
    KiraManagerUtils.setAdminPhoneNumbers(adminPhoneNumbers);
    String newAdminPhoneNumbers = KiraManagerUtils.getAdminPhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, newAdminPhoneNumbers);
    return null;
  }

  public String getHealthEventReceiverEmails() throws Exception {
    String healthEventReceiverEmails = KiraManagerUtils.getHealthEventReceiverEmails();
    Utils.sendHttpResponseForStruts2(null, healthEventReceiverEmails);
    return null;
  }

  public String setHealthEventReceiverEmails() throws Exception {
    String healthEventReceiverEmails = criteria.getHealthEventReceiverEmails();
    KiraManagerUtils.setHealthEventReceiverEmails(healthEventReceiverEmails);
    String newHealthEventReceiverEmails = KiraManagerUtils.getHealthEventReceiverEmails();
    Utils.sendHttpResponseForStruts2(null, newHealthEventReceiverEmails);
    return null;
  }

  public String getHealthEventReceiverPhoneNumbers() throws Exception {
    String healthEventReceiverPhoneNumbers = KiraManagerUtils.getHealthEventReceiverPhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, healthEventReceiverPhoneNumbers);
    return null;
  }

  public String setHealthEventReceiverPhoneNumbers() throws Exception {
    String healthEventReceiverPhoneNumbers = criteria.getHealthEventReceiverPhoneNumbers();
    KiraManagerUtils.setHealthEventReceiverPhoneNumbers(healthEventReceiverPhoneNumbers);
    String newHealthEventReceiverPhoneNumbers = KiraManagerUtils
        .getHealthEventReceiverPhoneNumbers();
    Utils.sendHttpResponseForStruts2(null, newHealthEventReceiverPhoneNumbers);
    return null;
  }

  public String getChildsUnderZNnodeFullPath() throws Exception {
    String znodeFullPath = criteria.getZnodeFullPath();
    if (StringUtils.isNotBlank(znodeFullPath)) {
      if (zkClient.exists(znodeFullPath)) {
        List<String> list = zkClient.getChildren(znodeFullPath);
        Utils.sendHttpResponseForStruts2(null, list);
      } else {
        Utils.sendHttpResponseForStruts2(null,
            "znodeFullPath do not exist. znodeFullPath=" + znodeFullPath);
      }
    } else {
      Utils.sendHttpResponseForStruts2(null, "znodeFullPath should not be blank.");
    }
    return null;
  }

  public String getZNodeValueOfZNnodeFullPath() throws Exception {
    String znodeFullPath = criteria.getZnodeFullPath();
    if (StringUtils.isNotBlank(znodeFullPath)) {
      if (zkClient.exists(znodeFullPath)) {
        Object znodeData = zkClient.readData(znodeFullPath, true);
        Utils.sendHttpResponseForStruts2(null, KiraCommonUtils.toString(znodeData));
      } else {
        Utils.sendHttpResponseForStruts2(null,
            "znodeFullPath do not exist. znodeFullPath=" + znodeFullPath);
      }
    } else {
      Utils.sendHttpResponseForStruts2(null, "znodeFullPath should not be blank.");
    }

    return null;
  }

  public String getJobTimeoutHandleFailedMaxCount() throws Exception {
    int jobTimeoutHandleFailedMaxCount = KiraManagerUtils.getJobTimeoutHandleFailedMaxCount();
    Utils.sendHttpResponseForStruts2(null, jobTimeoutHandleFailedMaxCount);
    return null;
  }

  public String setJobTimeoutHandleFailedMaxCount() throws Exception {
    int paramValue = criteria.getJobTimeoutHandleFailedMaxCount();
    KiraManagerUtils.setJobTimeoutHandleFailedMaxCount(paramValue);
    int jobTimeoutHandleFailedMaxCount = KiraManagerUtils.getJobTimeoutHandleFailedMaxCount();
    Utils.sendHttpResponseForStruts2(criteria, jobTimeoutHandleFailedMaxCount);
    return null;
  }

  public String getInValidRunningEnvironmentListForTimerTrigger() throws Exception {
    List<RunningEnvironmentForTimerTrigger> inValidRunningEnvironmentListForTimerTrigger = this.otherService
        .getInValidRunningEnvironmentListForTimerTrigger();
    Utils.sendHttpResponseForStruts2(null, inValidRunningEnvironmentListForTimerTrigger);
    return null;
  }

  public String cleanInValidEnvironmentsForTimerTrigger() throws Exception {
    List<String> cleanedInValidEnvironmentsForTimerTrigger = this.otherService
        .cleanInValidEnvironmentsForTimerTrigger();
    Utils.sendHttpResponseForStruts2(null, cleanedInValidEnvironmentsForTimerTrigger);
    return null;
  }

  public String exportTriggersPredictReport() throws Exception {
    HttpServletRequest httpServletRequest = ServletActionContext.getRequest();
    HttpServletResponse httpServletResponse = ServletActionContext.getResponse();
    byte[] excelFileByteDataForTriggersPredictReport = KiraManagerDataCenter.excelFileByteDataForTriggersPredictReport;
    if (null != excelFileByteDataForTriggersPredictReport) {
      WebUtil.setHttpResponseHeaderForExcelExport(httpServletRequest, httpServletResponse, true,
          "TriggersPredictReport.xls");
      IOUtils
          .write(excelFileByteDataForTriggersPredictReport, httpServletResponse.getOutputStream());
    } else {
      String hint = "请先生成定时任务预执行报告相关数据。";
      Utils.sendHttpResponseForStruts2(null, hint);
    }
    return null;
  }

}
