package com.fullfacing.keycloak4s.authz.monix.bio.models

import com.fullfacing.keycloak4s.core.models.AccessToken.Permission

final case class IntrospectionResponse(active: Boolean,
                                       permissions: List[Permission] = List.empty[Permission],
                                       jti: Option[String] = None,
                                       iss: Option[String] = None,
                                       aud: List[String] = List.empty[String],
                                       sub: Option[String] = None,
                                       typ: Option[String] = None,
                                       azp: Option[String] = None,
                                       acr: Option[String] = None,
                                       exp: Option[Long] = None,
                                       nbf: Option[Long] = None,
                                       iat: Option[Long] = None)

object IntrospectionResponse {
  final case class WithoutAud(active: Boolean,
                              permissions: List[Permission] = List.empty[Permission],
                              jti: Option[String] = None,
                              iss: Option[String] = None,
                              sub: Option[String] = None,
                              typ: Option[String] = None,
                              azp: Option[String] = None,
                              acr: Option[String] = None,
                              exp: Option[Long] = None,
                              nbf: Option[Long] = None,
                              iat: Option[Long] = None)

  private[bio] def apply(withoutAud: WithoutAud, aud: List[String]): IntrospectionResponse = {
    IntrospectionResponse(
      active      = withoutAud.active,
      permissions = withoutAud.permissions,
      jti         = withoutAud.jti,
      iss         = withoutAud.iss,
      aud         = aud,
      sub         = withoutAud.sub,
      typ         = withoutAud.typ,
      azp         = withoutAud.azp,
      acr         = withoutAud.acr,
      exp         = withoutAud.exp,
      nbf         = withoutAud.nbf,
      iat         = withoutAud.iat
    )
  }
}