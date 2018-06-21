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

import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.dto.KiraClientRegisterData;
import com.yihaodian.architecture.kira.common.exception.ValidationException;
import com.yihaodian.architecture.kira.manager.criteria.KiraClientMetadataCriteria;
import com.yihaodian.architecture.kira.manager.dao.KiraClientMetadataDao;
import com.yihaodian.architecture.kira.manager.domain.KiraClientMetadata;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataCreateContent;
import com.yihaodian.architecture.kira.manager.dto.KiraClientMetadataUpdateContent;
import com.yihaodian.architecture.kira.manager.security.SecurityUtils;
import com.yihaodian.architecture.kira.manager.security.UserContextData;
import com.yihaodian.architecture.kira.manager.service.KiraClientMetadataService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.service.TriggerMetadataService;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

public class KiraClientMetadataServiceImpl extends Service implements KiraClientMetadataService {

  private KiraClientMetadataDao kiraClientMetadataDao;

  private TriggerMetadataService triggerMetadataService;

  public void setKiraClientMetadataDao(KiraClientMetadataDao kiraClientMetadataDao) {
    this.kiraClientMetadataDao = kiraClientMetadataDao;
  }

  public void insert(KiraClientMetadata kiraClientMetadata) {
    kiraClientMetadataDao.insert(kiraClientMetadata);
  }

  public TriggerMetadataService getTriggerMetadataService() {
    return triggerMetadataService;
  }

  public void setTriggerMetadataService(
      TriggerMetadataService triggerMetadataService) {
    this.triggerMetadataService = triggerMetadataService;
  }

  public int update(KiraClientMetadata kiraClientMetadata) {
    int actualRowsAffected = 0;

    Long id = kiraClientMetadata.getId();

    KiraClientMetadata _oldKiraClientMetadata = kiraClientMetadataDao.select(id);

    if (_oldKiraClientMetadata != null) {
      actualRowsAffected = kiraClientMetadataDao.update(kiraClientMetadata);
    }

    return actualRowsAffected;
  }

  public int delete(Long id) {
    int actualRowsAffected = 0;

    KiraClientMetadata _oldKiraClientMetadata = kiraClientMetadataDao.select(id);

    if (_oldKiraClientMetadata != null) {
      actualRowsAffected = kiraClientMetadataDao.delete(id);
    }

    return actualRowsAffected;
  }

  public KiraClientMetadata select(Long id) {
    return kiraClientMetadataDao.select(id);
  }

  public List<KiraClientMetadata> list(KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    return kiraClientMetadataDao.list(kiraClientMetadataCriteria);
  }

  public List<KiraClientMetadata> listOnPage(
      KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    return kiraClientMetadataDao.listOnPage(kiraClientMetadataCriteria);
  }

  @Override
  public List<KiraClientMetadata> getKiraClientMetadataListByPoolIdList(List<String> poolIdList) {
    List<KiraClientMetadata> returnValue = null;
    KiraClientMetadataCriteria kiraClientMetadataCriteria = new KiraClientMetadataCriteria();
    kiraClientMetadataCriteria.setPoolIdList(poolIdList);
    returnValue = kiraClientMetadataDao.list(kiraClientMetadataCriteria);

    if (null == returnValue) {
      returnValue = new ArrayList<KiraClientMetadata>();
    }
    return returnValue;
  }

