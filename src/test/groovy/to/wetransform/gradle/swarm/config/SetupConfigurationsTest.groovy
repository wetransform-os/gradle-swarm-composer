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
package to.wetransform.gradle.swarm.config

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

/**
 * Tests for SetupConfigurations class.
 *
 * @author Simon Templer
 */
class SetupConfigurationsTest {

  private SetupConfigurations example1

  @Before
  void prepare() {
    SetupConfigurations e1 = new SetupConfigurations()

    def stacks = (1..3).collect{ "stack$it" }
    def setups = (1..4).collect{ "setup$it" }

    setups.each { setup ->
      stacks.each { stack ->
        def conf = new SetupConfiguration(configFiles: [], stackName: stack, setupName: setup)
        e1.add(conf)
      }
    }

    example1 = e1
  }

  @Test
  void testList() {
    assertEquals(12, example1.toList().size())
  }

  @Test
  void testIterator() {
    def it = example1.iterator()
    def list = []
    while (it.hasNext()) {
      list << it.next()
    }
    assertEquals(12, list.size())
  }

  @Test
  void testFindStack() {
    def stack = 'stack1'
    def stackConfig = example1.findStack(stack)
    def setups = new HashSet<>()
    stackConfig.each {
      assertEquals(stack,  it.config.stack)
      setups.add(it.config.setup)
    }
    assertEquals(4, setups.size())
  }

  @Test
  void testFindSetup() {
    def setup = 'setup2'
    def setupConfig = example1.findSetup(setup)
    def stacks = new HashSet<>()
    setupConfig.each {
      assertEquals(setup,  it.config.setup)
      stacks.add(it.config.stack)
    }
    assertEquals(3, stacks.size())
  }

  @Test
  void testFindUnkownStack() {
    assertTrue(example1.findStack('setup1').toList().empty)
  }

  @Test
  void testFindUnkownSetup() {
    assertTrue(example1.findSetup('stack2').toList().empty)
  }

  @Test
  void testStackAddConfig() {
    def stack = 'stack1'
    example1.findStack(stack).addConfig([test: true])

    example1.each {
      if (it.config.stack == stack) {
        assertNotNull(it.config.test)
        assertTrue(it.config.test)
      }
    }
  }
}
