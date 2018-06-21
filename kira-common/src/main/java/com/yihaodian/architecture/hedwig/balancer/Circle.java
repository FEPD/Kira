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
package com.yihaodian.architecture.hedwig.balancer;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Archer
 */
public class Circle<K, V> extends TreeMap<K, V> {

  /**
   *
   */
  private static final long serialVersionUID = 3884218896481073020L;

  public Circle() {
    super();
  }

  public Circle(Comparator<? super K> comparator) {
    super(comparator);
  }

  public Circle(Map<? extends K, ? extends V> m) {
    super(m);
  }

  public Circle(SortedMap<K, ? extends V> m) {
    super(m);
  }

  public V firstVlue() {
    V v = null;
    Map.Entry<K, V> entry = super.firstEntry();
    if (entry != null) {
      v = entry.getValue();
    }
    return v;
  }

  public V lastValue() {
    V v = null;
    Map.Entry<K, V> entry = super.lastEntry();
    if (entry != null) {
      v = entry.getValue();
    }
    return v;
  }

  public V lowerValue(K key) {
    V v = null;
    K k = lowerKey(key);
    if (k != null) {
      v = get(k);
    }
    return v;
  }

  public V higherValue(K key) {
    V v = null;
    K k = higherKey(key);
    if (k != null) {
      v = get(k);
    }
    return v;
  }

  @Override
  public Map.Entry<K, V> lowerEntry(K key) {
    Map.Entry<K, V> entry = super.lowerEntry(key);
    if (entry == null) {
      entry = super.lastEntry();
    }
    return entry;
  }

  @Override
  public K lowerKey(K key) {
    K k = super.lowerKey(key);
    if (k == null) {
      k = super.lastKey();
    }
    return k;
  }

  @Override
  public Map.Entry<K, V> floorEntry(K key) {
    Map.Entry<K, V> entry = super.floorEntry(key);
    if (entry == null) {
      entry = super.lastEntry();
    }
    return entry;
  }

  @Override
  public K floorKey(K key) {
    K k = super.floorKey(key);
    if (k == null) {
      k = super.lastKey();
    }
    return k;
  }

  @Override
  public Map.Entry<K, V> higherEntry(K key) {
    Map.Entry<K, V> entry = super.higherEntry(key);
    if (entry == null) {
      entry = super.firstEntry();
    }
    return entry;
  }

  @Override
  public K higherKey(K key) {
    K k = super.higherKey(key);
    if (k == null) {
      k = super.firstKey();
    }
    return k;
  }

  public void removeValue(V value) {
    if (value != null) {
      for (Map.Entry<K, V> e = firstEntry(); e != null; higherEntry(e.getKey())) {
        if (e.getValue() != null && value.equals(e.getValue())) {
          super.remove(e.getKey());
        }
      }
    }

  }
}
