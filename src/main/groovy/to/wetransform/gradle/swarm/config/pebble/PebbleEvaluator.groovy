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

package to.wetransform.gradle.swarm.config.pebble

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.lexer.LexerImpl;
import com.mitchellbosecke.pebble.lexer.TokenStream;
import com.mitchellbosecke.pebble.loader.StringLoader
import com.mitchellbosecke.pebble.node.RootNode;
import com.mitchellbosecke.pebble.parser.Parser;
import com.mitchellbosecke.pebble.parser.ParserImpl
import com.mitchellbosecke.pebble.template.PebbleTemplate

import to.wetransform.gradle.swarm.actions.assemble.template.ContextWrapper;
import to.wetransform.gradle.swarm.actions.assemble.template.SwarmComposerExtension
import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.ConfigHelper

/**
 * Evaluates config based on Pebble templates.
 *
 * @author Simon Templer
 *
 * @deprecated Use PebbleCachingEvaluator instead.
 */
@Deprecated
class PebbleEvaluator extends AbstractPebbleEvaluator {

  private static final Logger log = LoggerFactory.getLogger(PebbleEvaluator)

  PebbleEvaluator() {
    super()
  }

  PebbleEvaluator(boolean lenient) {
    super(lenient)
  }

  @Override
  public Map<String, Object> evaluate(Map<String, Object> config) {
    Set<String> toEvaluate = new HashSet<>()

    /*
     * FIXME the simple approach used for evaluation here is based on
     * the assumption that evaluation will be replacements, meaning that
     * there can be multiple passes on evaluation.
     * This does not work on conditions however, because they may use
     * the non-evaluated text for their check, which will very likely
     * have a wrong result.
     *
     * XXX Idea: Use DynamicAttributeProvider interface to evaluate
     * dependencies as they are requested? Problem: Not for top level
     * variables.
     */

    Set<String> ignoreKeys = new HashSet<>()
    // do a first evaluation pass
    log.info('First evaluation pass...')
    toEvaluate = doEvaluationPass([], ignoreKeys, config, config)
    // do additional evaluation passes while there is still stuff to be evaluated (maximum 5 passes)
    for (int i = 0; !toEvaluate.empty && i < 5; i++) {
      log.info("Evaluation pass ${i + 2}...")
      toEvaluate = doEvaluationPass([], ignoreKeys, config, config)
    }
    //FIXME improvement: use info on keys still to be evaluated

    if (!toEvaluate.empty && !lenient) {
      throw new IllegalStateException('The following configuration keys could not be evaluated: ' +
        toEvaluate.join(', '))
    }

    return config
  }

  private Set<String> doEvaluationPass(List<String> path, Set<String> ignoreKeys, Map config, Map overall) {
    def keys = new LinkedHashSet(config.keySet())
    Set<String> toEvaluateAfter = new HashSet<>()

    // merge config to make relative (neighboring) resolving possible
    def evalConfig = ConfigHelper.mergeConfigs([config, overall])

    // def evalConfig = overall

    keys.each { key ->
      def value = config[key]

      def childPath = []
      childPath.addAll(path)
      childPath.add(key)
      def childKey = childPath.join('.')

      if (value != null && !ignoreKeys.contains(childKey)) {
        if (value instanceof List) {
          //FIXME lists are not supported ATM
        }
        else if (value instanceof String || value instanceof GString) {
          // evaluate value

          boolean isVerbatim = isVerbatim(value as String)

          def newValue = evaluateValue(value, evalConfig)
          if (isVerbatim) {
            // the wrapping is necessary because in some cases a config
            // is reevaluated later on
            newValue = new VerbatimWrapper(newValue)
          }
          config.put(key, newValue)
          if (isVerbatim) {
            // ignore in future passes
            log.info "Ignoring $childKey"
            ignoreKeys.add(childKey)
          }
          // is the result still dynamic? -> try again in next iteration
          else if (newValue instanceof String && isDynamicValue(newValue)) {
            toEvaluateAfter.add(childKey)
          }
        }
        // proceed to child maps
        else if (value instanceof Map) {
          toEvaluateAfter.addAll(doEvaluationPass(childPath, ignoreKeys, value, overall))
        }
      }
    }

    toEvaluateAfter
  }

  private def evaluateValue(def value, Map context) {
    PebbleTemplate compiledTemplate = engine.getTemplate(value as String);
    StringWriter writer = new StringWriter()
    try {
      if (lenient) {
        compiledTemplate.evaluate(writer, context)
      }
      else {
        compiledTemplate.evaluate(writer, ContextWrapper.create(context))
      }
    } catch (ClassCastException e) {
      //XXX hack: try again next iteration (until for instance boolean is correctly resolved)
      return value
    }
    def result = writer.toString()

    // "hack" to convert to a boolean (for conditions)
    if ('true' == result) {
      true
    }
    else if('false' == result) {
      false
    }
    else {
      result
    }
  }

  /**
   * Determines if there should only be one pass to evaluate this configuration value.
   */
  boolean isVerbatim(String value) {
    //XXX more sophisticated check?
    //XXX for now just a simple check if the whole text is wrapped with verbatim
    if (value != null) {
      String trimmed = value.trim();
      return trimmed.startsWith('{% verbatim %}') && trimmed.endsWith('{% endverbatim %}')
    }
    else {
      return true;
    }
  }

}
