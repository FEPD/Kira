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

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.constants.PropKeyConstants;
import com.yihaodian.architecture.hedwig.common.util.SystemUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer Jiang
 */
public class ProperitesContainer {

  public static Properties HEDWIG_ENV = new Properties();
  private static Logger logger = LoggerFactory.getLogger(ProperitesContainer.class);
  private static ProperitesContainer container;

  private static String fileName = InternalConstants.PROPERITIES_FILE_NAME;

  private ProperitesContainer() {
  }

  public static synchronized ProperitesContainer client() {
    if (container == null) {
      container = new ProperitesContainer();
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_PROVIDER);
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_CLIENT);
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_ZKCLUSTER);
      container.load();
    }
    return container;
  }

  public static synchronized ProperitesContainer provider() {
    if (container == null) {
      container = new ProperitesContainer();
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_ZKCLUSTER);
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_PROVIDER);
      container.loadFromConfigCenter(InternalConstants.CONFIG_GROUP,
          InternalConstants.CONFIG_FILE_CLIENT);
      container.load();
    }
    return container;
  }

  public static void pullAll(Map<String, String> map) {
    HEDWIG_ENV.putAll(map);
  }

  private void loadFromConfigCenter(String group, String file) {
    ConfigCenterHelper cc = new ConfigCenterHelper(group, file);
    if (cc != null) {
      HEDWIG_ENV.putAll(cc.getProperites());
    }
    createListener(group, file, this);
  }

  public void loadFromClassPath() {
    InputStream input = null;
    Properties p = new Properties();
    String path = System.getProperty(InternalConstants.PROPERITIES_PATH_KEY);
    try {
      if (path != null) {
        File file = new File(path);
        if (file.exists()) {
          input = new FileInputStream(file);
        } else {
          input = loadFileFromClasspath();
        }
      } else {
        input = loadFileFromClasspath();
      }
      if (input != null) {
        p.load(input);
        if (!p.isEmpty()) {
          HEDWIG_ENV.putAll(p);
        } else {
          logger.debug("Load properties file:" + fileName + " failed!!!");
        }
      }
    } catch (IOException e) {
      logger.error("Load properties file:" + fileName + " failed!!!", e);
      System.exit(1);
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
      }
    }
  }

  private InputStream loadFileFromClasspath() throws IOException {
    InputStream input = null;
    ClassLoader clzLoader = this.getClass().getClassLoader();
    URL url = clzLoader.getSystemResource(fileName);
    if (url != null) {
      input = url.openStream();
    } else {
      input = clzLoader.getSystemResourceAsStream(fileName);
    }
    return input;
  }

  public void load() {
    loadFromClassPath();
    HEDWIG_ENV.put(PropKeyConstants.JVM_PID, SystemUtil.getJvmPid());
    HEDWIG_ENV.put(PropKeyConstants.HOST_IP, SystemUtil.getLocalhostIp());
  }

  public String getProperty(String key) {
    return HEDWIG_ENV.getProperty(key);
  }

  public String getProperty(String key, String defValue) {
    String value = getProperty(key);
    return value == null ? defValue : value.trim();
  }

  public int getIntProperty(String key, int defValue) {
    int v = defValue;
    String value = getProperty(key);
    if (value != null) {
      try {
        v = Integer.valueOf(value.trim());
      } catch (Exception e) {
      }
    }
    return v;
  }

  public long getLongProperty(String key, long defValue) {
    long v = defValue;
    String value = getProperty(key);
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
    String strValue = getProperty(key);
    if (strValue != null) {
      strValue = strValue.trim();
      if (strValue.equalsIgnoreCase("true")) {
        value = true;
      }
    }
    return value;
  }

  public void createListener(String group_name, String config_file_name,
      ProperitesContainer container) {
		/*YConfigurationDynamicBean dynamicBean = new YConfigurationDynamicBean(group_name, config_file_name);

		ProperitesListener pl = new ProperitesListener();

		dynamicBean.addListener(container, pl);

		dynamicBean.startListeners();*/
  }

}
