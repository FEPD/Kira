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
package com.yihaodian.architecture.hedwig.common.config;

import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import java.util.Hashtable;

/**
 * @author Archer
 */
public class ConfigCenterHelper {


  private Hashtable<String, String> properites = new Hashtable<String, String>();

  public ConfigCenterHelper(String group, String file) {
    init(group, file);
  }

  public Hashtable<String, String> getProperites() {
    return properites;
  }

  private void init(String group, String file) {
    LoadProertesContainer.provider().loadFromClassPath(file);
  }

  public String getProperty(String key, String defaultValue) {
    String value = LoadProertesContainer.provider().getProperty(key, defaultValue);
    return value == null ? defaultValue : value;
  }


}
