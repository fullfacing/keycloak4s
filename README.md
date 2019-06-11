[![CircleCI](https://circleci.com/gh/fullfacing/keycloak4s.svg?style=shield&circle-token=0788f14be0abb7f8ab8194fbd2cd179122b3ee85)](https://circleci.com/gh/fullfacing/keycloak4s)
[![codecov](https://codecov.io/gh/fullfacing/keycloak4s/branch/master/graph/badge.svg?token=WKbJaagGhz)](https://codecov.io/gh/fullfacing/keycloak4s)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.fullfacing/keycloak4s-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/fullfacing/keycloak4s-core_2.12/)

Developed for Keycloak v6.0.1

# Changelog
All notable changes to this project will be documented in this file.

## [0.16.0]
### Added
- Added a new policy config structure that uses full paths to define rules instead of rules configured on nodes for each segment of the request path.
### Removed
- Removed the initial authorisation directives in favour of the policy configuration based authorisation.

## [0.15.0]
### Added
- Added a json security security structure that is used to configure security for a server.
- Added a security directive that handles auth at the top level of the directive structure, using the above json config.

## [0.14.0]
### Added
- Added an integration test suite and a bootstrap for a fresh Keycloak server instance to test on.
- Added integration tests for User, Realm, Role, Group and Client calls.
### Changed
- Minor model and function fixes for issues exposed by the integration tests.
- Fixed an illegal reflection warning by changing how a throwable was being handled by the Akka directives.

## [0.13.0]
### Changed
- Functions returning Observables in the Monix module have been modified to no longer return Eithers. Instead they are processed so that the right gets extracted while the left causes an exception to be thrown.

## [0.12.0]
### Added
- Added detailed logging for the token validation in the Admin module.
- Created an implicit Class for Payload that adds extraction helper functions.
### Changed
- Modified the token validation process to return the full payloads of the bearer and ID tokens.
- Fixed failing unit tests caused by lack of an implicit correlationId in the tests.

## [0.11.1]
### Changed
- Fixed token endpoint scheme to use the scheme defined in KeycloakConfig as opposed to being hard coded.

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
