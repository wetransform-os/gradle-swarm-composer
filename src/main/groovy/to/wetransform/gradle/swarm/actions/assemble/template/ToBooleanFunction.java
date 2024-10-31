/*
 * Copyright 2019 wetransform GmbH
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

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Simple function that tries to convert a value to a boolean.
 *
 * @author Simon Templer
 */
public class ToBooleanFunction implements Function {

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList("value");
  }

  /* (non-Javadoc)
   * @see io.pebbletemplates.pebble.extension.Function#execute(java.util.Map, io.pebbletemplates.pebble.template.PebbleTemplate, io.pebbletemplates.pebble.template.EvaluationContext, int)
   */
  @Override
  public Boolean execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Object value = args.get("value");
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    else if (value instanceof String) {
      String lower = ((String) value).toLowerCase();
      if (lower.equals("true")) {
        return true;
      }
      if (lower.equals("false")) {
        return false;
      }
    }

    throw new IllegalArgumentException("Cannot convert value to boolean: " + value);
  }

}
