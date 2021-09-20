/*
 * Copyright 2021 wetransform GmbH
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator;
import to.wetransform.gradle.swarm.config.pebble.RootOrLocalMap;

/**
 * Filter for filtering iterables with a custom template expression as predicate.
 *
 * @author Simon Templer
 */
public class PredicateFilter implements Filter {

  public static enum PredicateFilterType {
    FILTER,
    ANY_MATCH,
    ALL_MATCH,
    NONE_MATCH,
    FIRST
  }

  private static final String RESULT_KEY = "___result";

  /**
   * Name of predicate argument.
   */
  public static final String ARGUMENT_PREDICATE = "predicate";

  private final PebbleCachingEvaluator evaluator;

  private final PredicateFilterType type;

  public PredicateFilter(PebbleCachingEvaluator evaluator, PredicateFilterType type) {
    super();
    this.evaluator = evaluator;
    this.type = type;
  }

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList(ARGUMENT_PREDICATE);
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
      int lineNumber) throws PebbleException {
    Object pred = args.get(ARGUMENT_PREDICATE);
    if (pred == null) {
      return input;
    }

    StringBuilder predString = new StringBuilder();
    predString.append("{{ ");
    predString.append(pred);
    predString.append(" }}");

    Predicate<Object> predicate = item -> {
      Map<String, Object> map = new HashMap<>();
      map.put("it", item);
      map.put(RESULT_KEY, predString.toString());

      map = new RootOrLocalMap(map,
          new EvaluationContextMap(context), //TODO instead a Map based on the ScopeChain in EvaluationContextImpl?
          false, false);

      Map<String, Object> eval = evaluator.evaluate(map);

      Object res = eval.get(RESULT_KEY);
      if (res instanceof Boolean) {
        return (boolean) res;
      }
      throw new IllegalStateException("result must be boolean");
    };

    if (input instanceof Map<?, ?>) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      Stream<Entry<?, ?>> stream = StreamSupport.stream(((Map) input).entrySet().spliterator(), false);

      switch(type) {
      case FILTER:
        return stream.filter(predicate).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
      case FIRST:
        return stream.filter(predicate).findFirst().orElse(null);
      case ANY_MATCH:
        return stream.anyMatch(predicate);
      case ALL_MATCH:
        return stream.allMatch(predicate);
      case NONE_MATCH:
        return stream.noneMatch(predicate);
      default:
        throw new IllegalStateException("Unrecognized filter type: " + type.name());
      }
    }
    else if (input instanceof Iterable<?>) {
      Stream<?> stream = StreamSupport.stream(((Iterable<?>) input).spliterator(), false);

      switch(type) {
      case FILTER:
        return stream.filter(predicate).collect(Collectors.toList());
      case FIRST:
        return stream.filter(predicate).findFirst().orElse(null);
      case ANY_MATCH:
        return stream.anyMatch(predicate);
      case ALL_MATCH:
        return stream.allMatch(predicate);
      case NONE_MATCH:
        return stream.noneMatch(predicate);
      default:
        throw new IllegalStateException("Unrecognized filter type: " + type.name());
      }
    }
    else {
      switch(type) {
      case FILTER:
      case FIRST:
        if (predicate.test(input)) return input; else return null;
      case ANY_MATCH:
      case ALL_MATCH:
        return predicate.test(input);
      case NONE_MATCH:
        return !predicate.test(input);
      default:
        throw new IllegalStateException("Unrecognized filter type: " + type.name());
      }
    }
  }

}
