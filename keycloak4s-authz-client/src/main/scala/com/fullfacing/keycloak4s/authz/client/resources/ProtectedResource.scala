package com.fullfacing.keycloak4s.authz.client.resources

import com.fullfacing.keycloak4s.authz.client.AuthzClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, Resource}
import monix.bio.IO
import sttp.client.UriContext
import com.fullfacing.keycloak4s.admin.Utilities._
import sttp.model.Uri.QuerySegment.KeyValue

class ProtectedResource[S]()(implicit client: AuthzClient[S]) {

  private val REGISTRATION_ENDPOINT = client.serverConfig.registrationEndpoint

  def create(body: Resource.Create): IO[KeycloakError, Resource] = {
    client.post[Resource](uri"$REGISTRATION_ENDPOINT", body)
  }

  // TODO Check if ID required in body as well (as per PermissionResource)
  def update(id: String, patch: Resource.Update): IO[KeycloakError, Unit] = {
    client.put[Unit](uri"$REGISTRATION_ENDPOINT/$id")
  }

  private def buildQuery(first: Option[Int] = None,
                         max: Option[Int] = None,
                         id: Option[String] = None,
                         name: Option[String] = None,
                         uri: Option[String] = None,
                         owner: Option[String] = None,
                         `type`: Option[String] = None,
                         scope: Option[String] = None,
                         matchingUri: Option[Boolean] = None,
                         exactName: Option[Boolean] = None): Seq[KeyValue] = {
    createQuery(
      ("first", first),
      ("max", max),
      ("id", id),
      ("name", name),
      ("uri", uri),
      ("owner", owner),
      ("type", `type`),
      ("scope", scope),
      ("matchingUri", matchingUri),
      ("exactName", exactName)
    )
  }

  def findIds(first: Option[Int] = None,
              max: Option[Int] = None,
              id: Option[String] = None,
              name: Option[String] = None,
              uri: Option[String] = None,
              owner: Option[String] = None,
              `type`: Option[String] = None,
              scope: Option[String] = None,
              matchingUri: Option[Boolean] = None,
              exactName: Option[Boolean] = None): IO[KeycloakError, List[String]] = {

    val params = buildQuery(first, max, id, name, uri, owner, `type`, scope, matchingUri, exactName)

    client.get[List[String]](uri"$REGISTRATION_ENDPOINT", params :+ KeyValue("deep", false.toString))
  }

  def find(first: Option[Int] = None,
           max: Option[Int] = None,
           id: Option[String] = None,
           name: Option[String] = None,
           uri: Option[String] = None,
           owner: Option[String] = None,
           `type`: Option[String] = None,
           scope: Option[String] = None,
           matchingUri: Option[Boolean] = None,
           exactName: Option[Boolean] = None): IO[KeycloakError, List[Resource]] = {

    val params = buildQuery(first, max, id, name, uri, owner, `type`, scope, matchingUri, exactName)

    client.get[List[Resource]](uri"$REGISTRATION_ENDPOINT", params :+ KeyValue("deep", true.toString))
  }
}
