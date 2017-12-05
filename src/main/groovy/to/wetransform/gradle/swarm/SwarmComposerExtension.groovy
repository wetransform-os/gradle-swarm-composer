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
   * Enables checking if Docker is connected to the right swarm for
   * a specific setup by checking the node label <code>sc-setup</code>.
   *
   * Currently only applies for the generated scripts for Swarm setups
   * and Docker Compose.
   */
  boolean swarmSetupChecks = true

  /**
   * Docker configuration applied to the Gradle Docker plugin.
   */
  void docker(Closure cl) {
    dockerConfig = cl.clone()
  }

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

  // advanced users

  final SetupConfigurations configs = new SetupConfigurations()

  // internal

  final Project project

  Closure dockerConfig

}
