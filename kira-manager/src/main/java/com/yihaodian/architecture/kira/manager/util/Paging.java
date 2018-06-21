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

import java.io.Serializable;

/**
 * Paging utility.
 *
 * <pre>
 * Paging paging = new Paging();
 * // set total results to 100, current page to 3.
 * paging.setTotalResults(100);
 * paging.setCurrentPage(3);
 * </pre>
 */
public class Paging implements Serializable {

  public static final int INITED_CURRENT_PAGE = 1;
  public static final int DEFAULT_MAX_RESULTS = 10;
  private static final long serialVersionUID = 2439137595310411069L;
  private boolean inited = false;

  private int currentPage = INITED_CURRENT_PAGE;

  private int maxResults = DEFAULT_MAX_RESULTS;

  private int totalResults = 0;

  public Paging() {
    this(DEFAULT_MAX_RESULTS);
  }

  public Paging(int maxResults) {
    if (maxResults < 1) {
      throw new IllegalArgumentException("maxResults must not smaller than 1 but " + maxResults);
    }

    this.maxResults = maxResults;
  }

  public int getFirstResult() {
    return Math.max((getCurrentPage() - 1) * maxResults, 0);
  }

  public int getFirstRownum() {
    return getFirstResult() + 1;
  }

  public int getLastRownum() {
    return getFirstResult() + maxResults;
  }

  public int getFirstPage() {
    return Math.min(totalResults, 1);
  }

  public int getCurrentResultNum() {
    return getTotalResults() - ((getCurrentPage() - 1) * getMaxResults());
  }

  public int getLastPage() {
    return (int) Math.ceil((double) totalResults / maxResults);
  }

  public int getCurrentPage() {
    if (isInited()) {
      return Math.min(currentPage, getLastPage());
    } else {
      return currentPage;
    }
  }

  public void setCurrentPage(int currentPage) {
    if (currentPage < 1) {
      return;
    }

    this.currentPage = currentPage;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int maxResults) {
    if (maxResults < 1) {
      return;
    }

    this.maxResults = maxResults;
  }

  public int getTotalResults() {
    return totalResults;
  }

  public void setTotalResults(int totalResults) {
    this.totalResults = totalResults;
    this.inited = true;
  }

  public boolean isInited() {
    return inited;
  }

  public void setInited(boolean inited) {
    this.inited = inited;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
        "(" +
        "firstPage=" + "'" + getFirstPage() + "'" + ", " +
        "lastPage=" + "'" + getLastPage() + "'" + ", " +
        "currentPage=" + "'" + getCurrentPage() + "'" + ", " +
        ")";
  }

}

