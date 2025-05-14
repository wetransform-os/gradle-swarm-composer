/*
 * Copyright 2024 wetransform GmbH
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

import java.util.*;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import to.wetransform.gradle.swarm.config.ConfigHelper;

/**
 * Filter that merges the input with the argument.
 * Input and argument must both be maps.
 *
 * @author Simon Templer
 */
public class MergeFilter implements Filter {

  private static final String ARG_VALUE = "value";

  private final List<String> argumentNames = new ArrayList<>();

  public MergeFilter() {
    this.argumentNames.add(ARG_VALUE);
  }

  @Override
  public List<String> getArgumentNames() {
    return this.argumentNames;
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self,
    EvaluationContext context, int lineNumber) throws PebbleException {

    // TODO also support merging lists?

    if (input instanceof ContextWrapper) {
      input = ((ContextWrapper) input).getInternalMap();
    }

    if (!(input instanceof Map)) {
      throw new PebbleException(null, "merge filter can only be applied to maps", lineNumber, self.getName());
    }

    Object value = args.get(ARG_VALUE);

    if (value instanceof ContextWrapper) {
      value = ((ContextWrapper) value).getInternalMap();
    }

    if (!(value instanceof Map)) {
      throw new PebbleException(null, "merge filter only accepts a map as argument", lineNumber, self.getName());
    }

    return ConfigHelper.mergeConfigs(Arrays.asList((Map) input, (Map) value));
  }

}
