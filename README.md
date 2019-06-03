[![CircleCI](https://circleci.com/gh/fullfacing/keycloak4s.svg?style=shield&circle-token=0788f14be0abb7f8ab8194fbd2cd179122b3ee85)](https://circleci.com/gh/fullfacing/keycloak4s)
[![codecov](https://codecov.io/gh/fullfacing/keycloak4s/branch/master/graph/badge.svg?token=WKbJaagGhz)](https://codecov.io/gh/fullfacing/keycloak4s)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.fullfacing/keycloak4s-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/fullfacing/keycloak4s-core_2.12/)

# Changelog
All notable changes to this project will be documented in this file.

## [0.15.0]
### Added
- New security directive that handles auth at the top level, using a json config object.

## [0.11.0]
### Added
- Observable streaming to relevant get calls in Roles, Clients and RealmsAdmin in the monix module.
### Changed
- Updated the getList streaming call to return Eithers in accordance with update to all other calls.
- Renamed functions in RealmsAdmin to match project standards.
- Modified KeycloakClient in the monix module to extend that in the base module instead of duplicating code.

## [0.10.3]
### Changed
- Modified the logging for successful Admin API requests to log the raw response instead of the deserialized response.

## [0.10.1]
### Changed
- Moved models from admin module to core.

## [0.9.0]
### Added
- Added proper logging for the Admin module.
### Changed
- Injected a correlation ID into the Admin API calls.

## [0.8.0]
### Added
- Created core module.
- Created playground module.
### Changed
- Renamed "keycloak4s" module to "keycloak4s-admin".
- Renamed "keycloak4s-adapters" module to "keycloak4s-auth".
- Renamed "keycloak4s-monix" module to "keycloak4s-admin-monix".
- Moved core functionality from the auth, admin and monix modules to the newly created core module. Refactored as necessary.
- Moved sandbox code from the auth, admin and monix modules to the newly created playground module. Expanded sandbox code.
