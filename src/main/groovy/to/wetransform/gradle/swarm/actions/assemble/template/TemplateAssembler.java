/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Interface for assembling a template.
 *
 * @author Simon Templer
 */
@FunctionalInterface
public interface TemplateAssembler {

  void compile(File template, Map<String, Object> context, Supplier<OutputStream> target) throws Exception;

}
