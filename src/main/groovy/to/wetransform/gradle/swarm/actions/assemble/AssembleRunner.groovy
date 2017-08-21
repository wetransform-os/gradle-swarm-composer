/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble

import org.gradle.api.Project

import static to.wetransform.gradle.swarm.util.Helpers.*
import static to.wetransform.gradle.swarm.config.ConfigHelper.*

import java.nio.charset.StandardCharsets

import groovy.json.JsonOutput;
import groovy.lang.Delegate
import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler
import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.pebble.PebbleEvaluator;;

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
    Map<String, Object> context = loadConfig(config, stackName, setupName)

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
