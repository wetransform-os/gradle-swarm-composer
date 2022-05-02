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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;

/**
 * Functions that processes a template like an include and returns it as string.
 *
 * @author Simon Templer
 */
public class ReadTemplateFunction implements Function {

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList("value");
  }

  /* (non-Javadoc)
   * @see com.mitchellbosecke.pebble.extension.Function#execute(java.util.Map, com.mitchellbosecke.pebble.template.PebbleTemplate, com.mitchellbosecke.pebble.template.EvaluationContext, int)
   */
  @Override
  public String execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Object value = args.get("value");

    if (value == null) {
      throw new NullPointerException("Template name/path must be specified");
    }
    else {
      String templateName = value.toString();
      Map<String, Object> addVars = new LinkedHashMap<>();

      StringWriter writer = new StringWriter();
      try {
        ((PebbleTemplateImpl) self).includeTemplate(writer, (EvaluationContextImpl) context, templateName, addVars);
      } catch (IOException e) {
        throw new RuntimeException("Error processing template", e);
      }
      return writer.toString();
    }
  }

}
