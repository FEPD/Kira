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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.StringUtils;

public interface Parser {

  @SuppressWarnings("rawtypes")
  static Map<Class, Parser> PARSER_MAP = new HashMap<Class, Parser>() {
    private static final long serialVersionUID = 1L;

    {
      put(Boolean.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Boolean.parseBoolean(source);
        }
      });
      put(Character.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          if (source.length() > 1) {
            throw new RuntimeException("无法将" + source + "转化为Character类型。");
          }

          return source.charAt(0);
        }
      });
      put(Byte.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Byte.parseByte(source);
        }
      });
      put(Short.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Short.parseShort(source);
        }
      });
      put(Integer.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Integer.parseInt(source);
        }
      });
      put(Long.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Long.parseLong(source);
        }
      });
      put(Float.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Float.parseFloat(source);
        }
      });
      put(java.util.Date.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new java.util.Date(Long.valueOf(source));
        }
      });
      put(java.sql.Date.class, new Parser() {
        @SuppressWarnings("deprecation")
        public Object parse(String source) {
          return new java.sql.Date(Date.parse(source));
        }
      });
      put(String.class, new Parser() {
        public Object parse(String source) {
          return source;
        }
      });
      put(StringBuffer.class, new Parser() {
        public Object parse(String source) {
          return new StringBuffer(source);
        }
      });
      put(StringBuilder.class, new Parser() {
        public Object parse(String source) {
          return new StringBuilder(source);
        }
      });
      put(BigDecimal.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new BigDecimal(source);
        }
      });

      put(BigInteger.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new BigInteger(source);
        }
      });
      put(AtomicBoolean.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new AtomicBoolean(Boolean.valueOf(source));
        }
      });
      put(AtomicInteger.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new AtomicInteger(Integer.valueOf(source));
        }
      });
      put(AtomicLong.class, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return new AtomicLong(Long.valueOf(source));
        }
      });

      put(Integer.TYPE, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Integer.parseInt(source);
        }
      });
      put(Boolean.TYPE, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Boolean.valueOf(source);
        }
      });
      put(Character.TYPE, new Parser() {
        public Object parse(String source) {
          if (source == null || "null".equals(source) || "".equals(source)) {
            return null;
          }
          if (source.length() > 1) {
            throw new RuntimeException("无法将" + source + "转化为Character类型。");
          }

          return source.charAt(0);
        }
      });

      put(Byte.TYPE, new Parser() {
        public Object parse(String source) {
          return Byte.parseByte(source);
        }
      });
      put(Short.TYPE, new Parser() {
        public Object parse(String source) {
          return Short.valueOf(source);
        }
      });
      put(Long.TYPE, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Long.valueOf(source);
        }
      });
      put(Float.TYPE, new Parser() {
        public Object parse(String source) {
          if (StringUtils.isEmpty(source) || StringUtils.equals(source, "null")) {
            return null;
          }
          return Float.valueOf(source);
        }
      });

      put(Class.class, new Parser() {
        public Object parse(String source) {
          Object result = null;
          try {
            result = Class.forName(source);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
          return result;
        }
      });
    }
  };

  Object parse(String source);
}
