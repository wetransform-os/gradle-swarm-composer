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

    // config files
    def configs = configFiles.collect { cfg ->
      Map<String, Object> result = [:]
      File configFile = toFile(cfg)
      if (configFile && configFile.exists()) {
        if (configFile.name.endsWith('.env')) {
          // load environment file
          result.env = loadEnvironment(configFile)
        }
        else if (configFile.name.endsWith('.yml') || configFile.name.endsWith('.yaml')) {
          //TODO support yaml configuration
        }
      }
      result
    }

    Map<String, Object> context = mergeConfigs(configs)

    // stack and setup names
    if (stackName) {
      context.stack = stackName
    }
    if (setupName) {
      context.setup = setupName
    }

    // build template
    targetFile.withOutputStream { out ->
      assembler.compile(templateFile, context) {
        out
      }
    }
  }

  private Map<String, Object> mergeConfigs(Iterable<Map<String, Object>> configs) {
    configs.asCollection().inject([:], this.&combineMap)
  }

  private Map combineMap(Map a, Map b) {
    Map result = [:]
    result.putAll(a)
    b.each { key, value ->
      result.merge(key, value, this.&combineValue)
    }
    result
  }

  private Object combineValue(Object a, Object b) {
    if (a instanceof Map) {
      if (b instanceof Map) {
        combineMap(a, b)
      }
      else {
        //XXX error?
        a
      }
    }
    else if (b instanceof Map) {
      //XXX error?
      b
    }
    else if (a instanceof List && b instanceof List) {
      def combined = []
      combined.addAll(a)
      combined.addAll(b)
      combined
    }
    else if (a instanceof List && !(b instanceof List)) {
      def combined = []
      combined.addAll(a)
      combined.add(b)
      combined
    }
    else if (!(a instanceof List) && b instanceof List) {
      def combined = []
      combined.add(a)
      combined.addAll(b)
      combined
    }
    else {
      // b overrides a
      //XXX message?
      b
    }
  }

  private Map loadEnvironment(File envFile) {
    List lines = envFile.readLines(StandardCharsets.UTF_8.name())
    def pairs = lines.findResults { String line ->
      def matcher = (line =~ /^([^#=]+)=(.*)$/)
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
