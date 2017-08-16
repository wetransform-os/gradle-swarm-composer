/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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
   * @param config the configuration
   * @return the evaluated configuration
   */
  Map<String, Object> evaluate(Map<String, Object> config);

}