  @Override
  public void handleKiraClientRegisterData(
      KiraClientRegisterData kiraClientRegisterData) {
    String appId = kiraClientRegisterData.getAppId();
    List<String> poolIdList = new ArrayList<String>();
    poolIdList.add(appId);
    List<KiraClientMetadata> kiraClientMetadataCriteriaList = getKiraClientMetadataListByPoolIdList(
        poolIdList);
    Date now = new Date();
    boolean dataExist = false;
    KiraClientMetadata kiraClientMetadata = null;
    if (CollectionUtils.isEmpty(kiraClientMetadataCriteriaList)) {
      kiraClientMetadata = new KiraClientMetadata();
      BeanUtils.copyProperties(kiraClientRegisterData, kiraClientMetadata);
      kiraClientMetadata.setCreateTime(now);
    } else {
      dataExist = true;
      kiraClientMetadata = kiraClientMetadataCriteriaList.get(0);
      Date oldCreateTime = kiraClientMetadata.getCreateTime();
      BeanUtils.copyProperties(kiraClientRegisterData, kiraClientMetadata);
      kiraClientMetadata.setCreateTime(oldCreateTime);
    }
    kiraClientMetadata.setLastRegisterTime(now);
    kiraClientMetadata.setLastRegisterDetail(KiraCommonUtils.toString(kiraClientRegisterData));
    kiraClientMetadata.setLastManuallyUpdateBy(null);
    kiraClientMetadata.setLastManuallyUpdateTime(null);
    kiraClientMetadata.setManuallyCreated(Boolean.FALSE);
    kiraClientMetadata.setManuallyCreatedBy(null);
    kiraClientMetadata.setManuallyCreatedDetail(null);
    if (dataExist) {
      boolean keepKiraClientConfigDataOnKiraServerUnchanged = kiraClientRegisterData
          .isKeepKiraClientConfigDataOnKiraServerUnchanged();
      if (keepKiraClientConfigDataOnKiraServerUnchanged) {
        logger.info(
            "keepKiraClientConfigDataOnKiraServerUnchanged=true for appId={} and kiraClientRegisterData={}",
            appId, KiraCommonUtils.toString(kiraClientRegisterData));
      } else {
        kiraClientMetadataDao.update(kiraClientMetadata);
      }
    } else {
      kiraClientMetadataDao.insert(kiraClientMetadata);
    }
  }

  @Override
  public List<String> getInvisiblePoolListForUser(UserContextData userContextData) {
    List<String> returnValue = new ArrayList<String>();
    if (null != userContextData) {
      KiraClientMetadataCriteria kiraClientMetadataCriteria = new KiraClientMetadataCriteria();
      kiraClientMetadataCriteria.setVisibilityLimited(Boolean.TRUE);
      String userName = userContextData.getUserName();
      kiraClientMetadataCriteria.setUserNameWhichIsNotInVisibleUsers(userName);
      List<KiraClientMetadata> kiraClientMetadataList = kiraClientMetadataDao
          .list(kiraClientMetadataCriteria);
      if (null != kiraClientMetadataList) {
        String oneAppId = null;
        for (KiraClientMetadata oneKiraClientMetadata : kiraClientMetadataList) {
          oneAppId = oneKiraClientMetadata.getAppId();
          returnValue.add(oneAppId);
        }
      }

      //ccjtodo: need to be removed later
      if (StringUtils.isNotBlank(userName)) {
        String adminUserNames = KiraManagerUtils.getAdminUserNames();
        if (StringUtils.isNotBlank(adminUserNames)) {
          if (adminUserNames.contains(userName)) {
            returnValue.clear();
          }
        }
      }

    }
    return returnValue;
  }

  @Override
  public List<String> getPoolIdList(KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    return kiraClientMetadataDao.getPoolIdList(kiraClientMetadataCriteria);
  }

  @Override
  public KiraClientMetadata getKiraClientMetadataById(Long id) {
    KiraClientMetadata returnValue = null;
    if (null != id) {
      returnValue = this.select(id);
    }
    return returnValue;
  }

  private void validateKiraClientMetadataUpdateContent(
      KiraClientMetadataUpdateContent kiraClientMetadataUpdateContent) {
    if (null == kiraClientMetadataUpdateContent) {
      throw new ValidationException("kiraClientMetadataUpdateContent object should not be null.");
    }

    Long id = kiraClientMetadataUpdateContent.getId();
    if (null == id) {
      throw new ValidationException("The value of id should not be null.");
    }
    Boolean visibilityLimited = kiraClientMetadataUpdateContent.getVisibilityLimited();
    if (null == visibilityLimited) {
      throw new ValidationException("The value of visibilityLimited should not be null.");
    }
  }

  @Override
  public HandleResult updateKiraClientMetadata(
      KiraClientMetadataUpdateContent kiraClientMetadataUpdateContent) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      validateKiraClientMetadataUpdateContent(kiraClientMetadataUpdateContent);
      Long id = kiraClientMetadataUpdateContent.getId();
      KiraClientMetadata oldKiraClientMetadata = this.select(id);
      if (null != oldKiraClientMetadata) {
        int updateCount = doUpdateKiraClientMetadata(oldKiraClientMetadata,
            kiraClientMetadataUpdateContent);
        if (1 == updateCount) {
          resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "Update failed. updateCount=" + updateCount;
        }
      } else {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        resultData = "Can not find KiraClientMetadata by id=" + id;
      }
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on updateKiraClientMetadata. kiraClientMetadataUpdateContent="
          + KiraCommonUtils.toString(kiraClientMetadataUpdateContent), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on updateKiraClientMetadata. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public int doUpdateKiraClientMetadata(
      KiraClientMetadata oldKiraClientMetadata,
      KiraClientMetadataUpdateContent kiraClientMetadataUpdateContent) throws Exception {
    BeanUtils.copyProperties(kiraClientMetadataUpdateContent, oldKiraClientMetadata);
    Date now = new Date();
    oldKiraClientMetadata.setLastManuallyUpdateTime(now);
    String userName = SecurityUtils.getUserNameViaStruts2();
    if (StringUtils.isBlank(userName)) {
      userName = KiraServerConstants.UNKNOWN_USER;
    }
    oldKiraClientMetadata.setLastManuallyUpdateBy(userName);
    int count = this.update(oldKiraClientMetadata);
    return count;
  }

