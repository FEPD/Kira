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
package com.yihaodian.architecture.hedwig.client.event.engine;

import com.yihaodian.architecture.hedwig.client.event.BaseEvent;
import com.yihaodian.architecture.hedwig.client.event.HedwigContext;
import com.yihaodian.architecture.hedwig.client.event.handle.HandlerUtil;
import com.yihaodian.architecture.hedwig.client.event.handle.HedwigHandlerFactory;
import com.yihaodian.architecture.hedwig.client.event.util.EngineUtil;
import com.yihaodian.architecture.hedwig.client.util.HedwigMonitorClientUtil;
import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.constants.RequestType;
import com.yihaodian.architecture.hedwig.common.dto.ArgsMeta;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.util.HedwigAssert;
import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigExecutors;
import com.yihaodian.architecture.hedwig.common.util.HedwigGlobalIdVo;
import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.engine.IEventEngine;
import com.yihaodian.architecture.hedwig.engine.event.EventState;
import com.yihaodian.architecture.hedwig.engine.exception.EngineException;
import com.yihaodian.architecture.hedwig.engine.handler.IEventHandler;
import com.yihaodian.architecture.hedwig.engine.handler.IHandlerFactory;
import com.yihaodian.monitor.dto.ClientBizLog;
import com.yihaodian.monitor.util.MonitorConstants;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer
 */
public class HedwigEventEngine implements IEventEngine<HedwigContext, BaseEvent, Object> {

  private static Logger logger = LoggerFactory.getLogger(HedwigEventEngine.class);
  private static HedwigEventEngine engine = new HedwigEventEngine();
  protected IHandlerFactory<HedwigContext, BaseEvent, Object> handlerFactory;
  protected BlockingQueue<Runnable> eventQueue;
  protected ThreadPoolExecutor tpes;
  protected ScheduledThreadPoolExecutor stpes;

  private HedwigEventEngine() {
    super();
    this.handlerFactory = new HedwigHandlerFactory();
    this.tpes = HedwigExecutors.newCachedThreadPool(InternalConstants.HEDWIG_CLIENT);
    this.stpes = HedwigExecutors.newSchedulerThreadPool(InternalConstants.HEDWIG_CLIENT);
  }

  public static HedwigEventEngine getEngine() {
    return engine;
  }

  @Override
  public Object syncInnerThreadExec(HedwigContext context, final BaseEvent event) {
    HedwigAssert.isNull(event, "Execute event must not null!!!");
    Object result = null;
    Date reqTime = new Date(event.getStart());
    initMateData(context, event);
    String reqId = HedwigContextUtil.getRequestId();
    HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
    final ClientBizLog cbLog = HedwigMonitorClientUtil
        .createClientBizLog(event, context, reqId, globalIdVo.getGlobalId(), reqTime);
    Object[] params = event.getInvocation().getArguments();
    ArgsMeta metas = HedwigMonitorUtil.generateMeta(params);
    IEventHandler<HedwigContext, BaseEvent, Object> handler = handlerFactory.create(event);
    event.setState(EventState.processing);
    try {
      result = handler.handle(context, event);
      cbLog.setRespTime(new Date());
      cbLog.setSuccessed(MonitorConstants.SUCCESS);
      cbLog.setInParamObjects(metas);
    } catch (Throwable e) {
      cbLog.setInParamObjects(metas, params);
      HedwigMonitorClientUtil.setException(cbLog, e);
    } finally {
      cbLog.setProviderHost(event.getLastTryHost());
      cbLog.setLayerType(MonitorConstants.LAYER_TYPE_ENGINE);
      cbLog.setCurtLayer(HedwigContextUtil.getRequestHop());
      cbLog.setLocalLayer(System.nanoTime());
      //MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
      //谁生成谁清理
      HedwigContextUtil.cleanGlobal(globalIdVo);
    }

    return result;
  }

