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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.yihaodian.architecture.hedwig.common.dto.ServiceProfile;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigJsonUtil {

  public static final String toJSONString(Object object) {
    SimplePropertyPreFilter filter = new SimplePropertyPreFilter(ServiceProfile.class, "servicePath"
        , "serviceUrl", "protocolPrefix", "urlPattern", "hostIp", "jvmPid", "port", "revision",
        "weighted"
        , "loadRate", "loadThreshold", "curWeight", "status", "curStatus", "relivePolicy",
        "registTime"
        , "assembleAppName", "codecName", "pubZone", "regZone", "pubPoolName", "methodRP",
        "mehodNames");
    return JSON.toJSONString(object, filter);
  }
}
