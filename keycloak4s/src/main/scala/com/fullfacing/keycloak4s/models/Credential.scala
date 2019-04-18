package com.fullfacing.keycloak4s.models

import com.fullfacing.keycloak4s.models.enums.CredentialType

final case class Credential(`type`: CredentialType,
                            value: String,
                            algorithm: Option[String],
                            config: Option[MultivaluedHashMap],
                            counter: Option[Int],
                            createdDate: Option[Long],
                            device: Option[String],
                            digits: Option[Int],
                            hashIterations: Option[Int],
                            hashedSaltedValue: Option[String],
                            period: Option[Int],
                            salt: Option[String],
                            temporary: Option[Boolean])
