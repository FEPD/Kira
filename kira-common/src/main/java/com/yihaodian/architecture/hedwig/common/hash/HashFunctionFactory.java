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
package com.yihaodian.architecture.hedwig.common.hash;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Archer Jiang
 */
public class HashFunctionFactory {

  private static HashFunctionFactory hfFactory = new HashFunctionFactory();

  public Map<String, HashFunction> functionMap = new HashMap<String, HashFunction>();

  private HashFunctionFactory() {
    super();
    functionMap.put(InternalConstants.HASH_FUNCTION_MUR2, new Murmur2());
  }

  public static HashFunctionFactory getInstance() {
    return hfFactory;
  }

  public HashFunction getMur2Function() {
    HashFunction f = null;
    try {
      f = getHashFunction(InternalConstants.HASH_FUNCTION_MUR2);
    } catch (HedwigException e) {
      e.printStackTrace();
    }
    return f;
  }

  public HashFunction getHashFunction(String key) throws HedwigException {
    if (key == null) {
      throw new InvalidParamException("Hash function key must not null!!!");
    }
    if (functionMap.containsKey(key)) {
      return functionMap.get(key);
    } else {
      throw new HedwigException("Hash function key:" + key + " is not support");
    }
  }
}
