gradle-swarm-composer
=====================

Plugin that provides utilities for assembling Docker compose configurations for use with Docker Compose or as Stacks for Docker Swarm mode.


Usage
-----


```groovy
buildscript {
  repositories {
    maven {
      url 'https://artifactory.wetransform.to/artifactory/private-snapshot-local'
    }
  }
  dependencies {
    classpath 'to.wetransform.hale:gradle-hale-plugin:1.0.0-SNAPSHOT'
  }
}

apply plugin: 'to.wetransform.swarm-composer'
```
