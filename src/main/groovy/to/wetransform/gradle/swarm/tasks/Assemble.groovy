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
