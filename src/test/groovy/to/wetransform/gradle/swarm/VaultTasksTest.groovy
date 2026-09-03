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
package to.wetransform.gradle.swarm

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Tests for the vault related tasks created by the plugin.
 *
 * @author Simon Templer
 */
class VaultTasksTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder()

  private File setupDir

  @Before
  void createLayout() {
    def stackDir = tmp.newFolder('stacks', 'mystack')
    new File(stackDir, 'stack.yml').text = 'version: "3"\nservices: {}\n'
    setupDir = tmp.newFolder('setups', 'dev')
  }

  private Project buildProject(Map<String, Object> properties = [:]) {
    Project project = ProjectBuilder.builder().withProjectDir(tmp.root).build()
    properties.each { k, v -> project.ext[k] = v }
    project.apply(plugin: 'to.wetransform.swarm-composer')
    // never contact a developer's fnox store from tests
    project.composer.enableFnox = false
    project.evaluate()
    project
  }

  private static void execute(Task task) {
    task.actions.each { it.execute(task) }
  }

  @Test
  void testVaultTasksExistWithoutPassword() {
    Project project = buildProject()

    assert project.tasks.findByName('encrypt-dev')
    assert project.tasks.findByName('decrypt-dev')
    assert project.tasks.findByName('purgeSecrets-dev')
    assert project.tasks.findByName('prepareSetup-dev').taskDependencies.getDependencies(null).name.contains('decrypt-dev')
  }

  @Test
  void testDecryptWithoutVaultFilesNeedsNoPassword() {
    Project project = buildProject()

    execute(project.tasks.getByName('decrypt-dev'))
  }

  @Test
  void testDecryptFailsWithoutPassword() {
    new File(setupDir, 'app.vault.yml').text = 'key: value\n'
    Project project = buildProject()

    Task decrypt = project.tasks.getByName('decrypt-dev')
    try {
      execute(decrypt)
      throw new AssertionError('Expected decryption to fail')
    } catch (GradleException e) {
      assert e.message.contains('vault_password_dev')
      assert e.message.contains('fnox (disabled')
    }
  }

  @Test
  void testEncryptDecryptRoundTripWithProperty() {
    def plainFile = new File(setupDir, 'app.secret.yml')
    plainFile.text = 'key: value\n'
    Project project = buildProject(['vault_password_dev': 'test-password'])

    execute(project.tasks.getByName('encrypt-dev'))
    def vaultFile = new File(setupDir, 'app.vault.yml')
    assert vaultFile.exists()
    assert !vaultFile.text.contains('value')

    plainFile.delete()
    execute(project.tasks.getByName('decrypt-dev'))
    assert plainFile.exists()
    assert plainFile.text.contains('key: value')
  }
}
