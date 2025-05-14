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

import org.yaml.snakeyaml.Yaml;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Filter that parses YAML from input an input string to a map/list structure.
 *
 * @author Simon Templer
 */
public class ParseYamlFilter implements Filter {

  @Override
  public List<String> getArgumentNames() {
    return Collections.emptyList();
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
    int lineNumber) throws PebbleException {
    if (input == null) {
      return null;
    }

    Yaml yaml = new Yaml();
    String toParse = input.toString();
    try {
      return yaml.load(toParse); // TODO also support loading multiple yaml documents with loadAll?
    } catch (Exception e) {
      // include string to parse in exception for easier debugging
      throw new PebbleException(e, "Error parsing Yaml:\n" + toParse);
    }
  }

}
