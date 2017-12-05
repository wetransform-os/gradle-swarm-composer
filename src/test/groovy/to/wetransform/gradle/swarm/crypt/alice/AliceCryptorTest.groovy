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

package to.wetransform.gradle.swarm.crypt.alice

import org.junit.Test;

/**
 * Simple AliceCryptor test.
 *
 * @author Simon Templer
 */
class AliceCryptorTest {

  @Test
  void testEncryptDecrypt() {
    AliceCryptor c = new AliceCryptor()

    String plain = "Hello world"
    String password = "Goodbye"

    String encrypted = c.encrypt(plain, password)

    String decrypted = c.decrypt(encrypted, password)

    assert plain == decrypted

    assert encrypted != decrypted
  }

}
