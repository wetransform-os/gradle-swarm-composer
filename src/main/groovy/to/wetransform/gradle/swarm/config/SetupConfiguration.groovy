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

/**
 * Configuration object for a specific setup and stack.
 *
 * @author Simon Templer
 */
class SetupConfiguration {

  private boolean configInitialized = false

  private boolean unevaluatedInitialized = false

  File stackFile

  File setupDir

  String stackName

  String setupName

  List configFiles

  private Map config

  private Map unevaluated

  /**
   * Get the setup configuration. This configuration only should be retrieved by the internal API,
   * and there only from running tasks, as other (user-defined) tasks that run before may add
   * configuration.
   *
   * @return the configuration map
   */
  Map getConfig() {
    if (!configInitialized) {
      assert stackName
      assert setupName
      config = ConfigHelper.loadConfig(configFiles, stackName, setupName, config)
      configInitialized = true
    }
    config.asImmutable()
  }

  /**
   * Add configuration for the setup.
   *
   * @param conf the configuration to add
   */
  void addConfig(Map conf) {
    config = ConfigHelper.mergeConfigs([config ?: [:], conf])
    unevaluated = ConfigHelper.mergeConfigs([unevaluated ?: [:], conf])
  }

  /**
   * Get the unevaluated setup configuration. This configuration may be accessed in the configuration phase.
   * It does only contain configuration from stack and setup configuration files and is not evaluated.
   * It is intended for internal use only.
   *
   * @return the unevaluated configuration
   */
  Map getUnevaluated() {
    if (!unevaluatedInitialized) {
      assert stackName
      assert setupName
      unevaluated = ConfigHelper.loadConfig(configFiles, stackName, setupName, unevaluated, false)
      unevaluatedInitialized = true
    }
    unevaluated.asImmutable()
  }

  Map settings

  List builds

  /**
   * Add a post-processor that can modify the YAML structure.
   *
   * @param processor the post-processor, a closure that takes the YAML
   *   map/list structure as argument and returns a boolean the states
   *   if any changes were made
   */
  def processYaml(Closure processor) {
    yamlPostProcessors << processor
  }

  final List yamlPostProcessors = []

}
