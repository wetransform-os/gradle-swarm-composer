/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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

    def encrypted = c.encrypt(copy, password)
    def copyEncrypted = new JsonSlurper().parseText(JsonOutput.toJson(encrypted))

    assert encrypted == copyEncrypted

    println(encrypted.inspect())

    def decrypted = c.decrypt(copyEncrypted, password)

    assert plain == decrypted

    assert encrypted != decrypted
  }

}
