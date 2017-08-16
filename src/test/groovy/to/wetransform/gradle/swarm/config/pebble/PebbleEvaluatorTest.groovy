/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config.pebble

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*

/**
 * Tests for Pebble configuration evaluator.
 *
 * @author Simon Templer
 */
class PebbleEvaluatorTest {

  private static PebbleEvaluator pebble

  @BeforeClass
  static void init() {
    pebble = new PebbleEvaluator()
  }

  @AfterClass
  static void close() {
    pebble = null
  }

  @Test
  void testIsDynamicValueStatic() {
    assertFalse(pebble.isDynamicValue("Hallo Welt"))
  }

  @Test
  void testIsDynamicValueFullExpr() {
    assertTrue(pebble.isDynamicValue("{{ somevar }}"))
  }

  @Test
  void testIsDynamicValuePartial() {
    assertTrue(pebble.isDynamicValue("Hallo {{ name }}"))
  }

  @Test
  void testEvalConfig() {
    def config = [
      name: 'World',
      phrase: '{{ hello }}, nice to meet you!',
      hello: 'Hello {{ name }}'
      ]

    def evaluated = pebble.evaluate(config)

    def expected = [
      name: 'World',
      phrase: 'Hello World, nice to meet you!',
      hello: 'Hello World'
      ]

    assert config == expected
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

    def evaluated = pebble.evaluate(config)

    def expected = [
      name: 'World',
      down: [
          phrase: 'Hello World, nice to meet you!',
          hello: 'Hello World'
        ],
      letter: 'To World: Hello World, nice to meet you!'
      ]

    assert config == expected
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

    def evaluated = pebble.evaluate(config)

    def expected = [
      name: 'World',
      down: [
          phrase: 'Hello World, nice to meet you!',
          hello: 'Hello World'
        ],
      letter: 'To World: Hello World, nice to meet you!'
      ]

    assert config == expected
  }

  @Test
  void testEvalConfigNumber() {
    def config = [
      number: 1,
      value: '{{ number }}'
      ]

    def evaluated = pebble.evaluate(config)

    def expected = [
      number: 1,
      value: '1' // the type is not retained
      ]

    assert config == expected
  }

}
