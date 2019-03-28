package com.fullfacing.keycloak4s.monix.utilities

import monix.eval.Task
import monix.reactive.Observable
object ObservableUtils {

  val obs: ObservableExtensions.ObservableExtensions = ObservableExtensions.ObservableExtensions(Observable)

  sealed trait State
  object State {
    case object Init extends State
    case class Continue(offset: Int) extends State
  }



  /**
   * Generator functions for the `fromAsyncStateAction` on `Observable`. This function continually fetches
   * the next set of resources until a result set less than 100 is returned.
   *
   * @param source a function that fetches the next batch of resources.
   * @return the next state and element to be pushed downstream.
   */
  def fetchResources[A](source: Int => Task[Seq[A]]): State => Task[Either[Seq[A], (Seq[A], State)]] = {
    case State.Init =>
      source(0).map { resources =>
        if (resources.size >= 100) {
          Right((resources, State.Continue(resources.size)))
        } else {
          Left(resources)
        }
      }
    case State.Continue(offset) =>
      source(offset).map { resources =>
        if (resources.size >= 100) {
          Right((resources, State.Continue(offset + resources.size)))
        } else {
          Left(resources)
        }
      }
  }
}
