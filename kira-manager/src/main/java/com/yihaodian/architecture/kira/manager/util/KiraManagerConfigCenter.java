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

import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KiraManagerConfigCenter {

  private static Logger logger = LoggerFactory.getLogger(KiraManagerConfigCenter.class);

  private static KiraManagerConfigCenter kiraManagerConfigCenter;
  private Hashtable<String, String> kiraManagerProperties = new Hashtable<String, String>();

  private KiraManagerConfigCenter() {
    // TODO Auto-generated constructor stub
  }

  public static String getProperty(String key, String defaultValue) {
    KiraManagerConfigCenter.getKiraManagerConfigCenter();
    String value = LoadProertesContainer.provider().getProperty(key, defaultValue);
    return null == value ? defaultValue : value;
  }

  private static synchronized KiraManagerConfigCenter getKiraManagerConfigCenter() {
    if (null == kiraManagerConfigCenter) {
      kiraManagerConfigCenter = new KiraManagerConfigCenter();
    }
    return kiraManagerConfigCenter;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  private Hashtable<String, String> getKiraManagerProperties() {
    return kiraManagerProperties;
  }

  private void init() {

  }

}
