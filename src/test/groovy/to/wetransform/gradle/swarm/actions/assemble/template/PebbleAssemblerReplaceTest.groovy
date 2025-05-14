/*
 * Copyright 2025 wetransform GmbH
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

class PebbleAssemblerReplaceTest {

  @Test
  void testEscapeHereDoc() {
    String filter = 'escape(strategy="hereDoc")'

    filterTest("\\\\", "\\\\\\\\", filter)
    filterTest("\$", "\\\$", filter)
    filterTest("\\", "\\\\", filter)
    filterTest("\"Wert \\\"information\\\" als OnlineFunctionCode für WFS verwenden (GDI-DE-Konvention)\"", "\"Wert \\\\\"information\\\\\" als OnlineFunctionCode für WFS verwenden (GDI-DE-Konvention)\"", filter)
  }

  @Test
  void testReplaceHereDoc() {
    String filter = 'replace({\'\\\\\': \'\\\\\\\\\'}) | replace({\'$\': \'\\$\'})'

    filterTest("\\\\", "\\\\\\\\", filter)
    filterTest("\$", "\\\$", filter)

    // replacing single backslash does not work with replace function
    // see https://github.com/PebbleTemplates/pebble/issues/670

    // filterTest("\\", "\\\\", filter)
    // filterTest("\"Wert \\\"information\\\" als OnlineFunctionCode für WFS verwenden (GDI-DE-Konvention)\"", "\"Wert \\\\\"information\\\\\" als OnlineFunctionCode für WFS verwenden (GDI-DE-Konvention)\"", filter)
  }

  void filterTest(String text, String expected, String filter) {
    def assembler = new PebbleAssembler()

    def tmpFile = Files.createTempFile('template', '.tmp').toFile()
    try {
      tmpFile.text = """
      |{{ text | $filter }}
      """.stripMargin().trim()

      def context = [
        text: text
      ]

      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)

      def out = new ByteArrayOutputStream()

      assembler.compile(tmpFile, context) { out }

      def result = out.toString()

      assert result == expected
    } finally {
      tmpFile.delete()
    }
  }
}
