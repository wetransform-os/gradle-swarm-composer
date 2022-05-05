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
 */
abstract class AbstractPebbleEvaluator implements ConfigEvaluator {

  protected PebbleEngine engine
  private final SwarmComposerExtension extension

  private static final Logger log = LoggerFactory.getLogger(AbstractPebbleEvaluator)

  protected final boolean lenient

  AbstractPebbleEvaluator(File rootDir) {
    this(false, rootDir)
  }

  AbstractPebbleEvaluator(boolean lenient, File rootDir) {
    this(lenient, new SwarmComposerExtension(lenient, rootDir))
  }

  AbstractPebbleEvaluator(boolean lenient, SwarmComposerExtension extension) {
    super()
    this.lenient = lenient
    this.extension = extension
  }

  protected void init() {
    if (engine == null) {
      engine = new PebbleEngine.Builder()
      .newLineTrimming(true)
      .strictVariables(!lenient)
      .autoEscaping(false)
      .extension(this.extension)
      .loader(new StringLoader())
      .build()
    }
  }

  boolean isDynamicValue(String value) {
    init()
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

  Collection<List<String>> getDependencies(String value) {
    init()
    //XXX this function uses Pebble internal API

    try {
      LexerImpl lexer = new LexerImpl(engine.syntax, engine.extensionRegistry.getUnaryOperators().values(),
        engine.extensionRegistry.getBinaryOperators().values())
      Reader templateReader = new StringReader(value)
      TokenStream tokenStream = lexer.tokenize(templateReader, 'dynamic')

      Parser parser = new ParserImpl(engine.extensionRegistry.getUnaryOperators(),
        engine.extensionRegistry.getBinaryOperators(), engine.extensionRegistry.getTokenParsers());
      RootNode root = parser.parse(tokenStream)
      def visitor = new DependencyCollectorVisitor()
      root.accept(visitor)
      visitor.dependencies
    } catch (e) {
      log.warn("Could not determine if expression is dynamic: $value", e)
      false
    }
  }

}
