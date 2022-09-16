package com.fullfacing.keycloak4s.admin

import java.io.File
import java.nio.file.Files
import java.util.UUID

import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.KeycloakClient.Headers
import com.fullfacing.keycloak4s.core.Exceptions
import com.fullfacing.keycloak4s.core.models.KeycloakError
import sttp.client3.BasicRequestBody
import sttp.model.Part
import sttp.model.Uri.QuerySegment.KeyValue
import sttp.client3._
import scala.collection.immutable.{Seq => ImmutableSeq}
import scala.util.Try

package object services {

  /** Allows for implicit conversion of UUID to String in sequences. */
  type Path = ImmutableSeq[String]
  implicit def uuidToString: UUID => String = _.toString

  def extractString(response: Either[KeycloakError, Headers]): Either[KeycloakError, String] = response.flatMap { headers =>
    headers
      .get("Location")
      .orElse(headers.get("location"))
      .flatMap(location => location.split('/').lastOption)
      .toRight(Exceptions.ID_NOT_FOUND)
  }

  def extractUuid(response: Either[KeycloakError, Headers]): Either[KeycloakError, UUID] = extractString(response).flatMap { id =>
    Try(UUID.fromString(id))
      .fold(_ => Exceptions.ID_PARSE_FAILED.asLeft, _.asRight)
  }

  /** Creates a sequence of sttp KeyValues representing query parameters. */
  def createQuery(queries: (String, Option[Any])*): ImmutableSeq[KeyValue] = ImmutableSeq.apply(
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }: _*
  )

  /** Creates a Multipart from a file. */
  def createMultipart(file: File): Part[BasicRequestBody] = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  /** Creates a Multipart from a Map. */
  def createMultipart(formData: Map[String, String]): Part[BasicRequestBody] = multipart("form", formData)

  def toCsvList(list: Option[List[String]]): Option[String] = list.map(_.mkString(","))

  def flattenOptionMap(map: Map[String, Option[Any]]): Map[String, String] =
    map.flatMap { case (key, value) =>
      value.map(v => (key, v.toString))
    }
}
