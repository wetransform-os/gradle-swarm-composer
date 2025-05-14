/*
 * Copyright 2017 wetransform GmbH
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
package to.wetransform.gradle.swarm.actions.assemble;

import java.util.List;

/**
 * Configuration interface for assemble action.
 *
 * @author Simon Templer
 */
public interface AssembleConfig {

  /**
   * @return the name of the stack
   */
  String getStackName();

  void setStackName(String name);

  /**
   * @return the name of the setup
   */
  String getSetupName();

  void setSetupName(String name);

  /**
   * Get the configured template file.
   *
   * @return the template to assemble
   */
  Object getTemplate();

  /**
   * Set the template file to assemble.
   *
   * @param template
   *          the template file, should be a File or String with the file path
   */
  void setTemplate(Object template);

  /**
   * Get the configured configuration files.
   *
   * @return the configuration files
   */
  List<Object> getConfig();

  /**
   * Set the file list of configuration files or configuration maps.
   *
   * @param config
   *          the list of configurations, each should be a File or
   *          String with the file path, or an already loaded configuration map
   */
  void setConfig(List<Object> config);

  /**
   * Get the target file.
   *
   * @return the target file
   */
  Object getTarget();

  /**
   * Set the target file to write the assembled content to.
   *
   * @param target
   *          the target file, should be a File or String with the file path
   */
  void setTarget(Object target);

}
