package com.fullfacing.keycloak4s.admin

import java.io.File
import java.nio.file.Files
import java.util.UUID

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Multipart, multipart}

import scala.collection.immutable.{Seq => ImmutableSeq}

package object services {

  /** Allows for implicit conversion of UUID to String in sequences. */
  type Path = ImmutableSeq[String]
  implicit def uuidToString: UUID => String = _.toString

  /** Creates a sequence of sttp KeyValues representing query parameters. */
  def createQuery(queries: (String, Option[Any])*): ImmutableSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[ImmutableSeq]
  }

  /** Creates a Multipart from a file. */
  def createMultipart(file: File): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  /** Creates a Multipart from a Map. */
  def createMultipart(formData: Map[String, String]): Multipart = multipart("form", formData)

  def toCsvList(list: Option[List[String]]): Option[String] = list.map(_.mkString(","))

  def flattenOptionMap(map: Map[String, Option[Any]]): Map[String, String] =
    map.flatMap { case (key, value) =>
      value.map(v => (key, v.toString))
    }
}
