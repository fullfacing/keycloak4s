package com.fullfacing.keycloak4s.core.models

import com.fullfacing.keycloak4s.core.models.enums.CredentialType

final case class Credential(`type`: CredentialType,
                            value: String,
                            algorithm: Option[String] = None,
                            config: Option[MultivaluedHashMap] = None,
                            counter: Option[Int] = None,
                            createdDate: Option[Long] = None,
                            device: Option[String] = None,
                            digits: Option[Int] = None,
                            hashIterations: Option[Int] = None,
                            hashedSaltedValue: Option[String] = None,
                            period: Option[Int] = None,
                            salt: Option[String] = None,
                            temporary: Option[Boolean] = None)
