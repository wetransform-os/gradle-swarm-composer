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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

/**
 * Custom Pebble extension.
 *
 * @author Simon Templer
 */
public class SwarmComposerExtension extends AbstractExtension {


  private Map<String, Filter> filters = new HashMap<>();

  public SwarmComposerExtension() {
    super();

    filters.put("indent", new IndentLineFilter());
    filters.put("yaml", new YamlFilter());
    filters.put("json", new JsonFilter());
    filters.put("prettyJson", new PrettyJsonFilter());
  }

  @Override
  public Map<String, Filter> getFilters() {
    return Collections.unmodifiableMap(filters);
  }

}
