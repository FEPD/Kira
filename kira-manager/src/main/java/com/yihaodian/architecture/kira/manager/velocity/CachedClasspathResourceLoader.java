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
package com.yihaodian.architecture.kira.manager.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedClasspathResourceLoader extends ResourceLoader {

  private static Logger logger = LoggerFactory.getLogger(CachedClasspathResourceLoader.class);
  private Map<String, URL> urlMap = new HashMap<String, URL>();

  public CachedClasspathResourceLoader() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void init(ExtendedProperties configuration) {
  }

  public synchronized InputStream getResourceStream(String resourceName)
      throws ResourceNotFoundException {
    try {
      URL url = getURL(resourceName);

      if (url == null) {
        throw new ResourceNotFoundException("Can not find resource: "
            + resourceName);
      }
      return url.openStream();
    } catch (IOException e) {
      throw new ResourceNotFoundException("Can not find resource: "
          + resourceName + " - Reason: " + e.getMessage());
    }
  }

  public long getLastModified(Resource res) {
    try {
      URL url = getURL(res.getName());
      long lm = url.openConnection().getLastModified();
      return lm;
    } catch (Exception e) {
      logger.error("Error for getLastModified. res=" + res, e);
      return 0;
    }
  }

  public boolean isSourceModified(Resource res) {
    long lastModified = getLastModified(res);
    return (lastModified != res.getLastModified());
  }

  private URL getURL(String rn) {
    if (urlMap.containsKey(rn)) {
      return (URL) urlMap.get(rn);
    }

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = this.getClass().getClassLoader();
    }

    URL url = classLoader.getResource(rn);

    if (url != null) {
      urlMap.put(rn, url);
    }

    return url;
  }

}
