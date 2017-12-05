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

package to.wetransform.gradle.swarm.config.pebble

import org.junit.AfterClass
import org.junit.Before;
import org.junit.BeforeClass
import org.junit.Ignore;
import org.junit.Test

import to.wetransform.gradle.swarm.config.ConfigEvaluator;

import static org.junit.Assert.*

import org.junit.After

/**
 * Tests for Pebble configuration evaluator.
 *
 * @author Simon Templer
 */
abstract class ConfigEvaluatorTest<T extends ConfigEvaluator> {

  protected T eval

  @Before
  void init() {
    eval = createEvaluator()
  }

  protected abstract T createEvaluator()

  @After
  void close() {
    eval = null
  }

  @Test
  void testEvalConfig() {
    def config = [
      name: 'World',
      phrase: '{{ hello }}, nice to meet you!',
      hello: 'Hello {{ name }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'World',
      phrase: 'Hello World, nice to meet you!',
      hello: 'Hello World'
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigNested() {
    def config = [
      name: 'World',
      down: [
          phrase: '{{ down.hello }}, nice to meet you!',
          hello: 'Hello {{ name }}'
        ],
      letter: 'To {{name}}: {{ down.phrase }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'World',
      down: [
          phrase: 'Hello World, nice to meet you!',
          hello: 'Hello World'
        ],
      letter: 'To World: Hello World, nice to meet you!'
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigNestedRelative() {
    def config = [
      name: 'World',
      down: [
          phrase: '{{ hello }}, nice to meet you!',
          hello: 'Hello {{ name }}'
        ],
      letter: 'To {{name}}: {{ down.phrase }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      name: 'World',
      down: [
          phrase: 'Hello World, nice to meet you!',
          hello: 'Hello World'
        ],
      letter: 'To World: Hello World, nice to meet you!'
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigNumber() {
    def config = [
      number: 1,
      value: '{{ number }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      number: 1,
      value: '1' // the type is not retained
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigBoolean() {
    def config = [
      bool: true,
      value: '{{ bool }}',
      value2: '{{ not bool }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      bool: true,
      value: true,
      value2: false
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigIf() {
    def config = [
      bool: true,
      cond: '{{ value ? "one" : "two" }}',
      value: '{{ not bool }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      bool: true,
      cond: 'two',
      value: false
      ]

    assert evaluated == expected
  }

  @Test
  void testEvalConfigIfOrder() {
    def config = [
      bool: true,
      value: '{{ not bool }}',
      cond: '{{ value ? "one" : "two" }}'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      bool: true,
      value: false,
      cond: 'two'
      ]

    assert evaluated == expected
  }

  @Ignore
  @Test
  void testEvalConfigIfString() {
    def config = [
      val: 'xxx',
      cond: '{{ value == "laxxxal" ? "one" : "two" }}',
      value: 'la{{ val }}al'
      ]

    def evaluated = eval.evaluate(config)

    def expected = [
      val: 'xxx',
      cond: 'one',
      value: 'laxxxal'
      ]

    assert evaluated == expected
  }

}
