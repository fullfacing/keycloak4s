package com.fullfacing.keycloak4s.admin.monix

import cats.implicits._
import com.fullfacing.keycloak4s.core.models.KeycloakError
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import monix.reactive.Consumer

import scala.collection.immutable.Seq

package object services {

  def createQuery(queries: (String, Option[Any])*): Seq[KeyValue] =
    queries.flatMap { case (key, value) =>
      value.map(v => KeyValue(key, v.toString))
    }.to[Seq]

  def toCsvList(list: Option[List[String]]): Option[String] = list.map(_.mkString(","))

  def consumer[A](): Consumer[A, Seq[A]] = Consumer.foldLeft(Seq.empty[A])(_ :+ _)
}
