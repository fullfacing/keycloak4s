package com.fullfacing.keycloak4s.admin.monix

import monix.reactive.Consumer
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq

package object services {

  def createQuery(queries: (String, Option[Any])*): Seq[KeyValue] = Seq {
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }:_*
  }

  def toCsvList(list: Option[List[String]]): Option[String] = list.map(_.mkString(","))

  def consumer[A](): Consumer[A, Seq[A]] = Consumer.foldLeft(Seq.empty[A])(_ :+ _)
}
