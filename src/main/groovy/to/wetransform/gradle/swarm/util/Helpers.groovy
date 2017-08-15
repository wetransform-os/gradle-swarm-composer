/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.util

import java.nio.file.Path;

import org.gradle.api.Project

class Helpers {

  static File toFile(Object fileOrPath) {
    if (fileOrPath == null) {
      null
    }
    else if (fileOrPath instanceof File) {
      fileOrPath
    }
    else if (fileOrPath instanceof Path) {
      ((Path) fileOrPath).toFile()
    }
    else {
      fileOrPath as File
    }
  }

}
