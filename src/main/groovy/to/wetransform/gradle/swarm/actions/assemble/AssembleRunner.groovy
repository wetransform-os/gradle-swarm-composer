/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble

import org.gradle.api.Project

import static to.wetransform.gradle.swarm.util.Helpers.*

import java.nio.charset.StandardCharsets

import groovy.lang.Delegate
import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler

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

    // build template context
    Map<String, Object> context = [:]

    // environment
    File envFile = toFile(environment)
    if (envFile && envFile.exists()) {
      context.env = loadEnvironment(envFile)
    }
    else {
      context.env = [:]
    }

    // build template
    targetFile.withOutputStream { out ->
      assembler.compile(templateFile, context) {
        out
      }
    }
  }

  Map loadEnvironment(File envFile) {
    List lines = envFile.readLines(StandardCharsets.UTF_8.name())
    def pairs = lines.findResults { String line ->
      def matcher = (line =~ /^([^#=+])=(.*)$/)
      if (matcher.size() >= 1) {
        def name = matcher[0][1].trim()
        def value = matcher[0][2]
        [name, value]
      }
      else {
        null
      }
    }
    pairs.collectEntries()
  }

}
