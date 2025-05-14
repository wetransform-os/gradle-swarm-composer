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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pebbletemplates.pebble.error.AttributeNotFoundException;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Test;
import io.pebbletemplates.pebble.extension.core.DefaultFilter;
import io.pebbletemplates.pebble.extension.core.EmptyTest;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * Filter that throws an error with a custom message if a property is not present.
 *
 * Needs to extend {@link DefaultFilter} because only an instanceof check prevents
 * the normal {@link AttributeNotFoundException}.
 *
 * @author Simon Templer
 */
public class OrErrorFilter extends DefaultFilter {

  private static final String ARG_MESSAGE = "message";

  private final List<String> argumentNames = new ArrayList<>();

  public OrErrorFilter() {
    this.argumentNames.add(ARG_MESSAGE);
  }

  @Override
  public List<String> getArgumentNames() {
    return this.argumentNames;
  }

  @Override
  public Object apply(Object input, Map<String, Object> args, PebbleTemplate self,
    EvaluationContext context, int lineNumber) throws PebbleException {

    Test emptyTest = new EmptyTest();
    if (emptyTest.apply(input, new HashMap<>(), self, context, lineNumber)) {
      Object messageRaw = args.get(ARG_MESSAGE);

      String message;
      if (messageRaw != null) {
        message = messageRaw.toString();
      } else {
        message = "orError: null or empty input";
      }

      throw new PebbleException(null, message.toString(), lineNumber, self.getName());
    }
    return input;
  }

}
