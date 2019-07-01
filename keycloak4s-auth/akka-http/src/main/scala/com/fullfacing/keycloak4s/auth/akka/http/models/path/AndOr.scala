package com.fullfacing.keycloak4s.auth.akka.http.models.path

import org.json4s.JsonAST.{JArray, JObject, JString, JValue}

trait AndOr

final case class And(and: List[Either[AndOr, String]]) extends AndOr
final case class Or(or: List[Either[AndOr, String]])  extends AndOr

object AndOr {

  def apply(roles: JObject): AndOr = {
    convert(roles)
  }

  def toEithers(list: List[JValue]): List[Either[AndOr, String]] = list.collect {
    case JString(s)   => Right(s)
    case obj: JObject => Left(convert(obj))
  }

  def convert(obj: JObject): AndOr = obj.obj.headOption.collect {
    case ("and", JArray(arr)) => And(toEithers(arr))
    case ("or", JArray(arr))  => Or(toEithers(arr))
  }.getOrElse(throw new Exception)
}