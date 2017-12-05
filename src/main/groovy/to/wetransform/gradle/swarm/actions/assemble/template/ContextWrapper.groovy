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

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.text.MessageFormat
import java.util.Iterator;
import java.util.Map
import java.util.stream.Collectors;

import com.mitchellbosecke.pebble.error.AttributeNotFoundException;
import com.mitchellbosecke.pebble.extension.DynamicAttributeProvider
import com.mitchellbosecke.pebble.utils.Pair;;

/**
 * Context wrapper for a map that throws an exception if the map key does not exist.
 * This is to enforce variable to be set. The strict mode in Pebble only seems to work for root variables.
 *
 * @author Simon Templer
 */
public class ContextWrapper implements DynamicAttributeProvider, Iterable<Object> {

  private final String path;

  private final Map contextMap;

  public static Map<String, Object> create(Map<String, Object> contextMap) {
    contextMap.entrySet().stream()
      .map { entry ->
        String key = entry.key;
        Object value = entry.value;
        if (value instanceof Map) {
          value = new ContextWrapper(key, (Map) value);
        }
        return new Pair<>(key, value);
      }
      .collect(Collectors.toMap { p -> p.left} { p -> p.right });
  }

  public ContextWrapper(String path, Map contextMap) {
    super();
    this.path = path;
    this.contextMap = contextMap;
  }

  @Override
  public boolean canProvideDynamicAttribute(Object attributeName) {
    // always say we can -> we want to throw an exception otherwise
    return true;
  }

  @Override
  public Object getDynamicAttribute(Object attributeName, Object[] argumentValues) {
    Object value = contextMap.get(attributeName);
    if (value == null) {
      /*
       * XXX This is an awful hack:
       * If we throw a RuntimeException here, there will always be a failure if
       * the attribute is not there, even if we use a default filter.
       * We only can achieve a proper handling by throwing an
       * AttributeNotFoundException, which we only can do here because Groovy
       * is used.
       */
      String fullName = (path != null) ? (path + '.' + attributeName) : (attributeName)
      throw new AttributeNotFoundException(null,
        MessageFormat.format("Attribute {0} not found in context map", fullName),
        attributeName, 0, 'unknown');
    }
    else if (value instanceof Map) {
      String fullName = (path != null) ? (path + '.' + attributeName) : (attributeName)
      return new ContextWrapper(fullName, (Map) value);
    }
    else {
      return value;
    }
  }

  @Override
  Iterator<Object> iterator() {
    contextMap.entrySet().iterator();
  }

}
