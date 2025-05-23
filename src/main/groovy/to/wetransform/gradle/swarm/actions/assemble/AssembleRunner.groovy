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
package to.wetransform.gradle.swarm.actions.assemble

import static to.wetransform.gradle.swarm.config.ConfigHelper.*
import static to.wetransform.gradle.swarm.util.Helpers.*

import groovy.json.JsonOutput
import groovy.lang.Delegate

import java.nio.charset.StandardCharsets

import org.gradle.api.Project

import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler
import to.wetransform.gradle.swarm.config.ConfigEvaluator

/**
 * Assembles a configuration template.
 *
 * @author Simon Templer
 */
class AssembleRunner implements AssembleConfig {

  private final Project project

  @Delegate
  private final AssembleConfig _config

  AssembleRunner(Project project, AssembleConfig config) {
    this.project = project
    this._config = config
  }

  void runCommand() {
    // template is required
    assert template

    // template engine
    TemplateAssembler assembler = project.composer.templateEngine
    assert assembler

    // template must exist
    File templateFile = toFile(template)
    assert templateFile
    assert templateFile.exists()

    // target file must be valid
    File targetFile = toFile(target)
    assert targetFile
    assert !targetFile.isDirectory()

    // load configuration
    Map<String, Object> context = loadConfig(project.rootDir, config, stackName, setupName)

    if (project.logger.infoEnabled) {
      project.logger.info('Context for assembling:\n' +
        JsonOutput.prettyPrint(JsonOutput.toJson(context)))
    }

    // build template
    targetFile.withOutputStream { out ->
      assembler.compile(templateFile, context) {
        out
      }
    }
  }
}
