package com.fullfacing.keycloak4s.core

import java.util.UUID

package object logging {

  val cy: String = Console.CYAN
  val gr: String = Console.GREEN
  val re: String = Console.RED
  val rs: String = Console.RESET
  val ye: String = Console.YELLOW

  def cIdLog(cId: UUID): String = s"${ye}keycloak4s - ${cy}Internal Correlation ID: $gr$cId $cy- "
  def cIdErr(cId: UUID): String = s"keycloak4s - Internal Correlation ID: $cId - "
}
