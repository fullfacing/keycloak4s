package com.fullfacing.transport.backends

import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.implicits._
import monix.eval.Task
import sttp.client.monad.MonadError
import sttp.client.ws.WebSocketResponse
import sttp.client.{NothingT, Request, Response, SttpBackend}

import scala.concurrent.Future

class AkkaHttpBackendL(delegate: SttpBackend[Future, Source[ByteString, Any], NothingT]) extends SttpBackend[Task, Source[ByteString, Any], NothingT] {
  override def send[T](request: Request[T, Source[ByteString, Any]]): Task[Response[T]] =
    Task.fromFuture(delegate.send(request))

  override def close(): Task[Unit] = Task.deferFuture(delegate.close())

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

  override def openWebsocket[T, WS](request: Request[T, Source[ByteString, Any]], handler: NothingT[WS]): Task[WebSocketResponse[WS]] = handler
}
