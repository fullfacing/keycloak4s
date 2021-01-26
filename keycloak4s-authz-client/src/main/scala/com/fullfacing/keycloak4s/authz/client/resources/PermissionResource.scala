package com.fullfacing.keycloak4s.authz.client.resources

import com.fullfacing.keycloak4s.authz.client.AuthzClient
import com.fullfacing.keycloak4s.authz.client.models.PermissionTicket
import com.fullfacing.keycloak4s.core.models.KeycloakError
import monix.bio.IO
import com.fullfacing.keycloak4s.admin.Utilities._
import sttp.client.UriContext
import sttp.model.Uri.QuerySegment.KeyValue

class PermissionResource[S](implicit client: AuthzClient[S]) {

  private val PERMISSION_ENDPOINT = client.serverConfig.permissionEndpoint

  def request(requests: List[PermissionTicket.Request]): IO[KeycloakError, PermissionTicket.Response] = {
    client.post[PermissionTicket.Response](uri"$PERMISSION_ENDPOINT", requests)
  }

  def create(body: PermissionTicket.Create): IO[KeycloakError, PermissionTicket] = {
    client.post[PermissionTicket](uri"$PERMISSION_ENDPOINT/ticket", body)
  }

  def findByScope(scopeId: String): IO[KeycloakError, List[PermissionTicket]] = {
    client
      .get[List[PermissionTicket]](uri"$PERMISSION_ENDPOINT/ticket", KeyValue("scopeId", scopeId) :: Nil)
  }

  def findByResource(resourceId: String): IO[KeycloakError, List[PermissionTicket]] = {
    client
      .get[List[PermissionTicket]](uri"$PERMISSION_ENDPOINT/ticket", KeyValue("resourceId", resourceId) :: Nil)
  }

  def find(resource: Option[String] = None,
           scope: Option[String] = None,
           owner: Option[String] = None,
           requester: Option[String] = None,
           granted: Option[Boolean] = None,
           returnNames: Option[Boolean] = None,
           first: Option[Int] = None,
           max: Option[Int] = None): IO[KeycloakError, List[PermissionTicket]] = {
    val query = List(
      ("resourceId", resource),
      ("scopeId", scope),
      ("owner", owner),
      ("requester", requester),
      ("granted", granted),
      ("returnNames", returnNames),
      ("first", first),
      ("max", max)
    )

    client
      .get[List[PermissionTicket]](uri"$PERMISSION_ENDPOINT/ticket", createQuery(query: _*))
  }

  def update(update: PermissionTicket.Update): IO[KeycloakError, Unit] = {
    client.put[Unit](uri"$PERMISSION_ENDPOINT/ticket", update)
  }

  def delete(ticketId: String): IO[KeycloakError, Unit] = {
    client.delete[Unit](uri"$PERMISSION_ENDPOINT/ticket/$ticketId")
  }
}
