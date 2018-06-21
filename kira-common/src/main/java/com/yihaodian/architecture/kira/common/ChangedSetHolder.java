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
package com.yihaodian.architecture.kira.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.commons.collections.CollectionUtils;

public class ChangedSetHolder<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;
  private LinkedHashSet<T> oldSet;
  private LinkedHashSet<T> newSet;

  private LinkedHashSet<T> addedSet = null;
  private LinkedHashSet<T> deletedSet = null;

  private volatile boolean theSetChanged;

  /**
   * @param oldSet ensure the cloned oldSet
   * @param newSet ensure the cloned newSet
   */
  public ChangedSetHolder(LinkedHashSet<T> oldSet, LinkedHashSet<T> newSet) {
    if (null == oldSet) {
      this.oldSet = new LinkedHashSet<T>();
    } else {
      this.oldSet = oldSet;
    }
    if (null == newSet) {
      this.newSet = new LinkedHashSet<T>();
    } else {
      this.newSet = newSet;
    }

    Collection<T> addedCollection = CollectionUtils.subtract(this.newSet, this.oldSet);
    addedSet = new LinkedHashSet<T>();
    addedSet.addAll(addedCollection);
    Collection<T> deletedCollection = CollectionUtils.subtract(this.oldSet, this.newSet);
    deletedSet = new LinkedHashSet<T>();
    deletedSet.addAll(deletedCollection);

    if (CollectionUtils.isNotEmpty(addedSet) || CollectionUtils.isNotEmpty(deletedSet)) {
      this.theSetChanged = true;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public LinkedHashSet<T> getOldSet() {
    if (null == oldSet) {
      oldSet = new LinkedHashSet<T>();
    }
    return oldSet;
  }

  public LinkedHashSet<T> getNewSet() {
    if (null == newSet) {
      newSet = new LinkedHashSet<T>();
    }
    return newSet;
  }

  public LinkedHashSet<T> getAddedSet() {
    if (null == addedSet) {
      addedSet = new LinkedHashSet<T>();
    }
    return addedSet;
  }

  public LinkedHashSet<T> getDeletedSet() {
    if (null == deletedSet) {
      deletedSet = new LinkedHashSet<T>();
    }
    return deletedSet;
  }

  public boolean isTheSetChanged() {
    return this.theSetChanged;
  }

  @Override
  public String toString() {
    return "ChangedSetHolder [oldSet=" + oldSet + ", newSet=" + newSet
        + ", addedSet=" + addedSet + ", deletedSet=" + deletedSet
        + ", theSetChanged=" + theSetChanged + "]";
  }

}
