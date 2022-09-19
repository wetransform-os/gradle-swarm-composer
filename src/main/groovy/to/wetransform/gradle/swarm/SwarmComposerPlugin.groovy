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

package to.wetransform.gradle.swarm

import java.util.function.Supplier;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar

import com.bmuschko.gradle.docker.DockerRegistryCredentials
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler;
import to.wetransform.gradle.swarm.config.ConfigHelper
import to.wetransform.gradle.swarm.config.SetupConfiguration
import to.wetransform.gradle.swarm.config.pebble.PebbleCachingEvaluator
import to.wetransform.gradle.swarm.config.pebble.RootOrLocalMap
import to.wetransform.gradle.swarm.crypt.ConfigCryptor
import to.wetransform.gradle.swarm.crypt.SimpleConfigCryptor;
import to.wetransform.gradle.swarm.crypt.alice.AliceCryptor;
import to.wetransform.gradle.swarm.tasks.Assemble;

class SwarmComposerPlugin implements Plugin<Project> {

  private static final Map<String, Object> DEFAULT_SC_CONFIG = [:]

  private static final String PLAIN_FILE_IDENTIFIER = 'secret'

  private static final String ENCRYPTED_FILE_IDENTIFIER = 'vault'

  private final def groovyEngine = new groovy.text.SimpleTemplateEngine()

  void apply(Project project) {
    // register extension
    project.extensions.create('composer', SwarmComposerExtension, project)

    project.afterEvaluate { p ->
      if (project.composer.enableBuilds) {
        project.apply(plugin: 'com.bmuschko.docker-remote-api')
        project.repositories {
          jcenter()
        }

        if (project.composer.dockerConfig) {
          project.docker(project.composer.dockerConfig)
        }
      }

      addDefaultTasks(p)
    }
  }

  void addDefaultTasks(Project project) {
    File stacksDir = project.composer.stacksDir
    File setupsDir = project.composer.setupsDir

    if (stacksDir?.exists()) {

      // create default tasks for assembling compose files
      stacksDir.eachDir { dir ->
        def name = dir.name

        def stackConfigFiles = []

        def stackSettings = loadSettings(dir)
        def extendedStacks = collectExtendedConfigs(project, stacksDir, name, stackSettings)
        project.logger.info("Stack $name extends these stacks: $extendedStacks")

        // add configuration for extended stacks
        try {
          extendedStacks.each { extended ->
            stackConfigFiles.addAll(collectConfigFiles(project, stacksDir, extended,
              ['config/**/*.env', 'config/**/*.yml', 'config/**/*.yaml']))
          }
        } catch (e) {
          throw new RuntimeException('Error collecting configuration files from extended stacks', e)
        }

        // stack base configuration
        stackConfigFiles.addAll(collectConfigFiles(project, stacksDir, name,
          ['config/**/*.env', 'config/**/*.yml', 'config/**/*.yaml']))

        // identify builds
        def stackBuilds = []
        if (project.composer.enableBuilds) {
          // add builds from extended stacks
          try {
            extendedStacks.each { extended ->
              stackBuilds.addAll(collectConfigFiles(project, stacksDir, extended,
                ['builds/*/Dockerfile']))
            }
          } catch (e) {
            throw new RuntimeException('Error collecting Dockerfile builds from extended stacks', e)
          }

          // add builds from stack
          stackBuilds.addAll(collectConfigFiles(project, stacksDir, name,
            ['builds/*/Dockerfile']))

          project.logger.info("Stack $name includes these builds:\n${stackBuilds.join('\n')}\n")
        }

        def defaultStackFile = new File(dir, 'stack.yml')
        if (defaultStackFile.exists()) {
          // build tasks for the stack

          if (setupsDir?.exists()) {
            // build tasks for setups
            setupsDir.eachDir { setupDir ->
              def setup = setupDir.name

              def configFiles = []
              configFiles.addAll(stackConfigFiles)

              // load swarm composer config for setup
              def scConfig = loadSettings(setupDir)

              def stackFile = defaultStackFile
              // support alternate stack file
              if (scConfig['stack-file']) {
                def altFile = new File(defaultStackFile.parentFile, scConfig['stack-file'])
                if (altFile.exists()) {
                  stackFile = altFile
                }
                else {
                  project.logger.info("Alternate stack file ${scConfig['stack-file']} for setup $setup does not exist in stack $name - using default")
                }
              }

              def extendedSetups = collectExtendedConfigs(project, setupsDir, setup, scConfig)
              project.logger.info("Setup $setup extends these setups: $extendedSetups")

              // add configuration for extended setups
              try {
                extendedSetups.each { extended ->
                  configFiles.addAll(collectConfigFiles(project, setupsDir, extended))
                }
              } catch (e) {
                throw new RuntimeException('Error collecting configuration files from extended setups', e)
              }

              // add configuration for this setup
              configFiles.addAll(collectConfigFiles(project, setupsDir, setup))

              project.logger.info("Setup $setup uses these configuration files:\n${configFiles.join('\n')}\n")

              def sc = new SetupConfiguration(
                stackFile: stackFile,
                stackName: name,
                setupName: setup,
                configFiles: configFiles,
                settings: scConfig,
                builds: stackBuilds.asImmutable(),
                setupDir: setupDir)

              configureSetup(project, sc)
            }
          }
          else {
            // build default task

            def configFiles = []
            configFiles.addAll(stackConfigFiles)

            // load swarm composer config for setup
            def scConfig = loadSettings(project.projectDir)

            // collect setup configuration files
            def defaultConfig = project.fileTree(
              dir: project.projectDir,
              includes: ['*.env', '*-config.yml', '*-config.yaml'],
              excludes: ['swarm-composer.yml'])
            configFiles.addAll(defaultConfig.asCollection())

            def sc = new SetupConfiguration(
              stackFile: stackFile,
              stackName: name,
              setupName: 'default',
              configFiles: configFiles,
              settings: scConfig,
              builds: stackBuilds.asImmutable(),
              setupDir: null)

            configureSetup(project, sc)
          }
        }
      }

    }
  }

