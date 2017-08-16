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
   ├──local
   │  ├─ config1.yml
   │  └─ config2.env
   │
   └──setup2
      └─ config1.yml
```

### Configuration

Configuration can be done in `.yml` and `.env` files.
YAML configurations are accessible via their property path (segments separated separated by dots), variables defined in environment files are available with the `env.` prefix.

**TODO: ** Example file, Example for variable replacement, where can config files be placed, ...

#### Reserved variable names

Some variables are provided by swarm-composer and will override any variables you define with the same name:

- **stack** - The name of the stack
- **setup** - The name of the setup
- **mode** - The mode for the configuration assembly, either `swarm` or `compose`
- **DockerCompose** - Boolean that states if the mode is `compose` - very handy for conditions on Docker Compose specific blocks
- **SwarmMode** - Boolean that states if the mode is `swarm` - very handy for conditions on Swarm mode specific blocks
