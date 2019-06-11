package utils

import com.fullfacing.keycloak4s.core.models.{KeycloakError, KeycloakThrowable}

object Errors {
  val CLIENT_NOT_FOUND: KeycloakError    = KeycloakThrowable(new Throwable("Client not found."))
  val COMPONENT_NOT_FOUND: KeycloakError = KeycloakThrowable(new Throwable("Component not found."))
  val SCOPE_NOT_FOUND: KeycloakError     = KeycloakThrowable(new Throwable("Client-Scope not found."))
  val GROUP_NOT_FOUND: KeycloakError     = KeycloakThrowable(new Throwable("Group not found."))
  val NO_CLIENTS_FOUND: KeycloakError    = KeycloakThrowable(new Throwable("No Clients found."))
  val NO_GROUPS_FOUND: KeycloakError     = KeycloakThrowable(new Throwable("No Groups found."))
  val NO_SESSIONS_FOUND: KeycloakError   = KeycloakThrowable(new Throwable("No Sessions found."))
  val NO_TOKENS_FOUND: KeycloakError     = KeycloakThrowable(new Throwable("No Tokens found."))
  val NO_USERS_FOUND: KeycloakError      = KeycloakThrowable(new Throwable("No Users found."))
  val ROLE_NOT_FOUND: KeycloakError      = KeycloakThrowable(new Throwable("Role not found."))
  val USER_NOT_FOUND: KeycloakError      = KeycloakThrowable(new Throwable("User not found."))
}
