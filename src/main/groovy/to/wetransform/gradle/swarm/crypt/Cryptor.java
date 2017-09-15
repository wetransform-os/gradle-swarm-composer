/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.crypt;

/**
 * Encryption and decryption interface.
 *
 * @author Simon Templer
 */
public interface Cryptor {

  String encrypt(String plain, String password) throws Exception;

  String decrypt(String encrypted, String password) throws Exception;

}
