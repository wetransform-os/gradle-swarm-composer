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
import java.util.List;
import java.util.Map;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Functions that runs a Groovy script.
 *
 * @author Simon Templer
 */
public class GroovyScriptFunction implements Function {

  private final GroovyScriptFilter filter;

  /**
   * @param rootDir
   *          the project root directory for resolving absolute references
   */
  public GroovyScriptFunction(File rootDir) {
    this.filter = new GroovyScriptFilter(rootDir);
  }

  @Override
  public List<String> getArgumentNames() {
    return filter.getArgumentNames();
  }

  /*
   * (non-Javadoc)
   *
   * @see io.pebbletemplates.pebble.extension.Function#execute(java.util.Map,
   * io.pebbletemplates.pebble.template.PebbleTemplate, io.pebbletemplates.pebble.template.EvaluationContext, int)
   */
  @Override
  public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    return filter.apply(null, args, self, context, lineNumber);
  }

}
