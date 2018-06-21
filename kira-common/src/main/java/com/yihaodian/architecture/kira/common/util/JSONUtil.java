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

import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JSONUtil {

  /**
   * @see java.lang.Boolean#TYPE
   * @see java.lang.Character#TYPE
   * @see java.lang.Byte#TYPE
   * @see java.lang.Short#TYPE
   * @see java.lang.Integer#TYPE
   * @see java.lang.Long#TYPE
   * @see java.lang.Float#TYPE
   * @see java.lang.Double#TYPE
   * @see java.lang.Void#TYPE
   */
  @SuppressWarnings("rawtypes")
  static final Set<Class> simpleTypeSet = new HashSet<Class>() {
    private static final long serialVersionUID = 1L;

    {
      add(Boolean.class);
      add(Character.class);
      add(Byte.class);
      add(Short.class);
      add(Integer.class);
      add(Long.class);
      add(Float.class);
      add(java.util.Date.class);
      add(java.sql.Date.class);
      add(String.class);
      add(StringBuffer.class);
      add(StringBuilder.class);
      add(BigDecimal.class);
      add(BigInteger.class);
      add(Class.class);

      add(AtomicBoolean.class);
      add(AtomicInteger.class);
      add(AtomicLong.class);

      add(Integer.TYPE);
      add(Boolean.TYPE);
      add(Character.TYPE);
      add(Byte.TYPE);
      add(Short.TYPE);
      add(Long.TYPE);
      add(Float.TYPE);

      add(Class.class);


    }
  };

  public static <T> T parseObject(String source, Class<T> clazz) {
    if (isSimpleType(clazz)) {
      Parser parser = Parser.PARSER_MAP.get(clazz);
      if (parser == null) {
        throw new RuntimeException("暂不支持该类型的字符串值转换为" + clazz.getSimpleName());
      }
      Object result = parser.parse(source);
      return (T) result;
    }
    // 非简单类型的数据，按指定clazz转化

    return (T) JSONObject.parseObject(source, clazz);
  }

  public static boolean isSimpleType(Class clazz) {
    return simpleTypeSet.contains(clazz);
  }
}