  Map loadSettings(File setupDir) {
    def scConfigFile = new File(setupDir, 'swarm-composer.yml')
    scConfigFile.exists() ? (ConfigHelper.loadYaml(scConfigFile) ?: DEFAULT_SC_CONFIG) : DEFAULT_SC_CONFIG
  }

  Collection collectExtendedConfigs(Project project, File setupsDir, String setupName, Map scConfig) {
    def extend = scConfig['extends']
    if (extend) {
      Deque<String> toProcess = new LinkedList<>()
      toProcess.addAll(extend)
      Set<String> handled = new HashSet<>()
      def result = []

      while (!toProcess.empty) {
        String candidate = toProcess.pollLast()

        if (!handled.contains(candidate)) {
          // add to result
          result << candidate

          // check candidate for direct dependencies
          def candConfig = loadSettings(new File(setupsDir, candidate))
          def candExtend = candConfig['extends']
          if (candExtend) {
            candExtend.reverse().each {
              toProcess.addFirst(it)
            }
          }
        }
      }

      result.reverse() // reverse to have correct extension order
    }
    else {
      []
    }
  }

  Collection collectConfigFiles(Project project, File setupsDir, String setupName,
    List includes = ['*.env', '*.yml', '*.yaml']) {

    File setupDir = new File(setupsDir, setupName)

    // collect setup configuration files
    def setupConfig = project.fileTree(
      dir: setupDir,
      includes: includes,
      excludes: ['swarm-composer.yml'])

    setupConfig.asCollection().collect { file ->
      def name = file.name

      // for secret files use plain counterpart
      if (name.contains(".${ENCRYPTED_FILE_IDENTIFIER}.")) {
        def plain = name.replaceAll("\\.${ENCRYPTED_FILE_IDENTIFIER}\\.",
          ".${PLAIN_FILE_IDENTIFIER}.")
        def neighbor = new File(file.parentFile, plain)
        neighbor
      }
      else {
        file
      }
    }.unique()
  }

