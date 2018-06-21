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
package com.yihaodian.architecture.kira.common.akka.router;

import java.io.Serializable;

/**
 * Created by zhoufeiqiang on 18/09/2017.
 */
public class HostInfo implements Serializable {

  private static final long serialVersionUID = -4034290450185196595L;
  private String ip;
  private int port;
  private String connect;

  public HostInfo(String ip, int port) {
    this.ip = ip;
    this.port = port;
    this.connect = ip + ":" + port;
  }

  public HostInfo(String connect) {
    String[] arr = connect.split(":");
    this.connect = connect;
    this.ip = arr[0];
    this.port = Integer.parseInt(arr[1]);
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getConnect() {
    return connect;
  }

  public void setConnect(String connect) {
    this.connect = connect;
  }

  @Override
  public String toString() {
    return "HostInfo{" +
        "ip='" + ip + '\'' +
        ", port=" + port +
        ", connect='" + connect + '\'' +
        '}';
  }
}