  @Override
  public Object syncPoolExec(final HedwigContext context, final BaseEvent event) {
    HedwigAssert.isNull(event, "Execute event must not null!!!");
    initMateData(context, event);
    Object result = null;
    Future<Object> f = null;
    String reqId = HedwigContextUtil.getRequestId();
    HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
    final ClientBizLog cbLog = HedwigMonitorClientUtil
        .createClientBizLog(event, context, reqId, globalIdVo.getGlobalId(),
            new Date(event.getStart()));
    Object[] params = event.getInvocation().getArguments();
    ArgsMeta metas = HedwigMonitorUtil.generateMeta(params);
    try {
      final IEventHandler<HedwigContext, BaseEvent, Object> handler = handlerFactory.create(event);
      cbLog.setMemo(HedwigMonitorUtil.getThreadPoolInfo(tpes));
      f = tpes.submit(new Callable<Object>() {

        @Override
        public Object call() throws EngineException {
          Object r = null;
          try {
            event.setState(EventState.processing);
            r = handler.handle(context, event);
          } catch (Throwable e) {
            if (HandlerUtil.isNetworkException(e)) {
              r = EngineUtil.retry(handler, event, context);
            }
          } finally {
            cbLog.setProviderHost(event.getLastTryHost());
            cbLog.setCommId(HedwigContextUtil.getTransactionId());
          }
          return r;
        }
      });
      try {
        result = f.get(event.getExpireTime(), event.getExpireTimeUnit());
      } catch (TimeoutException e) {
        f.cancel(false);//超时立即将Future任务取消
        throw e;
      }
      cbLog.setRespTime(new Date());
      if (event.getState().equals(EventState.sucess)) {
        cbLog.setSuccessed(MonitorConstants.SUCCESS);
      } else {
        cbLog.setSuccessed(MonitorConstants.FAIL);
      }
      cbLog.setInParamObjects(metas);
    } catch (Throwable e) {
      String errMsg = e.getMessage();
      event.setErrorMessage(errMsg == null ? e.toString() : errMsg);
      cbLog.setInParamObjects(metas, params);
      cbLog.setProviderHost(event.getLastTryHost());
      HedwigMonitorClientUtil.setException(cbLog, e);
      logger.error("\nreqId:" + reqId + ", providerHost:" + event.getTryHostList(), e);
    } finally {
      cbLog.setLayerType(MonitorConstants.LAYER_TYPE_ENGINE);
      cbLog.setCurtLayer(HedwigContextUtil.getRequestHop());
      cbLog.setLocalLayer(System.nanoTime());
      //谁生成谁清理
      HedwigContextUtil.cleanGlobal(globalIdVo);
      //MonitorJmsSendUtil.asyncSendClientBizLog(cbLog);
    }
    return result;
  }

  @Override
  public Future<Object> asyncExec(final HedwigContext context, final BaseEvent event)
      throws HedwigException {
    throw new HedwigException("Not supported yet!!!");
  }

  @Override
  public void asyncReliableExec(final HedwigContext context, final BaseEvent event)
      throws HedwigException {
    throw new HedwigException("Not supported yet!!!");
  }

  @Override
  public Object oneWayExec(final HedwigContext context, final BaseEvent event)
      throws HedwigException {
    throw new HedwigException("Not supported yet!!!");
  }

  @Override
  public void schedulerExec(final HedwigContext context, final BaseEvent event)
      throws HedwigException {
    final IEventHandler<HedwigContext, BaseEvent, Object> handler = handlerFactory.create(event);
    final HedwigGlobalIdVo globalIdVo = HedwigContextUtil.getGlobalIdVo();
    try {
      stpes.schedule(new Runnable() {
        @Override
        public void run() {
          try {
            HedwigContextUtil.setGlobalId(globalIdVo.getGlobalId());
            HedwigContextUtil.setRequestId(event.getReqestId());
            event.setState(EventState.processing);
            handler.handle(context, event);
          } catch (Throwable e) {
            logger.debug(e.getMessage());
          }
        }
      }, event.getDelay(), event.getDelayUnit());
    } finally {
      //谁生成谁清理
      HedwigContextUtil.cleanGlobal(globalIdVo);
    }
  }

  public Object exec(HedwigContext eventContext, BaseEvent event) {
    BaseEvent bevent = event;
    int type = bevent.getRequestType().getIndex();
    Object result = null;
    if (RequestType.SyncInner.getIndex() == type) {
      result = syncInnerThreadExec(eventContext, event);
    } else {
      result = syncPoolExec(eventContext, event);
    }
    return result;
  }

  public void initMateData(HedwigContext context, BaseEvent event) {
    String reqId = event.getReqestId();
    HedwigContextUtil.setRequestId(reqId);
    HedwigContextUtil
        .setAttribute(InternalConstants.HEDWIG_INVOKE_TIME, new Date(event.getStart()));
    String clientAppName = context.getClientProfile().getClientAppName();
    HedwigContextUtil.setAttribute(PropKeyConstants.POOL_ID, clientAppName);
    String clientVersion = context.getClientProfile().getClientVersion();
    HedwigContextUtil.setAttribute(PropKeyConstants.CLIENT_VERSION, clientVersion);
    Set<String> groupSet = context.getClientProfile().getGroupNames();
    String groups = (groupSet == null || groupSet.size() == 0) ? InternalConstants.NON_GROUP
        : groupSet.toString();
    HedwigContextUtil.setAttribute(PropKeyConstants.CAMPS_NAME, groups);
  }

  public ThreadPoolExecutor getTpes() {
    return tpes;
  }

  public ScheduledThreadPoolExecutor getStpes() {
    return stpes;
  }

  @Override
  public void shutdown() {
    tpes.shutdown();
    stpes.shutdown();
  }
}
