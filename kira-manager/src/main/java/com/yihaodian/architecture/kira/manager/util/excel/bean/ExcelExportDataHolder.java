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
package com.yihaodian.architecture.kira.manager.util.excel.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExcelExportDataHolder implements Serializable {

  private List<String> headerLineList = new ArrayList<String>();
  private List<List<String>> dataList = new ArrayList<List<String>>();
  //Make sure the length of dataCellDescriptorList and dataList should be the same if the dataCellDescriptorList is not empty.
  private List<ExcelCellDescriptor> dataCellDescriptorList = new ArrayList<ExcelCellDescriptor>();

  public ExcelExportDataHolder(List<String> headerLineList, List<List<String>> dataList,
      List<ExcelCellDescriptor> dataCellDescriptorList) {
    this.headerLineList = headerLineList;
    this.dataList = dataList;
    this.dataCellDescriptorList = dataCellDescriptorList;
  }

  public List<List<String>> getDataList() {
    return dataList;
  }

  public List<String> getHeaderLineList() {
    return headerLineList;
  }

  public List<ExcelCellDescriptor> getDataCellDescriptorList() {
    return dataCellDescriptorList;
  }

}
