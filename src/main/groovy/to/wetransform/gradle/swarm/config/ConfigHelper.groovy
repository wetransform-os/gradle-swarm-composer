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

package to.wetransform.gradle.swarm.config

import java.nio.charset.StandardCharsets

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator
import to.wetransform.gradle.swarm.util.Helpers

/**
 * Helpers for configurations based on maps and lists.
 *
 * @author Simon Templer
 */
class ConfigHelper {

  static Map<String, Object> loadConfig(List configFiles, String stackName = null, String setupName = null,
      Map initialConfig = null, boolean evaluate = true) {
    // config files
    def configs = configFiles.collect { cfg ->

      if (cfg instanceof Map) {
        // already a loaded configuration
        cfg
      }
      else {
        Map<String, Object> result = [:]
        File configFile = Helpers.toFile(cfg)
        if (configFile && configFile.exists()) {
          if (configFile.name.endsWith('.env')) {
            // load environment file
            result.env = loadEnvironment(configFile)
          }
          else if (configFile.name.endsWith('.yml') || configFile.name.endsWith('.yaml')) {
            result = loadYaml(configFile)
          }
        }
        result
      }
    }

    // merge configuration files
    def clist
    if (initialConfig) {
      //XXX should this configuration override config from files?
      //XXX for now let it override, otherwise external tasks cannot override configuration
      clist = []
      clist.addAll(configs)
      clist << initialConfig
    }
    else {
      clist = configs
    }
    Map<String, Object> context = mergeConfigs(clist)

    // stack and setup names
    if (stackName) {
      context.stack = stackName
    }
    if (setupName) {
      context.setup = setupName
    }

    // evaluate configuration
    if (evaluate) {
      ConfigEvaluator evaluator = new PebbleCachingEvaluator()
      context = evaluator.evaluate(context)
    }

    context
  }

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
    result ?: [:]
  }

  /**
   * Load a configuration from a YAML file.
   *
   * @param yamlFile the YAML file
   * @return the loaded configuration map
   */
  static void saveYaml(Map config, File yamlFile) {
    DumperOptions options = new DumperOptions()
//    options.explicitStart = true
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    Yaml yaml = new Yaml(options);
    Map result
    yamlFile.withWriter(StandardCharsets.UTF_8.name()) {
      result = yaml.dump(config, it)
    }
  }

}
