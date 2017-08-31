/*
 * Copyright (c) 2017 wetransform GmbH
 * All rights reserved.
 */

package to.wetransform.gradle.swarm

import java.util.function.Supplier;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

import to.wetransform.gradle.swarm.actions.assemble.template.TemplateAssembler;
import to.wetransform.gradle.swarm.config.ConfigHelper
import to.wetransform.gradle.swarm.config.SetupConfiguration;
import to.wetransform.gradle.swarm.tasks.Assemble;

class SwarmComposerPlugin implements Plugin<Project> {

  private static final Map<String, Object> DEFAULT_SC_CONFIG = [:]

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

        def stackFile = new File(dir, 'stack.yml')
        if (stackFile.exists()) {
          // build tasks for the stack

          if (setupsDir?.exists()) {
            // build tasks for setups
            setupsDir.eachDir { setupDir ->
              def setup = setupDir.name

              def configFiles = []
              configFiles.addAll(stackConfigFiles)

              // load swarm composer config for setup
              def scConfig = loadSettings(setupDir)

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
                builds: stackBuilds.asImmutable())

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
              builds: stackBuilds.asImmutable())

            configureSetup(project, sc)
          }
        }
      }

    }
  }

  Map loadSettings(File setupDir) {
    def scConfigFile = new File(setupDir, 'swarm-composer.yml')
    scConfigFile.exists() ? (ConfigHelper.loadYaml(scConfigFile)) : DEFAULT_SC_CONFIG
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

    setupConfig.asCollection()
  }

  void configureSetup(Project project, final SetupConfiguration sc) {
    // store configuration in extension (for access for other tasks etc.)
    project.composer.configs.add(sc)


    // task loading the configuration
//    def configTaskName = "config-${sc.stackName}-${sc.setupName}"
//    def configTask = project.task(configTaskName).doFirst {
//      // load configuration -> write somewhere?
//      sc.config
//    }

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

      // create helper script
      boolean createScript = true
      if (createScript) {
        // add a script file for convenient Docker Compose calls
        File scriptFile = project.file(composeSupported ? "${sc.stackName}-${sc.setupName}.sh" : "deploy-${sc.stackName}-${sc.setupName}.sh")
        def relPath = project.projectDir.toPath().relativize( composeFile.toPath() ).toFile().toString()

        def run
        if (composeSupported) {
          run = "docker-compose -f \"$relPath\" \"\$@\""
        }
        else {
          run = "docker stack deploy --compose-file \"$relPath\" --with-registry-auth ${sc.stackName}"
        }

        def images = "./gradlew -Pquiet=true build-${sc.stackName}-${sc.setupName}"
        if (!composeSupported) {
          //TODO also push?!
        }

        scriptFile.text = """#!/bin/bash
set -e
$images
./gradlew ${taskName}
$run"""
        try {
          ['chmod', 'a+x', scriptFile.absolutePath].execute()
        } catch (e) {
          // ignore
        }
      }
    }

    // configure Docker image build tasks
    configureBuilds(project, sc, task)

  }

  void configureBuilds(Project project, final SetupConfiguration sc, def assembleTask) {
    if (!project.composer.enableBuilds) {
      return
    }

    def allName = "build-${sc.stackName}-${sc.setupName}"
    def allTask = project.task(allName) {
      group 'Build docker images'
      description "Build all Docker Images for stack ${sc.stackName} with setup ${sc.setupName}"
    }

    sc.builds.each { dFile ->
      if (dFile instanceof File && dFile.exists()) {
        final File parentDir = dFile.parentFile
        final File tempDir = new File(parentDir, '.sc-build')
        final String buildName = parentDir.name

        def settings = loadSettings(parentDir)

        //FIXME better configurable, other sources - for instance one image/repo for all stack images (e.g. to protect secrets)
        String imageName = settings.image_name
        boolean buildSpecificName = true
        assert imageName
        String imageTag
        if (buildSpecificName) {
          // relation to build already contained in name
          imageTag = "${imageName}:sc-${sc.stackName}-${sc.setupName}"
        }
        else {
          // build name should be included in tag
          imageTag = "${imageName}:sc-${sc.stackName}-${sc.setupName}-${buildName}"
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

          project.fileTree(dir: tempDir, includes: ['**/*'])
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

        boolean quietMode = Boolean.parseBoolean(project.findProperty('quiet') ?: 'false')

        def task = project.task("build-${sc.stackName}-${sc.setupName}-${buildName}", type: DockerBuildImage) {
          dependsOn setupTask

          dockerFile = new File(tempDir, dFile.name)
          inputDir = tempDir
          labels = ['sc-stack': sc.stackName, 'sc-setup': sc.setupName, 'sc-build': buildName]
          tag = imageTag

          //XXX quiet seems to break build
          //quiet = quietMode

          group 'Build individual image'
          description "Build \"${buildName}\" for stack ${sc.stackName} with setup ${sc.setupName}"
        }.doLast {
          // post processing

          //TODO delete temporary artifacts?
        }

        allTask.dependsOn(task)

        //TODO add push tasks
      }
    }
  }

}
