/*
 * Copyright 2022 wetransform GmbH
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
package to.wetransform.gradle.swarm.actions.assemble.template;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;

/**
 * Functions that reads a file as String.
 *
 * @author Simon Templer
 */
public class ReadFileFunction implements Function {

  private final File rootDir;

  /**
   * @param rootDir
   *          the project root directory for resolving absolute references
   */
  public ReadFileFunction(File rootDir) {
    this.rootDir = rootDir;
  }

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList("value");
  }

  /*
   * (non-Javadoc)
   *
   * @see io.pebbletemplates.pebble.extension.Function#execute(java.util.Map,
   * io.pebbletemplates.pebble.template.PebbleTemplate, io.pebbletemplates.pebble.template.EvaluationContext, int)
   */
  @Override
  public String execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Object value = args.get("value");
    Path file;

    if (value == null) {
      throw new NullPointerException("File path must be specified");
    } else if (value instanceof Path) {
      file = (Path) value;
    } else if (value instanceof File) {
      file = ((File) value).toPath();
    } else {
      String path = value.toString();
      path = resolvePath((PebbleTemplateImpl) self, rootDir, path);
      file = Paths.get(path);
    }

    try {
      return Files.readAllLines(file, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + file.toString(), e);
    }
  }

  /**
   * Resolve a path in relation to a template and the project.
   *
   * @param template
   *          the template to resolve relative paths to
   * @param rootDir
   *          the project root directory to resolve absolute paths with
   * @param path
   *          the path to resolve
   * @return the resolved path
   */
  public static String resolvePath(PebbleTemplateImpl template, File rootDir, String path) {
    if (path.startsWith("/")) {
      // absolute
      if (rootDir == null) {
        throw new IllegalStateException("Project root directory must be provided to resolve absolute paths");
      } else if (path.length() == 1) {
        return rootDir.getAbsolutePath() + path;
      } else {
        return new File(rootDir, path.substring(1)).getAbsolutePath();
      }
    } else {
      return ((PebbleTemplateImpl) template).resolveRelativePath(path);
    }
  }

}
