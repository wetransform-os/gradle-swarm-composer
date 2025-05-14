/*
 * Copyright 2022 wetransform GmbH
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Filter that uses Json encoding to facilitate copying configuration into types that are not only string or boolean.
 * It uses a magix prefix and suffix for identification.
 *
 * Note that recreating objects from the filter result is only possible if the expanded object is the only content of
 * the string.
 *
 * @author Simon Templer
 */
public class ExpandFilter implements Filter {

  public static final String PREFIX = "___GSC_EXPAND(";
  public static final String SUFFIX = ")___EXPAND_GSC";

  public static Object expandString(String text) {
    if (text != null && text.startsWith(PREFIX) && text.endsWith(SUFFIX)) {
      String core = text.substring(PREFIX.length(), text.length() - SUFFIX.length());
      try {
        return new JsonSlurper().parseText(core);
      } catch (Exception e) {
        throw new RuntimeException("Unable to parse Json expected from expand filter", e);
      }
    }

    return text;
  }

  @Override
  public List<String> getArgumentNames() {
    return Collections.emptyList();
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
    int lineNumber) throws PebbleException {
    if (input instanceof ContextWrapper) {
      input = ((ContextWrapper) input).getInternalMap();
    }

    StringBuilder result = new StringBuilder();
    result.append(PREFIX);
    result.append(JsonOutput.toJson(input));
    result.append(SUFFIX);

    return result.toString();
  }

}
