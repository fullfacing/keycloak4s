package com.fullfacing.keycloak4s.authz.client.models

import java.util.UUID

final case class PermissionTicket(id: UUID,
                                  owner: String,
                                  resource: String,
                                  scope: String,
                                  granted: Boolean,
                                  scopeName: String,
                                  resourceName: String,
                                  requester: String,
                                  ownerName: String,
                                  requesterName: String)

object PermissionTicket {

  final case class Create(owner: Option[String] = None,
                          resource: Option[String] = None,
                          scope: Option[String] = None,
                          granted: Option[Boolean] = None,
                          scopeName: Option[String] = None,
                          resourceName: Option[String] = None,
                          requester: Option[String] = None,
                          ownerName: Option[String] = None,
                          requesterName: Option[String] = None)

  final case class Request(resource_id: String,
                           scopes: List[String] = List.empty[String],
                           resourceServerId: String,
                           claims: Map[String, List[String]])

  final case class Response(ticket: String)

  final case class Update(id: String,
                          owner: Option[String] = None,
                          resource: Option[String] = None,
                          scope: Option[String] = None,
                          granted: Option[Boolean] = None,
                          scopeName: Option[String] = None,
                          resourceName: Option[String] = None,
                          requester: Option[String] = None,
                          ownerName: Option[String] = None,
                          requesterName: Option[String] = None)
}
