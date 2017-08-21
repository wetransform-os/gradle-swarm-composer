/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble

/**
 * Default configuration for assemble action.
 *
 * @author Simon Templer
 */
class AssembleDefaultConfig implements AssembleConfig {

  String stackName

  String setupName

  def template

  List<Object> config = []

  def target

}
