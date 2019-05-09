package com.fullfacing.keycloak4s.admin.client.implicits

import scala.annotation.implicitAmbiguous
import scala.reflect.Manifest

/**
 * Used to represent a generic type that cannot be a Nothing, and therefor must be specified. Carries a Manifest of the type.
 * Based on code by Piotr Tarsa from contributors.scala-lang.org
 *
 * https://contributors.scala-lang.org/t/ability-to-force-the-caller-to-specify-a-type-parameter-for-a-polymorphic-method/2116/9
 */
class Anything[T <: Any](implicit val manifest: Manifest[T])

object Anything {
  implicit def something[T : Manifest]: Anything[T] = new Anything[T]()

  @implicitAmbiguous("Generic return type must be specified.")
  implicit def nothingA: Anything[Nothing] = new Anything[Nothing]()
  implicit def nothingB: Anything[Nothing] = new Anything[Nothing]()
}
