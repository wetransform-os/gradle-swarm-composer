/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.tasks

import groovy.lang.Delegate

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import to.wetransform.gradle.swarm.actions.assemble.AssembleConfig
import to.wetransform.gradle.swarm.actions.assemble.AssembleDefaultConfig
import to.wetransform.gradle.swarm.actions.assemble.AssembleRunner

/**
 * Task that runs an assemble action.
 *
 * @author Simon Templer
 */
class Assemble extends DefaultTask implements AssembleConfig {

  @Delegate
  private final AssembleConfig _config = new AssembleDefaultConfig()

  @TaskAction
  void runCommand() {
    new AssembleRunner(project, _config).runCommand()
  }

}
