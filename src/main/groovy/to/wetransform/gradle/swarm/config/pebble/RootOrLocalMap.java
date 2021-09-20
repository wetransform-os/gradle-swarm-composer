/*
 * Copyright 2021 wetransform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package to.wetransform.gradle.swarm.config.pebble;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Map that delegates to two maps, a root one and a local one, where the later one is
 * intended for local/relative resolving. Resolving via root takes precedence over
 * resolving via the local map.
 *
 * @author Simon Templer
 */
public class RootOrLocalMap implements Map<String, Object> {

  public static final String LOCAL_ACCESS_KEY = "_";

  private final Map<String, Object> root;
  private final Map<String, Object> local;

  private final boolean allowPut;
  private final boolean localAccess;

  public RootOrLocalMap(Map<String, Object> root, Map<String, Object> local, boolean allowPut, boolean localAccess) {
    super();

    if (root == null) throw new IllegalArgumentException("root should not be null");
    if (local == null) throw new IllegalArgumentException("local should not be null");

    this.root = root;
    this.local = local;
    this.allowPut = allowPut;
    this.localAccess = localAccess;
  }

  @Override
  public int size() {
    return keySet().size();
  }

  @Override
  public boolean isEmpty() {
    return root.isEmpty() && local.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return root.containsKey(key) ||  local.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return root.containsValue(value) || local.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    if (localAccess && LOCAL_ACCESS_KEY.equals(key) && !root.containsKey(LOCAL_ACCESS_KEY) && !local.containsKey(LOCAL_ACCESS_KEY)) {
      return local;
    }

    if (root.containsKey(key)) {
      return root.get(key);
    }
    return local.get(key); //FIXME fail if key not contained?
  }

  @Override
  public Object put(String key, Object value) {
    if (allowPut) {
      return root.put(key, value);
    }

    throw new UnsupportedOperationException("Adding element to map not supported (Key " + key + ")");
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    if (allowPut) {
      for (java.util.Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
        root.put(entry.getKey(), entry.getValue());
      }
    }
    else throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    Set<String> keys = new HashSet<>(root.keySet());
    keys.addAll(local.keySet());
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException();
  }

}
