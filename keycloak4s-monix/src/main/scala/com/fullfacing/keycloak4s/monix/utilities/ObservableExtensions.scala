package com.fullfacing.keycloak4s.monix.utilities

import monix.eval.Task
import monix.reactive.Observable

object ObservableExtensions {
  implicit class ObservableExtensions(val obs: Observable.type) {
    def walk[A](initial: Int)(f: Int => Task[Either[A, (A, Int)]]) =
      new AsyncState(initial)(f)
  }
}
