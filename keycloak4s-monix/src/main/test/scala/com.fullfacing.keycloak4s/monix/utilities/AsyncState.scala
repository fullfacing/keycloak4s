package com.fullfacing.keycloak4s.monix.utilities

import monix.eval.Task
import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Callback, Cancelable}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.util.control.NonFatal

class AsyncState[S, A](seed: => S, f: S => Task[Either[A, (A, S)]]) extends Observable[A] {
  def unsafeSubscribeFn(subscriber: Subscriber[A]): Cancelable = {
    import subscriber.scheduler
    var streamErrors = true
    try {
      val init = seed
      streamErrors = false

      Task.defer(loop(subscriber, init))
        .executeWithOptions(_.enableAutoCancelableRunLoops)
        .runAsync(Callback.empty)
    }
    catch {
      case ex if NonFatal(ex) =>
        if (streamErrors) subscriber.onError(ex)
        else subscriber.scheduler.reportFailure(ex)
        Cancelable.empty
    }
  }

  def loop(subscriber: Subscriber[A], state: S): Task[Unit] =
    try f(state).redeemWith(
      { ex =>
        subscriber.onError(ex)
        Task.unit
      },
      {
        case Right((elem, newState)) =>
          Task.fromFuture(subscriber.onNext(elem)).flatMap {
            case Continue => loop(subscriber, newState)
            case Stop => Task.unit
          }

        case Left(elem) =>
          Task.fromFuture(subscriber.onNext(elem)).flatMap {
            case Continue => Task(subscriber.onComplete())
            case Stop => Task.unit
          }
      }
    ) catch {
      case ex if NonFatal(ex) =>
        Task.raiseError(ex)
    }
}

object ObservableExtensions {
  implicit class ObservableExtensions(val obs: Observable.type) {
    def walk[S, A](seed: => S)(f: S => Task[Either[A, (A, S)]]) =
      new AsyncState(seed, f)
  }
}