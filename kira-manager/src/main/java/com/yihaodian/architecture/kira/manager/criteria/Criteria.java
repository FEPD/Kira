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
package com.yihaodian.architecture.kira.manager.criteria;

import com.yihaodian.architecture.kira.manager.util.Paging;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Criteria implements Serializable {

  private static final long serialVersionUID = -1232337967872094768L;

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected Paging paging;

  private String orderByClause;

  public Criteria() {
    this(new Paging());
  }

  public Criteria(int maxResults) {
    this(new Paging(maxResults));
  }

  public Criteria(Paging paging) {
    this.paging = paging;
  }

  public Paging getPaging() {
    return paging;
  }

  public void setPaging(Paging paging) {
    this.paging = paging;
  }

  public String getOrderByClause() {
    return orderByClause;
  }

  public void setOrderByClause(String orderByClause) {
    this.orderByClause = orderByClause;
  }
}
