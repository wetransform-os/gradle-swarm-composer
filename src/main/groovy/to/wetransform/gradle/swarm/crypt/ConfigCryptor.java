/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.crypt;

import java.util.Map;

/**
 * Interface for configuration encryption/decryption.
 *
 * @author Simon Templer
 */
public interface ConfigCryptor {

  /**
   * Encrypt the (string) settings in the given configuration and return the
   * encrypted version of the configuration.
   * The encryptor may mutate the given configuration and return it or create a new configuration.
   *
   * @param config the configuration
   * @param password the password
   * @return the encrypted configuration
   */
  Map<String, Object> encrypt(Map<String, Object> config, String password) throws Exception;

  /**
   * Decrypt the (string) settings in the given configuration and return the
   * decrypted version of the configuration.
   * The decryptor may mutate the given configuration and return it or create a new configuration.
   *
   * @param config the configuration
   * @param password the password
   * @return the decrypted configuration
   */
  Map<String, Object> decrypt(Map<String, Object> config, String password) throws Exception;

}
