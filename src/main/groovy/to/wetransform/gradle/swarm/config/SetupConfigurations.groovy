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

package to.wetransform.gradle.swarm.config

import java.util.Iterator
import java.util.Map;
import java.util.stream.Collectors

import org.gradle.internal.impldep.bsh.This

import groovy.lang.Closure

/**
 * Collection of configurations for setups and stacks.
 *
 * @author Simon Templer
 */
class SetupConfigurations implements Iterable<SetupConfiguration> {

  private final Map<String, Map<String, SetupConfiguration>> setups

  // not use SwarmComposerExtension type as Gradle uses a different (decorated) type of object at runtime
  private final def extension

  SetupConfigurations(Map<String, Map<String, SetupConfiguration>> setups) {
    this(setups, null)
  }

  SetupConfigurations(Map<String, Map<String, SetupConfiguration>> setups, def extension) {
    this.setups = setups
    this.extension = extension
  }

  SetupConfigurations(def extension) {
    this([:], extension)
  }

  SetupConfigurations() {
    this([:], null)
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

    if (extension != null) {
      // run custom configuration adaptions
      for (Closure cl : extension.configureClosures) {
        Closure conf = cl.clone()
        conf.delegate = setup
        conf.directive = Closure.DELEGATE_FIRST
        conf.call()
      }
    }
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
