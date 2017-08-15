/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import org.gradle.api.Project

class SwarmComposerExtension {

  /**
   * @param project the project the extension is applied to
   */
  public SwarmComposerExtension(Project project) {
    super();
    this.project = project;
  }

  // public API


  // task actions


  // internal

  final Project project

}
