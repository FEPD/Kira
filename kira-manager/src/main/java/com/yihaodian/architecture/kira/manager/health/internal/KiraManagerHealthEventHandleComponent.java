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
package com.yihaodian.architecture.kira.manager.health.internal;

import com.yihaodian.architecture.kira.common.ComponentAdaptor;
import com.yihaodian.architecture.kira.common.CustomizedThreadFactory;
import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.KiraCommonUtils;
import com.yihaodian.architecture.kira.common.monitor.MonitorContext;
import com.yihaodian.architecture.kira.common.monitor.MonitorNoticeInfo;
import com.yihaodian.architecture.kira.manager.health.event.ClusterInternalConnectionFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.ClusterInternalConnectionRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.CreateAndRunJobForTimerTriggerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.DBForMenuRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.DBForMenuUnavailableEvent;
import com.yihaodian.architecture.kira.manager.health.event.DBForScheduleRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.DBForScheduleUnavailableEvent;
import com.yihaodian.architecture.kira.manager.health.event.ExternalOverallMonitorForTimerTriggerFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.ExternalOverallMonitorForTimerTriggerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraClientRegisterDataConsumerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraClientRegisterDataConsumerSickEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraJobItemStatusConsumerRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraJobItemStatusConsumerSickEvent;
import com.yihaodian.architecture.kira.manager.health.event.KiraManagerHealthEvent;
import com.yihaodian.architecture.kira.manager.health.event.NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent;
import com.yihaodian.architecture.kira.manager.health.event.RunTimerTriggerTaskFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.RunTimerTriggerTaskRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.TimerTriggerScheduleFailedEvent;
import com.yihaodian.architecture.kira.manager.health.event.TimerTriggerScheduleRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.ZKForKiraRecoveredEvent;
import com.yihaodian.architecture.kira.manager.health.event.ZKForKiraUnavailableEvent;
import com.yihaodian.architecture.kira.manager.util.EmailUtils;
import com.yihaodian.architecture.kira.manager.util.KiraManagerDataCenter;
import com.yihaodian.architecture.kira.manager.util.KiraManagerUtils;
import com.yihaodian.architecture.kira.manager.util.SMSUtils;
import com.yihaodian.architecture.kira.manager.util.VelocityUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class can not be restarted.
 */
