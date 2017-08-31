/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config

import java.util.Iterator
import java.util.Map;
import java.util.stream.Collectors

import org.gradle.internal.impldep.bsh.This;;;

/**
 * Collection of configurations for setups and stacks.
 *
 * @author Simon Templer
 */
class SetupConfigurations implements Iterable<SetupConfiguration> {

  private final Map<String, Map<String, SetupConfiguration>> setups

  SetupConfigurations(Map<String, Map<String, SetupConfiguration>> setups) {
    this.setups = setups
  }

  SetupConfigurations() {
    this([:])
  }

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

  SetupConfigurations findStack(String stack) {
    if (!stack) {
      new SetupConfigurations()
    }
    else {
      def setupsForStack = setups.collectEntries { setup, stacks ->
        if (stacks.containsKey(stack)) {
          def onlyStack = [(stack): (stacks[stack]?:[:])]
          [(setup): onlyStack]
        }
        else {
          [:]
        }
      }
      new SetupConfigurations(setupsForStack)
    }
  }

  SetupConfigurations findSetup(String setup) {
    if (!setup || !setups.containsKey(setup)) {
      new SetupConfigurations()
    }
    else {
      new SetupConfigurations([(setup): (setups[setup])?:[:]])
    }
  }

  @Override
  public Iterator<SetupConfiguration> iterator() {
    def all = setups.values().collectMany{m -> m.values()}
    all.iterator()
  }

  void addConfig(Map conf) {
    this.toList()*.addConfig(conf)
  }

}
