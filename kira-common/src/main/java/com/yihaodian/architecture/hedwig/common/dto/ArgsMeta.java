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
package com.yihaodian.architecture.hedwig.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Archer
 */
public class ArgsMeta implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -8950704891888406880L;

  List<ArgMeta> argsMeta = new ArrayList<ArgMeta>();

  public List<ArgMeta> getArgsMeta() {
    return argsMeta;
  }

  public void setArgsMeta(List<ArgMeta> argsMeta) {
    this.argsMeta = argsMeta;
  }

  public void addArgMeta(ArgMeta argMeta) {
    this.argsMeta.add(argMeta);
  }

}
