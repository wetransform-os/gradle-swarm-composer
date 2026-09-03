/*
 * Copyright 2026 wetransform GmbH
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
package to.wetransform.gradle.swarm.crypt

import groovy.transform.Immutable

import java.nio.charset.StandardCharsets

/**
 * Resolves the password protecting the vault files of a setup.
 *
 * Sources are checked in order: setup specific Gradle property, generic
 * Gradle property, setup specific fnox secret, generic fnox secret.
 *
 * @author Simon Templer
 */
class VaultPasswordResolver {

  /** Name of the generic property / fnox key, setup specific ones append `_<setup>` */
  static final String PROPERTY_NAME = 'vault_password'

  /**
   * Result of running an external command.
   */
  @Immutable
  static class CommandResult {
    int exitCode
    String stdout
    String stderr
  }

  /**
   * Runs an external command.
   */
  interface CommandRunner {
    /**
     * @throws IOException if the command cannot be started
     */
    CommandResult run(List<String> command, File workingDir) throws IOException
  }

  /**
   * Command runner that starts a process and captures its output.
   */
  static class ProcessCommandRunner implements CommandRunner {
    @Override
    CommandResult run(List<String> command, File workingDir) throws IOException {
      def out = new ByteArrayOutputStream()
      def err = new ByteArrayOutputStream()
      Process process = new ProcessBuilder(command).directory(workingDir).start()
      // no input is provided, make sure the process cannot wait for it
      process.outputStream.close()
      // waits for the process to finish and both streams to be fully read
      process.waitForProcessOutput(out, err)
      new CommandResult(process.exitValue(), out.toString(StandardCharsets.UTF_8.name()),
        err.toString(StandardCharsets.UTF_8.name()))
    }
  }

  /**
   * Find the fnox executable on the PATH.
   *
   * @return the absolute path of the executable or null if it is not available
   */
  static String findFnoxOnPath() {
    findFnoxOnPath(System.getenv('PATH'), System.getProperty('os.name')?.toLowerCase()?.contains('windows'))
  }

  /**
   * Find the fnox executable in the given search path.
   *
   * On Windows only `fnox.exe` is accepted, as batch shims cannot be started directly.
   *
   * @param path the search path, entries separated by the platform path separator
   * @param windows if the platform is Windows
   * @return the absolute path of the executable or null if it is not available
   */
  static String findFnoxOnPath(String path, boolean windows) {
    if (!path) {
      return null
    }
    String name = windows ? 'fnox.exe' : 'fnox'
    path.split(File.pathSeparator).findResult { String dir ->
      def f = new File(dir, name)
      f.isFile() && f.canExecute() ? f.absolutePath : null
    }
  }

  private final Closure<Object> propertyLookup
  private final String fnoxExecutable
  private final CommandRunner runner
  private final File workingDir
  private final boolean fnoxDisabled

  private final Map<String, String> cache = [:]
  /** Errors from fnox lookups by key */
  private final Map<String, String> fnoxErrors = [:]

  /**
   * @param propertyLookup closure returning the value of a Gradle property by name or null
   * @param fnoxExecutable path or name of the fnox executable, null if fnox is not available
   * @param runner the runner for external commands
   * @param workingDir the working directory for fnox invocations
   * @param fnoxDisabled if the fnox lookup is disabled by configuration, fnoxExecutable is ignored then
   */
  VaultPasswordResolver(Closure<Object> propertyLookup, String fnoxExecutable, CommandRunner runner, File workingDir,
  boolean fnoxDisabled = false) {
    this.propertyLookup = propertyLookup
    this.fnoxExecutable = fnoxDisabled ? null : fnoxExecutable
    this.runner = runner
    this.workingDir = workingDir
    this.fnoxDisabled = fnoxDisabled
  }

  /**
   * Create a resolver using the properties of a Gradle project and fnox from the PATH.
   *
   * @param project the project
   * @param enableFnox if fnox may be used to look up passwords
   */
  static VaultPasswordResolver forProject(org.gradle.api.Project project, boolean enableFnox) {
    new VaultPasswordResolver({ String name -> project.findProperty(name) },
    enableFnox ? findFnoxOnPath() : null, new ProcessCommandRunner(), project.projectDir, !enableFnox)
  }

  /**
   * Resolve the vault password for a setup.
   *
   * @param setupName the name of the setup
   * @return the password or null if no source provides one
   */
  synchronized String resolve(String setupName) {
    if (cache.containsKey(setupName)) {
      return cache[setupName]
    }

    String password = keys(setupName).findResult { String key -> fromProperty(key) }
    if (!password && fnoxExecutable) {
      password = keys(setupName).findResult { String key -> fromFnox(key) }
    }

    cache[setupName] = password
    password
  }

  /**
   * Describe why no password could be resolved for a setup.
   *
   * @param setupName the name of the setup
   * @return a message listing the sources that were checked
   */
  synchronized String missingPasswordMessage(String setupName) {
    def sources = keys(setupName).collect { "Gradle property ${it}" }
    if (fnoxExecutable) {
      sources.addAll(keys(setupName).collect { String key ->
        String error = fnoxErrors[key]
        error ? "fnox get ${key}: ${error}" : "fnox get ${key}"
      })
    }
    else if (fnoxDisabled) {
      sources << 'fnox (disabled via composer.enableFnox)'
    }
    else {
      sources << 'fnox (not found on PATH)'
    }
    "No vault password found for setup ${setupName}. Checked sources:\n" +
      sources.collect { "  - ${it}" }.join('\n')
  }

  private List<String> keys(String setupName) {
    [
      "${PROPERTY_NAME}_${setupName}".toString(),
      PROPERTY_NAME
    ]
  }

  private String fromProperty(String key) {
    String value = propertyLookup(key)?.toString()
    value ?: null
  }

  private String fromFnox(String key) {
    CommandResult result
    try {
      result = runner.run([fnoxExecutable, 'get', key], workingDir)
    } catch (IOException e) {
      fnoxErrors[key] = e.message
      return null
    }
    if (result.exitCode == 0) {
      // only remove the line ending added by the CLI, the secret itself is kept verbatim
      String value = result.stdout?.replaceFirst(/\r?\n$/, '')
      return value ?: null
    }
    fnoxErrors[key] = result.stderr?.trim() ?: "exit code ${result.exitCode}".toString()
    null
  }
}
