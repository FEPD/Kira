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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author archer
 */
public class StringUtils {

  private static Pattern numPattern = Pattern.compile("[0-9]*");

  public static boolean isNumeric(String str) {
    boolean b = false;
    if (str != null && !str.equals("")) {
      Matcher isNum = numPattern.matcher(str);
      if (isNum.matches()) {
        b = true;
      }
    }
    return b;
  }

}
