/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

import to.wetransform.gradle.swarm.config.ConfigHelper;
import to.wetransform.gradle.swarm.tasks.Assemble;

class SwarmComposerPlugin implements Plugin<Project> {

  private static final Map<String, Object> DEFAULT_SC_CONFIG = [:]

  void apply(Project project) {
    // register extension
    project.extensions.create('composer', SwarmComposerExtension, project)

    project.afterEvaluate { p ->
      addDefaultTasks(p)
    }
  }

  void addDefaultTasks(Project project) {
    File stacksDir = project.composer.stacksDir
    File setupsDir = project.composer.setupsDir

    if (stacksDir?.exists()) {

      // create default tasks for assembling compose files
      stacksDir.eachDir { dir ->
        def name = dir.name

        def stackConfigFiles = []

        // stack base configuration
        def stackConfig = project.fileTree(
          dir: dir,
          includes: ['config/**/*.env', 'config/**/*.yml', 'config/**/*.yaml'])
        stackConfigFiles.addAll(stackConfig.asCollection())

        def stackFile = new File(dir, 'stack.yml')
        if (stackFile.exists()) {
          // build tasks for the stack

          if (setupsDir?.exists()) {
            // build tasks for setups
            setupsDir.eachDir { setupDir ->
              def setup = setupDir.name

              def configFiles = []
              configFiles.addAll(stackConfigFiles)

              // load swarm composer config for setup
              def scConfig = loadSetupConfig(setupDir)

              def extendedSetups = collectExtendedSetups(project, setupsDir, setup, scConfig)
              project.logger.info("Setup $setup extends these setups: $extendedSetups")

              // add configuration for extended setups
              try {
                extendedSetups.each { extended ->
                  configFiles.addAll(collectSetupConfigFiles(project, setupsDir, extended))
                }
              } catch (e) {
                throw new RuntimeException('Error collecting configuration files from extended setups', e)
              }

              // add configuration for this setup
              configFiles.addAll(collectSetupConfigFiles(project, setupsDir, setup))

              project.logger.info("Setup $setup uses these configuration files:\n${configFiles.join('\n')}")

              configureSetup(project, stackFile, name, setup, configFiles, scConfig)
            }
          }
          else {
            // build default task

            def configFiles = []
            configFiles.addAll(stackConfigFiles)

            // load swarm composer config for setup
            def scConfig = loadSetupConfig(project.projectDir)

            // collect setup configuration files
            def defaultConfig = project.fileTree(
              dir: project.projectDir,
              includes: ['*.env', '*-config.yml', '*-config.yaml'],
              excludes: ['swarm-composer.yml'])
            configFiles.addAll(defaultConfig.asCollection())

            configureSetup(project, stackFile, name, 'default', configFiles, scConfig)
          }
        }
      }

    }
  }

  Map loadSetupConfig(File setupDir) {
    def scConfigFile = new File(setupDir, 'swarm-composer.yml')
    scConfigFile.exists() ? (ConfigHelper.loadYaml(scConfigFile)) : DEFAULT_SC_CONFIG
  }

  Collection collectExtendedSetups(Project project, File setupsDir, String setupName, Map scConfig) {
    def extend = scConfig['extends']
    if (extend) {
      Deque<String> toProcess = new LinkedList<>()
      toProcess.addAll(extend)
      Set<String> handled = new HashSet<>()
      def result = []

      while (!toProcess.empty) {
        String candidate = toProcess.pollLast()

        if (!handled.contains(candidate)) {
          // add to result
          result << candidate

          // check candidate for direct dependencies
          def candConfig = loadSetupConfig(new File(setupsDir, candidate))
          def candExtend = candConfig['extends']
          if (candExtend) {
            candExtend.reverse().each {
              toProcess.addFirst(it)
            }
          }
        }
      }

      result.reverse() // reverse to have correct extension order
    }
    else {
      []
    }
  }

  Collection collectSetupConfigFiles(Project project, File setupsDir, String setupName) {
    File setupDir = new File(setupsDir, setupName)

    // collect setup configuration files
    def setupConfig = project.fileTree(
      dir: setupDir,
      includes: ['*.env', '*.yml', '*.yaml'],
      excludes: ['swarm-composer.yml'])

    setupConfig.asCollection()
  }

  void configureSetup(Project project, File stackFile, String stack, String setup,
    List cfgFiles, Map scConfig) {

    def dcConfig = scConfig['docker-compose']
    boolean composeSupported = dcConfig == null ? false : dcConfig

    def desc = "Generates compose file for stack $stack with setup $setup"
    if (scConfig.description) {
      desc = scConfig.description.trim()
    }

    // default target file
    def composeFile = new File(stackFile.parentFile, "${setup}-stack.yml")

    // custom target file
    def targetFile = scConfig['target-file']
    if (targetFile) {
      composeFile = project.file(targetFile)
    }

    // task for assembling compose file
    def taskName = "assemble-${stack}-${setup}"
    def task = project.task(taskName, type: Assemble) {
      template = stackFile
      configFiles = cfgFiles ?: []
      target = composeFile

      group 'Assemble compose file'
      description desc
    }

    boolean createScript = true
    if (createScript) {
      task.doLast {
        // add a script file for convenient Docker Compose calls
        File scriptFile = project.file(composeSupported ? "${stack}-${setup}.sh" : "deploy-${stack}-${setup}.sh")
        def relPath = project.projectDir.toPath().relativize( composeFile.toPath() ).toFile().toString()

        def run
        if (composeSupported) {
          run = "docker-compose -f \"$relPath\" \"\$@\""
        }
        else {
          run = "docker stack deploy --compose-file \"$relPath\" --with-registry-auth $stack"
        }

        scriptFile.text = """#!/bin/bash
set -e
./gradlew ${taskName}
$run"""
        try {
          ['chmod', 'a+x', scriptFile.absolutePath].execute()
        } catch (e) {
          // ignore
        }
      }
    }

  }

}
