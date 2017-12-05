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
