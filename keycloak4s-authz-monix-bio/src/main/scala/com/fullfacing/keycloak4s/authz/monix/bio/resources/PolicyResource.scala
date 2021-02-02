package com.fullfacing.keycloak4s.authz.monix.bio.resources

import com.fullfacing.keycloak4s.admin.utils.Service.createQuery
import com.fullfacing.keycloak4s.authz.monix.bio.client.AuthzClient
import com.fullfacing.keycloak4s.authz.monix.bio.models.UmaPermission
import com.fullfacing.keycloak4s.authz.monix.bio.models.UmaPermission.Create
import com.fullfacing.keycloak4s.core.models.KeycloakError
import monix.bio.IO
import sttp.client.UriContext

final class PolicyResource[S](resourceId: String)(implicit client: AuthzClient[S]) {

  private val POLICY_ENDPOINT = client.serverConfig.policy_endpoint

  def create(create: Create): IO[KeycloakError, UmaPermission] = {
    client.post[UmaPermission](uri"$POLICY_ENDPOINT/$resourceId", create)
  }

  def update(update: UmaPermission): IO[KeycloakError, Unit] = {
    client.put[Unit](uri"$POLICY_ENDPOINT/${update.id}", update)
  }

  def find(first: Option[Int] = None,
           max: Option[Int] = None,
           name: Option[String] = None,
           scope: Option[String] = None): IO[KeycloakError, List[UmaPermission]] = {
    val params = List(
      ("first", first),
      ("max", max),
      ("name", name),
      ("scope", scope),
      ("resource", Some(resourceId))
    )

    client.get[List[UmaPermission]](uri"$POLICY_ENDPOINT", createQuery(params: _*))
  }

  def findById(id: String): IO[KeycloakError, UmaPermission] = {
    client.get[UmaPermission](uri"$POLICY_ENDPOINT/$id")
  }

  def delete(id: String): IO[KeycloakError, Unit] = {
    client.delete[Unit](uri"$POLICY_ENDPOINT/$id")
  }
}
