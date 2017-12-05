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

package to.wetransform.gradle.swarm.crypt;

import java.util.Map;

import javax.annotation.Nullable;

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
   * @param reference an already encrypted reference configuration (to reuse existing encryptions)
   * @return the encrypted configuration
   */
  Map<String, Object> encrypt(Map<String, Object> config, String password, @Nullable Map<String, Object> reference) throws Exception;

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
