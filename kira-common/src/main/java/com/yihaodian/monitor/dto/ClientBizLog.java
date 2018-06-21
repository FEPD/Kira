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

public class ClientBizLog implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 表分区
   */
  private String partition;
  /**
   * 服务版本
   */
  private String serviceVersion;
  /**
   * 服务分组
   */
  private String serviceGroup;
  /**
   * This field corresponds to the database column monitor_client_biz_log.Id
   */
  private Integer id;

  private String methodName;

  private String serviceMethodName;
  /**
   * This field corresponds to the database column monitor_client_biz_log.UNIQ_REQ_ID
   */
  private String uniqReqId;

  /**
   * This field corresponds to the database column monitor_client_biz_log.REQ_ID
   */
  private String reqId;

  private String callZone;
  private String providerZone;
  private String extInfo;

  /**
   * This field corresponds to the database column monitor_client_biz_log.CALL_APP
   */
  private String callApp;

  /**
   * This field corresponds to the database column monitor_client_biz_log.CALL_HOST
   */
  private String callHost;

  /**
   * This field corresponds to the database column monitor_client_biz_log.SERVICE_NAME
   */
  private String serviceName;

  /**
   * This field corresponds to the database column monitor_client_biz_log.SUCCESSED
   */
  private Integer successed;
  private Long inParamLength;
  private Long outParamLength;
  /**
   * This field corresponds to the database column monitor_client_biz_log.REQ_TIME
   */
  private Date reqTime;

  /**
   * This field corresponds to the database column monitor_client_biz_log.RESP_TIME
   */
  private Date respTime;

  /**
   * This field corresponds to the database column monitor_client_biz_log.COST_TIME
   */
  private Integer costTime;

  /**
   * This field corresponds to the database column monitor_client_biz_log.GMT_CREATE
   */
  private Date gmtCreate;

  /**
   * This field corresponds to the database column monitor_client_biz_log.MEMO
   */
  private String memo;

  /**
   * This field corresponds to the database column monitor_client_biz_log.PROVIDER_APP
   */
  private String providerApp;

  /**
   * This field corresponds to the database column monitor_client_biz_log.PROVIDER_HOST
   */
  private String providerHost;

  private String servicePath;
  /**
   * This field corresponds to the database column monitor_client_biz_log.EXCEPTION_CLASSNAME
   */
  private String exceptionClassname;

  /**
   * This field corresponds to the database column monitor_client_biz_log.IN_PARAM
   */
  private String inParam;

  /**
   * This field corresponds to the database column monitor_client_biz_log.OUT_PARAM
   */
  private String outParam;

  /**
   * This field corresponds to the database column monitor_client_biz_log.EXCEPTION_DESC
   */
  private String exceptionDesc;
  private Integer layerType;
  /**
   * This field corresponds to the database column monitor_server_biz_log.ERROR_TYPE
   */
  @SuppressWarnings("unused")
  private String errorType;
  /***remote层次*/
  private Integer curtLayer;
  /***local层次**/
  private Long localLayer;
  /**
   * This field corresponds to the database column monitor_client_biz_log.COMM_ID
   */
  private String commId;

  /**
   * This method returns the value of the database column monitor_client_biz_log.IN_PARAM
   *
   * @return the value of monitor_client_biz_log.IN_PARAM
   */
  public String getInParam() {
    return inParam;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.IN_PARAM
   *
   * @param inParam the value for monitor_client_biz_log.IN_PARAM
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
   * This method returns the value of the database column monitor_client_biz_log.OUT_PARAM
   *
   * @return the value of monitor_client_biz_log.OUT_PARAM
   */
  public String getOutParam() {
    return outParam;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.OUT_PARAM
   *
   * @param outParam the value for monitor_client_biz_log.OUT_PARAM
   */
  public void setOutParam(String outParam) {
    this.outParam = outParam;
  }

  public String getServiceMethodName() {
    return serviceMethodName;
  }

  public void setServiceMethodName(String serviceMethodName) {
    this.serviceMethodName = serviceMethodName;
  }

  public void setOutParamObject(Object param) {
    this.outParam = JSONObject.toJSONString(param);
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.EXCEPTION_DESC
   *
   * @return the value of monitor_client_biz_log.EXCEPTION_DESC
   */
  public String getExceptionDesc() {
    return exceptionDesc;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.EXCEPTION_DESC
   *
   * @param exceptionDesc the value for monitor_client_biz_log.EXCEPTION_DESC
   */
  public void setExceptionDesc(String exceptionDesc) {
    this.exceptionDesc = exceptionDesc;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.Id
   *
   * @return the value of monitor_client_biz_log.Id
   */
  public Integer getId() {
    return id;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.Id
   *
   * @param id the value for monitor_client_biz_log.Id
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.UNIQ_REQ_ID
   *
   * @return the value of monitor_client_biz_log.UNIQ_REQ_ID
   */
  public String getUniqReqId() {
    return uniqReqId;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.UNIQ_REQ_ID
   *
   * @param uniqReqId the value for monitor_client_biz_log.UNIQ_REQ_ID
   */
  public void setUniqReqId(String uniqReqId) {
    this.uniqReqId = uniqReqId;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.REQ_ID
   *
   * @return the value of monitor_client_biz_log.REQ_ID
   */
  public String getReqId() {
    return reqId;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.REQ_ID
   *
   * @param reqId the value for monitor_client_biz_log.REQ_ID
   */
  public void setReqId(String reqId) {
    this.reqId = reqId;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.CALL_APP
   *
   * @return the value of monitor_client_biz_log.CALL_APP
   */
  public String getCallApp() {
    return callApp == null ? "unKnowCallApp" : callApp;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.CALL_APP
   *
   * @param callApp the value for monitor_client_biz_log.CALL_APP
   */
  public void setCallApp(String callApp) {
    this.callApp = callApp;
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

  /**
   * This method returns the value of the database column monitor_client_biz_log.CALL_HOST
   *
   * @return the value of monitor_client_biz_log.CALL_HOST
   */
  public String getCallHost() {
    return (callHost == null ? "unknowCallerHost" : callHost);
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.CALL_HOST
   *
   * @param callHost the value for monitor_client_biz_log.CALL_HOST
   */
  public void setCallHost(String callHost) {
    this.callHost = callHost;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.SERVICE_NAME
   *
   * @return the value of monitor_client_biz_log.SERVICE_NAME
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.SERVICE_NAME
   *
   * @param serviceName the value for monitor_client_biz_log.SERVICE_NAME
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.SUCCESSED
   *
   * @return the value of monitor_client_biz_log.SUCCESSED
   */
  public Integer getSuccessed() {
    return successed;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.SUCCESSED
   *
   * @param successed the value for monitor_client_biz_log.SUCCESSED
   */
  public void setSuccessed(Integer successed) {
    this.successed = successed;
  }

  public Long getLocalLayer() {
    return localLayer;
  }

  public void setLocalLayer(Long localLayer) {
    this.localLayer = localLayer;
  }

  public Integer getCurtLayer() {
    return curtLayer;
  }

  public void setCurtLayer(Integer curtLayer) {
    this.curtLayer = curtLayer;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.REQ_TIME
   *
   * @return the value of monitor_client_biz_log.REQ_TIME
   */
  public Date getReqTime() {
    return reqTime == null ? new Date() : reqTime;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.REQ_TIME
   *
   * @param reqTime the value for monitor_client_biz_log.REQ_TIME
   */
  public void setReqTime(Date reqTime) {
    this.reqTime = reqTime;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.RESP_TIME
   *
   * @return the value of monitor_client_biz_log.RESP_TIME
   */
  public Date getRespTime() {
    return respTime;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.RESP_TIME
   *
   * @param respTime the value for monitor_client_biz_log.RESP_TIME
   */
  public void setRespTime(Date respTime) {
    this.respTime = respTime;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.COST_TIME
   *
   * @return the value of monitor_client_biz_log.COST_TIME
   */
  public Integer getCostTime() {
    if (respTime != null && reqTime != null) {
      /** 要求服务器设置时间要一致 */
      return (int) (respTime.getTime() - reqTime.getTime());
    }
    return 50;
  }


  /**
   * This method sets the value of the database column monitor_client_biz_log.COST_TIME
   *
   * @param costTime the value for monitor_client_biz_log.COST_TIME
   */
  public void setCostTime(Integer costTime) {
    this.costTime = costTime;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.GMT_CREATE
   *
   * @return the value of monitor_client_biz_log.GMT_CREATE
   */
  public Date getGmtCreate() {
    return gmtCreate;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.GMT_CREATE
   *
   * @param gmtCreate the value for monitor_client_biz_log.GMT_CREATE
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.MEMO
   *
   * @return the value of monitor_client_biz_log.MEMO
   */
  public String getMemo() {
    return memo;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.MEMO
   *
   * @param memo the value for monitor_client_biz_log.MEMO
   */
  public void setMemo(String memo) {
    this.memo = memo;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.PROVIDER_APP
   *
   * @return the value of monitor_client_biz_log.PROVIDER_APP
   */
  public String getProviderApp() {
    return (providerApp == null ? "unknowApp" : providerApp);
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.PROVIDER_APP
   *
   * @param providerApp the value for monitor_client_biz_log.PROVIDER_APP
   */
  public void setProviderApp(String providerApp) {
    this.providerApp = providerApp;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.PROVIDER_HOST
   *
   * @return the value of monitor_client_biz_log.PROVIDER_HOST
   */
  public String getProviderHost() {
    return (providerHost == null ? "unknowProviderHost" : providerHost);
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.PROVIDER_HOST
   *
   * @param providerHost the value for monitor_client_biz_log.PROVIDER_HOST
   */
  public void setProviderHost(String providerHost) {
    this.providerHost = providerHost;
  }

  public String getMethodName() {
    return (methodName == null ? "unknowMethodName" : methodName);
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getCommId() {
    return commId;
  }

  public void setCommId(String commId) {
    this.commId = commId;
  }

  /**
   * This method returns the value of the database column monitor_client_biz_log.EXCEPTION_CLASSNAME
   *
   * @return the value of monitor_client_biz_log.EXCEPTION_CLASSNAME
   */
  public String getExceptionClassname() {
    return exceptionClassname;
  }

  /**
   * This method sets the value of the database column monitor_client_biz_log.EXCEPTION_CLASSNAME
   *
   * @param exceptionClassname the value for monitor_client_biz_log.EXCEPTION_CLASSNAME
   */
  public void setExceptionClassname(String exceptionClassname) {
    this.exceptionClassname = exceptionClassname;
  }

  public Integer getLayerType() {
    return layerType;
  }

  public void setLayerType(Integer layerType) {
    this.layerType = layerType;
  }

  public String getErrorType() {
    if (successed != null && successed.intValue() == 1) {
      return null;
    }
    if (StringUtils.isEmpty(exceptionClassname) || exceptionClassname.indexOf(".") <= 0) {
      return "UNKNOW_ERROR";
    }
    String[] array = exceptionClassname.split("\\.");
    return array[array.length - 1];
  }

  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }


  public void setPartition(String partition) {
    this.partition = partition;
  }

  public String getServicePath() {
    return servicePath;
  }

  public void setServicePath(String servicePath) {
    this.servicePath = servicePath;
  }

  public Long getInParamLength() {
    return inParamLength;
  }

  public void setInParamLength(Long inParamLength) {
    this.inParamLength = inParamLength;
  }

  public Long getOutParamLength() {
    return outParamLength;
  }

  public void setOutParamLength(Long outParamLength) {
    this.outParamLength = outParamLength;
  }


  public String getExtInfo() {
    return extInfo;
  }

  public void setExtInfo(String extInfo) {
    this.extInfo = extInfo;
  }

  public String getCallZone() {
    return callZone;
  }

  public void setCallZone(String callZone) {
    this.callZone = callZone;
  }

  public String getProviderZone() {
    return providerZone;
  }

  public void setProviderZone(String providerZone) {
    this.providerZone = providerZone;
  }
}