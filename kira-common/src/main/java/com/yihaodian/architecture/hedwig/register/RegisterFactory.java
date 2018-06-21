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
package com.yihaodian.architecture.hedwig.register;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidReturnValueException;
import com.yihaodian.architecture.hedwig.common.util.HedwigUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Archer Jiang
 */
public class RegisterFactory {

  public static Map<String, String> registers = new HashMap<String, String>();
  private static Logger logger = LoggerFactory.getLogger(RegisterFactory.class);

  static {
    registers.put(InternalConstants.SERVICE_REGISTER_ZK, ServiceProviderZkRegister.class.getName());
  }

  public static IServiceProviderRegister getZKRegister() throws HedwigException {
    return getRegister(ServiceProviderZkRegister.class.getName());
  }

  public static IServiceProviderRegister getRegister(String name) throws HedwigException {
    if (HedwigUtil.isBlankString(name)) {
      throw new InvalidParamException("Balancer name must not null");
    }
    String clazzName = registers.get(name);
    if (clazzName != null) {
      try {
        Class clazz = Class.forName(clazzName);
        return (IServiceProviderRegister) clazz.newInstance();
      } catch (Throwable e) {
        throw new InvalidReturnValueException("Can't find " + clazzName + " balancer");
      }
    } else {
      throw new InvalidReturnValueException("Can't find " + name + " balancer");
    }
  }

}
