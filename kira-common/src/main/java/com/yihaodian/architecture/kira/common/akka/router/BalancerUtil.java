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
package com.yihaodian.architecture.kira.common.akka.router;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Archer
 */
public class BalancerUtil {

  public static Collection<HostInfo> filte(Collection<HostInfo> profiles,
      Collection<String> whiteList) {
    Collection<HostInfo> groupedProfiles = new ArrayList<HostInfo>();
    if (whiteList == null) {
      groupedProfiles = profiles;
    } else {
      if (whiteList.size() > 0) {
        for (HostInfo sp : profiles) {
          try {
            String process = sp.getIp();
            if (whiteList.contains(process)) {
              groupedProfiles.add(sp);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return groupedProfiles;
  }
}
