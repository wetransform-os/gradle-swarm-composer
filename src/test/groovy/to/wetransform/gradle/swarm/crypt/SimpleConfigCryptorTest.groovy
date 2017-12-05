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

import org.junit.Test

import groovy.json.JsonOutput
import groovy.json.JsonSlurper;
import to.wetransform.gradle.swarm.crypt.alice.AliceCryptor;;

/**
 * Tests for SimpleConfigCryptor
 *
 * @author Simon Templer
 */
class SimpleConfigCryptorTest {

  @Test
  void testEncryptDecrypt() {
    AliceCryptor alice = new AliceCryptor()
    SimpleConfigCryptor c = new SimpleConfigCryptor(alice)

    def plain = [
      greeting: "Hello world",
      more: [
        title: 'Title',
        content: '''Lorem

ipsum - or what?'''
      ]
    ]
    def copy = new JsonSlurper().parseText(JsonOutput.toJson(plain))

    assert plain == copy

    String password = "Goodbye"

    def encrypted = c.encrypt(copy, password, null)
    def copyEncrypted = new JsonSlurper().parseText(JsonOutput.toJson(encrypted))

    assert encrypted == copyEncrypted

    println(encrypted.inspect())

    def decrypted = c.decrypt(copyEncrypted, password)

    assert plain == decrypted

    assert encrypted != decrypted
  }

}
