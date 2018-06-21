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

package com.yihaodian.architecture.kira.client;

import com.yihaodian.architecture.kira.common.KiraCommonConstants;
import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class ShellMainBootStrap {

  public static final int monitor_port = LoadProertesContainer.provider()
      .getIntProperty("kira.server.listen.port", 8080);

  private ShellMainBootStrap() {

  }

  public static void main(String[] args) {

    String log4j = ShellMainBootStrap.class.getClassLoader().getResource("log4j.properties")
        .getFile();
    System.out.println("Loading log4j config files : " + log4j);
    PropertyConfigurator.configureAndWatch(log4j, 20 * 1000);

    //启动http server
    try {
      Server server = new Server(monitor_port);
      WebAppContext wac = new WebAppContext();
      String appDir = LoadProertesContainer.provider()
          .getProperty(KiraCommonConstants.KIRA_AGENT_WEBAPPS_DIR,
              "/export/App/kira-client-0.0.1-SNAPSHOT-agent");
      String webappDir = System.getProperty("webapp.dir", appDir);
      wac.setResourceBase(webappDir);
      System.out.println(
          "*******************Spring init finished********************" + wac.getResourceBase());
      wac.setDescriptor(wac.getResourceBase() + "/webapps/WEB-INF/web.xml");
      wac.setParentLoaderPriority(true);
      server.setHandler(wac);
      server.start();
      System.out.println("################Jetty Server init finished#################");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}