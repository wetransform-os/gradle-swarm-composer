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

        def stackFile = new File(dir, 'stack.yml')
        if (stackFile.exists()) {
          // build tasks for the stack

          if (setupsDir?.exists()) {
            // build tasks for setups
            setupsDir.eachDir { setupDir ->
              def setup = setupDir.name
              project.task("assemble-${name}-${setup}", type: Assemble) {
                template = stackFile
                environment = new File(setupDir, '.env')
                target = project.file("${name}-${setup}.yml")

                group 'Assemble compose file'
                description "Generates compose file for $name with setup $setup"
              }
            }
          }
          else {
            // build default task
            def setup = 'default'
            project.task("assemble-${name}-${setup}", type: Assemble) {
              template = stackFile
              environment = project.file('.env')
              target = project.file("${name}-${setup}.yml")

              group 'Assemble compose file'
              description "Generates compose file for $name with setup $setup"
            }
          }
        }
      }

    }
  }

}
