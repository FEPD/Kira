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
package com.yihaodian.architecture.hedwig.common.constants;

public enum RequestType {
  Direct(0, "direct"),
  SyncInner(1, "syncInner"),
  SyncPool(2, "syncPool"),
  ASync(3, "async"),
  ASyncReliable(4, "asyncReliable"),
  OneWay(5, "oneWay");

  private int index;
  private String name;

  RequestType(int index, String name) {
    this.index = index;
    this.name = name;

  }

  public static RequestType getByIndex(int index) {
    if (index < 0) {
      return RequestType.SyncPool;
    }
    for (RequestType type : RequestType.values()) {
      if (type.getIndex() == index) {
        return type;
      }
    }
    return RequestType.SyncPool;
  }

  public static RequestType getByName(String name) {
    if (name == null) {
      return RequestType.SyncPool;
    }
    for (RequestType type : RequestType.values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    return RequestType.SyncPool;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }
}
