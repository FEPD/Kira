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
package com.yihaodian.architecture.hedwig.client.event.handle;

import com.yihaodian.architecture.hedwig.common.util.HedwigMonitorUtil;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Archer
 */
public class HandlerUtil {

  public static Set<String> NETWORK_EXCEPTIONS = null;

  static {
    NETWORK_EXCEPTIONS = new HashSet<String>();
    NETWORK_EXCEPTIONS.add(ConnectException.class.getName());
    NETWORK_EXCEPTIONS.add(SocketException.class.getName());
    NETWORK_EXCEPTIONS.add(IOException.class.getName());
    NETWORK_EXCEPTIONS.add(java.net.SocketTimeoutException.class.getName());
  }

  public static boolean isNetworkException(Throwable exception) {
    boolean value = false;
    String rootCause = HedwigMonitorUtil.getExceptionClassName(exception);
    if (!HedwigUtil.isBlankString(rootCause)) {
      value = NETWORK_EXCEPTIONS.contains(rootCause);
    }
    return value;
  }
}
