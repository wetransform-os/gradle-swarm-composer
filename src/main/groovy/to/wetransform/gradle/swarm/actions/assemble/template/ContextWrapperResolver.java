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

package to.wetransform.gradle.swarm.actions.assemble.template;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.attributes.ResolvedAttribute;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;

/**
 * @author simon
 *
 */
public class ContextWrapperResolver implements AttributeResolver {

  @Override
  public ResolvedAttribute resolve(Object instance, Object attributeNameValue, Object[] argumentValues,
      ArgumentsNode args, EvaluationContextImpl context, String filename, int lineNumber) {
    if (instance instanceof ContextWrapper) {
      return new ResolvedAttribute(((ContextWrapper) instance).getDynamicAttribute(attributeNameValue, argumentValues));
    }
    return null;
  }

}
