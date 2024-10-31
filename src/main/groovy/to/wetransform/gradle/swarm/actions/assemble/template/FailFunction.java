/*
 * Copyright 2024 wetransform GmbH
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

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Simple function that fails with a given message.
 *
 * @author Simon Templer
 */
public class FailFunction implements Function {

  @Override
  public List<String> getArgumentNames() {
    return Collections.singletonList("message");
  }

  @Override
  public Integer execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Number result;
    Object messageRaw = args.get("message");

    String message;
    if (messageRaw != null) {
      message = messageRaw.toString();
    }
    else {
      message = "fail: (no message)";
    }

    throw new PebbleException(null, message.toString(), lineNumber, self.getName());
  }

}
