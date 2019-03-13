package com.fullfacing.keycloak4s.models

case class AccessTokenAuthorization(permissions: Option[Map[String, AnyRef]]) //Potential for stronger typing, requires example.
