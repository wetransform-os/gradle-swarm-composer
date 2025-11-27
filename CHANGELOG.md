## [3.0.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.4.0...v3.0.0) (2025-11-27)

### ⚠ BREAKING CHANGES

* require Java 17

### Features

* make plugin classpath available to Groovy scripts ([ba42f55](https://github.com/wetransform-os/gradle-swarm-composer/commit/ba42f55fffa57ee65129e73a207ef8220c17ddcc)), closes [ING-4849](https://wetransform.atlassian.net/browse/ING-4849)

### Bug Fixes

* **deps:** update dependency io.pebbletemplates:pebble to v4 ([1c87e65](https://github.com/wetransform-os/gradle-swarm-composer/commit/1c87e65275ce79ccd4bb5139f0c6ef4468b1bfe8))
* **deps:** update dependency org.yaml:snakeyaml to v2.5 ([43403c2](https://github.com/wetransform-os/gradle-swarm-composer/commit/43403c29d4577b264f35804a11d9b189031d8c79))

### Build System

* require Java 17 ([fcf4c75](https://github.com/wetransform-os/gradle-swarm-composer/commit/fcf4c75b3208c14a0ecac6011bcd857a325ee1da))

## [2.4.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.3.0...v2.4.0) (2025-05-21)

### Features

* add flatten filter for collections ([50a55c9](https://github.com/wetransform-os/gradle-swarm-composer/commit/50a55c9c964a12dc7db0088055c4a19487ba1682)), closes [WGS-3239](https://wetransform.atlassian.net/browse/WGS-3239)

## [2.3.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.2.0...v2.3.0) (2025-05-15)

### Features

* pass HTTP proxy settings to image builds ([16ed375](https://github.com/wetransform-os/gradle-swarm-composer/commit/16ed375144b2c238d33ad3c6bb4da0781f83595d))

## [2.2.0](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.1.2...v2.2.0) (2025-05-15)

### Features

* add escape strategy for here documents ([63a3a61](https://github.com/wetransform-os/gradle-swarm-composer/commit/63a3a6136d4cbbb7964f72c8834811c9facc5855)), closes [ING-4797](https://wetransform.atlassian.net/browse/ING-4797)

## [2.1.2](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.1.1...v2.1.2) (2025-04-14)

### Bug Fixes

* **deps:** update dependency io.pebbletemplates:pebble to v3.2.3 ([a1680b8](https://github.com/wetransform-os/gradle-swarm-composer/commit/a1680b89f13eb1dfa4a0cb32a0b55d4e57f50d3d))
* **deps:** update dependency io.pebbletemplates:pebble to v3.2.4 ([b140ae7](https://github.com/wetransform-os/gradle-swarm-composer/commit/b140ae7e0a83a86f1031a599987c3a584748b1b1))
* **deps:** update dependency org.yaml:snakeyaml to v2.4 ([1f6d1d0](https://github.com/wetransform-os/gradle-swarm-composer/commit/1f6d1d0015dfbc52f6cd8f35ed1d8c134c308bd5))
* retain map structure when filtering/mapping ContextWrapper ([cc8a959](https://github.com/wetransform-os/gradle-swarm-composer/commit/cc8a9598e861165dc3df1a39f646b05377b0d662)), closes [ING-4587](https://wetransform.atlassian.net/browse/ING-4587)

## [2.1.1](https://github.com/wetransform-os/gradle-swarm-composer/compare/v2.1.0...v2.1.1) (2025-01-23)

### Bug Fixes

* fix reference to stack file in common stack deploy scripts ([8046264](https://github.com/wetransform-os/gradle-swarm-composer/commit/80462648c2a69d9e5ad074d5cc787b8b6d96ec31))

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
