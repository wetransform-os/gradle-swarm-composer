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
import java.util.AbstractMap.SimpleEntry
import java.util.Collection
import java.util.Map
import java.util.Set
import java.util.function.Function
import java.util.stream.Collectors

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

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import to.wetransform.gradle.swarm.actions.assemble.template.ContextWrapper;
import to.wetransform.gradle.swarm.actions.assemble.template.ExpandFilter
import to.wetransform.gradle.swarm.actions.assemble.template.LazyContextWrapper
import to.wetransform.gradle.swarm.actions.assemble.template.SwarmComposerExtension
import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.ConfigHelper

/**
 * Evaluates configuration based on Pebble templates.
 * Map entries are evaluated lazily if possible and the result is cached.
 *
 * @author Simon Templer
 */
@CompileStatic
public class PebbleCachingEvaluator extends AbstractPebbleEvaluator {

  class PebbleCachingConfig implements Map<String, Object> {

    private final Map<String, Object> original

    private final Map<String, Object> evaluated

    private final PebbleCachingConfig root

    PebbleCachingConfig(Map<String, Object> original, PebbleCachingConfig root) {
      if (original instanceof PebbleCachingConfig) throw new IllegalStateException('Cannot wrap a PebbleCachingConfig (would result in multiple evaluations)')

      this.original = original
      this.evaluated = new HashMap<>()
      this.root = root
    }

    private Object evaluate(Object key) {
      def value = original.get(key)

      return evaluateObject(value)
    }

    private def evaluateObject(Object value) {
      if (value == null) {
        return null
      }
      else if (value instanceof PebbleCachingConfig) {
        // prevent double evaluation
        return value
      }
      else if (value instanceof Map) {
        return new PebbleCachingConfig(value, root ?: this)
      }
      else if (value instanceof List) {
        return value.collect { Object obj ->
          this.evaluateObject(obj)
        }.toList()
      }
      else if (value instanceof String || value instanceof GString) {
        return evaluateValue(value as String)
      }
      else {
        // leave as-is
        return value
      }
    }

    private def evaluateValue(String value) {
      Map context
      if (root == null) {
        // this is root
        context = this
      }
      else {
        // this is local
        context = new RootOrLocalMap(root, this, false, true)
      }

      PebbleTemplate compiledTemplate = PebbleCachingEvaluator.this.engine.getTemplate(value);
      StringWriter writer = new StringWriter()
      if (PebbleCachingEvaluator.this.lenient) {
        compiledTemplate.evaluate(writer, context)
      }
      else {
        compiledTemplate.evaluate(writer, new LazyContextWrapper(context))
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
        ExpandFilter.expandString(result)
      }
    }

    @Override
    public int size() {
      return original.size()
    }

    @Override
    public boolean isEmpty() {
      return original.isEmpty()
    }

    @Override
    public boolean containsKey(Object key) {
      return original.containsKey(key)
    }

    @Override
    public boolean containsValue(Object value) {
      throw new UnsupportedOperationException('Not implemented')
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    @Override
    public Object get(Object key) {
      return evaluated.computeIfAbsent(key, this.&evaluate)
    }

    @Override
    public Object put(String key, Object value) {
      throw new UnsupportedOperationException('Not implemented')
    }

    @Override
    public Object remove(Object key) {
      throw new UnsupportedOperationException('Not implemented')
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
      throw new UnsupportedOperationException('Not implemented')
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException('Not implemented')
    }

    @Override
    public Set<String> keySet() {
      return original.keySet();
    }

    @Override
    public Collection<Object> values() {
      // Note: This method results in evaluation of all entries (via entrySet)

      return entrySet().collect{ it.value }
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
      // Note: This method results in evaluation of all entries

      Set<java.util.Map.Entry<String, Object>> result = new LinkedHashSet()

      for (String key : keySet()) {
        Object value = this.get(key)
        result.add(new SimpleEntry(key, value))
      }

      return result
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      return new LinkedHashMap(this);
    }

  }

  public PebbleCachingEvaluator() {
    this(null)
  }

  public PebbleCachingEvaluator(File rootDir) {
    super(rootDir)
  }

  public PebbleCachingEvaluator(boolean lenient, File rootDir) {
    super(lenient, rootDir)
  }

  public PebbleCachingEvaluator(boolean lenient, SwarmComposerExtension sce) {
    super(lenient, sce)
  }

  @Override
  public Map<String, Object> evaluate(Map<String, Object> config) {
    init()

    return new PebbleCachingConfig(config, null)
  }

}
