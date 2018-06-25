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
package com.yihaodian.architecture.kira.common.util;

import com.yihaodian.architecture.kira.common.InternalConstants;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadProertesContainer {

  private static Properties InternalProperties = new Properties();
  private static LoadProertesContainer loadProertesContainer;
  private Logger logger = LoggerFactory.getLogger(LoadProertesContainer.class);

  public LoadProertesContainer() {

  }

  public static synchronized LoadProertesContainer provider() {
    if (loadProertesContainer == null) {
      loadProertesContainer = new LoadProertesContainer();
      loadProertesContainer
          .loadFromClassPath(InternalConstants.CONFIG_FILENAME_KIRASERVERPROPERTIES);
      loadProertesContainer.loadFromClassPath(InternalConstants.CONFIG_ZOOKEEPER_CLITER);
    }
    return loadProertesContainer;
  }

  public void loadFromClassPath(String file) {
    InputStream input = null;
    Properties p = new Properties();
    ClassLoader cloader = this.getClass().getClassLoader();
    URL url = cloader.getSystemResource(file);
    try {
      if (url != null) {
        input = url.openStream();
      } else {
        input = cloader.getSystemResourceAsStream(file);
        if (input == null) {
          input = cloader.getResourceAsStream(file);
        }
      }
      if (input != null) {
        p.load(input);
        if (!p.isEmpty()) {
          InternalProperties.putAll(p);
        }
      }
    } catch (Exception e) {
      logger.error("load classpath is fail! " + file, e);
      System.exit(1);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public String getProperty(String key, String defaultVaule) {
    String value = InternalProperties.getProperty(key);
    return value == null ? defaultVaule : value.trim();
  }

  public int getIntProperty(String key, int defalutValue) {
    String value = InternalProperties.getProperty(key);
    int v = defalutValue;
    if (value != null) {
      try {
        v = Integer.valueOf(value);
      } catch (Exception e) {

      }
    }
    return v;
  }


  public long getLongProperty(String key, long defValue) {
    long v = defValue;
    String value = InternalProperties.getProperty(key);
    if (value != null) {
      try {
        v = Long.valueOf(value.trim());
      } catch (Exception e) {
      }
    }
    return v;
  }

  public boolean getBoolean(String key, boolean defValue) {
    boolean value = defValue;
    String strValue = InternalProperties.getProperty(key);
    if (strValue != null) {
      strValue = strValue.trim();
      if (strValue.equalsIgnoreCase("true")) {
        value = true;
      }
    }
    return value;
  }


}
