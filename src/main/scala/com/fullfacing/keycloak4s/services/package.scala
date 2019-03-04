package com.fullfacing.keycloak4s

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import java.io.File
import java.nio.file.Files

import com.fullfacing.keycloak4s.models.enums.ContentType
import com.softwaremill.sttp.{ByteArrayBody, Multipart}

import scala.collection.immutable.{Seq => iSeq}

package object services {

  def createQuery(queries: (String, Option[Any])*): iSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[iSeq]
  }

  def createMultipart(file: File, contentType: ContentType): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    Multipart("file", ByteArrayBody(byteArray), contentType = Some(contentType.value))
  }
}