public class KiraManagerHealthEventHandleComponent extends ComponentAdaptor implements
    IKiraManagerHealthEventHandleComponent {

  private static KiraManagerHealthEventHandleComponent kiraManagerHealthEventHandleComponent;
  protected ExecutorService kiraManagerHealthEventExecutorService;

  private KiraManagerHealthEventHandleComponent() throws Exception {
    this.init();
    this.start();
  }

  public static synchronized KiraManagerHealthEventHandleComponent getKiraManagerHealthEventHandleComponent()
      throws Exception {
    if (null == KiraManagerHealthEventHandleComponent.kiraManagerHealthEventHandleComponent) {
      KiraManagerHealthEventHandleComponent.kiraManagerHealthEventHandleComponent = new KiraManagerHealthEventHandleComponent();
    }

    return KiraManagerHealthEventHandleComponent.kiraManagerHealthEventHandleComponent;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  protected void init() throws Exception {
    this.prepareKiraManagerHealthEventExecutorService();
    KiraManagerHealthEventDispatcherWrapper.getKiraManagerHealthEventDispatcherWrapper()
        .registerKiraManagerHealthEventHandler(this);
  }

  protected void prepareKiraManagerHealthEventExecutorService() throws Exception {
    ThreadFactory threadFactory = this.getThreadFactoryForKiraManagerHealthEventExecutorService();
    RejectedExecutionHandler kiraCrossMultiZoneEventHandlerRejectedExecutionHandler = this
        .getRejectedExecutionHandlerForKiraManagerHealthEventExecutorService();
    int corePoolSize = this.getCorePoolSizeForKiraManagerHealthEventExecutorService();
    int maximumPoolSize = this.getMaximumPoolSizeForKiraManagerHealthEventExecutorService();
    int capacity = this.getCapacityOfQueueForKiraManagerHealthEventExecutorService();
    this.kiraManagerHealthEventExecutorService = new ThreadPoolExecutor(corePoolSize,
        maximumPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(capacity),
        threadFactory, kiraCrossMultiZoneEventHandlerRejectedExecutionHandler);
  }

  protected ThreadFactory getThreadFactoryForKiraManagerHealthEventExecutorService() {
    String classSimpleName = this.getClass().getSimpleName();
    return new CustomizedThreadFactory(classSimpleName + "-KiraManagerHealthEventHandler-");
  }

  protected RejectedExecutionHandler getRejectedExecutionHandlerForKiraManagerHealthEventExecutorService() {
    return new KiraManagerHealthEventHandlerRejectedExecutionHandler();
  }

  protected int getCorePoolSizeForKiraManagerHealthEventExecutorService() {
    return 1;
  }

  protected int getMaximumPoolSizeForKiraManagerHealthEventExecutorService() {
    return 1;
  }

  protected int getCapacityOfQueueForKiraManagerHealthEventExecutorService() {
    return 1000;
  }

  @Override
  public void handle(KiraManagerHealthEvent event) {
    try {
      if (!this.kiraManagerHealthEventExecutorService.isShutdown()) {
        KiraManagerHealthEventHandleTask kiraManagerHealthEventHandleTask = new KiraManagerHealthEventHandleTask(
            event);
        this.kiraManagerHealthEventExecutorService.submit(kiraManagerHealthEventHandleTask);
      } else {
        logger.warn(
            "Can not handle KiraManagerHealthEvent for kiraManagerHealthEventExecutorService is shutdown in "
                + this.getClass().getSimpleName() + ". kiraManagerHealthEvent={}", event);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurs for handle KiraManagerHealthEvent in " + this.getClass().getSimpleName()
              + ". kiraManagerHealthEvent={}", event);
    }
  }

  public void handleKiraManagerHealthEvent(KiraManagerHealthEvent kiraManagerHealthEvent) {
    switch (kiraManagerHealthEvent.getEventType()) {
      case ZK_FOR_KIRA_UNAVAILABLE:
        ZKForKiraUnavailableEvent zkForKiraUnavailableEvent = (ZKForKiraUnavailableEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught zkForKiraUnavailableEvent={}",
            zkForKiraUnavailableEvent);
        this.handleZKForKiraUnavailableEvent(zkForKiraUnavailableEvent);
        break;
      case ZK_FOR_KIRA_RECOVERED:
        ZKForKiraRecoveredEvent zkForKiraRecoveredEvent = (ZKForKiraRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught zkForKiraRecoveredEvent={}",
            zkForKiraRecoveredEvent);
        this.handleZKForKiraRecoveredEvent(zkForKiraRecoveredEvent);
        break;
      case DB_FOR_SCHEDULE_UNAVAILABLE:
        DBForScheduleUnavailableEvent dbForScheduleUnavailableEvent = (DBForScheduleUnavailableEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught dbForScheduleUnavailableEvent={}",
            dbForScheduleUnavailableEvent);
        this.handleDBForScheduleUnavailableEvent(dbForScheduleUnavailableEvent);
        break;
      case DB_FOR_SCHEDULE_RECOVERED:
        DBForScheduleRecoveredEvent dbForScheduleRecoveredEvent = (DBForScheduleRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught dbForScheduleRecoveredEvent={}",
            dbForScheduleRecoveredEvent);
        this.handleDBForScheduleRecoveredEvent(dbForScheduleRecoveredEvent);
        break;
      case DB_FOR_MENU_UNAVAILABLE:
        DBForMenuUnavailableEvent dbForMenuUnavailableEvent = (DBForMenuUnavailableEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught dbForMenuUnavailableEvent={}",
            dbForMenuUnavailableEvent);
        this.handleDBForMenuUnavailableEvent(dbForMenuUnavailableEvent);
        break;
      case DB_FOR_MENU_RECOVERED:
        DBForMenuRecoveredEvent dbForMenuRecoveredEvent = (DBForMenuRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught dbForMenuRecoveredEvent={}",
            dbForMenuRecoveredEvent);
        this.handleDBForMenuRecoveredEvent(dbForMenuRecoveredEvent);
        break;
      case KIRAJOBITEMSTATUSCONSUMER_SICK:
        KiraJobItemStatusConsumerSickEvent kiraJobItemStatusConsumerSickEvent = (KiraJobItemStatusConsumerSickEvent) kiraManagerHealthEvent;
        logger
            .warn(this.getClass().getSimpleName() + " caught kiraJobItemStatusConsumerSickEvent={}",
                kiraJobItemStatusConsumerSickEvent);
        this.handleKiraJobItemStatusConsumerSickEvent(kiraJobItemStatusConsumerSickEvent);
        break;
      case KIRAJOBITEMSTATUSCONSUMER_RECOVERED:
        KiraJobItemStatusConsumerRecoveredEvent kiraJobItemStatusConsumerRecoveredEvent = (KiraJobItemStatusConsumerRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(
            this.getClass().getSimpleName() + " caught kiraJobItemStatusConsumerRecoveredEvent={}",
            kiraJobItemStatusConsumerRecoveredEvent);
        this.handleKiraJobItemStatusConsumerRecoveredEvent(kiraJobItemStatusConsumerRecoveredEvent);
        break;
      case KIRACLIENTREGISTERDATACONSUMER_SICK:
        KiraClientRegisterDataConsumerSickEvent kiraClientRegisterDataConsumerSickEvent = (KiraClientRegisterDataConsumerSickEvent) kiraManagerHealthEvent;
        logger.warn(
            this.getClass().getSimpleName() + " caught kiraClientRegisterDataConsumerSickEvent={}",
            kiraClientRegisterDataConsumerSickEvent);
        this.handleKiraClientRegisterDataConsumerSickEvent(kiraClientRegisterDataConsumerSickEvent);
        break;
      case KIRACLIENTREGISTERDATACONSUMER_RECOVERED:
        KiraClientRegisterDataConsumerRecoveredEvent kiraClientRegisterDataConsumerRecoveredEvent = (KiraClientRegisterDataConsumerRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName()
                + " caught kiraClientRegisterDataConsumerRecoveredEvent={}",
            kiraClientRegisterDataConsumerRecoveredEvent);
        this.handleKiraClientRegisterDataConsumerRecoveredEvent(
            kiraClientRegisterDataConsumerRecoveredEvent);
        break;
      case CLUSTER_INTERNAL_CONNECTION_FAILED:
        ClusterInternalConnectionFailedEvent clusterInternalConnectionFailedEvent = (ClusterInternalConnectionFailedEvent) kiraManagerHealthEvent;
        logger.warn(
            this.getClass().getSimpleName() + " caught clusterInternalConnectionFailedEvent={}",
            clusterInternalConnectionFailedEvent);
        this.handleClusterInternalConnectionFailedEvent(clusterInternalConnectionFailedEvent);
        break;
      case CLUSTER_INTERNAL_CONNECTION_RECOVERED:
        ClusterInternalConnectionRecoveredEvent clusterInternalConnectionRecoveredEvent = (ClusterInternalConnectionRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(
            this.getClass().getSimpleName() + " caught clusterInternalConnectionRecoveredEvent={}",
            clusterInternalConnectionRecoveredEvent);
        this.handleClusterInternalConnectionRecoveredEvent(clusterInternalConnectionRecoveredEvent);
        break;
      case TIMER_TRIGGER_SCHEDULE_FAILED:
        TimerTriggerScheduleFailedEvent timerTriggerScheduleFailedEvent = (TimerTriggerScheduleFailedEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught timerTriggerScheduleFailedEvent={}",
            timerTriggerScheduleFailedEvent);
        this.handleTimerTriggerScheduleFailedEvent(timerTriggerScheduleFailedEvent);
        break;
      case TIMER_TRIGGER_SCHEDULE_RECOVERED:
        TimerTriggerScheduleRecoveredEvent timerTriggerScheduleRecoveredEvent = (TimerTriggerScheduleRecoveredEvent) kiraManagerHealthEvent;
        logger
            .warn(this.getClass().getSimpleName() + " caught timerTriggerScheduleRecoveredEvent={}",
                timerTriggerScheduleRecoveredEvent);
        this.handleTimerTriggerScheduleRecoveredEvent(timerTriggerScheduleRecoveredEvent);
        break;
      case RUN_TIMER_TRIGGER_TASK_FAILED:
        RunTimerTriggerTaskFailedEvent runTimerTriggerTaskFailedEvent = (RunTimerTriggerTaskFailedEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName() + " caught runTimerTriggerTaskFailedEvent={}",
            runTimerTriggerTaskFailedEvent);
        this.handleRunTimerTriggerTaskFailedEvent(runTimerTriggerTaskFailedEvent);
        break;
      case RUN_TIMER_TRIGGER_TASK_RECOVERED:
        RunTimerTriggerTaskRecoveredEvent runTimerTriggerTaskRecoveredEvent = (RunTimerTriggerTaskRecoveredEvent) kiraManagerHealthEvent;
        logger
            .warn(this.getClass().getSimpleName() + " caught runTimerTriggerTaskRecoveredEvent={}",
                runTimerTriggerTaskRecoveredEvent);
        this.handleRunTimerTriggerTaskRecoveredEvent(runTimerTriggerTaskRecoveredEvent);
        break;
      case NO_JOBCREATEDANDRUN_FORSOMETIME_FORTIMERTRIGGER:
        NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent = (NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName()
                + " caught noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent={}",
            noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent);
        this.handleNoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent(
            noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent);
        break;
      case CREATEANDRUNJOB_FORTIMERTRIGGER_RECOVERED:
        CreateAndRunJobForTimerTriggerRecoveredEvent createAndRunJobForTimerTriggerRecoveredEvent = (CreateAndRunJobForTimerTriggerRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName()
                + " caught createAndRunJobForTimerTriggerRecoveredEvent={}",
            createAndRunJobForTimerTriggerRecoveredEvent);
        this.handleCreateAndRunJobForTimerTriggerRecoveredEvent(
            createAndRunJobForTimerTriggerRecoveredEvent);
        break;
      case EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_FAILED:
        ExternalOverallMonitorForTimerTriggerFailedEvent externalOverallMonitorForTimerTriggerFailedEvent = (ExternalOverallMonitorForTimerTriggerFailedEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName()
                + " caught externalOverallMonitorForTimerTriggerFailedEvent={}",
            externalOverallMonitorForTimerTriggerFailedEvent);
        this.handleExternalOverallMonitorForTimerTriggerFailedEvent(
            externalOverallMonitorForTimerTriggerFailedEvent);
        break;
      case EXTERNAL_OVERALL_MONITOR_FORTIMERTRIGGER_RECOVERED:
        ExternalOverallMonitorForTimerTriggerRecoveredEvent externalOverallMonitorForTimerTriggerRecoveredEvent = (ExternalOverallMonitorForTimerTriggerRecoveredEvent) kiraManagerHealthEvent;
        logger.warn(this.getClass().getSimpleName()
                + " caught externalOverallMonitorForTimerTriggerRecoveredEvent={}",
            externalOverallMonitorForTimerTriggerRecoveredEvent);
        this.handleExternalOverallMonitorForTimerTriggerRecoveredEvent(
            externalOverallMonitorForTimerTriggerRecoveredEvent);
        break;
      default:
        logger.error(this.getClass().getSimpleName()
            + " caught unknown eventType for kiraManagerHealthEvent=" + kiraManagerHealthEvent);
    }
  }

  private void sendHealthEventEmail(String emailSubject, String emailContent,
      boolean privateOnlyEvent) {
    try {
      Set<String> emailSet = new LinkedHashSet<String>();
      List<String> privateEmailsAsList = KiraCommonUtils
          .getStringListByDelmiter(KiraManagerUtils.getPrivateEmails(),
              KiraCommonConstants.COMMA_DELIMITER);
      emailSet.addAll(privateEmailsAsList);
      if (!privateOnlyEvent) {
        List<String> healthEventReceiverEmailsAsList = KiraCommonUtils
            .getStringListByDelmiter(KiraManagerUtils.getHealthEventReceiverEmails(),
                KiraCommonConstants.COMMA_DELIMITER);
        KiraManagerUtils.addAdminEmailsToList(healthEventReceiverEmailsAsList);
        emailSet.addAll(healthEventReceiverEmailsAsList);
      }

      if (CollectionUtils.isNotEmpty(emailSet)) {
        List<String> emailList = new ArrayList<String>(emailSet);
        EmailUtils.sendInnerEmail(emailList, emailContent, emailSubject);
      } else {
        logger.warn(
            "emailSet is empty. So can not send email. emailSubject={} and emailContent={} and privateOnlyEvent={}",
            emailSubject, emailContent, privateOnlyEvent);
      }
    } catch (Throwable t) {
      logger.error("Error occurs when sendHealthEventEmail. emailSubject=" + emailSubject
          + " and emailContent=" + emailContent + " and privateOnlyEvent=" + privateOnlyEvent);
    }
  }

  private void sendHealthEventSMS(String smsContent, boolean privateOnlyEvent) {
    try {
      Set<String> phoneNumberSet = new LinkedHashSet<String>();
      List<String> privatePhoneNumbersAsList = KiraCommonUtils
          .getStringListByDelmiter(KiraManagerUtils.getPrivatePhoneNumbers(),
              KiraCommonConstants.COMMA_DELIMITER);
      phoneNumberSet.addAll(privatePhoneNumbersAsList);
      if (!privateOnlyEvent) {
        List<String> healthEventReceiverPhoneNumbersAsList = KiraCommonUtils
            .getStringListByDelmiter(KiraManagerUtils.getHealthEventReceiverPhoneNumbers(),
                KiraCommonConstants.COMMA_DELIMITER);
        KiraManagerUtils.addAdminPhoneNumbersToList(healthEventReceiverPhoneNumbersAsList);
        phoneNumberSet.addAll(healthEventReceiverPhoneNumbersAsList);
      }

      if (CollectionUtils.isNotEmpty(phoneNumberSet)) {
        List<String> phoneNumberList = new ArrayList<String>(phoneNumberSet);
        SMSUtils.sendSMS(phoneNumberList, smsContent);
      } else {
        logger.warn(
            "phoneNumberSet is empty. So can not send SMS. smsContent={} and privateOnlyEvent={}",
            smsContent, privateOnlyEvent);
      }
    } catch (Throwable t) {
      logger.error("Error occurs when sendHealthEventSMS. emailContent=" + smsContent
          + " and privateOnlyEvent=" + privateOnlyEvent);
    }
  }

  private void notifyByKiraManagerHealthEvent(KiraManagerHealthEvent kiraManagerHealthEvent,
      boolean privateOnlyEvent) {
    try {
      String host = kiraManagerHealthEvent.getHost();
      MonitorNoticeInfo monitorNoticeInfo = kiraManagerHealthEvent.getMonitorNoticeInfo();

      MonitorContext monitorContext = monitorNoticeInfo.getMonitorContext();
      String monitorTarget = monitorContext.getMonitorTarget();

      boolean isBad = monitorNoticeInfo.isBad();
      boolean isRecovered = monitorNoticeInfo.isRecovered();
      boolean isRepeatNoticeForBad = monitorNoticeInfo.isRepeatNoticeForBad();
      String monitorDetails = monitorNoticeInfo.getMonitorDetails();

      String emailSubject = this
          .getEmailSubject(host, monitorTarget, isBad, isRecovered, isRepeatNoticeForBad);
      String emailContent = this
          .getEmailContent(emailSubject, host, monitorContext, monitorDetails);
      this.sendHealthEventEmail(emailSubject, emailContent, privateOnlyEvent);

      String smsContent = emailSubject;
      this.sendHealthEventSMS(smsContent, privateOnlyEvent);
    } catch (Throwable t) {
      logger.error("Error occurs for notifyByKiraManagerHealthEvent. kiraManagerHealthEvent="
          + KiraCommonUtils.toString(kiraManagerHealthEvent), t);
    }
  }

  private String getEmailContent(String emailSubject, String host, MonitorContext monitorContext,
      String monitorDetails) throws Exception {
    String monitorTarget = monitorContext.getMonitorTarget();
    String monitorTargetDetail = monitorContext.getMonitorTargetDetail();
    Date firstRecentBadTime = monitorContext.getFirstRecentBadTime();
    Date firstRecentGoodTime = monitorContext.getFirstRecentGoodTime();

    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("emailSubject", emailSubject);
    velocityContext.put("host", host);
    velocityContext.put("monitorTarget", monitorTarget);
    velocityContext.put("monitorTargetDetail", monitorTargetDetail);
    velocityContext.put("firstRecentBadTime",
        KiraCommonUtils.getDateAsStringToMsPrecision(firstRecentBadTime));
    velocityContext.put("firstRecentGoodTime",
        KiraCommonUtils.getDateAsStringToMsPrecision(firstRecentGoodTime));
    velocityContext.put("monitorDetails", monitorDetails);
    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    velocityContext.put("env", env);
    String kiraManagerUrlPath = KiraManagerUtils.getKiraManagerUrlPath();
    velocityContext.put("kiraManagerUrlPath", kiraManagerUrlPath);

    VelocityUtils velocityUtils = KiraManagerDataCenter.getVelocityUtils();
    String returnValue = velocityUtils.getMergedContent(velocityContext,
        VelocityUtils.TEMPLATE_NAME_KIRA_MANAGER_HEALTH_EVENT_EMAIL);

    return returnValue;
  }

  private String getEmailSubject(String host, String monitorTarget, boolean isBad,
      boolean isRecovered, boolean isRepeatNoticeForBad) {
    StringBuilder sb = new StringBuilder();

    String env = KiraManagerUtils.getEnvWithZoneIfPossible();
    sb.append("[京东]-[Kira]");
    if (StringUtils.isNotBlank(env)) {
      sb.append("-[").append(env).append("环境]");
    }
    sb.append("-健康检查");
    sb.append("(");
    sb.append(monitorTarget);
    sb.append(")");
    sb.append(" on ");
    sb.append(host);

    if (isBad) {
      if (isRepeatNoticeForBad) {
        sb.append(" still failed!");
      } else {
        sb.append(" failed!");
      }
    } else if (isRecovered) {
      sb.append(" is recovered.");
    } else {
      logger.warn(
          "Monitor data error. May have some bugs. host={} and monitorTarget={} and isBad={} and isRecovered={} and isRepeatNoticeForBad={}",
          host, monitorTarget, isBad, isRecovered, isRepeatNoticeForBad);
    }

    return sb.toString();
  }

  private void handleExternalOverallMonitorForTimerTriggerRecoveredEvent(
      ExternalOverallMonitorForTimerTriggerRecoveredEvent externalOverallMonitorForTimerTriggerRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(externalOverallMonitorForTimerTriggerRecoveredEvent, false);
  }

  private void handleExternalOverallMonitorForTimerTriggerFailedEvent(
      ExternalOverallMonitorForTimerTriggerFailedEvent externalOverallMonitorForTimerTriggerFailedEvent) {
    this.notifyByKiraManagerHealthEvent(externalOverallMonitorForTimerTriggerFailedEvent, false);
  }

  private void handleCreateAndRunJobForTimerTriggerRecoveredEvent(
      CreateAndRunJobForTimerTriggerRecoveredEvent createAndRunJobForTimerTriggerRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(createAndRunJobForTimerTriggerRecoveredEvent, true);
  }

  private void handleNoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent(
      NoJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent) {
    this.notifyByKiraManagerHealthEvent(noJobBeCreatedAndRunForSomeTimeForTimerTriggerEvent, true);
  }

  private void handleRunTimerTriggerTaskRecoveredEvent(
      RunTimerTriggerTaskRecoveredEvent runTimerTriggerTaskRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(runTimerTriggerTaskRecoveredEvent, true);
  }

  private void handleRunTimerTriggerTaskFailedEvent(
      RunTimerTriggerTaskFailedEvent runTimerTriggerTaskFailedEvent) {
    this.notifyByKiraManagerHealthEvent(runTimerTriggerTaskFailedEvent, true);
  }

  private void handleKiraClientRegisterDataConsumerRecoveredEvent(
      KiraClientRegisterDataConsumerRecoveredEvent kiraClientRegisterDataConsumerRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(kiraClientRegisterDataConsumerRecoveredEvent, true);
  }

  private void handleKiraClientRegisterDataConsumerSickEvent(
      KiraClientRegisterDataConsumerSickEvent kiraClientRegisterDataConsumerSickEvent) {
    this.notifyByKiraManagerHealthEvent(kiraClientRegisterDataConsumerSickEvent, true);
  }

  private void handleKiraJobItemStatusConsumerRecoveredEvent(
      KiraJobItemStatusConsumerRecoveredEvent kiraJobItemStatusConsumerRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(kiraJobItemStatusConsumerRecoveredEvent, true);
  }

  private void handleKiraJobItemStatusConsumerSickEvent(
      KiraJobItemStatusConsumerSickEvent kiraJobItemStatusConsumerSickEvent) {
    this.notifyByKiraManagerHealthEvent(kiraJobItemStatusConsumerSickEvent, true);
  }

  private void handleTimerTriggerScheduleRecoveredEvent(
      TimerTriggerScheduleRecoveredEvent timerTriggerScheduleRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(timerTriggerScheduleRecoveredEvent, true);
  }

  private void handleTimerTriggerScheduleFailedEvent(
      TimerTriggerScheduleFailedEvent timerTriggerScheduleFailedEvent) {
    this.notifyByKiraManagerHealthEvent(timerTriggerScheduleFailedEvent, true);
  }

  private void handleClusterInternalConnectionRecoveredEvent(
      ClusterInternalConnectionRecoveredEvent clusterInternalConnectionRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(clusterInternalConnectionRecoveredEvent, true);
  }

  private void handleClusterInternalConnectionFailedEvent(
      ClusterInternalConnectionFailedEvent clusterInternalConnectionFailedEvent) {
    this.notifyByKiraManagerHealthEvent(clusterInternalConnectionFailedEvent, true);
  }

  private void handleDBForMenuRecoveredEvent(
      DBForMenuRecoveredEvent dbForMenuRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(dbForMenuRecoveredEvent, true);
  }

  private void handleDBForMenuUnavailableEvent(
      DBForMenuUnavailableEvent dbForMenuUnavailableEvent) {
    this.notifyByKiraManagerHealthEvent(dbForMenuUnavailableEvent, true);
  }

  private void handleDBForScheduleRecoveredEvent(
      DBForScheduleRecoveredEvent dbForScheduleRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(dbForScheduleRecoveredEvent, true);
  }

  private void handleDBForScheduleUnavailableEvent(
      DBForScheduleUnavailableEvent dbForScheduleUnavailableEvent) {
    this.notifyByKiraManagerHealthEvent(dbForScheduleUnavailableEvent, true);
  }

  private void handleZKForKiraRecoveredEvent(ZKForKiraRecoveredEvent zkForKiraRecoveredEvent) {
    this.notifyByKiraManagerHealthEvent(zkForKiraRecoveredEvent, true);
  }

  private void handleZKForKiraUnavailableEvent(
      ZKForKiraUnavailableEvent zkForKiraUnavailableEvent) {
    this.notifyByKiraManagerHealthEvent(zkForKiraUnavailableEvent, true);
  }

  protected void destroyKiraManagerHealthEventExecutorService() {
    if (null != this.kiraManagerHealthEventExecutorService) {
      this.kiraManagerHealthEventExecutorService.shutdown();
    }
  }

  @Override
  protected void doDestroy() {
    this.destroyKiraManagerHealthEventExecutorService();
  }

  protected class KiraManagerHealthEventHandlerRejectedExecutionHandler implements
      RejectedExecutionHandler {

    private Logger logger = LoggerFactory
        .getLogger(KiraManagerHealthEventHandlerRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      logger.error(
          "KiraManagerHealthEventHandlerRejectedExecutionHandler is triggered. runnable={} and queueSize={} and poolSize={} and corePoolSize={} and maximumPoolSize={} and KeepAliveTimeInSeconds={}",
          runnable, executor.getQueue().size(), executor.getPoolSize(), executor.getCorePoolSize(),
          executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.SECONDS));
      throw new RejectedExecutionException();
    }
  }

  protected class KiraManagerHealthEventHandleTask implements Callable<Object> {

    private KiraManagerHealthEvent kiraManagerHealthEvent;

    public KiraManagerHealthEventHandleTask(KiraManagerHealthEvent kiraManagerHealthEvent) {
      super();
      this.kiraManagerHealthEvent = kiraManagerHealthEvent;
    }

    @Override
    public Object call() throws Exception {
      KiraManagerHealthEventHandleComponent.this
          .handleKiraManagerHealthEvent(kiraManagerHealthEvent);
      return null;
    }
  }

}
