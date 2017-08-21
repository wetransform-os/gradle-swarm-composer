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

  File stackFile

  String stackName

  String setupName

  List configFiles

  private Map config

  Map getConfig() {
    if (config == null) {
      assert stackName
      assert setupName
      config = ConfigHelper.loadConfig(configFiles, stackName, setupName)
    }
    config
  }

  void addConfig(Map conf) {
    config = ConfigHelper.mergeConfigs([config, conf])
  }

  Map settings

  List builds

}
