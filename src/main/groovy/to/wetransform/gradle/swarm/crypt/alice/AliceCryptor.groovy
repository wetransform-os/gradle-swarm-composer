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
        // on Oracle Java may require to install
        // http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
        .setKeyLength(KeyLength.BITS_256)
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
