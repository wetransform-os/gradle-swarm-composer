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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.pebbletemplates.pebble.template.EvaluationContext;

/**
 * Map backed by an evaluation context.
 *
 * @author Simon Templer
 */
public class EvaluationContextMap implements Map<String, Object> {

  private final EvaluationContext context;

  public EvaluationContextMap(EvaluationContext context) {
    super();
    this.context = context;
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsKey(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(Object key) {
    return context.getVariable(key.toString());
  }

  @Override
  public Object put(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
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
