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

import com.yihaodian.architecture.kira.client.api.KiraClientAPI;
import com.yihaodian.architecture.kira.common.HandleResult;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.common.exception.KiraWorkCanceledException;
import com.yihaodian.architecture.kira.manager.core.metadata.timertrigger.IKiraTimerTriggerMetadataManager;
import com.yihaodian.architecture.kira.manager.core.schedule.timertrigger.IKiraTimerTriggerScheduleCenter;
import com.yihaodian.architecture.kira.manager.core.server.spi.IClusterInternalService;
import com.yihaodian.architecture.kira.manager.core.server.util.KiraServerUtils;
import com.yihaodian.architecture.kira.manager.criteria.KiraServerCriteria;
import com.yihaodian.architecture.kira.manager.dto.KiraServerDetailData;
import com.yihaodian.architecture.kira.manager.service.KiraServerService;
import com.yihaodian.architecture.kira.manager.service.Service;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraServerConstants;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class KiraServerServiceImpl extends Service implements KiraServerService {

  public KiraServerServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<KiraServerDetailData> getKiraServerDetailDataList(
      KiraServerCriteria kiraServerCriteria) {
    List<KiraServerDetailData> returnValue = new ArrayList<KiraServerDetailData>();
    try {
      LinkedHashSet<IClusterInternalService> allClusterInternalServices = KiraServerUtils
          .getAllClusterInternalServices();
      KiraServerDetailData kiraServerDetailData = null;
      for (IClusterInternalService clusterInternalService : allClusterInternalServices) {
        kiraServerDetailData = clusterInternalService.getKiraServerDetailData();
        returnValue.add(kiraServerDetailData);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs when getKiraServerDetailDataList. appCenterCriteria=" + KiraCommonUtils
              .toString(kiraServerCriteria), e);
    }
    return returnValue;
  }

  @Override
  public HandleResult recoverServer(KiraServerCriteria kiraServerCriteria) {
    // TODO Auto-generated method stub
    return null;
  }

//	private boolean isKiraServerClusterHealthy(KiraServerZNodeData kiraServerZNodeData) {
//		boolean returnValue = false;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.isKiraServerClusterHealthy();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.isKiraServerClusterHealthy();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for isKiraServerClusterHealthy. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private boolean isKiraServerIntentionallyStopped(KiraServerZNodeData kiraServerZNodeData) {
//		boolean returnValue = false;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.isKiraServerIntentionallyStopped();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.isKiraServerIntentionallyStopped();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for isKiraServerIntentionallyStopped. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private boolean isSchedulerInStandbyMode(KiraServerZNodeData kiraServerZNodeData) {
//		boolean returnValue = false;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.isSchedulerInStandbyMode();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.isSchedulerInStandbyMode();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for isSchedulerInStandbyMode. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getLastRecoverServerTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getLastRecoverServerTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getLastRecoverServerTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getLastRecoverServerTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getServerSideNowTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getServerSideNowTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getServerSideNowTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getServerSideNowTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getLastRestartServerTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getLastRestartServerTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getLastRestartServerTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getLastRestartServerTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getLastStopServerTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getLastStopServerTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getLastStopServerTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getLastStopServerTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getLastStartServerTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getLastStartServerTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getLastStartServerTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getLastStartServerTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}

//	private Date getElectedAsLeaderServerTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getElectedAsLeaderServerTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getElectedAsLeaderServerTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getElectedAsLeaderServerTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}	

//	private Date getServerBirthTime(KiraServerZNodeData kiraServerZNodeData) {
//		Date returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.getServerBirthTime();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.getServerBirthTime();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for getServerBirthTime. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//
//		return returnValue;
//	}

//	private Boolean isLeaderDoingRoutineWork(KiraServerZNodeData kiraServerZNodeData) {
//		Boolean returnValue = null;
//		try{
//			if(null!=kiraServerZNodeData) {
//				String clusterInternalServiceUrl = kiraServerZNodeData.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					if(clusterInternalServiceUrl.equals(KiraServerDataCenter.getClusterInternalServiceUrl())) {
//						//handled locally
//						returnValue = kiraServerHandler.isLeaderDoingRoutineWork();
//					} else {
//						IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalService(clusterInternalServiceUrl);
//						if(null!=clusterInternalService) {
//							returnValue = clusterInternalService.isLeaderDoingRoutineWork();
//						}
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error("Error occurs for isLeaderDoingRoutineWork. kiraServerZNodeData="+KiraCommonUtils.toString(kiraServerZNodeData),e);
//		}
//		
//		return returnValue;
//	}

  //start admin usage
  @Override
  public HandleResult startServer(KiraServerCriteria kiraServerCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (null != kiraServerCriteria) {
        String clusterInternalServiceUrl = kiraServerCriteria.getClusterInternalServiceUrl();
        if (StringUtils.isNotBlank(clusterInternalServiceUrl)) {
          IClusterInternalService clusterInternalService = KiraServerUtils
              .getClusterInternalService(clusterInternalServiceUrl);
          if (null != clusterInternalService) {
            clusterInternalService.startKiraServer();
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          } else {
            resultData = "clusterInternalService is null.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "clusterInternalServiceUrl should not be blank.";
        }
      } else {
        resultData = "kiraServerCriteria should not be null";
      }
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on startServer.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on startServer. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public HandleResult stopServer(KiraServerCriteria kiraServerCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (null != kiraServerCriteria) {
        String clusterInternalServiceUrl = kiraServerCriteria.getClusterInternalServiceUrl();
        if (StringUtils.isNotBlank(clusterInternalServiceUrl)) {
          IClusterInternalService clusterInternalService = KiraServerUtils
              .getClusterInternalService(clusterInternalServiceUrl);
          if (null != clusterInternalService) {
            clusterInternalService.shutdownKiraServer();
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          } else {
            resultData = "clusterInternalService is null.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "clusterInternalServiceUrl should not be blank.";
        }
      } else {
        resultData = "kiraServerCriteria should not be null";
      }
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on stopServer.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on stopServer. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public HandleResult restartServer(KiraServerCriteria kiraServerCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (null != kiraServerCriteria) {
        String clusterInternalServiceUrl = kiraServerCriteria.getClusterInternalServiceUrl();
        if (StringUtils.isNotBlank(clusterInternalServiceUrl)) {
          IClusterInternalService clusterInternalService = KiraServerUtils
              .getClusterInternalService(clusterInternalServiceUrl);
          if (null != clusterInternalService) {
            clusterInternalService.restartKiraServer();
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          } else {
            resultData = "clusterInternalService is null.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "clusterInternalServiceUrl should not be blank.";
        }
      } else {
        resultData = "kiraServerCriteria should not be null";
      }
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on restartServer.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on restartServer. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public HandleResult destroyServer(KiraServerCriteria kiraServerCriteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      if (null != kiraServerCriteria) {
        String clusterInternalServiceUrl = kiraServerCriteria.getClusterInternalServiceUrl();
        if (StringUtils.isNotBlank(clusterInternalServiceUrl)) {
          IClusterInternalService clusterInternalService = KiraServerUtils
              .getClusterInternalService(clusterInternalServiceUrl);
          if (null != clusterInternalService) {
            clusterInternalService.destroyKiraServer();
            resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          } else {
            resultData = "clusterInternalService is null.";
          }
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_FAILED;
          resultData = "clusterInternalServiceUrl should not be blank.";
        }
      } else {
        resultData = "kiraServerCriteria should not be null";
      }
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on destroyServer.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on destroyServer. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

//	@Override
//	public HandleResult recoverServer(KiraServerCriteria kiraServerCriteria) {
//		HandleResult handleResult = null;
//		String resultCode = null;
//		String resultData = null;
//		Exception exceptionOccured = null;
//		try{
//			if(null!=kiraServerCriteria) {
//				String clusterInternalServiceUrl = kiraServerCriteria.getClusterInternalServiceUrl();
//				if(StringUtils.isNotBlank(clusterInternalServiceUrl)) {
//					IClusterInternalService clusterInternalService = KiraManagerUtils.getClusterInternalServiceByClusterInternalServiceUrl(clusterInternalServiceUrl);
//					if(null!=clusterInternalService) {
//						clusterInternalService.recoverServer();
//						resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
//					} else {
//						resultData = "clusterInternalService is null.";
//					}
//				} else {
//					resultData = "clusterInternalServiceUrl should not be blank.";
//				}
//			} else {
//				resultData = "kiraServerCriteria should not be null";
//			}
//		}  catch(KiraHandleException e) {
//			resultCode = KiraServerConstants.RESULT_CODE_FAILED;
//			resultData = e.getMessage();
//		} catch(Exception e){
//			exceptionOccured = e;
//			logger.error("Error occurs on recoverServer.",e);
//		}  finally{
//			if(null!=exceptionOccured) {
//				resultCode = KiraServerConstants.RESULT_CODE_FAILED;
//				String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
//				if(null==resultData) {
//					resultData = "";
//				}
//				resultData+=" Exception occurs on recoverServer. exceptionOccured="+exceptionOccuredDesc;
//			}
//			handleResult = new HandleResult(resultCode,resultData);
//		}
//		return handleResult;
//	}

  @Override
  public HandleResult doLeaderRoutineWork() {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      IKiraTimerTriggerMetadataManager kiraTimerTriggerMetadataManager = KiraManagerDataCenter
          .getKiraTimerTriggerMetadataManager();
      if (kiraTimerTriggerMetadataManager.isLeaderServer(false)) {
        kiraTimerTriggerMetadataManager.tryToDoLeaderRoutineWork();
        IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
            .getKiraTimerTriggerScheduleCenter();
        if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
          resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          kiraTimerTriggerScheduleCenter.tryToDoLeaderRoutineWork();
        } else {
          resultCode = KiraServerConstants.RESULT_CODE_PARTIAL_SUCCESS;
          resultData = "tryToDoLeaderRoutineWork success for kiraTimerTriggerMetadataManager. But kiraTimerTriggerScheduleCenter is not leaderServer.";
        }
      } else {
        IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
            .getClusterInternalServiceOfLeader();
        if (null != leaderServerClusterInternalService) {
          leaderServerClusterInternalService.doLeaderRoutineWorkOfKiraTimerTriggerMetadataManager();
          resultCode = KiraServerConstants.RESULT_CODE_PARTIAL_SUCCESS;
          resultData = "doLeaderRoutineWorkOfKiraTimerTriggerMetadataManager success. But doLeaderRoutineWorkOfKiraTimerTriggerScheduleCenter failed.";

          leaderServerClusterInternalService.doLeaderRoutineWorkOfKiraTimerTriggerScheduleCenter();
          resultCode = KiraServerConstants.RESULT_CODE_SUCCESS;
          resultData = "";
        } else {
          resultData = "leaderServerClusterInternalService is null.";
        }
      }
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on doLeaderRoutineWork.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on doLeaderRoutineWork. exceptionOccured=" + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public HandleResult addServerIdToAssignedServerIdBlackList(
      KiraServerCriteria criteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    String assignedServerIdInBlackList = criteria.getAssignedServerIdInBlackList();
    try {
      this.addServerIdToAssignedServerIdBlackList(assignedServerIdInBlackList);
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error(
          "Error occurs on addServerIdToAssignedServerIdBlackList. assignedServerIdInBlackList="
              + assignedServerIdInBlackList, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on addServerIdToAssignedServerIdBlackList. exceptionOccured="
                + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public void addServerIdToAssignedServerIdBlackList(String assignedServerIdInBlackList)
      throws Exception {
    if (StringUtils.isNotBlank(assignedServerIdInBlackList)) {
      IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
          .getKiraTimerTriggerScheduleCenter();
      if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
        kiraTimerTriggerScheduleCenter
            .addServerIdToAssignedServerIdBlackList(assignedServerIdInBlackList);
      } else {
        IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
            .getClusterInternalServiceOfLeader();
        if (null != leaderServerClusterInternalService) {
          leaderServerClusterInternalService
              .addServerIdToAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
                  assignedServerIdInBlackList);
        } else {
          String errorMessage = "leaderServerClusterInternalService is null.";
          throw new KiraHandleException(errorMessage);
        }
      }
    } else {
      String errorMessage =
          "assignedServerIdInBlackList should not be blank. assignedServerIdInBlackList="
              + assignedServerIdInBlackList;
      throw new KiraHandleException(errorMessage);
    }
  }

  @Override
  public HandleResult removeServerIdFromAssignedServerIdBlackList(
      KiraServerCriteria criteria) {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    String assignedServerIdInBlackList = criteria.getAssignedServerIdInBlackList();
    try {
      this.removeServerIdFromAssignedServerIdBlackList(assignedServerIdInBlackList);
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error(
          "Error occurs on addServerIdToAssignedServerIdBlackList. assignedServerIdInBlackList="
              + assignedServerIdInBlackList, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData +=
            " Exception occurs on addServerIdToAssignedServerIdBlackList. exceptionOccured="
                + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public void removeServerIdFromAssignedServerIdBlackList(String assignedServerIdInBlackList)
      throws Exception {
    if (StringUtils.isNotBlank(assignedServerIdInBlackList)) {
      IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
          .getKiraTimerTriggerScheduleCenter();
      if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
        kiraTimerTriggerScheduleCenter
            .removeServerIdFromAssignedServerIdBlackList(assignedServerIdInBlackList);
      } else {
        IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
            .getClusterInternalServiceOfLeader();
        if (null != leaderServerClusterInternalService) {
          leaderServerClusterInternalService
              .removeServerIdFromAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
                  assignedServerIdInBlackList);
        } else {
          String errorMessage = "leaderServerClusterInternalService is null.";
          throw new KiraHandleException(errorMessage);
        }
      }
    } else {
      String errorMessage =
          "assignedServerIdInBlackList should not be blank. assignedServerIdInBlackList="
              + assignedServerIdInBlackList;
      throw new KiraHandleException(errorMessage);
    }
  }

  @Override
  public HandleResult setAssignedServerIdBlackList(
      KiraServerCriteria criteria) throws Exception {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    String assignedServerIdBlackList = criteria.getAssignedServerIdBlackList();
    try {
      this.setAssignedServerIdBlackList(assignedServerIdBlackList);
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on setAssignedServerIdBlackList. assignedServerIdBlackList="
          + assignedServerIdBlackList, e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on setAssignedServerIdBlackList. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public void setAssignedServerIdBlackList(String assignedServerIdBlackList)
      throws Exception {
    List<String> blackList = KiraCommonUtils
        .getStringListByDelmiter(assignedServerIdBlackList, KiraCommonConstants.COMMA_DELIMITER);
    LinkedHashSet<String> blackListAsSet = new LinkedHashSet<String>(blackList);
    this.setAssignedServerIdBlackList(blackListAsSet);
  }

  private void setAssignedServerIdBlackList(LinkedHashSet<String> assignedServerIdBlackList)
      throws Exception {
    if (null == assignedServerIdBlackList) {
      assignedServerIdBlackList = new LinkedHashSet<String>();
    }
    IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
        .getKiraTimerTriggerScheduleCenter();
    if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
      kiraTimerTriggerScheduleCenter.setAssignedServerIdBlackList(assignedServerIdBlackList);
    } else {
      IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
          .getClusterInternalServiceOfLeader();
      if (null != leaderServerClusterInternalService) {
        leaderServerClusterInternalService
            .setAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter(
                assignedServerIdBlackList);
      } else {
        String errorMessage = "leaderServerClusterInternalService is null.";
        throw new KiraHandleException(errorMessage);
      }
    }
  }

  @Override
  public HandleResult clearAssignedServerIdBlackList(KiraServerCriteria criteria) throws Exception {
    HandleResult handleResult = null;
    String resultCode = null;
    String resultData = null;
    Exception exceptionOccured = null;
    try {
      this.clearAssignedServerIdBlackList();
    } catch (KiraHandleException e) {
      resultCode = KiraServerConstants.RESULT_CODE_FAILED;
      resultData = e.getMessage();
    } catch (Exception e) {
      exceptionOccured = e;
      logger.error("Error occurs on clearAssignedServerIdBlackList.", e);
    } finally {
      if (null != exceptionOccured) {
        resultCode = KiraServerConstants.RESULT_CODE_FAILED;
        String exceptionOccuredDesc = ExceptionUtils.getFullStackTrace(exceptionOccured);
        if (null == resultData) {
          resultData = "";
        }
        resultData += " Exception occurs on clearAssignedServerIdBlackList. exceptionOccured="
            + exceptionOccuredDesc;
      }
      handleResult = new HandleResult(resultCode, resultData);
    }
    return handleResult;
  }

  @Override
  public void clearAssignedServerIdBlackList() throws Exception {
    IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
        .getKiraTimerTriggerScheduleCenter();
    if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
      kiraTimerTriggerScheduleCenter.clearAssignedServerIdBlackList();
    } else {
      IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
          .getClusterInternalServiceOfLeader();
      if (null != leaderServerClusterInternalService) {
        leaderServerClusterInternalService
            .clearAssignedServerIdBlackListOfKiraTimerTriggerScheduleCenter();
      } else {
        String errorMessage = "leaderServerClusterInternalService is null.";
        throw new KiraHandleException(errorMessage);
      }
    }
  }

  @Override
  public void loadBalanceForKiraTimerTriggerSchedule(Integer maxDoLoadBalanceRoundCount)
      throws Exception {
    if (null != maxDoLoadBalanceRoundCount) {
      if (maxDoLoadBalanceRoundCount <= 0) {
        String errorMessage =
            "maxDoLoadBalanceRoundCount should not be <=0. maxDoLoadBalanceRoundCount="
                + maxDoLoadBalanceRoundCount;
        throw new KiraHandleException(errorMessage);
      } else {
        try {
          IKiraTimerTriggerScheduleCenter kiraTimerTriggerScheduleCenter = KiraManagerDataCenter
              .getKiraTimerTriggerScheduleCenter();
          if (kiraTimerTriggerScheduleCenter.isLeaderServer(false)) {
            kiraTimerTriggerScheduleCenter
                .loadBalanceForKiraTimerTriggerSchedule(maxDoLoadBalanceRoundCount.intValue());
          } else {
            IClusterInternalService leaderServerClusterInternalService = KiraServerUtils
                .getClusterInternalServiceOfLeader();
            if (null != leaderServerClusterInternalService) {
              leaderServerClusterInternalService
                  .loadBalanceForKiraTimerTriggerSchedule(maxDoLoadBalanceRoundCount.intValue());
            } else {
              String errorMessage = "leaderServerClusterInternalService is null.";
              throw new KiraHandleException(errorMessage);
            }
          }
        } catch (KiraWorkCanceledException kiraWorkCanceledException) {
          String logMessage =
              "kiraWorkCanceledException caught. It may do not mean an error for loadBalanceForKiraTimerTriggerSchedule. So do not throw this out. kiraWorkCanceledException="
                  + ExceptionUtils.getFullStackTrace(kiraWorkCanceledException);
          logger.info(logMessage);
          KiraClientAPI.setUserAddedMethodInvokeLog(logMessage);
        }
      }
    } else {
      String errorMessage = "maxDoLoadBalanceRoundCount should not be null.";
      throw new KiraHandleException(errorMessage);
    }
  }

  //end admin usage

}
