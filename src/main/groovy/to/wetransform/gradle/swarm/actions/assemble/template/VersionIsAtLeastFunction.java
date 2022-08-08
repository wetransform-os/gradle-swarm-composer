/*
 * Copyright 2019 wetransform GmbH
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

/**
 * Function that checks if the given version meets the required version.
 *
 * @author Simon Templer
 */
public class VersionIsAtLeastFunction implements Function {

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList("version", "required");
  }

  /*
   * For testing
   */
  public Object execute(Map<String, Object> args) {
    return execute(args, null, null, -1);
  }

  /* (non-Javadoc)
   * @see com.mitchellbosecke.pebble.extension.Function#execute(java.util.Map, com.mitchellbosecke.pebble.template.PebbleTemplate, com.mitchellbosecke.pebble.template.EvaluationContext, int)
   */
  @Override
  public Boolean execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
    Semver version = toVersion(args.get("version"));
    Semver compareTo = toVersion(args.get("required"));

    return version.isGreaterThanOrEqualTo(compareTo);
  }

  private Semver toVersion(Object version) {
    String versionString = version.toString();

    // allow non-numeric prefix - remove it
    int firstDigit;
    for (firstDigit = 0; firstDigit < versionString.length() && !Character.isDigit(versionString.charAt(firstDigit)); firstDigit++) {}
    versionString = versionString.substring(firstDigit);

    return new Semver(versionString, SemverType.LOOSE);
  }

}
