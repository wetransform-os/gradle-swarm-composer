gradle-swarm-composer
=====================

Plugin that provides utilities for assembling Docker compose configurations for use with Docker Compose or as Stacks for Docker Swarm mode.

Goals of this plugin are:

- Support independent configurations for multiple deployments / setups for the same stack
- Allow distributing configuration for both setups and stacks across multiple files for better maintainability
- Support storing deployment specific configuration encrypted
- Ensure that required configuration variables are set
- Allow to easily extend an existing deployment configuration


Usage
-----


```groovy
buildscript {
  repositories {
    jcenter()
    maven {
      url 'https://artifactory.wetransform.to/artifactory/libs-snapshot-local'
    }
  }
  dependencies {
    classpath 'to.wetransform:gradle-swarm-composer:1.0.0-SNAPSHOT'
  }
}

apply plugin: 'to.wetransform.swarm-composer'
```

### Default project layout

```
root
│
├──stacks
│  ├──stack1
│  │  ├─ stack.yml
│  │  └──config
│  │     ├─ config1.yml
│  │     └─ config2.env
│  │
│  └──stack2
│     └─ stack.yml
│
└──setups
   ├──setup1
   │  ├─ swarm-composer.yml
   │  ├─ config1.yml
   │  └─ config2.env
   │
   └──setup2
      └─ config1.yml
```

### Configuration

Individual configuration for swarm-composer regarding each setup can be done via the `swarm-compose.yml` file.
Here are the currently supported options explained with an example:

```yaml
# Swarm composer configuration
description: |
  Optional description of the setup for documentation purposes

# a setup may extend other setups to avoid duplicate configuration
extends:
  - other-setup
  - another-setup

# States that this setup creates a configuration compatible to Docker Compose
docker-compose: true

# Custom target file in project root
target-file: docker-compose.yml
```

### Template variables

Configuration variables to be used in templates can be defined in `.yml` and `.env` files.
YAML configurations are accessible via their property path (segments separated separated by dots), variables defined in environment files are available with the `env.` prefix.

**TODO:** Example file, Example for variable replacement, where can config files be placed, ...

**TODO:** Configuration that controls swarm-composer behavior (e.g. which modes are enabled, if another setup should be extended, if a custom file name should be used, etc.)

Restrictions for variable evaluation in configuration files: Simple value insertions/replacements work, for conditions only boolean variables are supported right now. 

#### Reserved variable names

Some variables are provided by swarm-composer and will override any variables you define with the same name:

- **stack** - The name of the stack
- **setup** - The name of the setup
