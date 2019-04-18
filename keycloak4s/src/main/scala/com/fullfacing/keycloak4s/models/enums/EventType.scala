package com.fullfacing.keycloak4s.models.enums

import enumeratum.EnumEntry
import enumeratum.Enum

import scala.collection.immutable

sealed trait EventType extends EnumEntry

case object EventTypes extends Enum[EventType] {
  case object CLIENT_DELETE                           extends EventType
  case object CLIENT_DELETE_ERROR                     extends EventType
  case object CLIENT_INFO                             extends EventType
  case object CLIENT_INFO_ERROR                       extends EventType
  case object CLIENT_INITIATED_ACCOUNT_LINKING        extends EventType
  case object CLIENT_INITIATED_ACCOUNT_LINKING_ERROR  extends EventType
  case object CLIENT_LOGIN                            extends EventType
  case object CLIENT_LOGIN_ERROR                      extends EventType
  case object CLIENT_REGISTER                         extends EventType
  case object CLIENT_REGISTER_ERROR                   extends EventType
  case object CLIENT_UPDATE                           extends EventType
  case object CLIENT_UPDATE_ERROR                     extends EventType
  case object CODE_TO_TOKEN                           extends EventType
  case object CODE_TO_TOKEN_ERROR                     extends EventType
  case object CUSTOM_REQUIRED_ACTION                  extends EventType
  case object CUSTOM_REQUIRED_ACTION_ERROR            extends EventType
  case object EXECUTE_ACTIONS                         extends EventType
  case object EXECUTE_ACTIONS_ERROR                   extends EventType
  case object EXECUTE_ACTION_TOKEN                    extends EventType
  case object EXECUTE_ACTION_TOKEN_ERROR              extends EventType
  case object FEDERATED_IDENTITY_LINK                 extends EventType
  case object FEDERATED_IDENTITY_LINK_ERROR           extends EventType
  case object IDENTITY_PROVIDER_FIRST_LOGIN           extends EventType
  case object IDENTITY_PROVIDER_FIRST_LOGIN_ERROR     extends EventType
  case object IDENTITY_PROVIDER_LINK_ACCOUNT          extends EventType
  case object IDENTITY_PROVIDER_LINK_ACCOUNT_ERROR    extends EventType
  case object IDENTITY_PROVIDER_LOGIN                 extends EventType
  case object IDENTITY_PROVIDER_LOGIN_ERROR           extends EventType
  case object IDENTITY_PROVIDER_POST_LOGIN            extends EventType
  case object IDENTITY_PROVIDER_POST_LOGIN_ERROR      extends EventType
  case object IDENTITY_PROVIDER_RESPONSE              extends EventType
  case object IDENTITY_PROVIDER_RESPONSE_ERROR        extends EventType
  case object IDENTITY_PROVIDER_RETRIEVE_TOKEN        extends EventType
  case object IDENTITY_PROVIDER_RETRIEVE_TOKEN_ERROR  extends EventType
  case object IMPERSONATE                             extends EventType
  case object IMPERSONATE_ERROR                       extends EventType
  case object INTROSPECT_TOKEN                        extends EventType
  case object INTROSPECT_TOKEN_ERROR                  extends EventType
  case object INVALID_SIGNATURE                       extends EventType
  case object INVALID_SIGNATURE_ERROR                 extends EventType
  case object LOGIN                                   extends EventType
  case object LOGIN_ERROR                             extends EventType
  case object LOGOUT                                  extends EventType
  case object LOGOUT_ERROR                            extends EventType
  case object PERMISSION_TOKEN                        extends EventType
  case object PERMISSION_TOKEN_ERROR                  extends EventType
  case object REFRESH_TOKEN                           extends EventType
  case object REFRESH_TOKEN_ERROR                     extends EventType
  case object REGISTER                                extends EventType
  case object REGISTER_ERROR                          extends EventType
  case object REGISTER_NODE                           extends EventType
  case object REGISTER_NODE_ERROR                     extends EventType
  case object REMOVE_FEDERATED_IDENTITY               extends EventType
  case object REMOVE_FEDERATED_IDENTITY_ERROR         extends EventType
  case object REMOVE_TOTP                             extends EventType
  case object REMOVE_TOTP_ERROR                       extends EventType
  case object RESET_PASSWORD                          extends EventType
  case object RESET_PASSWORD_ERROR                    extends EventType
  case object RESTART_AUTHENTICATION                  extends EventType
  case object RESTART_AUTHENTICATION_ERROR            extends EventType
  case object REVOKE_GRANT                            extends EventType
  case object REVOKE_GRANT_ERROR                      extends EventType
  case object SEND_IDENTITY_PROVIDER_LINK             extends EventType
  case object SEND_IDENTITY_PROVIDER_LINK_ERROR       extends EventType
  case object SEND_RESET_PASSWORD                     extends EventType
  case object SEND_RESET_PASSWORD_ERROR               extends EventType
  case object SEND_VERIFY_EMAIL                       extends EventType
  case object SEND_VERIFY_EMAIL_ERROR                 extends EventType
  case object TOKEN_EXCHANGE                          extends EventType
  case object TOKEN_EXCHANGE_ERROR                    extends EventType
  case object UNREGISTER_NODE                         extends EventType
  case object UNREGISTER_NODE_ERROR                   extends EventType
  case object UPDATE_EMAIL                            extends EventType
  case object UPDATE_EMAIL_ERROR                      extends EventType
  case object UPDATE_PASSWORD                         extends EventType
  case object UPDATE_PASSWORD_ERROR                   extends EventType
  case object UPDATE_PROFILE                          extends EventType
  case object UPDATE_PROFILE_ERROR                    extends EventType
  case object UPDATE_TOTP                             extends EventType
  case object UPDATE_TOTP_ERROR                       extends EventType
  case object VALIDATE_ACCESS_TOKEN                   extends EventType
  case object VALIDATE_ACCESS_TOKEN_ERROR             extends EventType
  case object VERIFY_EMAIL                            extends EventType
  case object VERIFY_EMAIL_ERROR                      extends EventType

  val values: immutable.IndexedSeq[EventType] = findValues
}
