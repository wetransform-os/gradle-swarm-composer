/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mitchellbosecke.pebble.extension.Filter;

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
  public Object apply(Object input, Map<String, Object> args) {
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
