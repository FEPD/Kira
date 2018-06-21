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
package com.yihaodian.monitor.dto;

import com.alibaba.fastjson.JSONObject;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

public class ServerBizLog implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 表分区
   */
  private String partition;
  /**
   * This field corresponds to the database column monitor_server_biz_log.Id
   */
  private Integer id;
  /**
   * 服务版本
   */
  private String serviceVersion;
  /**
   * 服务分组
   */
  private String serviceGroup;
  private String methodName;
  private String providerZone;
  /**
   * This field corresponds to the database column monitor_server_biz_log.UNIQ_REQ_ID
   */
  private String uniqReqId;

  /**
   * This field corresponds to the database column monitor_server_biz_log.REQ_ID
   */
  private String reqId;

  /**
   * This field corresponds to the database column monitor_server_biz_log.PROVIDER_APP
   */
  private String providerApp;

  /**
   * This field corresponds to the database column monitor_server_biz_log.PROVIDER_HOST
   */
  private String providerHost;

  /**
   * This field corresponds to the database column monitor_server_biz_log.SERVICE_NAME
   */
  private String serviceName;

  /**
   * This field corresponds to the database column monitor_server_biz_log.SUCCESSED
   */
  private Integer successed;

  /**
   * 从客户端带到服务端，用来做表分区选择
   */
  private Date reqTime;
  /**
   * This field corresponds to the database column monitor_server_biz_log.GET_REQ_TIME
   */
  private Date getReqTime;

  /**
   * This field corresponds to the database column monitor_server_biz_log.RESP_RESULT_TIME
   */
  private Date respResultTime;

  /**
   * This field corresponds to the database column monitor_server_biz_log.COST_TIME
   */
  private Integer costTime;

  /**
   * This field corresponds to the database column monitor_server_biz_log.GMT_CREATE
   */
  private Date gmtCreate;

  /**
   * This field corresponds to the database column monitor_server_biz_log.MEMO
   */
  private String memo;

  /**
   * This field corresponds to the database column monitor_server_biz_log.EXCEPTION_CLASSNAME
   */
  private String exceptionClassname;

  /**
   * This field corresponds to the database column monitor_server_biz_log.IN_PARAM
   */
  private String inParam;

  /**
   * This field corresponds to the database column monitor_server_biz_log.OUT_PARAM
   */
  private String outParam;

  /**
   * This field corresponds to the database column monitor_server_biz_log.EXCEPTION_DESC
   */
  private String exceptionDesc;
  /**
   * This field corresponds to the database column monitor_server_biz_log.ERROR_TYPE
   */
  private String errorType;

  /**
   * This field corresponds to the database column monitor_client_biz_log.COMM_ID
   */
  private String commId;

  /**
   * This method returns the value of the database column monitor_server_biz_log.IN_PARAM
   *
   * @return the value of monitor_server_biz_log.IN_PARAM
   */
  public String getInParam() {
    return inParam;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.IN_PARAM
   *
   * @param inParam the value for monitor_server_biz_log.IN_PARAM
   */
  public void setInParam(String inParam) {
    this.inParam = inParam;
  }

  public void setInParamObjects(Object... params) {
    this.inParam = JSONObject.toJSONString(params);
    if (StringUtils.isNotEmpty(inParam) && inParam.length() > 500) {
      this.inParam = inParam.substring(0, 500);
    }
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.OUT_PARAM
   *
   * @return the value of monitor_server_biz_log.OUT_PARAM
   */
  public String getOutParam() {
    return outParam;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.OUT_PARAM
   *
   * @param outParam the value for monitor_server_biz_log.OUT_PARAM
   */
  public void setOutParam(String outParam) {
    this.outParam = outParam;
  }

  public void setOutParamObject(Object param) {
    this.outParam = JSONObject.toJSONString(param);
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.EXCEPTION_DESC
   *
   * @return the value of monitor_server_biz_log.EXCEPTION_DESC
   */
  public String getExceptionDesc() {
    return exceptionDesc;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.EXCEPTION_DESC
   *
   * @param exceptionDesc the value for monitor_server_biz_log.EXCEPTION_DESC
   */
  public void setExceptionDesc(String exceptionDesc) {
    this.exceptionDesc = exceptionDesc;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.Id
   *
   * @return the value of monitor_server_biz_log.Id
   */
  public Integer getId() {
    return id;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.Id
   *
   * @param id the value for monitor_server_biz_log.Id
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.UNIQ_REQ_ID
   *
   * @return the value of monitor_server_biz_log.UNIQ_REQ_ID
   */
  public String getUniqReqId() {
    return uniqReqId;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.UNIQ_REQ_ID
   *
   * @param uniqReqId the value for monitor_server_biz_log.UNIQ_REQ_ID
   */
  public void setUniqReqId(String uniqReqId) {
    this.uniqReqId = uniqReqId;
  }

  public String getProviderZone() {
    return providerZone;
  }

  public void setProviderZone(String providerZone) {
    this.providerZone = providerZone;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.REQ_ID
   *
   * @return the value of monitor_server_biz_log.REQ_ID
   */
  public String getReqId() {
    return reqId;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.REQ_ID
   *
   * @param reqId the value for monitor_server_biz_log.REQ_ID
   */
  public void setReqId(String reqId) {
    this.reqId = reqId;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.PROVIDER_APP
   *
   * @return the value of monitor_server_biz_log.PROVIDER_APP
   */
  public String getProviderApp() {
    return providerApp;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.PROVIDER_APP
   *
   * @param providerApp the value for monitor_server_biz_log.PROVIDER_APP
   */
  public void setProviderApp(String providerApp) {
    this.providerApp = providerApp;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.PROVIDER_HOST
   *
   * @return the value of monitor_server_biz_log.PROVIDER_HOST
   */
  public String getProviderHost() {
    return providerHost;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.PROVIDER_HOST
   *
   * @param providerHost the value for monitor_server_biz_log.PROVIDER_HOST
   */
  public void setProviderHost(String providerHost) {
    this.providerHost = providerHost;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.SERVICE_NAME
   *
   * @return the value of monitor_server_biz_log.SERVICE_NAME
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.SERVICE_NAME
   *
   * @param serviceName the value for monitor_server_biz_log.SERVICE_NAME
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.SUCCESSED
   *
   * @return the value of monitor_server_biz_log.SUCCESSED
   */
  public Integer getSuccessed() {
    return successed;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.SUCCESSED
   *
   * @param successed the value for monitor_server_biz_log.SUCCESSED
   */
  public void setSuccessed(Integer successed) {
    this.successed = successed;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.GET_REQ_TIME
   *
   * @return the value of monitor_server_biz_log.GET_REQ_TIME
   */
  public Date getGetReqTime() {
    return getReqTime;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.GET_REQ_TIME
   *
   * @param getReqTime the value for monitor_server_biz_log.GET_REQ_TIME
   */
  public void setGetReqTime(Date getReqTime) {
    this.getReqTime = getReqTime;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.RESP_RESULT_TIME
   *
   * @return the value of monitor_server_biz_log.RESP_RESULT_TIME
   */
  public Date getRespResultTime() {
    return respResultTime;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.RESP_RESULT_TIME
   *
   * @param respResultTime the value for monitor_server_biz_log.RESP_RESULT_TIME
   */
  public void setRespResultTime(Date respResultTime) {
    this.respResultTime = respResultTime;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.COST_TIME
   *
   * @return the value of monitor_server_biz_log.COST_TIME
   */
  public Integer getCostTime() {
    if (getReqTime != null && respResultTime != null) {
      /** 要求服务器设置时间要一致 */
      return (int) (respResultTime.getTime() - getReqTime.getTime());
    }
    //return default cost time
    return (costTime = 30);
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.COST_TIME
   *
   * @param costTime the value for monitor_server_biz_log.COST_TIME
   */
  public void setCostTime(Integer costTime) {
    this.costTime = costTime;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.GMT_CREATE
   *
   * @return the value of monitor_server_biz_log.GMT_CREATE
   */
  public Date getGmtCreate() {
    return gmtCreate;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.GMT_CREATE
   *
   * @param gmtCreate the value for monitor_server_biz_log.GMT_CREATE
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.MEMO
   *
   * @return the value of monitor_server_biz_log.MEMO
   */
  public String getMemo() {
    return memo;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.MEMO
   *
   * @param memo the value for monitor_server_biz_log.MEMO
   */
  public void setMemo(String memo) {
    this.memo = memo;
  }

  /**
   * This method returns the value of the database column monitor_server_biz_log.EXCEPTION_CLASSNAME
   *
   * @return the value of monitor_server_biz_log.EXCEPTION_CLASSNAME
   */
  public String getExceptionClassname() {
    return exceptionClassname;
  }

  /**
   * This method sets the value of the database column monitor_server_biz_log.EXCEPTION_CLASSNAME
   *
   * @param exceptionClassname the value for monitor_server_biz_log.EXCEPTION_CLASSNAME
   */
  public void setExceptionClassname(String exceptionClassname) {
    this.exceptionClassname = exceptionClassname;
  }

  public String getErrorType() {
    return errorType;
  }

  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  public String getCommId() {
    return commId;
  }

  public void setCommId(String commId) {
    this.commId = commId;
  }

  public Date getReqTime() {
    return reqTime;
  }

  public void setReqTime(Date reqTime) {
    this.reqTime = reqTime;
  }


  public void setPartition(String partition) {
    this.partition = partition;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }

  public String getServiceGroup() {
    return serviceGroup;
  }

  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }
}