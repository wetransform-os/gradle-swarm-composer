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
package to.wetransform.gradle.swarm.crypt

import java.util.List
import java.util.Map
import java.util.Set
import java.util.function.Function

/**
 * Applies encryption/decryption to configuration.
 *
 * @author Simon Templer
 */
class SimpleConfigCryptor implements ConfigCryptor {

  private final Cryptor cryptor


  SimpleConfigCryptor(Cryptor cryptor) {
    super()
    this.cryptor = cryptor
  }

  @Override
  Map<String, Object> encrypt(Map<String, Object> config, String password, Map<String, Object> reference) throws Exception {
    apply(config) { value, path ->
      def reuse

      if (reference) {
        // look up path and check if value should be reused
        def refValue = reference
        for (int i = 0; i < path.size() && refValue; i++) {
          refValue = refValue?."${path[i]}"
        }
        if (refValue) {
          refValue = refValue.toString()
          try {
            def decrypted = cryptor.decrypt(refValue, password)
            if (decrypted == value) {
              reuse = refValue
            }
          } catch (Exception e) {
            // ignore
          }
        }
      }

      reuse ?: cryptor.encrypt(value, password)
    }
    config
  }

  @Override
  Map<String, Object> decrypt(Map<String, Object> config, String password) throws Exception {
    apply(config) { value ->
      cryptor.decrypt(value, password)
    }
    config
  }

  private void apply(Map config, Closure crypt, List path = []) {
    def keys = new LinkedHashSet(config.keySet())

    keys.each { key ->
      def value = config[key]

      if (value != null) {
        def childPath = []
        childPath.addAll(path)
        childPath.add(key)

        if (value instanceof List) {
          //FIXME lists are not supported ATM
        }
        else if (value instanceof String || value instanceof GString) {
          // evaluate value

          def newValue
          if (crypt.maximumNumberOfParameters > 1) {
            newValue = crypt(value.toString(), childPath)
          }
          else {
            newValue = crypt(value.toString())
          }

          config.put(key, newValue)
        }
        // proceed to child maps
        else if (value instanceof Map) {
          apply(value, crypt, childPath)
        }
      }
    }
  }
}
