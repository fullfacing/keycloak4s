package com.fullfacing.keycloak4s

import java.io.File
import java.nio.file.Files
import java.util.UUID

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Multipart, multipart}

import scala.collection.immutable.{Seq => ImmutableSeq}

package object services {

  type Path = ImmutableSeq[String]

  def createQuery(queries: (String, Option[Any])*): ImmutableSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[ImmutableSeq]
  }

  def flattenOptionMap(map: Map[String, Option[Any]]): Map[String, String] =
    map.flatMap { case (key, value) =>
      value.map(v => (key, v.toString))
    }

  def createMultipart(file: File): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  def createMultipart(formData: Map[String, String]): Multipart = multipart("form", formData)

  implicit def uuidToString: UUID => String = _.toString
}
