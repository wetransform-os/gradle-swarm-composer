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

}
