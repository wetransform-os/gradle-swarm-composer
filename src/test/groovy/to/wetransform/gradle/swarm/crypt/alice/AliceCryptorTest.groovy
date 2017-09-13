/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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
  }

}
