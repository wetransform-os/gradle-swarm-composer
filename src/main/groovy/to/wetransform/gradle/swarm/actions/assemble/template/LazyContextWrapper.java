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
package to.wetransform.gradle.swarm.actions.assemble.template;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps a map and returns {@link ContextWrapper}s for all children of type map.
 *
 * Is used to avoid evaluating all expressions in a {@link PebbleCachingEvaluator}
 *
 * @author Simon Templer
 */
public class LazyContextWrapper implements Map<String, Object> {

  private final Map<String, Object> decoratee;

  public LazyContextWrapper(Map<String, Object> decoratee) {
    super();
    this.decoratee = decoratee;
  }

  @Override
  public int size() {
    return decoratee.size();
  }

  @Override
  public boolean isEmpty() {
    return decoratee.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return decoratee.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    if (value instanceof ContextWrapper) {
      value = ((ContextWrapper) value).getInternalMap();
    }

    return decoratee.containsValue(value);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object get(Object key) {
    Object value = decoratee.get(key);
    if (value instanceof Map) {
      value = new ContextWrapper(key.toString(), (Map) value);
    }
    return value;
  }

  @Override
  public Object put(String key, Object value) {
    return decoratee.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return decoratee.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    decoratee.putAll(m);
  }

  @Override
  public void clear() {
    decoratee.clear();
  }

  @Override
  public Set<String> keySet() {
    return decoratee.keySet();
  }

  @Override
  public Collection<Object> values() {
    return entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return decoratee.entrySet().stream().map(entry -> {
      Object value = entry.getValue();
      if (value instanceof Map) {
        value = new ContextWrapper(entry.getKey(), (Map) value);
      }
      return new AbstractMap.SimpleEntry<String, Object>(entry.getKey(), value);
    }).collect(Collectors.toSet());
  }

}
