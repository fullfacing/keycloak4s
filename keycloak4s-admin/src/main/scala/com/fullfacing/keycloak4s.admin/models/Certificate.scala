package com.fullfacing.keycloak4s.admin.models

final case class Certificate(certificate: Option[String],
                             kid: Option[String],
                             privateKey: Option[String],
                             publicKey: Option[String])