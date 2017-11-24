/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
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

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * Assembles a template using the Pebble template engine.
 *
 * @author Simon Templer
 */
public class PebbleAssembler implements TemplateAssembler {

  private final PebbleEngine engine = new PebbleEngine.Builder()
      .newLineTrimming(false)
      .autoEscaping(false)
      .strictVariables(true)
      .addEscapingStrategy("doublequotes", new DoubleQuotesEscaper())
      .extension(new SwarmComposerExtension())
      .build();

  @Override
  public void compile(File template, Map<String, Object> context, Supplier<OutputStream> target) throws PebbleException, IOException {
    PebbleTemplate compiledTemplate = engine.getTemplate(template.getAbsolutePath());

    try (OutputStream out = target.get();
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      compiledTemplate.evaluate(writer, ContextWrapper.create(context));
    }
  }

}
