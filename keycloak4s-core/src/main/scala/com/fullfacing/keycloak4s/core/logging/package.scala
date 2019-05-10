package com.fullfacing.keycloak4s.core

import java.util.UUID

package object logging {

  val cy: String = Console.CYAN
  val gr: String = Console.GREEN
  val rs: String = Console.RESET
  val ye: String = Console.YELLOW

  def cIdLog(cId: UUID): String = s"${ye}keycloak4s - ${cy}Internal Correlation ID: $gr$cId $cy- "
}
