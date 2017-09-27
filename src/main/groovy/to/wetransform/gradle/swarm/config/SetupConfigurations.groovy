/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config

import java.util.Iterator
import java.util.Map;
import java.util.stream.Collectors

import org.gradle.internal.impldep.bsh.This

import groovy.lang.Closure;;;;

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

  def getSetupNames() {
    setups.keySet().asImmutable()
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

  /**
   * Add configuration for the setups.
   *
   * @param conf the configuration to add
   */
  void addConfig(Map conf) {
    this.toList()*.addConfig(conf)
  }

  /**
   * Add a post-processor that can modify the YAML structure.
   *
   * @param processor the post-processor, a closure that takes the YAML
   *   map/list structure as argument and returns a boolean the states
   *   if any changes were made
   */
  void processYaml(Closure processor) {
    this.toList()*.processYaml(processor)
  }

}
