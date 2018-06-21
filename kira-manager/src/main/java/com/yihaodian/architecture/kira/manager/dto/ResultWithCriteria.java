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
package com.yihaodian.architecture.kira.manager.dto;

import java.io.Serializable;

public class ResultWithCriteria<R, C> implements Serializable {

  private static final long serialVersionUID = 1L;

  private R result;
  private C criteria;

  public ResultWithCriteria() {
    // TODO Auto-generated constructor stub
  }

  public ResultWithCriteria(R result, C criteria) {
    super();
    this.result = result;
    this.criteria = criteria;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public R getResult() {
    return result;
  }

  public void setResult(R result) {
    this.result = result;
  }

  public C getCriteria() {
    return criteria;
  }

  public void setCriteria(C criteria) {
    this.criteria = criteria;
  }

}
