# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2019-09-05
### Added
- ConfigWithoutAuth; a version of KeycloakConfig that does not contain admin authentication details.
### Fixed
- Fixed a flaw in attemptBuild in PolicyBuilders.scala that could potentially throw a FileNotFoundException in certain environments.
### Changed
- Core functionality in the Akka-HTTP auth module has been split into a separate module to allow code reuse by any client adapter.
- Transformed KeycloakConfig into a trait, with ConfigWithAuth and ConfigWithoutAuth as subtypes.

## [1.1.0] - 2019-08-27
### Added
- KeycloakConfig.Auth split into two subtypes, Secret and Password, to additionally support a password grant type.
### Changed
- Updated EventTypes to support additions for Keycloak 7.0.0
- Modified create calls for services to return the created resource's UUID. 

## [0.20.1]
### Changed
- Changed the functions for evaluating user access to be stack safe.

## [0.20.0]
### Added
- Documentation has been added.
### Changed
- KeycloakClient in the Monix submodule now takes a parameter for the type of byte collection used by the backend for streaming.
- The changelog is now separate from the readme.
- Modified the authorization "secure" function to return the bearer tokens' payloads if successful.
- Renamed RealmRepresentation to Realm, EventRepresentation to Event, and RolesRepresentation to Roles.
- Moved case classes that are only implemented inside other case classes to their companion objects.
- Rewrote all admin javadocs to follow a standard.
- Renamed admin functions to follow a standard.
### Removed
- Removed the MonixHttpBackendL from the playground module.

## [0.19.3]
### Added
- Added missing Clients service calls with the path /{realm}/clients/{id}/scope-mappings.
- Created an Update model for Group.
### Changed
- Separated all client-scope calls from the Clients service into a new ClientScopes service.
- Updated the Groups service update function to take the new Group.Update model.
### Removed
- Removed the ScopeMappings service and moved the functions out into the Clients and ClientScopes services as necessary.

## [0.19.2]
### Changed
- Groups service role mapping functions (add and remove) now take Role.Mapping case class.
- Changed the fields in the Role.Mapping case class to be mandatory.

## [0.19.1]
### Fixed
- Fixed logging for requests sent to Keycloak being evaluated eagerly.

## [0.19.0]
### Added
- Added an And/Or data structure for better configuration of required roles on a path.
- Added support for UUID ( /{id}/ ) segments in configured paths.

## [0.18.0]
### Added
- Added policy enforcement object builders.
- Expanded logging to cover authorization.

## [0.17.0]
### Added
- Added a new policy config structure that uses full paths to define rules instead of rules configured on nodes for each segment of the request path.
- TokenValidator now has a validateParallel function that parses and validates an access and ID token asynchronously.
- Leeway for the exp, iat and nbf fields can now be specified in the TokenValidator constructor.
### Changed
- TokenValidator's validate function now only accepts one token.
- TokenValidator's constructor now requires the URI scheme to be specified. 
### Removed
- Removed the initial authorisation directives in favour of the policy configuration based authorisation.

## [0.16.0]
### Added
- Added new models and enumerators for Keycloak services.
### Changed
- Fixed multiple small issues encountered during integration testing.

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
