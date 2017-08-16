/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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
import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.ConfigHelper

/**
 * Evaluates config based on Pebble templates.
 *
 * @author Simon Templer
 */
class PebbleEvaluator implements ConfigEvaluator {

  private final PebbleEngine engine = new PebbleEngine.Builder()
    .newLineTrimming(true)
    .strictVariables(true)
    .autoEscaping(false)
    .loader(new StringLoader())
    .build()

  private static final Logger log = LoggerFactory.getLogger(PebbleEvaluator)

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
     */

    // do a first evaluation pass
    toEvaluate = doEvaluationPass([], config, config)
    // do additional evaluation passes while there is still stuff to be evaluated (maximum 5 passes)
    for (int i = 0; !toEvaluate.empty || i < 5; i++) {
      toEvaluate = doEvaluationPass([], config, config)
    }
    //FIXME improvement: use info on keys still to be evaluated

    if (!toEvaluate.empty) {
      throw new IllegalStateException('The following configuration keys could not be evaluated: ' +
        toEvaluate.join(', '))
    }

    return config
  }

  private Set<String> doEvaluationPass(List<String> path, Map config, Map overall) {
    def keys = new LinkedHashSet(config.keySet())
    Set<String> toEvaluateAfter = new HashSet<>()

    // merge config to make relative (neighboring) resolving possible?
    //XXX not working, mabye due to the self-references -> using overall config instead
    def evalConfig = ConfigHelper.mergeConfigs([config, overall])

    // def evalConfig = overall

    keys.each { key ->
      def value = config[key]

      def childPath = []
      childPath.addAll(path)
      childPath.add(key)
      def childKey = childPath.join('.')

      if (value != null) {
        if (value instanceof List) {
          //FIXME lists are not supported ATM
        }
        else if (value instanceof String || value instanceof GString) {
          // evaluate value

          def newValue = evaluateValue(value, evalConfig)
          config.put(key, newValue)
          // is the result still dynamic? -> try again in next iteration
          if (newValue instanceof String && isDynamicValue(newValue)) {
            toEvaluateAfter.add(childKey)
          }
        }
        // proceed to child maps
        else if (value instanceof Map) {
          toEvaluateAfter.addAll(doEvaluationPass(childPath, value, overall))
        }
      }
    }

    toEvaluateAfter
  }

  private def evaluateValue(def value, Map context) {
    PebbleTemplate compiledTemplate = engine.getTemplate(value as String);
    StringWriter writer = new StringWriter()
    compiledTemplate.evaluate(writer, ContextWrapper.create(context))
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

  boolean isDynamicValue(String value) {
    //XXX this function uses Pebble internal API

    try {
      LexerImpl lexer = new LexerImpl(engine.syntax, engine.extensionRegistry.getUnaryOperators().values(),
        engine.extensionRegistry.getBinaryOperators().values())
      Reader templateReader = new StringReader(value)
      TokenStream tokenStream = lexer.tokenize(templateReader, 'dynamic')

      Parser parser = new ParserImpl(engine.extensionRegistry.getUnaryOperators(),
        engine.extensionRegistry.getBinaryOperators(), engine.extensionRegistry.getTokenParsers());
      RootNode root = parser.parse(tokenStream)
      def visitor = new DynamicCheckVisitor()
      root.accept(visitor)
      visitor.dynamic
    } catch (e) {
      log.warn("Could not determine if expression is dynamic: $value", e)
      false
    }
  }

}
