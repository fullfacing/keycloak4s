package com.fullfacing.keycloak4s

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.{Seq => iSeq}

package object services {

  trait TODO extends Nothing

  def createQuery(queries: (String, Option[Any])*): iSeq[KeyValue] = {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[iSeq]
  }
}
