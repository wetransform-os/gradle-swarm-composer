/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mitchellbosecke.pebble.extension.escaper.EscapingStrategy;

/**
 * Escapes double quotes.
 *
 * @author Simon Templer
 */
public class DoubleQuotesEscaper implements EscapingStrategy {

  @Override
  public String escape(String input) {
    return input.replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\""));
  }

}
