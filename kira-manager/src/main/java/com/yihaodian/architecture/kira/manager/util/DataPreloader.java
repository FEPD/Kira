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
package com.yihaodian.architecture.kira.manager.util;

import com.yihaodian.architecture.kira.common.iface.ILifecycle;
import com.yihaodian.architecture.kira.manager.domain.Operation;
import com.yihaodian.architecture.kira.manager.service.OperationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class DataPreloader implements ILifecycle {

  private static Logger logger = LoggerFactory.getLogger(DataPreloader.class);

  private OperationService operationService;

  public DataPreloader() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    logger.info("init for DataPreloader");
    List<Operation> allNotReadonlyOperations = operationService.getAllNotReadonlyOperations();
    KiraManagerDataCenter.setAllNotReadonlyOperations(allNotReadonlyOperations);
    if (!CollectionUtils.isEmpty(allNotReadonlyOperations)) {
      Map<String, Operation> nameOperationMap = new LinkedHashMap<String, Operation>();
      for (Operation operation : allNotReadonlyOperations) {
        String name = operation.getName();
        nameOperationMap.put(name, operation);
      }
      KiraManagerDataCenter.setNameNotReadonlyOperationMap(nameOperationMap);
    }
  }

  @Override
  public void destroy() {
    logger.info("destroy for DataPreloader");
  }

  public OperationService getOperationService() {
    return operationService;
  }

  public void setOperationService(OperationService operationService) {
    this.operationService = operationService;
  }

}
