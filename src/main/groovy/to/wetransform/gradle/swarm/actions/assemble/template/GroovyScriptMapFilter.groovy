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

package to.wetransform.gradle.swarm.actions.assemble.template

import java.util.List
import java.util.Map

import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

import groovy.transform.CompileStatic

/**
 * Filter for applying a Groovy script to each element of the input.
 */
@CompileStatic
class GroovyScriptMapFilter implements Filter {

  private final GroovyScriptFilter filter

  GroovyScriptMapFilter(File rootDir) {
    this.filter = new GroovyScriptFilter(rootDir)
  }

  @Override
  public List<String> getArgumentNames() {
    return filter.argumentNames
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber)
      throws PebbleException {
    if (input instanceof Map) {
      input.collectEntries {
        filter.apply(it, args, self, context, lineNumber)
      }
    }
    else if (input instanceof Iterable) {
      input.collect {
        filter.apply(it, args, self, context, lineNumber)
      }
    }
    else {
      filter.apply(input, args, self, context, lineNumber)
    }
  }

}
