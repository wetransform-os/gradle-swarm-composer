/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.actions.assemble.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

/**
 * Custom Pebble extension.
 *
 * @author Simon Templer
 */
public class SwarmComposerExtension extends AbstractExtension {


  private Map<String, Filter> filters = new HashMap<>();

  public SwarmComposerExtension() {
    super();

    filters.put("indent", new IndentLineFilter());
  }

  @Override
  public Map<String, Filter> getFilters() {
    return Collections.unmodifiableMap(filters );
  }

}
