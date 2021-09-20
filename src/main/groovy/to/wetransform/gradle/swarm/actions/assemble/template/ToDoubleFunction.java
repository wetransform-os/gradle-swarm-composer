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

import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * Simple function that tries to convert a value to a double.
 *
 * @author Simon Templer
 */
public class ToDoubleFunction implements Function {

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList("value");
  }

  @Override
  public Double execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Number result;
    Object value = args.get("value");
    if (value instanceof Number) {
      result = (Number) value;
    }
    else {
      result = Double.valueOf(value.toString());
    }
    return result.doubleValue();
  }

}
