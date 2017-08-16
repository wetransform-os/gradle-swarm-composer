/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm.config

import java.nio.charset.StandardCharsets

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

/**
 * Helpers for configurations based on maps and lists.
 *
 * @author Simon Templer
 */
class ConfigHelper {

  /**
   * Merge configuration maps together.
   * Configurations in subsequent maps may override configuration from the previous maps.
   *
   * @param configs the configurations
   * @return the merged configuration
   */
  static Map mergeConfigs(Iterable<Map> configs) {
    configs.asCollection().inject([:], ConfigHelper.&combineMap)
  }

  private static Map combineMap(Map a, Map b) {
    if (a.is(b)) {
      return a
    }

    Map result = [:]
    result.putAll(a)
    b.each { key, value ->
      if (value != null) {
        result.merge(key, value, ConfigHelper.&combineValue)
      }
    }
    result
  }

  private static Object combineValue(Object a, Object b) {
    if (a instanceof Map) {
      if (b instanceof Map) {
        combineMap(a, b)
      }
      else {
        //XXX error?
        a
      }
    }
    else if (b instanceof Map) {
      //XXX error?
      b
    }
    else if (a instanceof List && b instanceof List) {
      def combined = []
      combined.addAll(a)
      combined.addAll(b)
      combined
    }
    else if (a instanceof List && !(b instanceof List)) {
      def combined = []
      combined.addAll(a)
      combined.add(b)
      combined
    }
    else if (!(a instanceof List) && b instanceof List) {
      def combined = []
      combined.add(a)
      combined.addAll(b)
      combined
    }
    else {
      // b overrides a
      //XXX message?
      b
    }
  }

  /**
   * Load a configuration from an environment file.
   *
   * @param envFile the environment file
   * @return the loaded configuration
   */
  static Map loadEnvironment(File envFile) {
    List lines = envFile.readLines(StandardCharsets.UTF_8.name())
    def pairs = lines.findResults { String line ->
      def matcher = (line =~ /^([^#=]+)=(.*)$/)
      if (matcher.size() >= 1) {
        def name = matcher[0][1].trim()
        def value = matcher[0][2]
        [name, value]
      }
      else {
        null
      }
    }
    pairs.collectEntries()
  }

  /**
   * Load a configuration from a YAML file.
   *
   * @param yamlFile the YAML file
   * @return the loaded configuration map
   */
  static Map loadYaml(File yamlFile) {
    Yaml yaml = new Yaml(new SafeConstructor());
    Map result
    yamlFile.withInputStream {
      result = yaml.load(it)
    }
  }

}