  String toHcl(Object config) {
    // rudimentary hcl export
    // currently used to create tfvars files for terraspace (terraform actually supports tfvars.json files, but terraspace does not)

    def res = new StringBuilder()

    if (config instanceof Map) {
      config.each { key, value ->
        res.append(key)
        res.append(' = ')
        res.append(toHcl(value))
        res.append('\n')
      }
    }
    else if (config instanceof List) {
      res.append('[')
      config.eachWithIndex { value, index ->
        if (index > 0) {
          res.append(',')
        }
        res.append('\n  ')
        res.append(toHcl(value))
      }
      res.append('\n]')
    }
    else {
      // otherwise use Json representation
      res.append(JsonOutput.toJson(config))
    }

    res.toString()
  }

  void configureSetup(Project project, final SetupConfiguration sc) {
    // store configuration in extension (for access for other tasks etc.)
    project.composer.configs.add(sc)

    if (project.composer.enableConfigExport) {
      // task exporting the configuration (mainly for debugging purposes)
      def configTaskName = "export-config-${sc.stackName}-${sc.setupName}"
      def exportConfigTask = project.task(configTaskName) {
        group 'Export configuration'
      }.doFirst {
        // export unevaluated configuration
        def unevaluatedFile = new File(sc.stackFile.parentFile, "${sc.setupName}-unevaluated-config.yml")
        ConfigHelper.saveYaml(sc.unevaluated, unevaluatedFile)

        // export evaluated configuration
        def evaluatedFile = new File(sc.stackFile.parentFile, "${sc.setupName}-evaluated-config.yml")
        // lenient evaluation so a failure does not prevent the export
        def config = new PebbleCachingEvaluator(true, project.projectDir).evaluate(sc.unevaluated)
        ConfigHelper.saveYaml(config, evaluatedFile)
      }

      // make sure preparation tasks are run before export, as well as decryption
      setupPrepareTasks(project, exportConfigTask, sc)
    }

    def getConfigValue = { Map config, String varname ->
      Deque parts = new LinkedList(varname.split(/\./).toList())
      def value = config
      while (!parts.empty) {
        String part = parts.poll()
        if (value instanceof Map) {
          value = value[part]
        }
        else {
          throw new IllegalStateException("$varname not found")
        }
      }

      return value
    }

    if (project.composer.enableConfigExport) {
      // task exporting the specific configuration variables
      def configTaskName = "export-vars-${sc.stackName}-${sc.setupName}"
      def exportVarsTask = project.task(configTaskName) {
        group 'Export configuration variables to a file'
      }.doFirst {
        def exportFile = project.properties.'export-file' as String
        def vars = project.properties.'export-vars' as String
        assert exportFile
        assert vars

        def format = (project.properties.'export-format' as String)?.toLowerCase()
        if (!format) {
          // try to use file extension
          int lastDot = exportFile.lastIndexOf('.')
          if (lastDot >= 0) {
            format = exportFile.substring(lastDot + 1)
          }
        }

        def varList = vars.split(/,/).collect{ it.trim() }

        /*
         * Note: when using the unevaluated config as in the export-config task
         * for some reason references (e.g. to vault) can't be resolved.
         *
         * So using the evaluated config directly instead of the unevaluated one.
         */
        //new PebbleCachingEvaluator(false).evaluate(sc.unevaluated)
        def config = sc.config

        def varMap = varList.collectEntries { varname ->
          [(varname): getConfigValue(config, varname)]
        }

        def writeTo = new File(exportFile)
        if (format == 'shell' || format == 'sh') {
          def lines = varMap.collect { String varname, value ->
            def var = varname.replaceAll(/\W/, '_')
            def val = JsonOutput.toJson(value)
            //TODO properly escape/quote?
            if (value instanceof Map || value instanceof List) {
              // attempt to properly quote lists and maps, and wrap them in strings
              val = JsonOutput.toJson(val)
            }

            "${var}=${val}"
          }

          writeTo.text = lines.join('\n') + '\n'
        }
        else if (format == 'json') {
          if (varMap.size() == 1 && varMap.values().iterator().next() instanceof Map) {
            // if there is only one variable and it's value is a map, write only the value map
            writeTo.text = JsonOutput.prettyPrint(JsonOutput.toJson(varMap.values().iterator().next()))
          }
          else {
            // write variable map
            writeTo.text = JsonOutput.prettyPrint(JsonOutput.toJson(varMap))
          }
        }
        else if (format == 'tfvars' || format == 'hcl') {
          if (varMap.size() == 1 && varMap.values().iterator().next() instanceof Map) {
            // if there is only one variable and it's value is a map, write only the value map
            writeTo.text = toHcl(varMap.values().iterator().next())
          }
          else {
            // write variable map
            writeTo.text = toHcl(varMap)
          }
        }
        else {
          // default to yaml

          if (varMap.size() == 1 && varMap.values().iterator().next() instanceof Map) {
            // if there is only one variable and it's value is a map, write only the value map
            ConfigHelper.saveYaml(varMap.values().iterator().next(), writeTo)
          }
          else {
            // write variable map
            ConfigHelper.saveYaml(varMap, writeTo)
          }
        }
      }

      // make sure preparation tasks are run before export, as well as decryption
      setupPrepareTasks(project, exportVarsTask, sc)
    }

    def vaultGroup = 'Configuration vault'

    def purgeSecretsName = 'purgeSecrets'
    def purgeSecretsTask = project.tasks.findByPath(purgeSecretsName)
    if (!purgeSecretsTask) {
      purgeSecretsTask = project.task(purgeSecretsName) {
        group = vaultGroup
        description = 'Delete all plain text secret files'
      }
    }

    def decryptTask
    if (sc.setupDir) {
      // encryption / decryption tasks

      // get password
      def password = project.findProperty("vault_password_${sc.setupName}")
      if (!password) {
        password = project.findProperty("vault_password")
      }

      if (password) {
        def encryptName = "encrypt-${sc.setupName}"
        if (!project.tasks.findByPath(encryptName)) {
          def encryptTask = project.task(encryptName) {
            group = vaultGroup
            description = "Create encrypted vault files from plain text secret files for setup ${sc.setupName}"
          }.doFirst {
            ConfigCryptor cryptor = new SimpleConfigCryptor(new AliceCryptor())

            def files = project.fileTree(
              dir: sc.setupDir,
              includes: ["*.${PLAIN_FILE_IDENTIFIER}.*"]).asCollection()

            files.each { plainFile ->
              def name = plainFile.name.replaceAll("\\.${PLAIN_FILE_IDENTIFIER}\\.", ".${ENCRYPTED_FILE_IDENTIFIER}.")
              def secretFile = new File(plainFile.parentFile, name)

              /*
               * XXX instead encrypt whole file?
               *
               * Advantages:
               * - structure and comments preserved exactly
               * - independent of file format
               * Disadvantages:
               * - not transparent which settings were changed in the encrypted file
               *
               * Both the file and the current implementation would allow handling
               * encrypted configuration in memory without creating plain files.
               * What stands in the way there is the fact that extended setups
               * may have a different password protecting it.
               */

              // read, encrypt (with reference), write
              //XXX only YAML supported right now
              def config = ConfigHelper.loadYaml(plainFile)
              def reference
              if (secretFile.exists()) {
                try {
                  reference = ConfigHelper.loadYaml(secretFile)
                } catch (e) {
                  // ignore
                }
              }
              config = cryptor.encrypt(config, password, reference)
              ConfigHelper.saveYaml(config, secretFile)
              // add comment to file
              def comment = "# Encrypted configuration"
              secretFile.text = comment + '\n' + secretFile.text
            }
          }
        }

        def decryptName = "decrypt-${sc.setupName}"
        if (!project.tasks.findByPath(decryptName)) {
          decryptTask = project.task(decryptName) {
            group = vaultGroup
            description = "Create plain text secret files from encrypted vault files for setup ${sc.setupName}"
          }.doFirst {
            ConfigCryptor cryptor = new SimpleConfigCryptor(new AliceCryptor())

            def files = project.fileTree(
              dir: sc.setupDir,
              includes: ["*.${ENCRYPTED_FILE_IDENTIFIER}.*"]).asCollection()

            files.each { secretFile ->
              def name = secretFile.name.replaceAll("\\.${ENCRYPTED_FILE_IDENTIFIER}\\.",
                ".${PLAIN_FILE_IDENTIFIER}.")
              def plainFile = new File(secretFile.parentFile, name)

              // read, decrypt, write
              //XXX only YAML supported right now
              def config = ConfigHelper.loadYaml(secretFile)
              config = cryptor.decrypt(config, password)
              ConfigHelper.saveYaml(config, plainFile)
              // add comment to file
              def now = new Date().toInstant().toString()
              def comment = "# Decrypted configuration last updated on ${now}\n" +
                '# DO NOT ADD TO VERSION CONTROL'
              plainFile.text = comment + '\n' + plainFile.text
            }
          }
        }

        // purge task
        def purgeName = "purgeSecrets-${sc.setupName}"
        if (!project.tasks.findByPath(purgeName)) {
          def purgeTask = project.task(purgeName) {
            group = vaultGroup
            description = "Delete all plain text secret files for setup ${sc.setupName}"
          }.doLast {
            project.fileTree(dir: sc.setupDir,
                includes: ["*.${PLAIN_FILE_IDENTIFIER}.*"]).each { File file ->
              file.delete()
            }
          }
          purgeSecretsTask.dependsOn(purgeTask)
        }

      }
    }


    // assemble description
    def desc = "Generates compose file for stack ${sc.stackName} with setup ${sc.setupName}"
    def customDesc = sc.settings?.description?.trim()
    if (customDesc) {
      desc = customDesc
    }

    // task for assembling compose file
    def taskName = "assemble-${sc.stackName}-${sc.setupName}"
    def task = project.task(taskName) {
      group 'Assemble compose file'
      description desc
    }.doFirst {
      def dcConfig = sc.settings['docker-compose']
      boolean composeSupported = dcConfig == null ? false : dcConfig

      def k8sConfig = sc.settings['kubernetes']
      boolean k8sSupported = k8sConfig == null ? false : k8sConfig

      // default target file
      def composeFile = new File(sc.stackFile.parentFile, "${sc.setupName}-stack.yml")

      // custom target file
      def targetFile = sc.settings['target-file']
      if (targetFile) {
        composeFile = project.file(targetFile)
      }

      // run actual assembly of the compose/stack file
      project.composer.assemble {
        template = sc.stackFile
        config = [sc.config]
        target = composeFile
      }

      // YAML post processors
      // XXX post processing disabled for kubernetes
      if (!sc.yamlPostProcessors.empty && !k8sSupported) {
        sc.yamlPostProcessors.each { Closure processor ->
          Closure c = processor.clone()
          // load yaml
          def yaml = ConfigHelper.loadYaml(composeFile)
          def changed
          if (c.maximumNumberOfParameters == 1) {
            // only provide yaml
            changed = c(yaml)
          }
          else if (c.maximumNumberOfParameters == 2) {
            // provide yaml and configuration
            changed = c(yaml, sc.config)
          }
          else {
            // provide yaml, configuration and target file
            changed = c(yaml, sc.config, composeFile)
          }
          if (changed) {
            ConfigHelper.saveYaml(yaml, composeFile)
          }
        }
      }

      // create helper script
      def scriptConfig = sc.settings['generate-scripts']
      boolean createScript = scriptConfig == null ? true : scriptConfig

      //XXX disable script generation for kubernetes until we have code for that
      if (k8sSupported) {
        createScript = false
      }

      if (createScript) {
        // add a script file for convenient Docker Compose calls
        File scriptFile = project.file(composeSupported ? "${sc.stackName}-${sc.setupName}.sh" : "deploy-${sc.stackName}-${sc.setupName}.sh")
        def relPath = project.projectDir.toPath().relativize( composeFile.toPath() ).toFile().toString()

        def run
        def check = ''

        boolean includeCheck = project.composer.swarmSetupChecks

        if (composeSupported) {
          run = "docker-compose -f \"$relPath\" \"\$@\""
          if (includeCheck) {
            check = """echo "Checking if connected to a Swarm..."
              |docker node ls
              |if [ \$? -ne 0 ]; then
              |  echo "Not connected to a Swarm node - continuing..."
              |else
              |  echo "You are connected to a Swarm node."
              |  echo "Use Docker service or stack commands to interact with the Swarm instead of docker-compose."
              |  exit 1
              |fi
              |""".stripMargin()
          }
        }
        else {
          run = "docker stack deploy --compose-file \"$relPath\" --with-registry-auth ${sc.stackName}"
          if (includeCheck) {
            check = """echo "Checking \\"sc-setup\\" label to check if Docker is connected to the correct Swarm..."
              |SETUP=\$(docker node inspect self --format "{{ index .Spec.Labels \\"sc-setup\\"}}")
              |if [ -z "\$SETUP" ]; then
              |  SETUP=\$(docker node inspect self --format "{{ index .Description.Engine.Labels \\"sc-setup\\"}}")
              |fi
              |if [ "\$SETUP" != "${sc.setupName}" ]; then
              |  echo "Found label for setup \\"\$SETUP\\" instead of \\"${sc.setupName}\\""
              |  echo "Please make sure you are connected to the right swarm"
              |  exit 1
              |fi
              |echo "Found setup label \\"\$SETUP\\""
              |""".stripMargin()
          }
        }

        def gradleArgs = []

        gradleArgs.add(0, '-Pquiet=true')
        gradleArgs << "build-${sc.stackName}-${sc.setupName}"
        if (!composeSupported) {
          // also add push
          gradleArgs << "push-${sc.stackName}-${sc.setupName}"
        }

        gradleArgs << taskName

        scriptFile.text = """#!/bin/bash
$check
set -e
./gradlew ${gradleArgs.join(' ')}
$run"""
        try {
          ['chmod', 'a+x', scriptFile.absolutePath].execute()
        } catch (e) {
          // ignore
        }
      }
    }

    setupPrepareTasks(project, task, sc)

    // make sure decrypt task runs as part of preparation
    if (decryptTask) {
      project.tasks."prepareSetup-${sc.setupName}".dependsOn(decryptTask)
    }

    // configure Docker image build tasks
    configureBuilds(project, sc, task)

  }

