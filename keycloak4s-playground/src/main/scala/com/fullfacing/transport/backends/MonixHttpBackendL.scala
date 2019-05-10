package com.fullfacing.transport.backends

import java.nio.ByteBuffer

import cats.implicits._
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import monix.eval.Task
import monix.reactive.Observable

class MonixHttpBackendL(delegate: SttpBackend[Task, Observable[ByteBuffer]]) extends SttpBackend[Task, Observable[ByteBuffer]] {
  override def send[T](request: Request[T, Observable[ByteBuffer]]): Task[Response[T]] =
    delegate.send(request)

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[Task] = new MonadError[Task] {
    override def unit[T](t: T): Task[T] =
      Task.now(t)

    override def map[T, T2](fa: Task[T])(f: T => T2): Task[T2] =
      fa.map(f)

    override def flatMap[T, T2](fa: Task[T])(f: T => Task[T2]): Task[T2] =
      fa.flatMap(f)

    override def error[T](t: Throwable): Task[T] =
      Task.raiseError(t)

    override protected def handleWrappedError[T](rt: Task[T])(h: PartialFunction[Throwable, Task[T]]): Task[T] =
      rt.recoverWith(h)
  }
}