  private void validateKiraClientMetadataCreateContent(
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent) {
    if (null == kiraClientMetadataCreateContent) {
      throw new ValidationException("kiraClientMetadataCreateContent object should not be null.");
    }

    String appId = kiraClientMetadataCreateContent.getAppId();
    if (StringUtils.isBlank(appId)) {
      throw new ValidationException("The value of appId should not be blank.");
    }

    boolean isKiraClientMetadataExistForPool = isKiraClientMetadataExistForPool(appId);
    if (isKiraClientMetadataExistForPool) {
      throw new ValidationException("The kiraClient with the appId=" + appId + " exist.");
    }
  }

  @Override
  public HandleResult createKiraClientMetadata(
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      validateKiraClientMetadataCreateContent(kiraClientMetadataCreateContent);
      KiraClientMetadata kiraClientMetadata = doCreateKiraClientMetadata(
          kiraClientMetadataCreateContent);
      resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on createKiraClientMetadata. kiraClientMetadataCreateContent="
          + KiraCommonUtils.toString(kiraClientMetadataCreateContent), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on createKiraClientMetadata. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public KiraClientMetadata doCreateKiraClientMetadata(
      KiraClientMetadataCreateContent kiraClientMetadataCreateContent) throws Exception {
    KiraClientMetadata returnValue = null;
    if (null != kiraClientMetadataCreateContent) {
      Date createTime = kiraClientMetadataCreateContent.getCreateTime();
      if (null == createTime) {
        createTime = new Date();
        kiraClientMetadataCreateContent.setCreateTime(createTime);
      }
      returnValue = new KiraClientMetadata();
      BeanUtils.copyProperties(kiraClientMetadataCreateContent, returnValue);
      kiraClientMetadataDao.insert(returnValue);
    }
    return returnValue;
  }

  @Override
  public boolean isKiraClientMetadataExistForPool(String appId) {
    boolean returnValue = false;
    List<String> poolIdList = new ArrayList<String>();
    poolIdList.add(appId);
    List<KiraClientMetadata> kiraClientMetadataCriteriaList = getKiraClientMetadataListByPoolIdList(
        poolIdList);
    if (!CollectionUtils.isEmpty(kiraClientMetadataCriteriaList)) {
      returnValue = true;
    }
    return returnValue;
  }

  private void validateKiraClientMetadataCriteriaForDeletePool(
      KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    if (null == kiraClientMetadataCriteria) {
      throw new ValidationException("kiraClientMetadataCriteria object should not be null.");
    }

    String appId = kiraClientMetadataCriteria.getAppId();
    if (StringUtils.isBlank(appId)) {
      throw new ValidationException("The value of appId should not be blank.");
    }
  }

  private int doDeletePool(KiraClientMetadataCriteria kiraClientMetadataCriteria) throws Exception {
    int returnValue = 0;
    List<KiraClientMetadata> kiraClientMetadataList = kiraClientMetadataDao
        .list(kiraClientMetadataCriteria);
    if (!CollectionUtils.isEmpty(kiraClientMetadataList)) {
      KiraClientMetadata kiraClientMetadata = kiraClientMetadataList.get(0);
      Long id = kiraClientMetadata.getId();
      returnValue = kiraClientMetadataDao.delete(id);
    }

    String appId = kiraClientMetadataCriteria.getAppId();
    KiraCommonUtils.deletePoolZKNode(appId);

    return returnValue;
  }

  @Override
  public HandleResult deletePool(KiraClientMetadataCriteria kiraClientMetadataCriteria,
      String deletedBy) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      validateKiraClientMetadataCriteriaForDeletePool(kiraClientMetadataCriteria);

      String appId = kiraClientMetadataCriteria.getAppId();
      this.triggerMetadataService.doDeleteTriggersOfPool(appId, deletedBy);

      doDeletePool(kiraClientMetadataCriteria);
      resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on deletePool. kiraClientMetadataCriteria=" + KiraCommonUtils
          .toString(kiraClientMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on deletePool. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  private void validateKiraClientMetadataCriteriaForDeleteKiraClientMetadata(
      KiraClientMetadataCriteria kiraClientMetadataCriteria) {
    if (null == kiraClientMetadataCriteria) {
      throw new ValidationException("kiraClientMetadataCriteria object should not be null.");
    }

    String appId = kiraClientMetadataCriteria.getAppId();
    if (StringUtils.isBlank(appId)) {
      throw new ValidationException("The value of appId should not be blank.");
    }
  }

  @Override
  public int doDeleteKiraClientMetadata(KiraClientMetadataCriteria kiraClientMetadataCriteria,
      String deletedBy) throws Exception {
    int returnValue = 0;
    List<KiraClientMetadata> kiraClientMetadataList = kiraClientMetadataDao
        .list(kiraClientMetadataCriteria);
    String appId = kiraClientMetadataCriteria.getAppId();
    if (!CollectionUtils.isEmpty(kiraClientMetadataList)) {
      KiraClientMetadata kiraClientMetadata = kiraClientMetadataList.get(0);
      Boolean manuallyCreated = kiraClientMetadata.getManuallyCreated();
      boolean isAdminUser = KiraManagerUtils.isAdminUser(deletedBy);
      if (!Boolean.TRUE.equals(manuallyCreated) && !isAdminUser) {
        throw new ValidationException(
            "Only manually created kiraClient can be deleted by non-admin user. appId=" + appId);
      } else {
        Long id = kiraClientMetadata.getId();
        returnValue = kiraClientMetadataDao.delete(id);
      }
    } else {
      throw new ValidationException("This kiraClient do not exist. appId=" + appId);
    }

    return returnValue;
  }

  @Override
  public HandleResult deleteKiraClientMetadata(
      KiraClientMetadataCriteria kiraClientMetadataCriteria, String deletedBy) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      validateKiraClientMetadataCriteriaForDeleteKiraClientMetadata(kiraClientMetadataCriteria);
      doDeleteKiraClientMetadata(kiraClientMetadataCriteria, deletedBy);
      resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
    } catch (ValidationException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error(
          "Error occurs on deleteKiraClientMetadata. kiraClientMetadataCriteria=" + KiraCommonUtils
              .toString(kiraClientMetadataCriteria), e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on deleteKiraClientMetadata. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }

    return handleResult;
  }

  @Override
  public KiraClientMetadata getKiraClientMetadataByPoolId(String appId) {
    KiraClientMetadata returnValue = null;
    List<String> poolIdList = new ArrayList<String>();
    poolIdList.add(appId);
    List<KiraClientMetadata> kiraClientMetadataCriteriaList = getKiraClientMetadataListByPoolIdList(
        poolIdList);
    if (!CollectionUtils.isEmpty(kiraClientMetadataCriteriaList)) {
      returnValue = kiraClientMetadataCriteriaList.get(0);
    }
    if (null == returnValue) {
      logger.warn("Can not get kiraClientMetadata by appId=" + appId);
    }
    return returnValue;
  }

  @Override
  public boolean isPoolNeedAlarmAndAlarmReceiverSet(String appId) {
    boolean returnValue = false;
    if (StringUtils.isNotBlank(appId)) {
      KiraClientMetadata kiraClientMetadata = this.getKiraClientMetadataByPoolId(appId);
      if (null != kiraClientMetadata) {
        Boolean sendAlarmEmail = kiraClientMetadata.getSendAlarmEmail();
        Boolean sendAlarmSMS = kiraClientMetadata.getSendAlarmSMS();
        String emailsToReceiveAlarm = kiraClientMetadata.getEmailsToReceiveAlarm();
        String phoneNumbersToReceiveAlarmSMS = kiraClientMetadata
            .getPhoneNumbersToReceiveAlarmSMS();
        if ((sendAlarmEmail && StringUtils.isNotBlank(emailsToReceiveAlarm))
            || (sendAlarmSMS && StringUtils.isNotBlank(phoneNumbersToReceiveAlarmSMS))) {
          returnValue = true;
        }
      }
    }
    return returnValue;
  }
}
