/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble;

/**
 * Configuration interface for assemble action.
 *
 * @author Simon Templer
 */
public interface AssembleConfig {

  /**
   * Get the configured template file.
   *
   * @return the template to assemble
   */
  Object getTemplate();

  /**
   * Set the template file to assemble.
   *
   * @param template the template file, should be a File or String with the file path
   */
  void setTemplate(Object template);

  /**
   * Get the configured environment file.
   *
   * @return the environment file
   */
  Object getEnvironment();

  /**
   * Set the file defining the environment.
   *
   * @param environment the environment file, should be a File or String with the file path
   */
  void setEnvironment(Object environment);

  /**
   * Get the target file.
   *
   * @return the target file
   */
  Object getTarget();

  /**
   * Set the target file to write the assembled content to.
   *
   * @param target the target file, should be a File or String with the file path
   */
  void setTarget(Object target);

}
