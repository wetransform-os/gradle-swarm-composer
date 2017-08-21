/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import to.wetransform.gradle.swarm.actions.assemble.AssembleDefaultConfig;
import to.wetransform.gradle.swarm.actions.assemble.AssembleRunner
import to.wetransform.gradle.swarm.actions.assemble.template.PebbleAssembler;
import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler
import to.wetransform.gradle.swarm.config.SetupConfigurations;
import to.wetransform.gradle.swarm.tasks.Assemble;;

class SwarmComposerExtension {

  /**
   * @param project the project the extension is applied to
   */
  public SwarmComposerExtension(Project project) {
    super();
    this.project = project;
  }

  // public API

  /**
   * Template engine, defaults to Pebble.
   */
  TemplateAssembler templateEngine = new PebbleAssembler()

  /**
   * Folder holding stacks directory structure.
   */
  File stacksDir = project.file('stacks')

  /**
   * Folder holding setups directory structure.
   */
  File setupsDir = project.file('setups')

  // task actions

  /**
   * Run an assemble action directly. The configuration is the same as for the Assemble task.
   */
  final def assemble = { Closure cl ->
    if (cl) {
      AssembleRunner runner = new AssembleRunner(project, new AssembleDefaultConfig())
      ConfigureUtil.configure(cl, runner)
      runner.runCommand()
    }
    else {
      Assemble
    }
  }

  // internal

  final Project project

//  final SetupConfigurations configs = new SetupConfigurations();

}
