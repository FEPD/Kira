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
package com.yihaodian.architecture.hedwig.common.util;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Archer
 */
public class InvocationContext implements Cloneable {

  private static List<String> globalKeyList = new ArrayList<String>(5);

  static {
    globalKeyList.add(InternalConstants.HEDWIG_GLOBAL_ID);
    globalKeyList.add(InternalConstants.HEDWIG_REQUEST_HOP);
    globalKeyList.add(PropKeyConstants.HEDWIG_TOKEN_GRAY);
  }

  private Map<String, Object> globalContext = new HashMap<String, Object>();
  private Map<String, Object> localContext = new HashMap<String, Object>();

  private Map<String, Object> getGlobalContext() {
    return globalContext;
  }

  private Map<String, Object> getLocalContext() {
    return localContext;
  }

  public void putValue(String key, Object value) {
    if (key != null && value != null) {
      if (value instanceof String) {
        if (value != null) {
          String str = (String) value;
          str = HedwigUtil.limitString(str, InternalConstants.VALUE_LENGTH_LIMIT);
          value = str;
        }
      }
      if (isGlobalKey(key)) {
        getGlobalContext().put(key, value);
      } else {
        getLocalContext().put(key, value);
      }
    }
  }

  public Object getValue(String key, Object defaultObj) {
    Object value = null;
    if (key != null) {
      if (isGlobalKey(key)) {
        value = getGlobalContext().get(key);
      } else {
        value = getLocalContext().get(key);
      }
    }
    value = value == null ? defaultObj : value;
    return value;
  }

  public String getStrValue(String key, String defValue) {
    Object obj = getValue(key, defValue);
    return obj == null ? "" : obj.toString();//注意：obj.toString()
  }

  private boolean isGlobalKey(String key) {
    boolean result = false;
    if (org.apache.commons.lang.StringUtils.isNotBlank(key)) {
      result = globalKeyList.contains(key);
    }
    return result;
  }

  public void cleanLocalContext() {
    if (getLocalContext() != null) {
      getLocalContext().clear();
    }
  }

  public void cleanGlobalContext() {
    if (getLocalContext() != null) {
      getLocalContext().clear();
    }
    if (getGlobalContext() != null) {
      getGlobalContext().clear();
    }
  }

  @Override
  public InvocationContext clone() {
    InvocationContext result = new InvocationContext();
    result.globalContext.putAll(this.getGlobalContext());
    result.localContext.putAll(this.getLocalContext());
    return result;
  }
}