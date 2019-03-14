package com.fullfacing.keycloak4s

import java.io.File
import java.nio.file.Files

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Multipart, multipart}

import scala.collection.immutable.{Seq => iSeq}

package object services {

  def createQuery(queries: (String, Option[Any])*): iSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[iSeq]
  }

  def createdUrlEncodedMap(queries: (String, Option[Any])*): Map[String, String] = {
    queries.flatMap { case (key, value) =>
      value.map(v => Map(key -> v.toString))
    }.headOption
      .getOrElse(Map.empty[String, String])
  }

  def createMultipart(file: File): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  def createMultipart(formData: Map[String, String]): Multipart = {
    multipart("form", formData)
  }
}
