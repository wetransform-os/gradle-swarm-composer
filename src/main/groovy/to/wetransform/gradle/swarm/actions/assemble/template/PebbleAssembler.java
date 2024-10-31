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

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Assembles a template using the Pebble template engine.
 *
 * @author Simon Templer
 */
public class PebbleAssembler implements TemplateAssembler {

  private final PebbleEngine engine;

  public PebbleAssembler() {
    this(null);
  }

  public PebbleAssembler(File rootDir) {
    engine = new PebbleEngine.Builder()
        .newLineTrimming(false)
        .autoEscaping(false)
        .strictVariables(true)
        .addEscapingStrategy("doublequotes", new DoubleQuotesEscaper())
        .addEscapingStrategy("compose", new ComposeFileEscaper())
        .addEscapingStrategy("hcl", new HclEscaper())
        .extension(new SwarmComposerExtension(false, rootDir))
        .build();
  }

  @Override
  public void compile(File template, Map<String, Object> context, Supplier<OutputStream> target) throws PebbleException, IOException {
    PebbleTemplate compiledTemplate = engine.getTemplate(template.getAbsolutePath());

    try (OutputStream out = target.get();
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      compiledTemplate.evaluate(writer, ContextWrapper.create(context));
    }
  }

}
