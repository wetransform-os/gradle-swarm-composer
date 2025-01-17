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
   * If Docker image builds are enabled. Applies in addition the Gradle Docker plugin
   * (https://github.com/bmuschko/gradle-docker-plugin).
   */
  boolean enableBuilds = true

  /**
   * If tasks for exporting configurations are enabled.
   * Mainly intended for debugging.
   * Create files in folder of respective stack that likely contains secrets.
   */
  boolean enableConfigExport = false

  /**
   * Enables checking if Docker is connected to the right swarm for
   * a specific setup by checking the node label <code>sc-setup</code>.
   *
   * Currently only applies for the generated scripts for Swarm setups
   * and Docker Compose.
   */
  boolean swarmSetupChecks = true

  /**
   * If swarm scripts should be generated per setup.
   * If false, a single script is generated for all setups per stack.
   */
  boolean swarmScriptsPerSetup = true

  /**
   * Docker configuration applied to the Gradle Docker plugin.
   */
  void docker(Closure cl) {
    dockerConfig = cl.clone()
  }

  /**
   * Adapt a setup configuration after it was initialized.
   * Called with the respective SetupConfiguration object as delegate.
   */
  void configureSetup(Closure cl) {
    configureClosures << cl.clone()
  }

  /**
   * Template engine, defaults to Pebble.
   */
  TemplateAssembler templateEngine = new PebbleAssembler(project.projectDir)

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

  // advanced users

  final SetupConfigurations configs = new SetupConfigurations(this)

  // internal

  final Project project

  Closure dockerConfig

  final List<Closure> configureClosures = []

}
