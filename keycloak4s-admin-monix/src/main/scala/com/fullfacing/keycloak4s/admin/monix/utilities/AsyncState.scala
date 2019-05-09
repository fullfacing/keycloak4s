package com.fullfacing.keycloak4s.admin.monix.utilities

import monix.eval.Task
import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Callback, Cancelable}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.util.control.NonFatal

/* Created by https://github.com/Executioner1939  */

class AsyncState[A, B](initial: B)(f: B => Task[Either[A, (A, B)]]) extends Observable[A] {
  def unsafeSubscribeFn(subscriber: Subscriber[A]): Cancelable = {
    import subscriber.scheduler
    var streamErrors = true
    try {
      streamErrors = false

      Task.defer(loop(subscriber, initial))
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

  def loop(subscriber: Subscriber[A], next: B): Task[Unit] =
    try f(next).redeemWith(
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