/*
 * Copyright 2018 wetransform GmbH
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
package to.wetransform.gradle.swarm.actions.assemble.template

import static org.junit.Assert.*

import org.junit.Test

/**
 * Tests for password generation function.
 *
 * @author Simon Templer
 */
class GeneratePasswordFunctionTest {

  private final GeneratePasswordFunction function = new GeneratePasswordFunction()

  @Test
  void testDefaults() {
    String password = function.execute([:])

    assertEquals(16, password.length())

    // default config (alphanumeric)
    assertTrue(password ==~ /[a-zA-Z0-9]+/)

    println "Example password: $password"
  }

  @Test
  void testLength1() {
    int length = 3
    String password = function.execute([length: length])

    assertEquals(length, password.length())
  }

  @Test
  void testLength2() {
    int length = 20
    String password = function.execute([length: length])

    assertEquals(length, password.length())
  }

  @Test
  void testCharacters() {
    def length = 24
    def chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
    String password = function.execute([length: length, characters: chars])

    assertEquals(length, password.length())
    assertTrue(password ==~ /[a-zA-Z]+/)

    println "Example password: $password"
  }

  @Test
  void testAlpha() {
    def length = 10
    String password = function.execute([length: length, alphabetic: true])

    assertEquals(length, password.length())
    assertTrue(password ==~ /[a-zA-Z]+/)

    println "Example password: $password"
  }

  @Test
  void testNumeric() {
    def length = 10
    String password = function.execute([length: length, numeric: true])

    assertEquals(length, password.length())
    assertTrue(password ==~ /[0-9]+/)

    println "Example password: $password"
  }
}
