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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore
import org.junit.Test

import to.wetransform.gradle.swarm.config.ConfigEvaluator;

import static org.junit.Assert.*

/**
 * Tests for Pebble configuration evaluator.
 *
 * @author Simon Templer
 */
@Ignore
class PebbleEvaluatorTest extends ConfigEvaluatorTest<PebbleEvaluator> {

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

  @Test
  void testIsDynamicValueNested() {
    assertTrue(eval.isDynamicValue("Hallo {{ object.name }}"))
  }

  @Test
  void testDependenciesValueStatic() {
    def deps = eval.getDependencies("Hallo Welt")
    assert deps.isEmpty()
  }

  @Test
  void testDependenciesFullExpr() {
    def deps = eval.getDependencies("{{ some_var }}")
    assert deps.size() == 1
    assert deps.iterator().next() == ['some_var']
  }

  @Test
  void testDependenciesValuePartial() {
    def deps = eval.getDependencies("Hallo {{ name }}")
    assert deps.size() == 1
    assert deps.iterator().next() == ['name']
  }

  @Test
  void testDependenciesValueNested() {
    def deps = eval.getDependencies("Hallo {{ object.name }}")
    assert deps.size() == 1
    assert deps.iterator().next() == ['object', 'name']
  }

  @Test
  void testDependenciesValueNested2() {
    def deps = eval.getDependencies("Hallo {{ object.property.name }}")
    assert deps.size() == 1
    assert deps.iterator().next() == ['object', 'property', 'name']
  }

  @Test
  void testDependenciesValueMapAccessor() {
    def deps = eval.getDependencies("Hallo {{ object['property'].name }}")
    assert deps.size() == 1
    assert deps.iterator().next() == ['object', 'property', 'name']
  }

  @Ignore
  @Test
  void testDependenciesValueDependent() {
    def deps = eval.getDependencies("Hallo {{ object[variable].name }}")
    assert deps.size() == 2
    assert deps.contains(['variable'])
    assert deps.contains(['object']) //XXX what to expect here? without a value for "variable" we can't get anything meaning full here?
  }

  @Ignore
  @Test
  void testDependenciesValueDependent2() {
    def deps = eval.getDependencies("Hallo {{ object[variable.sub].name }}")
    assert deps.size() == 2
    assert deps.contains(['variable', 'sub'])
    assert deps.contains(['object']) //XXX what to expect here? without a value for "variable" we can't get anything meaning full here?
  }

  @Test
  void testDependenciesValueComplex() {
    def deps = eval.getDependencies("{{ (toInt(config_breaking_version) < 4) and (not service_publisher.map_proxy.use_s3_cache) }}")
    assert deps.size() == 2
    assert deps.contains(['config_breaking_version'])
    assert deps.contains(['service_publisher', 'map_proxy', 'use_s3_cache'])
  }

}