  void configureBuilds(Project project, final SetupConfiguration sc, def assembleTask) {
    if (!project.composer.enableBuilds) {
      return
    }

    // task for all builds for a stack-setup combination
    def allName = "build-${sc.stackName}-${sc.setupName}"
    def allTask = project.task(allName) {
      group 'Build docker images'
      description "Build all Docker images for stack ${sc.stackName} with setup ${sc.setupName}"
    }

    def pushAllName = "push-${sc.stackName}-${sc.setupName}"
    def pushAllTask = project.task(pushAllName) {
      group 'Push docker images'
      description "Push all Docker images for stack ${sc.stackName} with setup ${sc.setupName}"
    }

    def config = sc.unevaluated
    // hacky way to create a copy of the config
    config = new JsonSlurper().parseText(JsonOutput.toJson(config))
    // try to evaluate config as good as possible
    try {
      config = new PebbleCachingEvaluator(true, project.projectDir).evaluate(config)
    } catch (e) {
      project.logger.error("Error evaluating configuration for builds of stack ${sc.stackName} with setup ${sc.setupName}", e)
    }

    sc.builds.each { dFile ->
      if (dFile instanceof File && dFile.exists()) {
        final File parentDir = dFile.parentFile
        final File tempDir = new File(parentDir, '.sc-build')
        final String buildName = parentDir.name

        def settings = loadSettings(parentDir)

        def fixed = [stack: sc.stackName, setup: sc.setupName, build: buildName]
        def settingBinding = config == null ? fixed : new RootOrLocalMap(
          // add fixed bindings as root (to override values)
          fixed,
          // main config
          config,
          // allow extending root map
          true,
          // don't make local available in addition on special key
          false
          )

        // check if build is enabled
        def enabled = settings.enabled
        if (enabled == null) {
          enabled = true
        }
        else if (enabled != true && enabled != false) {
          // expecting a string
          def str = enabled.toString()
          enabled = evaluateSetting(str, settingBinding)
          if (enabled instanceof String) {
            enabled = Boolean.parseBoolean(enabled)
          }
        }

        if (!enabled) {
          // only configure build if it is enabled
          return
        }

        // build configured image
        String image = settings.image_name
        boolean buildSpecificName = true

        if (!image) {
          // check if there is a global image configured (for all builds)
          // use unevaluated because config may not be accessed at configuration time
          //XXX or allow inheritance also for swarm-composer.yml files?
          def globalImage = sc.unevaluated.builds?.image_name
          if (globalImage) {
            image = globalImage
            buildSpecificName = false
          }
        }

        assert image
        image = evaluateSetting(image, settingBinding)

        // build configured image version
        String imageVersion = settings.image_version
        if (!imageVersion) {
          // try "global" configuration (for all builds)
          imageVersion = sc.unevaluated.builds?.image_version
        }
        if (imageVersion) {
          // evaluate
          imageVersion = evaluateSetting(imageVersion, settingBinding)
        }
        else {
          // use defaults
          if (buildSpecificName) {
            imageVersion = "sc-${sc.stackName}-${sc.setupName}"
          }
          else {
            imageVersion = "sc-${sc.stackName}-${sc.setupName}-${buildName}"
          }
        }

        // custom registry credentials for a build
        def customCredentials = settings.registry_credentials
        if (customCredentials) {
          if (customCredentials.url) { // evaluate url
            customCredentials.url = evaluateSetting(customCredentials.url, settingBinding)
          }
          if (customCredentials.username) { // evaluate username
            customCredentials.username = evaluateSetting(customCredentials.username, settingBinding)
          }
          if (customCredentials.password) { // evaluate password
            customCredentials.password = evaluateSetting(customCredentials.password, settingBinding)
          }
        }

        String imageTag
        if (buildSpecificName) {
          // relation to build already contained in name
          imageTag = "${image}:${imageVersion}"
        }
        else {
          // build name should be included in tag
          imageTag = "${image}:${imageVersion}"
        }

        // extend configuration with info on build image
        def results = [
          builds:
            [(buildName): [
                image_tag: imageTag
              ]
            ]
          ]
        sc.addConfig(results)

        def setupTask = project.task("setup-build-${sc.stackName}-${sc.setupName}-${buildName}").doFirst {
          // setup Docker build context
          tempDir.deleteDir()
          tempDir.mkdir()

          project.copy {
            from parentDir
            into tempDir
            include '**/*'
            exclude tempDir.name
            exclude 'swarm-composer.yml'
          }

          // evaluate templates
          TemplateAssembler processor = project.composer.templateEngine
          assert processor

          project.fileTree(dir: tempDir, includes: ['**/*'], excludes: ['**/*.inc.*'])
            .filter { File f -> !f.isDirectory() }
            .each { File f ->
              ByteArrayOutputStream result
              def supplier = {
                result = new ByteArrayOutputStream()
                result
              } as Supplier<OutputStream>
              processor.compile(f, sc.config, supplier)
              if (result != null) {
                f.withOutputStream {
                  result.writeTo(it)
                }
              }
            }
        }

        setupPrepareTasks(project, setupTask, sc, buildName)

        boolean quietMode = Boolean.parseBoolean(project.findProperty('quiet') ?: 'false')

        boolean pullImage = Boolean.parseBoolean(project.findProperty('pull') ?: 'true')

        def task = project.task("build-${sc.stackName}-${sc.setupName}-${buildName}", type: DockerBuildImage) {
          dependsOn setupTask

          dockerFile = new File(tempDir, dFile.name)
          inputDir = tempDir
          labels = ['sc-stack': sc.stackName, 'sc-setup': sc.setupName, 'sc-build': buildName]
          tag = imageTag

          //XXX quiet seems to break build
          //quiet = quietMode

          pull = pullImage

          if (customCredentials) {
            registryCredentials = new DockerRegistryCredentials()
            registryCredentials.url = customCredentials.url
            registryCredentials.username = customCredentials.username
            registryCredentials.password = customCredentials.password
          }

          group 'Build individual image'
          description "Build \"${buildName}\" for stack ${sc.stackName} with setup ${sc.setupName}"
        }.doLast {
          // post processing

          //TODO delete temporary artifacts?
        }

        allTask.dependsOn(task)

        // add push tasks

        def pushTask = project.task("push-${sc.stackName}-${sc.setupName}-${buildName}", type: DockerPushImage) {
          def sepIndex = imageTag.lastIndexOf(':')

          imageName = (sepIndex >= 0) ? imageTag.substring(0, sepIndex) : imageTag
          tag = (sepIndex >= 0 && sepIndex + 1 < imageTag.length()) ? imageTag.substring(sepIndex + 1) : ''

          group 'Push individual image'
          description "Push image for build \"${buildName}\" for stack ${sc.stackName} with setup ${sc.setupName}"
        }

        pushAllTask.dependsOn(pushTask)

      }
    }
  }

