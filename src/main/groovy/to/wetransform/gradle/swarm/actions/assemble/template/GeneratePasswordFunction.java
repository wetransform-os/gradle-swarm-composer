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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Function that generates a random password.
 *
 * @author Simon Templer
 */
public class GeneratePasswordFunction implements Function {

  private final SecureRandom random = new SecureRandom();

  private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMERIC = "0123456789";
  private static final String SPECIAL_CHARS = "!@#$%^&*_=+-/";

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList("length", "characters", "alphabetic", "numeric", "special");
  }

  public Object execute(Map<String, Object> args) {
    return execute(args, null, null, -1);
  }

  @Override
  public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    // defaults
    int length = 16;
    String characters = null;

    // configure length
    Object confLength = args.get("length");
    if (confLength != null) {
      if (confLength instanceof Number) {
        length = ((Number) confLength).intValue();
      } else if (confLength instanceof String) {
        length = Integer.parseInt((String) confLength);
      } else {
        throw new IllegalArgumentException("Invalid type for argument 'length': " + confLength.getClass());
      }
    }

    // configure characters
    Object confCharacters = args.get("characters");
    if (confCharacters != null) {
      characters = confCharacters.toString();
    } else if (args.containsKey("alphabetic") || args.containsKey("numeric") || args.containsKey("special")) {
      // flags
      boolean alphabetic = getFlag(args, "alphabetic", false);
      boolean numeric = getFlag(args, "numeric", false);
      boolean special = getFlag(args, "special", false);

      StringBuilder ch = new StringBuilder();
      if (alphabetic) {
        ch.append(ALPHA);
        ch.append(ALPHA_CAPS);
      }
      if (numeric) {
        ch.append(NUMERIC);
      }
      if (special) {
        ch.append(SPECIAL_CHARS);
      }

      characters = ch.toString();
    }

    if (characters == null || characters.isEmpty()) {
      // fall-back
      characters = ALPHA + ALPHA_CAPS + NUMERIC;
    }

    // generate password
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < length; i++) {
      char randChar = characters.charAt(random.nextInt(characters.length()));
      res.append(randChar);
    }

    return res.toString();
  }

  private boolean getFlag(Map<String, Object> args, String name, boolean def) {
    Object obj = args.get(name);

    if (obj instanceof Boolean) {
      return (Boolean) obj;
    } else if (obj instanceof String) {
      return Boolean.parseBoolean((String) obj);
    }

    return def;
  }

}
