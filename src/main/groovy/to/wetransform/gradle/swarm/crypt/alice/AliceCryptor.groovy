/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.crypt.alice;

import java.nio.charset.StandardCharsets;

import com.rockaport.alice.Alice;
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContext.KeyLength;
import com.rockaport.alice.AliceContextBuilder;

import to.wetransform.gradle.swarm.crypt.Cryptor;

/**
 * Cryptor based on Alice encryption library.
 *
 * @author Simon Templer
 */
public class AliceCryptor implements Cryptor {

  private final Alice alice

  public AliceCryptor() {
    super()

    //TODO based on configuration?

    AliceContext aliceContext = new AliceContextBuilder()
        .setAlgorithm(AliceContext.Algorithm.AES)
        .setKeyLength(KeyLength.BITS_128)
        .setMode(AliceContext.Mode.GCM)
        .setIvLength(12)
        .setGcmTagLength(AliceContext.GcmTagLength.BITS_128)
        .build()
    alice = new Alice(aliceContext)
  }

  @Override
  public String encrypt(String plain, String password) {
    byte[] data = alice.encrypt(plain.getBytes(StandardCharsets.UTF_8), password.toCharArray())
    data.encodeBase64().toString()
  }

  @Override
  public String decrypt(String encrypted, String password) {
    byte[] decoded = encrypted.decodeBase64()
    byte[] decrypted = alice.decrypt(decoded, password.toCharArray())
    new String(decrypted, StandardCharsets.UTF_8)
  }

}
