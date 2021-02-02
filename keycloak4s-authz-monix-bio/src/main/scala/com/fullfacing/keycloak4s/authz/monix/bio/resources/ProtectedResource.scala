package com.fullfacing.keycloak4s.authz.monix.bio.resources

import com.fullfacing.keycloak4s.admin.utils.Service._
import com.fullfacing.keycloak4s.authz.monix.bio.client.AuthzClient
import com.fullfacing.keycloak4s.core.models.{KeycloakError, Resource}
import monix.bio.IO
import sttp.client.UriContext
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq

class ProtectedResource[S]()(implicit client: AuthzClient[S]) {

  private val REGISTRATION_ENDPOINT = client.serverConfig.resource_registration_endpoint

  def create(body: Resource.Create): IO[KeycloakError, Resource] = {
    client.post[Resource](uri"$REGISTRATION_ENDPOINT", body)
  }

  def update(id: String, patch: Resource): IO[KeycloakError, Unit] = {
    client.put[Unit](uri"$REGISTRATION_ENDPOINT/$id", patch)
  }

  private def buildQuery(first: Option[Int],
                         max: Option[Int],
                         id: Option[String],
                         name: Option[String],
                         uri: Option[String],
                         owner: Option[String],
                         `type`: Option[String],
                         scope: Option[String],
                         matchingUri: Option[Boolean],
                         exactName: Option[Boolean]): Seq[KeyValue] = {
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