  private Task ensureTask(String name, String groupName, String descr, Project project) {
    def task = project.tasks.findByPath(name)
    if (task == null) {
      task = project.task(name) {
        group groupName
        description descr
      }
    }
    task
  }

  private void setupPrepareTasks(Project project, Task task, SetupConfiguration sc, String build = null) {
    // prepare tasks allow for easily adding custom logic/configuration
    // in preparation for assemble and build tasks

    def groupName = 'Prepare for build and assemble'
    def groupBuild = 'Build preparation'

    // overall

    ensureTask('prepare', groupName, 'Preparation for all stacks and setups', project)
    task.dependsOn('prepare')

    if (build) {
      ensureTask('prepareBuild', groupBuild, 'Preparation for all stacks and setups', project)
      task.dependsOn('prepareBuild')
    }

    // stack

    ensureTask("prepare-${sc.stackName}", groupName, "Preparation for ${sc.stackName} stack", project)
    task.dependsOn("prepare-${sc.stackName}")

    if (build) {
      ensureTask("prepareBuild-${sc.stackName}", groupBuild, "Preparation for ${sc.stackName} stack", project)
      task.dependsOn("prepareBuild-${sc.stackName}")

      ensureTask("prepareBuild-${sc.stackName}-${build}", groupBuild, "Preparation for build ${build} in ${sc.stackName} stack", project)
      task.dependsOn("prepareBuild-${sc.stackName}-${build}")
    }

    // setup

    ensureTask("prepareSetup-${sc.setupName}", groupName, "Preparation for setup ${sc.setupName}", project)
    task.dependsOn("prepareSetup-${sc.setupName}")
  }

  /**
   * Evaluate a setting using a GString like template.
   *
   * That a different syntax than for the stack/setup configuration is used is
   * by intention, to make clear that the settings follow different rules.
   *
   * @param setting the setting
   * @param binding the binding for the evaluation
   * @return the evaluated setting
   */
  private String evaluateSetting(String setting, Map binding) {
    def template = groovyEngine.createTemplate(setting).make(binding)
    try {
      template.toString()
    } catch (Exception e) {
      // catch and rethrow to add more details
      throw new RuntimeException("Error evaluating setting script:\n$setting\n\nBinding keys: ${binding.keySet()}", e)
    }
  }

}
