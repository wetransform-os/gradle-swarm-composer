## [2.1.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.0.0...v2.1.0) (2025-01-17)

### Features

* support disabling setup specific Swarm scripts ([ee18793](https://github.com/wetransform-os/gradle-swarm-composer/commit/ee1879384828495a1a8cd9b22fed0215d24ee99b))

### Bug Fixes

* change Docker Compose command in scripts ([057ba00](https://github.com/wetransform-os/gradle-swarm-composer/commit/057ba007ffdca38cb430f4792a10fb60c700ff0c))

## [2.0.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v1.1.0...v2.0.0) (2024-10-31)

### ⚠ BREAKING CHANGES

* **deps:** Previously docker tasks required Java 8 to run and
failed with later versions. Now at least Java 11 is required.

### Bug Fixes

* **deps:** update dependency io.pebbletemplates:pebble to v3.2.2 ([af5cd40](https://github.com/wetransform-os/gradle-swarm-composer/commit/af5cd40ab2bcc73c0822c13b4882b5325ee84369))
* **deps:** update dependency junit:junit to v4.13.2 ([b07368f](https://github.com/wetransform-os/gradle-swarm-composer/commit/b07368f05d75f599e013c814f51585ef13034891))
* **deps:** update dependency org.yaml:snakeyaml to v2.3 ([a6a6307](https://github.com/wetransform-os/gradle-swarm-composer/commit/a6a63073f0bec42bb5c8432c8ea8c0536b769f51))
* **deps:** upgrade gradle-docker-plugin version to 9.4.0 ([21a5eb7](https://github.com/wetransform-os/gradle-swarm-composer/commit/21a5eb761354aab1c723d7c4f1b0059279af1708))

## [1.1.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v1.0.0...v1.1.0) (2024-10-31)

### Features

* add filter for merging objects ([c1ceee4](https://github.com/wetransform-os/gradle-swarm-composer/commit/c1ceee4823199ec67479ff2228c78c7d995dafb1))
* add function for failing processing ([a190ec1](https://github.com/wetransform-os/gradle-swarm-composer/commit/a190ec18bbbe370ac6a0e5213d3ae166f907924a))
* collect path information in PebbleCachingConfig ([9f48a83](https://github.com/wetransform-os/gradle-swarm-composer/commit/9f48a833a8c33a3bccb80d73deaa9fb19baa9fdd))

### Bug Fixes

* **deps:** use alice library published in wetransform artifactory ([a1d4766](https://github.com/wetransform-os/gradle-swarm-composer/commit/a1d4766d98cfc73cf0e5fcac8100de53675c2982))
* fix ConcurrentModificationException on Java 9+ ([298714a](https://github.com/wetransform-os/gradle-swarm-composer/commit/298714ac382c21206d2bbeaa8783f4398c79f456))

### Performance Improvements

* cache setting templates ([04e57ea](https://github.com/wetransform-os/gradle-swarm-composer/commit/04e57ea37bc588acd3ffc5ab4c735ae9bb18c192))
