package com.fullfacing.keycloak4s.core.utilities

import cats.implicits._
import cats.effect.IO
import org.slf4j.Logger

object IoImplicits {
  implicit class IoImplicit[A](i: IO[A]) {
    def handleErrorWithLogging(f: Throwable => A)(implicit logger: Logger): IO[A] = {
      i.handleError { throwable =>
        logger.error("Error while executing IO.", throwable)
        f(throwable)
      }
    }
  }
}
