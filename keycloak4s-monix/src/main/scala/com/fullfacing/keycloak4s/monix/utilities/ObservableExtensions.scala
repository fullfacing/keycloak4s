package com.fullfacing.keycloak4s.monix.utilities

import monix.eval.Task
import monix.reactive.Observable

object ObservableExtensions {
  implicit class ObservableExtensions(val obs: Observable.type) {
    def walk[S, A](seed: => S)(f: S => Task[Either[A, (A, S)]]) =
      new AsyncState(seed, f)
  }
}
