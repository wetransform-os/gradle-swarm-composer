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
    mavenCentral()
    maven {
      url 'https://artifactory.wetransform.to/artifactory/local'
    }
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath 'to.wetransform:gradle-swarm-composer:2.0.0'
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
│  │  ├──builds
│  │  │  └──mycustom
│  │  │     └─ Dockerfile
│  │  └──config
│  │     ├─ config1.yml
│  │     ├─ config2.vault.yml
│  │     └─ config3.env
│  │
│  └──stack2
│     ├─ swarm-composer.yml
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

#### Secret variables

Sensible information like passwords can be stored in encrypted configuration files.
These files then also for instance can be added to version control.

For encrypted configuration files right now only the YAML format is supported, variables must be string values.

To create an encrypted configuration file, first create its plain counterpart in the setup folder.
The file names of the plain configuration files should end with `.secret.yml`.

You also need to provide the password to use for the encryption.
It is looked up in the following order, the first source that provides a password wins:

1. Gradle property `vault_password_<setup>`
2. Gradle property `vault_password`
3. [fnox](https://fnox.jdx.dev) secret `vault_password_<setup>` (via `fnox get`)
4. fnox secret `vault_password` (via `fnox get`)

The fnox lookup is only attempted if the `fnox` executable is found on the `PATH`.
It can be turned off completely by setting `enableFnox = false` in the `composer` extension.
It is run in the project directory, so the `fnox.toml` of the project (or a parent directory) is used.
This allows keeping vault passwords out of Gradle properties files and the environment.

The password is only resolved when a task actually needs it, i.e. when there are files to encrypt or decrypt.
If vault files exist for a setup but no source provides a password, decryption fails with a message listing the sources that were checked.
As decryption is part of the preparation for a setup, this also applies to the assemble and build tasks of that setup.

Note that the `purgeSecrets` task deletes the plain `.secret.yml` files of all setups, regardless of whether a password is configured for them.

To encrypt the configuration file, run the encryption task for the respective setup (e.g. `./gradlew encrypt-<setup>`).
Encrypted vault files have a file name that ends with `.vault.yml`.

Note that when accessing the setup configuration, the plain files are recreated.
If you want to remove them after a task, also add the `purgeSecrets` task.

If you want to edit a vault file, you can either add encrpyted entries there, or simply decrypt the file with the task `decrypt-<setup>` and encrypt it after you completed your changes.

#### Reserved variable names

Some variables are provided by swarm-composer and will override any variables you define with the same name:

- **stack** - The name of the stack
- **setup** - The name of the setup
- **builds** - Under this key information on Docker images built individually for a setup can be found, for example `builds.mycustom.image_tag`
