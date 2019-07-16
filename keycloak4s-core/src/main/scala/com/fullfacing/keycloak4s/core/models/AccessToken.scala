package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.Category

final case class AccessToken(acr: Option[String],
                             address: Option[AccessToken.AddressClaimSet],
                             allowed_origins: Option[List[String]],
                             at_hash: Option[String],
                             auth_time: Option[Int],
                             authorization: Option[AccessToken.Authorization],
                             azp: Option[String],
                             birthdate: Option[String],
                             c_hash: Option[String],
                             category: Option[Category],
                             claims_locales: Option[String],
                             cnf: Option[AccessToken.CertificateConfig],
                             email: Option[String],
                             email_verified: Option[Boolean],
                             exp: Option[String],
                             family_name: Option[String],
                             gender: Option[String],
                             given_name: Option[String],
                             iat: Option[Int],
                             iss: Option[String],
                             jti: Option[String],
                             local: Option[String],
                             middle_name: Option[String],
                             name: Option[String],
                             nickname: Option[String],
                             nonce: Option[String],
                             otherClaims: Option[Map[String, AnyRef]],
                             phone_number: Option[String],
                             phone_number_verified: Option[Boolean],
                             picture: Option[String],
                             preferred_username: Option[String],
                             profile: Option[String],
                             realm_access: Option[AccessToken.RealmAccess],
                             s_hash: Option[String],
                             scope: Option[String],
                             session_state: Option[String],
                             sub: Option[String],
                             `trusted-certs`: Option[List[String]],
                             typ: Option[String],
                             updated_at: Option[Long],
                             website: Option[String],
                             zoneinfo: Option[String])

object AccessToken {
  final case class RealmAccess(roles: Option[List[String]],
                               verify_caller: Option[Boolean])

  final case class Authorization(permissions: List[Permission] = List.empty[Permission])

  final case class Permission(claims: Option[Map[String, Any]],
                              rsid: Option[String],
                              rsname: Option[String],
                              scopes: Option[List[String]])

  final case class CertificateConfig(`x5t#S256`: Option[String])

  final case class AddressClaimSet(country: Option[String],
                                   formatted: Option[String],
                                   locality: Option[String],
                                   postal_code: Option[String],
                                   region: Option[String],
                                   street_address: Option[String])
}