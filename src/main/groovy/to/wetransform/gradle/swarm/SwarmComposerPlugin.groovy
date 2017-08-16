/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

import to.wetransform.gradle.swarm.tasks.Assemble;

class SwarmComposerPlugin implements Plugin<Project> {

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

        def configFiles = []
        def configPattern = ['*.env', '*-config.yml', '*-config.yaml']

        // stack base configuration
        def stackConfig = project.fileTree(dir: dir, includes: configPattern)
        configFiles.addAll(stackConfig.asCollection())

        def stackFile = new File(dir, 'stack.yml')
        if (stackFile.exists()) {
          // build tasks for the stack

          if (setupsDir?.exists()) {
            // build tasks for setups
            setupsDir.eachDir { setupDir ->
              def setup = setupDir.name

              def setupConfig = project.fileTree(
                dir: setupDir,
                includes: ['*.env', '*.yml', '*.yaml'])
              configFiles.addAll(setupConfig.asCollection())

              configureSetup(project, stackFile, name, setup, configFiles)
            }
          }
          else {
            // build default task

            def defaultConfig = project.fileTree(dir: project.projectDir, includes: configPattern)
            configFiles.addAll(defaultConfig.asCollection())

            configureSetup(project, stackFile, name, 'default', configFiles)
          }
        }
      }

    }
  }

  void configureSetup(Project project, File stackFile, String stack, String setup,
    List cfgFiles) {

    project.task("assemble-${stack}-${setup}", type: Assemble) {
      template = stackFile
      configFiles = cfgFiles ?: []
      target = project.file("stack-${stack}-${setup}.yml")
      mode = 'swarm'

      group 'Assemble compose file for swarm mode'
      description "Generates compose file for stack $stack with setup $setup"
    }

    if (setup == 'local' || setup == 'default') {
      // offer compose mode

      project.task("compose-${stack}-${setup}", type: Assemble) {
        template = stackFile
        configFiles = cfgFiles ?: []
        target = project.file("${stack}-${setup}.yml")
        mode = 'compose'

        group 'Assemble compose file for Docker Compose'
        description "Generates Docker Compose file for $stack with setup $setup"
      }
    }

  }

}
