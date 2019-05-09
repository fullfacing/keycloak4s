package com.fullfacing.keycloak4s.admin.models

import java.util.UUID

final case class ClientMappings(client: Option[String],
                                id: UUID,
                                mappings: List[Role])