/*
 * Copyright 2021 wetransform GmbH
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

import java.nio.file.Files

import org.junit.Test

import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator

class VersionIsAtLeastFunctionTest {

  private final VersionIsAtLeastFunction function = new VersionIsAtLeastFunction()

  @Test
  void testNumericFail() {
    boolean result = function.execute([
      version: 3.2,
      required: 3.4
    ])

    assert result == false
  }

  @Test
  void testNumericSuccess() {
    boolean result = function.execute([
      version: 3.4,
      required: 3.2
    ])

    assert result == true
  }

  @Test
  void testNumericSame() {
    boolean result = function.execute([
      version: 3.4,
      required: 3.4
    ])

    assert result == true
  }

  @Test
  void testString1() {
    boolean result = function.execute([
      version: '3.4.1',
      required: '3.4'
    ])

    assert result == true
  }

  @Test
  void testString2() {
    boolean result = function.execute([
      version: '3.4.1',
      required: '3.4.2'
    ])

    assert result == false
  }

  @Test
  void testStringPrefix() {
    boolean result = function.execute([
      version: 'v0.24.0',
      required: '0.22.0'
    ])

    assert result == true
  }

  @Test
  void testSingle1() {
    boolean result = function.execute([
      version: '3',
      required: '4'
    ])

    assert result == false
  }

  @Test
  void testSingle2() {
    boolean result = function.execute([
      version: '6',
      required: '4'
    ])

    assert result == true
  }

  @Test
  void testSingleSame() {
    boolean result = function.execute([
      version: '3',
      required: 3
    ])

    assert result == true
  }

  @Test
  void testSnapshot() {
    boolean result = function.execute([
      version: '3.4-SNAPSHOT',
      required: '3.4'
    ])

    assert result == false
  }

  @Test
  void testSnapshot2() {
    boolean result = function.execute([
      version: '3.4',
      required: '3.4-SNAPSHOT'
    ])

    assert result == true
  }

  @Test
  void testCheckVersion() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ checkVersion(version='3.4',required=compareTo) }}
      '''.stripMargin().trim()

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()

      def context = [
        compareTo: '3.2'
      ]

      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['true']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testCheckVersionPositional() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ checkVersion(3.4,compareTo) }}
      '''.stripMargin().trim()

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()

      def context = [
        compareTo: '3.2'
      ]

      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['true']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testCheckVersionFail() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ checkVersion(3.4,compareTo) }}
      '''.stripMargin().trim()

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()

      def context = [
        compareTo: 3.6
      ]

      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['false']
    } finally {
      tmpFile.delete()
    }
  }
}
