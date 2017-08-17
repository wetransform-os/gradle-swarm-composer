/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config.pebble

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test

import to.wetransform.gradle.swarm.config.ConfigEvaluator;

import static org.junit.Assert.*

/**
 * Tests for Pebble configuration evaluator.
 *
 * @author Simon Templer
 */
class PebbleEvaluatorTest extends ConfigEvaluatorTest<PebbleEvaluator> {

  @Override
  protected PebbleEvaluator createEvaluator() {
    new PebbleEvaluator()
  }

  @Test
  void testIsDynamicValueStatic() {
    assertFalse(eval.isDynamicValue("Hallo Welt"))
  }

  @Test
  void testIsDynamicValueFullExpr() {
    assertTrue(eval.isDynamicValue("{{ somevar }}"))
  }

  @Test
  void testIsDynamicValuePartial() {
    assertTrue(eval.isDynamicValue("Hallo {{ name }}"))
  }

}
