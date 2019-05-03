package com.fullfacing.keycloak4s.monix

import java.io.File
import java.nio.file.Files

import cats.implicits._
import com.fullfacing.keycloak4s.models.KeycloakError
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Multipart, multipart}
import monix.reactive.Consumer

import scala.collection.immutable.Seq

package object services {

  def createQuery(queries: (String, Option[Any])*): Seq[KeyValue] =
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[Seq]

  def flattenOptionMap(map: Map[String, Option[Any]]): Map[String, String] =
    map.flatMap { case (key, value) =>
      value.map(v => (key, v.toString))
    }

  def createMultipart(file: File): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  def createMultipart(formData: Map[String, String]): Multipart = multipart("form", formData)

  def consumer[A](): Consumer[Either[KeycloakError, Seq[A]], Either[KeycloakError, Seq[A]]] = {
    Consumer.foldLeft(List.empty[A].asRight[KeycloakError]){ case (a, b) =>
      b.flatMap(bb => a.map(_ ++ bb))
    }
  }
}
