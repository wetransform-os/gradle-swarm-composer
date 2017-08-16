gradle-swarm-composer
=====================

Plugin that provides utilities for assembling Docker compose configurations for use with Docker Compose or as Stacks for Docker Swarm mode.

Goals of this plugin are:

- Support independent configurations for multiple deployments / setups for the same stack
- Allow distributing configuration for both setups and stacks across multiple files for better maintainability
- Support storing deployment specific configuration encrypted


Usage
-----


```groovy
buildscript {
  repositories {
    jcenter()
    maven {
      url 'https://artifactory.wetransform.to/artifactory/private-snapshot-local'
    }
  }
  dependencies {
    classpath 'to.wetransform:gradle-swarm-composer:1.0.0-SNAPSHOT'
  }
}

apply plugin: 'to.wetransform.swarm-composer'
```
