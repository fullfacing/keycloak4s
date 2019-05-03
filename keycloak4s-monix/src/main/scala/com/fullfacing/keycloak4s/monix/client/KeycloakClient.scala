package com.fullfacing.keycloak4s.monix.client

import java.nio.ByteBuffer

import cats.implicits._
import com.fullfacing.keycloak4s.client.{KeycloakConfig, KeycloakClient => KeycloakClientA}
import com.fullfacing.keycloak4s.models.KeycloakError
import com.fullfacing.keycloak4s.monix.utilities.ObservableExtensions.ObservableExtensions
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq
import scala.reflect._

class KeycloakClient(config: KeycloakConfig)(implicit client: SttpBackend[Task, Observable[ByteBuffer]]) extends KeycloakClientA[Task, Observable[ByteBuffer]](config) {

  type EitherSeqA[A] = Either[KeycloakError, Seq[A]]

  /**
   *
   *
   * @param path
   * @param query
   * @param offset
   * @param batch
   * @tparam A
   * @return
   */
  def getList[A <: Any : Manifest](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue], offset: Int = 0, limit: Int, batch: Int = 100): Observable[EitherSeqA[A]] = {
    val call = { i: Int =>
      val max = if (i + batch >= limit) limit - i else batch
      val q = (query :+ KeyValue("first", s"$i")) :+ KeyValue("max", s"$max")
      get[Seq[A]](path, q)
    }

    Observable.walk[EitherSeqA[A]](offset)(fetchResources(call, batch, limit))
  }

  /** Created by https://github.com/Executioner1939
   *
   * Generator functions for the `fromAsyncStateAction` on `Observable`. This function continually fetches
   * the next set of resources until a result set less than the batch size is returned.
   *
   * @param source a function that fetches the next batch of resources.
   * @return the next state and element to be pushed downstream.
   */
  def fetchResources[A](source: Int => Task[EitherSeqA[A]], batchSize: Int, limit: Int): Int => Task[Either[EitherSeqA[A], (EitherSeqA[A], Int)]] = { offset =>
    source(offset).map {
      case Right(resources) =>
        if (resources.size >= batchSize && resources.size + offset < limit) {
          (resources.asRight, offset + resources.size).asRight
        } else {
          resources.asRight.asLeft
        }
      case left => left.asLeft
    }
  }
}
