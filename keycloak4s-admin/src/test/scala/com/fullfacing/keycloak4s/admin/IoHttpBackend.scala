package com.fullfacing.keycloak4s.admin

import cats.effect.IO
import cats.implicits._
import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}

class IoHttpBackend(delegate: SttpBackend[IO, Nothing]) extends SttpBackend[IO, Nothing] {
  override def send[T](request: Request[T, Nothing]): IO[Response[T]] =
    delegate.send(request)

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[IO] = new MonadError[IO] {
    override def unit[T](t: T): IO[T] =
      IO.pure(t)

    override def map[T, T2](fa: IO[T])(f: T => T2): IO[T2] =
      fa.map(f)

    override def flatMap[T, T2](fa: IO[T])(f: T => IO[T2]): IO[T2] =
      fa.flatMap(f)

    override def error[T](t: Throwable): IO[T] =
      IO.raiseError(t)

    override protected def handleWrappedError[T](rt: IO[T])(h: PartialFunction[Throwable, IO[T]]): IO[T] =
      rt.recoverWith(h)
  }
}