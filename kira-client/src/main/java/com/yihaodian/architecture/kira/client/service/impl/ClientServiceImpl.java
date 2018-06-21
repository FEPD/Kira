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
package com.yihaodian.architecture.kira.client.service.impl;

import com.yihaodian.architecture.kira.client.iface.IJobCancelable;
import com.yihaodian.architecture.kira.client.service.ClientService;
import com.yihaodian.architecture.kira.client.util.JobCancelResult;
import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientServiceImpl implements ClientService, ILifecycle, IJobCancelable {

  static Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

  @Override
  public void runShellMethod(LinkedHashMap<String, String> paramsMap) {

    String scriptPath = paramsMap.get("runShellPath");
    String jobType = paramsMap.get("jobType");
    String scriptName = paramsMap.get("shellName");
    String runScriptPath = null;

    if (StringUtils.isBlank(scriptPath) && StringUtils.isBlank(scriptName)) {
      throw new NullPointerException(
          "running script path or scriptName is not null! please check kira schedule config ");
    }

    if (StringUtils.isNotBlank(scriptPath)) {
      runScriptPath = scriptPath;
    } else {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      runScriptPath = classLoader.getResource(scriptName).getPath();
    }

    List<String> commandsList = new ArrayList<String>();
    commandsList.add("sh");
    commandsList.add(runScriptPath);
    ProcessBuilder processBuilder = new ProcessBuilder(commandsList);
    processBuilder.redirectErrorStream(true);
    BufferedReader br = null;
    StringBuffer linesBuffer = null;

    try {
      Process process = processBuilder.start();
      try {
        //读取标准输出流和标准错误输出流
        br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        linesBuffer = new StringBuffer();
        String line = null;
        while (null != (line = br.readLine())) {
          linesBuffer.append(line).append("\n");
        }

        if (linesBuffer.toString().contains("command not found")) {
          throw new RuntimeException(
              "shell command not fund ! please check shell" + linesBuffer.toString());
        } else if (linesBuffer.toString().contains("No such file or directory")
            || linesBuffer.toString().contains("没有那个文件或目录")) {
          throw new RuntimeException("can not fund execute script: " + linesBuffer.toString());
        }

        int exitValue = process.waitFor();
        if (0 != exitValue) {
          System.out.println("execute script failed: exitValue = "
              + exitValue);
          throw new RuntimeException(
              "execute script failed, please check schedule config : " + linesBuffer.toString());
        }
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("execute script failed:" + e);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.out.println("execute script failed:" + e);
      } finally {
        if (null != br) {
          try {
            br.close();
            br = null;
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("execute script fail: ", e);
    }
  }

  public void init() {

  }

  public void destroy() {

  }

  public JobCancelResult cancelJob(Map<Serializable, Serializable> dataMap) {
    return null;
  }
}
