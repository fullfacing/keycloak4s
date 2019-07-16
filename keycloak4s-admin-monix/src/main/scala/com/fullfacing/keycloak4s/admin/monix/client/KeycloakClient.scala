package com.fullfacing.keycloak4s.admin.monix.client

import cats.implicits._
import com.fullfacing.keycloak4s.admin.client.{KeycloakClient => KeycloakClientA}
import com.fullfacing.keycloak4s.admin.monix.utilities.ObservableExtensions.ObservableExtensions
import com.fullfacing.keycloak4s.core.models._
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import monix.eval.Task
import monix.reactive.Observable

import scala.collection.immutable.Seq
import scala.reflect._

class KeycloakClient[T](config: KeycloakConfig)(implicit client: SttpBackend[Task, Observable[T]]) extends KeycloakClientA[Task, Observable[T]](config) {

  /**
   * Used for calls that return a sequence of items, this sequentially makes calls to retrieve and process
   * a set number of items until the limit is reached or all items have been retrieved.
   *
   * @param offset Used for pagination, skips the specified number of items.
   * @param limit  The max amount of items to return.
   * @param batch  The amount of items each call should return.
   */
  def getList[A <: Any : Manifest](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue], offset: Int = 0, limit: Int, batch: Int = 100): Observable[A] = {
    val call = { i: Int =>
      val max = if (i + batch >= limit) limit - i else batch
      val q = (query :+ KeyValue("first", s"$i")) :+ KeyValue("max", s"$max")
      get[Seq[A]](path, q).map(_.fold(throw _, res => res))
    }

    Observable.walk[Seq[A]](offset)(fetchResources(call, batch, limit)).flatMap(Observable.fromIterable)
  }

  /** Created by https://github.com/Executioner1939
   *
   * Generator functions for the `fromAsyncStateAction` on `Observable`. This function continually fetches
   * the next set of resources until a result set less than the batch size is returned.
   *
   * @param source a function that fetches the next batch of resources.
   * @return the next state and element to be pushed downstream.
   */
  def fetchResources[A](source: Int => Task[Seq[A]], batchSize: Int, limit: Int): Int => Task[Either[Seq[A], (Seq[A], Int)]] = { offset =>
    source(offset).map { resources =>
      if (resources.size >= batchSize && resources.size + offset < limit) {
        (resources, offset + resources.size).asRight
      } else {
        resources.asLeft
      }
    }
  }
}