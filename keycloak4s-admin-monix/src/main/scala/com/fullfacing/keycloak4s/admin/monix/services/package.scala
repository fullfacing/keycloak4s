package com.fullfacing.keycloak4s.admin.monix

import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import monix.reactive.Consumer

import scala.collection.immutable.Seq

package object services {

  def createQuery(queries: (String, Option[Any])*): Seq[KeyValue] = Seq.from {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }
  }

  def toCsvList(list: Option[List[String]]): Option[String] = list.map(_.mkString(","))

  def consumer[A](): Consumer[A, Seq[A]] = Consumer.foldLeft(Seq.empty[A])(_ :+ _)
}
