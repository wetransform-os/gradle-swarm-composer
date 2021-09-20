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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * Filter that indents new lines.
 *
 * @author Simon Templer
 */
public class IndentLineFilter implements Filter {

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList("number");
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
      int lineNumber) throws PebbleException {
    if (input == null) {
      return null;
    }

    Object numberVal = args.get("number");
    int number;
    if (numberVal == null) {
      number = 0;
    }
    else if (numberVal instanceof Number) {
      number = ((Number) numberVal).intValue();
    }
    else {
      number = Integer.parseInt(numberVal.toString());
    }

    if (number == 0) {
      // ignore
      return input;
    }

    String str = input.toString();

    String preChar = " ";
    StringBuilder prefixBuilder = new StringBuilder();
    for (int i = 0; i < number; i++) {
      prefixBuilder.append(preChar);
    }
    String prefix = prefixBuilder.toString();

    String[] lines = str.split("\\r?\\n");
    for (int i = 1; i < lines.length; i++) {
      lines[i] = prefix + lines[i];
    }

    return Arrays.asList(lines).stream().collect(Collectors.joining("\n"));
  }

}
