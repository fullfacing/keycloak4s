package com.fullfacing.keycloak4s

import java.io.File
import java.nio.file.Files

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{Multipart, multipart}

import scala.collection.immutable.{Seq => iSeq}

package object services {

  def createQuery(queries: (String, Option[Any])*): iSeq[KeyValue] =
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[iSeq]

  def flattenOptionMap(map: Map[String, Option[Any]]): Map[String, String] =
    map.flatMap { case (key, value) =>
      value.map(v => (key, v.toString))
    }

  def createMultipart(file: File): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    multipart("file-part", byteArray)
  }

  def createMultipart(formData: Map[String, String]): Multipart = multipart("form", formData)
}
