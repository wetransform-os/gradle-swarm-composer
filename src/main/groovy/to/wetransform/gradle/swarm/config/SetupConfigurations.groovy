/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config

/**
 * Collection of configurations for setups and stacks.
 *
 * @author Simon Templer
 */
class SetupConfigurations {

  private final Map<String, Map<String, SetupConfiguration>> setups = [:]

  void add(SetupConfiguration setup) {
    def stacks = setups[setup.setupName]
    if (stacks == null) {
      stacks = [:]
      setups[setup.setupName] = stacks
    }
    stacks[setup.stackName] = setup
  }

  SetupConfiguration get(String stack, String setup) {
    setups[setup]?."$stack"
  }

}
