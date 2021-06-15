package com.fullfacing.keycloak4s.admin.monix.client

import com.fullfacing.keycloak4s.admin.client.{KeycloakClient => KeycloakClientA}
import com.fullfacing.keycloak4s.core.models._
import monix.eval.Task
import monix.reactive.Observable
import sttp.client3.SttpBackend
import sttp.model.Uri.QuerySegment.KeyValue

import scala.collection.immutable.Seq
import scala.reflect._

class KeycloakClient[T](config: ConfigWithAuth)(implicit client: SttpBackend[Task, Any]) extends KeycloakClientA[Task](config) {

  /**
   * Used for calls that return a sequence of items, this sequentially makes calls to retrieve and process
   * a set number of items until the limit is reached or all items have been retrieved.
   *
   * @param offset Used for pagination, skips the specified number of items.
   * @param limit  The max amount of items to return.
   * @param batch  The amount of items each call should return.
   */
  def getList[A <: Any : Manifest](path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue], offset: Int = 0, limit: Int, batch: Int = 100): Observable[A] = {
    Observable.unfoldEval(offset)(fetchResources(_, batch, limit, path, query)).flatMap(Observable.fromIterable)
  }

  /**
   * This function continually fetches the next set of resources until a result set less than the batch size is returned.
   *
   * @return the next state and element to be pushed downstream.
   */
  def fetchResources[A <: Any : Manifest](offset: Int, batchSize: Int, limit: Int, path: Seq[String], query: Seq[KeyValue] = Seq.empty[KeyValue]): Task[Option[(Seq[A], Int)]] = {
    if (offset == -1) { // Offset of -1 means we have nothing more to fetch
      Task.now(None)

    } else {
      val q = (query :+ KeyValue("first", s"$offset")) :+ KeyValue("max", s"$batchSize")
      get[Seq[A]](path, q).map(_.fold(throw _, res => res)).map { results =>

        // Result set is less than batch size
        if (results.size < batchSize) { // No more to fetch
          Some((results, -1))

        // Result set is equal to batch size
        } else if (results.size == batchSize) { // More to Fetch
          Some((results, offset + results.size))

        // Result set is empty
        } else {
          None
        }
      }
    }
  }
}