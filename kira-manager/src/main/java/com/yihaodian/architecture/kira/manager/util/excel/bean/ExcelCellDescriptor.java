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

public class ExcelCellDescriptor implements Serializable {

  private Integer cellType; //e.g. HSSFCell.CELL_TYPE_NUMERIC
  private Short alignment; //e.g. HSSFCellStyle.ALIGN_RIGHT

  public ExcelCellDescriptor(Integer cellType, Short alignment) {
    this.cellType = cellType;
    this.alignment = alignment;
  }

  public Short getAlignment() {
    return alignment;
  }

  public void setAlignment(Short alignment) {
    this.alignment = alignment;
  }

  public Integer getCellType() {
    return cellType;
  }

  public void setCellType(Integer cellType) {
    this.cellType = cellType;
  }
}
