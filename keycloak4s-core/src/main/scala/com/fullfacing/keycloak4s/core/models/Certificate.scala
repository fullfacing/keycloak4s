package com.fullfacing.keycloak4s.core.models

final case class Certificate(certificate: Option[String],
                             kid: Option[String],
                             privateKey: Option[String],
                             publicKey: Option[String])