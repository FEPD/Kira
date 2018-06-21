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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityUtils implements ILifecycle {

  public static final String TEMPLATE_NAME_JOB_ALARM_EMAIL = "/template/jobAlarmEmail.vm";
  public static final String TEMPLATE_NAME_JOB_ALARM_SMS = "/template/jobAlarmSMS.vm";
  public static final String TEMPLATE_NAME_JOBITEM_ALARM_EMAIL = "/template/jobItemAlarmEmail.vm";
  public static final String TEMPLATE_NAME_JOBITEM_ALARM_SMS = "/template/jobItemAlarmSMS.vm";
  public static final String TEMPLATE_NAME_JOB_TIMEOUT_ALARM_EMAIL = "/template/jobTimeoutAlarmEmail.vm";
  public static final String TEMPLATE_NAME_JOB_TIMEOUT_ALARM_SMS = "/template/jobTimeoutAlarmSMS.vm";
  public static final String TEMPLATE_NAME_KIRA_MANAGER_HEALTH_EVENT_EMAIL = "/template/kiraManagerHealthEventEmail.vm";
  private static Logger logger = LoggerFactory.getLogger(VelocityUtils.class);

  public VelocityUtils() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public String getMergedContent(VelocityContext velocityContext, String templateName) {
    String returnValue = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    OutputStreamWriter writer = null;
    try {
      Template template = Velocity.getTemplate(templateName);
      byteArrayOutputStream = new ByteArrayOutputStream();
      writer = new OutputStreamWriter(byteArrayOutputStream, KiraServerConstants.DEFAULT_CHARSET);
      template.merge(velocityContext, writer);
      writer.flush();
      returnValue = byteArrayOutputStream.toString(KiraServerConstants.DEFAULT_CHARSET);
    } catch (Exception e) {
      logger.error("Error occurs for getMergedContent. velocityContext=" + velocityContext
          + " and templateName=" + templateName, e);
    } finally {
      if (null != byteArrayOutputStream) {
        try {
          byteArrayOutputStream.close();
        } catch (IOException e) {
          logger.error("IOException occurs for byteArrayOutputStream.close(). velocityContext="
              + velocityContext + " and templateName=" + templateName, e);
        }
      }
      if (null != writer) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.error("IOException occurs for writer.close(). velocityContext=" + velocityContext
              + " and templateName=" + templateName, e);
        }
      }
    }

    return returnValue;
  }

  @Override
  public void init() {
    logger.info("VelocityUtils init.");
    InputStream inputStream = null;
    try {
      Properties p = new Properties();
      inputStream = VelocityUtils.class.getResourceAsStream("/velocity.properties");
      if (null != inputStream) {
        p.load(inputStream);
        Velocity.init(p);
        KiraManagerDataCenter.setVelocityUtils(this);
      } else {
        throw new RuntimeException("Failed to get inputstream from /velocity.properties.");
      }
    } catch (Exception e) {
      logger.error("Error occurs when VelocityUtils init.", e);
    } finally {
      if (null != inputStream) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.error("Error occurs when inputStream.close.", e);
        }
      }
    }

  }

  @Override
  public void destroy() {
    logger.info("VelocityUtils destroy.");

  }

}
