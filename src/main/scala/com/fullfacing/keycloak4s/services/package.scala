package com.fullfacing.keycloak4s

import java.io.File
import java.nio.file.Files

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp.{ByteArrayBody, Multipart}

import scala.collection.immutable.{Seq => iSeq}

package object services {

  def createQuery(queries: (String, Option[Any])*): iSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[iSeq]
  }

  def createMultipart(file: File, contentType: String): Multipart = {
    val byteArray = Files.readAllBytes(file.toPath)
    Multipart("file", ByteArrayBody(byteArray), contentType = Some(contentType))
  }
}
