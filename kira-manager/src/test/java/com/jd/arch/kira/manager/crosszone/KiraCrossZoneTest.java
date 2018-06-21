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
package com.jd.arch.kira.manager.crosszone;

import com.yihaodian.architecture.kira.common.crossmultizone.util.KiraCrossMultiZoneConstants;
import com.yihaodian.architecture.kira.common.util.LoadProertesContainer;
import com.yihaodian.architecture.zkclient.ZkClient;
import com.yihaodian.architecture.zkclient.exception.ZkException;
import com.yihaodian.architecture.zkclient.exception.ZkInterruptedException;
import com.yihaodian.architecture.zkclient.exception.ZkNodeExistsException;
import org.junit.Test;

public class KiraCrossZoneTest {

  private  ZkClient zkClient =  new ZkClient("10.182.24.91:2181,10.182.24.92:2181,10.182.24.93:2181,10.182.24.94:2181,10.182.24.95:2181");

  @Test
  private  void setKiraMasterZoneForTest() {

    try {
      if(!zkClient.exists(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_ROOT)){
        try{
          zkClient.createPersistent(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_ROOT, true);
        } catch(ZkNodeExistsException nodeExistsException) {
          System.out.println("ZkNodeExistsException occurs. message="+nodeExistsException.getMessage()+
              ". Just ignore this exception. This node may be created by someone.");
        }
      }

      String kiraMasterZone = LoadProertesContainer.provider().getProperty("kira.masterZone", null);
      if(!zkClient.exists(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE)){
        try{
          zkClient.createPersistent(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE, kiraMasterZone);
        } catch(ZkNodeExistsException nodeExistsException) {
          System.out.println("ZkNodeExistsException occurs. message="+nodeExistsException.getMessage()+
              ". Just ignore this exception. This node may be created by someone.");
        }
      } else {
        zkClient.writeData(KiraCrossMultiZoneConstants.ZK_PATH_CROSS_MULTI_ZONE_MASTER_ZONE, kiraMasterZone);
      }

    } catch (ZkInterruptedException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (ZkException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }

}
