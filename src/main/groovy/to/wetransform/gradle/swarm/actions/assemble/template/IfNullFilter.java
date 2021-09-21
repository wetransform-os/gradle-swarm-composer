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

import java.util.Map;

import com.mitchellbosecke.pebble.error.AttributeNotFoundException;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.core.DefaultFilter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * Filter that returns a provided default value if the input is <code>null</code>.
 *
 * Extends {@link DefaultFilter} to prevent a possible {@link AttributeNotFoundException}
 * to be thrown (because of internal instanceof check in Pebble).
 *
 * @author Simon Templer
 */
public class IfNullFilter extends DefaultFilter {

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
      int lineNumber) throws PebbleException {
    Object defaultObj = args.get("default");

    if (input == null) {
        return defaultObj;
    }
    return input;
  }

}
