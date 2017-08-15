/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class SwarmComposerPlugin implements Plugin<Project> {

  void apply(Project project) {
    // register extension
    project.extensions.create('swarm-composer', SwarmComposerExtension, project)

//    project.afterEvaluate { p ->
//
//    }
  }

}
