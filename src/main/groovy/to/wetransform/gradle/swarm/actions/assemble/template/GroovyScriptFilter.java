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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import to.wetransform.gradle.swarm.config.pebble.RootOrLocalMap;

/**
 * Filter for applying a Groovy script and returning its result.
 *
 * @author Simon Templer
 */
public class GroovyScriptFilter implements Filter {

  /**
   * Cache of script path or script content to parsed script.
   *
   * Note: We assume that the path to a script is never equal to a script content.
   */
  private static Map<String, Script> cachedScripts = new HashMap<>();

  /**
   * Name of the argument that provides the script content.
   */
  public static final String ARGUMENT_SCRIPT = "code";

  /**
   * Name of the argument specifying the script file path.
   */
  public static final String ARGUMENT_PATH = "script";

  /**
   * Name of the variables/binding argument.
   */
  public static final String ARGUMENT_BINDING = "with";

  private File rootDir;

  public GroovyScriptFilter(File rootDir) {
    super();
    this.rootDir = rootDir;
  }

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList(ARGUMENT_SCRIPT, ARGUMENT_BINDING, ARGUMENT_PATH);
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
      int lineNumber) throws PebbleException {
    Object scriptValue = args.get(ARGUMENT_SCRIPT);
    Object pathValue = args.get(ARGUMENT_PATH);
    if (pathValue == null && scriptValue == null) {
      return input;
    }

    boolean usePath = pathValue != null;

    String scriptContentOrPath;
    File scriptFile;
    if (usePath) {
      // load script from file
      scriptContentOrPath = ReadFileFunction.resolvePath((PebbleTemplateImpl) self, rootDir, pathValue.toString());
      scriptFile = new File(scriptContentOrPath);
      if (!scriptFile.exists()) {
        // check if adding .groovy extension helps
        String otherPath = scriptContentOrPath + ".groovy";
        File otherFile = new File(otherPath);
        if (otherFile.exists()) {
          scriptFile = otherFile;
          scriptContentOrPath = otherPath;
        }
        else throw new PebbleException(null, "Could not find script file " + scriptFile.getAbsolutePath());
      }
    }
    else {
      // use provided script
      scriptContentOrPath = scriptValue.toString();
      scriptFile = null;
    }

    // map for binding
    Map<String, Object> map = new HashMap<>();

    // add local binding
    Object bindingValue = args.get(ARGUMENT_BINDING);
    if (bindingValue instanceof Map) {
      for (Entry<?, ?> entry : ((Map<?, ?>) bindingValue).entrySet()) {
        map.put(entry.getKey().toString(), entry.getValue());
      }
    }

    // add it
    map.put("it", input);

    // add template context
    map = new RootOrLocalMap(map,
        new EvaluationContextMap(context), //TODO instead a Map based on the ScopeChain in EvaluationContextImpl?
        false, false);

    Binding binding = new Binding(map);

    final File fFile = scriptFile;
    Script script = cachedScripts.computeIfAbsent(scriptContentOrPath, id -> {
      GroovyShell shell = createShell(binding);
      try {
        if (fFile != null) {
          return shell.parse(fFile);
        }
        else {
          return shell.parse(id);
        }
      } catch (CompilationFailedException | IOException e) {
        throw new PebbleException(e, "Error parsing script for filter");
      }
    });

    synchronized (script) {
      script.setBinding(binding);
      return script.run();
    }
  }

  private GroovyShell createShell(Binding binding) {
    CompilerConfiguration cc = new CompilerConfiguration();
    return new GroovyShell(binding, cc);
  }

}
