/*
 * Copyright 2018 wetransform GmbH
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

import com.mitchellbosecke.pebble.extension.Filter;

import groovy.json.JsonOutput;

/**
 * Filter that produces JSON from input.
 *
 * @author Simon Templer
 */
public class PrettyJsonFilter implements Filter {

  @Override
  public List<String> getArgumentNames() {
    return Collections.emptyList();
  }

  @Override
  public Object apply(Object input, Map<String, Object> args) {
    if (input instanceof ContextWrapper) {
      input = ((ContextWrapper) input).getInternalMap();
    }

    return JsonOutput.prettyPrint(JsonOutput.toJson(input));
  }

}
