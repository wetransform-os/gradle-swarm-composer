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

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.extension.Test;

import to.wetransform.gradle.swarm.actions.assemble.template.PredicateFilter.PredicateFilterType;
import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator;

/**
 * Custom Pebble extension.
 *
 * @author Simon Templer
 */
public class SwarmComposerExtension extends AbstractExtension {

  private Map<String, Filter> filters = new HashMap<>();
  private Map<String, Function> functions = new HashMap<>();
  private Map<String, Test> tests = new HashMap<>();
  private List<AttributeResolver> resolvers = new ArrayList<>();

  private final boolean lenient;

  private boolean smartFiltersInitialized = false;

  public SwarmComposerExtension(boolean lenient, File rootDir) {
    super();
    this.lenient = lenient;

    filters.put("indent", new IndentLineFilter());
    filters.put("yaml", new YamlFilter());
    filters.put("parseYaml", new ParseYamlFilter());
    filters.put("json", new JsonFilter());
    filters.put("expand", new ExpandFilter());
    filters.put("prettyJson", new PrettyJsonFilter());
    filters.put("ifNull", new IfNullFilter());
    filters.put("orError", new OrErrorFilter());
    filters.put("merge", new MergeFilter());

    filters.put("apply", new GroovyScriptFilter(rootDir));
    filters.put("map", new GroovyScriptMapFilter(rootDir));
    functions.put("run", new GroovyScriptFunction(rootDir));

    functions.put("generatePassword", new GeneratePasswordFunction());

    functions.put("toInt", new ToIntFunction());
    functions.put("toDouble", new ToDoubleFunction());
    functions.put("toBoolean", new ToBooleanFunction());
    functions.put("checkVersion", new VersionIsAtLeastFunction());
    functions.put("file", new ReadFileFunction(rootDir));
    functions.put("file64", new ReadFileBase64Function(rootDir));
    functions.put("template", new ReadTemplateFunction(rootDir));

    resolvers.add(new ContextWrapperResolver());

    tests.put("Number", new IsNumberTest());
    tests.put("String", new IsStringTest());
    tests.put("Map", new IsMapTest());
    tests.put("List", new IsListTest());
  }

  @Override
  public Map<String, Filter> getFilters() {
    synchronized (this) {
      if (!smartFiltersInitialized) {
        PebbleCachingEvaluator evaluator = new PebbleCachingEvaluator(lenient, this);
        filters.put("filter", new PredicateFilter(evaluator, PredicateFilterType.FILTER));
        filters.put("anyMatch", new PredicateFilter(evaluator, PredicateFilterType.ANY_MATCH));
        filters.put("allMatch", new PredicateFilter(evaluator, PredicateFilterType.ALL_MATCH));
        filters.put("noneMatch", new PredicateFilter(evaluator, PredicateFilterType.NONE_MATCH));
        filters.put("findFirst", new PredicateFilter(evaluator, PredicateFilterType.FIRST));

        smartFiltersInitialized = true;
      }
    }

    return Collections.unmodifiableMap(filters);
  }

  @Override
  public Map<String, Function> getFunctions() {
    return Collections.unmodifiableMap(functions);
  }

  @Override
  public Map<String, Test> getTests() {
    return Collections.unmodifiableMap(tests);
  }

  @Override
  public List<AttributeResolver> getAttributeResolver() {
    return Collections.unmodifiableList(resolvers);
  }

}
