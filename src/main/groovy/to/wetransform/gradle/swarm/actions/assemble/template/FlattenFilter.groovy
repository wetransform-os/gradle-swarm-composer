/*
 * Copyright 2025 wetransform GmbH
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

import io.pebbletemplates.pebble.error.PebbleException
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate

/**
 * Filter that flattens collections. Output it always a collection.
 * If the input is null, an empty collection is returned.
 * If the input is not a collection, it is wrapped in a collection.
 *
 * @author Simon Templer
 */
public class FlattenFilter implements Filter {

  @Override
  public List<String> getArgumentNames() {
    return Collections.emptyList()
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
    int lineNumber) throws PebbleException {
    if (input == null) {
      return Collections.emptyList()
    }

    if (!(input instanceof Collection) && input instanceof Iterable) {
      input = input.asCollection()
    }

    if (input instanceof Collection) {
      return input.flatten()
    }
    else {
      return Collections.singleton(input)
    }
  }
}
