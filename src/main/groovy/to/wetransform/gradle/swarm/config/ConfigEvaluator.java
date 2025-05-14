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
package to.wetransform.gradle.swarm.config;

import java.util.Map;

/**
 * Interface for classes processing configuration maps and evaluating dynamic expressions.
 *
 * @author Simon Templer
 */
public interface ConfigEvaluator {

  /**
   * Evaluate the given configuration and return the evaluated version of the configuration.
   * The evaluator may mutate the given configuration and return it or create a new configuration.
   *
   * @param config
   *          the configuration
   * @return the evaluated configuration
   */
  Map<String, Object> evaluate(Map<String, Object> config) throws Exception;

}
