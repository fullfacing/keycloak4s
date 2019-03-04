package com.fullfacing.keycloak4s.models.enums

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable.IndexedSeq

sealed abstract class ContentType(val value: String) extends StringEnumEntry

object ContentTypes extends StringEnum[ContentType] {
  override def values: IndexedSeq[ContentType] = findValues

  case object Csv       extends ContentType("text/csv")
  case object Json      extends ContentType("application/json")
  case object Multipart extends ContentType("multipart/form-data")
  case object Pdf       extends ContentType("application/pdf")
  case object TextPlain extends ContentType("text/plain")
  case object XmlApp    extends ContentType("application/xml")
  case object XmlPlain  extends ContentType("text/xml")
}
