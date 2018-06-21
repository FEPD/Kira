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

import com.google.gson.Gson;
import com.yihaodian.architecture.hedwig.common.bean.ExecutorInfo;
import com.yihaodian.architecture.hedwig.common.dto.ArgMeta;
import com.yihaodian.architecture.hedwig.common.dto.ArgsMeta;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Archer
 */
public class HedwigMonitorUtil {

  public static Gson gson = new Gson();

  public static String getExceptionClassName(Throwable throwable) {
    String name = "";
    if (throwable != null) {
      Throwable t = throwable;
      Throwable cause = throwable.getCause();
      while (cause != null) {
        t = cause;
        cause = cause.getCause();
      }
      name = t.getClass().getName();
    }
    return name;
  }

  public static String getExceptionMsg(Throwable throwable) {
    StringBuilder msg = new StringBuilder();
    if (throwable != null) {
      msg.append(getErrorMsg(throwable));
      Throwable cause = throwable;
      while (cause != null) {
        msg.append(cause.getClass().getName()).append(":");
        msg.append(cause.getLocalizedMessage()).append("\n");
        cause = cause.getCause();
      }
    }
    return msg.toString();
  }

  public static String getErrorMsg(Throwable error) {
    StringBuilder sb = new StringBuilder();
    if (error != null) {
      StackTraceElement[] traces = error.getStackTrace();
      if (traces != null && traces.length > 0) {
        StackTraceElement cause = traces[0];
        if (cause != null) {
          sb.append("ErrorMsg:").append(error.getLocalizedMessage()).append("\n")
              .append("ClassName:")
              .append(cause.getClassName()).append(";").append(" MethodName:")
              .append(cause.getMethodName())
              .append("; LineNumber:").append(cause.getLineNumber()).append(";").append("\n");
        }
      }
    }
    return sb.toString();
  }

  public static String getThreadPoolInfo(ThreadPoolExecutor tpes) {
    return gson.toJson(new ExecutorInfo(tpes));
  }

  public static ArgsMeta generateMeta(Object[] args) {
    ArgsMeta metas = new ArgsMeta();
    try {
      if (args != null && args.length > 0) {
        int len = args.length;
        for (int i = 0; i < len; i++) {
          Object o = args[i];
          ArgMeta arg = new ArgMeta();
          arg.setPos(i);
          if (o != null) {
            arg.setType(o.getClass().getName());
            int size = 0;
            String subType = "";
            if (o instanceof Collection) {
              Collection c = ((Collection) o);
              size = c.size();
              if (size > 0) {
                subType = getSubType(c);
              }
            } else if (o instanceof Map) {
              Map m = (Map) o;
              size = m.size();
              if (size > 0) {
                subType = getSubType(m.values());
              }
            }
            arg.setSize(size);
            arg.setSubType(subType);
          }
          metas.addArgMeta(arg);
        }
      }
    } catch (Throwable e) {

    }
    return metas;
  }

  public static String getSubType(Collection c) {
    String subType = "";
    if (c != null && c.size() > 0) {
      Object[] arr = c.toArray();
      if (arr != null && arr.length > 0) {
        Object o = arr[0];
        if (o != null) {
          subType = o.getClass().getName();
        }
      }
    }
    return subType;
  }

}
