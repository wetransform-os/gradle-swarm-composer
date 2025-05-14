/*
 * Copyright 2017 wetransform GmbH
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
package to.wetransform.gradle.swarm.actions.assemble.template

import java.text.MessageFormat
import java.util.Iterator
import java.util.Map
import java.util.stream.Collectors

import io.pebbletemplates.pebble.error.AttributeNotFoundException
import io.pebbletemplates.pebble.utils.Pair

/**
 * Context wrapper for a map that throws an exception if the map key does not exist.
 * This is to enforce variable to be set. The strict mode in Pebble only seems to work for root variables.
 *
 * @author Simon Templer
 */
public class ContextWrapper implements Iterable<Object> {

  private final String path

  private final Map contextMap

  public static Map<String, Object> create(Map<String, Object> contextMap) {
    contextMap.entrySet().stream()
      .map { entry ->
        String key = entry.key
        Object value = entry.value
        if (value instanceof Map) {
          value = new ContextWrapper(key, (Map) value)
        }
        return new Pair<>(key, value)
      }
      .collect(Collectors.toMap { p -> p.left} { p -> p.right })
  }

  public ContextWrapper(String path, Map contextMap) {
    super()
    this.path = path
    this.contextMap = contextMap
  }

  public Object getDynamicAttribute(Object attributeName, Object[] argumentValues) throws AttributeNotFoundException {
    Object value = contextMap.get(attributeName)
    if (value == null) {
      String fullName = (path != null) ? (path + '.' + attributeName) : (attributeName)
      String knownKeys = contextMap.keySet().stream().collect(Collectors.joining(', '))
      throw new AttributeNotFoundException(null,
      MessageFormat.format("Attribute {0} not found in context map. Known keys are {1}", fullName, knownKeys),
      attributeName, 0, 'unknown')
    }
    else if (value instanceof Map) {
      String fullName = (path != null) ? (path + '.' + attributeName) : (attributeName)
      return new ContextWrapper(fullName, (Map) value)
    }
    else {
      return value
    }
  }

  /**
   * Get a property.
   *
   * Implemented to support getting a property from Groovy.
   *
   * @param name the property name
   * @return the property value
   */
  public Object getProperty(String name) {
    return getDynamicAttribute(name, null)
  }

  @Override
  Iterator<Object> iterator() {
    contextMap.entrySet().iterator()
  }

  Map getInternalMap() {
    return contextMap
  }
}
