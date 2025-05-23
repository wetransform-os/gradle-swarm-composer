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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;

/**
 * Functions that reads a file and encodes as a base64 string.
 *
 * @author Simon Templer
 */
public class ReadFileBase64Function implements Function {

  private final File rootDir;

  /**
   * @param rootDir
   *          the project root directory for resolving absolute references
   */
  public ReadFileBase64Function(File rootDir) {
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
      path = ReadFileFunction.resolvePath((PebbleTemplateImpl) self, rootDir, path);
      file = Paths.get(path);
    }

    try {
      byte[] bytes = Files.readAllBytes(file);
      return Base64.getEncoder().encodeToString(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + file.toString(), e);
    }
  }

}
