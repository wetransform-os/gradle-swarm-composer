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

import org.junit.Test

import io.pebbletemplates.pebble.error.AttributeNotFoundException
import io.pebbletemplates.pebble.error.PebbleException

import to.wetransform.gradle.swarm.config.ConfigEvaluator
import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator
import java.nio.file.Files
import java.io.ByteArrayOutputStream

/**
 * Tests evaluating templates with PebbleAssembler.
 *
 * @author Simon Templer
 */
class PebbleAssemblerTest {

  @Test
  void testIterateNullObjects() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{% for property in beninfos.results.properties %}
      |{{ property.key }}
      |{% endfor %}
      '''.stripMargin().trim()

      def context = [beninfos: [
        results: [
          properties: [
            BQI: 'test',
            MAMBI: null
          ]
        ]
      ]]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['BQI', 'MAMBI']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testIterateFilter() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{% for item in list | filter('it.enabled | default(false)') %}
      |{{ item.name }}
      |{% endfor %}
      '''.stripMargin().trim()

      def context = [
        list: [
          [name: 'Test1', enabled: true],
          [name: 'Test2', enabled: false],
          [name: 'Test3'],
          [name: 'Test4', enabled: true]
        ]
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test1', 'Test4']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testIterateFilterUsingContext() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{% for item in list | filter('tests[it.name].enabled | default(false)') %}
      |{{ item.name }}
      |{% endfor %}
      '''.stripMargin().trim()

      def context = [
        list: [
          [name: 'Test1'],
          [name: 'Test2'],
          [name: 'Test3'],
          [name: 'Test4']
        ],
        tests: [
          Test1: [enabled: true],
          Test2: [:],
          Test3: [enabled: true],
          Test4: [enabled: false]
        ]

      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test1', 'Test3']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testIterateAnyMatchUsingContext() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{% if list | anyMatch('tests[it.name].enabled | default(false)') %}
      |Yes
      |{% else %}
      |No
      |{% endif %}
      '''.stripMargin().trim()

      def context = [
        list: [
          [name: 'Test1'],
          [name: 'Test2'],
          [name: 'Test3'],
          [name: 'Test4']
        ],
        tests: [
          Test1: [enabled: true],
          Test2: [:],
          Test3: [enabled: true],
          Test4: [enabled: false]
        ]

      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Yes']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testFindFirst() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ (items | findFirst('it.expose | default(false)')).name }}
      '''.stripMargin().trim()

      def context = [
        items: [
          [name: 'Test1'],
          [name: 'Test2', expose: false],
          [name: 'Test3', expose: true],
          [name: 'Test4', expose: true]
        ]
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test3']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testFindFirstMap() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ (items | findFirst('it.value.expose | default(false)')).key }}
      '''.stripMargin().trim()

      def context = [
        items: [
          Test1: [name: 'Test1'],
          Test2: [name: 'Test2', expose: false],
          Test3: [name: 'Test3', expose: true]
        ]
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test3']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testApplyGroovyScript() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ items | apply('it[1].name') }}
      '''.stripMargin().trim()

      def context = [
        items: [
          [name: 'Test1'],
          [name: 'Test2'],
          [name: 'Test3']
        ]
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test2']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testMapGroovyScript() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{% for item in items | map('it.name') %}
      |{{ item }}
      |{% endfor %}
      '''.stripMargin().trim()

      def context = [
        items: [
          [name: 'Test1'],
          [name: 'Test2'],
          [name: 'Test3']
        ]
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['Test1', 'Test2', 'Test3']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testApplyGroovyScriptBinding() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ what | apply(code='"${foo*bar} is $it"',with={'foo': 7, 'bar': 6}) }}
      '''.stripMargin().trim()

      def context = [
        foo: 42,
        what: 'the answer to everything'
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['42 is the answer to everything']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testRunGroovyScriptBinding() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ run(code='"${foo*bar} is $what"',with={'foo': 7, 'bar': 6}) }}
      '''.stripMargin().trim()

      def context = [
        foo: 42,
        what: 'the answer to everything'
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll().toSorted()

      assert list == ['42 is the answer to everything']
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testNoneMatchMapDefault() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ (items | noneMatch('it.value.expose | default(false)')) }}
      '''.stripMargin().trim()

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()

      // explicitly wrap one item in PebbleCachingConfig (because the error in production mentioned the class for the item explicitly)
      def test1 = [name: 'Test1']
      test1 = evaluator.evaluate(test1)

      def context = [
        items: [
          Test1: test1,
          Test2: [name: 'Test2', expose: false],
          Test3: [name: 'Test3']
        ]
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
  void testOrError() {
    def assembler =  new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = '''
      |{{ service.path | orError('No path for service ' + serviceId) }}
      '''.stripMargin().trim()

      def context = [
        service: [
          name: 'Test'
        ],
        serviceId: 'test'
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      try {
        assembler.compile(tmpFile, context) { out }
      } catch (e) {
        assert e instanceof PebbleException
        assert !(e instanceof AttributeNotFoundException)
        assert e.message.startsWith('No path for service test')
      }
    } finally {
      tmpFile.delete()
    }
  }

  @Test
  void testReadTemplateBinding() {
    def assembler =  new PebbleAssembler()

    def tmpFolder = Files.createTempDirectory('templates').toFile()
    def tmpMain = new File(tmpFolder, 'main.tmp')
    def tmpInclude = new File(tmpFolder, 'include.tmp')
    try {
      tmpMain.text = '''
      |Includes:
      |{{ template(path='./include.tmp',with={'foo': 7, 'bar': 6, 'what': what}) }}
      '''.stripMargin().trim()

      def context = [
        foo: 12,
        what: 'the answer to everything'
      ]

      tmpInclude.text = '''
      |{{ foo * bar }} is {{ what }}!
      '''.stripMargin().trim()

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpMain, context) { out }

      def result = out.toString()

      def list = result.split(/\n/).collect{ it.trim() }.findAll()

      assert list == ['Includes:', '42 is the answer to everything!']
    } finally {
      tmpFolder.deleteDir()
    }
  }

}
