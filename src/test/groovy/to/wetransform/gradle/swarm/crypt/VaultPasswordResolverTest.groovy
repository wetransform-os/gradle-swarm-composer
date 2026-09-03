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

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Tests for VaultPasswordResolver
 *
 * @author Simon Templer
 */
class VaultPasswordResolverTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder()

  /**
   * Command runner that records calls and answers from a map of fnox keys to results.
   */
  static class FakeRunner implements VaultPasswordResolver.CommandRunner {
    final List<List<String>> calls = []
    final Map<String, VaultPasswordResolver.CommandResult> results

    FakeRunner(Map<String, VaultPasswordResolver.CommandResult> results = [:]) {
      this.results = results
    }

    @Override
    VaultPasswordResolver.CommandResult run(List<String> command, File workingDir) {
      calls << command
      String key = command.last()
      results[key] ?: new VaultPasswordResolver.CommandResult(1, '', "Secret '${key}' not found")
    }
  }

  private static VaultPasswordResolver.CommandResult ok(String stdout) {
    new VaultPasswordResolver.CommandResult(0, stdout, '')
  }

  private VaultPasswordResolver resolver(Map<String, Object> properties, FakeRunner runner, String fnox = 'fnox') {
    new VaultPasswordResolver({ String name -> properties[name] }, fnox, runner, new File('.'))
  }

  @Test
  void testSetupPropertyWins() {
    def runner = new FakeRunner(['vault_password_dev': ok('from-fnox')])
    def r = resolver(['vault_password_dev': 'dev-secret', 'vault_password': 'generic'], runner)

    assert r.resolve('dev') == 'dev-secret'
    assert runner.calls.isEmpty()
  }

  @Test
  void testGenericPropertyFallback() {
    def runner = new FakeRunner(['vault_password_dev': ok('from-fnox')])
    def r = resolver(['vault_password': 'generic'], runner)

    assert r.resolve('dev') == 'generic'
    assert runner.calls.isEmpty()
  }

  @Test
  void testFnoxSetupKey() {
    def runner = new FakeRunner(['vault_password_dev': ok('dev-fnox'), 'vault_password': ok('generic-fnox')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == 'dev-fnox'
    assert runner.calls == [
      [
        'fnox',
        'get',
        'vault_password_dev'
      ]
    ]
  }

  @Test
  void testFnoxGenericKeyAfterFailure() {
    def runner = new FakeRunner(['vault_password': ok('generic-fnox')])
    def r = resolver([:], runner)

    assert r.resolve('my-setup') == 'generic-fnox'
    assert runner.calls == [
      [
        'fnox',
        'get',
        'vault_password_my-setup'
      ],
      [
        'fnox',
        'get',
        'vault_password'
      ]
    ]
  }

  @Test
  void testFnoxNotAvailable() {
    def runner = new FakeRunner(['vault_password': ok('generic-fnox')])
    def r = resolver([:], runner, null)

    assert r.resolve('dev') == null
    assert runner.calls.isEmpty()
  }

  @Test
  void testFnoxOutputTrimmed() {
    def runner = new FakeRunner(['vault_password_dev': ok('secret\n')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == 'secret'
  }

  @Test
  void testFnoxEmptyOutputIsMissing() {
    def runner = new FakeRunner(['vault_password_dev': ok('\n'), 'vault_password': ok('generic')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == 'generic'
  }

  @Test
  void testResultIsMemoized() {
    def runner = new FakeRunner(['vault_password_dev': ok('dev-fnox')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == 'dev-fnox'
    assert r.resolve('dev') == 'dev-fnox'
    assert runner.calls.size() == 1
  }

  @Test
  void testMissingPasswordMessageListsSources() {
    def runner = new FakeRunner()
    def r = resolver([:], runner)

    assert r.resolve('dev') == null
    String msg = r.missingPasswordMessage('dev')
    assert msg.contains('vault_password_dev')
    assert msg.contains('vault_password')
    assert msg.contains('fnox get vault_password_dev')
    assert msg.contains('fnox get vault_password')
    assert msg.contains("Secret 'vault_password' not found")
  }

  @Test
  void testMissingPasswordMessageWithoutFnox() {
    def r = resolver([:], new FakeRunner(), null)

    assert r.resolve('dev') == null
    String msg = r.missingPasswordMessage('dev')
    assert msg.contains('vault_password_dev')
    assert msg.contains('fnox')
    assert !msg.contains('fnox get')
  }

  @Test
  void testProcessRunnerCapturesOutputAndExitCode() {
    def runner = new VaultPasswordResolver.ProcessCommandRunner()

    // large output to make sure the streams are fully drained before the result is built
    int size = 500_000
    def result = runner.run(ProcessOutputHelper.command('output', "${size}", '3'), new File('.'))

    assert result.exitCode == 3
    assert result.stdout.count('o') == size
    // stderr may additionally contain JVM diagnostics (e.g. JAVA_TOOL_OPTIONS), the payload comes last
    assert result.stderr.endsWith('e' * size)
  }

  @Test(timeout = 10000L)
  void testProcessRunnerClosesStdin() {
    def runner = new VaultPasswordResolver.ProcessCommandRunner()

    // the helper only terminates once stdin is closed
    def result = runner.run(ProcessOutputHelper.command('stdin'), new File('.'))

    assert result.exitCode == 0
    assert result.stdout.trim() == 'done'
  }

  @Test
  void testProcessRunnerDecodesUtf8() {
    def runner = new VaultPasswordResolver.ProcessCommandRunner()

    def result = runner.run(ProcessOutputHelper.command('utf8'), new File('.'))

    assert result.stdout == 'ü'
  }

  @Test
  void testRunnerFailureIsTreatedAsMissing() {
    def runner = new FakeRunner() {
        @Override
        VaultPasswordResolver.CommandResult run(List<String> command, File workingDir) {
          throw new IOException('Cannot run program "fnox"')
        }
      }
    def r = resolver([:], runner)

    assert r.resolve('dev') == null
    assert r.missingPasswordMessage('dev').contains('Cannot run program "fnox"')
  }

  @Test
  void testMissingPasswordMessageListsErrorPerKey() {
    def runner = new FakeRunner([
      'vault_password_dev': new VaultPasswordResolver.CommandResult(1, '', 'dev key error'),
      'vault_password': new VaultPasswordResolver.CommandResult(1, '', 'generic key error')
    ])
    def r = resolver([:], runner)
    // lookup for another setup must not leak into the message for dev
    r.resolve('other')

    assert r.resolve('dev') == null
    String msg = r.missingPasswordMessage('dev')
    assert msg.contains('dev key error')
    assert msg.contains('generic key error')
    assert !msg.contains('vault_password_other')
  }

  @Test
  void testEmptyPropertyIsMissing() {
    def r = resolver(['vault_password_dev': '', 'vault_password': ''], new FakeRunner(), null)

    assert r.resolve('dev') == null
  }

  @Test
  void testFnoxOutputKeepsSurroundingSpaces() {
    def runner = new FakeRunner(['vault_password_dev': ok(' secret \n')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == ' secret '
  }

  @Test
  void testFnoxOutputStripsWindowsLineEnding() {
    def runner = new FakeRunner(['vault_password_dev': ok('secret\r\n')])
    def r = resolver([:], runner)

    assert r.resolve('dev') == 'secret'
  }

  @Test
  void testFindFnoxReturnsAbsolutePath() {
    File empty = tmp.newFolder('empty')
    File bin = tmp.newFolder('bin')
    File fnox = new File(bin, 'fnox')
    fnox.text = '#!/bin/sh\n'
    fnox.setExecutable(true)
    String path = [
      empty.absolutePath,
      bin.absolutePath
    ].join(File.pathSeparator)

    assert VaultPasswordResolver.findFnoxOnPath(path, false) == fnox.absolutePath
  }

  @Test
  void testFindFnoxIgnoresNonExecutable() {
    File bin = tmp.newFolder('bin')
    File fnox = new File(bin, 'fnox')
    fnox.text = 'not executable'
    fnox.setExecutable(false)

    assert VaultPasswordResolver.findFnoxOnPath(bin.absolutePath, false) == null
  }

  @Test
  void testFindFnoxOnWindowsOnlyAcceptsExe() {
    File bin = tmp.newFolder('bin')
    [
      'fnox.cmd',
      'fnox.bat',
      'fnox'
    ].each { new File(bin, it).text = 'shim' }

    assert VaultPasswordResolver.findFnoxOnPath(bin.absolutePath, true) == null

    File exe = new File(bin, 'fnox.exe')
    exe.text = 'binary'
    // on Windows any existing file is executable, mimic that here
    exe.setExecutable(true)
    assert VaultPasswordResolver.findFnoxOnPath(bin.absolutePath, true) == exe.absolutePath
  }

  @Test
  void testMissingPasswordMessageWhenFnoxDisabled() {
    def r = new VaultPasswordResolver({ null }, null, new FakeRunner(), new File('.'), true)

    assert r.resolve('dev') == null
    assert r.missingPasswordMessage('dev').contains('fnox (disabled')
  }
}
