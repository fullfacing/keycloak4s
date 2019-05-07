[![CircleCI](https://circleci.com/gh/fullfacing/keycloak4s.svg?style=shield&circle-token=0788f14be0abb7f8ab8194fbd2cd179122b3ee85)](https://circleci.com/gh/fullfacing/keycloak4s)
[![codecov](https://codecov.io/gh/fullfacing/keycloak4s/branch/master/graph/badge.svg?token=WKbJaagGhz)](https://codecov.io/gh/fullfacing/keycloak4s)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.fullfacing/keycloak4s_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/fullfacing/keycloak4s_2.12/)

# Changelog
All notable changes to this project will be documented in this file.

## [0.8.0]
### Added
- Created core module.
- Created playground module.
###Changed
- Renamed "keycloak4s" module to "keycloak4s-admin".
- Renamed "keycloak4s-adapters" module to "keycloak4s-auth".
- Moved core functionality from the auth and admin modules to the newly created core module. Refactored as necessary.
- Moved sandbox code from the auth and admin modules to the newly created playground module. Expanded sandbox code.