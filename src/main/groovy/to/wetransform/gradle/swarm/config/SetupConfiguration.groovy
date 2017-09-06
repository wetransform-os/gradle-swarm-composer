/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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

}
