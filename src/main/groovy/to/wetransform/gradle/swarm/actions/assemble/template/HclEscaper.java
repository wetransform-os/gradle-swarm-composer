/*
 * Copyright 2020 wetransform GmbH
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.pebbletemplates.pebble.extension.escaper.EscapingStrategy;

/**
 * Escaper for HCL (https://github.com/hashicorp/hcl).
 *
 * @author Simon Templer
 */
public class HclEscaper implements EscapingStrategy {

  @Override
  public String escape(String input) {
    String res = input;

    res = res.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"));
    res = res.replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\""));

    return res;
  }

}
