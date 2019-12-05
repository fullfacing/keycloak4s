package com.fullfacing.keycloak4s.core.models

import java.util.UUID

import com.fullfacing.keycloak4s.core.models.enums.CredentialType

final case class Credential(id: Option[UUID] = None,
                            `type`: Option[CredentialType] = None,
                            createdDate: Option[Long] = None,
                            credentialData: Option[String] = None,
                            userLabel: Option[String] = None,
                            priority: Option[Int] = None,
                            secretData: Option[String] = None,
                            temporary: Option[Boolean] = None,
                            value: Option[String] = None)