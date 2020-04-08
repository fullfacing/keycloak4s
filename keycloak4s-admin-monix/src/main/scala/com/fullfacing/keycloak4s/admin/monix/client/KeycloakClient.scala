package com.fullfacing.keycloak4s.admin.monix.client

import com.fullfacing.keycloak4s.admin.client.{KeycloakClient => KeycloakClientA}
import com.fullfacing.keycloak4s.core.models._
import monix.eval.Task
import monix.reactive.Observable
import sttp.client.{NothingT, SttpBackend}
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq
import scala.reflect._

class KeycloakClient[T](config: ConfigWithAuth)(implicit client: SttpBackend[Task, Observable[T], NothingT]) extends KeycloakClientA[Task, Observable[T]](config) {

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

    Observable.unfoldEval(offset)(fetchResources(call, batch, limit)).flatMap(Observable.fromIterable)
  }

  /**
   * This function continually fetches the next set of resources until a result set less than the batch size is returned.
   *
   * @param source a function that fetches the next batch of resources.
   * @return the next state and element to be pushed downstream.
   */
  def fetchResources[A](source: Int => Task[Seq[A]], batchSize: Int, limit: Int): Int => Task[Option[(Seq[A], Int)]] = { offset =>
    source(offset).map { results =>
      if (results.size >= batchSize && results.size + offset <= limit) {
        Some((results, results.size + offset))
      } else {
        None
      }
    }
  }
}