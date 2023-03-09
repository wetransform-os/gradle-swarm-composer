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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

  /**
   * Name of the argument specifying the script file path.
   */
  public static final String ARGUMENT_PATH = "path";

  /**
   * Name of the variables/binding argument.
   */
  public static final String ARGUMENT_BINDING = "with";

  private final File rootDir;

  /**
   * @param rootDir the project root directory for resolving absolute references
   */
  public ReadTemplateFunction(File rootDir) {
    this.rootDir = rootDir;
  }

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList(ARGUMENT_PATH, ARGUMENT_BINDING);
  }

  /* (non-Javadoc)
   * @see com.mitchellbosecke.pebble.extension.Function#execute(java.util.Map, com.mitchellbosecke.pebble.template.PebbleTemplate, com.mitchellbosecke.pebble.template.EvaluationContext, int)
   */
  @Override
  public String execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Object value = args.get(ARGUMENT_PATH);

    if (value == null) {
      throw new NullPointerException("Template name/path must be specified");
    }
    else {
      String templateName = value.toString();
      Map<String, Object> addVars = new LinkedHashMap<>();

      // add local binding
      Object bindingValue = args.get(ARGUMENT_BINDING);
      if (bindingValue instanceof Map) {
        for (Entry<?, ?> entry : ((Map<?, ?>) bindingValue).entrySet()) {
          addVars.put(entry.getKey().toString(), entry.getValue());
        }
      }

      StringWriter writer = new StringWriter();
      boolean absolutePath = false;
      try {
        if (templateName.startsWith("/")) {
          // absolute path
          absolutePath = true;
          templateName = ReadFileFunction.resolvePath((PebbleTemplateImpl) self, rootDir, templateName);
        }
        ((PebbleTemplateImpl) self).includeTemplate(writer, (EvaluationContextImpl) context, templateName, addVars);
      } catch (IOException e) {
        throw new RuntimeException("Error processing template", e);
      }
      String result = writer.toString();
      if (absolutePath && templateName.equals(result)) {
        /*
         * If the result is equal to the resolved absolute path, we very likely have the case
         * that a string loader is used for the original template, so it can actually not include
         * file templates.
         * Instead, we load the file content and use that as template name.
         */
        try {
          writer = new StringWriter();
          Path templateFile = Paths.get(templateName);
          String templateContent = Files.readAllLines(templateFile, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
          ((PebbleTemplateImpl) self).includeTemplate(writer, (EvaluationContextImpl) context, templateContent, addVars);
          result = writer.toString();
        } catch (IOException e) {
          throw new RuntimeException("Error processing template using string loader", e);
        }
      }

      return result;
    }
  }

}
